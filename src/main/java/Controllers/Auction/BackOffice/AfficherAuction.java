package Controllers.Auction.BackOffice;

import entities.Auction;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import service.AuctionService;
import utils.SceneSwitch;
import entities.Session;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherAuction {

    @FXML
    private FlowPane cardsContainer;

    @FXML
    private Button bidsBtn;

    private final AuctionService auctionService = new AuctionService();

    @FXML
    public void initialize() {
        bidsBtn.setOnAction(e -> {
            SceneSwitch.switchScene(cardsContainer, "/AuctionUtils/Bid/BackOffice/AfficherBid.fxml");
        });

        try {
            loadAuctions();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
    }

    private void loadAuctions() throws SQLException, IOException {
        cardsContainer.getChildren().clear();
        List<Auction> auctions = auctionService.recuperer();

        if (auctions.isEmpty()) {
            cardsContainer.getChildren().add(new Label("No auctions available."));
            return;
        }

        for (Auction auction : auctions) {
            VBox card = createCard(auction, auctionService.getArtworkById(auction.getArtworkId()).getPicture());
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

        ImageView imageView = new ImageView(new Image(imageUrl));
        imageView.setFitWidth(200);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        imageWrapper.getChildren().add(imageView);

        Label label = new Label("Label: " + auction.getLabel());
        Label startPrice = new Label("Start Price: " + auction.getStartPrice());
        Label timeLeft = new Label("Time Left: " + auctionService.getRemainingTime(auction));

        HBox buttonsBox = new HBox(10);
        buttonsBox.setStyle("-fx-alignment: center;");

        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 3 10;");
        editBtn.setOnAction(e -> {
            Session.setCurrentAuction(auction);
            SceneSwitch.switchScene(cardsContainer, "/AuctionUtils/Auction/FrontOffice/ModifierAuction.fxml");
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 3 10;");
        deleteBtn.setOnAction(e -> {
            try {
                auctionService.supprimer(auction.getId());
                loadAuctions();
            } catch (SQLException | IOException ex) {
                ex.printStackTrace();
            }
        });

        buttonsBox.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(imageWrapper, label, startPrice, timeLeft, buttonsBox);

        return card;
    }
}
