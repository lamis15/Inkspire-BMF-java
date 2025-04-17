package Controllers.Artwork;

import entities.Artwork;
import entities.Session;
import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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
            User currentUser = Session.getCurrentUser();
            if (currentUser == null) {
                System.out.println("No user logged in");
                return;
            }

            int userId = currentUser.getId();
            System.out.println("Fetching artworks for user ID: " + userId);

            List<Artwork> artworks = artworkService.getArtworksByUserId(userId);

            System.out.println("Found " + artworks.size() + " artworks");

            if (artworks.isEmpty()) {
                System.out.println("No artworks found for this user");
            }

            cardsContainer.getChildren().clear(); // Clear existing cards

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
            // Consider showing an alert to the user

        }
    }

    @FXML
    void onAddClick(ActionEvent event) {
        SceneSwitch.switchScene(rootVBox, "/AjouterArtwork.fxml");
    }
}
