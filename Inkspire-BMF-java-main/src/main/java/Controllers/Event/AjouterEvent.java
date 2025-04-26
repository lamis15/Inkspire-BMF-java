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
    @FXML private VBox rootVBox;               // doit matcher le fx:id rootVBox
    @FXML private TextField titleField;
    @FXML private DatePicker startDatePicker;  // startDatePicker, pas startingDatePicker
    @FXML private DatePicker endDatePicker;    // endDatePicker, pas endingDatePicker
    @FXML private TextField locationField;
    @FXML private TextField latitudeField;
    @FXML private TextField longitudeField;
    @FXML private Label imagePathLabel;        // Label pour afficher le nom de l'image
    @FXML private Button backButton;
    @FXML private ComboBox<Category> categoryComboBox;

    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
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
            // Ajouter le préfixe "file:/"
            String filePath = "file:" + file.getAbsolutePath().replace("\\", "/"); // Remplacer les antislashs par des slashes
            imagePathLabel.setText(filePath); // Afficher le chemin avec file:/ pour l'utilisateur
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }
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
/*
    @FXML
    void chooseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedImageFile = file;
            imagePathLabel.setText(file.getName());
        }
    }*/

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
                    try {
                        latitude = Double.parseDouble(latitudeField.getText());
                    } catch (NumberFormatException e) {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Latitude invalide !");
                        return;
                    }
                }

                if (!longitudeField.getText().isEmpty()) {
                    try {
                        longitude = Double.parseDouble(longitudeField.getText());
                    } catch (NumberFormatException e) {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Longitude invalide !");
                        return;
                    }
                }

                Category selectedCategory = categoryComboBox.getValue();
                if (selectedCategory == null) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner une catégorie !");
                    return;
                }

                String categoryId = String.valueOf(selectedCategory.getId());

                // Créer l'objet Event
                Event newEvent = new Event(
                        titleField.getText(),
                        startDatePicker.getValue(),
                        endDatePicker.getValue(),
                        locationField.getText(),
                        latitude,
                        longitude,
                        categoryId
                );

                // Définir l'image avec le préfixe file:/ si une image est sélectionnée
                if (selectedImageFile != null) {
                    String imagePath = "file:" + selectedImageFile.getAbsolutePath().replace("\\", "/");
                    newEvent.setImage(imagePath); // Utiliser le chemin formaté avec file:/
                } else {
                    newEvent.setImage("file:/default.png"); // Image par défaut
                }

                // Ajouter l'événement
                eventService.ajouter(newEvent);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement ajouté avec succès !");

                // Redirection après ajout
                switchSceneToAfficherEventBack(); // Redirection vers AfficherEventBack.fxml

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout : " + e.getMessage());
            }
        }
    }

    private void switchSceneToAfficherEventBack() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherEventBack.fxml"));
            Pane newPane = loader.load();

            // Accéder à la scène actuelle et la mettre à jour
            Stage currentStage = (Stage) rootVBox.getScene().getWindow();
            Scene newScene = new Scene(newPane);
            currentStage.setScene(newScene);  // Appliquer la nouvelle scène
            currentStage.show(); // Afficher la nouvelle scène

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la redirection vers la page des événements : " + e.getMessage());
        }
    }

    @FXML
    private void switchScene(Pane rootVBox, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Pane newPane = loader.load();
            Stage currentStage = (Stage) rootVBox.getScene().getWindow();
            Scene newScene = new Scene(newPane);
            currentStage.setScene(newScene);  // Appliquer la nouvelle scène
            currentStage.show(); // Afficher la nouvelle scène
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la redirection vers la page précédente : " + e.getMessage());
        }
    }



    private boolean validateInputs() {
        String title = titleField.getText().trim();
        String location = locationField.getText().trim();

        // Champs obligatoires
        if (title.isEmpty() || location.isEmpty() ||
                startDatePicker.getValue() == null ||
                endDatePicker.getValue() == null ||
                categoryComboBox.getValue() == null) {

            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs obligatoires.");
            return false;
        }

        // Longueur minimale pour le titre
        if (title.length() < 3) {
            showAlert(Alert.AlertType.WARNING, "Titre invalide", "Le titre doit contenir au moins 3 caractères.");
            return false;
        }

        // Caractères autorisés dans le titre
        if (!title.matches("[a-zA-Z0-9\\s\\-éèàçêâîïùûü]*")) {
            showAlert(Alert.AlertType.WARNING, "Titre invalide", "Le titre contient des caractères non autorisés.");
            return false;
        }

        // Longueur minimale pour l'emplacement
        if (location.length() < 3) {
            showAlert(Alert.AlertType.WARNING, "Emplacement invalide", "L'emplacement doit contenir au moins 3 caractères.");
            return false;
        }

        // Vérification des dates
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

        // Vérification latitude
        if (!latitudeField.getText().isEmpty()) {
            try {
                double lat = Double.parseDouble(latitudeField.getText());
                if (lat < -90 || lat > 90) {
                    showAlert(Alert.AlertType.ERROR, "Latitude invalide", "La latitude doit être entre -90 et 90.");
                    return false;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Latitude invalide", "Veuillez entrer un nombre valide pour la latitude.");
                return false;
            }
        }

        // Vérification longitude
        if (!longitudeField.getText().isEmpty()) {
            try {
                double lon = Double.parseDouble(longitudeField.getText());
                if (lon < -180 || lon > 180) {
                    showAlert(Alert.AlertType.ERROR, "Longitude invalide", "La longitude doit être entre -180 et 180.");
                    return false;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Longitude invalide", "Veuillez entrer un nombre valide pour la longitude.");
                return false;
            }
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
        // Si tu veux revenir, tu peux fermer la fenêtre ou recharger l'affichage précédent
        Stage stage = (Stage) rootVBox.getScene().getWindow();
        stage.close();
        closeWindow();
    }

    @FXML
    void ajouter(ActionEvent actionEvent) {
        ajouterEvent(actionEvent);
    }

    public void annuler(ActionEvent actionEvent) {
        onBackClick(actionEvent);
    }
}
