package service;

import entities.Artwork;
import entities.Auction;
import entities.Session;
import entities.User;
import utils.DataSource;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import java.time.*;

public class AuctionService implements IService<Auction> {

    private final Connection connection = DataSource.getInstance().getConnection();
    private final User session = Session.getCurrentUser();

    @Override
    public boolean ajouter(Auction auction) throws SQLException {
        String query = "INSERT INTO auction (label, start_date, end_date, start_price, end_price, status, artwork_id) VALUES (?, ?, ?, ?, ?, ?, ?)" ;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, auction.getLabel());
            statement.setString(2, auction.getStartDate());
            statement.setString(3, auction.getEndDate());
            statement.setDouble(4, auction.getStartPrice());
            statement.setDouble(5, auction.getEndPrice());
            statement.setString(6, auction.getStatus());
            statement.setInt(7, auction.getArtworkId());
            statement.executeUpdate();
            System.out.println("Auction added successfully");
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add auction: " + e.getMessage());
        }
    }

    @Override
    public boolean modifier(Auction auction) throws SQLException {
        String query = "UPDATE auction SET label = ?, start_date = ?, end_date = ?, start_price = ?, end_price = ?, status = ?, artwork_id = ? WHERE id = ?" ;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, auction.getLabel());
            statement.setString(2, auction.getStartDate());
            statement.setString(3, auction.getEndDate());
            statement.setDouble(4, auction.getStartPrice());
            statement.setDouble(5, auction.getEndPrice());
            statement.setString(6, auction.getStatus());
            statement.setInt(7, auction.getArtworkId());
            statement.setInt(8, auction.getId());
            int rows = statement.executeUpdate();
            if (rows > 0) {
                System.out.println("Auction updated successfully.");
                return true;
            } else {
                System.out.println("No auction found with ID: " + auction.getId());
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update auction: " + e.getMessage());
        }
    }

    @Override
    public boolean supprimer(int id) throws SQLException {
        String query = "DELETE FROM auction WHERE id = ?" ;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            int rows = statement.executeUpdate();
            if (rows > 0) {
                System.out.println("Auction deleted successfully.");
                return true;
            } else {
                System.out.println("No auction found with ID: " + id);
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete auction: " + e.getMessage());
        }
    }

    @Override
    public List<Auction> recuperer() throws SQLException {
        List<Auction> auctions = new ArrayList<>();
        String query = "SELECT * FROM auction" ;
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery(query);
        while (result.next()) {
            Auction auction = new Auction(
                    result.getString("label"),
                    result.getString("start_date"),
                    result.getString("end_date"),
                    result.getDouble("start_price"),
                    result.getDouble("end_price"),
                    result.getString("status"),
                    result.getInt("artwork_id")
            );
            auction.setId(result.getInt("id"));
            auctions.add(auction);
        }
        return auctions;
    }

    public List<Auction> recupererMonAuctions(int id) throws SQLException {
        List<Auction> auctions = new ArrayList<>();
        String query = "SELECT a.* FROM auction a JOIN artwork aw ON a.artwork_id = aw.id WHERE aw.user_id = ?" ;
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, id);
        // Execute the prepared statement without re-passing the query string
        ResultSet result = statement.executeQuery(); // Removed the 'query' parameter here
        while (result.next()) {
            Auction auction = new Auction(
                    result.getString("label"),
                    result.getString("start_date"),
                    result.getString("end_date"),
                    result.getDouble("start_price"),
                    result.getDouble("end_price"),
                    result.getString("status"),
                    result.getInt("artwork_id")
            );
            auction.setId(result.getInt("id"));
            auctions.add(auction);
        }
        return auctions;
    }
    public List<Artwork> getAvailableArtworksByUser(int userId) throws SQLException {
        List<Artwork> artworks = new ArrayList<>();
        String query = """
        SELECT * FROM artwork a 
        WHERE a.user_id = ? 
        AND NOT EXISTS (
            SELECT 1 FROM auction au 
            WHERE au.artwork_id = a.id
        )
        """;

        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, userId);
        ResultSet result = statement.executeQuery();

        while (result.next()) {
            Artwork artwork = new Artwork();
            artwork.setId(result.getInt("id"));
            artwork.setName(result.getString("name"));
            artworks.add(artwork);
        }
        System.out.println(artworks.size());
        return artworks;
    }
    public Artwork getArtworkById(int artworkId) throws SQLException {
        Artwork artwork = null;
        String query = "SELECT * FROM artwork WHERE id = ?";

        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, artworkId);
        ResultSet result = statement.executeQuery();

        if (result.next()) {
            artwork = new Artwork();
            artwork.setPicture(result.getString("picture"));
            artwork.setName(result.getString("name"));
        }
        return artwork;
    }
    public int getUserByArtworkId(int artworkId) throws SQLException {
        int id  = 0;
        String query = "SELECT user_id FROM artwork WHERE id = ?";

        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, artworkId);
        ResultSet result = statement.executeQuery();

        if (result.next()) {
            id = result.getInt("user_id");
        }
        return id;
    }

    public Auction getAuctionById(int id) throws SQLException {
        String query = "SELECT * FROM auction WHERE id = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, id);
        ResultSet result = statement.executeQuery();

        if (result.next()) {
            Auction auction = new Auction(
                    result.getString("label"),
                    result.getString("start_date"),
                    result.getString("end_date"),
                    result.getDouble("start_price"),
                    result.getDouble("end_price"),
                    result.getString("status"),
                    result.getInt("artwork_id")
            );
            auction.setId(result.getInt("id"));
            return auction;
        }
        return null;
    }
    public boolean isMine(Auction auction) throws SQLException {
        int currentUserId = Session.getCurrentUser().getId();

        String query = """
        SELECT COUNT(*)
        FROM auction a
        JOIN artwork aw ON a.artwork_id = aw.id
        WHERE a.id = ? AND aw.user_id = ?
    """;
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, auction.getId());
        statement.setInt(2, currentUserId);

        ResultSet result = statement.executeQuery();
        if (result.next()) {
            return result.getInt(1) > 0;
        }
        return false;
    }

    public String getRemainingTime(Auction auction) throws SQLException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime endDateTime = LocalDateTime.parse(auction.getEndDate(), formatter);
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(endDateTime)) {
            auction.setStatus("ended");
            this.modifier(auction);
            return "auction "+auction.getStatus();
        }
        Duration duration = Duration.between(now, endDateTime);

        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;

        return String.format("%d days,%d hours,%d minutes", days, hours, minutes);
    }
}

