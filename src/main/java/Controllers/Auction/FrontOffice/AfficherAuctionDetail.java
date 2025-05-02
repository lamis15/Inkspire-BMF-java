package Controllers.Auction.FrontOffice;

import entities.Auction;
import entities.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import service.BidService;

import java.sql.SQLException;
import java.util.Map;

public class AfficherAuctionDetail {

    @FXML
    private PieChart biddersByCountryChart;

    @FXML
    public void initialize() {
        try {
            Auction currentAuction = Session.getCurrentAuction();
            if (currentAuction != null) {
                loadChartData(currentAuction);
            } else {
                System.err.println("No auction found in session.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void loadChartData(Auction auction) throws SQLException {
        BidService bidService = new BidService();
        Map<String, Integer> stats = bidService.countBiddersPerCountry(auction);

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            pieData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
        biddersByCountryChart.setData(pieData);
        biddersByCountryChart.setTitle("Bidders by Country");
    }
}
