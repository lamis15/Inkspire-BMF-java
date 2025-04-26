package Controllers.Category;

import entities.Category;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.CategoryService;
import utils.SceneSwitch;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AjoutCategory implements Initializable {
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> statutComboBox;
    @FXML
    private VBox rootVBox;
    private final CategoryService categoryService = new CategoryService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialiser le ComboBox avec les statuts possibles
        statutComboBox.getItems().addAll("Actif", "Inactif");
        statutComboBox.setValue("Actif");
    }

    @FXML
    private void handleAdd() {
        if (validateInputs()) {
            Category category = new Category(
                    nameField.getText(),
                    descriptionField.getText(),
                    statutComboBox.getValue()
            );

            try {
                categoryService.ajouter(category);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie ajoutée avec succès");
                closeWindow();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateInputs() {
        String name = nameField.getText().trim();
        String description = descriptionField.getText().trim();
        String statut = statutComboBox.getValue();

        // Vérification champs vides
        if (name.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le nom est obligatoire.");
            return false;
        }

        if (description.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La description est obligatoire.");
            return false;
        }

        // Longueur minimale
        if (name.length() < 3) {
            showAlert(Alert.AlertType.WARNING, "Nom invalide", "Le nom doit contenir au moins 3 caractères.");
            return false;
        }

        if (description.length() < 10) {
            showAlert(Alert.AlertType.WARNING, "Description invalide", "La description doit contenir au moins 10 caractères.");
            return false;
        }

        // Caractères autorisés dans le nom
        if (!name.matches("[a-zA-Z0-9\\s\\-éèàçêâîïùûü]*")) {
            showAlert(Alert.AlertType.WARNING, "Nom invalide", "Le nom contient des caractères non autorisés.");
            return false;
        }

        // Vérification du statut
        if (statut == null || (!statut.equals("Actif") && !statut.equals("Inactif"))) {
            showAlert(Alert.AlertType.ERROR, "Statut invalide", "Veuillez sélectionner un statut valide.");
            return false;
        }

        return true;
    }


    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML
    void ajouterCategory(ActionEvent event) {
        if (validateInputs()) {
            try {
                String nom = nameField.getText();
                String description = descriptionField.getText();
                String statut = statutComboBox.getValue();

                Category nouvelleCategorie = new Category(nom, description, statut);

                categoryService.ajouter(nouvelleCategorie);

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie ajoutée avec succès !");
                closeWindow();

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout : " + e.getMessage());
            }
        }
    }
    @FXML
    void onBackClick(ActionEvent event) {
        // Si tu veux revenir, tu peux fermer la fenêtre ou recharger l'affichage précédent
        Stage stage = (Stage) rootVBox.getScene().getWindow();
        stage.close();
    }
    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
    @FXML
    void ajouter(ActionEvent actionEvent) {
        ajouterCategory(actionEvent);
    }

    public void annuler(ActionEvent actionEvent) {
        onBackClick(actionEvent);
    }


}