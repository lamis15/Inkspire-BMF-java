package Controllers.Donations;

import entities.Collections;
import entities.Donation;
import entities.Session;
import entities.User;
import enums.CollectionStatus;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import service.CollectionsService;
import service.DonationService;
import service.UserService;
import utils.SceneSwitch;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AjouterDonation implements Initializable {

    @FXML
    private TextField amountField;

    @FXML
    private VBox rootVBox;

    @FXML
    private Button backButton;

    @FXML
    private VBox collectionDetailsContainer;

    @FXML
    private Label collectionImageLabel;

    @FXML
    private Label collectionTitleLabel;

    @FXML
    private Label collectionDescriptionLabel;

    @FXML
    private Label collectionGoalLabel;

    @FXML
    private Label collectionCurrentAmountLabel;

    @FXML
    private Label collectionStatusLabel;

    @FXML
    private Label selectedCollectionLabel;

    @FXML
    private Label userTokensLabel;

    @FXML
    private Label amountErrorLabel;

    private CollectionsService collectionsService = new CollectionsService();
    private DonationService donationService = new DonationService();
    private UserService userService = new UserService();

    // The selected collection to donate to
    private Collections selectedCollection;

    // Current user for this session
    private User currentUser;

    // User's token balance
    private int userTokenBalance = 0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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

        // Initialize current user from session
        currentUser = Session.getCurrentUser();
        if (currentUser == null) {
            // Handle case where user is not logged in
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Authentication Error");
            alert.setHeaderText(null);
            alert.setContentText("You must be logged in to perform this action.");
            alert.showAndWait();
            
            // Disable donation functionality if not logged in
            amountField.setDisable(true);
            return;
        }

        // Load user token balance
        loadUserTokenBalance();

        // Set up amount field validation
        amountField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateAmount(null);
        });
    }

    /**
     * Load the current user's token balance
     */
    private void loadUserTokenBalance() {
        try {
            User user = userService.getById(currentUser.getId());
            if (user != null) {
                userTokenBalance = user.getTokens() != null ? user.getTokens() : 0;
                userTokensLabel.setText(String.valueOf(userTokenBalance));
            } else {
                userTokensLabel.setText("0");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            userTokensLabel.setText("0");
        }
    }

    /**
     * Method to pre-select a collection when coming from the collection details view
     * @param collection The collection to donate to
     */
    public void preSelectCollection(Collections collection) {
        this.selectedCollection = collection;

        if (collection != null) {
            // Show the selected collection name
            selectedCollectionLabel.setText(collection.getTitle());

            // Show collection details
            collectionDetailsContainer.setVisible(true);

            // Refresh user token balance
            loadUserTokenBalance();

            // Set collection details
            collectionTitleLabel.setText(collection.getTitle());
            collectionDescriptionLabel.setText(collection.getDescription());

            if (collection.getGoal() != null) {
                collectionGoalLabel.setText(String.format("%.2f TND", collection.getGoal()));
            } else {
                collectionGoalLabel.setText("No goal set");
            }

            if (collection.getCurrentAmount() != null) {
                collectionCurrentAmountLabel.setText(String.format("%.2f TND", collection.getCurrentAmount()));
            } else {
                collectionCurrentAmountLabel.setText("0.00 TND");
            }

            // Display the status based on the enum value
            if (collection.getStatus() != null) {
                collectionStatusLabel.setText(collection.getStatusValue());
            } else {
                collectionStatusLabel.setText("No status");
            }

            // Display collection image if available
            if (collection.getImage() != null && !collection.getImage().isEmpty()) {
                try {
                    // Create an ImageView to display the image
                    ImageView imageView = new ImageView(new Image(collection.getImage()));
                    imageView.setFitWidth(150);
                    imageView.setFitHeight(150);
                    imageView.setPreserveRatio(true);

                    // Replace the label with the ImageView
                    collectionImageLabel.setGraphic(imageView);
                    collectionImageLabel.setText("");
                } catch (Exception e) {
                    collectionImageLabel.setText("Image not available");
                    collectionImageLabel.setGraphic(null);
                }
            } else {
                collectionImageLabel.setText("No Image");
                collectionImageLabel.setGraphic(null);
            }
        } else {
            // Hide collection details
            collectionDetailsContainer.setVisible(false);
        }
    }

    /**
     * Add a donation to the selected collection
     */
    /**
     * Validate the donation amount in real-time
     */
    @FXML
    void validateAmount(KeyEvent event) {
        String amountText = amountField.getText().trim();
        boolean isValid = true;
        String errorMessage = "";

        // Check if amount is empty
        if (amountText.isEmpty()) {
            isValid = false;
            errorMessage = "Please enter a donation amount";
        } else {
            try {
                double amount = Double.parseDouble(amountText);
                int tokenAmount = (int) amount;

                // Check if amount is positive
                if (amount <= 0) {
                    isValid = false;
                    errorMessage = "Amount must be greater than zero";
                }
                // Check if user has enough tokens
                else if (tokenAmount > userTokenBalance) {
                    isValid = false;
                    errorMessage = "You only have " + userTokenBalance + " tokens available";
                }
            } catch (NumberFormatException e) {
                isValid = false;
                errorMessage = "Please enter a valid number";
            }
        }

        // Show/hide error message
        amountErrorLabel.setText(errorMessage);
        amountErrorLabel.setVisible(!isValid);

        // Change text field style based on validation
        if (isValid) {
            amountField.setStyle("-fx-border-color: #4D81F7;");
        } else {
            amountField.setStyle("-fx-border-color: #e74c3c;");
        }
    }

    @FXML
    void ajouterDonation(ActionEvent event) {
        try {
            // Validate inputs
            if (selectedCollection == null) {
                showError("No collection selected for donation.");
                return;
            }

            // Validate amount
            String amountText = amountField.getText().trim();
            if (amountText.isEmpty()) {
                showError("Please enter a donation amount.");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountText);
                if (amount <= 0) {
                    showError("Donation amount must be greater than zero.");
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Please enter a valid donation amount.");
                return;
            }

            // Get the collection owner
            User collectionOwner = selectedCollection.getUser();
            if (collectionOwner == null) {
                showError("Collection owner information not available.");
                return;
            }

            // Convert the donation amount to tokens (assuming 1 TND = 1 token)
            int tokenAmount = (int) amount;

            // Final check if the user has enough tokens
            if (tokenAmount > userTokenBalance) {
                showError("You don't have enough tokens for this donation. Your balance: " +
                        userTokenBalance + " tokens.");
                return;
            }

            // Transfer tokens from donator to collection owner
            boolean transferSuccess = userService.transferTokens(
                    currentUser.getId(),
                    collectionOwner.getId(),
                    tokenAmount
            );

            if (!transferSuccess) {
                showError("Failed to transfer tokens. Please try again.");
                return;
            }

            // Update local token balance
            userTokenBalance -= tokenAmount;
            userTokensLabel.setText(String.valueOf(userTokenBalance));

            // Create the donation object
            Donation donation = new Donation(
                    amount,
                    selectedCollection,
                    currentUser
            );
            // The date is automatically set to now in the constructor

            // Add the donation to the database
            donationService.ajouter(donation);

            // Update the collection's current amount
            double newAmount = selectedCollection.getCurrentAmount() != null ?
                    selectedCollection.getCurrentAmount() + amount : amount;
            selectedCollection.setCurrentAmount(newAmount);

            // Check if the goal has been reached
            if (selectedCollection.getGoal() != null && newAmount >= selectedCollection.getGoal()) {
                selectedCollection.setStatus(CollectionStatus.REACHED);
            }

            // Update the collection in the database
            collectionsService.modifier(selectedCollection);

            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Donation of " + String.format("%.2f TND", amount) +
                    " successfully made to " + selectedCollection.getTitle() + ".\n" +
                    tokenAmount + " tokens transferred from your account to " +
                    collectionOwner.getFirstName() + " " + collectionOwner.getLastName() + ".");
            alert.showAndWait();

            // Reset form
            amountField.clear();

            // Return to collections view
            onBackClick(null);

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to make donation: " + e.getMessage());
        }
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
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
                // Switch back to the main view
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
