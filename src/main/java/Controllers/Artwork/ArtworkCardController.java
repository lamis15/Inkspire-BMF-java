package Controllers.Artwork;

import entities.Artwork;
import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import service.ArtworklikeService;

import java.io.IOException;
import java.sql.SQLException;

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

    @FXML
    private Button likeButton;
    @FXML
    private Label likeCountLabel;

    private Artwork artwork;
    private User currentUser;

    private final ArtworklikeService likeService = new ArtworklikeService();

    public void setParentController(ArtworkDisplayController controller) {
        // If needed for future interaction
    }

    public void setData(Artwork artwork, User user) {
        this.artwork = artwork;
        this.currentUser = user;

        // Set text content
        titleLabel.setText(artwork.getName());
        descriptionLabel.setText(artwork.getDescription());
        goalLabel.setText(artwork.getTheme());

        // Set status
        if (artwork.getStatus() != null) {
            if (artwork.getStatus()) {
                statusLabel.setText("on bid");
                statusLabel.setStyle("-fx-text-fill: green;"); // Green for active
            } else {
                statusLabel.setText("off bid");
                statusLabel.setStyle("-fx-text-fill: red;"); // Red for inactive
            }
        } else {
            statusLabel.setText("Unknown");
        }

        // Load image
        if (artwork.getPicture() != null && !artwork.getPicture().isEmpty()) {
            try {
                imageView.setImage(new Image(artwork.getPicture(), true));
            } catch (Exception e) {
                System.out.println("Could not load image: " + artwork.getPicture());
            }
        }


        updateLikeButtonLabel();


        updateLikeCountLabel();
    }

    private void updateLikeCountLabel() {
        try {
            if (artwork != null) {
                int likeCount = likeService.getLikeCount(artwork); // Get like count
                likeCountLabel.setText(likeCount + " Likes");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to load like count. Please try again.");
            alert.show();
        }
    }

    private void updateLikeButtonLabel() {
        try {
            if (currentUser != null && artwork != null) {
                boolean liked = !likeService.hasUserLiked(artwork, currentUser);
                likeButton.setText("♥");

                if (liked) {
                    // Make the heart bigger and red when liked
                    likeButton.setStyle("-fx-font-size: 30px; -fx-text-fill: gray;");
                } else {
                    // Set a smaller size and gray color when unliked
                    likeButton.setStyle("-fx-font-size: 40px; -fx-text-fill: red;");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLikeButtonClick(ActionEvent event) {
        try {
            if (artwork != null && currentUser != null) {
                // Toggle the like status for the artwork
                likeService.toggleLike(artwork, currentUser);

                // Update the like button text after toggling the like status
                updateLikeButtonLabel();

                // Update the like count dynamically
                updateLikeCountLabel();

                System.out.println("Like status toggled.");
            } else {
                System.out.println("Artwork or user is not set.");
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please log in to like artworks.");
                alert.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleCardClick(MouseEvent event) {
        if (artwork == null) {
            System.out.println("No artwork data found for the card click.");
            Alert alert = new Alert(Alert.AlertType.ERROR, "Unable to display artwork details. The artwork is missing.");
            alert.show();
            return;
        }

        try {
            System.out.println("Artwork card clicked: " + artwork.getId() + " - " + artwork.getName());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ArtworkDetails.fxml"));
            Parent detailsView = loader.load();

            ArtworkDetails controller = loader.getController();
            controller.setArtwork(artwork);
            controller.loadComments();

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
                Alert alert = new Alert(Alert.AlertType.ERROR, "Could not load artwork details. Please try again.");
                alert.show();
            }

        } catch (IOException e) {
            System.out.println("Error loading artwork details: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "An error occurred while loading artwork details. Please try again.");
            alert.show();
        }
    }

    public Label getLikeCountLabel() {
        return likeCountLabel;
    }

    public void setLikeCountLabel(Label likeCountLabel) {
        this.likeCountLabel = likeCountLabel;
    }
}
