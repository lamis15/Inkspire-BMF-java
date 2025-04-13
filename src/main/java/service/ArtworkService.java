package service;

import entities.Artwork;
import entities.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import utils.DataSource;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArtworkService implements IService<Artwork> {

    private Connection connection;

    public ArtworkService() {
        connection = DataSource.getInstance().getConnection();
    }

    @Override
    public void ajouter(Artwork artwork) throws SQLException {
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
    }

    @Override
    public void modifier(Artwork artwork) throws SQLException {
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
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM artwork WHERE id=?";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, id);
        preparedStatement.executeUpdate();
    }

    @Override
    public List<Artwork> recuperer() throws SQLException {
        String sql = "SELECT * FROM artwork";
        Statement statement = connection.createStatement();

        ResultSet rs = statement.executeQuery(sql);
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
    
    /**
     * Load artwork cards into a FlowPane container
     * @param container The FlowPane container to load cards into
     * @param artworks The list of artworks to display
     * @param artworkCheckboxes Map to store checkbox references by artwork ID
     * @param selectedArtworks Optional list to track selected artworks
     * @return Map of artwork checkboxes by ID
     */
    public void loadArtworkCards(FlowPane container, List<Artwork> artworks, 
                                Map<Integer, CheckBox> artworkCheckboxes,
                                List<Artwork> selectedArtworks) {
        try {
            // Clear previous artworks and checkboxes
            container.getChildren().clear();
            artworkCheckboxes.clear();
            if (selectedArtworks != null) {
                selectedArtworks.clear();
            }
            
            // Configure the FlowPane for proper scrolling
            container.setPrefWidth(600);
            container.setMaxWidth(Double.MAX_VALUE);
            container.setMinHeight(400);
            
            // Create a card for each artwork
            for (Artwork artwork : artworks) {
                try {
                    // Load the artwork card template
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/ArtworkCard.fxml"));
                    Node artworkCard = loader.load();
                    
                    // Add style class to the card
                    artworkCard.getStyleClass().add("artwork-card");
                    
                    // Find components in the card
                    ImageView artworkImage = (ImageView) ((VBox) artworkCard).lookup("#artworkImage");
                    Label artworkTitle = (Label) ((VBox) artworkCard).lookup("#artworkTitle");
                    Label artworkTheme = (Label) ((VBox) artworkCard).lookup("#artworkTheme");
                    Label artworkDescription = (Label) ((VBox) artworkCard).lookup("#artworkDescription");
                    CheckBox artworkSelect = (CheckBox) ((VBox) artworkCard).lookup("#artworkSelect");
                    
                    // Set artwork data
                    if (artwork.getPicture() != null && !artwork.getPicture().isEmpty()) {
                        try {
                            Image image = new Image(artwork.getPicture());
                            artworkImage.setImage(image);
                        } catch (Exception e) {
                            // Use placeholder image if artwork image can't be loaded
                            Image placeholder = new Image(getClass().getResourceAsStream("/placeholder.png"));
                            artworkImage.setImage(placeholder);
                        }
                    } else {
                        // Use placeholder image if no artwork image
                        Image placeholder = new Image(getClass().getResourceAsStream("/placeholder.png"));
                        artworkImage.setImage(placeholder);
                    }
                    
                    artworkTitle.setText(artwork.getName());
                    artworkTheme.setText(artwork.getTheme());
                    artworkDescription.setText(artwork.getDescription());
                    
                    // Store the checkbox for later reference
                    artworkCheckboxes.put(artwork.getId(), artworkSelect);
                    
                    // Add the card to the container
                    container.getChildren().add(artworkCard);
                    
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            // Show a message if no artworks are available
            if (artworks.isEmpty()) {
                Label noArtworksLabel = new Label("You don't have any artworks yet. Create some artworks first!");
                noArtworksLabel.getStyleClass().add("no-artworks-label");
                container.getChildren().add(noArtworksLabel);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to load artworks: " + e.getMessage());
            alert.showAndWait();
        }
    }
}
