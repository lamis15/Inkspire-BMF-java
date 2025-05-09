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
import service.UserService;


import java.io.IOException;
import java.sql.SQLException;

public class ArtworkCardController {



    @FXML
    private Label ownerlabel;

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
    private UserService userService;

    @FXML
    public void initialize() throws SQLException {
        userService = new UserService();
    }

    public void setParentController(ArtworkDisplayController controller) {
        // Reserved for future use
    }
    public void setArtworkImage(Image image) {
        imageView.setImage(image);
    }

    public void setData(Artwork artwork, User user) {
        this.artwork = artwork;
        this.currentUser = user;

        titleLabel.setText(artwork.getName());

        try {
            String ownerName = artwork.getUser() != null
                    ? userService.getById(artwork.getUser().getId()).getFirstName()
                    : "Unknown User";
            ownerlabel.setText("👤 " + ownerName);
        } catch (SQLException e) {
            ownerlabel.setText("👤 Unknown User");
            e.printStackTrace();
        }

        descriptionLabel.setText(artwork.getDescription());
        goalLabel.setText(artwork.getTheme());

        if (artwork.getStatus() != null) {
            if (artwork.getStatus()) {
                statusLabel.setText("on bid");
                statusLabel.setStyle("-fx-text-fill: green;");
            } else {
                statusLabel.setText("off bid");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } else {
            statusLabel.setText("Unknown");
        }

        if (artwork.getPicture() != null && !artwork.getPicture().isEmpty()) {
            try {
                String baseUrl = "http://localhost/";
                String imageUrl = baseUrl + artwork.getPicture();

                Image image = new Image(imageUrl, true);
                imageView.setImage(image);
            } catch (Exception e) {
                System.out.println("Could not load image: " + artwork.getPicture());
                e.printStackTrace();
            }
        }


        updateLikeButtonLabel();
        updateLikeCountLabel();
    }

    private void updateLikeCountLabel() {
        try {
            if (artwork != null) {
                int likeCount = likeService.getLikeCount(artwork);
                likeCountLabel.setText(likeCount + " Likes");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Failed to load like count.");
        }
    }

    private void updateLikeButtonLabel() {
        try {
            if (currentUser != null && artwork != null) {
                boolean liked = !likeService.hasUserLiked(artwork, currentUser);
                likeButton.setText("♥");

                likeButton.setStyle(liked
                        ? "-fx-font-size: 30px; -fx-text-fill: gray;"
                        : "-fx-font-size: 40px; -fx-text-fill: red;");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLikeButtonClick(ActionEvent event) {
        try {
            if (artwork != null && currentUser != null) {
                likeService.toggleLike(artwork, currentUser);
                updateLikeButtonLabel();
                updateLikeCountLabel();
            } else {
                showAlert(Alert.AlertType.WARNING, "Please log in to like artworks.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCardClick(MouseEvent event) {
        if (artwork == null) {
            showAlert(Alert.AlertType.ERROR, "Unable to display artwork details.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ArtworkDetails.fxml"));
            Parent detailsView = loader.load();

            ArtworkDetails controller = loader.getController();
            controller.setArtwork(artwork);
            controller.loadComments();

            Node source = (Node) event.getSource();
            AnchorPane mainRouter = (AnchorPane) source.getScene().getRoot().lookup("#mainRouter");

            if (mainRouter != null) {
                mainRouter.getChildren().setAll(detailsView);
                AnchorPane.setTopAnchor(detailsView, 0.0);
                AnchorPane.setRightAnchor(detailsView, 0.0);
                AnchorPane.setBottomAnchor(detailsView, 0.0);
                AnchorPane.setLeftAnchor(detailsView, 0.0);
            } else {
                showAlert(Alert.AlertType.ERROR, "Could not find main layout.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error loading artwork details.");
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message);
        alert.show();
    }

    public Label getLikeCountLabel() {
        return likeCountLabel;
    }

    public void setLikeCountLabel(Label likeCountLabel) {
        this.likeCountLabel = likeCountLabel;
    }
}
