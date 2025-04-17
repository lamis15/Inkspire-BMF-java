package Controllers;

import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import utils.SceneSwitch;

public class BaseWindow {

    @FXML
    private AnchorPane mainRouter;

    @FXML
    void goCollections(ActionEvent event) {
        SceneSwitch.switchScene(mainRouter, "/AfficherCollections.fxml");
    }

    @FXML
    void goDonations(ActionEvent event) {
        // Check user role to determine which donations view to show
        User currentUser = entities.Session.getCurrentUser();
        
        if (currentUser != null && currentUser.getRole() == 1) {
            // Admin user - show all donations
            SceneSwitch.switchScene(mainRouter, "/AfficherAllDonations.fxml");
        } else {
            // Regular user - show only their donations
            SceneSwitch.switchScene(mainRouter, "/AfficherDonations.fxml");
        }
    }

    @FXML
    void goEvents(ActionEvent event) {

    }
    @FXML
    void goAuction(ActionEvent event) {
        SceneSwitch.switchScene(mainRouter, "/AuctionUtils/Auction/AfficherAuction.fxml");;
    }

    @FXML
    void goUser(ActionEvent event) {
        SceneSwitch.switchScene(mainRouter, "/UserUtils/AfficherUsers.fxml");;
    }

}
