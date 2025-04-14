package service;

import entities.User;
import utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IService<User> {

    private Connection connection;

    public UserService() {
        connection = DataSource.getInstance().getConnection();
    }

    @Override
    public void ajouter(User user) throws SQLException {
        String sql = "INSERT INTO user (first_name, last_name, email, password, role, status, tokens) VALUES (?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, user.getFirstName());
        preparedStatement.setString(2, user.getLastName());
        preparedStatement.setString(3, user.getEmail());
        preparedStatement.setString(4, user.getPassword());
        preparedStatement.setInt(5, user.getRole());
        preparedStatement.setInt(6, user.getStatus());
        preparedStatement.setInt(7, user.getTokens());

        preparedStatement.executeUpdate();
    }

    @Override
    public void modifier(User user) throws SQLException {
        String sql = "UPDATE user SET first_name=?, last_name=?, email=?, password=?, role=?, status=?, tokens=? WHERE id=?";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, user.getFirstName());
        preparedStatement.setString(2, user.getLastName());
        preparedStatement.setString(3, user.getEmail());
        preparedStatement.setString(4, user.getPassword());
        preparedStatement.setInt(5, user.getRole());
        preparedStatement.setInt(6, user.getStatus());
        preparedStatement.setInt(7, user.getTokens());
        preparedStatement.setInt(8, user.getId());

        preparedStatement.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM user WHERE id=?";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, id);
        preparedStatement.executeUpdate();
    }

    @Override
    public List<User> recuperer() throws SQLException {
        String sql = "SELECT * FROM user";
        Statement statement = connection.createStatement();

        ResultSet rs = statement.executeQuery(sql);
        List<User> list = new ArrayList<>();

        while (rs.next()) {
            User u = new User();
            u.setId(rs.getInt("id"));
            u.setFirstName(rs.getString("first_name"));
            u.setLastName(rs.getString("last_name"));
            u.setEmail(rs.getString("email"));
            u.setPassword(rs.getString("password"));
            u.setRole(rs.getInt("role"));
            u.setStatus(rs.getInt("status"));
            u.setTokens(rs.getInt("tokens"));
            
            // Optional fields
            try {
                u.setAddress(rs.getString("address"));
                u.setBio(rs.getString("bio"));
                u.setPicture(rs.getString("picture"));
                u.setPhoneNumber(rs.getInt("phone_number"));
                u.setGoogleAuthenticatorSecret(rs.getString("google_authenticator_secret"));
            } catch (SQLException e) {
                // Ignore if some columns don't exist
            }

            list.add(u);
        }
        return list;
    }
    
    /**
     * Get a user by ID
     * @param id The user ID
     * @return The user object or null if not found
     */
    public User getById(int id) throws SQLException {
        String sql = "SELECT * FROM user WHERE id=?";
        
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, id);
        
        ResultSet rs = preparedStatement.executeQuery();
        if (rs.next()) {
            User u = new User();
            u.setId(rs.getInt("id"));
            u.setFirstName(rs.getString("first_name"));
            u.setLastName(rs.getString("last_name"));
            u.setEmail(rs.getString("email"));
            u.setPassword(rs.getString("password"));
            u.setRole(rs.getInt("role"));
            u.setStatus(rs.getInt("status"));
            u.setTokens(rs.getInt("tokens"));
            
            // Optional fields
            try {
                u.setAddress(rs.getString("address"));
                u.setBio(rs.getString("bio"));
                u.setPicture(rs.getString("picture"));
                u.setPhoneNumber(rs.getInt("phone_number"));
                u.setGoogleAuthenticatorSecret(rs.getString("google_authenticator_secret"));
            } catch (SQLException e) {
                // Ignore if some columns don't exist
            }
            
            return u;
        }
        return null;
    }
    
    /**
     * Update a user's token balance
     * @param userId The user ID
     * @param amount The amount to add (positive) or subtract (negative)
     * @return true if successful, false otherwise
     */
    public boolean updateTokens(int userId, int amount) throws SQLException {
        // First get the current token balance
        User user = getById(userId);
        if (user == null) {
            return false;
        }
        
        // Calculate new token balance
        int currentTokens = user.getTokens() != null ? user.getTokens() : 0;
        int newTokens = currentTokens + amount;
        
        // Don't allow negative balance
        if (newTokens < 0) {
            return false;
        }
        
        // Update the token balance
        String sql = "UPDATE user SET tokens=? WHERE id=?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, newTokens);
        preparedStatement.setInt(2, userId);
        
        int rowsAffected = preparedStatement.executeUpdate();
        return rowsAffected > 0;
    }
    
    /**
     * Transfer tokens from one user to another
     * @param fromUserId The user ID to transfer from
     * @param toUserId The user ID to transfer to
     * @param amount The amount to transfer
     * @return true if successful, false otherwise
     */
    public boolean transferTokens(int fromUserId, int toUserId, int amount) throws SQLException {
        if (amount <= 0) {
            return false; // Can't transfer zero or negative amounts
        }
        
        // Get the sender's current token balance
        User fromUser = getById(fromUserId);
        if (fromUser == null) {
            return false;
        }
        
        int currentTokens = fromUser.getTokens() != null ? fromUser.getTokens() : 0;
        
        // Check if the sender has enough tokens
        if (currentTokens < amount) {
            return false;
        }
        
        // Start a transaction
        connection.setAutoCommit(false);
        
        try {
            // Subtract from sender
            boolean senderUpdated = updateTokens(fromUserId, -amount);
            if (!senderUpdated) {
                connection.rollback();
                return false;
            }
            
            // Add to receiver
            boolean receiverUpdated = updateTokens(toUserId, amount);
            if (!receiverUpdated) {
                connection.rollback();
                return false;
            }
            
            // Commit the transaction
            connection.commit();
            return true;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
}
