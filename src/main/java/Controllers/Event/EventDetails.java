package Controllers.Event;

import entities.Category;
import entities.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.CategoryService;
import service.EventService;
import utils.SceneSwitch;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class EventDetails implements javafx.fxml.Initializable {

    public Label imagelabel;
    @FXML
    private Label titleLabel;
    @FXML
    private Label startingDateLabel;
    @FXML
    private Label endingDateLabel;
    @FXML
    private Label locationlabel;
    @FXML
    private Label statusLabel;
    @FXML
    private ImageView imageView;
    @FXML
    private Button backButton;
    @FXML
    private FlowPane categoryContainer;

    private final EventService eventService = new EventService();
    private final CategoryService categoryService = new CategoryService();

    private Event event;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        categoryContainer.setHgap(15);
        categoryContainer.setVgap(15);
        categoryContainer.setPrefWrapLength(700);
    }

    public void setEvent(Event event) {
        this.event = event;

        titleLabel.setText(event.getTitle());
        locationlabel.setText(event.getLocation());
        startingDateLabel.setText(event.getStartingDate().toString());
        endingDateLabel.setText(event.getEndingDate().toString());

        if (event.getImage() != null && !event.getImage().isEmpty()) {
            try {
                Image image = new Image(event.getImage());
                imageView.setImage(image);
            } catch (Exception e) {
                System.out.println("Error loading image: " + e.getMessage());
            }
        }

        // Load and display categories for this event
        loadCategories();
    }

    private void loadCategories() {
        try {
            categoryContainer.getChildren().clear();
            System.out.println("Loading categories for event ID: " + event.getId()); // Debug log

            List<Category> categories = categoryService.getCategoriesForEvent(event.getId());
            System.out.println("Retrieved " + categories.size() + " categories"); // Debug log

            for (Category category : categories) {
                Label categoryLabel = new Label(category.getName());
                categoryLabel.getStyleClass().add("category-label");
                categoryContainer.getChildren().add(categoryLabel);
                System.out.println("Added category: " + category.getName()); // Debug log
            }

            if (categories.isEmpty()) {
                System.out.println("No categories found for event " + event.getId()); // Debug log
                Label noCategoriesLabel = new Label("No categories assigned");
                noCategoriesLabel.getStyleClass().add("no-categories-label");
                categoryContainer.getChildren().add(noCategoriesLabel);
            }
        } catch (SQLException e) {
            System.err.println("Error loading categories: " + e.getMessage());
            e.printStackTrace(); // Full stack trace
            Label errorLabel = new Label("Error loading categories");
            errorLabel.getStyleClass().add("category-error-label");
            categoryContainer.getChildren().add(errorLabel);
        }
    }

    @FXML
    private void onBackClick() {
        Node node = backButton.getScene().getRoot().lookup("#mainRouter");
        if (node instanceof Pane) {
            SceneSwitch.switchScene((Pane) node, "/EventUtils/AfficherEvent.fxml");
            System.out.println("Successfully navigated back to events view");
        } else {
            System.out.println("Could not find mainRouter for navigation");
        }
    }
}