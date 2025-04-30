package Controllers.Auction;

import entities.Artwork;
import entities.Auction;
import entities.Session;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import service.AuctionService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ModiferAuction {
    @FXML private TextField labelField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField startPriceField;
    @FXML private TextField endPriceField;
    @FXML private ComboBox<Artwork> artworkComboBox;
    @FXML private Button updateButton;

    // Error labels
    @FXML private Label labelError;
    @FXML private Label startDateError;
    @FXML private Label endDateError;
    @FXML private Label startPriceError;
    @FXML private Label endPriceError;
    @FXML private Label artworkError;

    private final AuctionService auctionService = new AuctionService();
    private Auction currentAuction;

    public void setAuction(Auction auction) {
        this.currentAuction = auction;

        // Pre-fill fields
        labelField.setText(auction.getLabel());
        startDatePicker.setValue(LocalDate.parse(auction.getStartDate().substring(0, 10)));
        endDatePicker.setValue(LocalDate.parse(auction.getEndDate().substring(0, 10)));
        startPriceField.setText(String.valueOf(auction.getStartPrice()));
        endPriceField.setText(String.valueOf(auction.getEndPrice()));

        try {
            int userId = Session.getCurrentUser().getId();
            List<Artwork> artworks = auctionService.getAvailableArtworksByUser(userId);
            artworkComboBox.getItems().addAll(artworks);

            Artwork current = auctionService.getArtworkById(auction.getArtworkId());
            if (current != null) {
                artworkComboBox.getSelectionModel().select(current);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize() {
        updateButton.setOnAction(e -> {
            clearErrors();

            boolean isValid = true;
            String label = labelField.getText();
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            String startPriceText = startPriceField.getText();
            String endPriceText = endPriceField.getText();
            Artwork selectedArtwork = artworkComboBox.getValue();

            if (label == null || label.isEmpty()) {
                labelError.setText("Label is required.");
                isValid = false;
            }

            if (startDate == null) {
                startDateError.setText("Start date is required.");
                isValid = false;
            }

            if (endDate == null) {
                endDateError.setText("End date is required.");
                isValid = false;
            }
            if (endDate.isBefore(startDate)) {
                endDateError.setText("End date cannot be before start date.");
                isValid = false;
            }

            double startPrice = 0, endPrice = 0;
            try {
                startPrice = Double.parseDouble(startPriceText);
            } catch (NumberFormatException ex) {
                startPriceError.setText("Invalid start price.");
                isValid = false;
            }

            try {
                endPrice = Double.parseDouble(endPriceText);
            } catch (NumberFormatException ex) {
                endPriceError.setText("Invalid end price.");
                isValid = false;
            }

            if (selectedArtwork == null) {
                artworkError.setText("Please select an artwork.");
                isValid = false;
            }

            if (!isValid) return;

            try {
                currentAuction.setLabel(label);
                currentAuction.setStartDate(String.valueOf(startDate));
                currentAuction.setEndDate(String.valueOf(endDate));
                currentAuction.setStartPrice(startPrice);
                currentAuction.setEndPrice(endPrice);
                currentAuction.setArtworkId(selectedArtwork.getId());

                auctionService.modifier(currentAuction);

                Stage stage = (Stage) updateButton.getScene().getWindow();
                stage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void clearErrors() {
        labelError.setText("");
        startDateError.setText("");
        endDateError.setText("");
        startPriceError.setText("");
        endPriceError.setText("");
        artworkError.setText("");
    }
}
