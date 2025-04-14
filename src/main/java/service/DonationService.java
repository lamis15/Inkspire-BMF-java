package service;

import entities.Collections;
import entities.Donation;
import entities.User;
import utils.DataSource;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DonationService implements IService<Donation> {

    private Connection connection;

    public DonationService() {
        connection = DataSource.getInstance().getConnection();
    }

    @Override
    public void ajouter(Donation donation) throws SQLException {
        String sql = "INSERT INTO donation (date, amount, collections_id, user_id) VALUES (?, ?, ?, ?)";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setTimestamp(1, Timestamp.valueOf(donation.getDate()));
        preparedStatement.setDouble(2, donation.getAmount());
        preparedStatement.setInt(3, donation.getCollections().getId());
        preparedStatement.setInt(4, donation.getUser().getId());

        preparedStatement.executeUpdate();
    }

    @Override
    public void modifier(Donation donation) throws SQLException {
        String sql = "UPDATE donation SET date=?, amount=?, collections_id=?, user_id=? WHERE id=?";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setTimestamp(1, Timestamp.valueOf(donation.getDate()));
        preparedStatement.setDouble(2, donation.getAmount());
        preparedStatement.setInt(3, donation.getCollections().getId());
        preparedStatement.setInt(4, donation.getUser().getId());
        preparedStatement.setInt(5, donation.getId());

        preparedStatement.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM donation WHERE id=?";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, id);
        preparedStatement.executeUpdate();
    }

    @Override
    public List<Donation> recuperer() throws SQLException {
        String sql = "SELECT d.id, d.date, d.amount, d.collections_id, d.user_id, c.title as collection_title FROM donation d " +
                     "JOIN collections c ON d.collections_id = c.id";
        Statement statement = connection.createStatement();

        ResultSet rs = statement.executeQuery(sql);
        List<Donation> list = new ArrayList<>();

        while (rs.next()) {
            Donation d = new Donation();
            d.setId(rs.getInt("id"));
            d.setDate(rs.getTimestamp("date").toLocalDateTime());
            d.setAmount(rs.getDouble("amount"));

            // Set collection
            Collections collection = new Collections();
            collection.setId(rs.getInt("collections_id"));
            collection.setTitle(rs.getString("collection_title"));
            d.setCollections(collection);

            // Set user
            User user = new User();
            user.setId(rs.getInt("user_id"));
            d.setUser(user);

            list.add(d);
        }
        return list;
    }
    
    /**
     * Get donations by collection ID
     * @param collectionId The collection ID to filter by
     * @return List of donations for the collection
     */
    public List<Donation> getDonationsByCollectionId(int collectionId) throws SQLException {
        String sql = "SELECT d.id, d.date, d.amount, d.collections_id, d.user_id, u.first_name, u.last_name FROM donation d " +
                     "JOIN user u ON d.user_id = u.id " +
                     "WHERE d.collections_id = ?";
        
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, collectionId);
        
        ResultSet rs = preparedStatement.executeQuery();
        List<Donation> list = new ArrayList<>();

        while (rs.next()) {
            Donation d = new Donation();
            d.setId(rs.getInt("id"));
            d.setDate(rs.getTimestamp("date").toLocalDateTime());
            d.setAmount(rs.getDouble("amount"));

            // Set collection
            Collections collection = new Collections();
            collection.setId(collectionId);
            d.setCollections(collection);

            // Set user
            User user = new User();
            user.setId(rs.getInt("user_id"));
            user.setFirstName(rs.getString("first_name"));
            user.setLastName(rs.getString("last_name"));
            d.setUser(user);

            list.add(d);
        }
        return list;
    }
    
    /**
     * Get donations by user ID
     * @param userId The user ID to filter by
     * @return List of donations made by the user
     */
    public List<Donation> getDonationsByUserId(int userId) throws SQLException {
        String sql = "SELECT d.id, d.date, d.amount, d.collections_id, d.user_id, c.title as collection_title FROM donation d " +
                     "JOIN collections c ON d.collections_id = c.id " +
                     "WHERE d.user_id = ?";
        
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, userId);
        
        ResultSet rs = preparedStatement.executeQuery();
        List<Donation> list = new ArrayList<>();

        while (rs.next()) {
            Donation d = new Donation();
            d.setId(rs.getInt("id"));
            d.setDate(rs.getTimestamp("date").toLocalDateTime());
            d.setAmount(rs.getDouble("amount"));

            // Set collection
            Collections collection = new Collections();
            collection.setId(rs.getInt("collections_id"));
            collection.setTitle(rs.getString("collection_title"));
            d.setCollections(collection);

            // Set user
            User user = new User();
            user.setId(userId);
            d.setUser(user);

            list.add(d);
        }
        return list;
    }
    
    /**
     * Calculate total donations for a collection
     * @param collectionId The collection ID
     * @return Total amount donated
     */
    public double getTotalDonationsForCollection(int collectionId) throws SQLException {
        String sql = "SELECT SUM(amount) as total FROM donation WHERE collections_id = ?";
        
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, collectionId);
        
        ResultSet rs = preparedStatement.executeQuery();
        if (rs.next()) {
            return rs.getDouble("total");
        }
        return 0.0;
    }
}
