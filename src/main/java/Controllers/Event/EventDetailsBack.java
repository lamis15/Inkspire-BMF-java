package Controllers.Event;

import Controllers.Collections.ModifierCollections;
import entities.Artwork;
import entities.Category;
import entities.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.CategoryService;
import service.EventService;
import utils.SceneSwitch;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class EventDetailsBack implements javafx.fxml.Initializable {

    public Label imagelabel;
    @FXML
    private Label titleLabel;

    @FXML
    private Label getStartingDatelabel;

    @FXML
    private Label locationlabel;

    @FXML
    private Label statusLabel;

    @FXML
    private ImageView imageView;

    @FXML
    private Button deleteButton;

    @FXML
    private Button modifyButton;

    @FXML
    private Button backButton;

    @FXML
    private FlowPane categoryContainer;

    private final EventService eventService = new EventService();
    private final CategoryService categoryService = new CategoryService();

    private Event event;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize the controller
        System.out.println("Initializing EventDetails controller");

        // Configure the FlowPane for proper display of artworks
        categoryContainer.setHgap(15);
        categoryContainer.setVgap(15);
        categoryContainer.setPrefWrapLength(700);
    }

    public void setEvent(Event event) {
        System.out.println("Setting collection: " + event.getId() + " - " + event.getTitle());
        this.event = event;

        // Set collection details
        titleLabel.setText(event.getTitle());
        locationlabel.setText(event.getLocation());
        imagelabel.setText(event.getImage());

        if (event.getImage() != null && !event.getImage().isEmpty()) {
            try {
                Image image = new Image(event.getImage());
                imageView.setImage(image);
            } catch (Exception e) {
                System.out.println("Error loading image: " + e.getMessage());
            }
        }


        // Set up delete button action
        deleteButton.setOnAction(e -> {
            try {
                eventService.supprimer(event.getId());
                // Navigate back to event list using mainRouter
                Node mainRouter = deleteButton.getScene().getRoot().lookup("#mainRouter");
                if (mainRouter instanceof Pane) {
                    SceneSwitch.switchScene((Pane) mainRouter, "/AfficherEvent.fxml");
                    System.out.println("Successfully navigated back to event view after delete");
                } else {
                    System.out.println("Could not find mainRouter for navigation after delete");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

    }


    @FXML
    private void onBackClick() {
        // Find the mainRouter in the scene graph
        Node node = backButton.getScene().getRoot().lookup("#mainRouter");

        if (node instanceof Pane) {
            SceneSwitch.switchScene((Pane) node, "/AfficherEvent.fxml");
            System.out.println("Successfully navigated back to events view");
        } else {
            System.out.println("Could not find mainRouter for navigation");
        }
    }
}
