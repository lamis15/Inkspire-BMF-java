package service;

import entities.Auction;
import utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuctionService implements IService<Auction> {

    private final Connection connection = DataSource.getInstance().getConnection();

    @Override
    public boolean ajouter(Auction auction) throws SQLException {
        String query = "INSERT INTO auction (label, start_date, end_date, start_price, end_price, status, artwork_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
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
        String query = "UPDATE auction SET label = ?, start_date = ?, end_date = ?, start_price = ?, end_price = ?, status = ?, artwork_id = ? WHERE id = ?";
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
        String query = "DELETE FROM auction WHERE id = ?";
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
        String query = "SELECT * FROM auction";
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery(query);
        while(result.next()){
            Auction auction = new Auction(
                    result.getString("label"),
                    result.getString("start_date"),
                    result.getString("end_date"),
                    result.getDouble("start_price"),
                    result.getDouble("end_price"),
                    result.getString("status"),
                    result.getInt("artwork_id")
            );
            auctions.add(auction);
        }
        return auctions;
    }
    public List<Auction> recupererMonAuctions(int id) throws SQLException {
        List<Auction> auctions = new ArrayList<>();
        String query = "SELECT a.* FROM auction a JOIN artwork aw ON a.artwork_id = aw.id WHERE aw.user_id = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, id);
        // Execute the prepared statement without re-passing the query string
        ResultSet result = statement.executeQuery(); // Removed the 'query' parameter here
        while(result.next()){
            Auction auction = new Auction(
                    result.getString("label"),
                    result.getString("start_date"),
                    result.getString("end_date"),
                    result.getDouble("start_price"),
                    result.getDouble("end_price"),
                    result.getString("status"),
                    result.getInt("artwork_id")
            );
            auctions.add(auction);
        }
        return auctions;
    }
}
