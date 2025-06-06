package service;

import entities.User;
import org.mindrot.jbcrypt.BCrypt;
import utils.DataSource;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;



public class UserService implements IService<User> {

    private Connection connection;

    public UserService() {
        connection = DataSource.getInstance().getConnection();
    }
    /*
    @Override
    public List<User> list() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user";
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(query);
            while (result.next()) {
                User user = new User(
                        result.getString("last_name"),
                        result.getString("email"),
                        result.getString("password")
                );
                users.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return users;
    }
    */

    @Override
    public boolean ajouter(User user) {
        String query = "INSERT INTO user (first_name, last_name, email, password, role, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getFirstName());
            statement.setString(2, user.getLastName());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPassword());
            statement.setInt(5, user.getRole());   // e.g. 0 = admin, 1 = user
            statement.setInt(6, user.getStatus()); // status from your model
            statement.executeUpdate();
            System.out.println("User added successfully.");
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add user: " + e.getMessage());
        }
    }



    @Override
    public boolean modifier(User user) {
        String query = "UPDATE user SET first_name = ?, last_name = ?, email = ?, password = ?, picture = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getFirstName());
            statement.setString(2, user.getLastName());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPassword());
            statement.setString(5, user.getPicture());
            statement.setInt(6, user.getId());

            int rows = statement.executeUpdate();
            if (rows > 0) {
                System.out.println("User updated successfully.");
                return true;
            } else {
                System.out.println("No User found with ID: " + user.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user: " + e.getMessage());
        }
        return false;
    }



    @Override
    public boolean supprimer(int id) {
        String query = "DELETE FROM user WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            int rows = statement.executeUpdate();
            if (rows > 0) {
                System.out.println("User deleted successfully.");
            } else {
                System.out.println("No User found with ID: " + id);
            }
            return  true ;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete User: " + e.getMessage());
        }
    }

    @Override
    public List<User> recuperer() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, first_name, email, role ,  picture FROM user";

        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);

        while (rs.next()) {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setFirstName(rs.getString("first_name"));
            user.setEmail(rs.getString("email"));
            user.setRole(rs.getInt("role"));
            user.setPicture(rs.getString("picture"));

            users.add(user);
        }
        return users;
    }



    public User checkUser(String email, String password) {
        String query = "SELECT * FROM user WHERE email = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String hashedPassword = resultSet.getString("password");
                    hashedPassword = hashedPassword.replace("$2y$", "$2a$");

                    if (BCrypt.checkpw(password, hashedPassword) && resultSet.getInt("status") == 1) {
                        User user = new User();
                        user.setId(resultSet.getInt("id"));
                        user.setFirstName(resultSet.getString("first_name"));
                        user.setLastName(resultSet.getString("last_name"));
                        user.setAddress(resultSet.getString("address"));
                        user.setEmail(resultSet.getString("email"));
                        user.setPassword(hashedPassword); // Optional: you can skip setting it
                        user.setBio(resultSet.getString("bio"));
                        user.setTokens(resultSet.getInt("tokens"));
                        user.setPicture(resultSet.getString("picture"));
                        user.setGoogleAuthenticatorSecret(resultSet.getString("google_authenticator_secret"));
                        user.setRole(resultSet.getInt("role"));
                        user.setStatus(resultSet.getInt("status"));
                        user.setPhoneNumber(resultSet.getInt("phone_number"));

                        return user;
                    } else {
                        return null;
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check user: " + e.getMessage());
        }

        return null;
    }


    public Integer GetUserId(String email) {
        String query = "SELECT id FROM user WHERE email = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    public boolean emailExists(String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM user WHERE email = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }


    public void SaveToken(String email , String token) throws SQLException {
        String query = "Update user SET  email = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);

        }catch(SQLException e) {
            throw new RuntimeException("Failed to save token");
        }
    }


    public User getUserByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM user WHERE email = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, email);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setFirstName(rs.getString("first_name"));
                    user.setLastName(rs.getString("last_name"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password"));
                    user.setRole(rs.getInt("role"));
                    user.setStatus(rs.getInt("status"));
                    user.setTokens(rs.getInt("tokens"));

                    // Optional fields - handle potential null values
                    try {
                        user.setAddress(rs.getString("address"));
                        user.setBio(rs.getString("bio"));
                        user.setPicture(rs.getString("picture"));
                        user.setPhoneNumber(rs.getInt("phone_number"));
                        user.setGoogleAuthenticatorSecret(rs.getString("google_authenticator_secret"));
                    } catch (SQLException e) {
                        // Ignore if some columns don't exist
                    }

                    return user;
                }
            }
        }

        return null;
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

    public boolean banUser(String email) throws SQLException {
        String query = "UPDATE user SET status = 0 WHERE email = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Successfully banned user with email: " + email);
                return true;
            } else {
                System.out.println("No user found with email: " + email);
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error banning user: " + e.getMessage());
            throw e;
        }
    }


    public boolean unbanUser(String email) throws SQLException {
        String query = "UPDATE user SET status = 1 WHERE email = ? AND status = 0";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Successfully unbanned user with email: " + email);
                return true;
            } else {
                System.out.println("No banned user found with email: " + email);
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error unbanning user: " + e.getMessage());
            throw e;
        }
    }


    public List<User> searchUsers(String searchText) throws SQLException {
        List<User> users = new ArrayList<>();

        if (searchText == null || searchText.trim().isEmpty()) {
            return recuperer(); // Return all users if search is empty
        }

        String query = "SELECT id, first_name, email, role, picture, status FROM user " +
                "WHERE LOWER(first_name) LIKE ? OR LOWER(email) LIKE ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            String searchPattern = "%" + searchText.toLowerCase() + "%";
            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setFirstName(rs.getString("first_name"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getInt("role"));
                    user.setPicture(rs.getString("picture"));
                    user.setStatus(rs.getInt("status"));
                    users.add(user);
                }
            }
        }

        return users;
    }

}