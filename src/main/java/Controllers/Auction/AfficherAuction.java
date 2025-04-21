package Controllers.Auction;

import entities.Auction;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import service.AuctionService;

import java.sql.SQLException;
import java.util.List;

public class AfficherAuction {

    @FXML
    private AnchorPane anchorPane;

    private VBox cardContainer;

    @FXML
    public void initialize() throws SQLException {
        // Buttons at top
        HBox topButtons = new HBox(10);
        topButtons.setLayoutX(20);
        topButtons.setLayoutY(10);

        Button addBtn = new Button("+ Add Auction");
        Button myAuctionsBtn = new Button("📁 My Auctions");

        // Inline styling
        addBtn.setStyle("-fx-background-color: #4e73df; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 6 12;");
        myAuctionsBtn.setStyle("-fx-background-color: #4e73df; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 6 12;");

        topButtons.getChildren().addAll(addBtn, myAuctionsBtn);
        anchorPane.getChildren().add(topButtons);

        // Card container
        cardContainer = new VBox(10);
        cardContainer.setLayoutX(20);
        cardContainer.setLayoutY(60);
        anchorPane.getChildren().add(cardContainer);

        // Load all auctions by default
        loadAuctions(false);

        // Handle button actions
        addBtn.setOnAction(e -> {
            System.out.println("Redirect to add auction...");
        });

        myAuctionsBtn.setOnAction(e -> {
            try {
                loadAuctions(true); // Load only current user's auctions
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void loadAuctions(boolean onlyMine) throws SQLException {
        cardContainer.getChildren().clear();
        AuctionService auctionService = new AuctionService();
        List<Auction> auctions = onlyMine ? auctionService.recupererMonAuctions(4) : auctionService.recuperer();

        for (Auction auction : auctions) {
            Pane card = new Pane();
            card.setPrefSize(300, 110);
            card.setStyle("""
                -fx-background-color: #fdfdfd;
                -fx-background-radius: 10;
                -fx-border-radius: 10;
                -fx-border-color: #ccc;
                -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 4, 0, 0, 2);
            """);

            Label title = new Label("🎨 " + auction.getLabel());
            title.setFont(Font.font("Arial", 16));
            title.setStyle("-fx-font-weight: bold;");
            title.setLayoutX(15);
            title.setLayoutY(10);

            Label date = new Label("📅 Start: " + auction.getStartDate());
            date.setFont(Font.font("Arial", 12));
            date.setLayoutX(15);
            date.setLayoutY(40);

            Label price = new Label("💰 $" + auction.getStartPrice());
            price.setFont(Font.font("Arial", 12));
            price.setLayoutX(15);
            price.setLayoutY(65);

            card.getChildren().addAll(title, date, price);

            if (onlyMine) {
                Button editBtn = new Button("✏️ Edit");
                Button deleteBtn = new Button("🗑️ Delete");

                editBtn.setLayoutX(190);
                editBtn.setLayoutY(30);
                deleteBtn.setLayoutX(250);
                deleteBtn.setLayoutY(30);

                // Inline style for small buttons
                editBtn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333; -fx-background-radius: 6; -fx-padding: 3 8; -fx-font-size: 11;");
                deleteBtn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333; -fx-background-radius: 6; -fx-padding: 3 8; -fx-font-size: 11;");

                editBtn.setOnAction(e -> {
                    System.out.println("Edit auction ID: " + auction.getId());
                });

                deleteBtn.setOnAction(e -> {
                    System.out.println("Delete auction ID: " + auction.getId());
                });

                card.getChildren().addAll(editBtn, deleteBtn);
            }

            cardContainer.getChildren().add(card);
        }
    }
}