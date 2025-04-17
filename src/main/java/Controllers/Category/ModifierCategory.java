package Controllers.Category;

import entities.Category;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.CategoryService;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ModifierCategory implements Initializable {
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> statutComboBox;

    private Category category;
    private final CategoryService categoryService = new CategoryService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        statutComboBox.getItems().addAll("Actif", "Inactif");
    }

    public void setCategory(Category category) {
        this.category = category;
        nameField.setText(category.getName());
        descriptionField.setText(category.getDescription());
        statutComboBox.setValue(category.getStatut());
    }

    @FXML
    private void handleSave() {
        if (validateInputs()) {
            category.setName(nameField.getText());
            category.setDescription(descriptionField.getText());
            category.setStatut(statutComboBox.getValue());

            try {
                categoryService.modifier(category);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie modifiée avec succès");
                closeWindow();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateInputs() {
        if (nameField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le nom est obligatoire");
            return false;
        }
        if (descriptionField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La description est obligatoire");
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

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}