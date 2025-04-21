package service;

import entities.Bid;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BidService implements IService<Bid>{

    private Connection connection;

    @Override
    public boolean ajouter(Bid bid) throws SQLException {
        String query = "INSERT INTO bid (amount, time, auctionId, userId) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setDouble(1, bid.getAmount());
            statement.setString(2, bid.getTime());
            statement.setInt(3, bid.getAuctionId());
            statement.setInt(4, bid.getUserId());
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
}
