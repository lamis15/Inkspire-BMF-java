package Controllers.Event;

import entities.Event;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.EventService;
import utils.SceneSwitch;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherEventBack extends SceneSwitch {

    @FXML
    private FlowPane cardsContainer;

    @FXML
    private VBox rootVBox;

    private final EventService eventService = new EventService();

    @FXML
    public void initialize() {
        loadEventCards();
    }

    @FXML
    public void loadEventCards() {
        cardsContainer.getChildren().clear();

        try {
            List<Event> events = eventService.recuperer();
            for (Event event : events) {
                cardsContainer.getChildren().add(createEventCard(event));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des événements : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private VBox createEventCard(Event event) {
        VBox card = new VBox();
        card.setSpacing(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setMinWidth(250);
        card.setMaxWidth(250);

        Label titleLabel = new Label(event.getTitle());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label locationLabel = new Label("Lieu: " + event.getLocation());
        locationLabel.setStyle("-fx-text-fill: #555;");

        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);

        Button modifyButton = new Button("Modifier");
        modifyButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        modifyButton.setOnAction(e -> handleModifyEvent(event));

        Button deleteButton = new Button("Supprimer");
        deleteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
        deleteButton.setOnAction(e -> handleDeleteEvent(event));

        buttonsBox.getChildren().addAll(modifyButton, deleteButton);

        card.getChildren().addAll(titleLabel, locationLabel, buttonsBox);

        return card;
    }

    @FXML
    private void onAddClick(ActionEvent event) throws IOException {
        switchScene(rootVBox, "/AjouterEvent.fxml");
    }

    @FXML
    private void onCalendarClick(ActionEvent event) throws IOException {
        switchScene(rootVBox, "/Calendrier.fxml");
    }

    private void handleModifyEvent(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierEvent.fxml"));
            VBox modifierRoot = loader.load();

            ModifierEvent controller = loader.getController();
            controller.setEvent(event);

            Stage stage = new Stage();
            stage.setTitle("Modifier l'Événement");
            stage.setScene(new javafx.scene.Scene(modifierRoot));
            stage.showAndWait();

            loadEventCards();
        } catch (IOException e) {
            System.err.println("Erreur lors de la modification de l'événement : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDeleteEvent(Event event) {
        try {
            eventService.supprimer(event.getId());
            loadEventCards();
            System.out.println("Événement supprimé : " + event.getTitle());
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'événement : " + e.getMessage());
            e.printStackTrace();
        }
    }
}