package Controllers.Event;

import entities.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import service.EventService;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ModifierEvent implements Initializable {
    @FXML
    private TextField latitudeField;

    @FXML
    private TextField longitudeField;

    @FXML
    private TextField imageField;

    @FXML
    private TextField titleField;

    @FXML
    private DatePicker startingDatePicker;

    @FXML
    private DatePicker endingDatePicker;

    @FXML
    private TextField locationField;

    private Event eventToEdit;
    private final EventService eventService = new EventService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation si nécessaire
    }

    public void setEvent(Event event) {
        this.eventToEdit = event;
        populateFields();
    }

    private void populateFields() {
        titleField.setText(eventToEdit.getTitle());
        startingDatePicker.setValue(eventToEdit.getStartingDate());
        endingDatePicker.setValue(eventToEdit.getEndingDate());
        locationField.setText(eventToEdit.getLocation());
        latitudeField.setText(String.valueOf(eventToEdit.getLatitude()));
        longitudeField.setText(String.valueOf(eventToEdit.getLongitude()));
        imageField.setText(eventToEdit.getImage());
    }


    @FXML
    void saveChanges() {
        eventToEdit.setTitle(titleField.getText());
        eventToEdit.setStartingDate(startingDatePicker.getValue());
        eventToEdit.setEndingDate(endingDatePicker.getValue());
        eventToEdit.setLocation(locationField.getText());
        eventToEdit.setLatitude(Double.parseDouble(latitudeField.getText()));
        eventToEdit.setLongitude(Double.parseDouble(longitudeField.getText()));
        eventToEdit.setImage(imageField.getText());

        try {
            eventService.modifier(eventToEdit);
            closeWindow();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void cancelEdit() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }
}
