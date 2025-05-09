package service;

import entities.Artwork;
import entities.Artworklike;
import entities.User;
import utils.DataSource;
import java.sql.*;



public class ArtworklikeService {

    private final Connection connection;

    public ArtworklikeService() {
        connection = DataSource.getInstance().getConnection();
    }

    public boolean hasUserLiked(Artwork artwork, User user) throws SQLException {
        if (user == null || user.getId() == null) {
            return false;
        }
        String query = "SELECT 1 FROM artwork_like WHERE artwork_id = ? AND user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, artwork.getId());
            statement.setInt(2, user.getId());
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void addLike(Artwork artwork, User user) throws SQLException {
        String query = "INSERT INTO artwork_like (artwork_id, user_id) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, artwork.getId());
            statement.setInt(2, user.getId());
            statement.executeUpdate();
        }
        System.out.println("Like added");
    }

    public int getLikeCount(Artwork artwork) throws SQLException {
        String query = "SELECT COUNT(*) FROM artwork_like WHERE artwork_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, artwork.getId());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
    public void removeLike(Artwork artwork, User user) throws SQLException {
        String query = "DELETE FROM artwork_like WHERE artwork_id = ? AND user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, artwork.getId());
            statement.setInt(2, user.getId());
            statement.executeUpdate();
        }
    }


    public void toggleLike(Artwork artwork, User user) throws SQLException {
        if (hasUserLiked(artwork, user)) {
            removeLike(artwork, user);
        } else {
            addLike(artwork, user);
        }
    }



}
