package Controllers.Artwork;

import entities.Artwork;
import entities.Session;
import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
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

    @FXML private FlowPane cardsContainer;
    @FXML private VBox rootVBox;
    @FXML private TextField searchField;
    @FXML private CheckBox filterByLikesCheckBox;


    private List<Artwork> artworks;
    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 6;

    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;

    @FXML
    public void initialize() {
        try {
            loadArtworks();
            displayArtworks();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    // Loads artworks based on search and filter
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
        artworks = artworkService.getAllArtworks();

        // Filter artworks by the keyword (name or theme)
        if (!keyword.isEmpty()) {
            artworks = artworks.stream()
                    .filter(a -> (a.getName() != null && a.getName().toLowerCase().contains(keyword)) ||
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
    }

    // Displays the artworks based on the current page
    private void displayArtworks() {
        cardsContainer.getChildren().clear();

        int start = currentPage * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, artworks.size());

        for (int i = start; i < end; i++) {
            try {
                Artwork artwork = artworks.get(i);
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ArtworkCardLL.fxml"));
                Node card = loader.load();

                ArtworkCardController controller = loader.getController();
                controller.setData(artwork, Session.getCurrentUser());

                cardsContainer.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Disable the buttons if on the first or last page
        prevPageButton.setDisable(currentPage == 0);
        nextPageButton.setDisable((currentPage + 1) * ITEMS_PER_PAGE >= artworks.size());
    }

    @FXML
    void onAddClick(ActionEvent event) {
        SceneSwitch.switchScene(rootVBox, "/AjouterArtwork.fxml");
    }

    @FXML
    public void onSearch() {
        try {
            loadArtworks();
            currentPage = 0;  // Reset page on new search
            displayArtworks();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onFilterChange() {
        try {
            loadArtworks();
            currentPage = 0;  // Reset page on filter change
            displayArtworks();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    // Navigation Methods for Pagination
    @FXML
    void onPreviousPage(ActionEvent event) {
        if (currentPage > 0) {
            currentPage--;
            displayArtworks();
        }
    }

    @FXML
    void onNextPage(ActionEvent event) {
        if ((currentPage + 1) * ITEMS_PER_PAGE < artworks.size()) {
            currentPage++;
            displayArtworks();
        }
    }
}
