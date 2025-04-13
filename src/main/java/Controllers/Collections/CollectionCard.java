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

import java.io.IOException;

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

    private Collections collection;

    public void setData(Collections collection) {
        this.collection = collection;

        titleLabel.setText(collection.getTitle());
        descriptionLabel.setText(collection.getDescription());
        goalLabel.setText("Goal: " + collection.getGoal() + " TND");
        statusLabel.setText("Status: " + collection.getStatus());

        if (collection.getImage() != null && !collection.getImage().isEmpty()) {
            try {
                Image image = new Image(collection.getImage());
                imageView.setImage(image);
            } catch (Exception e) {
                System.out.println("Image loading failed: " + e.getMessage());
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
            
            // Get the controller and set the collection
            CollectionDetails controller = loader.getController();
            controller.setCollection(collection);
            
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
