package Controllers;

import entities.SceneManager;
import entities.Session;
import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import utils.SceneSwitch;

import javafx.scene.image.Image;
import javafx.scene.control.Label;

import java.io.File;

public class BaseWindow {

    @FXML private VBox sidebar;
    @FXML
    private Circle profilepicture ;

    @FXML
    private Label displayName;

    @FXML
    private AnchorPane mainRouter;
    
    @FXML
    private ImageView userIcon;

    @FXML
    void goCollections(ActionEvent event) {
        SceneSwitch.switchScene(mainRouter, "/AfficherCollections.fxml");
    }

    @FXML
    void goDonations(ActionEvent event) {
        User currentUser = Session.getCurrentUser();
        if (currentUser != null && currentUser.getRole() == 1) {
            SceneSwitch.switchScene(mainRouter, "/AfficherAllDonations.fxml");
        } else {
            SceneSwitch.switchScene(mainRouter, "/AfficherDonations.fxml");
        }
    }

    @FXML
    void goEvents(ActionEvent event) {
        User currentUser = Session.getCurrentUser();
        if (currentUser != null && currentUser.getRole() == 1) {
            SceneSwitch.switchScene(mainRouter, "/AfficherEventBack.fxml");
        } else {
            SceneSwitch.switchScene(mainRouter, "/AfficherEvent.fxml");
        }
    }
    @FXML
    void goCategory(ActionEvent event) {
        User currentUser = Session.getCurrentUser();
        if (currentUser != null && currentUser.getRole() == 1) {
            SceneSwitch.switchScene(mainRouter, "/AfficherCategory.fxml");
        }
    }
    @FXML
    void goAuction(ActionEvent event) {
        User currentUser = Session.getCurrentUser();
        if (currentUser != null && currentUser.getRole() == 1) {
            SceneSwitch.switchScene(mainRouter, "/AuctionUtils/Auction/AfficherAuctionAdmin.fxml");
        } else {
            SceneSwitch.switchScene(mainRouter, "/AuctionUtils/Auction/AfficherAuction.fxml");
        }

    }
    
    @FXML
    void goUser(MouseEvent event) {
        SceneSwitch.switchScene(mainRouter, "/UserUtils/ModifierUser.fxml");
    }
    
    @FXML
    void goMessages(MouseEvent event) {
        SceneSwitch.switchScene(mainRouter, "/messages/Messages.fxml");
    }

    @FXML
    void goArtwork(ActionEvent actionEvent) {
        SceneSwitch.switchScene(mainRouter, "/ArtworkDisplay.fxml");
    }

    @FXML
    public void initialize() {
        User currentUser = Session.getCurrentUser();
        if(currentUser.getRole() == 1) {  // Check if admin
            Button adminButton = new Button("Users");
            adminButton.getStyleClass().add("nav-button");
            adminButton.setMaxWidth(Double.MAX_VALUE);
            adminButton.setOnAction(this::handleUserButtonClicked);
            sidebar.getChildren().add(adminButton);
        }
        String imagePath;

        // Use default if user has no picture
        if (currentUser.getPicture() == null || currentUser.getPicture().isEmpty()) {
            imagePath = "C:/xampp/htdocs/images/profilePictures/user.png";
        } else {
            imagePath = "C:/xampp/htdocs/images/profilePictures/" + currentUser.getPicture();
        }

        displayName.setText(currentUser.getFirstName() + " " + currentUser.getLastName());

        try {
            File file = new File(imagePath);
            if (file.exists()) {
                Image image = new Image(file.toURI().toString());
                profilepicture.setFill(new ImagePattern(image));
            } else {
                System.err.println("Image file does not exist: " + imagePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML private void handleUserButtonClicked(ActionEvent event) {
        SceneSwitch.switchScene(mainRouter, "/UserUtils/ShowUsers.fxml");
    }



}
