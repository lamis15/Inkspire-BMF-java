package Controllers.Artwork;

import entities.Artwork;
import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import service.ArtworkService;
import service.UserService;
import service.ArtworklikeService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.layout.VBox;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class ArtworkAdminController {

    @FXML
    private FlowPane artworkContainer;

    @FXML
    private TextField searchField;
    private ArtworklikeService artworklikeservices;
    private ArtworkService artworkService;
    private UserService userService;
    private Artwork artwork;

    @FXML
    public void initialize() throws SQLException {
        artworkService = new ArtworkService();
        artworklikeservices = new ArtworklikeService();
        userService = new UserService();
        refreshArtworks();
    }

    @FXML
    public void refreshArtworks() throws SQLException {
        displayArtworks(artworkService.getAllArtworks());
    }

    @FXML
    public void onSearch() throws SQLException {
        String keyword = searchField.getText().trim().toLowerCase();
        List<Artwork> artworks = artworkService.getAllArtworks();

        if (!keyword.isEmpty()) {
            artworks = artworks.stream()
                    .filter(a ->
                            (a.getName() != null && a.getName().toLowerCase().contains(keyword)) ||
                                    (a.getTheme() != null && a.getTheme().toLowerCase().contains(keyword)))
                    .toList();
        }

        displayArtworks(artworks);
    }

    private void displayArtworks(List<Artwork> artworks) throws SQLException {
        artworkContainer.getChildren().clear();

        for (Artwork artwork : artworks) {
            VBox card = new VBox(10);
            card.setAlignment(Pos.TOP_CENTER);
            card.setPrefWidth(500);
            card.setStyle(
                    "-fx-border-radius: 12;" +
                            "-fx-background-radius: 12;" +
                            "-fx-border-color: #dcdcdc;" +
                            "-fx-background-color: white;" +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 4);" +
                            "-fx-padding: 15;"
            );


            ImageView imageView = new ImageView();
            imageView.setFitWidth(700);
            imageView.setFitHeight(250);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageView.setCache(true);

            Image image = null;

            try {
                if (artwork.getPicture() != null && !artwork.getPicture().trim().isEmpty()) {
                    String imagePath = "C:/xampp/htdocs/images/artwork/";

                    String imageUrl = imagePath + artwork.getPicture();

                    File file = new File(imageUrl);
                    if (file.exists()) {
                        image = new Image(file.toURI().toString());
                        imageView.setImage(image);
                    } else {
                        System.out.println("Image not found: " + imageUrl);
                    }
                }
            } catch (Exception e) {
                System.out.println("❌ Could not load image: " + artwork.getPicture());
                e.printStackTrace();

                // Provide a fallback image URL in case of error
                String fallbackUrl = "http://localhost/images/artwork/default.jpg";
                image = new Image(fallbackUrl);
            }

            if (image != null) {
                imageView.setImage(image);
            }

            imageView.setImage(image);
            Label idLabel = new Label("🆔 ID: " + artwork.getId());
            Label titleLabel = new Label("🎨 " + artwork.getName());
            Label descLabel = new Label("📝 " + artwork.getDescription());
            Label ownerLabel = new Label("👤 " + userService.getById(artwork.getUser().getId()).getFirstName());
            Label emailLabel = new Label("📧 " + userService.getById(artwork.getUser().getId()).getEmail());
            int likesCount = artworklikeservices.getLikeCount(artwork);
            Label likesLabel = new Label("👍 " + likesCount + " Likes");
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            descLabel.setWrapText(true);

            Button deleteBtn = new Button("Delete");
            deleteBtn.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white;");
            deleteBtn.setOnAction(e -> {
                try {
                    artworkService.deleteArtwork(artwork.getId());
                    refreshArtworks();
                } catch (SQLException ex) {
                    System.err.println("Error deleting: " + ex.getMessage());
                }
            });

            card.getChildren().addAll(imageView, titleLabel, descLabel, ownerLabel, emailLabel, idLabel,likesLabel, deleteBtn);
            artworkContainer.getChildren().add(card);
        }
    }
    @FXML
    private VBox chartContainer;

    public void showLikesStatsChart(ActionEvent actionEvent) {
        chartContainer.getChildren().clear();

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Artwork");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Number of Likes");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Artwork Likes");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Likes");

        try {
            Map<Integer, Integer> likesPerArtwork = artworkService.getLikesPerArtwork();

            for (Map.Entry<Integer, Integer> entry : likesPerArtwork.entrySet()) {

                Artwork artwork = artworkService.getArtworkById(entry.getKey());

                if (artwork != null) {
                    String artworkName = artwork.getName();
                    int likeCount = entry.getValue();

                    series.getData().add(new XYChart.Data<>(artworkName, likeCount));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching likes: " + e.getMessage());
        }

        barChart.getData().add(series);

        chartContainer.getChildren().add(barChart);
    }


}
