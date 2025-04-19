package Controllers.Event;

import entities.Category;
import entities.Event;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import service.CategoryService;
import service.EventService;
import utils.SceneSwitch;

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

    private File selectedImageFile;
    private final EventService eventService = new EventService();
    private final CategoryService categoryService = new CategoryService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadCategories();
        setupDateValidation();
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
            showAlert("Erreur", "Impossible de charger les catégories : " + e.getMessage());
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

                if (!latitudeField.getText().isEmpty()) {
                    latitude = Double.parseDouble(latitudeField.getText());
                }

                if (!longitudeField.getText().isEmpty()) {
                    longitude = Double.parseDouble(longitudeField.getText());
                }

                Category selectedCategory = categoryComboBox.getValue();
                if (selectedCategory == null) {
                    showAlert("Erreur", "Veuillez sélectionner une catégorie !");
                    return;
                }

                Event newEvent = new Event(
                        titleField.getText(),
                        startDatePicker.getValue(),
                        endDatePicker.getValue(),
                        locationField.getText(),
                        latitude,
                        longitude,
                        selectedImageFile != null ?
                                "file:" + selectedImageFile.getAbsolutePath().replace("\\", "/") :
                                "file:/default.png"
                );

                newEvent.setCategoryId(selectedCategory.getId());

                if (eventService.ajouter(newEvent)) {
                    showAlert("Succès", "Événement ajouté avec succès !");
                    switchSceneToAfficherEventBack();
                } else {
                    showAlert("Erreur", "Échec de l'ajout de l'événement");
                }
            } catch (NumberFormatException e) {
                showAlert("Erreur", "Latitude ou longitude invalide !");
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur lors de l'ajout : " + e.getMessage());
                e.printStackTrace();
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

            showAlert("Champs manquants", "Veuillez remplir tous les champs obligatoires.");
            return false;
        }

        if (title.length() < 3) {
            showAlert("Titre invalide", "Le titre doit contenir au moins 3 caractères.");
            return false;
        }

        if (!title.matches("[a-zA-Z0-9\\s\\-éèàçêâîïùûü]*")) {
            showAlert("Titre invalide", "Le titre contient des caractères non autorisés.");
            return false;
        }

        if (location.length() < 3) {
            showAlert("Emplacement invalide", "L'emplacement doit contenir au moins 3 caractères.");
            return false;
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate.isBefore(today)) {
            showAlert("Date invalide", "La date de début ne peut pas être dans le passé.");
            return false;
        }

        if (endDate.isBefore(today)) {
            showAlert("Date invalide", "La date de fin ne peut pas être dans le passé.");
            return false;
        }

        if (startDate.isAfter(endDate)) {
            showAlert("Dates incohérentes", "La date de début ne peut pas être après la date de fin.");
            return false;
        }

        if (!latitudeField.getText().isEmpty()) {
            try {
                double lat = Double.parseDouble(latitudeField.getText());
                if (lat < -90 || lat > 90) {
                    showAlert("Latitude invalide", "La latitude doit être entre -90 et 90.");
                    return false;
                }
            } catch (NumberFormatException e) {
                showAlert("Latitude invalide", "Veuillez entrer un nombre valide pour la latitude.");
                return false;
            }
        }

        if (!longitudeField.getText().isEmpty()) {
            try {
                double lon = Double.parseDouble(longitudeField.getText());
                if (lon < -180 || lon > 180) {
                    showAlert("Longitude invalide", "La longitude doit être entre -180 et 180.");
                    return false;
                }
            } catch (NumberFormatException e) {
                showAlert("Longitude invalide", "Veuillez entrer un nombre valide pour la longitude.");
                return false;
            }
        }

        return true;
    }

    private void switchSceneToAfficherEventBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventUtils/AfficherEventBack.fxml"));
            Pane newPane = loader.load();
            Stage currentStage = (Stage) rootVBox.getScene().getWindow();
            Scene newScene = new Scene(newPane);
            currentStage.setScene(newScene);
            currentStage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Erreur lors de la redirection : " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void onBackClick(ActionEvent event) {
        Stage stage = (Stage) rootVBox.getScene().getWindow();
        stage.close();
    }

    @FXML
    void ajouter(ActionEvent actionEvent) {
        ajouterEvent(actionEvent);
    }

    public void annuler(ActionEvent actionEvent) {
        onBackClick(actionEvent);
    }
}