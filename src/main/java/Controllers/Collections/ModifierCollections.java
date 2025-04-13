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
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import service.ArtworkService;
import service.CollectionsService;
import utils.SceneSwitch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ModifierCollections implements Initializable {

    @FXML
    private TextField titleField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextField goalField;

    @FXML
    private Label imagePathLabel;
    
    @FXML
    private Button backButton;
    
    @FXML
    private VBox rootVBox;
    
    @FXML
    private FlowPane availableArtworksContainer;
    
    @FXML
    private FlowPane collectionArtworksContainer;
    

    private File selectedImageFile;
    private Collections currentCollection;
    private boolean imageRemoved = false;

    private CollectionsService collectionsService = new CollectionsService();
    private ArtworkService artworkService = new ArtworkService();
    
    private List<Artwork> availableArtworks = new ArrayList<Artwork>();
    private List<Artwork> collectionArtworks = new ArrayList<Artwork>();
    private List<Artwork> artworksToAdd = new ArrayList<Artwork>();
    private List<Artwork> artworksToRemove = new ArrayList<Artwork>();
    private Map<Integer, CheckBox> availableArtworkCheckboxes = new HashMap<Integer, CheckBox>();
    private Map<Integer, CheckBox> collectionArtworkCheckboxes = new HashMap<Integer, CheckBox>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configure the scene after it's loaded
        // Configure the scene after it's loaded
        if (rootVBox != null) {
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
        }
    }

    /**
     * Load collection data into the form
     * @param collection The collection to edit
     */
    public void setCollection(Collections collection) {
        this.currentCollection = collection;
        
        // Populate form fields with collection data
        titleField.setText(collection.getTitle());
        descriptionArea.setText(collection.getDescription());
        
        if (collection.getGoal() != null) {
            goalField.setText(String.valueOf(collection.getGoal()));
        } else {
            goalField.clear();
        }
        
        if (collection.getImage() != null && !collection.getImage().isEmpty()) {
            imagePathLabel.setText(collection.getImage().substring(collection.getImage().lastIndexOf("/") + 1));
        } else {
            imagePathLabel.setText("No image");
        }
        
        // Load artworks for this collection and user
        loadArtworks();
    }
    
    /**
     * Load artworks that belong to the current user and collection
     */
    private void loadArtworks() {
        try {
            if (currentCollection == null) {
                return;
            }
            
            // Simulate logged-in user - replace with actual user session in production
            int currentUserId = currentCollection.getUser().getId();
            
            // Get artworks by user ID
            List<Artwork> userArtworks = artworkService.getArtworksByUserId(currentUserId);
            
            // Get artworks already in the collection
            List<Artwork> existingCollectionArtworks = artworkService.getArtworksByCollectionId(currentCollection.getId());
            
            // Filter out artworks that are already in the collection
            List<Artwork> filteredUserArtworks = userArtworks.stream()
                .filter(artwork -> !existingCollectionArtworks.contains(artwork))
                .collect(Collectors.toList());
            
            // Update the lists
            availableArtworks.clear();
            availableArtworks.addAll(filteredUserArtworks);
            
            collectionArtworks.clear();
            collectionArtworks.addAll(existingCollectionArtworks);
            
            // Use the service method to load artwork cards for available artworks
            artworkService.loadArtworkCards(availableArtworksContainer, availableArtworks, 
                                          availableArtworkCheckboxes, null);
            
            // Use the service method to load artwork cards for collection artworks
            artworkService.loadArtworkCards(collectionArtworksContainer, collectionArtworks, 
                                          collectionArtworkCheckboxes, null);
            
            // Show a message if no artworks are available
            if (availableArtworks.isEmpty() && availableArtworksContainer.getChildren().isEmpty()) {
                Label noArtworksLabel = new Label("You don't have any additional artworks to add");
                noArtworksLabel.getStyleClass().add("no-artworks-label");
                availableArtworksContainer.getChildren().add(noArtworksLabel);
            }
            
            // Show a message if no artworks in collection
            if (collectionArtworks.isEmpty() && collectionArtworksContainer.getChildren().isEmpty()) {
                Label noArtworksLabel = new Label("This collection doesn't have any artworks yet");
                noArtworksLabel.getStyleClass().add("no-artworks-label");
                collectionArtworksContainer.getChildren().add(noArtworksLabel);
            }
            
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error loading artworks");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    void chooseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose an image");
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
                imageRemoved = false;
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
    
    @FXML
    void removeImage(ActionEvent event) {
        selectedImageFile = null;
        imagePathLabel.setText("No image");
        imageRemoved = true;
    }

    /**
     * Add selected artworks to the collection
     */
    @FXML
    void addArtworksToCollection(ActionEvent event) {
        // Get selected artworks based on checkboxes
        List<Artwork> selected = new ArrayList<Artwork>();
        for (Artwork artwork : availableArtworks) {
            CheckBox checkbox = availableArtworkCheckboxes.get(artwork.getId());
            if (checkbox != null && checkbox.isSelected()) {
                selected.add(artwork);
            }
        }
        
        if (selected.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText(null);
            alert.setContentText("Please select at least one artwork to add to the collection.");
            alert.showAndWait();
            return;
        }
        
        // Add to artworks to add list
        artworksToAdd.addAll(selected);
        
        // Update the artwork lists
        collectionArtworks.addAll(selected);
        availableArtworks.removeAll(selected);
        
        // Reload the artwork cards
        loadArtworks();
        
        // Show confirmation
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Artworks Added");
        alert.setHeaderText(null);
        alert.setContentText(selected.size() + " artwork(s) added to this collection.");
        alert.showAndWait();
    }
    
    /**
     * Remove selected artworks from the collection
     */
    @FXML
    void removeArtworksFromCollection(ActionEvent event) {
        // Get selected artworks based on checkboxes
        List<Artwork> selected = new ArrayList<Artwork>();
        for (Artwork artwork : collectionArtworks) {
            CheckBox checkbox = collectionArtworkCheckboxes.get(artwork.getId());
            if (checkbox != null && checkbox.isSelected()) {
                selected.add(artwork);
            }
        }
        
        if (selected.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText(null);
            alert.setContentText("Please select at least one artwork to remove from the collection.");
            alert.showAndWait();
            return;
        }
        
        // Add to artworks to remove list
        artworksToRemove.addAll(selected);
        
        // Update the lists
        collectionArtworks.removeAll(selected);
        availableArtworks.addAll(selected);
        
        // Reload the artwork cards
        loadArtworks();
        
        // Show confirmation
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Artworks Removed");
        alert.setHeaderText(null);
        alert.setContentText(selected.size() + " artwork(s) removed from this collection.");
        alert.showAndWait();
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
                // Switch back to the collection details view
                SceneSwitch.switchScene((javafx.scene.layout.Pane) mainRouter, "/CollectionDetails.fxml");
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
    
    @FXML
    void modifierCollection(ActionEvent event) {
        try {
            if (currentCollection == null) {
                throw new IllegalStateException("No collection loaded for editing");
            }
            
            // Validate that the collection name is not empty
            if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Input");
                alert.setHeaderText(null);
                alert.setContentText("Collection name cannot be empty. Please enter a name for your collection.");
                alert.showAndWait();
                return;
            }
            
            // Update collection with form data
            currentCollection.setTitle(titleField.getText());
            currentCollection.setDescription(descriptionArea.getText());
            
            // Handle goal field and set status accordingly
            String goalText = goalField.getText().trim();
            if (!goalText.isEmpty()) {
                try {
                    double goal = Double.parseDouble(goalText);
                    currentCollection.setGoal(goal);
                    // Automatically set status to active when a goal is entered
                    currentCollection.setStatus("active");
                } catch (NumberFormatException e) {
                    // If the goal is not a valid number, set it to null
                    currentCollection.setGoal(null);
                    currentCollection.setStatus(null);
                }
            } else {
                // If no goal is entered, set it to null
                currentCollection.setGoal(null);
                currentCollection.setStatus(null);
            }
            
            // Handle image
            if (selectedImageFile != null) {
                // New image selected
                currentCollection.setImage(selectedImageFile.toURI().toString());
            } else if (imageRemoved) {
                // Image was explicitly removed
                currentCollection.setImage(null);
            }
            // If neither condition is true, keep the existing image

            // Update the collection in the database
            collectionsService.modifier(currentCollection);
            
            // Handle artwork changes
            // Add new artworks to the collection
            for (Artwork artwork : artworksToAdd) {
                artworkService.addArtworkToCollection(artwork.getId(), currentCollection.getId());
            }
            
            // Remove artworks from the collection
            for (Artwork artwork : artworksToRemove) {
                artworkService.removeArtworkFromCollection(artwork.getId(), currentCollection.getId());
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Collection updated successfully.");
            alert.showAndWait();
            
            // Reset artwork tracking lists
            artworksToAdd.clear();
            artworksToRemove.clear();
            
            // Reload artworks to reflect changes
            loadArtworks();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error updating collection");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}
