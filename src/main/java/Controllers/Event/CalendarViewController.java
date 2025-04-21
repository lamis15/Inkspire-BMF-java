package Controllers.Event;

import entities.Event;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class CalendarViewController {
    @FXML
    private GridPane calendarGrid;

    @FXML
    private Label monthYearLabel;

    @FXML
    private Button prevMonthButton;

    @FXML
    private Button nextMonthButton;

    @FXML
    private Button backButton;

    @FXML
    private Button todayButton;

    private List<Event> eventList;
    private YearMonth currentYearMonth;

    public void setEventList(List<Event> eventList) {
        this.eventList = eventList;
        currentYearMonth = YearMonth.now();
        updateCalendar();
    }

    private void updateCalendar() {
        calendarGrid.getChildren().clear();
        monthYearLabel.setText(currentYearMonth.getMonth() + " " + currentYearMonth.getYear());

        // Add day headers
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(days[i]);
            dayLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setPrefSize(60, 40);
            calendarGrid.add(dayLabel, i, 0);
        }

        // Get first day of the month and length of the month
        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() - 1; // 0 = Monday
        int daysInMonth = currentYearMonth.lengthOfMonth();

        // Collect start dates of events in the current month
        List<LocalDate> eventStartDates = eventList.stream()
                .map(Event::getStartingDate)
                .filter(date -> date.getYear() == currentYearMonth.getYear() &&
                        date.getMonth() == currentYearMonth.getMonth())
                .collect(Collectors.toList());

        // Fill the calendar
        int row = 1;
        int col = dayOfWeek;
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);
            Label dayLabel = new Label(String.valueOf(day));
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setPrefSize(60, 60);
            dayLabel.getStyleClass().add("calendar-cell");

            // Highlight start dates
            if (eventStartDates.contains(date)) {
                dayLabel.getStyleClass().add("event-cell");
                // Add tooltip with event titles
                List<String> eventTitles = eventList.stream()
                        .filter(event -> event.getStartingDate().equals(date))
                        .map(Event::getTitle)
                        .collect(Collectors.toList());
                if (!eventTitles.isEmpty()) {
                    Tooltip tooltip = new Tooltip(String.join("\n", eventTitles));
                    Tooltip.install(dayLabel, tooltip);
                }
                // Add click handler to show events
                dayLabel.setOnMouseClicked((MouseEvent mouseEvent) -> showEventsForDate(date));
            }

            calendarGrid.add(dayLabel, col, row);
            col++;
            if (col == 7) {
                col = 0;
                row++;
            }
        }
    }

    private void showEventsForDate(LocalDate date) {
        // Filter events starting on the selected date
        List<Event> eventsOnDate = eventList.stream()
                .filter(event -> event.getStartingDate().equals(date))
                .collect(Collectors.toList());

        // Create a dialog to show events
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Events starting on " + date);
        alert.setHeaderText(null);

        if (eventsOnDate.isEmpty()) {
            alert.setContentText("No events start on this date.");
        } else {
            VBox eventListVBox = new VBox(5);
            for (Event event : eventsOnDate) {
                Label eventLabel = new Label(event.getTitle() + " (" + event.getStartingDate() + " to " + event.getEndingDate() + ")");
                eventListVBox.getChildren().add(eventLabel);
            }
            alert.getDialogPane().setContent(eventListVBox);
        }

        alert.showAndWait();
    }

    @FXML
    private void onPreviousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        updateCalendar();
    }

    @FXML
    private void onNextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        updateCalendar();
    }

    @FXML
    private void onTodayButtonClick() {
        currentYearMonth = YearMonth.now();
        updateCalendar();
    }

    @FXML
    private void onBackButtonClick(ActionEvent event) {
        try {
            System.out.println("Loading AfficherEvent.fxml from: " + getClass().getResource("/AfficherEvent.fxml"));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherEvent.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("Cannot find AfficherEvent.fxml");
            }
            Parent afficherEventView = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(afficherEventView);
            stage.setScene(scene);
            stage.setTitle("Event List");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load event list");
            alert.setContentText("Could not find AfficherEvent.fxml. Please check the file path.");
            alert.showAndWait();
        }
    }
}