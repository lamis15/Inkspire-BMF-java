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
    private VBox root;
    @FXML
    private Label startingDateLabel;
    @FXML
    private Label locationLabel;
    @FXML
    private ImageView eventImage;
    @FXML
    private Label titleLabel;
    @FXML
    private Button voirDetailsButton;

    private Event event;
    private Pane container;

    public EventCard() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/EventUtils/EventCard.fxml"));
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException("Failed to load EventCard.fxml: " + exception.getMessage(), exception);
        }
    }

    public VBox getRoot() {
        return root;
    }

    public void setContainer(Pane container) {
        this.container = container;
    }

    public void setEvent(Event event) {
        this.event = event;
        titleLabel.setText(event.getTitle());
        startingDateLabel.setText("Start Date: " + event.getStartingDate().toString());
        locationLabel.setText("Lieu: " + event.getLocation());

        if (event.getImage() != null && !event.getImage().isEmpty()) {
            String imagePath = event.getImage().startsWith("file:") ? event.getImage() : "file:" + event.getImage();
            Image image = new Image(imagePath, true);
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
        if (container == null) {
            System.err.println("Container is not set for scene switching");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventUtils/EventDetails.fxml"));
            Pane detailsPane = loader.load();
            EventDetails controller = loader.getController();
            controller.setEvent(event);
            container.getChildren().setAll(detailsPane); // Direct replacement
        } catch (IOException e) {
            System.err.println("Failed to load EventDetails.fxml: " + e.getMessage());
        }
    }
}