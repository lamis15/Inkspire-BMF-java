package Controllers;

import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import utils.SceneSwitch;

import javafx.scene.image.Image;
import javafx.scene.control.Label;

import java.io.File;

public class BaseWindow {


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
        User currentUser = entities.Session.getCurrentUser();
        if (currentUser != null && currentUser.getRole() == 1) {
            SceneSwitch.switchScene(mainRouter, "/AfficherAllDonations.fxml");
        } else {
            SceneSwitch.switchScene(mainRouter, "/AfficherDonations.fxml");
        }
    }

    @FXML
    void goEvents(ActionEvent event) {
        User currentUser = entities.Session.getCurrentUser();
        if (currentUser != null && currentUser.getRole() == 1) {
            SceneSwitch.switchScene(mainRouter, "/AfficherEventBack.fxml");
        } else {
            SceneSwitch.switchScene(mainRouter, "/AfficherEvent.fxml");
        }
    }
    @FXML
    void goCategory(ActionEvent event) {
        User currentUser = entities.Session.getCurrentUser();
        if (currentUser != null && currentUser.getRole() == 1) {
            SceneSwitch.switchScene(mainRouter, "/AfficherCategory.fxml");
        }
    }
    @FXML
    void goAuction(ActionEvent event) {
        User currentUser = entities.Session.getCurrentUser();
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
    void goArtwork(ActionEvent actionEvent) {
        SceneSwitch.switchScene(mainRouter, "/ArtworkDisplay.fxml");
    }

    @FXML
    public void initialize() {
        User currentUser = entities.Session.getCurrentUser();
        if (currentUser != null && currentUser.getRole() == 0) {

            displayName.setText(currentUser.getFirstName() + " " + currentUser.getLastName());

            String imagePath = "C:/xampp/htdocs/images/profilePictures/" + currentUser.getPicture();

            if (imagePath != null && !imagePath.isEmpty()) {
                try {
                    File file = new File(imagePath);
                    if (file.exists()) {
                        Image image = new Image(file.toURI().toString());
                        userIcon.setImage(image);
                    } else {
                        System.out.println("Image file not found at: " + imagePath);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
