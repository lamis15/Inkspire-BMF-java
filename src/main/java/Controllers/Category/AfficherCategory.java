package Controllers.Category;

import entities.Category;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import service.CategoryService;
import utils.SceneSwitch;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

public class AfficherCategory extends SceneSwitch {

    @FXML private FlowPane cardsContainer;
    @FXML private VBox rootVBox;
    @FXML private ScrollPane scrollPane;

    private final CategoryService categoryService = new CategoryService();

    @FXML
    public void initialize() {
        loadCategoryCards();
    }

    private void loadCategoryCards() {
        cardsContainer.getChildren().clear();

        try {
            List<Category> categories = categoryService.afficher();

            for (Category category : categories) {
                VBox card = createCategoryCard(category);
                cardsContainer.getChildren().add(card);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les catégories: " + e.getMessage());
        }
    }

    private VBox createCategoryCard(Category category) {
        VBox card = new VBox();
        card.setSpacing(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setMinWidth(250);
        card.setMaxWidth(250);

        Label nameLabel = new Label(category.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label descLabel = new Label(category.getDescription());
        descLabel.setWrapText(true);

        Label statusLabel = new Label("Statut: " + category.getStatut());
        statusLabel.setTextFill(category.getStatut().equals("Actif") ? Color.GREEN : Color.RED);

        // Ajouter le nombre d'événements
        int eventCount;
        try {
            eventCount = categoryService.getEventCountForCategory(category.getId());
        } catch (SQLException e) {
            eventCount = 0;
        }
        Label eventCountLabel = new Label("Événements: " + eventCount);
        eventCountLabel.setStyle("-fx-font-size: 14px;");

        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);

        Button editBtn = new Button("Modifier");
        editBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        editBtn.setOnAction(e -> handleEdit(category));

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> handleDelete(category));

        buttonsBox.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(nameLabel, descLabel, statusLabel, eventCountLabel, buttonsBox);
        return card;
    }

    @FXML
    private void handleAdd(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/CategoryUtils/AjoutCategory.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle("Ajouter une catégorie");
        stage.setScene(new Scene(root));
        stage.showAndWait();

        loadCategoryCards();
    }

    private void handleEdit(Category category) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CategoryUtils/ModifierCategory.fxml"));
            VBox modifierRoot = loader.load();

            ModifierCategory controller = loader.getController();
            controller.setCategory(category);

            Stage stage = new Stage();
            stage.setTitle("Modifier la catégorie");
            stage.setScene(new Scene(modifierRoot));
            stage.showAndWait();

            loadCategoryCards();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir l'interface de modification");
        }
    }

    private void handleDelete(Category category) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la catégorie");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer '" + category.getName() + "' ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    categoryService.supprimer(category.getId());
                    cardsContainer.getChildren().removeIf(node -> {
                        if (node instanceof VBox card && card.getChildren().get(0) instanceof Label label) {
                            return label.getText().equals(category.getName());
                        }
                        return false;
                    });
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la suppression: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleStatistics(ActionEvent event) {
        try {
            Stage statsStage = new Stage();
            statsStage.setTitle("Statistiques des catégories");

            // Créer les axes
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("Catégorie");
            yAxis.setLabel("Nombre d'événements");

            // Créer le graphique
            BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
            barChart.setTitle("Nombre d'événements par catégorie");
            barChart.setLegendVisible(false); // Masquer la légende car chaque barre a une couleur unique

            // Liste de couleurs prédéfinies
            String[] colors = {
                    "#FF6347", // Tomato
                    "#4682B4", // SteelBlue
                    "#9ACD32", // YellowGreen
                    "#FFD700", // Gold
                    "#6A5ACD", // SlateBlue
                    "#FF4500", // OrangeRed
                    "#20B2AA", // LightSeaGreen
                    "#9932CC"  // DarkOrchid
            };

            // Ajouter les données
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Événements");

            List<Category> categories = categoryService.afficher();
            for (int i = 0; i < categories.size(); i++) {
                Category category = categories.get(i);
                int eventCount = categoryService.getEventCountForCategory(category.getId());
                XYChart.Data<String, Number> data = new XYChart.Data<>(category.getName(), eventCount);
                final int colorIndex = i % colors.length; // Capture a final variable
                data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        newNode.setStyle("-fx-bar-fill: " + colors[colorIndex] + ";");
                    }
                });
                series.getData().add(data);
            }

            barChart.getData().add(series);

            // Ajouter du style global au graphique
            barChart.setStyle("-fx-background-color: #f4f4f4;");

            // Créer la scène
            VBox statsVBox = new VBox(10);
            statsVBox.setPadding(new Insets(20));
            statsVBox.getChildren().add(barChart);

            Scene statsScene = new Scene(statsVBox, 600, 400);
            statsStage.setScene(statsScene);
            statsStage.show();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les statistiques: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void onAddClick(ActionEvent actionEvent) {
        SceneSwitch.switchScene(rootVBox, "/CategoryUtils/AjoutCategory.fxml");
    }
}