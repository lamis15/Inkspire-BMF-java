package Controllers.Collections;

import entities.Artwork;
import entities.Collections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.ArtworkService;
import service.CollectionsService;
import utils.SceneSwitch;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class CollectionDetails implements javafx.fxml.Initializable {

    @FXML
    private Label titleLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Label goalLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private ImageView imageView;

    @FXML
    private Button deleteButton;

    @FXML
    private Button modifyButton;
    
    @FXML
    private Button backButton;
    
    @FXML
    private FlowPane artworkContainer;

    private final CollectionsService collectionsService = new CollectionsService();
    private final ArtworkService artworkService = new ArtworkService();

    private Collections collection;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize the controller
        System.out.println("Initializing CollectionDetails controller");
        
        // Configure the FlowPane for proper display of artworks
        artworkContainer.setHgap(15);
        artworkContainer.setVgap(15);
        artworkContainer.setPrefWrapLength(700);
    }
    
    public void setCollection(Collections collection) {
        System.out.println("Setting collection: " + collection.getId() + " - " + collection.getTitle());
        this.collection = collection;

        // Set collection details
        titleLabel.setText(collection.getTitle());
        descriptionLabel.setText(collection.getDescription());
        
        if (collection.getGoal() != null) {
            goalLabel.setText("Goal: " + collection.getGoal() + " TND");
        } else {
            goalLabel.setText("No goal set");
        }
        
        if (collection.getStatus() != null) {
            statusLabel.setText("Status: " + collection.getStatus());
        } else {
            statusLabel.setText("Status: Not active");
        }

        // Load collection image
        if (collection.getImage() != null && !collection.getImage().isEmpty()) {
            try {
                Image image = new Image(collection.getImage());
                imageView.setImage(image);
            } catch (Exception e) {
                System.out.println("Error loading image: " + e.getMessage());
            }
        }

        // Load artworks that belong to this collection
        loadCollectionArtworks();

        // Set up delete button action
        deleteButton.setOnAction(e -> {
            try {
                collectionsService.supprimer(collection.getId());
                // Navigate back to collections list using mainRouter
                Node mainRouter = deleteButton.getScene().getRoot().lookup("#mainRouter");
                if (mainRouter instanceof Pane) {
                    SceneSwitch.switchScene((Pane) mainRouter, "/AfficherCollections.fxml");
                    System.out.println("Successfully navigated back to collections view after delete");
                } else {
                    System.out.println("Could not find mainRouter for navigation after delete");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        // Set up modify button action
        modifyButton.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierCollections.fxml"));
                Parent root = loader.load();

                // Pass the collection to the controller
                ModifierCollections controller = loader.getController();
                controller.setCollection(collection);
                
                // Find the mainRouter and load the modify view
                Node mainRouter = modifyButton.getScene().getRoot().lookup("#mainRouter");
                if (mainRouter instanceof Pane) {
                    ((Pane) mainRouter).getChildren().clear();
                    ((Pane) mainRouter).getChildren().add(root);
                    System.out.println("Successfully loaded modify view");
                } else {
                    System.out.println("Could not find mainRouter for navigation to modify view");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }
    
    /**
     * Load artworks that belong to this collection
     */
    private void loadCollectionArtworks() {
        try {
            // Clear any existing content
            artworkContainer.getChildren().clear();
            
            // Get artworks in this collection
            List<Artwork> collectionArtworks = artworkService.getArtworksByCollectionId(collection.getId());
            
            // Debug log
            System.out.println("Loading artworks for collection ID: " + collection.getId());
            System.out.println("Found " + collectionArtworks.size() + " artworks in this collection");
            
            // Create a card for each artwork
            for (Artwork artwork : collectionArtworks) {
                try {
                    System.out.println("Processing artwork: " + artwork.getId() + " - " + artwork.getName());
                    
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
                    
                    // Hide the checkbox since we're just displaying, not selecting
                    Node checkbox = ((VBox) artworkCard).lookup("#artworkSelect");
                    if (checkbox != null) {
                        checkbox.setVisible(false);
                        checkbox.setManaged(false);
                    }
                    
                    // Set artwork data
                    if (artwork.getPicture() != null && !artwork.getPicture().isEmpty()) {
                        try {
                            Image image = new Image(artwork.getPicture());
                            artworkImage.setImage(image);
                        } catch (Exception e) {
                            System.out.println("Error loading artwork image: " + e.getMessage());
                            // Use placeholder image if artwork image can't be loaded
                            Image placeholder = new Image(getClass().getResourceAsStream("/placeholder.png"));
                            artworkImage.setImage(placeholder);
                        }
                    } else {
                        // Use placeholder image if no artwork image
                        Image placeholder = new Image(getClass().getResourceAsStream("/placeholder.png"));
                        artworkImage.setImage(placeholder);
                    }
                    
                    // Set text content
                    artworkTitle.setText(artwork.getName());
                    artworkTheme.setText(artwork.getTheme());
                    artworkDescription.setText(artwork.getDescription());
                    
                    // Add the card to the container
                    artworkContainer.getChildren().add(artworkCard);
                    System.out.println("Added artwork card to container");
                    
                } catch (IOException e) {
                    System.out.println("Error creating artwork card: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // Show a message if no artworks are in this collection
            if (collectionArtworks.isEmpty()) {
                Label noArtworksLabel = new Label("No artworks in this collection");
                noArtworksLabel.getStyleClass().add("no-artworks-label");
                artworkContainer.getChildren().add(noArtworksLabel);
                System.out.println("No artworks found, added empty message");
            }
            
        } catch (SQLException e) {
            System.out.println("SQL error loading artworks: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void onBackClick() {
        // Find the mainRouter in the scene graph
        Node node = backButton.getScene().getRoot().lookup("#mainRouter");
        
        if (node instanceof Pane) {
            SceneSwitch.switchScene((Pane) node, "/AfficherCollections.fxml");
            System.out.println("Successfully navigated back to collections view");
        } else {
            System.out.println("Could not find mainRouter for navigation");
        }
    }
}
