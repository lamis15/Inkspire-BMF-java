package Controllers.Event;

import entities.Event;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import service.EventService;
import utils.SceneSwitch;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherEvent {

    @FXML
    private FlowPane cardsContainer; // Conteneur FlowPane défini dans le FXML

    @FXML
    private VBox rootVBox;

    private final EventService service = new EventService(); // Service pour récupérer les événements

    @FXML
    public void initialize() {
        try {
            // Récupérer la liste des événements depuis la base
            List<Event> list = service.recuperer();

            // Pour chaque événement, charger un card FXML et l'ajouter dans le FlowPane
            for (Event c : list) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventDetails.fxml"));
                Node eventCard = loader.load();

                // Récupération du contrôleur associé à EventDetails.fxml
                EventDetails controller = loader.getController();
                controller.setEvent(c); // Passer l'objet Event au contrôleur

                // Ajouter la card dans le FlowPane (horizontalement)
                cardsContainer.getChildren().add(eventCard);
            }

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}
