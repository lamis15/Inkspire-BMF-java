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
    private UserService userService;

    public CommentService() {
        connection = DataSource.getInstance().getConnection();
        this.userService = new UserService();
    }


    public boolean addComment(Comment comment) {
        String sql = "INSERT INTO comment (user_id, artwork_id, content, posted_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, comment.getUser().getId());
            ps.setInt(2, comment.getArtwork().getId());
            ps.setString(3, comment.getContent());
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error while adding comment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    public List<Comment> getCommentsByArtwork(Artwork artwork) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT * FROM comment WHERE artwork_id = ?";

        if (artwork == null) {
            System.out.println("Artwork object is null");
            return comments;
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, artwork.getId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int userId = rs.getInt("user_id");
                    User user = userService.getById(userId);


                    Comment comment = new Comment(
                            rs.getInt("id"),
                            user,
                            artwork,
                            rs.getString("content")
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
            ps.setInt(1, commentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error while deleting comment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    public boolean editComment(int commentId, String newContent) {
        String sql = "UPDATE comment SET content = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newContent);
            ps.setInt(2, commentId);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
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