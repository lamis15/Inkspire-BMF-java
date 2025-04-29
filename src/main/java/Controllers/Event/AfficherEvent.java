package Controllers.Event;

import entities.Event;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import utils.SceneSwitch;
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
    private FlowPane cardsContainer;

    @FXML
    private VBox rootVBox;

    @FXML
    private TextField searchField;

    private final EventService service = new EventService();

    private List<Event> eventList;

    @FXML
    public void initialize() {
        try {
            eventList = service.recuperer();
            System.out.println("Retrieved " + (eventList != null ? eventList.size() : 0) + " events");
            showEvents(eventList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showEvents(List<Event> list) {
        cardsContainer.getChildren().clear();
        try {
            for (Event c : list) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventUtils/EventDetails.fxml"));
                Node eventCard = loader.load();
                EventDetails controller = loader.getController();
                controller.setEvent(c);
                cardsContainer.getChildren().add(eventCard);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSearch() {
        String searchText = searchField.getText().toLowerCase();
        List<Event> filteredEvents = eventList.stream()
                .filter(event -> event.getTitle().toLowerCase().contains(searchText))
                .collect(Collectors.toList());
        showEvents(filteredEvents);
    }

    @FXML
    private void onCalendarButtonClick(ActionEvent event) {
        try {
            // Load Calendrier.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventUtils/Calendrier.fxml"));
            Node calendarRoot = loader.load();

            // Get the controller and pass the eventList
            CalendarViewController controller = loader.getController();
            if (eventList != null && !eventList.isEmpty()) {
                controller.setEventList(eventList);
            } else {
                System.err.println("Event list is null or empty");
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                alert.setTitle("Aucun événement");
                alert.setHeaderText(null);
                alert.setContentText("Aucun événement disponible pour afficher dans le calendrier.");
                alert.showAndWait();
                return;
            }

            // Set the new root directly
            rootVBox.getChildren().setAll(calendarRoot);

        } catch (IOException e) {
            e.printStackTrace();
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Erreur lors du chargement du calendrier : " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void onSort(ActionEvent event) {
        String selectedOption = sortComboBox.getValue();
        List<Event> sortedList = new ArrayList<>(eventList);

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