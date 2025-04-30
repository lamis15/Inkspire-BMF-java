package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class SceneSwitch {

    public static void switchScene(Pane container, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneSwitch.class.getResource(fxmlPath));
            Parent root = loader.load();
            container.getChildren().setAll(root);

            // Set anchor constraints to make the view fill the container
            if (container instanceof AnchorPane && root instanceof Parent) {
                AnchorPane.setTopAnchor(root, 0.0);
                AnchorPane.setRightAnchor(root, 0.0);
                AnchorPane.setBottomAnchor(root, 0.0);
                AnchorPane.setLeftAnchor(root, 0.0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
