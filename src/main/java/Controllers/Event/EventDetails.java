package Controllers.Event;

import entities.Category;
import entities.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class EventDetails implements Initializable {
    private static final Logger logger = Logger.getLogger(EventDetails.class.getName());

    @FXML private Label titleLabel;
    @FXML private Label startingDateLabel;
    @FXML private Label endingDateLabel;
    @FXML private Label locationlabel;
    @FXML private Label statusLabel;
    @FXML private ImageView imageView;
    @FXML private Button backButton;
    @FXML private FlowPane categoryContainer;
    @FXML private WebView mapView;

    private final EventService eventService = new EventService();
    private final CategoryService categoryService = new CategoryService();
    private Event event;
    private WebEngine webEngine;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        categoryContainer.setHgap(15);
        categoryContainer.setVgap(15);
        categoryContainer.setPrefWrapLength(320);
        logger.info("imageView initialized: " + (imageView != null));
        setupMap();
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
        logger.info("Loading map URL: " + mapUrl);
        webEngine.load(mapUrl);

        webEngine.getLoadWorker().stateProperty().addListener((obs, old, newVal) -> {
            if (newVal == javafx.concurrent.Worker.State.SUCCEEDED) {
                try {
                    // Inject JavaScript console log handler
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

                    // Disable all map interactions
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
            logger.info("WebView Console: " + message);
        }
    }

    public void setEvent(Event event) {
        this.event = event;

        // Set basic event details (read-only)
        titleLabel.setText(event.getTitle() != null ? event.getTitle() : "Untitled");
        locationlabel.setText(event.getLocation() != null ? event.getLocation() : "Unknown");
        startingDateLabel.setText(event.getStartingDate() != null ? event.getStartingDate().toString() : "Unknown");
        endingDateLabel.setText(event.getEndingDate() != null ? event.getEndingDate().toString() : "Unknown");
        statusLabel.setText(event.getStatus());

        // Log event details for debugging
        logger.info("Event details: id=" + event.getId() + ", title=" + event.getTitle() +
                ", location=" + event.getLocation() + ", lat=" + event.getLatitude() +
                ", lng=" + event.getLongitude());

        // Load image
        if (imageView == null) {
            logger.severe("imageView is null in EventDetails");
            return;
        }
        String defaultImagePath = "/images/default-event.jpg";
        try {
            String imagePath = event.getImage();
            logger.info("Raw image path from event: " + imagePath);
            if (imagePath != null && !imagePath.isEmpty()) {
                // Handle paths from AjouterEvent
                if (!imagePath.startsWith("file:") && !imagePath.startsWith("http")) {
                    String resourcePath = "/images/events/" + imagePath;
                    URL imageUrl = getClass().getResource(resourcePath);
                    if (imageUrl != null) {
                        imagePath = imageUrl.toExternalForm();
                    } else {
                        imagePath = "file:" + System.getProperty("user.dir") + "/src/main/resources/images/events/" + imagePath;
                    }
                }
                logger.info("Processed image path: " + imagePath);
                Image image = new Image(imagePath, true);
                if (image.isError()) {
                    logger.severe("Failed to load image: " + imagePath + ", error: " + image.getException().getMessage());
                    imageView.setImage(new Image(getClass().getResource(defaultImagePath) != null
                            ? getClass().getResource(defaultImagePath).toExternalForm()
                            : "file:src/main/resources/images/default-event.jpg"));
                } else {
                    imageView.setImage(image);
                }
            } else {
                logger.info("No image provided for event: " + event.getTitle());
                imageView.setImage(new Image(getClass().getResource(defaultImagePath) != null
                        ? getClass().getResource(defaultImagePath).toExternalForm()
                        : "file:src/main/resources/images/default-event.jpg"));
            }
        } catch (Exception e) {
            logger.severe("Error loading image: " + e.getMessage());
            imageView.setImage(new Image(getClass().getResource(defaultImagePath) != null
                    ? getClass().getResource(defaultImagePath).toExternalForm()
                    : "file:src/main/resources/images/default-event.jpg"));
        }

        // Populate categories (read-only)
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
                    });
        } catch (SQLException e) {
            logger.severe("Error loading categories: " + e.getMessage());
            Label errorLabel = new Label("Erreur de chargement des catégories");
            errorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ff4444;");
            categoryContainer.getChildren().add(errorLabel);
        }

        // Initialize map with event coordinates (read-only)
        if (webEngine != null) {
            double lat = event.getLatitude();
            double lng = event.getLongitude();
            if (!Double.isNaN(lat) && !Double.isNaN(lng) && lat != 0.0 && lng != 0.0) {
                String escapedLocation = event.getLocation() != null ?
                        event.getLocation().replace("'", "\\'") : "Lieu inconnu";
                String script = String.format(java.util.Locale.US, "setMarker(%f, %f, '%s');", lat, lng, escapedLocation);
                logger.info("Executing setMarker script in EventDetails: " + script);

                new Thread(() -> {
                    try {
                        Thread.sleep(1500); // Delay to ensure map is fully loaded
                        javafx.application.Platform.runLater(() -> {
                            try {
                                webEngine.executeScript(script);
                                logger.info("setMarker script executed successfully in EventDetails");
                            } catch (Exception e) {
                                logger.severe("Error setting map marker in EventDetails: " + e.getMessage());
                            }
                        });
                    } catch (InterruptedException e) {
                        logger.severe("setMarker thread interrupted in EventDetails: " + e.getMessage());
                    }
                }).start();
            } else {
                logger.warning("Invalid or missing coordinates in EventDetails: lat=" + lat + ", lng=" + lng);
            }
        } else {
            logger.severe("WebEngine not initialized for map in EventDetails");
        }
    }

    @FXML
    private void onBackClick() {
        Node node = backButton.getScene().getRoot().lookup("#mainRouter");
        if (node instanceof Pane) {
            SceneSwitch.switchScene((Pane) node, "/EventUtils/AfficherEvent.fxml");
        } else {
            logger.severe("Could not find mainRouter for navigation");
        }
    }
}