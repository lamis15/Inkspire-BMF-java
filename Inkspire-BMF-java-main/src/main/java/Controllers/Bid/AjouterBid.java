package Controllers.Bid;

import entities.Auction;
import entities.Bid;  // Ensure the Bid entity is imported
import entities.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.AuctionService;
import service.BidService;

import java.sql.SQLException;

public class AjouterBid {

    @FXML private TextField bidAmountField;
    @FXML private Button placeBidButton;

    private Auction auction;
    private final AuctionService auctionService = new AuctionService();
    private final BidService bidService = new BidService();

    public void setAuction(Auction auction) {
        this.auction = auction;
    }

    @FXML
    public void handlePlaceBid() {
        try {
            String bidText = bidAmountField.getText();
            if (!bidText.isEmpty()) {
                double bidAmount = Double.parseDouble(bidText);
                if (bidAmount > auction.getStartPrice()) {
                    Bid bid = new Bid();
                    bid.setAmount(bidAmount);
                    bid.setTime(null);
                    bid.setAuctionId(auction.getId());
                    bid.setUserId(Session.getCurrentUser().getId());

                    bidService.ajouter(bid);
                    Stage stage = (Stage) placeBidButton.getScene().getWindow();
                    stage.close();
                } else {
                    System.out.println("Bid must be greater than the starting price.");
                }
            }
        } catch (NumberFormatException ex) {
            System.out.println("Invalid bid amount. Please enter a valid number.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
