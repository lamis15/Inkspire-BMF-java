package Controllers.Event;

import entities.Category;
import entities.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import service.CategoryService;
import service.EventService;
import utils.SceneSwitch;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class EventDetails implements Initializable {
    private static final Logger logger = Logger.getLogger(EventDetails.class.getName());
    private static final String WEATHER_API_KEY = "60114ea7d04f0e9bb984452f09d7ff7b"; // Replace with your valid OpenWeatherMap API key
    private static final String WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather"; // Updated to 2.5/weather endpoint

    @FXML private Label titleLabel;
    @FXML private Label startingDateLabel;
    @FXML private Label endingDateLabel;
    @FXML private Label locationlabel;
    @FXML private Label statusLabel;
    @FXML private ImageView imageView;
    @FXML private Button backButton;
    @FXML private Button weatherButton;
    @FXML private FlowPane categoryContainer;
    @FXML private WebView mapView;

    private final EventService eventService = new EventService();
    private final CategoryService categoryService = new CategoryService();
    private Event event;
    private WebEngine webEngine;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        categoryContainer.setHgap(15);
        categoryContainer.setVgap(15);
        categoryContainer.setPrefWrapLength(320);
        setupMap();

        Integer eventId = CalendarViewController.getSelectedEventId();
        if (eventId != null) {
            try {
                Event selectedEvent = eventService.getEventById(eventId);
                if (selectedEvent != null) {
                    setEvent(selectedEvent);
                } else {
                    showErrorAlert("Event Not Found", "The selected event could not be found.");
                }
            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to load event details: " + e.getMessage());
            } catch (Exception e) {
                showErrorAlert("Unexpected Error", "An unexpected error occurred: " + e.getMessage());
            }
        }
    }

    private void setupMap() {
        webEngine = mapView.getEngine();
        webEngine.setUserAgent("Inkspire-BMF/1.0 (JavaFX WebView; Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36)");
        webEngine.setJavaScriptEnabled(true);
        webEngine.setOnError(event -> {
            logger.severe("WebView Error: " + event.getMessage());
        });

        URL resourceUrl = getClass().getResource("/EventUtils/map.html");
        if (resourceUrl == null) {
            logger.severe("map.html not found in resources at /EventUtils/map.html");
            return;
        }
        String mapUrl = resourceUrl.toExternalForm();
        webEngine.load(mapUrl);

        webEngine.getLoadWorker().stateProperty().addListener((obs, old, newVal) -> {
            if (newVal == javafx.concurrent.Worker.State.SUCCEEDED) {
                try {
                    String consoleLogScript = """
                            window.console.log = function(message) {
                                if (window.javaObj && typeof window.javaObj.logConsole === 'function') {
                                    window.javaObj.logConsole('LOG: ' + message);
                                }
                            };
                            window.console.error = function(message) {
                                if (window.javaObj && typeof window.javaObj.logConsole === 'function') {
                                    window.javaObj.logConsole('ERROR: ' + message);
                                }
                            };
                            """;
                    webEngine.executeScript(consoleLogScript);

                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("javaObj", new JavaBridge());

                    webEngine.executeScript(
                            "map.dragging.disable(); " +
                                    "map.touchZoom.disable(); " +
                                    "map.doubleClickZoom.disable(); " +
                                    "map.scrollWheelZoom.disable(); " +
                                    "map.boxZoom.disable(); " +
                                    "map.keyboard.disable(); " +
                                    "map.zoomControl.remove(); " +
                                    "map.removeEventListener('click');"
                    );
                } catch (Exception e) {
                    logger.severe("Error initializing map in EventDetails: " + e.getMessage());
                }
            } else if (newVal == javafx.concurrent.Worker.State.FAILED) {
                logger.severe("Map loading failed: " + webEngine.getLoadWorker().getException());
            }
        });
    }

    public class JavaBridge {
        public void logConsole(String message) {
        }
    }

    public void setEvent(Event event) {
        this.event = event;

        titleLabel.setText(event.getTitle() != null ? event.getTitle() : "Untitled");
        locationlabel.setText(event.getLocation() != null ? event.getLocation() : "Unknown");
        startingDateLabel.setText(event.getStartingDate() != null ? event.getStartingDate().toString() : "Unknown");
        endingDateLabel.setText(event.getEndingDate() != null ? event.getEndingDate().toString() : "Unknown");
        statusLabel.setText(event.getStatus() != null ? event.getStatus() : "Unknown");

        if (imageView == null) {
            logger.severe("imageView is null in EventDetails");
            return;
        }
        String defaultImagePath = "/images/default-event.jpg";
        try {
            String imagePath = event.getImage();

            Image image;
            if (imagePath != null && !imagePath.isEmpty()) {
                if (!imagePath.startsWith("file:") && !imagePath.startsWith("http")) {
                    String resourcePath = "/images/events/" + imagePath;
                    URL imageUrl = this.getClass().getResource(resourcePath);
                    if (imageUrl != null) {
                        imagePath = imageUrl.toExternalForm();
                    } else {
                        imagePath = "file:" + System.getProperty("user.dir") + "/src/main/resources/images/events/" + imagePath;
                    }
                }
                image = new Image(imagePath, true);
            } else {
                logger.info("No image provided for event: " + event.getTitle());
                image = null;
            }

            if (image != null && !image.isError()) {
                imageView.setImage(image);
            } else {
                URL defaultImageUrl = this.getClass().getResource(defaultImagePath);
                if (defaultImageUrl != null) {
                    imageView.setImage(new Image(defaultImageUrl.toExternalForm()));
                } else {
                    logger.severe("Default image not found: " + defaultImagePath);
                    imageView.setImage(new Image("file:src/main/resources/images/default-event.jpg"));
                }
            }
        } catch (Exception e) {
            logger.severe("Error loading image: " + e.getMessage());
            URL defaultImageUrl = this.getClass().getResource(defaultImagePath);
            if (defaultImageUrl != null) {
                imageView.setImage(new Image(defaultImageUrl.toExternalForm()));
            } else {
                imageView.setImage(new Image("file:src/main/resources/images/default-event.jpg"));
            }
        }

        try {
            categoryContainer.getChildren().clear();
            List<Category> categories = categoryService.afficher();
            int categoryId = event.getCategoryId();
            categories.stream()
                    .filter(cat -> cat.getId() == categoryId)
                    .findFirst()
                    .ifPresent(cat -> {
                        Label categoryLabel = new Label(cat.getName());
                        categoryLabel.setStyle("-fx-font-size: 12px; -fx-font-family: 'Segoe UI'; -fx-text-fill: #5B87FF; -fx-background-color: #e8f0fe; -fx-padding: 5; -fx-background-radius: 5;");
                        categoryContainer.getChildren().add(categoryLabel);
                        logger.info("Category added: " + cat.getName());
                    });
        } catch (SQLException e) {
            logger.severe("Error loading categories: " + e.getMessage());
            Label errorLabel = new Label("Erreur de chargement des catégories");
            errorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ff4444;");
            categoryContainer.getChildren().add(errorLabel);
        }

        if (webEngine != null) {
            double lat = event.getLatitude();
            double lng = event.getLongitude();
            if (!Double.isNaN(lat) && !Double.isNaN(lng) && lat != 0.0 && lng != 0.0) {
                String escapedLocation = event.getLocation() != null ?
                        event.getLocation().replace("'", "\\'") : "Lieu inconnu";
                String script = String.format(java.util.Locale.US, "setMarker(%f, %f, '%s');", lat, lng, escapedLocation);

                new Thread(() -> {
                    try {
                        Thread.sleep(1500);
                        javafx.application.Platform.runLater(() -> {
                            try {
                                webEngine.executeScript(script);
                            } catch (Exception e) {
                                logger.severe("Error setting map marker: " + e.getMessage());
                            }
                        });
                    } catch (InterruptedException e) {
                        logger.severe("setMarker thread interrupted: " + e.getMessage());
                    }
                }).start();
            } else {
                logger.warning("Invalid or missing coordinates: lat=" + lat + ", lng=" + lng);
            }
        } else {
            logger.severe("WebEngine not initialized for map");
        }
    }

    @FXML
    private void onWeatherClick() {
        if (event == null) {
            showErrorAlert("No Event", "No event data available to fetch weather.");
            return;
        }

        double lat = event.getLatitude();
        double lng = event.getLongitude();

        if (Double.isNaN(lat) || Double.isNaN(lng) || lat == 0.0 || lng == 0.0) {
            showErrorAlert("Invalid Location", "Event location coordinates are invalid.");
            return;
        }

        try {
            String url = String.format("%s?lat=%f&lon=%f&appid=%s&units=metric",
                    WEATHER_API_URL, lat, lng, WEATHER_API_KEY);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URL(url).toURI())
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                logger.severe("Weather API request failed with status: " + response.statusCode() + ", body: " + response.body());
                showErrorAlert("Weather API Error", "Failed to fetch weather data: HTTP " + response.statusCode());
                return;
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode weather = root.get("weather").get(0);
            String description = weather.get("description").asText();
            double temp = root.get("main").get("temp").asDouble();
            int humidity = root.get("main").get("humidity").asInt();

            String weatherInfo = String.format(
                    "Current Weather:\n" +
                            "Description: %s\n" +
                            "Temperature: %.1f°C\n" +
                            "Humidity: %d%%",
                    description, temp, humidity);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Weather Forecast");
            alert.setHeaderText("Weather for " + event.getLocation());
            alert.setContentText(weatherInfo);
            alert.showAndWait();
        } catch (Exception e) {
            logger.severe("Error fetching weather: " + e.getMessage());
            showErrorAlert("Weather Error", "Failed to fetch weather data: " + e.getMessage());
        }
    }

    @FXML
    private void onBackClick() {
        Node node = backButton.getScene().getRoot().lookup("#mainRouter");
        if (node instanceof Pane) {
            SceneSwitch.switchScene((Pane) node, "/EventUtils/AfficherEvent.fxml");
        } else {
            logger.severe("Could not find mainRouter for navigation");
            showErrorAlert("Navigation Error", "Could not find mainRouter for navigation.");
        }
    }

    private void showErrorAlert(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}