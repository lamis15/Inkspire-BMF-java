package Controllers.Collections;

import entities.Collections;
import entities.Session;
import entities.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import service.CollectionsService;
import utils.SceneSwitch;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for displaying collections
 */
public class AfficherCollections {

    @FXML
    private FlowPane cardsContainer;

    @FXML
    private VBox rootVBox;
    
    @FXML
    private Button myCollectionsButton;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> sortComboBox;

    private final CollectionsService service = new CollectionsService();
    
    private boolean showingMyCollections = false;
    private List<Collections> allCollections = new ArrayList<>();
    private List<Collections> userCollections = new ArrayList<>();
    
    // Sort options
    private static final String SORT_TITLE_ASC = "A-Z";
    private static final String SORT_TITLE_DESC = "Z-A";
    private static final String SORT_DATE_NEWEST = "Newest";
    private static final String SORT_DATE_OLDEST = "Oldest";
    private static final String SORT_GOAL_HIGHEST = "Goal ↑";
    private static final String SORT_GOAL_LOWEST = "Goal ↓";
    private static final String SORT_AMOUNT_HIGHEST = "Amount ↑";
    private static final String SORT_AMOUNT_LOWEST = "Amount ↓";
    private static final String SORT_STATUS = "Status";

    @FXML
    public void initialize() {
        // Check user role and hide buttons for admin users
        User currentUser = Session.getCurrentUser();
        if (currentUser != null && currentUser.getRole() != null && currentUser.getRole() == 1) {
            // Admin user - hide add and my collections buttons
            if (myCollectionsButton != null) {
                myCollectionsButton.setVisible(false);
                myCollectionsButton.setManaged(false);
            }
            // Find the add button in the parent HBox
            if (myCollectionsButton != null && myCollectionsButton.getParent() != null) {
                Node addButton = myCollectionsButton.getParent().lookup("#addButton");
                if (addButton != null) {
                    addButton.setVisible(false);
                    addButton.setManaged(false);
                }
            }
        }
        
        // Initialize sort combo box
        initializeSortComboBox();
        
        // Set up search field listener for real-time filtering
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAndSortCollections();
        });
        
        // Set up sort combo box listener
        sortComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                filterAndSortCollections();
            }
        });
        
        // Load all collections initially
        loadAllCollections();
    }
    
    /**
     * Initialize the sort combo box with sorting options
     */
    private void initializeSortComboBox() {
        ObservableList<String> sortOptions = FXCollections.observableArrayList(
            SORT_TITLE_ASC,
            SORT_TITLE_DESC,
            SORT_DATE_NEWEST,
            SORT_DATE_OLDEST,
            SORT_GOAL_HIGHEST,
            SORT_GOAL_LOWEST,
            SORT_AMOUNT_HIGHEST,
            SORT_AMOUNT_LOWEST,
            SORT_STATUS
        );
        
        sortComboBox.setItems(sortOptions);
        
        // Default sort by newest first
        sortComboBox.setValue(SORT_DATE_NEWEST);
    }
    
    /**
     * Filter and sort collections based on search text and sort option
     */
    private void filterAndSortCollections() {
        // Get the source list based on current view
        List<Collections> sourceList = showingMyCollections ? userCollections : allCollections;
        
        // Use service to filter and sort collections
        List<Collections> processedCollections = service.filterAndSortCollections(
            sourceList, 
            searchField.getText(), 
            sortComboBox.getValue()
        );
        
        // Display the processed collections
        displayCollections(processedCollections);
    }
    
    /**
     * Display a list of collections in the UI
     * 
     * @param collections The list of collections to display
     */
    private void displayCollections(List<Collections> collections) {
        try {
            // Clear existing cards
            cardsContainer.getChildren().clear();
            
            // Display collections
            for (Collections c : collections) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/CollectionCard.fxml"));
                Node card = loader.load();

                CollectionCard controller = loader.getController();
                controller.setData(c);

                cardsContainer.getChildren().add(card);
            }
            
            // Show a message if no collections match the search
            if (collections.isEmpty() && !searchField.getText().isEmpty()) {
                // Create a label to show "No results found"
                Label noResultsLabel = new Label("No collections match your search");
                noResultsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666666; -fx-padding: 20px;");
                cardsContainer.getChildren().add(noResultsLabel);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load collection cards: " + e.getMessage());
        }
    }
    
    /**
     * Load all collections from the database and display them
     */
    private void loadAllCollections() {
        try {
            // Get all collections
            allCollections = service.recuperer();

            // Update button text
            myCollectionsButton.setText("My Collections");
            showingMyCollections = false;

            // Apply filtering and sorting
            filterAndSortCollections();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load collections: " + e.getMessage());
        }
    }
    
    /**
     * Load only collections belonging to the current user
     */
    private void loadMyCollections() {
        // Get current user from session
        User currentUser = Session.getCurrentUser();
        if (currentUser == null) {
            // Handle case where user is not logged in
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Authentication Error");
            alert.setHeaderText(null);
            alert.setContentText("You must be logged in to view your collections.");
            alert.showAndWait();
            return;
        }
        
        // Use service to get collections for the current user
        userCollections = service.getUserCollections(allCollections, currentUser.getId());
                
        // Update button text
        myCollectionsButton.setText("All Collections");
        showingMyCollections = true;
        
        // Apply filtering and sorting
        filterAndSortCollections();
        
        // Check if no collections found for this user
        if (userCollections.isEmpty() && (searchField == null || searchField.getText().isEmpty())) {
            // No collections found for this user
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Collections");
            alert.setHeaderText(null);
            alert.setContentText("You don't have any collections yet. Create one by clicking the + button.");
            alert.showAndWait();
            
            // Switch back to all collections view
            loadAllCollections();
        }
    }

    @FXML
    void onAddClick(ActionEvent event) {
        SceneSwitch.switchScene(rootVBox, "/AjouterCollections.fxml");
    }
    
    @FXML
    void onMyCollectionsClick(ActionEvent event) {
        if (showingMyCollections) {
            // Currently showing my collections, switch to all collections
            loadAllCollections();
        } else {
            // Currently showing all collections, switch to my collections
            loadMyCollections();
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
