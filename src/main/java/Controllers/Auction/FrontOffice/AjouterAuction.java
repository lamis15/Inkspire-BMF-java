package Controllers.Auction.FrontOffice;

import entities.Artwork;
import entities.Auction;
import entities.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import service.AuctionService;


import java.util.List;

public class AjouterAuction {
    @FXML private TextField labelField, startPriceField, endPriceField;
    @FXML private DatePicker startDatePicker, endDatePicker;
    @FXML private ComboBox<Artwork> artworkComboBox;
    @FXML private Label labelError, startDateError, endDateError, startPriceError, endPriceError, artworkError;
    private final AuctionService auctionService = new AuctionService();

    @FXML
    public void initialize() {
        try {
            int userId = Session.getCurrentUser().getId();
            List<Artwork> availableArtworks = auctionService.getAvailableArtworksByUser(userId);
            artworkComboBox.getItems().addAll(availableArtworks);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void onAddAuction(ActionEvent event) {
        clearErrorLabels();

        boolean isValid = true;

        if (labelField.getText().isEmpty()) {
            labelError.setText("Label is required.");
            isValid = false;
        }
        if (startDatePicker.getValue() == null) {
            startDateError.setText("Start date is required.");
            isValid = false;
        }

        if (endDatePicker.getValue() == null) {
            endDateError.setText("End date is required.");
            isValid = false;
        }
        if (endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
            endDateError.setText("End date must be after start date.");
            isValid = false;
        }
        if (startPriceField.getText().isEmpty()) {
            startPriceError.setText("Start price is required.");
            isValid = false;
        }

        if (endPriceField.getText().isEmpty()) {
            endPriceError.setText("End price is required.");
            isValid = false;
        }

        Artwork selectedArtwork = artworkComboBox.getValue();
        if (selectedArtwork == null) {
            artworkError.setText("Please select an artwork.");
            isValid = false;
        }

        if (!isValid) return;

        try {
            Auction auction = new Auction(
                    labelField.getText(),
                    startDatePicker.getValue().toString(),
                    endDatePicker.getValue().toString(),
                    Double.parseDouble(startPriceField.getText()),
                    Double.parseDouble(endPriceField.getText()),
                    "ongoing",
                    selectedArtwork.getId()
            );
            auctionService.ajouter(auction);
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Auction Added");
            successAlert.setHeaderText("Auction has been successfully added.");
            successAlert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearErrorLabels() {
        labelError.setText("");
        startDateError.setText("");
        endDateError.setText("");
        startPriceError.setText("");
        endPriceError.setText("");
        artworkError.setText("");
    }
}
