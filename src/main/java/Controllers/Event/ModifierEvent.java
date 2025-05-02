package Controllers.Event;

import entities.Event;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import service.EventService;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class ModifierEvent implements Initializable {
    private static final Logger logger = Logger.getLogger(ModifierEvent.class.getName());

    @FXML private TextField latitudeField;
    @FXML private TextField longitudeField;
    @FXML private TextField imageField;
    @FXML private TextField titleField;
    @FXML private DatePicker startingDatePicker;
    @FXML private DatePicker endingDatePicker;
    @FXML private TextField locationField;
    @FXML private WebView mapView;

    private Event eventToEdit;
    private final EventService eventService = new EventService();
    private WebEngine webEngine;
    private boolean isUserClick = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        latitudeField.setEditable(false);
        longitudeField.setEditable(false);
        locationField.setEditable(false);
    }

    public void setEvent(Event event) {
        this.eventToEdit = event;
        if (event != null) {
            logger.info("setEvent: id=" + event.getId() +
                    ", title=" + event.getTitle() +
                    ", lat=" + event.getLatitude() +
                    ", lng=" + event.getLongitude() +
                    ", location=" + event.getLocation());
        } else {
            logger.warning("setEvent: event is null");
        }
        populateFields();
        setupMap();
    }

    private void populateFields() {
        if (eventToEdit != null) {
            titleField.setText(eventToEdit.getTitle() != null ? eventToEdit.getTitle() : "");
            startingDatePicker.setValue(eventToEdit.getStartingDate());
            endingDatePicker.setValue(eventToEdit.getEndingDate());
            locationField.setText(eventToEdit.getLocation() != null ? eventToEdit.getLocation() : "");

            double latitude = eventToEdit.getLatitude();
            double longitude = eventToEdit.getLongitude();
            latitudeField.setText(String.format(java.util.Locale.US, "%.6f", latitude));
            longitudeField.setText(String.format(java.util.Locale.US, "%.6f", longitude));
            imageField.setText(eventToEdit.getImage() != null ? eventToEdit.getImage() : "");

            logger.info("populateFields: latField=" + latitudeField.getText() +
                    ", lngField=" + longitudeField.getText() +
                    ", locationField=" + locationField.getText());
        } else {
            logger.warning("populateFields: eventToEdit is null");
        }
    }

    private void setupMap() {
        webEngine = mapView.getEngine();
        // Update user agent to include application identifier
        webEngine.setUserAgent("Inkspire-BMF/1.0 (JavaFX WebView; Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36)");
        webEngine.setJavaScriptEnabled(true);
        webEngine.setOnError(event -> {
            logger.severe("WebView Error: " + event.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur WebView", "Erreur dans le WebView : " + event.getMessage());
        });

        URL resourceUrl = getClass().getResource("/EventUtils/upmap.html");
        if (resourceUrl == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Fichier mapBack.html introuvable dans les ressources.");
            return;
        }
        String mapUrl = resourceUrl.toExternalForm();
        logger.info("Loading map URL: " + mapUrl);
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
                    if (eventToEdit != null && !Double.isNaN(eventToEdit.getLatitude()) && !Double.isNaN(eventToEdit.getLongitude()) &&
                            eventToEdit.getLatitude() != 0.0 && eventToEdit.getLongitude() != 0.0) {
                        String escapedLocation = eventToEdit.getLocation() != null ?
                                eventToEdit.getLocation().replace("'", "\\'") : "Lieu inconnu";
                        String script = String.format(java.util.Locale.US, "setMarker(%f, %f, '%s');",
                                eventToEdit.getLatitude(), eventToEdit.getLongitude(), escapedLocation);
                        logger.info("Executing initial setMarker script: " + script);

                        new Thread(() -> {
                            try {
                                Thread.sleep(1500);
                                javafx.application.Platform.runLater(() -> {
                                    try {
                                        webEngine.executeScript(script);
                                        logger.info("setMarker script executed successfully");
                                    } catch (Exception e) {
                                        showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'exécution de setMarker : " + e.getMessage());
                                        logger.severe("setMarker execution error: " + e.getMessage());
                                    }
                                });
                            } catch (InterruptedException e) {
                                logger.severe("setMarker thread interrupted: " + e.getMessage());
                            }
                        }).start();
                    } else {
                        logger.warning("Invalid or missing coordinates for eventToEdit: lat=" +
                                (eventToEdit != null ? eventToEdit.getLatitude() : "null") +
                                ", lng=" + (eventToEdit != null ? eventToEdit.getLongitude() : "null"));
                    }
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'initialisation de JavaBridge : " + e.getMessage());
                    logger.severe("JavaBridge initialization error: " + e.getMessage());
                }
            } else if (newVal == javafx.concurrent.Worker.State.FAILED) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec du chargement de la carte : " + webEngine.getLoadWorker().getException());
                logger.severe("Map loading failed: " + webEngine.getLoadWorker().getException());
            }
        });
    }

    public class JavaBridge {
        public void updateCoordinates(double lat, double lng, String address) {
            if (latitudeField == null || longitudeField == null || locationField == null) {
                logger.warning("JavaBridge: Text fields are null");
                return;
            }
            if (!isUserClick) {
                logger.info("JavaBridge: Processing initial coordinates from setMarker: lat=" + lat + ", lng=" + lng + ", address=" + address);
            }

            if (address == null || address.trim().isEmpty()) {
                address = "Lieu inconnu";
                logger.warning("Empty address in JavaBridge, defaulting to: " + address);
            }

            latitudeField.setText(String.format(java.util.Locale.US, "%.6f", lat));
            longitudeField.setText(String.format(java.util.Locale.US, "%.6f", lng));
            locationField.setText(address);

            logger.info("JavaBridge: Updated fields - Latitude=" + latitudeField.getText() +
                    ", Longitude=" + longitudeField.getText() +
                    ", Address=" + address);

            String highlightStyle = "-fx-background-color: lightgreen;";
            latitudeField.setStyle(highlightStyle);
            longitudeField.setStyle(highlightStyle);
            locationField.setStyle(highlightStyle);

            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    javafx.application.Platform.runLater(() -> {
                        latitudeField.setStyle("");
                        longitudeField.setStyle("");
                        locationField.setStyle("");
                    });
                } catch (InterruptedException e) {
                    logger.severe("JavaBridge highlight thread interrupted: " + e.getMessage());
                }
            }).start();
        }

        public void logConsole(String message) {
            logger.info("WebView Console: " + message);
        }
    }

    @FXML
    void saveChanges() {
        if (validateInputs()) {
            try {
                String latText = latitudeField.getText() != null ? latitudeField.getText().trim() : "";
                String lngText = longitudeField.getText() != null ? longitudeField.getText().trim() : "";
                String locationText = locationField.getText() != null ? locationField.getText().trim() : "";
                logger.info("saveChanges: Latitude text=" + latText +
                        ", Longitude text=" + lngText +
                        ", Location=" + locationText);

                eventToEdit.setTitle(titleField.getText());
                eventToEdit.setStartingDate(startingDatePicker.getValue());
                eventToEdit.setEndingDate(endingDatePicker.getValue());
                eventToEdit.setLocation(locationText);
                try {
                    double latitude = Double.parseDouble(latText.replace(',', '.'));
                    double longitude = Double.parseDouble(lngText.replace(',', '.'));
                    eventToEdit.setLatitude(latitude);
                    eventToEdit.setLongitude(longitude);
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.WARNING, "Entrée invalide",
                            "Veuillez sélectionner un lieu valide sur la carte : " + e.getMessage());
                    logger.severe("NumberFormatException in saveChanges: " + e.getMessage());
                    return;
                }
                eventToEdit.setImage(imageField.getText());

                eventService.modifier(eventToEdit);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement modifié avec succès !");
                closeWindow();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification : " + e.getMessage());
                logger.severe("SQLException in saveChanges: " + e.getMessage());
            }
        }
    }

    @FXML
    void cancelEdit() {
        closeWindow();
    }

    private boolean validateInputs() {
        String title = titleField.getText() != null ? titleField.getText().trim() : "";
        String location = locationField.getText() != null ? locationField.getText().trim() : "";
        String latText = latitudeField.getText() != null ? latitudeField.getText().trim() : "";
        String lngText = longitudeField.getText() != null ? longitudeField.getText().trim() : "";

        if (title.isEmpty() || location.isEmpty() ||
                startingDatePicker.getValue() == null ||
                endingDatePicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs obligatoires.");
            logger.warning("Missing required fields: title=" + title + ", location=" + location);
            return false;
        }

        if (title.length() < 3) {
            showAlert(Alert.AlertType.WARNING, "Titre invalide", "Le titre doit contenir au moins 3 caractères.");
            logger.warning("Invalid title length: " + title);
            return false;
        }

        if (!title.matches("[a-zA-Z0-9\\s\\-éèàçêâîïùûü]*")) {
            showAlert(Alert.AlertType.WARNING, "Titre invalide", "Le titre contient des caractères non autorisés.");
            logger.warning("Invalid title characters: " + title);
            return false;
        }

        if (location.length() < 3) {
            showAlert(Alert.AlertType.WARNING, "Emplacement invalide", "L'emplacement doit contenir au moins 3 caractères.");
            logger.warning("Invalid location length: " + location);
            return false;
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate = startingDatePicker.getValue();
        LocalDate endDate = endingDatePicker.getValue();

        if (startDate.isBefore(today)) {
            showAlert(Alert.AlertType.WARNING, "Date invalide", "La date de début ne peut pas être dans le passé.");
            logger.warning("Invalid start date: " + startDate);
            return false;
        }

        if (endDate.isBefore(today)) {
            showAlert(Alert.AlertType.WARNING, "Date invalide", "La date de fin ne peut pas être dans le passé.");
            logger.warning("Invalid end date: " + endDate);
            return false;
        }

        if (startDate.isAfter(endDate)) {
            showAlert(Alert.AlertType.WARNING, "Dates incohérentes", "La date de début ne peut pas être après la date de fin.");
            logger.warning("Inconsistent dates: start=" + startDate + ", end=" + endDate);
            return false;
        }

        if (latText.isEmpty() || lngText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Entrée invalide", "Veuillez sélectionner un lieu valide sur la carte.");
            logger.warning("Empty coordinate fields: lat=" + latText + ", lng=" + lngText);
            return false;
        }

        try {
            Double.parseDouble(latText.replace(',', '.'));
            Double.parseDouble(lngText.replace(',', '.'));
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Entrée invalide", "Les coordonnées doivent être des nombres valides.");
            logger.severe("NumberFormatException in validateInputs: " + e.getMessage());
            return false;
        }

        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }
}