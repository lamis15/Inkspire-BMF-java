package Controllers.Artwork;

import entities.Artwork;
import entities.Session;
import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import service.ArtworkService;
import utils.SceneSwitch;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ArtworkDisplayController {
    @FXML private TextField searchField;
    @FXML private FlowPane cardsContainer;
    @FXML private VBox rootVBox;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;

    private final ArtworkService artworkService = new ArtworkService();
    private int currentPage = 1;
    private final int itemsPerPage = 3;
    private List<Artwork> allArtworks;

    @FXML
    public void initialize() {
        try {
            User currentUser = Session.getCurrentUser();
            if (currentUser == null) {
                System.out.println("No user logged in");
                return;
            }

            int userId = currentUser.getId();
            allArtworks = artworkService.getArtworksByUserId(userId);

            loadPage(currentPage);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadPage(int page) {
        try {
            User currentUser = Session.getCurrentUser();
            if (currentUser == null || allArtworks == null) return;

            cardsContainer.getChildren().clear();

            int fromIndex = (page - 1) * itemsPerPage;
            int toIndex = Math.min(fromIndex + itemsPerPage, allArtworks.size());

            List<Artwork> pageItems = allArtworks.subList(fromIndex, toIndex);

            for (Artwork artwork : pageItems) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ArtworkCardLL.fxml"));
                Node card = loader.load();
                ArtworkCardController controller = loader.getController();
                controller.setData(artwork, currentUser);
                controller.setParentController(this);
                cardsContainer.getChildren().add(card);
            }

            prevPageButton.setDisable(currentPage == 1);
            nextPageButton.setDisable(toIndex >= allArtworks.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onAddClick(ActionEvent event) {
        SceneSwitch.switchScene(rootVBox, "/AjouterArtwork.fxml");
    }

    @FXML
    public void onSearch() {
        try {
            User currentUser = Session.getCurrentUser();
            if (currentUser == null) return;

            int userId = currentUser.getId();
            String keyword = searchField.getText().trim().toLowerCase();
            allArtworks = artworkService.getArtworksByUserId(userId);

            if (!keyword.isEmpty()) {
                allArtworks = allArtworks.stream()
                        .filter(a -> (a.getName() != null && a.getName().toLowerCase().contains(keyword)) ||
                                (a.getTheme() != null && a.getTheme().toLowerCase().contains(keyword)))
                        .toList();
            }

            currentPage = 1;
            loadPage(currentPage);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onPreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            loadPage(currentPage);
        }
    }

    @FXML
    public void onNextPage() {
        if ((currentPage * itemsPerPage) < allArtworks.size()) {
            currentPage++;
            loadPage(currentPage);
        }
    }
}
