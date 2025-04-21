package Controllers.Auction;

import entities.Artwork;
import entities.Auction;
import entities.SceneManager;
import entities.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.AuctionService;


import java.util.List;

public class AjouterAuction {

    @FXML private TextField labelField;
    @FXML private DatePicker startDatePicker;

    @FXML private DatePicker endDatePicker;
    @FXML private TextField startPriceField;
    @FXML private TextField endPriceField;
    @FXML private ComboBox<Artwork> artworkComboBox;

    // Error labels
    @FXML private Label labelError;
    @FXML private Label startDateError;
    @FXML private Label endDateError;
    @FXML private Label startPriceError;
    @FXML private Label endPriceError;
    @FXML private Label artworkError;

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
            //Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            //SceneManager.switchTo(primaryStage,"/AuctionUtils/Auction/AfficherAuction.fxml");
            System.out.println("Auction added!");
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
