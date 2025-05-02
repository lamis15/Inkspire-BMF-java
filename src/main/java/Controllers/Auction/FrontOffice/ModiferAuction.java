package Controllers.Auction.FrontOffice;

import entities.Artwork;
import entities.Auction;
import entities.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import service.AuctionService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ModiferAuction {

    @FXML private TextField labelField, startPriceField, endPriceField;
    @FXML private DatePicker startDatePicker, endDatePicker;
    @FXML private ComboBox<Artwork> artworkComboBox;
    @FXML private Label labelError, startDateError, endDateError, startPriceError, endPriceError, artworkError;
    private final AuctionService auctionService = new AuctionService();

    @FXML
    public void initialize() {
        Auction auction = Session.getCurrentAuction();
        if (auction == null) {
            System.out.println("No auction found in the session.");
            return;
        }

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

    public void onUpdateAuction() {
        clearErrors();

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

        if (startDatePicker.getValue() != null && endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
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
        if(selectedArtwork.getId() == null){
            selectedArtwork.setId(Session.getCurrentAuction().getArtworkId());
        }
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
            auction.setId(Session.getCurrentAuction().getId());
            auctionService.modifier(auction);
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Auction Updated");
            successAlert.setHeaderText("Auction has been successfully updated.");
            successAlert.showAndWait();

        } catch (NumberFormatException e) {
            startPriceError.setText("Invalid price format.");
            endPriceError.setText("Invalid price format.");
        } catch (Exception e) {
            e.printStackTrace();
        }
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