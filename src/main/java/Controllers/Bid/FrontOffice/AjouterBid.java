package Controllers.Bid.FrontOffice;

import com.fasterxml.jackson.databind.JsonNode;
import com.stripe.exception.StripeException;
import entities.Auction;
import entities.Bid;
import entities.Session;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import service.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AjouterBid {

    @FXML private TextField bidAmountField;
    @FXML private CheckBox usdCheckBox;
    @FXML private CheckBox eurCheckBox;
    @FXML private Label checkboxerror;
    @FXML private Label amounterror;
    @FXML private ComboBox<String> fromcurrencyComboBox;
    @FXML private ComboBox<String> tocurrencyComboBox;
    @FXML private TextField fromoutput;
    @FXML private TextField toomoutput;

    private final BidService bidService = new BidService();
    private final AuctionService auctionService = new AuctionService();

    @FXML
    public void initialize() {
        List<String> supportedCurrencies = List.of(
                "USD", "EUR", "GBP", "JPY", "CAD", "AUD", "CHF", "CNY", "SEK", "NZD"
        );
        usdCheckBox.setOnAction(event -> {
            if (usdCheckBox.isSelected()) {
                eurCheckBox.setSelected(false);
            }
        });

        eurCheckBox.setOnAction(event -> {
            if (eurCheckBox.isSelected()) {
                usdCheckBox.setSelected(false);
            }
        });

        fromcurrencyComboBox.getItems().addAll(supportedCurrencies);
        tocurrencyComboBox.getItems().addAll(supportedCurrencies);
        fromcurrencyComboBox.setValue("USD");
        tocurrencyComboBox.setValue("EUR");

    }

    @FXML
    public void handlePlaceBid() {
        // Clear previous errors
        checkboxerror.setVisible(false);
        amounterror.setVisible(false);

        try {
            Auction auction = Session.getCurrentAuction();
            String bidText = bidAmountField.getText().trim();

            if (bidText.isEmpty()) {
                amounterror.setText("Bid Amount cannot be empty");
                amounterror.setVisible(true);
                return;
            }

            double bidAmount = Double.parseDouble(bidText);

            if (bidAmount <= auction.getEndPrice()) {
                amounterror.setText("Bid value should be greater than Auction Highest Bid: " + auction.getEndPrice() + " $");
                amounterror.setVisible(true);
                return;
            }

            String currency = null;
            if (usdCheckBox.isSelected()) {
                currency = "usd";
            } else if (eurCheckBox.isSelected()) {
                currency = "eur";
            } else {
                checkboxerror.setText("You need to select a currency.");
                checkboxerror.setVisible(true);
                return;
            }
            IPGeolocationService geoService = new IPGeolocationService();
            JsonNode geo = geoService.getGeoData();
            Bid bid = new Bid();
            bid.setAmount(bidAmount);
            bid.setTime(null);
            bid.setLocation(geo.path("city").asText());
            bid.setAuctionId(auction.getId());
            bid.setUserId(Session.getCurrentUser().getId());
            bidService.ajouter(bid);
            auction.setEndPrice(bidAmount);
            auctionService.modifier(auction);

            long amountInCents = (long) (bidAmount * 100);
            String description = "Pay " + bidAmount + " " + currency + " for " + auction.getLabel();
            PaymentService paymentService = new PaymentService();
            String url = paymentService.createCheckoutSession(amountInCents, currency, description);
            paymentService.openInBrowser(url);

            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Bid Added");
            successAlert.setHeaderText("Bid has been successfully added.");
            successAlert.showAndWait();

        } catch (NumberFormatException ex) {
            amounterror.setText("Please enter a valid number.");
            amounterror.setVisible(true);
        } catch (SQLException | StripeException e) {
            showAlert("Error", "Something went wrong: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    @FXML
    public void handleExchange(){
        try {
            String fromCurrency = fromcurrencyComboBox.getValue();
            String toCurrency = tocurrencyComboBox.getValue();
            String amountStr = fromoutput.getText();

            if (fromCurrency == null || toCurrency == null || amountStr.isEmpty()) {
                System.out.println("missing output");
                return;
            }

            double amount = Double.parseDouble(amountStr);

            ExchangeRateService service = new ExchangeRateService();
            double converted = service.convert(amount, fromCurrency, toCurrency);

            toomoutput.setText(String.format("%.2f", converted));

        } catch (Exception e) {
            toomoutput.setText("Error");
            e.printStackTrace();
        }
    }
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
