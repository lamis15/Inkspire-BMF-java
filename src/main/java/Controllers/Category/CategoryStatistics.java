package Controllers.Category;

import entities.Category;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import service.CategoryService;

import java.sql.SQLException;

public class CategoryStatistics {

    @FXML private Label categoryNameLabel;
    @FXML private Label eventCountLabel;

    private Category category;
    private final CategoryService categoryService = new CategoryService();

    public void setCategory(Category category) {
        this.category = category;
        initializeStatistics();
    }

    private void initializeStatistics() {
        if (category != null) {
            categoryNameLabel.setText("Catégorie: " + category.getName());
            try {
                int eventCount = categoryService.getEventCountForCategory(category.getId());
                eventCountLabel.setText("Nombre d'événements: " + eventCount);
            } catch (SQLException e) {
                eventCountLabel.setText("Erreur lors du chargement des statistiques: " + e.getMessage());
            }
        }
    }

    @FXML
    private void closeWindow(ActionEvent event) {
        Stage stage = (Stage) categoryNameLabel.getScene().getWindow();
        stage.close();
    }
}