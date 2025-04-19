package Controllers.Event;

import entities.Event;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextField;
import service.EventService;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AfficherEvent {
    @FXML
    private ComboBox<String> sortComboBox;

    @FXML
    private FlowPane cardsContainer; // Conteneur FlowPane défini dans le FXML

    @FXML
    private VBox rootVBox;

    @FXML
    private TextField searchField; // Le champ de recherche

    private final EventService service = new EventService(); // Service pour récupérer les événements

    private List<Event> eventList; // Liste pour stocker les événements récupérés

    @FXML

    public void initialize() {
        try {
            // Récupérer la liste des événements depuis la base
            eventList = service.recuperer();

            // Afficher tous les événements au début
            showEvents(eventList);

        } catch (SQLException e) {
            e.printStackTrace(); // Gestion de l'exception SQLException
        }
    }


    // Méthode pour afficher les événements dans le FlowPane
    private void showEvents(List<Event> list) {
        cardsContainer.getChildren().clear(); // Vider les cartes existantes
        try {
            for (Event c : list) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventDetails.fxml"));
                Node eventCard = loader.load();

                // Récupération du contrôleur associé à EventDetails.fxml
                EventDetails controller = loader.getController();
                controller.setEvent(c); // Passer l'objet Event au contrôleur

                // Ajouter la card dans le FlowPane (horizontalement)
                cardsContainer.getChildren().add(eventCard);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méthode de recherche dynamique
    @FXML
    private void onSearch() {
        String searchText = searchField.getText().toLowerCase(); // Récupérer la recherche en minuscules
        List<Event> filteredEvents = eventList.stream()
                .filter(event -> event.getTitle().toLowerCase().contains(searchText)) // Filtrer par titre
                .collect(Collectors.toList());

        // Afficher les événements filtrés
        showEvents(filteredEvents);
    }
    @FXML
    private void onSort(ActionEvent event) {
        String selectedOption = sortComboBox.getValue();
        List<Event> sortedList = new ArrayList<>(eventList); // On fait une copie

        switch (selectedOption) {
            case "Date de début":
                sortedList.sort(Comparator.comparing(Event::getStartingDate));
                break;
            case "Titre A-Z":
                sortedList.sort(Comparator.comparing(Event::getTitle, String.CASE_INSENSITIVE_ORDER));
                break;
            case "Titre Z-A":
                sortedList.sort(Comparator.comparing(Event::getTitle, String.CASE_INSENSITIVE_ORDER).reversed());
                break;
            default:
                break;
        }

        showEvents(sortedList);
    }

}
