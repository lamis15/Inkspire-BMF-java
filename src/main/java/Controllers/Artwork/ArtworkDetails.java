package Controllers.Artwork;

import entities.Artwork;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import entities.User;
import entities.Session;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.ArtworkService;
import service.ArtworklikeService;
import utils.SceneSwitch;
import entities.Comment;
import service.CommentService;
import javafx.scene.layout.HBox;


import javafx.scene.control.Label;
import javafx.scene.control.Button;


import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ArtworkDetails {
    @FXML
    private Label commentError;
    @FXML private Button backButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Label likeCountLabel;
    @FXML private Label nameLabel;
    @FXML private Label themeLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label statusLabel;
    @FXML private ImageView imageView;
    @FXML
    private AnchorPane mainRouter;
    private Artwork artwork;
    private final ArtworkService artworkService = new ArtworkService();
    private final ArtworklikeService likeService = new ArtworklikeService();

    public void setArtwork(Artwork artwork) {
        User currentUser = Session.getCurrentUser();
        boolean isOwner = currentUser != null && currentUser.equals(artwork.getUser());
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
        editButton.setVisible(isOwner);
        deleteButton.setVisible(isOwner);
        showLikeCount();
    }

    @FXML
    void onBackClick() {
       // SceneSwitch.goBack(backButton);
    }

    @FXML
    void onEditClick() {
        User currentUser = Session.getCurrentUser();

        if (currentUser == null || !currentUser.equals(artwork.getUser())) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Unauthorized");
            alert.setHeaderText("You are not allowed to edit this artwork.");
            alert.setContentText("Only the owner can edit this artwork.");
            alert.show();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditArtworkDetails.fxml"));
            VBox dialogRoot = loader.load();

            EditArtworkDialog controller = loader.getController();
            controller.setArtwork(artwork);
            controller.setCallback(updatedArtwork -> {
                setArtwork(updatedArtwork); // Update displayed artwork
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
        User currentUser = Session.getCurrentUser();

        if (currentUser == null || !currentUser.equals(artwork.getUser())) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Unauthorized");
            alert.setHeaderText("You are not allowed to delete this artwork.");
            alert.setContentText("Only the owner can delete this artwork.");
            alert.show();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Are you sure you want to delete this artwork?");
        alert.setContentText(artwork.getName());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    artworkService.deleteArtwork(artwork.getId());
                    // Optionally go back or refresh UI
                    // SceneSwitch.goBack(deleteButton);
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
    @FXML private TextField commentField;
    @FXML private VBox commentsContainer;

    private final CommentService commentService = new CommentService();
    private final Comment comment = new Comment();

    @FXML
    void onAddComment(ActionEvent event) {

        String content = commentField.getText().trim();

        // Check if the comment content is empty
        if (content.isEmpty()) {
            commentError.setText("Comment cannot be empty.");
            System.out.println("no comment");
            return;
        }

        User currentUser = Session.getCurrentUser();
        if (currentUser == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No user is logged in.");
            alert.show();
            return;
        }

        // Create a new comment object
        Comment newComment = new Comment(currentUser, artwork, content);

        // Add the comment through the service
        boolean success = commentService.addComment(newComment);

        // Handle the result of the add comment operation
        if (success) {
            commentField.clear(); // Clear the comment field
            loadComments(); // Refresh the comment list
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to post comment.");
            alert.show();
        }
    }

    public void loadComments() {
        commentsContainer.getChildren().clear();

        try {
            List<Comment> comments = commentService.getCommentsByArtwork(artwork);

            if (comments.isEmpty()) {
                Label noComments = new Label("No comments yet.");
                noComments.setStyle("-fx-text-fill: black; -fx-font-size: 14px; -fx-font-weight: normal;");


                commentsContainer.getChildren().add(noComments);
            } else {
                for (Comment comment : comments) {
                    // Label for comment content
                    Label commentLabel = new Label(comment.getUser().getFirstName() + ": " + comment.getContent());
                    commentLabel.getStyleClass().add("comment-text");
                    commentLabel.setStyle("-fx-text-fill: black;");

                    // Get current user
                    User currentUser = Session.getCurrentUser();

                    // Buttons for edit and delete (only if the user is the creator of the comment)
                    Button editBtn = null;
                    Button deleteBtn = null;

                    if (currentUser != null && currentUser.equals(comment.getUser())) {
                        // Only show edit and delete buttons if the current user is the comment's author
                        editBtn = new Button("Edit");
                        editBtn.setOnAction(e -> onEditComment(comment));

                        deleteBtn = new Button("Delete");
                        deleteBtn.setOnAction(e -> onDeleteComment(comment));

                        // Style buttons if needed
                        editBtn.getStyleClass().add("edit-button");
                        deleteBtn.getStyleClass().add("deletecomment");
                    }

                    // Put everything into HBox
                    HBox commentBox = new HBox(10); // spacing
                    commentBox.getChildren().addAll(commentLabel);

                    // Add edit and delete buttons if available
                    if (editBtn != null && deleteBtn != null) {
                        commentBox.getChildren().addAll(editBtn, deleteBtn);
                    }

                    commentBox.setStyle("-fx-padding: 5;");

                    commentsContainer.getChildren().add(commentBox);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load comments.").show();
        }
    }

    private void onEditComment(Comment comment) {
        TextInputDialog dialog = new TextInputDialog(comment.getContent());
        dialog.setTitle("Edit Comment");
        dialog.setHeaderText("Edit your comment:");
        dialog.showAndWait().ifPresent(newContent -> {
            if (!newContent.trim().isEmpty()) {
                if (commentService.editComment(comment.getId(), newContent)) {
                    loadComments();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Failed to edit comment.").show();
                }
            } else {
                new Alert(Alert.AlertType.WARNING, "Comment cannot be empty.").show();
            }
        });
    }

    private void onDeleteComment(Comment comment) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this comment?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (commentService.deleteComment(comment.getId())) {
                    loadComments();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Failed to delete comment.").show();
                }
            }
        });
    }
    private void showLikeCount() {
        try {
            Map<Integer, Integer> likesMap = artworkService.getLikesPerArtwork();
            int count = likesMap.getOrDefault(artwork.getId(), 0);
            likeCountLabel.setText(count + " Likes");
        } catch (SQLException e) {
            likeCountLabel.setText("0 Likes");
            e.printStackTrace();
        }
    }

}




