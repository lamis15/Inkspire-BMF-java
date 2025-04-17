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
<<<<<<< HEAD
        SceneSwitch.switchScene(mainRouter, "/AfficherEventBack.fxml");
    }
    @FXML
    void goCategory(ActionEvent event) {
        SceneSwitch.switchScene(mainRouter, "/AfficherCategory.fxml");
    }
    @FXML
    void goEv(ActionEvent event) {
        SceneSwitch.switchScene(mainRouter, "/AfficherEvent.fxml");
    }
=======
    }

>>>>>>> 85fe68fa65eb18b76018b05ec9434c92cbcfcf9e
    @FXML
    void goAuction(ActionEvent event) {
    }

    @FXML
    void goUser(ActionEvent event) {
        SceneSwitch.switchScene(mainRouter, "/UserUtils/AfficherUsers.fxml");;
    }

}
