package Controllers.Artwork;

import entities.Artwork;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import service.ArtworkService;

import java.io.File;
import java.sql.SQLException;
import java.util.function.Consumer;

public class EditArtworkDialog {

    @FXML private TextField nameField;
    @FXML private TextField themeField;
    @FXML private TextArea descriptionArea;
    @FXML private CheckBox statusCheckbox;
    @FXML
    private Label imagePathLabel;

    private String imagePath = null;

    private Artwork artwork;
    private final ArtworkService service = new ArtworkService();
    private Consumer<Artwork> callback;

    public void setArtwork(Artwork a) {
        this.artwork = a;
        nameField.setText(a.getName());
        themeField.setText(a.getTheme());
        descriptionArea.setText(a.getDescription());
        statusCheckbox.setSelected(a.getStatus() != null && a.getStatus());
        imagePathLabel.setText(a.getPicture());
    }

    public void setCallback(Consumer<Artwork> callback) {
        this.callback = callback;
    }

    @FXML
    void onCancel() {
        ((Stage) nameField.getScene().getWindow()).close();
    }
    @FXML
    public void chooseImage(ActionEvent event) {
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
    void onSave() {
        artwork.setName(nameField.getText());
        artwork.setTheme(themeField.getText());
        artwork.setDescription(descriptionArea.getText());
        artwork.setStatus(statusCheckbox.isSelected());
        artwork.setPicture(imagePath != null && !imagePath.isEmpty() ? imagePath : artwork.getPicture());

        try {
            service.modifier(artwork); // You must have this method
            if (callback != null) callback.accept(artwork);
            ((Stage) nameField.getScene().getWindow()).close();
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to update artwork.").show();
        }
    }
}