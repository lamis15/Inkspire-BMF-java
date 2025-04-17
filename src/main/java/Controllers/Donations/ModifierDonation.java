package Controllers.Donations;

import entities.Collections;
import entities.Donation;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import service.CollectionsService;
import service.DonationService;
import service.UserService;
import utils.SceneSwitch;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ModifierDonation implements Initializable {

    @FXML
    private TextField amountField;
    
    @FXML
    private Label selectedCollectionLabel;
    
    @FXML
    private Label titleLabel;
    
    @FXML
    private Label amountErrorLabel;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button backButton;
    
    @FXML
    private VBox rootVBox;
    
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
    private Label donationIdLabel;
    
    @FXML
    private Label donationDateLabel;
    
    @FXML
    private Label userTokensLabel;
    
    private Donation donation;
    private DonationService donationService;
    private CollectionsService collectionsService;
    
    // Current user for this session
    private User currentUser;
    
    // User's token balance
    private int userTokenBalance = 0;
    
    // The selected collection
    private Collections selectedCollection;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        donationService = new DonationService();
        collectionsService = new CollectionsService();
        
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
            saveButton.setDisable(true);
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
            UserService userService = new UserService();
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
     * Set the donation to be modified
     * @param donation The donation to modify
     */
    public void setDonation(Donation donation) {
        this.donation = donation;
        
        try {
            // Load the full collection details from the database using the collection ID
            if (donation.getCollections() != null && donation.getCollections().getId() != null) {
                // Get complete collection details from database
                this.selectedCollection = collectionsService.recupererById(donation.getCollections().getId());
                
                // If collection couldn't be loaded, fallback to the one from donation
                if (this.selectedCollection == null) {
                    this.selectedCollection = donation.getCollections();
                    
                    // Ensure the collection has a user set
                    if (this.selectedCollection.getUser() == null) {
                        this.selectedCollection.setUser(currentUser);
                    }
                }
            } else {
                this.selectedCollection = donation.getCollections();
            }
            
            // Update the donation's collection reference with our fully loaded one
            donation.setCollections(this.selectedCollection);
            
            // Populate UI fields
            populateFields();
            updateCollectionDetails(this.selectedCollection);
            
        } catch (SQLException e) {
            showError("Error loading collection details: " + e.getMessage());
            this.selectedCollection = donation.getCollections();
            populateFields();
            updateCollectionDetails(this.selectedCollection);
        }
    }
    

    
    /**
     * Populate form fields with donation data
     */
    private void populateFields() {
        if (donation != null) {
            // Set donation ID and date labels
            donationIdLabel.setText(String.valueOf(donation.getId()));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            donationDateLabel.setText(donation.getDate().format(formatter));
            
            // Set amount field
            amountField.setText(String.valueOf(donation.getAmount()));
            
            // Set the selected collection label
            selectedCollectionLabel.setText(donation.getCollections().getTitle());
        }
    }
    
    /**
     * Update collection details display
     * @param collection The collection to display details for
     */
    private void updateCollectionDetails(Collections collection) {
        if (collection != null) {
            // Set title and description with null checks
            collectionTitleLabel.setText(collection.getTitle() != null ? collection.getTitle() : "No Title");
            collectionDescriptionLabel.setText(collection.getDescription() != null ? collection.getDescription() : "No Description");
            
            // Set goal with null check
            if (collection.getGoal() != null) {
                collectionGoalLabel.setText(String.format("%.2f TND", collection.getGoal()));
            } else {
                collectionGoalLabel.setText("No goal set");
            }
            
            // Set current amount with null check
            if (collection.getCurrentAmount() != null) {
                collectionCurrentAmountLabel.setText(String.format("%.2f TND", collection.getCurrentAmount()));
            } else {
                collectionCurrentAmountLabel.setText("0.00 TND");
            }
            
            // Set status with null check
            if (collection.getStatus() != null) {
                collectionStatusLabel.setText(collection.getStatus().toString());
            } else {
                collectionStatusLabel.setText("Unknown");
            }
            
            // Show the collection details container
            collectionDetailsContainer.setVisible(true);
            
            // Load image if available
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
            collectionDetailsContainer.setVisible(false);
        }
    }
    
    /**
     * Validate the donation amount in real-time
     */
    @FXML
    public void validateAmount(KeyEvent event) {
        String amountText = amountField.getText().trim();
        
        if (amountText.isEmpty()) {
            amountErrorLabel.setText("Please enter an amount");
            amountErrorLabel.setVisible(true);
            saveButton.setDisable(true);
            return;
        }
        
        try {
            // Try to parse as double, but first check if it's an integer format
            double amount;
            if (amountText.matches("\\d+")) {
                // It's an integer format (e.g., "4"), convert to double
                amount = Integer.parseInt(amountText);
            } else {
                // It's already in decimal format (e.g., "4.0")
                amount = Double.parseDouble(amountText);
            }
            
            if (amount <= 0) {
                amountErrorLabel.setText("Amount must be greater than zero");
                amountErrorLabel.setVisible(true);
                saveButton.setDisable(true);
                return;
            }
            
            // Calculate the difference between new and original amounts
            double originalAmount = donation.getAmount();
            double amountDifference = amount - originalAmount;
            
            // Only check token balance if the user is increasing the donation amount
            if (amountDifference > 0) {
                // Convert to integer tokens (round up)
                int tokensNeeded = (int)Math.ceil(amountDifference);
                
                // Check if user has enough tokens for the increase
                if (tokensNeeded > userTokenBalance) {
                    amountErrorLabel.setText("Not enough tokens. You need " + tokensNeeded + 
                                           " tokens, but have only " + userTokenBalance);
                    amountErrorLabel.setVisible(true);
                    saveButton.setDisable(true);
                    return;
                }
            }
            
            // If we get here, the amount is valid
            amountErrorLabel.setVisible(false);
            saveButton.setDisable(false);
        } catch (NumberFormatException e) {
            amountErrorLabel.setText("Please enter a valid number");
            amountErrorLabel.setVisible(true);
            saveButton.setDisable(true);
        }
    }
    
    /**
     * Save the modified donation
     */
    @FXML
    public void saveDonation(ActionEvent event) {
        // Validate input
        String amountText = amountField.getText().trim();
        if (amountText.isEmpty()) {
            amountErrorLabel.setText("Please enter an amount");
            amountErrorLabel.setVisible(true);
            return;
        }
        
        try {
            // Parse the new amount, handling both integer and decimal formats
            double newAmount;
            if (amountText.matches("\\d+")) {
                // It's an integer format (e.g., "4"), convert to double
                newAmount = Integer.parseInt(amountText);
            } else {
                // It's already in decimal format (e.g., "4.0")
                newAmount = Double.parseDouble(amountText);
            }
            
            if (newAmount <= 0) {
                amountErrorLabel.setText("Amount must be greater than zero");
                amountErrorLabel.setVisible(true);
                return;
            }
            
            // Get the original amount
            double originalAmount = donation.getAmount();
            
            // Calculate the difference between new and original amounts
            double amountDifference = newAmount - originalAmount;
            
            // If no change in amount, just return success
            if (amountDifference == 0) {
                showSuccessAlert();
                onBackClick(null);
                return;
            }
            
            // Get the original collection
            Collections originalCollection = donation.getCollections();
            if (originalCollection == null) {
                showError("Collection not found. Cannot update donation.");
                return;
            }
            
            // Get collection owner (receiver of tokens)
            User collectionOwner = originalCollection.getUser();
            if (collectionOwner == null) {
                showError("Collection owner not found. Cannot update donation.");
                return;
            }
            
            // Start transaction - transfer tokens between users
            UserService userService = new UserService();
            boolean tokenTransferSuccess = false;
            int tokenAmount = 0;
            
            try {
                // Convert amount difference to integer tokens (round up for positive, round down for negative)
                tokenAmount = (int)Math.ceil(Math.abs(amountDifference));
                
                // Make sure we have at least 1 token to transfer
                if (tokenAmount < 1) {
                    tokenAmount = 1;
                }
                
                // Check if user has enough tokens for an increase
                if (amountDifference > 0) {
                    // Get latest token balance to ensure accuracy
                    User latestUser = userService.getById(currentUser.getId());
                    int latestTokenBalance = latestUser.getTokens() != null ? latestUser.getTokens() : 0;
                    
                    if (latestTokenBalance < tokenAmount) {
                        showError("You don't have enough tokens. You need " + tokenAmount + " tokens.");
                        return;
                    }
                    
                    // Increasing donation - transfer tokens from current user to collection owner
                    tokenTransferSuccess = userService.transferTokens(
                        currentUser.getId(), 
                        collectionOwner.getId(), 
                        tokenAmount
                    );
                } else {
                    // Check if collection owner has enough tokens for a decrease
                    User latestOwner = userService.getById(collectionOwner.getId());
                    int latestOwnerBalance = latestOwner.getTokens() != null ? latestOwner.getTokens() : 0;
                    
                    if (latestOwnerBalance < tokenAmount) {
                        showError("The collection owner doesn't have enough tokens to process this refund.");
                        return;
                    }
                    
                    // Decreasing donation - transfer tokens from collection owner to current user
                    tokenTransferSuccess = userService.transferTokens(
                        collectionOwner.getId(), 
                        currentUser.getId(), 
                        tokenAmount
                    );
                }
                
                if (!tokenTransferSuccess) {
                    showError("Failed to transfer tokens. Please try again.");
                    return;
                }
                
                // Update donation with new amount
                donation.setAmount(newAmount);
                
                // Save donation to database
                boolean donationUpdateSuccess = donationService.modifier(donation);
                
                if (!donationUpdateSuccess) {
                    showError("Failed to update donation. Please try again.");
                    
                    // Attempt to revert the token transfer
                    try {
                        if (amountDifference > 0) {
                            // Return tokens from collection owner to current user
                            userService.transferTokens(collectionOwner.getId(), currentUser.getId(), tokenAmount);
                        } else {
                            // Return tokens from current user to collection owner
                            userService.transferTokens(currentUser.getId(), collectionOwner.getId(), tokenAmount);
                        }
                    } catch (SQLException ex) {
                        System.err.println("Failed to revert token transfer: " + ex.getMessage());
                    }
                    return;
                }
                
                // Update the collection amount
                double newCollectionAmount = originalCollection.getCurrentAmount() + amountDifference;
                originalCollection.setCurrentAmount(Math.max(0, newCollectionAmount));
                
                // Update collection status if needed
                if (originalCollection.getGoal() != null && newCollectionAmount >= originalCollection.getGoal()) {
                    originalCollection.setStatus(enums.CollectionStatus.REACHED);
                } else {
                    originalCollection.setStatus(enums.CollectionStatus.IN_PROGRESS);
                }
                
                // Save collection changes
                boolean collectionUpdateSuccess = collectionsService.modifier(originalCollection);
                
                if (!collectionUpdateSuccess) {
                    showError("Donation updated but failed to update collection details.");
                    return;
                }
                
                // Refresh user token balance display
                loadUserTokenBalance();
                
                // Show success message and go back
                showSuccessAlert();
                onBackClick(null);
                
            } catch (SQLException e) {
                showError("Database error: " + e.getMessage());
            }
            
        } catch (NumberFormatException e) {
            amountErrorLabel.setText("Please enter a valid number");
            amountErrorLabel.setVisible(true);
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }
    
    /**
     * Show error message
     * @param message Error message to display
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show success alert
     */
    private void showSuccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Donation #" + donation.getId() + " has been successfully updated.");
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
                SceneSwitch.switchScene((javafx.scene.layout.Pane) mainRouter, "/AfficherDonations.fxml");
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
