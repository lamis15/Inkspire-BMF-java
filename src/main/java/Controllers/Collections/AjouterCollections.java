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
import javafx.scene.input.KeyEvent;
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
    
    @FXML
    private Label titleErrorLabel;
    
    @FXML
    private Label goalErrorLabel;

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
            // Validate that the file is a valid image
            try {
                // Attempt to create an image from the file to validate it
                new javafx.scene.image.Image(file.toURI().toString());
                
                // If no exception is thrown, it's a valid image
                selectedImageFile = file;
                imagePathLabel.setText(file.getName());
            } catch (Exception e) {
                // Not a valid image file
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Image");
                alert.setHeaderText(null);
                alert.setContentText("The selected file is not a valid image. Please select a valid image file.");
                alert.showAndWait();
            }
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
            
            // Get all artworks for the current user
            availableArtworks = artworkService.getArtworksByUserId(currentUser.getId());
            
            // Use the service method to load artwork cards
            artworkService.loadArtworkCards(artworkContainer, availableArtworks, artworkCheckboxes, selectedArtworks);
            
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
    
    /**
     * Validate the collection title in real-time
     */
    @FXML
    void validateTitle(KeyEvent event) {
        String title = titleField.getText().trim();
        boolean isValid = true;
        String errorMessage = "";
        
        // Check if title is empty
        if (title.isEmpty()) {
            isValid = false;
            errorMessage = "Collection name cannot be empty";
        }
        
        // Show/hide error message
        titleErrorLabel.setText(errorMessage);
        titleErrorLabel.setVisible(!isValid);
        
        // Change text field style based on validation
        if (isValid) {
            titleField.setStyle("-fx-border-color: #4D81F7;");
        } else {
            titleField.setStyle("-fx-border-color: #e74c3c;");
        }
    }
    
    /**
     * Validate the funding goal in real-time
     */
    @FXML
    void validateGoal(KeyEvent event) {
        String goalText = goalField.getText().trim();
        boolean isValid = true;
        String errorMessage = "";
        
        // Goal is optional, so empty is valid
        if (!goalText.isEmpty()) {
            try {
                double goal = Double.parseDouble(goalText);
                
                // Check if goal is positive
                if (goal <= 0) {
                    isValid = false;
                    errorMessage = "Goal amount must be greater than zero";
                }
            } catch (NumberFormatException e) {
                isValid = false;
                errorMessage = "Please enter a valid number";
            }
        }
        
        // Show/hide error message
        goalErrorLabel.setText(errorMessage);
        goalErrorLabel.setVisible(!isValid);
        
        // Change text field style based on validation
        if (isValid) {
            goalField.setStyle("-fx-border-color: #4D81F7;");
        } else {
            goalField.setStyle("-fx-border-color: #e74c3c;");
        }
    }
    
    @FXML
    void ajouterCollection(ActionEvent event) {
        try {
            // Validate that the collection name is not empty
            if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
                // Use the error label instead of alert
                titleErrorLabel.setText("Collection name cannot be empty");
                titleErrorLabel.setVisible(true);
                titleField.setStyle("-fx-border-color: #e74c3c;");
                return;
            }
            
            // Validate goal if entered
            String goalText = goalField.getText().trim();
            if (!goalText.isEmpty()) {
                try {
                    double goal = Double.parseDouble(goalText);
                    if (goal <= 0) {
                        goalErrorLabel.setText("Goal amount must be greater than zero");
                        goalErrorLabel.setVisible(true);
                        goalField.setStyle("-fx-border-color: #e74c3c;");
                        return;
                    }
                } catch (NumberFormatException e) {
                    goalErrorLabel.setText("Please enter a valid number");
                    goalErrorLabel.setVisible(true);
                    goalField.setStyle("-fx-border-color: #e74c3c;");
                    return;
                }
            }
            
            // Artworks are now optional - no need to validate if selectedArtworks is empty
            
            // Create the collection object
            Collections collection = new Collections();

            collection.setTitle(titleField.getText());
            collection.setDescription(descriptionArea.getText());
            // Get goal value - reuse the validation we already did
            if (!goalField.getText().trim().isEmpty()) {
                try {
                    double goal = Double.parseDouble(goalField.getText().trim());
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
            currentUser.setId(2); // Replace with actual user session
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
            // Find the mainRouter in the scene graph
            Node mainRouter = backButton.getScene().getRoot().lookup("#mainRouter");
            if (mainRouter != null) {
                // Switch back to the collections view using the mainRouter
                SceneSwitch.switchScene((javafx.scene.layout.Pane) mainRouter, "/AfficherCollections.fxml");
            } else {
                throw new Exception("mainRouter not found in scene graph");
            }
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
