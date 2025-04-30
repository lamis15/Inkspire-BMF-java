package Controllers.Event;

import entities.Event;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import netscape.javascript.JSObject;
import service.EventService;
import utils.SceneSwitch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class CalendarViewController {

    @FXML
    private WebView calendarWebView;

    private List<Event> eventList;
    private final EventService eventService = new EventService();
    private static Integer selectedEventId; // Static field to store eventId

    // Getter for selectedEventId
    public static Integer getSelectedEventId() {
        return selectedEventId;
    }

    // Setter for selectedEventId
    public static void setSelectedEventId(Integer eventId) {
        selectedEventId = eventId;
    }

    public void setEventList(List<Event> eventList) {
        this.eventList = eventList;
        initializeCalendar();
    }

    private void initializeCalendar() {
        try {
            if (eventList == null || eventList.isEmpty()) {
                throw new IllegalStateException("Event list is null or empty");
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            List<Map<String, Object>> fullCalendarEvents = eventList.stream().map(event -> {
                if (event.getStartingDate() == null) {
                    throw new IllegalArgumentException("Event '" + event.getTitle() + "' has null starting_date");
                }
                Map<String, Object> map = new HashMap<>();
                map.put("id", event.getId());
                map.put("title", event.getTitle() != null ? event.getTitle() : "Untitled");
                map.put("start", event.getStartingDate().toString());
                map.put("location", event.getLocation() != null ? event.getLocation() : "N/A");
                map.put("backgroundColor", "#ff0000");
                map.put("endingDate", event.getEndingDate() != null ? event.getEndingDate().toString() : "N/A");
                map.put("latitude", event.getLatitude());
                map.put("longitude", event.getLongitude());
                map.put("image", event.getImage() != null ? event.getImage() : "N/A");
                map.put("categoryId", event.getCategoryId());
                return map;
            }).collect(Collectors.toList());

            String eventsJson = mapper.writeValueAsString(fullCalendarEvents);

            String htmlContent = generateHtmlContent(eventsJson);

            WebEngine engine = calendarWebView.getEngine();
            engine.setOnError(event -> {
                System.err.println("WebView JavaScript Error: " + event.getMessage());
                showErrorAlert("WebView Error", "JavaScript error in calendar: " + event.getMessage());
            });

            engine.setOnStatusChanged(event -> {
                if (event.getData().equals("DOMContentLoaded")) {
                    JSObject window = (JSObject) engine.executeScript("window");
                    window.setMember("javaBridge", new JavaBridge());
                }
            });

            engine.setOnAlert(event -> System.out.println("JavaScript Alert: " + event.getData()));
            engine.setJavaScriptEnabled(true);
            engine.executeScript("""
                window.console = {
                    log: function(msg) { window.alert('console.log: ' + msg); },
                    error: function(msg) { window.alert('console.error: ' + msg); }
                };
            """);

            engine.loadContent(htmlContent);

            calendarWebView.setStyle("-fx-background-color: white;");

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Failed to Load Calendar", "An error occurred while loading the calendar: " + e.getMessage());
        }
    }

    public class JavaBridge {
        public void showEventDetails(int eventId, String title, String start, String endingDate, String location,
                                     double latitude, double longitude, String image, int categoryId) {
            Platform.runLater(() -> {
                try {
                    // Validate eventId
                    Event selectedEvent = eventService.getEventById(eventId);
                    if (selectedEvent == null) {
                        showErrorAlert("Event Not Found", "The selected event could not be found.");
                        return;
                    }

                    // Store eventId in static field
                    setSelectedEventId(eventId);

                    // Navigate to EventDetails page
                    Node node = calendarWebView.getScene().getRoot().lookup("#mainRouter");
                    if (node instanceof Pane) {
                        SceneSwitch.switchScene((Pane) node, "/EventUtils/EventDetails.fxml");
                    } else {
                        showErrorAlert("Navigation Error", "Could not find mainRouter for navigation.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showErrorAlert("Navigation Error", "Failed to open event details: " + e.getMessage());
                }
            });
        }
    }

    private void showErrorAlert(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String generateHtmlContent(String eventsJson) {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset='utf-8' />
            <link href='https://cdn.jsdelivr.net/npm/fullcalendar@6.1.15/index.global.min.css' rel='stylesheet' />
            <script src='https://cdn.jsdelivr.net/npm/fullcalendar@6.1.15/index.global.min.js'></script>
            <style>
                html, body {
                    margin: 0;
                    padding: 0;
                    font-family: Arial, sans-serif;
                    font-size: 16px;
                }
                #calendar {
                    max-width: 900px;
                    margin: 40px auto;
                    padding: 20px;
                    background-color: white;
                }
            </style>
        </head>
        <body>
            <div id='calendar'></div>
            <script>
                document.addEventListener('DOMContentLoaded', function() {
                    try {
                        console.log('DOM content loaded');
                        var calendarEl = document.getElementById('calendar');
                        if (!calendarEl) {
                            console.error('Calendar element not found');
                            return;
                        }
                        var calendar = new FullCalendar.Calendar(calendarEl, {
                            initialView: 'dayGridMonth',
                            events: %s,
                            eventClick: function(info) {
                                console.log('Event clicked: ' + info.event.title);
                                window.javaBridge.showEventDetails(
                                    info.event.id,
                                    info.event.title,
                                    info.event.startStr,
                                    info.event.extendedProps.endingDate || 'N/A',
                                    info.event.extendedProps.location || 'N/A',
                                    info.event.extendedProps.latitude || 0,
                                    info.event.extendedProps.longitude || 0,
                                    info.event.extendedProps.image || 'N/A',
                                    info.event.extendedProps.categoryId || 0
                                );
                            }
                        });
                        calendar.render();
                        console.log('Calendar rendered');
                        window.status = 'DOMContentLoaded';
                    } catch (e) {
                        console.error('FullCalendar Error: ' + e.message);
                    }
                });
            </script>
        </body>
        </html>
        """.formatted(eventsJson);
    }
}