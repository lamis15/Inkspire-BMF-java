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

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherAuction {

    @FXML
    private FlowPane cardsContainer;

    @FXML
    private Button bidsBtn;

    private final AuctionService auctionService = new AuctionService();
    
    // Define image path constant
    private static final String ARTWORK_IMAGE_PATH = "C:/xampp/htdocs/images/artwork/";

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

    private VBox createCard(Auction auction, String imageFilename) throws IOException, SQLException {
        VBox card = new VBox();
        card.setPrefSize(200, 300);
        card.setSpacing(10);
        card.setStyle("-fx-background-color: #fdfdfd; -fx-background-radius: 12; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 3);");

        StackPane imageWrapper = new StackPane();
        imageWrapper.setPrefSize(200, 150);

        // Handle artwork image loading properly
        ImageView imageView = new ImageView();
        imageView.setFitWidth(200);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        
        if (imageFilename != null && !imageFilename.isEmpty()) {
            try {
                // Create full path to the image
                String imagePath = ARTWORK_IMAGE_PATH + imageFilename;
                File imageFile = new File(imagePath);
                
                if (imageFile.exists()) {
                    // Load image from file system
                    Image image = new Image(imageFile.toURI().toString());
                    imageView.setImage(image);
                } else {
                    System.out.println("Artwork image not found: " + imagePath);
                    // Use a placeholder image
                    Image placeholder = new Image(getClass().getResourceAsStream("/assets/images/placeholder.png"));
                    imageView.setImage(placeholder);
                }
            } catch (Exception e) {
                System.out.println("Error loading image: " + e.getMessage());
                // Use a placeholder on error
                try {
                    Image placeholder = new Image(getClass().getResourceAsStream("/assets/images/placeholder.png"));
                    imageView.setImage(placeholder);
                } catch (Exception ex) {
                    System.out.println("Could not load placeholder image: " + ex.getMessage());
                }
            }
        } else {
            // Use placeholder for null/empty image path
            try {
                Image placeholder = new Image(getClass().getResourceAsStream("/assets/images/placeholder.png"));
                imageView.setImage(placeholder);
            } catch (Exception e) {
                System.out.println("Could not load placeholder image: " + e.getMessage());
            }
        }
        
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
