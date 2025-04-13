package Controllers.Collections;

import entities.Collections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import service.CollectionsService;
import utils.SceneSwitch;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherCollections {

    @FXML
    private FlowPane cardsContainer;

    @FXML
    private VBox rootVBox;

    private final CollectionsService service = new CollectionsService();

    @FXML
    public void initialize() {
        try {
            List<Collections> list = service.recuperer();

            for (Collections c : list) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/CollectionCard.fxml"));
                Node card = loader.load();

                CollectionCard controller = loader.getController();
                controller.setData(c);

                cardsContainer.getChildren().add(card);
            }

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onAddClick(ActionEvent event) {
        SceneSwitch.switchScene(rootVBox, "/AjouterCollections.fxml");
    }
}
