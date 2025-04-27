package Controllers.Auction;

import Controllers.Bid.AjouterBid;
import entities.Artwork;
import entities.Auction;
import entities.Session;
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
import utils.SceneSwitch;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherAuction {

    @FXML
    private FlowPane cardsContainer;

    @FXML
    private Button addBtn;

    @FXML
    private Button myAuctionsBtn;

    @FXML
    private Button myBidsBtn;

    private final AuctionService auctionService = new AuctionService();

    @FXML
    public void initialize() {
        try {
            loadAuctions(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        addBtn.setOnAction(e -> {
            SceneSwitch.switchScene(cardsContainer, "/AuctionUtils/Auction/AjouterAuction.fxml");
        });

        myAuctionsBtn.setOnAction(e -> {
            try {
                loadAuctions(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        myBidsBtn.setOnAction(e -> {
            SceneSwitch.switchScene(cardsContainer, "/AuctionUtils/Bid/AfficherBid.fxml");
        });
    }

    private void loadAuctions(boolean onlyMine) throws SQLException {
        cardsContainer.getChildren().clear();

        int currentUserId = Session.getCurrentUser().getId();
        List<Auction> auctions = onlyMine ?
                auctionService.recupererMonAuctions(currentUserId) :
                auctionService.recuperer();

        for (Auction auction : auctions) {
            VBox card = createAuctionCard(auction, onlyMine);
            cardsContainer.getChildren().add(card);
        }
    }

    private VBox createAuctionCard(Auction auction, boolean showActions) throws SQLException {
        VBox card = new VBox();
        card.setSpacing(10);
        card.setPrefWidth(250);
        card.setStyle("""
            -fx-background-color: #fdfdfd;
            -fx-background-radius: 12;
            -fx-padding: 15;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 3);
        """);

        // Create ImageView for this specific card
        ImageView artworkImageView = new ImageView();
        artworkImageView.setFitWidth(200);
        artworkImageView.setFitHeight(200);
        artworkImageView.setPreserveRatio(true);
        artworkImageView.setSmooth(true);

        try {
            Artwork artwork = auctionService.getArtworkById(auction.getArtworkId());
            String imagePath = artwork.getPicture();
            System.out.println("Loading image from: " + imagePath);

            if (imagePath != null && !imagePath.isEmpty()) {
                // Handle file: prefix if present
                if (imagePath.startsWith("file:")) {
                    imagePath = imagePath.substring(5);
                }

                File file = new File(imagePath);
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString(), true);
                    image.errorProperty().addListener((obs, wasError, isError) -> {
                        if (isError) {
                            System.err.println("Failed to load image: " + file.getAbsolutePath());
                        }
                    });
                    artworkImageView.setImage(image);
                } else {
                    System.err.println("File not found: " + file.getAbsolutePath());
                }
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Create labels
        Label titleLabel = new Label("label " + auction.getLabel());
        titleLabel.setFont(Font.font(16));
        titleLabel.setStyle("-fx-font-weight: bold;");

        Label dateLabel = new Label("date " + auction.getStartDate());
        dateLabel.setStyle("-fx-text-fill: #666;");

        Label priceLabel = new Label("start price $" + auction.getStartPrice());
        priceLabel.setStyle("-fx-text-fill: #444;");

        // Add all components to card
        card.getChildren().addAll(artworkImageView, titleLabel, dateLabel, priceLabel);

        // Add action buttons if needed
        if (showActions) {
            HBox actionBox = new HBox(10);
            actionBox.setAlignment(Pos.CENTER);

            Button editBtn = new Button("✏️ Edit");
            Button deleteBtn = new Button("🗑️ Delete");

            // Style buttons
            editBtn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333; -fx-background-radius: 6; -fx-padding: 3 10;");
            deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 3 10;");

            // Button actions
            editBtn.setOnAction(e -> handleEditAuction(auction));
            deleteBtn.setOnAction(e -> {
                try {
                    auctionService.supprimer(auction.getId());
                    loadAuctions(true);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });
            actionBox.getChildren().addAll(editBtn, deleteBtn);
            card.getChildren().add(actionBox);
        }
        else {
            // Add "Place a bid" button for non-owner users
            Button placeBidBtn = new Button("💰 Place a Bid");
            placeBidBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 3 10;");

            placeBidBtn.setOnAction(e -> handlePlaceBid(auction));
            card.getChildren().add(placeBidBtn);
        }

        return card;
    }
    private void handlePlaceBid(Auction auction) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AuctionUtils/Bid/AjouterBid.fxml"));
            Parent root = loader.load();

            AjouterBid controller = loader.getController();
            controller.setAuction(auction);  // Pass the auction to the bid controller

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Place a Bid on Auction #" + auction.getId());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                    loadAuctions(true);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}