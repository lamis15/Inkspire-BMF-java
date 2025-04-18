package Controllers.Auction;

import entities.Artwork;
import entities.Auction;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import service.AuctionService;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherAuctionAdmin {

    @FXML
    private FlowPane cardsContainer;

    @FXML
    private Button addBtn;

    @FXML
    private Button bidsBtn;

    private final AuctionService auctionService = new AuctionService();

    @FXML
    public void initialize() {
        try {
            loadAuctions();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        bidsBtn.setOnAction(e -> switchScene("/AuctionUtils/Bid/AfficherBidAdmin.fxml"));
    }

    private void loadAuctions() throws SQLException {
        cardsContainer.getChildren().clear();
        List<Auction> auctions = auctionService.recuperer();

        for (Auction auction : auctions) {
            VBox card = createAuctionCard(auction);
            cardsContainer.getChildren().add(card);
        }
    }

    private VBox createAuctionCard(Auction auction) throws SQLException {
        VBox card = new VBox(10);
        card.setPrefWidth(250);
        card.setStyle("""
            -fx-background-color: #fdfdfd;
            -fx-background-radius: 12;
            -fx-padding: 15;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 3);
        """);

        ImageView artworkImageView = new ImageView();
        artworkImageView.setFitWidth(200);
        artworkImageView.setFitHeight(200);
        artworkImageView.setPreserveRatio(true);

        try {
            Artwork artwork = auctionService.getArtworkById(auction.getArtworkId());
            String imagePath = artwork.getPicture();

            if (imagePath != null && !imagePath.isEmpty()) {
                if (imagePath.startsWith("file:")) imagePath = imagePath.substring(5);

                File file = new File(imagePath);
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString(), true);
                    artworkImageView.setImage(image);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Label titleLabel = new Label("label " + auction.getLabel());
        titleLabel.setFont(Font.font(16));
        titleLabel.setStyle("-fx-font-weight: bold;");

        Label dateLabel = new Label("date " + auction.getStartDate());
        dateLabel.setStyle("-fx-text-fill: #666;");

        Label priceLabel = new Label("start price $" + auction.getStartPrice());
        priceLabel.setStyle("-fx-text-fill: #444;");

        Button editBtn = new Button("✏️ Edit");
        Button deleteBtn = new Button("🗑️ Delete");

        editBtn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333; -fx-background-radius: 6;");
        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 6;");

        editBtn.setOnAction(e -> handleEditAuction(auction));
        deleteBtn.setOnAction(e -> {
            try {
                auctionService.supprimer(auction.getId());
                loadAuctions();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        HBox actionBox = new HBox(10, editBtn, deleteBtn);
        actionBox.setAlignment(Pos.CENTER);

        card.getChildren().addAll(artworkImageView, titleLabel, dateLabel, priceLabel, actionBox);

        return card;
    }

    private void handleEditAuction(Auction auction) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AuctionUtils/Auction/ModifierAuction.fxml"));
            Parent root = loader.load();

            ModiferAuction controller = loader.getController();
            controller.setAuction(auction);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setOnHidden(e -> {
                try {
                    loadAuctions();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void switchScene(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
