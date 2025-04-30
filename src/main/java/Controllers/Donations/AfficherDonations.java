package Controllers.Donations;

import entities.Collections;
import javafx.scene.control.ListView;
import entities.Donation;
import service.CollectionsService;
import entities.Session;
import entities.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import service.DonationService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

public class AfficherDonations implements Initializable {

    @FXML
    private Label pageTitle;

    @FXML
    private Label totalDonationsLabel;

    @FXML
    private Label totalAmountLabel;

    @FXML
    private Label collectionsCountLabel;

    @FXML
    private ListView<Donation> donationsListView; // New ListView for donations

    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> sortComboBox;

    private final DonationService donationService = new DonationService();
    private List<Donation> donations;
    private boolean isAdmin = false;
    
    // Sort options
    private static final String SORT_DATE_NEWEST = "Newest";
    private static final String SORT_DATE_OLDEST = "Oldest";
    private static final String SORT_AMOUNT_HIGHEST = "Amount ↑";
    private static final String SORT_AMOUNT_LOWEST = "Amount ↓";
    private static final String SORT_COLLECTION_ASC = "Collection A-Z";
    private static final String SORT_COLLECTION_DESC = "Collection Z-A";
    private static final String SORT_DONOR_ASC = "Donor A-Z";
    private static final String SORT_DONOR_DESC = "Donor Z-A";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Check if user is logged in
        User currentUser = Session.getCurrentUser();
        if (currentUser == null) {
            // Handle not logged in state
            Alert alert = new Alert(Alert.AlertType.ERROR, "You must be logged in to view donations");
            alert.showAndWait();
            return;
        }

        // Check if user is admin
        isAdmin = currentUser.getRole() != null && currentUser.getRole() == 1;

        // Update UI based on user role
        if (isAdmin) {
            pageTitle.setText("All Donations");
        } else {
            pageTitle.setText("My Donations");
        }
        
        // Initialize sort combo box
        initializeSortComboBox();
        
