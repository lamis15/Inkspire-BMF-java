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
import javafx.scene.control.TextField;
import javafx.scene.control.TextField; // This is the correct import

import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ArtworkDisplayController {
    @FXML private TextField searchField;

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
                controller.setData(artwork, currentUser);

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

    @FXML

    public void onSearch() {
        try {
            // Get the current user's ID
            User currentUser = Session.getCurrentUser();
            if (currentUser == null) {
                System.out.println("No user logged in");
                return;
            }

            int userId = currentUser.getId();
            String keyword = searchField.getText().trim().toLowerCase();

            // Fetch all artworks, but filter them to only include those created by the current user
            List<Artwork> artworks = artworkService.getArtworksByUserId(userId);

            // If a keyword is entered, filter the artworks based on the keyword
            if (!keyword.isEmpty()) {
                artworks = artworks.stream()
                        .filter(a ->
                                (a.getName() != null && a.getName().toLowerCase().contains(keyword)) ||
                                        (a.getTheme() != null && a.getTheme().toLowerCase().contains(keyword)))
                        .toList();
            }

            // Clear the existing cards before adding filtered results
            cardsContainer.getChildren().clear();

            // Add filtered artwork cards
            for (Artwork artwork : artworks) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ArtworkCardLL.fxml"));
                Node card = loader.load();  // This line throws IOException

                ArtworkCardController controller = loader.getController();
                controller.setData(artwork, currentUser); // Assume currentUser is accessible here

                controller.setParentController(this);

                cardsContainer.getChildren().add(card);
            }
        } catch (IOException e) {
            e.printStackTrace();  // Log the exception or show an error alert
        } catch (SQLException e) {
            e.printStackTrace();  // Handle the SQL exception if necessary
        }
    }



}
