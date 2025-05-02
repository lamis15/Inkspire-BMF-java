package Controllers.Donations;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ProgressBar;
import service.DonationPredictionService;
import service.DonationService;
import entities.Donation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DonationPredictionController {

    @FXML
    private TextField collectionIdField;
    
    @FXML
    private TextField goalAmountField;
    
    @FXML
    private TextField currentAmountField;
    
    @FXML
    private TextField daysActiveField;
    
    @FXML
    private Label predictionResultLabel;
    
    @FXML
    private ProgressBar confidenceBar;
    
    @FXML
    private Button predictButton;
    
    private final DonationPredictionService predictionService = new DonationPredictionService();
    private final DonationService donationService = new DonationService();
    
    @FXML
    public void initialize() {
        // Test API connection on initialization
        new Thread(() -> {
            boolean isConnected = predictionService.testApiConnection();
            Platform.runLater(() -> {
                if (!isConnected) {
                    showAlert(Alert.AlertType.WARNING, 
                            "API Connection Issue", 
                            "Could not connect to the prediction API. Make sure it's running on localhost:5000.");
                }
            });
        }).start();
    }
    
    @FXML
    private void onPredictButtonClick(ActionEvent event) {
        try {
            // Disable button during prediction
            predictButton.setDisable(true);
            
            // Get input values
            int collectionId = Integer.parseInt(collectionIdField.getText());
            double goalAmount = Double.parseDouble(goalAmountField.getText());
            double currentAmount = Double.parseDouble(currentAmountField.getText());
            int daysActive = Integer.parseInt(daysActiveField.getText());
            
            // Get donations for training data
            List<Donation> donations;
            try {
                donations = donationService.recuperer();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, 
                        "Database Error", 
                        "Failed to retrieve donation data: " + e.getMessage());
                predictButton.setDisable(false);
                return;
            }
            
            // Make prediction
            CompletableFuture<DonationPredictionService.PredictionResult> predictionFuture = 
                    predictionService.predictDonationAmount(collectionId, goalAmount, currentAmount, daysActive, donations);
            
            // Handle prediction result
            predictionFuture.thenAccept(result -> {
                Platform.runLater(() -> {
                    // Update UI with prediction result
                    predictionResultLabel.setText(String.format("Predicted final amount: $%.2f", result.getPredictedAmount()));
                    confidenceBar.setProgress(result.getConfidence());
                    
                    // Re-enable button
                    predictButton.setDisable(false);
                });
            }).exceptionally(ex -> {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, 
                            "Prediction Error", 
                            "An error occurred during prediction: " + ex.getMessage());
                    predictButton.setDisable(false);
                });
                return null;
            });
            
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, 
                    "Input Error", 
                    "Please enter valid numeric values for all fields.");
            predictButton.setDisable(false);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, 
                    "Error", 
                    "An unexpected error occurred: " + e.getMessage());
            predictButton.setDisable(false);
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
