package Controllers.Collections;

import entities.Collections;
import entities.Session;
import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import service.CollectionsService;
import utils.SceneSwitch;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class AfficherCollections {

    @FXML
    private FlowPane cardsContainer;

    @FXML
    private VBox rootVBox;
    
    @FXML
    private Button myCollectionsButton;

    private final CollectionsService service = new CollectionsService();
    
    private boolean showingMyCollections = false;

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
        
        loadAllCollections();
    }
    
    /**
     * Load all collections from the database and display them
     */
    private void loadAllCollections() {
        try {
            // Clear existing cards
            cardsContainer.getChildren().clear();
            
            // Get all collections
            List<Collections> list = service.recuperer();

            // Update button text
            myCollectionsButton.setText("My Collections");
            showingMyCollections = false;

            // Display collections
            for (Collections c : list) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/CollectionCard.fxml"));
                Node card = loader.load();

                CollectionCard controller = loader.getController();
                controller.setData(c);

                cardsContainer.getChildren().add(card);
            }

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Load only collections belonging to the current user
     */
    private void loadMyCollections() {
        try {
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
            
            // Clear existing cards
            cardsContainer.getChildren().clear();
            
            // Get all collections
            List<Collections> allCollections = service.recuperer();
            
            // Filter collections to show only those belonging to the current user
            List<Collections> userCollections = allCollections.stream()
                    .filter(c -> c.getUser() != null && c.getUser().getId() == currentUser.getId())
                    .collect(Collectors.toList());
            // Update button text
            myCollectionsButton.setText("All Collections");
            showingMyCollections = true;
            
            // Display user collections
            if (userCollections.isEmpty()) {
                // No collections found for this user
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("No Collections");
                alert.setHeaderText(null);
                alert.setContentText("You don't have any collections yet. Create one by clicking the + button.");
                alert.showAndWait();
                
                // Switch back to all collections view
                loadAllCollections();
                return;
            }
            
            for (Collections c : userCollections) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/CollectionCard.fxml"));
                Node card = loader.load();

                CollectionCard controller = loader.getController();
                controller.setData(c);

                cardsContainer.getChildren().add(card);
            }

        } catch (IOException | SQLException e) {
            e.printStackTrace();
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
}
