package Controllers.Donations;

import entities.Collections;
import entities.Donation;
import entities.Session;
import entities.User;
import enums.CollectionStatus;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Callback;
import service.DonationService;
import service.CollectionsService;
import service.UserService;
import utils.SceneSwitch;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

public class AfficherAllDonations implements Initializable {

    @FXML
    private Label pageTitle;
    @FXML
    private Label totalDonationsLabel;
    @FXML
    private Label totalAmountLabel;
    @FXML
    private Label collectionsCountLabel;
    @FXML
    private ListView<Donation> donationsListView;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> sortComboBox;

    // No actions column in admin view

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
        // Check if user is admin
        User currentUser = Session.getCurrentUser();
        isAdmin = currentUser != null && currentUser.getRole() == 1;

        // Set page title
        pageTitle.setText("All Donations");

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

        setupListViewCellFactory();
        loadDonations();
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
            SORT_COLLECTION_DESC,
            SORT_DONOR_ASC,
            SORT_DONOR_DESC
        );

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
        donationsListView.setCellFactory(lv -> new ListCell<Donation>() {
            @Override
            protected void updateItem(Donation donation, boolean empty) {
                super.updateItem(donation, empty);
                if (empty || donation == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label dateLabel = new Label(donation.getDate() != null ? donation.getDate().toString() : "-");
                    Label donorLabel = new Label(donation.getUser() != null ? (donation.getUser().getFirstName() + " " + donation.getUser().getLastName()) : "Unknown Donor");
                    Label collectionLabel = new Label(donation.getCollections() != null ? donation.getCollections().getTitle() : "Unknown Collection");
                    Label amountLabel = new Label(String.format("%.2f TND", donation.getAmount()));
                    dateLabel.setMaxWidth(Double.MAX_VALUE);
                    donorLabel.setMaxWidth(Double.MAX_VALUE);
                    collectionLabel.setMaxWidth(Double.MAX_VALUE);
                    amountLabel.setMaxWidth(Double.MAX_VALUE);
                    HBox.setHgrow(dateLabel, javafx.scene.layout.Priority.ALWAYS);
                    HBox.setHgrow(donorLabel, javafx.scene.layout.Priority.ALWAYS);
                    HBox.setHgrow(collectionLabel, javafx.scene.layout.Priority.ALWAYS);
                    HBox.setHgrow(amountLabel, javafx.scene.layout.Priority.ALWAYS);
                    HBox rowBox = new HBox(dateLabel, donorLabel, collectionLabel, amountLabel);
                    rowBox.setSpacing(2);
                    rowBox.getStyleClass().add("listview-row");
                    setText(null);
                    setGraphic(rowBox);
                }
            }
        });
    }

    // No actions column in admin view

    private void loadDonations() {
        try {
            // Load all donations for admin
            donations = donationService.recuperer();

            // Apply filtering and sorting
            filterAndSortDonations();
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load donations");
            alert.setContentText("An error occurred while loading donations: " + e.getMessage());
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

        // Update total donations count
        totalDonationsLabel.setText(String.valueOf(donationsList.size()));

        // Calculate and update total donation amount
        double totalAmount = donationsList.stream()
                .mapToDouble(Donation::getAmount)
                .sum();
        DecimalFormat df = new DecimalFormat("0.00");
        totalAmountLabel.setText(df.format(totalAmount) + " TND");

        // Calculate and update unique collections count
        Set<Integer> uniqueCollections = donationsList.stream()
                .map(d -> d.getCollections().getId())
                .collect(Collectors.toSet());
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CollectionDetails.fxml"));
            Parent root = loader.load();

            // Pass the collection to the controller
            Controllers.Collections.CollectionDetails controller = loader.getController();
            controller.setCollection(collection);

            // Find the mainRouter and load the view
            Pane mainRouter = (Pane) donationsListView.getScene().getRoot().lookup("#mainRouter");
            if (mainRouter != null) {
                mainRouter.getChildren().clear();
                mainRouter.getChildren().add(root);
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Failed to navigate to collection details");
            alert.setContentText("An error occurred: " + e.getMessage());
            alert.showAndWait();
        }
    }

    // No modify or refund actions in admin view

    // No refund functionality in admin view
}
