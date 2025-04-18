package Controllers;

import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import utils.SceneSwitch;

public class BaseWindow {

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
<<<<<<< HEAD
        User currentUser = entities.Session.getCurrentUser();
        if (currentUser != null && currentUser.getRole() == 1) {
            SceneSwitch.switchScene(mainRouter, "/AuctionUtils/Auction/AfficherAuctionAdmin.fxml");
        } else {
            SceneSwitch.switchScene(mainRouter, "/AuctionUtils/Auction/AfficherAuction.fxml");
        }

    }

    @FXML
    void goUser(ActionEvent event) {
        SceneSwitch.switchScene(mainRouter, "/UserUtils/AfficherUsers.fxml");
    }

    // OPTIONAL: if you still want mouse-based click handling separately
=======
        SceneSwitch.switchScene(mainRouter, "/AuctionUtils/Auction/AfficherAuction.fxml");;
    }

    
>>>>>>> c4663862cc0efb8c8219761cb1527ac05c3c7706
    @FXML
    void goUserWithMouse(MouseEvent event) {
        SceneSwitch.switchScene(mainRouter, "/UserUtils/AfficherUsers.fxml");
    }
<<<<<<< HEAD
=======
    @FXML
    public void goArtwork(ActionEvent actionEvent) {
        SceneSwitch.switchScene(mainRouter, "/AuctionUtils/ArtworkDisplay.fxml");
    }
>>>>>>> c4663862cc0efb8c8219761cb1527ac05c3c7706

    @FXML
    void goArtwork(ActionEvent actionEvent) {
        SceneSwitch.switchScene(mainRouter, "/ArtworkDisplay.fxml");
    }
}
