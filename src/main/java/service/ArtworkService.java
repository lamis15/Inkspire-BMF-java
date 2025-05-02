package service;

import entities.Artwork;
import entities.User;
import utils.DataSource;

import java.sql.*;
import java.util.*;

public class ArtworkService implements IService<Artwork> {

    private Connection connection;

    public ArtworkService() {
        connection = DataSource.getInstance().getConnection();
    }

    @Override
    public boolean ajouter(Artwork artwork) throws SQLException {
        String sql = "INSERT INTO artwork (name, theme, description, picture, status, user_id) VALUES (?, ?, ?, ?, ?, ?)";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, artwork.getName());
        preparedStatement.setString(2, artwork.getTheme());
        preparedStatement.setString(3, artwork.getDescription());
        preparedStatement.setString(4, artwork.getPicture());

        if (artwork.getStatus() != null) {
            preparedStatement.setInt(5, artwork.getStatus() ? 1 : 0);
        } else {
            preparedStatement.setNull(5, Types.INTEGER);
        }

        preparedStatement.setInt(6, artwork.getUser().getId());

        preparedStatement.executeUpdate();
        return true;
    }

    @Override
    public boolean modifier(Artwork artwork) throws SQLException {
        String sql = "UPDATE artwork SET name=?, theme=?, description=?, picture=?, status=?, user_id=? WHERE id=?";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, artwork.getName());
        preparedStatement.setString(2, artwork.getTheme());
        preparedStatement.setString(3, artwork.getDescription());
        preparedStatement.setString(4, artwork.getPicture());

        if (artwork.getStatus() != null) {
            preparedStatement.setBoolean(5, artwork.getStatus());
        } else {
            preparedStatement.setNull(5, Types.BOOLEAN);
        }

        preparedStatement.setInt(6, artwork.getUser().getId());
        preparedStatement.setInt(7, artwork.getId());

        preparedStatement.executeUpdate();
        return true;
    }
    public void deleteArtwork(int artworkId) throws SQLException {
        // Step 1: Delete dependent records from artwork_like table
        String deleteLikesSql = "DELETE FROM artwork_like WHERE artwork_id = ?";
        try (PreparedStatement psLikes = connection.prepareStatement(deleteLikesSql)) {
            psLikes.setInt(1, artworkId);
            psLikes.executeUpdate();
        }

        // Step 2: Delete the artwork record from the artwork table
        String deleteArtworkSql = "DELETE FROM artwork WHERE id = ?";
        try (PreparedStatement psArtwork = connection.prepareStatement(deleteArtworkSql)) {
            psArtwork.setInt(1, artworkId);
            psArtwork.executeUpdate();
        }
    }
    @Override
    public boolean supprimer(int id) throws SQLException {
        String sql = "DELETE FROM artwork WHERE id=?";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, id);
        preparedStatement.executeUpdate();
        return true;
    }

    @Override
    public List<Artwork> recuperer() throws SQLException {
        return  null;
    }

    public List<Artwork> recuperer1(int id) throws SQLException {
        String sql = "SELECT * FROM artwork WHERE id = ?";
        List<Artwork> list = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Artwork a = new Artwork();
                    a.setId(rs.getInt("id"));
                    a.setName(rs.getString("name"));
                    a.setTheme(rs.getString("theme"));
                    a.setDescription(rs.getString("description"));
                    a.setPicture(rs.getString("picture"));
                    if (rs.getObject("status") == null) {
                        a.setStatus(null);
                    } else {
                        a.setStatus(rs.getBoolean("status"));
                    }


                    list.add(a);
                }
            }
        }
        return list;
    }

    /**
     * Get artworks by user ID
     * @param userId The user ID to filter by
     * @return List of artworks belonging to the user
     */
    public List<Artwork> getArtworksByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM artwork WHERE user_id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, userId);
        ResultSet rs = preparedStatement.executeQuery();
        List<Artwork> list = new ArrayList<>();

        while (rs.next()) {
            Artwork a = new Artwork();
            a.setId(rs.getInt("id"));
            a.setName(rs.getString("name"));
            a.setTheme(rs.getString("theme"));
            a.setDescription(rs.getString("description"));
            a.setPicture(rs.getString("picture"));

            // Handle status values
            int status = rs.getInt("status");
            if (rs.wasNull()) {
                a.setStatus(null);
            } else {
                a.setStatus(status > 0);
            }

            User user = new User();
            user.setId(rs.getInt("user_id"));
            a.setUser(user);

            list.add(a);
        }
        return list;
    }

    /**
     * Add an artwork to a collection
     * @param artworkId The artwork ID to add
     * @param collectionId The collection ID to add to
     */
    public void addArtworkToCollection(int artworkId, int collectionId) throws SQLException {
        // First check if this relationship already exists to avoid duplicates
        String checkSql = "SELECT COUNT(*) FROM collections_artwork WHERE artwork_id = ? AND collections_id = ?";
        PreparedStatement checkStmt = connection.prepareStatement(checkSql);
        checkStmt.setInt(1, artworkId);
        checkStmt.setInt(2, collectionId);
        ResultSet checkRs = checkStmt.executeQuery();
        checkRs.next();
        int count = checkRs.getInt(1);

        // Only insert if the relationship doesn't already exist
        if (count == 0) {
            String sql = "INSERT INTO collections_artwork (artwork_id, collections_id) VALUES (?, ?)";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, artworkId);
            preparedStatement.setInt(2, collectionId);

            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println("Added artwork " + artworkId + " to collection " + collectionId + ". Rows affected: " + rowsAffected);
        } else {
            System.out.println("Artwork " + artworkId + " is already in collection " + collectionId + ". Skipping insertion.");
        }
    }

    /**
     * Remove an artwork from a collection
     * @param artworkId The artwork ID to remove
     * @param collectionId The collection ID to remove from
     */
    public void removeArtworkFromCollection(int artworkId, int collectionId) throws SQLException {
        String sql = "DELETE FROM collections_artwork WHERE artwork_id = ? AND collections_id = ?";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, artworkId);
        preparedStatement.setInt(2, collectionId);

        preparedStatement.executeUpdate();
    }

    /**
     * Get artworks in a collection
     * @param collectionId The collection ID to filter by
     * @return List of artworks in the collection
     */
    public List<Artwork> getArtworksByCollectionId(int collectionId) throws SQLException {
        // Debug log
        System.out.println("Fetching artworks for collection ID: " + collectionId);

        // First, check if there are any entries in the collections_artwork table
        String checkSql = "SELECT COUNT(*) FROM collections_artwork WHERE collections_id = ?";
        PreparedStatement checkStmt = connection.prepareStatement(checkSql);
        checkStmt.setInt(1, collectionId);
        ResultSet checkRs = checkStmt.executeQuery();
        checkRs.next();
        int count = checkRs.getInt(1);
        System.out.println("Found " + count + " entries in collections_artwork table for collection ID: " + collectionId);

        // Main query to get artwork details
        String sql = "SELECT a.* FROM artwork a " +
                "JOIN collections_artwork ca ON a.id = ca.artwork_id " +
                "WHERE ca.collections_id = ?";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, collectionId);

        ResultSet rs = preparedStatement.executeQuery();
        List<Artwork> list = new ArrayList<>();

        while (rs.next()) {
            Artwork a = new Artwork();
            a.setId(rs.getInt("id"));
            a.setName(rs.getString("name"));
            a.setTheme(rs.getString("theme"));
            a.setDescription(rs.getString("description"));
            a.setPicture(rs.getString("picture"));

            // Handle status values
            int status = rs.getInt("status");
            if (rs.wasNull()) {
                a.setStatus(null);
            } else {
                a.setStatus(status > 0);
            }

            User user = new User();
            user.setId(rs.getInt("user_id"));
            a.setUser(user);

            list.add(a);
            System.out.println("Added artwork to list: " + a.getId() + " - " + a.getName());
        }

        System.out.println("Returning " + list.size() + " artworks for collection ID: " + collectionId);
        return list;
    }

    public List<Artwork> getAllArtworks() throws SQLException {
        String sql = "SELECT * FROM artwork";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet rs = preparedStatement.executeQuery();
        List<Artwork> list = new ArrayList<>();

        while (rs.next()) {
            Artwork a = new Artwork();
            a.setId(rs.getInt("id"));
            a.setName(rs.getString("name"));
            a.setTheme(rs.getString("theme"));
            a.setDescription(rs.getString("description"));
            a.setPicture(rs.getString("picture"));

            // Handle status values
            int status = rs.getInt("status");
            if (rs.wasNull()) {
                a.setStatus(null);
            } else {
                a.setStatus(status > 0);
            }

            // Set user for each artwork (optional)
            User user = new User();
            user.setId(rs.getInt("user_id"));
            a.setUser(user);

            list.add(a);
        }

        return list;
    }




    public Map<Integer, Integer> getLikesPerArtwork() throws SQLException {
        Map<Integer, Integer> likesPerArtwork = new HashMap<>();
        String query = "SELECT artwork_id, COUNT(*) AS like_count FROM artwork_like GROUP BY artwork_id ORDER BY like_count DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int artworkId = rs.getInt("artwork_id");
                int likeCount = rs.getInt("like_count");
                likesPerArtwork.put(artworkId, likeCount);
            }
        }

        return likesPerArtwork;
    }

    public Artwork getArtworkById(int artworkId) throws SQLException {
        Artwork artwork = null;
        String query = "SELECT * FROM artwork WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, artworkId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    artwork = new Artwork();
                    artwork.setId(rs.getInt("id"));
                    artwork.setName(rs.getString("name"));
                    artwork.setTheme(rs.getString("theme"));
                    artwork.setDescription(rs.getString("description"));
                    artwork.setPicture(rs.getString("picture"));
                    artwork.setStatus(rs.getBoolean("status"));
                    // Set other fields if needed
                }
            }
        }

        return artwork;
    }
}



