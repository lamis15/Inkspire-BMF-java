package Controllers.Artwork;

import entities.Artwork;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class ArtworkCardController {
    @FXML
    private ImageView imageView;

    @FXML
    private Label titleLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Label goalLabel;

    @FXML
    private Label statusLabel;

    private Artwork artwork;
    private ArtworkDisplayController parentController;


    public void setParentController(ArtworkDisplayController controller) {
        this.parentController = controller;
    }

    public void setData(Artwork artwork) {
        this.artwork = artwork;

        titleLabel.setText(artwork.getName());
        descriptionLabel.setText(artwork.getDescription());
        goalLabel.setText(artwork.getTheme());

        if (artwork.getStatus() != null) {
            statusLabel.setText(artwork.getStatus() ? "Active" : "Inactive");
        } else {
            statusLabel.setText("Unknown");
        }

        if (artwork.getPicture() != null && !artwork.getPicture().isEmpty()) {
            try {
                imageView.setImage(new Image(artwork.getPicture(), true));
            } catch (Exception e) {
                System.out.println("Could not load image: " + artwork.getPicture());
            }
        }
    }

    @FXML
    private void handleCardClick(MouseEvent event) {
        try {
            System.out.println("Artwork card clicked: " + artwork.getId() + " - " + artwork.getName());

            // Load the ArtworkDetails view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ArtworkDetails.fxml"));
            Parent detailsView = loader.load();

            // Get the controller and set the artwork
            ArtworkDetails controller = loader.getController();
            controller.setArtwork(artwork);

            // Find the mainRouter in the StackPane structure
            Node source = (Node) event.getSource();
            AnchorPane mainRouter = (AnchorPane) source.getScene().getRoot().lookup("#mainRouter");

            if (mainRouter != null) {
                mainRouter.getChildren().clear();
                mainRouter.getChildren().add(detailsView);

                AnchorPane.setTopAnchor(detailsView, 0.0);
                AnchorPane.setRightAnchor(detailsView, 0.0);
                AnchorPane.setBottomAnchor(detailsView, 0.0);
                AnchorPane.setLeftAnchor(detailsView, 0.0);

                System.out.println("Successfully loaded artwork details into mainRouter");
            } else {
                System.out.println("Could not find mainRouter to load artwork details");
            }

        } catch (IOException e) {
            System.out.println("Error loading artwork details: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
