package Controllers.Artwork;

import entities.Artwork;
import entities.User;
import io.jsonwebtoken.io.IOException;
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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import service.ImageGenerationService;

public class AjouterArtworkController {
    public CheckBox status;
    @FXML
    private AnchorPane mainRouter;


    @FXML
    private TextField nameField;
    @FXML
    private Label nameError;

    @FXML
    private Label descriptionError;
    @FXML
    private Label imageError;
    @FXML
    private Label themeError;
    @FXML
    private Label blockedError;


    @FXML
    private ComboBox<String> themeComboBox;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private Label imagePathLabel;

    private String imagePath = null;
    @FXML
    private Button backButton;


    private final ArtworkService artworkService = new ArtworkService();

    @FXML
    void onBackClick(ActionEvent event) {
        SceneSwitch.switchScene(mainRouter, "/ArtworkDisplay.fxml");
    }

    @FXML
    void chooseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            // Define the destination path in the server's htdocs folder
            String destinationFolder = "C:/xampp/htdocs/images/artwork/";  // Change this path as necessary
            String destinationFilePath = destinationFolder + file.getName();

            File destFile = new File(destinationFilePath);

            try {
                // Copy the file to the destination folder in htdocs
                Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Store the relative path to the image
                imagePath = "images/artwork/" + file.getName();  // Relative path for use in web server

                // Update the label with the image name
                imagePathLabel.setText(file.getName());
            } catch (IOException e) {
                e.printStackTrace();
                imageError.setText("Error saving image to server.");
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    @FXML
    void addArtwork(ActionEvent event) {
        // Reset error labels
        nameError.setText("");
        descriptionError.setText("");
        imageError.setText("");
        themeError.setText("");


        String name = nameField.getText();
        String theme = themeComboBox.getValue();
        String description = descriptionArea.getText();
        boolean isOnBid = status.isSelected();

        boolean isValid = true;

        // Name validation
        if (name == null || name.trim().isEmpty()) {
            nameError.setText("Name cannot be empty.");
            isValid = false;
        } else if (name.length() < 3 || name.length() > 15) {
            nameError.setText("Name must be between 3 and 15 characters.");
            isValid = false;
        }

        // Description validation
        if (description == null || description.trim().isEmpty()) {
            descriptionError.setText("Description cannot be empty.");
            isValid = false;
        } else if (description.length() > 50) {
            descriptionError.setText("Description cannot exceed 50 characters.");
            isValid = false;
        }

        // Image validation
        if (imagePath == null) {
            imageError.setText("Please select or generate an image.");
            isValid = false;
        }

        // Theme validation
        if (theme == null || theme.trim().isEmpty()) {
            themeError.setText("Please choose a theme.");
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        try {
            User currentUser = Session.getCurrentUser();
            Artwork artwork = new Artwork(name, theme, description, imagePath, isOnBid, currentUser);

            artworkService.ajouter(artwork);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Artwork Added");
            alert.setContentText("The artwork has been added successfully.");
            alert.showAndWait();

            clearForm();

        } catch (SQLException e) {
            e.printStackTrace();
            blockedError.setText("An error occurred while adding the artwork. Please try again.");
        }
    }


    private void clearForm() {
        // Clear the form fields
        nameField.clear();
        themeComboBox.setValue(null);
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

    @FXML
    private ImageView artworkImageView;

    private String generatedImageUrl = null;

    @FXML
    private void onGenerateImage() {
        String prompt = descriptionArea.getText();
        if (prompt == null || prompt.isEmpty()) {
            descriptionError.setText("Prompt cannot be empty.");
            return;
        }

        try {
            // Get the relative path of the image generated
            generatedImageUrl = ImageGenerationService.generateImage(prompt);
            System.out.println("Generated Image URI (relative): " + generatedImageUrl);

            // Construct the full file path
            String fullPath = "file:C:/xampp/htdocs/" + generatedImageUrl;
            System.out.println("Full Image Path: " + fullPath);

            // Create the Image object using the full file path
            Image image = new Image(fullPath);
            if (image.isError()) {
                System.out.println("⚠️ Failed to load image: " + image.getException().getMessage());
            }

            // Display the image in the ImageView
            artworkImageView.setImage(image);
            imagePath = generatedImageUrl;  // Store the relative path
            imagePathLabel.setText("Generated Image");

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Image Generation Failed");
            alert.setContentText("An error occurred while generating the image.");
            alert.showAndWait();
        }
    }
}