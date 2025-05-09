package Controllers.Bid.BackOffice;
import entities.Auction;
import entities.Bid;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
        loadAllBids();
    }

    private void loadAllBids() {
        bidsContainer.getChildren().clear();

        try {
            List<Bid> allBids = bidService.recuperer();

            for (Bid bid : allBids) {
                Auction auction = auctionService.getAuctionById(bid.getAuctionId());

                VBox bidBox = new VBox(5);
                bidBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ddd; -fx-padding: 10; -fx-background-radius: 8;");

                bidBox.getChildren().addAll(
                        new Label("Auction: " + (auction != null ? auction.getLabel() : "Unknown")),
                        new Label("User ID: " + bid.getUserId()),
                        new Label("Amount: $" + bid.getAmount())
                );
                Button deleteBtn = new Button("Delete");
                deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 3 10;");
                deleteBtn.setOnAction(e -> {
                    try {
                        bidService.supprimer(bid.getId());
                        if(bidService.returnHighestBid(auction) == null) {
                            auction.setEndPrice(auction.getStartPrice());
                            auctionService.modifier(auction);
                        }
                        else {
                            auction.setEndPrice(bidService.returnHighestBid(auction).getAmount());
                            auctionService.modifier(auction);
                        }
                        this.loadAllBids();
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                });
                bidBox.getChildren().add(deleteBtn);
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
