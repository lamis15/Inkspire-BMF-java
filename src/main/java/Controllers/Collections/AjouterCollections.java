package Controllers.Collections;

import entities.Artwork;
import entities.Collections;
import entities.Session;
import entities.User;
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

        loadUserArtworks();
    }

    /**
     * Load artworks that belong to the current user
     */
    private void loadUserArtworks() {
        try {
            // Get current user from session
            User currentUser = Session.getCurrentUser();
            if (currentUser == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Authentication Error");
                alert.setHeaderText(null);
                alert.setContentText("You must be logged in to perform this action.");
                alert.showAndWait();
                return;
            }

            // Get all artworks for the current user
            availableArtworks = artworkService.getArtworksByUserId(currentUser.getId());

            // Use the local method to load artwork cards
            loadArtworkCards(artworkContainer, availableArtworks, artworkCheckboxes, selectedArtworks);

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
     * Helper method to validate the collection title
     * @return true if the title is valid, false otherwise
     */
    private boolean validateTitleField() {
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

        return isValid;
    }

    /**
     * Helper method to validate the funding goal
     * @return true if the goal is valid, false otherwise
     */
    private boolean validateGoalField() {
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

        return isValid;
    }

    /**
     * Validate the collection title in real-time
     */
    @FXML
    void validateTitle(KeyEvent event) {
        validateTitleField();
    }

    /**
     * Validate the funding goal in real-time
     */
    @FXML
    void validateGoal(KeyEvent event) {
        validateGoalField();
    }

    @FXML
    void ajouterCollection(ActionEvent event) {
        try {
            // Use the helper validation methods
            boolean titleValid = validateTitleField();
            boolean goalValid = validateGoalField();

            // If any validation fails, return early
            if (!titleValid || !goalValid) {
                return;
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
                    // Automatically set status to IN_PROGRESS when a goal is entered
                    collection.setStatus(CollectionStatus.IN_PROGRESS);
                } catch (NumberFormatException e) {
                    // If the goal is not a valid number, set it to null
                    collection.setGoal(null);
                    collection.setStatus(CollectionStatus.NO_GOAL);
                }
            } else {
                // If no goal is entered, set it to null
                collection.setGoal(null);
                collection.setStatus(CollectionStatus.NO_GOAL);
            }
            collection.setCreationDate(LocalDateTime.now());
            collection.setCurrentAmount(0.0);

            if (selectedImageFile != null) {
                collection.setImage(selectedImageFile.toURI().toString()); // Save as URI
            } else {
                // Image is optional, set to null or a default image path
                collection.setImage(null);
            }

            // Set the current user as the owner
            User currentUser = Session.getCurrentUser();
            if (currentUser == null) {
                // Handle case where user is not logged in
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Authentication Error");
                alert.setHeaderText(null);
                alert.setContentText("You must be logged in to perform this action.");
                alert.showAndWait();
                return;
            }
            collection.setUser(currentUser);

            // Add the collection to the database
            collectionsService.ajouter(collection);

            // Get the newly created collection using the service method
            Collections newCollection = collectionsService.getRecentlyCreatedCollection(currentUser.getId(), collection.getTitle());

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

            // Configure the FlowPane for proper scrolling and layout
            container.setPrefWidth(600);
            container.setMaxWidth(Double.MAX_VALUE);
            container.setMinHeight(400);

            // Check if artworks list is null or empty
            if (artworks == null || artworks.isEmpty()) {
                Label noArtworksLabel = new Label("You don't have any artworks yet. Create some artworks first!");
                noArtworksLabel.getStyleClass().add("no-artworks-label");
                container.getChildren().add(noArtworksLabel);
                return;
            }

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

                    // Set artwork image
                    if (artwork.getPicture() != null && !artwork.getPicture().isEmpty()) {
                        try {
                            // Try loading the image using the file path
                            File file = new File("C:/xampp/htdocs/" + artwork.getPicture());
                            if (file.exists()) {
                                Image image = new Image(file.toURI().toString());
                                artworkImage.setImage(image);
                            } else {
                                throw new Exception("File not found");
                            }
                        } catch (Exception e) {
                            // Use placeholder image if the artwork image can't be loaded
                            Image placeholder = new Image(getClass().getResourceAsStream("/assets/images/placeholder.png"));
                            artworkImage.setImage(placeholder);
                        }
                    } else {
                        // Use placeholder image if no artwork image is provided
                        Image placeholder = new Image(getClass().getResourceAsStream("/assets/images/placeholder.png"));
                        artworkImage.setImage(placeholder);
                    }

                    // Set artwork title, theme, and description
                    artworkTitle.setText(artwork.getName());
                    artworkTheme.setText(artwork.getTheme());
                    artworkDescription.setText(artwork.getDescription());

                    // Store the checkbox for later reference
                    artworkCheckboxes.put(artwork.getId(), artworkSelect);

                    // Add the card to the container
                    container.getChildren().add(artworkCard);

                } catch (IOException e) {
                    e.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error Loading Artwork Card");
                    alert.setHeaderText(null);
                    alert.setContentText("Failed to load artwork card template: " + e.getMessage());
                    alert.showAndWait();
                }
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
