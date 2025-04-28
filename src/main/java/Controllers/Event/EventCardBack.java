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

public class EventCardBack extends VBox {
    @FXML
    private Label startingDateLabel; // Déclarer la variable pour la date de début

    @FXML
    private Label endingDateLabel;   // Déclarer la variable pour la date de fin

    @FXML
    private Label latitudeLabel;     // Déclarer la variable pour la latitude

    @FXML
    private Label longitudeLabel;    // Déclarer la variable pour la longitude

    @FXML
    private Label categoryLabel;     // Déclarer la variable pour l'ID de catégorie

    @FXML
    private ImageView eventImage;

    @FXML
    private Label titleLabel;

    @FXML
    private Label locationLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Button voirDetailsButton;

    private Event event;
    private Event imageView;

    public EventCardBack() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/EventUtils/EventCard.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void setEvent(Event event) {
        this.event = event;
        titleLabel.setText(event.getTitle());
        locationLabel.setText("Lieu: " + event.getLocation());
        //dateLabel.setText("Date: " + event.getStartingDate() + " - " + event.getEndingDate());

        if (event.getImage() != null && !event.getImage().isEmpty()) {
            try {
                Image image = new Image(event.getImage());
                eventImage.setImage(image);
            } catch (Exception e) {
                // Si l'image ne peut pas être chargée, utiliser une image par défaut
                eventImage.setImage(new Image("/images/default-event.jpg"));
            }
        }
    }
    Pane container;
    @FXML
    private void handleVoirDetails() {
        SceneSwitch.switchScene(container, "/EventUtils/EventDetails.fxml");
    }

    public void setData(Event c) {
        this.titleLabel.setText(c.getTitle());  // Afficher le titre de l'événement
        this.startingDateLabel.setText("Start Date: " + c.getStartingDate().toString()); // Afficher la date de début
        this.endingDateLabel.setText("End Date: " + c.getEndingDate().toString()); // Afficher la date de fin
        this.locationLabel.setText("Location: " + c.getLocation()); // Afficher le lieu
        this.latitudeLabel.setText("Latitude: " + c.getLatitude()); // Afficher la latitude
        this.longitudeLabel.setText("Longitude: " + c.getLongitude()); // Afficher la longitude
        this.categoryLabel.setText("Category ID: " + c.getCategoryId()); // Afficher l'ID de la catégorie

        // Si l'image n'est pas vide ou nulle, l'afficher
        if (c.getImage() != null && !c.getImage().isEmpty()) {
            try {
                Image image = new Image(c.getImage());  // Charger l'image depuis l'URL ou le chemin
                this.eventImage.setImage(image); // Mettre l'image dans l'interface
            } catch (Exception e) {
                System.out.println("Image loading failed: " + e.getMessage());
            }
        } else {
            this.eventImage.setImage(null); // Si l'image est vide ou nulle, ne rien afficher
        }
    }

}
