package Controllers.Artwork;

import entities.Artwork;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.ArtworkService;
import utils.SceneSwitch;

import java.io.IOException;
import java.sql.SQLException;

public class ArtworkDetails {

    @FXML private Button backButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    @FXML private Label nameLabel;
    @FXML private Label themeLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label statusLabel;
    @FXML private ImageView imageView;
    @FXML
    private AnchorPane mainRouter;
    private Artwork artwork;
    private final ArtworkService artworkService = new ArtworkService();

    public void setArtwork(Artwork artwork) {
        this.artwork = artwork;
        nameLabel.setText(artwork.getName());
        themeLabel.setText("Theme: " + artwork.getTheme());
        descriptionLabel.setText(artwork.getDescription());
        statusLabel.setText("Status: " + (artwork.getStatus() != null && artwork.getStatus() ? "Available for Bid" : "Not for Bid"));

        if (artwork.getPicture() != null && !artwork.getPicture().isEmpty()) {
            imageView.setImage(new Image(artwork.getPicture()));
        } else {
            imageView.setImage(new Image("file:default.png")); // fallback image
        }
    }

    @FXML
    void onBackClick() {
       // SceneSwitch.goBack(backButton);
    }

    @FXML
    void onEditClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditArtworkDetails.fxml"));
            VBox dialogRoot = loader.load();

            EditArtworkDialog controller = loader.getController();
            controller.setArtwork(artwork);
            controller.setCallback(updatedArtwork -> {
                setArtwork(updatedArtwork); // update displayed data
            });

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Edit Artwork");
            dialogStage.setScene(new Scene(dialogRoot));
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onDeleteClick() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Are you sure you want to delete this artwork?");
        alert.setContentText(artwork.getName());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    System.out.println(artwork.getId());
                    artworkService.supprimer(artwork.getId());
                  //  SceneSwitch.goBack(deleteButton);
                } catch (SQLException e) {
                    e.printStackTrace();
                    Alert error = new Alert(Alert.AlertType.ERROR, "Failed to delete artwork.");
                    error.show();
                }
            }
        });
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
