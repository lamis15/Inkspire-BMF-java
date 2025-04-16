package Controllers.Collections;

import entities.Artwork;
import entities.Collections;
import entities.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import service.ArtworkService;
import service.CollectionsService;

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
    private TableView<Artwork> artworkTable;
    
    @FXML
    private TableColumn<Artwork, String> nameColumn;
    
    @FXML
    private TableColumn<Artwork, String> themeColumn;
    
    @FXML
    private TableColumn<Artwork, String> descriptionColumn;
    
    @FXML
    private TableView<Artwork> collectionArtworksTable;
    
    @FXML
    private TableColumn<Artwork, String> collectionNameColumn;
    
    @FXML
    private TableColumn<Artwork, String> collectionThemeColumn;

    private File selectedImageFile;
    private Collections currentCollection;
    private boolean imageRemoved = false;

    private CollectionsService collectionsService = new CollectionsService();
    private ArtworkService artworkService = new ArtworkService();
    
    private ObservableList<Artwork> availableArtworks = FXCollections.observableArrayList();
    private ObservableList<Artwork> collectionArtworks = FXCollections.observableArrayList();
    private ObservableList<Artwork> artworksToAdd = FXCollections.observableArrayList();
    private ObservableList<Artwork> artworksToRemove = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize table columns for available artworks
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        themeColumn.setCellValueFactory(new PropertyValueFactory<>("theme"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        // Initialize table columns for collection artworks
        collectionNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        collectionThemeColumn.setCellValueFactory(new PropertyValueFactory<>("theme"));
        
        // Set up selection models for the tables
        artworkTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        collectionArtworksTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
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
            
            // Update the observable lists
            availableArtworks.clear();
            availableArtworks.addAll(filteredUserArtworks);
            
            collectionArtworks.clear();
            collectionArtworks.addAll(existingCollectionArtworks);
            
            // Set the items in the tables
            artworkTable.setItems(availableArtworks);
            collectionArtworksTable.setItems(collectionArtworks);
            
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
            selectedImageFile = file;
            imagePathLabel.setText(file.getName());
            imageRemoved = false;
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
        // Get selected artworks from the available artworks table
        ObservableList<Artwork> selected = artworkTable.getSelectionModel().getSelectedItems();
        
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
        
        // Update the collection artworks table
        collectionArtworks.addAll(selected);
        availableArtworks.removeAll(selected);
        
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
        // Get selected artworks from the collection artworks table
        ObservableList<Artwork> selected = collectionArtworksTable.getSelectionModel().getSelectedItems();
        
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
        
        // Update the tables
        collectionArtworks.removeAll(selected);
        availableArtworks.addAll(selected);
        
        // Show confirmation
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Artworks Removed");
        alert.setHeaderText(null);
        alert.setContentText(selected.size() + " artwork(s) removed from this collection.");
        alert.showAndWait();
    }
    
    @FXML
    void modifierCollection(ActionEvent event) {
        try {
            if (currentCollection == null) {
                throw new IllegalStateException("No collection loaded for editing");
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
