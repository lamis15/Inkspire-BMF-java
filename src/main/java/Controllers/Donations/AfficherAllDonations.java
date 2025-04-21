package Controllers.Donations;

import entities.Collections;
import entities.Donation;
import entities.Session;
import entities.User;
import enums.CollectionStatus;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
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
    private TableView<Donation> donationsTable;
    @FXML
    private TableColumn<Donation, String> dateColumn;
    @FXML
    private TableColumn<Donation, String> donorColumn;
    @FXML
    private TableColumn<Donation, String> collectionColumn;
    @FXML
    private TableColumn<Donation, String> amountColumn;
    // No actions column in admin view

    private final DonationService donationService = new DonationService();
    private List<Donation> donations;
    private boolean isAdmin = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Check if user is admin
        User currentUser = Session.getCurrentUser();
        isAdmin = currentUser != null && currentUser.getRole() == 1;
        
        // Set page title
        pageTitle.setText("All Donations");
        
        setupTableColumns();
        loadDonations();
        updateStatistics();
    }

    private void setupTableColumns() {
        // Format date column
        dateColumn.setCellValueFactory(cellData -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            return new SimpleStringProperty(cellData.getValue().getDate().format(formatter));
        });

        // Set donor column to show user name
        donorColumn.setCellValueFactory(cellData -> {
            User donor = cellData.getValue().getUser();
            return new SimpleStringProperty(donor != null ? 
                donor.getFirstName() + " " + donor.getLastName() : "Unknown");
        });

        // Setup collection column with clickable links
        setupCollectionColumn();

        // Format amount column with TND
        amountColumn.setCellValueFactory(cellData -> {
            DecimalFormat df = new DecimalFormat("0.00");
            return new SimpleStringProperty(df.format(cellData.getValue().getAmount()) + " TND");
        });

        // No actions column in admin view
    }

    private void setupCollectionColumn() {
        collectionColumn.setCellValueFactory(cellData -> {
            Collections collection = cellData.getValue().getCollections();
            return new SimpleStringProperty(collection != null ? collection.getTitle() : "Unknown");
        });

        collectionColumn.setCellFactory(col -> new TableCell<Donation, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #4D81F7; -fx-cursor: hand;");
                    setOnMouseClicked(event -> {
                        Donation donation = getTableView().getItems().get(getIndex());
                        navigateToCollectionDetails(donation.getCollections());
                    });
                }
            }
        });
    }

    // No actions column in admin view

    private void loadDonations() {
        try {
            // Load all donations for admin
            donations = donationService.recuperer();
            donationsTable.setItems(FXCollections.observableArrayList(donations));
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load donations");
            alert.setContentText("An error occurred while loading donations: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void updateStatistics() {
        if (donations != null) {
            // Update total donations count
            totalDonationsLabel.setText(String.valueOf(donations.size()));
            
            // Calculate and update total donation amount
            double totalAmount = donations.stream()
                    .mapToDouble(Donation::getAmount)
                    .sum();
            DecimalFormat df = new DecimalFormat("0.00");
            totalAmountLabel.setText(df.format(totalAmount) + " TND");
            
            // Calculate and update unique collections count
            Set<Integer> uniqueCollections = donations.stream()
                    .map(d -> d.getCollections().getId())
                    .collect(Collectors.toSet());
            collectionsCountLabel.setText(String.valueOf(uniqueCollections.size()));
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
            Pane mainRouter = (Pane) donationsTable.getScene().getRoot().lookup("#mainRouter");
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
