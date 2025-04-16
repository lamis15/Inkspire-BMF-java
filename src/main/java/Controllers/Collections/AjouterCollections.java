package Controllers.Collections;

import entities.Artwork;
import entities.Collections;
import entities.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import service.ArtworkService;
import service.CollectionsService;
import utils.SceneSwitch;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AjouterCollections implements Initializable {

    @FXML
    private TextField titleField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextField goalField;

    // Status field removed - status will be set automatically

    @FXML
    private Label imagePathLabel;
    
    @FXML
    private FlowPane artworkContainer;
    
    @FXML
    private VBox rootVBox;

    @FXML
    private Button backButton;

    private File selectedImageFile;

    private CollectionsService collectionsService = new CollectionsService();
    private ArtworkService artworkService = new ArtworkService();
    
    private List<Artwork> availableArtworks = new ArrayList<>();
    private List<Artwork> selectedArtworks = new ArrayList<>();
    private Map<Integer, CheckBox> artworkCheckboxes = new HashMap<>();

    @FXML
    void chooseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedImageFile = file;
            imagePathLabel.setText(file.getName());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configure the scene after it's loaded
        rootVBox.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                // Find the ScrollPane parent
                Parent parent = rootVBox.getParent();
                while (parent != null && !(parent instanceof ScrollPane)) {
                    parent = parent.getParent();
                }
                
                if (parent instanceof ScrollPane) {
                    ScrollPane mainScrollPane = (ScrollPane) parent;
                    mainScrollPane.setFitToWidth(true);
                    mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                    mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                    mainScrollPane.setPannable(true);
                }
            }
        });
        
        // Load user's artworks
        loadUserArtworks();
    }
    
    /**
     * Load artworks that belong to the current user
     */
    private void loadUserArtworks() {
        try {
            // Simulate logged-in user - replace with actual user session in production
            User currentUser = new User();
            currentUser.setId(1);
            
            // Clear previous artworks and checkboxes
            artworkContainer.getChildren().clear();
            artworkCheckboxes.clear();
            selectedArtworks.clear();
            
            // Configure the FlowPane for proper scrolling
            artworkContainer.setPrefWidth(600);
            artworkContainer.setMaxWidth(Double.MAX_VALUE);
            artworkContainer.setMinHeight(400);
            
            // Get all artworks for the current user
            availableArtworks = artworkService.getArtworksByUserId(currentUser.getId());
            
            // Create a card for each artwork
            for (Artwork artwork : availableArtworks) {
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
                    artworkContainer.getChildren().add(artworkCard);
                    
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            // Show a message if no artworks are available
            if (availableArtworks.isEmpty()) {
                Label noArtworksLabel = new Label("You don't have any artworks yet. Create some artworks first!");
                noArtworksLabel.getStyleClass().add("no-artworks-label");
                artworkContainer.getChildren().add(noArtworksLabel);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to load artworks: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    /**
     * Add selected artworks to the collection
     */
    @FXML
    void selectArtworks(ActionEvent event) {
        // Clear previous selections
        selectedArtworks.clear();
        
        // Get selected artworks based on checkboxes
        for (Artwork artwork : availableArtworks) {
            CheckBox checkbox = artworkCheckboxes.get(artwork.getId());
            if (checkbox != null && checkbox.isSelected()) {
                selectedArtworks.add(artwork);
            }
        }
        
        if (selectedArtworks.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText(null);
            alert.setContentText("Please select at least one artwork to add to the collection.");
            alert.showAndWait();
            return;
        }
        
        // Show confirmation
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Artworks Selected");
        alert.setHeaderText(null);
        alert.setContentText(selectedArtworks.size() + " artwork(s) selected for this collection.");
        alert.showAndWait();
    }
    
    @FXML
    void ajouterCollection(ActionEvent event) {
        try {
            // Validate that at least one artwork is selected
            if (selectedArtworks.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("No Artworks Selected");
                alert.setHeaderText(null);
                alert.setContentText("Please select at least one artwork to add to this collection.");
                alert.showAndWait();
                return;
            }
            
            // Create the collection object
            Collections collection = new Collections();

            collection.setTitle(titleField.getText());
            collection.setDescription(descriptionArea.getText());
            String goalText = goalField.getText().trim();
            if (!goalText.isEmpty()) {
                try {
                    double goal = Double.parseDouble(goalText);
                    collection.setGoal(goal);
                    // Automatically set status to active when a goal is entered
                    collection.setStatus("active");
                } catch (NumberFormatException e) {
                    // If the goal is not a valid number, set it to null
                    collection.setGoal(null);
                    collection.setStatus(null);
                }
            } else {
                // If no goal is entered, set it to null
                collection.setGoal(null);
                collection.setStatus(null);
            }
            collection.setCreationDate(LocalDateTime.now());
            collection.setCurrentAmount(0.0);

            if (selectedImageFile != null) {
                collection.setImage(selectedImageFile.toURI().toString()); // Save as URI
            } else {
                // Image is optional, set to null or a default image path
                collection.setImage(null);
            }

            // Simulate logged-in user
            User currentUser = new User();
            currentUser.setId(1); // Replace with actual user session
            collection.setUser(currentUser);

            // Add the collection to the database
            collectionsService.ajouter(collection);
            
            // Get the newly created collection ID
            List<Collections> allCollections = collectionsService.recuperer();
            Collections newCollection = allCollections.stream()
                .filter(c -> c.getTitle().equals(collection.getTitle()) && 
                       c.getUser().getId().equals(currentUser.getId()))
                .sorted((c1, c2) -> c2.getCreationDate().compareTo(c1.getCreationDate()))
                .findFirst()
                .orElse(null);
                
            if (newCollection != null && !selectedArtworks.isEmpty()) {
                // Add selected artworks to the collection
                // Note: These artworks already belong to the current user because
                // we only loaded artworks from the current user in loadUserArtworks()
                for (Artwork artwork : selectedArtworks) {
                    // Double-check that the artwork belongs to the current user
                    if (artwork.getUser().getId() == currentUser.getId()) {
                        artworkService.addArtworkToCollection(artwork.getId(), newCollection.getId());
                    }
                }
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Collection ajoutée avec succès avec " + selectedArtworks.size() + " artworks.");
            alert.showAndWait();

            // Reset fields
            titleField.clear();
            descriptionArea.clear();
            goalField.clear();
            imagePathLabel.setText("Aucune image choisie");
            selectedImageFile = null;
            
            // Clear artwork selections
            for (CheckBox checkbox : artworkCheckboxes.values()) {
                checkbox.setSelected(false);
            }
            selectedArtworks.clear();

        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to add collection: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    /**
     * Handle back button click to return to the previous screen
     */
    @FXML
    void onBackClick(ActionEvent event) {
        try {
            // Get the current stage from the event source
            Node source = (Node) event.getSource();
            VBox container = (VBox) rootVBox;
            
            // Switch back to the collections view
            SceneSwitch.switchScene(container, "/Collections.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to navigate back: " + e.getMessage());
            alert.showAndWait();
        }
    }
}
