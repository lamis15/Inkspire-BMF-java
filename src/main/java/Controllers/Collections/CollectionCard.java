package Controllers.Collections;

import entities.Collections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.SceneSwitch;
import service.CollectionsService;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class CollectionCard {

    @FXML
    private Label titleLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Label goalLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private ImageView imageView;

    @FXML
    private Label ownerNameLabel;

    private Collections collection;

    public void setData(Collections collection) {
        this.collection = collection;

        titleLabel.setText(collection.getTitle());
        descriptionLabel.setText(collection.getDescription());
        goalLabel.setText("Goal: " + collection.getGoal() + " TND");
        statusLabel.setText("Status: " + collection.getStatus());

        // Set owner name if available
        if (collection.getUser() != null) {
            String ownerName = collection.getUser().getFirstName() + " " + collection.getUser().getLastName();
            ownerNameLabel.setText(ownerName);
        } else {
            ownerNameLabel.setText("Unknown owner");
        }

        if (collection.getImage() != null && !collection.getImage().isEmpty()) {
            try {
                // Create a File object with the full path
                File imageFile = new File(AfficherCollections.COLLECTIONS_IMAGE_PATH + collection.getImage());
                
                // Check if the file exists
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    imageView.setImage(image);
                } else {
                    // Use default image if file doesn't exist
                    Image defaultImage = new Image(getClass().getResourceAsStream("/assets/images/placeholder.png"));
                    imageView.setImage(defaultImage);
                    System.out.println("Image file not found: " + imageFile.getAbsolutePath());
                }
            } catch (Exception e) {
                System.out.println("Image loading failed: " + e.getMessage());
                // Use default image on error
                try {
                    Image defaultImage = new Image(getClass().getResourceAsStream("/assets/images/placeholder.png"));
                    imageView.setImage(defaultImage);
                } catch (Exception ex) {
                    System.out.println("Default image loading failed: " + ex.getMessage());
                }
            }
        } else {
            // Use default image if no image is set
            try {
                Image defaultImage = new Image(getClass().getResourceAsStream("/assets/images/placeholder.png"));
                imageView.setImage(defaultImage);
            } catch (Exception e) {
                System.out.println("Default image loading failed: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCardClick(MouseEvent event) {
        try {
            System.out.println("Collection card clicked: " + collection.getId() + " - " + collection.getTitle());

            // Load the CollectionDetails view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CollectionDetails.fxml"));
            Parent detailsView = loader.load();

            // Get the controller and set the collection with full user details
            CollectionDetails controller = loader.getController();

            try {
                // Load the collection with complete user details
                CollectionsService collectionsService = new CollectionsService();
                Collections collectionWithUserDetails = collectionsService.recupererById(collection.getId());

                if (collectionWithUserDetails != null) {
                    controller.setCollection(collectionWithUserDetails);
                } else {
                    // Fallback to the original collection if retrieval fails
                    controller.setCollection(collection);
                }
            } catch (SQLException e) {
                System.out.println("Error loading collection with user details: " + e.getMessage());
                // Fallback to the original collection if an error occurs
                controller.setCollection(collection);
            }

            // Find the mainRouter in the StackPane structure
            Node source = (Node) event.getSource();
            AnchorPane mainRouter = (AnchorPane) source.getScene().getRoot().lookup("#mainRouter");

            if (mainRouter != null) {
                // Clear the mainRouter and add the collection details view
                mainRouter.getChildren().clear();
                mainRouter.getChildren().add(detailsView);

                // Set the anchor constraints to make the view fill the mainRouter
                AnchorPane.setTopAnchor(detailsView, 0.0);
                AnchorPane.setRightAnchor(detailsView, 0.0);
                AnchorPane.setBottomAnchor(detailsView, 0.0);
                AnchorPane.setLeftAnchor(detailsView, 0.0);

                System.out.println("Successfully loaded collection details into mainRouter");
            } else {
                System.out.println("Could not find mainRouter to load collection details");
            }

        } catch (IOException e) {
            System.out.println("Error loading collection details: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
