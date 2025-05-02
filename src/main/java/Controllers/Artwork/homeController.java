package Controllers.Artwork;

import entities.Artwork;
import entities.Session;
import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import service.ArtworkService;
import utils.SceneSwitch;
import service.ArtworklikeService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class homeController {

    private final ArtworkService artworkService = new ArtworkService();
    private final ArtworklikeService artworklikeService = new ArtworklikeService();



    // Assume this service gets the artworks from DB
    @FXML
    private FlowPane cardsContainer;
    @FXML
    private VBox rootVBox; // Root VBox to handle the layout
    @FXML private TextField searchField;

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

            List<Artwork> artworks = artworkService.getAllArtworks();

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

            String keyword = searchField.getText().trim().toLowerCase();

            List<Artwork> artworks = artworkService.getAllArtworks();

            if (!keyword.isEmpty()) {
                artworks = artworks.stream()
                        .filter(a ->
                                (a.getName() != null && a.getName().toLowerCase().contains(keyword)) ||
                                        (a.getTheme() != null && a.getTheme().toLowerCase().contains(keyword)))
                        .toList();
            }

            cardsContainer.getChildren().clear();

            for (Artwork artwork : artworks) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ArtworkCardLL.fxml"));
                Node card = loader.load();

                ArtworkCardController controller = loader.getController();
                controller.setData(artwork,currentUser); // Assume currentUser is accessible here

                // Pass the correct parent controller (make sure it's ArtworkDisplayController)
                ArtworkDisplayController artworkDisplayController = new ArtworkDisplayController();  // You should get this from the actual context
                controller.setParentController(artworkDisplayController);

                cardsContainer.getChildren().add(card);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private CheckBox filterByLikesCheckBox;
    @FXML
    public void onFilterChange() {
        try {
            loadArtworks();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    // This method loads and filters artworks based on the search field and checkbox
    private void loadArtworks() throws SQLException, IOException {
        User currentUser = Session.getCurrentUser();
        if (currentUser == null) {
            System.out.println("No user logged in");
            return;
        }

        // Get the keyword from the search field and the status of the checkbox
        String keyword = searchField.getText().trim().toLowerCase();
        boolean sortByLikes = filterByLikesCheckBox.isSelected();

        // Retrieve all artworks
        List<Artwork> artworks = artworkService.getAllArtworks();

        // Filter artworks by the keyword (name or theme)
        if (!keyword.isEmpty()) {
            artworks = artworks.stream()
                    .filter(a ->
                            (a.getName() != null && a.getName().toLowerCase().contains(keyword)) ||
                                    (a.getTheme() != null && a.getTheme().toLowerCase().contains(keyword)))
                    .toList();
        }

        // Sort artworks by likes if the checkbox is selected
        if (sortByLikes) {
            artworks = artworks.stream()
                    .sorted((a1, a2) -> {
                        try {
                            int likes1 = artworklikeService.getLikeCount(a1);
                            int likes2 = artworklikeService.getLikeCount(a2);
                            return Integer.compare(likes2, likes1); // descending order by likes
                        } catch (SQLException e) {
                            e.printStackTrace();
                            return 0;
                        }
                    })
                    .toList();
        }

        // Clear the current display and reload filtered artworks
        cardsContainer.getChildren().clear();
        for (Artwork artwork : artworks) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ArtworkCardLL.fxml"));
            Node card = loader.load();

            // Set the data for each artwork card
            ArtworkCardController controller = loader.getController();
            controller.setData(artwork, currentUser);

            // Add the card to the display container
            cardsContainer.getChildren().add(card);
        }}

}
