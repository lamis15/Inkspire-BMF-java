package service;

import entities.Comment;
import entities.User;
import entities.Artwork;
import utils.DataSource;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommentService {

    private Connection connection;
    private UserService userService;  // Injecting UserService

    public CommentService() {
        connection = DataSource.getInstance().getConnection();
        this.userService = new UserService();  // Initialize UserService
    }

    // Add a comment to an artwork
    public boolean addComment(Comment comment) {
        String sql = "INSERT INTO comment (user_id, artwork_id, content, posted_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, comment.getUser().getId());  // Use 1 as the index for user_id
            ps.setInt(2, comment.getArtwork().getId());  // Use 2 as the index for artwork_id
            ps.setString(3, comment.getContent());  // Use 3 as the index for content
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));  // Set the current time for posted_at

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error while adding comment: " + e.getMessage());
            e.printStackTrace();  // To log the error details
            return false;
        }
    }


    // Get all comments for a specific artwork
    public List<Comment> getCommentsByArtwork(Artwork artwork) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT * FROM comment WHERE artwork_id = ?";

        // Ensure the artwork is not null
        if (artwork == null) {
            System.out.println("Artwork object is null");
            return comments;
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, artwork.getId());  // Set the artwork ID

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int userId = rs.getInt("user_id");
                    User user = userService.getById(userId);  // Fetch user details by user ID

                    // Create Comment object
                    Comment comment = new Comment(
                            rs.getInt("id"),    // Comment ID
                            user,               // User object
                            artwork,            // Artwork object
                            rs.getString("content") // Comment content
                    );
                    comments.add(comment);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error while retrieving comments: " + e.getMessage());
            e.printStackTrace();
        }
        return comments;
    }

    // Delete a comment by its ID
    public boolean deleteComment(int commentId) {
        String sql = "DELETE FROM comment WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, commentId);  // Set the comment ID to delete
            return ps.executeUpdate() > 0;  // Execute delete and check if it was successful
        } catch (SQLException e) {
            System.err.println("Error while deleting comment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Edit the content of a comment
    public boolean editComment(int commentId, String newContent) {
        String sql = "UPDATE comment SET content = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newContent);  // Set the new content
            ps.setInt(2, commentId);  // Set the comment ID

            int rowsAffected = ps.executeUpdate();  // Execute update query
            return rowsAffected > 0;  // If rowsAffected is greater than 0, the update was successful
        } catch (SQLException e) {
            System.err.println("Error while editing comment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public static int getCommentCountByArtwork(int artworkId) throws SQLException {
        Connection connection = DataSource.getInstance().getConnection();
        String sql = "SELECT COUNT(*) FROM comment WHERE artwork_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, artworkId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }


}
