package Controllers.Auction.FrontOffice;

import entities.Auction;
import entities.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import service.AuctionService;
import utils.SceneSwitch;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class AfficherAuction {

    @FXML
    private FlowPane cardsContainer;

    @FXML
    private Button addBtn, myAuctionsBtn, myBidsBtn;

    @FXML
    private TextField searchTextField;

    private final AuctionService auctionService = new AuctionService();

    private List<Auction> allAuctions ;

    @FXML
    public void initialize() throws SQLException {

        addBtn.setOnAction(e -> {
            SceneSwitch.switchScene(cardsContainer, "/AuctionUtils/Auction/FrontOffice/AjouterAuction.fxml");
        });

        myBidsBtn.setOnAction(e -> {
            SceneSwitch.switchScene(cardsContainer, "/AuctionUtils/Bid/FrontOffice/AfficherBid.fxml");
        });
        myAuctionsBtn.setOnAction(e -> {
            try {
                loadAuctions(true);
            } catch (SQLException | IOException ex) {
                ex.printStackTrace();
            }
        });
        allAuctions = auctionService.recuperer();
        searchTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (allAuctions != null) {
                String query = newVal.toLowerCase();
                List<Auction> filtered = allAuctions.stream()
                        .filter(a -> a.getLabel().toLowerCase().contains(query))
                        .collect(Collectors.toList());
                try {
                    displayAuctions(filtered);
                } catch (IOException | SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
        try {
            loadAuctions(false);
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
    }
    private void loadAuctions(boolean onlyMine) throws SQLException, IOException {
        cardsContainer.getChildren().clear();

        int currentUserId = Session.getCurrentUser().getId();
        List<Auction> auctions = onlyMine ?
                auctionService.recupererMonAuctions(currentUserId) :
                auctionService.recuperer();

        System.out.println(auctions.size());
        for (Auction auction : auctions) {
            VBox card = null;
            try {
                String imageUrl = auctionService.getArtworkById(auction.getArtworkId()).getPicture();
                System.out.println("Image URL: " + imageUrl);
                String imagePath =  "C:/xampp/htdocs/" + imageUrl;
                System.out.println("Image Path: " + imagePath);
                card = createCard(auction,imagePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            cardsContainer.getChildren().add(card);
            if (auctions.isEmpty()) {
                cardsContainer.getChildren().add(new Label("No Auctions have been posted yet."));
            }
        }
    }
    private void displayAuctions(List<Auction> auctions) throws IOException, SQLException {
        cardsContainer.getChildren().clear();

        if (auctions.isEmpty()) {
            cardsContainer.getChildren().add(new Label("No Auctions found."));
            return;
        }

        for (Auction auction : auctions) {
            String imageUrl = auctionService.getArtworkById(auction.getArtworkId()).getPicture();
            VBox card = createCard(auction, imageUrl);
            cardsContainer.getChildren().add(card);
        }
    }
    private VBox createCard(Auction auction, String imageUrl) throws IOException, SQLException {
        VBox card = new VBox();
        card.setPrefSize(200, 300);
        card.setSpacing(10);
        card.setStyle("-fx-background-color: #fdfdfd; -fx-background-radius: 12; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 3);");


        StackPane imageWrapper = new StackPane();
        imageWrapper.setPrefSize(200, 150);
        imageWrapper.setMaxSize(200, 150);
        imageWrapper.setMinSize(200, 150);
        File file = new File(imageUrl);
        Image image = null;
        if (file.exists()) {
            image = new Image(file.toURI().toString());
        } else {
            System.out.println("Image not found: " + imageUrl);
        }
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(200);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        imageWrapper.getChildren().add(imageView);

        Label nameLabel = new Label("label: " + auction.getLabel());
        Label bidLabel = new Label("Highest Bid: " + auction.getEndPrice());
        Label timeLeftLabel = new Label("Time Left: " + auctionService.getRemainingTime(auction));

        HBox buttonsBox = new HBox(10);
        buttonsBox.setPrefWidth(200);
        buttonsBox.setStyle("-fx-alignment: center;");

        if (auctionService.isMine(auction)) {
            Button editBtn = new Button("Edit");
            editBtn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333; -fx-background-radius: 6; -fx-padding: 3 10;");
            editBtn.setOnAction(e -> {
                Session.setCurrentAuction(auction);
                SceneSwitch.switchScene(cardsContainer, "/AuctionUtils/Auction/FrontOffice/ModifierAuction.fxml");
            });
            Button deleteBtn = new Button("Delete");
            deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 3 10;");
            deleteBtn.setOnAction(e -> {
                try {
                    auctionService.supprimer(auction.getId());
                    loadAuctions(false);
                } catch (SQLException | IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
            Button chartBtn = new Button("details");
            chartBtn.setStyle("-fx-background-color: #7c7c7c; -fx-text-fill: #333; -fx-background-radius: 6; -fx-padding: 3 10;");
            chartBtn.setOnAction(e -> {
                Session.setCurrentAuction(auction);
                SceneSwitch.switchScene(cardsContainer, "/AuctionUtils/Auction/FrontOffice/AfficherAuctionDetail.fxml");
            });
            buttonsBox.getChildren().addAll(editBtn, deleteBtn, chartBtn);
        } else {
            Button placeBidBtn = new Button("Place Bid");
            placeBidBtn.setStyle("-fx-background-color: #50c878; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 3 10;");
            placeBidBtn.setOnAction(e -> {
                Session.setCurrentAuction(auction);
                SceneSwitch.switchScene(cardsContainer, "/AuctionUtils/Bid/FrontOffice/AjouterBid.fxml");
            });
            buttonsBox.getChildren().addAll(placeBidBtn);
        }
        card.getChildren().addAll(imageWrapper, nameLabel, bidLabel, timeLeftLabel, buttonsBox);
        return card;
    }
}