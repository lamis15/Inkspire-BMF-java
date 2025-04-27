package Controllers.Event;

import entities.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import utils.SceneSwitch;

import java.io.IOException;

public class EventCard {
    @FXML
    private VBox root; // Add a field to hold the root VBox
    @FXML
    private Label startingDateLabel;
    @FXML
    private Label endingDateLabel;
    @FXML
    private Label latitudeLabel;
    @FXML
    private Label longitudeLabel;
    @FXML
    private Label categoryLabel;
    @FXML
    private ImageView eventImage;
    @FXML
    private Label titleLabel;
    @FXML
    private Label locationLabel;
    @FXML
    private Button voirDetailsButton;

    private Event event;
    private Pane container;


    public EventCard() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/EventUtils/EventCard.fxml"));
        fxmlLoader.setController(this); // Set this instance as the controller
        try {
            fxmlLoader.load(); // Load the FXML, which will create the VBox
        } catch (IOException exception) {
            throw new RuntimeException("Failed to load EventCard.fxml: " + exception.getMessage(), exception);
        }
    }

    public VBox getRoot() {
        return root; // Provide access to the root VBox
    }

    public void setContainer(Pane container) {
        this.container = container;
    }

    public void setEvent(Event event) {
        this.event = event;
        titleLabel.setText(event.getTitle());
        locationLabel.setText("Lieu: " + event.getLocation());
        startingDateLabel.setText("Start Date: " + event.getStartingDate().toString());
        endingDateLabel.setText("End Date: " + event.getEndingDate().toString());
        latitudeLabel.setText("Latitude: " + String.valueOf(event.getLatitude()));
        longitudeLabel.setText("Longitude: " + String.valueOf(event.getLongitude()));
        categoryLabel.setText("Category ID: " + String.valueOf(event.getCategoryId()));

        // Load event image
        if (event.getImage() != null && !event.getImage().isEmpty()) {
            String imagePath = event.getImage().startsWith("file:") ? event.getImage() : "file:" + event.getImage();
            System.out.println("Attempting to load image: " + imagePath); // Debug
            Image image = new Image(imagePath, true); // Load asynchronously
            if (image.isError()) {
                System.out.println("Image loading failed for " + event.getImage() + ": " + image.getException().getMessage());
                eventImage.setImage(new Image("/images/default-event.jpg"));
            } else {
                eventImage.setImage(image);
            }
        } else {
            System.out.println("No image provided for event: " + event.getTitle());
            eventImage.setImage(new Image("/images/default-event.jpg"));
        }
    }

    @FXML
    private void handleVoirDetails() {
        if (container != null) {
            SceneSwitch.switchScene(container, "/EventUtils/EventDetails.fxml");
        } else {
            System.err.println("Container is not set for scene switching");
        }
    }
}