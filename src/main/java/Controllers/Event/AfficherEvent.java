package Controllers.Event;

import entities.Event;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
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
            showEvents(eventList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showEvents(List<Event> list) {
        cardsContainer.getChildren().clear();
        try {
            for (Event c : list) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventDetails.fxml"));
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

    @FXML
    private void onCalendarButtonClick(ActionEvent event) {
        try {
            // Debug: Print resource path
            System.out.println("Loading Calendrier.fxml from: " + getClass().getResource("/Calendrier.fxml"));

            // Load the CalendarView FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Calendrier.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("Cannot find Calendrier.fxml");
            }
            Parent calendarView = loader.load();

            // Get the CalendarViewController and pass the eventList
            CalendarViewController controller = loader.getController();
            controller.setEventList(eventList);

            // Get the stage from the event source
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(calendarView);
            stage.setScene(scene);
            stage.setTitle("Event Calendar");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load calendar");
            alert.setContentText("Could not find Calendrier.fxml. Please check the file path.");
            alert.showAndWait();
        }
    }
}