        // Set up search field listener for real-time filtering
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAndSortDonations();
        });
        
        // Set up sort combo box listener
        sortComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                filterAndSortDonations();
            }
        });

        // Load donations
        loadDonations();

        // Set up ListView cell factory
        setupListViewCellFactory();

        // Update statistics
        updateStatistics();
    }
    
    /**
     * Initialize the sort combo box with sorting options
     */
    private void initializeSortComboBox() {
        ObservableList<String> sortOptions = FXCollections.observableArrayList(
            SORT_DATE_NEWEST,
            SORT_DATE_OLDEST,
            SORT_AMOUNT_HIGHEST,
            SORT_AMOUNT_LOWEST,
            SORT_COLLECTION_ASC,
            SORT_COLLECTION_DESC
        );
        
        // Add donor sort options only for admin view
        if (isAdmin) {
            sortOptions.addAll(SORT_DONOR_ASC, SORT_DONOR_DESC);
        }
        
        sortComboBox.setItems(sortOptions);
        
        // Default sort by newest first
        sortComboBox.setValue(SORT_DATE_NEWEST);
    }
    
    /**
     * Filter and sort donations based on search text and sort option
     */
    private void filterAndSortDonations() {
        if (donations == null) {
            return;
        }
        
        // Use service to filter and sort donations
        List<Donation> processedDonations = donationService.filterAndSortDonations(
            donations, 
            searchField.getText(), 
            sortComboBox.getValue()
        );
        
        // Update the ListView
        donationsListView.setItems(FXCollections.observableArrayList(processedDonations));
        
        // Update statistics based on filtered results
        updateStatistics(processedDonations);
    }

    private void setupListViewCellFactory() {
        donationsListView.setCellFactory(listView -> new ListCell<Donation>() {
            private final Button modifyButton = new Button("Modify");
            private final Button refundButton = new Button("Refund");
            private final HBox buttonBox = new HBox(10, modifyButton, refundButton);
            {
                modifyButton.getStyleClass().addAll("action-btn", "modify-btn");
                refundButton.getStyleClass().addAll("action-btn", "refund-btn");
            }
            @Override
            protected void updateItem(Donation donation, boolean empty) {
                super.updateItem(donation, empty);
                if (empty || donation == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Create Labels for each column
                    Label dateLabel = new Label(donation.getDate() != null ? donation.getDate().toString() : "-");
                    Label collectionLabel = new Label(donation.getCollections() != null ? donation.getCollections().getTitle() : "Unknown Collection");
                    Label amountLabel = new Label(String.format("%.2f TND", donation.getAmount()));
                    // Make fields expand evenly and closely
                    dateLabel.setMaxWidth(Double.MAX_VALUE);
                    collectionLabel.setMaxWidth(Double.MAX_VALUE);
                    amountLabel.setMaxWidth(Double.MAX_VALUE);
                    HBox.setHgrow(dateLabel, javafx.scene.layout.Priority.ALWAYS);
                    HBox.setHgrow(collectionLabel, javafx.scene.layout.Priority.ALWAYS);
                    HBox.setHgrow(amountLabel, javafx.scene.layout.Priority.ALWAYS);
                    buttonBox.setPrefWidth(120);
                    // Layout for row
                    HBox rowBox = new HBox(dateLabel, collectionLabel, amountLabel, buttonBox);
                    rowBox.setSpacing(2);
                    rowBox.getStyleClass().add("listview-row");
                    setText(null);
                    setGraphic(rowBox);
                    // Action handlers
                    modifyButton.setOnAction(e -> onModifyDonation(donation));
                    refundButton.setOnAction(e -> onRefundDonation(donation));
                }
            }
        });
    }

    private void loadDonations() {
        try {
            User currentUser = Session.getCurrentUser();

            // Get donations based on user role
            if (isAdmin) {
                // Admin sees all donations
                donations = donationService.recuperer();
            } else {
                // Regular user sees only their donations
                donations = donationService.getDonationsByUserId(currentUser.getId());
            }

            // Apply filtering and sorting
            filterAndSortDonations();

        } catch (SQLException e) {
            System.out.println("Error loading donations: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error loading donations: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Update statistics based on the provided list of donations
     * 
     * @param donationsList The list of donations to calculate statistics from
     */
    private void updateStatistics(List<Donation> donationsList) {
        if (donationsList == null || donationsList.isEmpty()) {
            totalDonationsLabel.setText("0");
            totalAmountLabel.setText("0 TND");
            collectionsCountLabel.setText("0");
            return;
        }

        // Count total donations
        totalDonationsLabel.setText(String.valueOf(donationsList.size()));

        // Calculate total amount
        double totalAmount = donationsList.stream()
                .mapToDouble(Donation::getAmount)
                .sum();
        totalAmountLabel.setText(String.format("%.2f TND", totalAmount));

        // Count unique collections
        Set<Integer> uniqueCollections = new HashSet<>();
        for (Donation donation : donationsList) {
            if (donation.getCollections() != null) {
                uniqueCollections.add(donation.getCollections().getId());
            }
        }
        collectionsCountLabel.setText(String.valueOf(uniqueCollections.size()));
    }
    
    /**
     * Update statistics based on all donations
     */
    private void updateStatistics() {
        // Get the current items in the ListView
        ObservableList<Donation> currentItems = donationsListView.getItems();
        if (currentItems != null && !currentItems.isEmpty()) {
            updateStatistics(new ArrayList<>(currentItems));
        } else if (donations != null) {
            updateStatistics(donations);
        } else {
            updateStatistics(new ArrayList<>());
        }
    }

    private void navigateToCollectionDetails(Collections collection) {
        try {
            // Fetch full collection details by ID
            CollectionsService collectionsService = new CollectionsService();
            Collections fullCollection = collectionsService.recupererById(collection.getId());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CollectionDetails.fxml"));
            Parent root = loader.load();

            // Pass the full collection to the controller
            Controllers.Collections.CollectionDetails controller = loader.getController();
            controller.setCollection(fullCollection);

            // Find the mainRouter and load the view
            Pane mainRouter = (Pane) donationsListView.getScene().getRoot().lookup("#mainRouter");
            if (mainRouter != null) {
                mainRouter.getChildren().clear();
                mainRouter.getChildren().add(root);
            }
        } catch (Exception e) {
            System.out.println("Error navigating to collection details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void onModifyDonation(Donation donation) {
        try {
            System.out.println("Modify donation: " + donation.getId());

            // Load the ModifierDonation view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierDonation.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the donation to modify
            ModifierDonation controller = loader.getController();
            controller.setDonation(donation);

            // Find the mainRouter and load the view
            Pane mainRouter = (Pane) donationsListView.getScene().getRoot().lookup("#mainRouter");
            if (mainRouter != null) {
                mainRouter.getChildren().clear();
                mainRouter.getChildren().add(root);
            }
        } catch (IOException e) {
            System.out.println("Error navigating to modify donation view: " + e.getMessage());
            e.printStackTrace();

            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Navigation Error");
            alert.setContentText("Could not open the donation modification view. Error: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void onRefundDonation(Donation donation) {
        System.out.println("Refund donation: " + donation.getId());

        // Ask for confirmation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Refund");
        confirmAlert.setHeaderText("Refund Donation");
        confirmAlert.setContentText("Are you sure you want to refund this donation of " +
                String.format("%.2f TND", donation.getAmount()) + " from " +
                donation.getCollections().getTitle() + "?");

        confirmAlert.showAndWait();
        if (confirmAlert.getResult() == ButtonType.OK) {
            try {
                // Process the refund
                boolean refundSuccess = processDonationRefund(donation);

                if (refundSuccess) {
                    // Show success message
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Refund Successful");
                    alert.setHeaderText("Donation Refunded");
                    alert.setContentText("Your donation of " + String.format("%.2f TND", donation.getAmount()) +
                            " has been refunded successfully. The tokens have been returned to your account.");
                    alert.showAndWait();

                    // Remove refunded donation from the list and update UI
                    donations.remove(donation);
                    donationsListView.getItems().remove(donation);
                    updateStatistics();
                } else {
                    // Show error message
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Refund Failed");
                    alert.setHeaderText("Donation Refund Failed");
                    alert.setContentText("There was an error processing your refund. Please try again later.");
                    alert.showAndWait();
                }
            } catch (Exception e) {
                // Show error message with details
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Refund Error");
                alert.setHeaderText("Donation Refund Error");
                alert.setContentText("Error: " + e.getMessage());
                alert.showAndWait();
                e.printStackTrace();
            }
        }
    }

    /**
     * Process a donation refund
     * @param donation The donation to refund
     * @return true if the refund was successful, false otherwise
     */
    private boolean processDonationRefund(Donation donation) throws SQLException {
        // Get services
        DonationService donationService = new DonationService();
        service.CollectionsService collectionsService = new service.CollectionsService();
        service.UserService userService = new service.UserService();

        // Get the donation amount
        double donationAmount = donation.getAmount();

        // Get the collection and its owner
        Collections collection = donation.getCollections();
        if (collection == null) {
            throw new SQLException("Collection not found for donation");
        }

        // Load the full collection details from the database to ensure we have the owner
        Collections fullCollection = collectionsService.recupererById(collection.getId());
        if (fullCollection == null) {
            throw new SQLException("Could not load complete collection details");
        }

        // Get the collection owner (who received the tokens)
        User collectionOwner = fullCollection.getUser();
        if (collectionOwner == null) {
            throw new SQLException("Collection owner not found");
        }

        // Get the donor (who will receive the tokens back)
        User donor = donation.getUser();
        if (donor == null) {
            throw new SQLException("Donor not found");
        }

        // Calculate tokens to transfer (round up to ensure full refund)
        int tokensToRefund = (int)Math.ceil(donationAmount);

        // Check if collection owner has enough tokens for the refund
        User collectionOwnerWithTokens = userService.getById(collectionOwner.getId());
        if (collectionOwnerWithTokens == null) {
            throw new SQLException("Could not retrieve collection owner details");
        }

        int ownerTokens = collectionOwnerWithTokens.getTokens() != null ? collectionOwnerWithTokens.getTokens() : 0;
        if (ownerTokens < tokensToRefund) {
            throw new SQLException("Collection owner doesn't have enough tokens for the refund");
        }

        // Start the refund process

        // 1. Transfer tokens from collection owner back to donor
        boolean tokenTransferSuccess = userService.transferTokens(
                collectionOwner.getId(),
                donor.getId(),
                tokensToRefund
        );

        if (!tokenTransferSuccess) {
            throw new SQLException("Failed to transfer tokens during refund");
        }

        // 2. Update the collection's current amount
        // We need to use the fullCollection object which has all properties properly set
        double newCollectionAmount = fullCollection.getCurrentAmount() - donationAmount;
        fullCollection.setCurrentAmount(Math.max(0, newCollectionAmount));

        // Update collection status if needed
        if (fullCollection.getGoal() != null && newCollectionAmount < fullCollection.getGoal()) {
            fullCollection.setStatus(enums.CollectionStatus.IN_PROGRESS);
        }

        // Make sure the user property is set before updating
        // This prevents NullPointerException in collectionsService.modifier
        if (fullCollection.getUser() == null) {
            fullCollection.setUser(collectionOwner);
        }

        boolean collectionUpdateSuccess = collectionsService.modifier(fullCollection);
        if (!collectionUpdateSuccess) {
            // If collection update failed, try to revert token transfer
            try {
                userService.transferTokens(donor.getId(), collectionOwner.getId(), tokensToRefund);
            } catch (SQLException ex) {
                // Log the error but continue with the exception from the collection update
                System.err.println("Failed to revert token transfer: " + ex.getMessage());
            }
            throw new SQLException("Failed to update collection during refund");
        }

        // 3. Delete the donation from the database
        boolean donationDeleteSuccess = donationService.supprimer(donation.getId());
        if (!donationDeleteSuccess) {
            // If donation deletion failed, try to revert everything
            try {
                // Revert collection update
                collection.setCurrentAmount(collection.getCurrentAmount() + donationAmount);
                if (collection.getGoal() != null && collection.getCurrentAmount() >= collection.getGoal()) {
                    collection.setStatus(enums.CollectionStatus.REACHED);
                }
                collectionsService.modifier(collection);

                // Revert token transfer
                userService.transferTokens(donor.getId(), collectionOwner.getId(), tokensToRefund);
            } catch (SQLException ex) {
                // Log the error but continue with the exception from the donation deletion
                System.err.println("Failed to revert changes: " + ex.getMessage());
            }
            throw new SQLException("Failed to delete donation during refund");
        }

        // If we got here, the refund was successful
        return true;
    }
}
