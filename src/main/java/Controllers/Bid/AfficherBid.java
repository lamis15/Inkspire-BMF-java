package Controllers.Bid;

import entities.Auction;
import entities.Bid;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import service.AuctionService;
import service.BidService;

import java.sql.SQLException;
import java.util.List;

public class AfficherBid {
    @FXML
    private VBox bidsContainer;

    private final BidService bidService = new BidService();
    private final AuctionService auctionService = new AuctionService();

    @FXML
    public void initialize() {
        loadAllBids(); // changed from loadMyBids
    }

    private void loadAllBids() {
        bidsContainer.getChildren().clear();

        try {
            List<Bid> allBids = bidService.recuperer(); // fetch all bids

            for (Bid bid : allBids) {
                Auction auction = auctionService.getAuctionById(bid.getAuctionId());

                VBox bidBox = new VBox(5);
                bidBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ddd; -fx-padding: 10; -fx-background-radius: 8;");

                bidBox.getChildren().addAll(
                        new Label("Auction: " + (auction != null ? auction.getLabel() : "Unknown")),
                        new Label("User ID: " + bid.getUserId()),
                        new Label("Amount: $" + bid.getAmount())
                );
                bidsContainer.getChildren().add(bidBox);
            }

            if (allBids.isEmpty()) {
                bidsContainer.getChildren().add(new Label("No bids have been placed yet."));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
