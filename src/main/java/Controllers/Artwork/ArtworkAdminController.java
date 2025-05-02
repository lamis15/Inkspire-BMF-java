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

            // Image
            ImageView imageView = new ImageView();
            imageView.setFitWidth(700);
            imageView.setFitHeight(250);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageView.setCache(true);

            Image image = null;
            try {
                String path = artwork.getPicture();
                if (path != null && !path.trim().isEmpty()) {
                    if (path.startsWith("http") || path.startsWith("file:/")) {
                        image = new Image(path, true);
                    } else {
                        File file = new File(path);
                        if (file.exists()) {
                            image = new Image(file.toURI().toString());
                        }
                    }
                }
                if (image == null || image.isError()) {
                    image = new Image(getClass().getResource("/images/default.jpg").toExternalForm());
                }
            } catch (Exception e) {
                image = new Image(getClass().getResource("/images/default.jpg").toExternalForm());
            }

            imageView.setImage(image);

            // Labels
            Label idLabel = new Label("🆔 ID: " + artwork.getId());
            Label titleLabel = new Label("🎨 " + artwork.getName());
            Label descLabel = new Label("📝 " + artwork.getDescription());
            Label ownerLabel = new Label("👤 " + userService.getById(artwork.getUser().getId()).getFirstName());
            Label emailLabel = new Label("📧 " + userService.getById(artwork.getUser().getId()).getEmail());
            int likesCount = artworklikeservices.getLikeCount(artwork);
            Label likesLabel = new Label("👍 " + likesCount + " Likes");
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            descLabel.setWrapText(true);

            // Delete button
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
        chartContainer.getChildren().clear(); // Clear previous chart

        // Define axes
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Artwork");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Number of Likes");

        // Create chart
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Artwork Likes");

        // Create data series
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Likes");

        try {
            // Get the likes data
            Map<Integer, Integer> likesPerArtwork = artworkService.getLikesPerArtwork();

            // Add data to series
            for (Map.Entry<Integer, Integer> entry : likesPerArtwork.entrySet()) {
                // Fetch the Artwork object by its ID (entry.getKey())
                Artwork artwork = artworkService.getArtworkById(entry.getKey());

                if (artwork != null) {
                    // Get the name of the artwork
                    String artworkName = artwork.getName();
                    // Get the like count
                    int likeCount = entry.getValue();

                    // Add the data to the series using the artwork name as the label
                    series.getData().add(new XYChart.Data<>(artworkName, likeCount));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching likes: " + e.getMessage());
        }

        barChart.getData().add(series);

        // Add chart to UI
        chartContainer.getChildren().add(barChart);
    }


}
