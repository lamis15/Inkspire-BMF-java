package Controllers.Event;

import entities.Category;
import entities.Event;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import service.CategoryService;
import service.EventService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AjouterEvent implements Initializable {
    @FXML private VBox rootVBox;
    @FXML private TextField titleField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField locationField;
    @FXML private TextField latitudeField;
    @FXML private TextField longitudeField;
    @FXML private Label imagePathLabel;
    @FXML private Button backButton;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private WebView mapView;

    private File selectedImageFile;
    private final EventService eventService = new EventService();
    private final CategoryService categoryService = new CategoryService();
    private WebEngine webEngine;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadCategories();
        setupDateValidation();
        setupMap();

        // Rendre les champs latitude, longitude, et location en lecture seule
        latitudeField.setEditable(false);
        longitudeField.setEditable(false);
        locationField.setEditable(false);
    }

    private void setupMap() {
        webEngine = mapView.getEngine();

        // Configurer un user-agent moderne
        webEngine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

        // Activer JavaScript
        webEngine.setJavaScriptEnabled(true);

        // Ajouter un écouteur d'erreurs général
        webEngine.setOnError(event -> showAlert(Alert.AlertType.ERROR, "Erreur WebView", "Erreur dans le WebView : " + event.getMessage()));

        URL resourceUrl = getClass().getResource("/EventUtils/map.html");
        if (resourceUrl == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Fichier map.html introuvable dans les ressources.");
            return;
        }
        String mapUrl = resourceUrl.toExternalForm();
        webEngine.load(mapUrl);

        webEngine.getLoadWorker().stateProperty().addListener((obs, old, newVal) -> {
            if (newVal == javafx.concurrent.Worker.State.SUCCEEDED) {
                try {
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("javaObj", new JavaBridge());
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'initialisation de JavaBridge : " + e.getMessage());
                }
            } else if (newVal == javafx.concurrent.Worker.State.FAILED) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec du chargement de la carte : " + webEngine.getLoadWorker().getException());
            }
        });
    }

    public class JavaBridge {
        public void updateCoordinates(double lat, double lng, String city) {
            // Vérifier que les champs ne sont pas null
            if (latitudeField == null || longitudeField == null || locationField == null) {
                return;
            }

            // Vérifier les valeurs avant de les assigner
            if (Double.isNaN(lat) || Double.isInfinite(lat)) {
                lat = 0.0;
            }
            if (Double.isNaN(lng) || Double.isInfinite(lng)) {
                lng = 0.0;
            }
            if (city == null || city.trim().isEmpty()) {
                city = "Tunis";
            }

            // Mettre à jour les champs
            latitudeField.setText(String.format("%.6f", lat));
            longitudeField.setText(String.format("%.6f", lng));
            locationField.setText(city);

            // Ajouter une indication visuelle (par exemple, fond vert pendant 1 seconde)
            String highlightStyle = "-fx-background-color: lightgreen;";
            latitudeField.setStyle(highlightStyle);
            longitudeField.setStyle(highlightStyle);
            locationField.setStyle(highlightStyle);

            // Revenir à la couleur par défaut après 1 seconde
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    javafx.application.Platform.runLater(() -> {
                        latitudeField.setStyle("");
                        longitudeField.setStyle("");
                        locationField.setStyle("");
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void loadCategories() {
        try {
            categoryComboBox.getItems().clear();
            categoryComboBox.getItems().addAll(categoryService.afficher());

            categoryComboBox.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Category category, boolean empty) {
                    super.updateItem(category, empty);
                    setText(empty || category == null ? null : category.getName());
                }
            });

            categoryComboBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Category category, boolean empty) {
                    super.updateItem(category, empty);
                    setText(empty || category == null ? null : category.getName());
                }
            });
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les catégories : " + e.getMessage());
        }
    }

    private void setupDateValidation() {
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && startDatePicker.getValue() != null && newVal.isBefore(startDatePicker.getValue())) {
                endDatePicker.setValue(startDatePicker.getValue());
            }
        });
    }

    @FXML
    void chooseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedImageFile = file;
            String filePath = "file:" + file.getAbsolutePath().replace("\\", "/");
            imagePathLabel.setText(filePath);
        }
    }

    @FXML
    void removeImage(ActionEvent event) {
        selectedImageFile = null;
        imagePathLabel.setText("Aucune image choisie");
    }

    @FXML
    void ajouterEvent(ActionEvent event) {
        if (validateInputs()) {
            try {
                double latitude = 0.0;
                double longitude = 0.0;

                Category selectedCategory = categoryComboBox.getValue();
                if (selectedCategory == null) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner une catégorie !");
                    return;
                }

                Event newEvent = new Event(
                        titleField.getText(),
                        startDatePicker.getValue(),
                        endDatePicker.getValue(),
                        locationField.getText(),
                        latitude,
                        longitude,
                        selectedImageFile != null ? "file:" + selectedImageFile.getAbsolutePath().replace("\\", "/") : "file:/default.png"
                );
                newEvent.setCategoryId(selectedCategory.getId());

                eventService.ajouter(newEvent);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement ajouté avec succès !");

                // Redirect to AfficherEvent
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventUtils/AfficherEventBack.fxml"));
                if (loader.getLocation() == null) {
                    throw new IOException("Cannot find AfficherEventBack.fxml");
                }
                Parent newPane = loader.load();
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene newScene = new Scene(newPane);
                stage.setScene(newScene);
                stage.setTitle("Event List");
                stage.show();

            } catch (SQLException | IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout ou redirection : " + e.getMessage());
            }
        }
    }

    private boolean validateInputs() {
        String title = titleField.getText().trim();
        String location = locationField.getText().trim();

        if (title.isEmpty() || location.isEmpty() ||
                startDatePicker.getValue() == null ||
                endDatePicker.getValue() == null ||
                categoryComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs obligatoires.");
            return false;
        }

        if (title.length() < 3) {
            showAlert(Alert.AlertType.WARNING, "Titre invalide", "Le titre doit contenir au moins 3 caractères.");
            return false;
        }

        if (!title.matches("[a-zA-Z0-9\\s\\-éèàçêâîïùûü]*")) {
            showAlert(Alert.AlertType.WARNING, "Titre invalide", "Le titre contient des caractères non autorisés.");
            return false;
        }

        if (location.length() < 3) {
            showAlert(Alert.AlertType.WARNING, "Emplacement invalide", "L'emplacement doit contenir au moins 3 caractères.");
            return false;
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate.isBefore(today)) {
            showAlert(Alert.AlertType.WARNING, "Date invalide", "La date de début ne peut pas être dans le passé.");
            return false;
        }

        if (endDate.isBefore(today)) {
            showAlert(Alert.AlertType.WARNING, "Date invalide", "La date de fin ne peut pas être dans le passé.");
            return false;
        }

        if (startDate.isAfter(endDate)) {
            showAlert(Alert.AlertType.WARNING, "Dates incohérentes", "La date de début ne peut pas être après la date de fin.");
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

    @FXML
    void onBackClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventUtils/AfficherEvent.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("Cannot find AfficherEvent.fxml");
            }
            Parent newPane = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene newScene = new Scene(newPane);
            stage.setScene(newScene);
            stage.setTitle("Event List");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la redirection : " + e.getMessage());
        }
    }

    @FXML
    void ajouter(ActionEvent actionEvent) {
        ajouterEvent(actionEvent);
    }

    @FXML
    void annuler(ActionEvent actionEvent) {
        onBackClick(actionEvent);
    }
}