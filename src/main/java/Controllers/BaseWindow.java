package Controllers;

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
    }

    @FXML
    void goEvents(ActionEvent event) {
    }

    @FXML
    void goAuction(ActionEvent event) {
    }
}
