package service;

import entities.Auction;
import entities.Bid;
import utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BidService implements IService<Bid>{

    private final Connection connection = DataSource.getInstance().getConnection();

    @Override
    public boolean ajouter(Bid bid) throws SQLException {
        String query = "INSERT INTO bid (amount, time, auction_id, user_id, location) VALUES (?, ?, ?, ? ,?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setDouble(1, bid.getAmount());
            statement.setString(2, bid.getTime());
            statement.setInt(3, bid.getAuctionId());
            statement.setInt(4, bid.getUserId());
            statement.setString(5, bid.getLocation());
            statement.executeUpdate();
            System.out.println("Bid added successfully");
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add bid: " + e.getMessage());
        }
    }
    @Override
    public boolean modifier(Bid bid) throws SQLException {
        String query = "UPDATE bid SET amount = ?, time = ?, auction_id = ?, user_id = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setDouble(1, bid.getAmount());
            statement.setString(2, bid.getTime());
            statement.setInt(3, bid.getAuctionId());
            statement.setInt(4, bid.getUserId());
            statement.setInt(5, bid.getId());
            int rows = statement.executeUpdate();
            if (rows > 0) {
                System.out.println("Bid updated successfully.");
                return true;
            } else {
                System.out.println("No bid found with ID: " + bid.getId());
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update bid: " + e.getMessage());
        }
    }
    @Override
    public boolean supprimer(int id) throws SQLException {
        String query = "DELETE FROM bid WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            int rows = statement.executeUpdate();
            if (rows > 0) {
                System.out.println("Bid deleted successfully.");
                return true;
            } else {
                System.out.println("No bid found with ID: " + id);
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete bid: " + e.getMessage());
        }
    }

    @Override
    public List<Bid> recuperer() throws SQLException {
        List<Bid> bids = new ArrayList<>();
        String query = "SELECT * FROM bid";
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(query);
            while (result.next()) {
                Bid bid = new Bid(
                        result.getDouble("amount"),
                        result.getString("time"),
                        result.getInt("auction_id"),
                        result.getInt("user_id")
                );
                bids.add(bid);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return bids;
    }
    public List<Bid> recupererParUtilisateur(int userId) throws SQLException {
        List<Bid> bids = new ArrayList<>();
        String query = "SELECT * FROM bid WHERE user_id = ?";

        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, userId);
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                Bid bid = new Bid(
                        result.getDouble("amount"),
                        result.getString("time"),
                        result.getInt("auction_id"),
                        result.getInt("user_id")
                );
                bid.setId(result.getInt("id")); // if needed
                bids.add(bid);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return bids;
    }
    public Bid returnHighestBid(Auction auction) throws SQLException {
        String query = "SELECT * FROM bid WHERE auction_id = ? ORDER BY amount DESC LIMIT 1";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, auction.getId());

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Bid highestBid = new Bid();
            highestBid.setId(rs.getInt("id"));
            highestBid.setAuctionId(rs.getInt("auction_id"));
            highestBid.setUserId(rs.getInt("user_id"));
            highestBid.setAmount(rs.getDouble("amount"));
            return highestBid;
        }
        return null;
    }


}
