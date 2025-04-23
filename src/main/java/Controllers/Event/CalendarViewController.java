package Controllers.Event;

import entities.Event;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.web.WebView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import netscape.javascript.JSObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CalendarViewController {

    @FXML
    private WebView calendarWebView;

    private List<Event> eventList;

    public void setEventList(List<Event> eventList) {
        this.eventList = eventList;
        initializeCalendar();
    }

    private void initializeCalendar() {
        try {
            // Validate eventList
            if (eventList == null || eventList.isEmpty()) {
                throw new IllegalStateException("Event list is null or empty");
            }

            // Create ObjectMapper and register JavaTimeModule
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Serialize dates as ISO strings

            // Transform eventList to FullCalendar-compatible JSON (only start date)
            List<Map<String, Object>> fullCalendarEvents = eventList.stream().map(event -> {
                if (event.getStartingDate() == null) {
                    throw new IllegalArgumentException("Event '" + event.getTitle() + "' has null starting_date");
                }
                Map<String, Object> map = new HashMap<>();
                map.put("id", event.getId());
                map.put("title", event.getTitle() != null ? event.getTitle() : "Untitled");
                map.put("start", event.getStartingDate().toString());
                map.put("location", event.getLocation() != null ? event.getLocation() : "N/A");
                map.put("backgroundColor", "#ff0000"); // Red background for events
                // Include additional fields for display in dialog
                map.put("endingDate", event.getEndingDate() != null ? event.getEndingDate().toString() : "N/A");
                map.put("latitude", event.getLatitude());
                map.put("longitude", event.getLongitude());
                map.put("image", event.getImage() != null ? event.getImage() : "N/A");
                map.put("categoryId", event.getCategoryId());
                return map;
            }).collect(Collectors.toList());

            // Convert to JSON
            String eventsJson = mapper.writeValueAsString(fullCalendarEvents);

            // Debug: Print JSON to verify format
            System.out.println("Events JSON: " + eventsJson);

            // Load the HTML content with FullCalendar
            String htmlContent = generateHtmlContent(eventsJson);

            // Log WebView errors
            calendarWebView.getEngine().setOnError(event -> {
                System.err.println("WebView JavaScript Error: " + event.getMessage());
                showErrorAlert("WebView Error", "JavaScript error in calendar: " + event.getMessage());
            });

            // Set up JavaScript bridge
            calendarWebView.getEngine().loadContent(htmlContent);
            calendarWebView.getEngine().setOnStatusChanged(event -> {
                if (event.getData().equals("DOMContentLoaded")) {
                    JSObject window = (JSObject) calendarWebView.getEngine().executeScript("window");
                    window.setMember("javaBridge", new JavaBridge());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Failed to Load Calendar", "An error occurred while loading the calendar: " + e.getMessage());
        }
    }

    // JavaScript bridge to handle event clicks
    public class JavaBridge {
        public void showEventDetails(int eventId, String title, String start, String endingDate, String location,
                                     double latitude, double longitude, String image, int categoryId) {
            Platform.runLater(() -> {
                String details = String.format(
                        "ID: %d\nTitle: %s\nStart Date: %s\nEnd Date: %s\nLocation: %s\nLatitude: %.6f\nLongitude: %.6f\nImage: %s\nCategory ID: %d",
                        eventId, title, start, endingDate, location, latitude, longitude, image, categoryId
                );
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Event Details");
                alert.setHeaderText("Event: " + title);
                alert.setContentText(details);
                alert.showAndWait();
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
            <!-- FontAwesome for event icon -->
            <link href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css' rel='stylesheet' />
            <style>
                html, body {
                    margin: 0;
                    padding: 0;
                    font-family: Arial, Helvetica Neue, Helvetica, sans-serif;
                    font-size: 16px;
                }
                #calendar {
                    max-width: 900px;
                    margin: 40px auto;
                    padding: 20px;
                }
                .fc-event {
                    padding: 5px 10px;
                    font-size: 14px;
                    border-radius: 4px;
                    cursor: pointer;
                    transition: background-color 0.3s;
                }
                .fc-event:hover {
                    opacity: 0.8;
                }
                .fc-event .fc-event-title::before {
                    content: "\\f073"; /* FontAwesome calendar icon */
                    font-family: "Font Awesome 6 Free";
                    font-weight: 900;
                    margin-right: 8px;
                    color: #ffffff;
                }
            </style>
        </head>
        <body>
            <div id='calendar'></div>
            <script>
                document.addEventListener('DOMContentLoaded', function() {
                    try {
                        var calendarEl = document.getElementById('calendar');
                        var calendar = new FullCalendar.Calendar(calendarEl, {
                            initialView: 'dayGridMonth',
                            events: %s,
                            eventContent: function(arg) {
                                let titleEl = document.createElement('span');
                                titleEl.innerHTML = '<i class="fas fa-calendar"></i> ' + arg.event.title;
                                return { domNodes: [titleEl] };
                            },
                            eventClick: function(info) {
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