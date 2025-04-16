package service;

import entities.User;
import utils.DataSource;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;



public class UserService implements IService<User> {

    private DataSource ConnectionManager;

    private final Connection connection = DataSource.getInstance().getConnection();

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
    public void ajouter(User user) {
        String query = "INSERT INTO user (last_name, email, password) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getFirstName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPassword());
            statement.executeUpdate();
            System.out.println("User added successfully.");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add user: " + e.getMessage());
        }
    }

    @Override
    public void modifier(User user) {
        String query = "UPDATE user SET last_name = ?, email = ?, password = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getFirstName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPassword());
            statement.setInt(4, user.getId());
            int rows = statement.executeUpdate();
            if (rows > 0) {
                System.out.println("User updated successfully.");
            } else {
                System.out.println("No User found with ID: " + user.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user: " + e.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {
        String query = "DELETE FROM user WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            int rows = statement.executeUpdate();
            if (rows > 0) {
                System.out.println("User deleted successfully.");
            } else {
                System.out.println("No User found with ID: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete User: " + e.getMessage());
        }
    }

    @Override
    public List<User> recuperer() throws SQLException {
        List<User> users = new ArrayList<>();

        String sql = "SELECT id, first_name, email, role FROM user"; // adjust column names if needed

        try (Connection conn = DataSource.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setFirstName(rs.getString("first_name"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getInt("role"));

                users.add(user);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

}


