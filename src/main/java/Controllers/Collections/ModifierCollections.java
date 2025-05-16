package Controllers.Collections;

import entities.Artwork;
import entities.Collections;
import enums.CollectionStatus;
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

    @FXML
    private Label titleErrorLabel;

    @FXML
    private Label goalErrorLabel;


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

            // Use the local method to load artwork cards for available artworks
            loadArtworkCards(availableArtworksContainer, availableArtworks,
                    availableArtworkCheckboxes, null);

            // Use the local method to load artwork cards for collection artworks
            loadArtworkCards(collectionArtworksContainer, collectionArtworks,
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CollectionDetails.fxml"));
            Parent detailsView = loader.load();

            CollectionDetails controller = loader.getController();
            controller.setCollection(currentCollection); // Pass the current collection

            // Find the mainRouter and set the new view
            Node mainRouter = backButton.getScene().getRoot().lookup("#mainRouter");
            if (mainRouter instanceof Pane) {
                ((Pane) mainRouter).getChildren().setAll(detailsView);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Show error alert as before
        }
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
    void modifierCollection(ActionEvent event) {
        try {
            if (currentCollection == null) {
                throw new IllegalStateException("No collection loaded for editing");
            }

            // Validate that the collection name is not empty
            if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
                // Use the error label instead of alert
                titleErrorLabel.setText("Collection name cannot be empty");
                titleErrorLabel.setVisible(true);
                titleField.setStyle("-fx-border-color: #e74c3c;");
                return;
            }

            // Validate goal if entered
            String goalFieldValue = goalField.getText().trim();
            if (!goalFieldValue.isEmpty()) {
                try {
                    double goal = Double.parseDouble(goalFieldValue);
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

            // Update collection with form data
            currentCollection.setTitle(titleField.getText());
            currentCollection.setDescription(descriptionArea.getText());

            // Handle goal field and set status accordingly
            // We already validated the goal field, so we can just use it directly
            if (!goalFieldValue.isEmpty()) {
                try {
                    double goal = Double.parseDouble(goalFieldValue);
                    currentCollection.setGoal(goal);
                    // Automatically set status to IN_PROGRESS when a goal is entered
                    currentCollection.setStatus(CollectionStatus.IN_PROGRESS);
                } catch (NumberFormatException e) {
                    // This shouldn't happen since we already validated it above
                    currentCollection.setGoal(null);
                    currentCollection.setStatus(CollectionStatus.NO_GOAL);
                }
            } else {
                // If no goal is entered, set it to null
                currentCollection.setGoal(null);
                currentCollection.setStatus(CollectionStatus.NO_GOAL);
            }

            // Handle image
            if (selectedImageFile != null) {
                // Generate a unique filename to avoid conflicts
                String originalFileName = selectedImageFile.getName();
                String fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.'));
                String uniqueFileName = System.currentTimeMillis() + fileExtension;
                
                // Set the path to save the file
                File destinationDir = new File("C:/xampp/htdocs/collections");
                if (!destinationDir.exists()) {
                    destinationDir.mkdirs();
                }
                
                // Copy the file to the destination directory
                File destinationFile = new File(destinationDir, uniqueFileName);
                
                try {
                    java.nio.file.Files.copy(
                        selectedImageFile.toPath(),
                        destinationFile.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                    );
                    
                    // If there was an old image, we could delete it here
                    
                    // Store only the filename in the collection
                    currentCollection.setImage(uniqueFileName);
                    
                } catch (IOException e) {
                    System.out.println("Error copying image file: " + e.getMessage());
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Image Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Failed to save the image file: " + e.getMessage());
                    alert.showAndWait();
                    return;
                }
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

    /**
     * Load artwork cards into a FlowPane container
     * @param container The FlowPane container to load cards into
     * @param artworks The list of artworks to display
     * @param artworkCheckboxes Map to store checkbox references by artwork ID
     * @param selectedArtworks Optional list to track selected artworks
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
                            // Local file path for artwork images
                            String imagePath = "C:/xampp/htdocs/images/artwork/";

                            // Combine path with the artwork's image filename
                            String imageUrl = imagePath + artwork.getPicture();

                            // Convert local file path to file URL
                            File file = new File(imageUrl);
                            if (file.exists()) {
                                Image image = new Image(file.toURI().toString());
                                artworkImage.setImage(image);
                            } else {
                                System.out.println("Artwork image not found: " + imageUrl);
                                // Use placeholder image if artwork image can't be found
                                Image placeholder = new Image(getClass().getResourceAsStream("/assets/images/placeholder.png"));
                                artworkImage.setImage(placeholder);
                            }
                        } catch (Exception e) {
                            System.out.println("Could not load artwork image: " + artwork.getPicture());
                            e.printStackTrace();
                            // Use placeholder image if artwork image can't be loaded
                            Image placeholder = new Image(getClass().getResourceAsStream("/assets/images/placeholder.png"));
                            artworkImage.setImage(placeholder);
                        }
                    } else {
                        // Use placeholder image if no artwork image
                        Image placeholder = new Image(getClass().getResourceAsStream("/assets/images/placeholder.png"));
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
