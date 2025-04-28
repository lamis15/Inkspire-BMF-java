package Controllers.Artwork;

import entities.Artwork;
import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import service.ArtworkService;
import utils.SceneSwitch;
import entities.Session;
import java.io.File;
import java.sql.SQLException;

public class AjouterArtworkController {
    public CheckBox status;
    @FXML
    private AnchorPane mainRouter;


    @FXML
    private TextField nameField;
    @FXML
    private TextField themeField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private Label imagePathLabel;

    private String imagePath = null;
    @FXML private Button backButton;


    private final ArtworkService artworkService = new ArtworkService();

    @FXML
    void onBackClick(ActionEvent event) {
        SceneSwitch.switchScene(mainRouter, "/ArtworkDisplay.fxml");
    }

    @FXML
    void chooseImage(ActionEvent event) {
        // Open a file chooser to select an image
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            imagePath = file.toURI().toString();
            imagePathLabel.setText(file.getName());
        }
    }

    @FXML
    void addArtwork(ActionEvent event) {
        // Get user inputs
        String name = nameField.getText();
        String theme = themeField.getText();
        String description = descriptionArea.getText();
        boolean isOnBid = status.isSelected();


        if (name.isEmpty() || theme.isEmpty() || description.isEmpty() || imagePath == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid Input");
            alert.setContentText("Please fill all fields and choose an image.");
            alert.showAndWait();
            return;
        }

        try {


            User currentUser = Session.getCurrentUser();

            Artwork artwork = new Artwork(name, theme, description, imagePath, isOnBid, currentUser);


            // Insert into DB
            artworkService.ajouter(artwork);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Artwork Added");
            alert.setContentText("The artwork has been added successfully.");
            alert.showAndWait();

            clearForm();

        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid Price");
            alert.setContentText("Price must be a valid number.");
            alert.showAndWait();
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Database Error");
            alert.setContentText("An error occurred while adding the artwork.");
            alert.showAndWait();
        }
    }


    private void clearForm() {
        // Clear the form fields
        nameField.clear();
        themeField.clear();
        descriptionArea.clear();
        imagePathLabel.setText("No image chosen");
    }

    @FXML
    public void goBack(ActionEvent event) {

        // Find the mainRouter in the scene graph
        Node node = backButton.getScene().getRoot().lookup("#mainRouter");

        if (node instanceof Pane) {
            SceneSwitch.switchScene((Pane) node, "/ArtworkDisplay.fxml");
            System.out.println("Successfully navigated back to Artwork");
        } else {
            System.out.println("Could not find mainRouter for navigation");

        }
    }
}