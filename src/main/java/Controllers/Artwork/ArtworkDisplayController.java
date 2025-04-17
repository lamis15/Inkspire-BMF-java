package Controllers.Artwork;

import entities.Artwork;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import service.ArtworkService;
import utils.SceneSwitch;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ArtworkDisplayController {

    private final ArtworkService artworkService = new ArtworkService(); // Assume this service gets the artworks from DB
    @FXML
    private FlowPane cardsContainer; // This will hold the artwork cards
    @FXML
    private VBox rootVBox; // Root VBox to handle the layout

    @FXML
    public void initialize() {
        try {
            List<Artwork> artworks = artworkService.recuperer();

            for (Artwork artwork : artworks) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ArtworkCardLL.fxml"));
                Node card = loader.load();


                ArtworkCardController controller = loader.getController();
                controller.setData(artwork);
                controller.setParentController(this);

                cardsContainer.getChildren().add(card);
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onAddClick(ActionEvent event) {
        SceneSwitch.switchScene(rootVBox, "/AjouterArtwork.fxml");
    }
}
