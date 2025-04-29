package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import entities.Donation;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for predicting donation amounts using the Python prediction API
 */
public class DonationPredictionService {
    private static final Logger LOGGER = Logger.getLogger(DonationPredictionService.class.getName());
    private static final String API_BASE_URL = "http://localhost:5000";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public DonationPredictionService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Test if the prediction API is available
     * @return true if the API is available, false otherwise
     */
    public boolean testApiConnection() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/test"))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to connect to prediction API: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Predict the final donation amount for a collection
     * 
     * @param collectionId The ID of the collection
     * @param goalAmount The goal amount for the collection
     * @param currentAmount The current amount collected
     * @param daysActive Number of days the collection has been active
     * @param donations List of previous donations for training data
     * @return CompletableFuture with the predicted amount and confidence
     */
    public CompletableFuture<PredictionResult> predictDonationAmount(
            int collectionId, 
            double goalAmount, 
            double currentAmount, 
            int daysActive,
            List<Donation> donations) {
        
        try {
            // Create the request body
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("collection_id", collectionId);
            requestBody.put("goal_amount", goalAmount);
            requestBody.put("current_amount", currentAmount);
            requestBody.put("days_active", daysActive);
            requestBody.put("num_donations", donations.size());
            
            // Create training data from historical donations
            ArrayNode trainingData = objectMapper.createArrayNode();
            // Here you would add historical collection data
            // This is just a placeholder - you would need to adapt this to your actual data model
            for (int i = 0; i < Math.min(10, donations.size()); i++) {
                Donation donation = donations.get(i);
                ObjectNode feature = objectMapper.createObjectNode();
                feature.put("id", i);
                feature.put("goal_amount", goalAmount);
                feature.put("current_amount", donation.getAmount());
                feature.put("ratio", donation.getAmount() / goalAmount);
                trainingData.add(feature);
            }
            
            requestBody.set("training_data", trainingData);
            requestBody.put("force_refresh", false);
            
            // Create the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/predict"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();
            
            // Send the request asynchronously
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() != 200) {
                            LOGGER.warning("API returned non-200 status code: " + response.statusCode());
                            return new PredictionResult(currentAmount, 0.5);
                        }
                        
                        try {
                            ObjectNode responseJson = (ObjectNode) objectMapper.readTree(response.body());
                            double predictedAmount = responseJson.get("predicted_amount").asDouble();
                            double confidence = responseJson.get("confidence").asDouble();
                            return new PredictionResult(predictedAmount, confidence);
                        } catch (Exception e) {
                            LOGGER.log(Level.WARNING, "Failed to parse prediction response: " + e.getMessage(), e);
                            return new PredictionResult(currentAmount, 0.5);
                        }
                    })
                    .exceptionally(e -> {
                        LOGGER.log(Level.WARNING, "Prediction request failed: " + e.getMessage(), e);
                        return new PredictionResult(currentAmount, 0.5);
                    });
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to create prediction request: " + e.getMessage(), e);
            CompletableFuture<PredictionResult> future = new CompletableFuture<>();
            future.complete(new PredictionResult(currentAmount, 0.5));
            return future;
        }
    }
    
    /**
     * Simple class to hold prediction results
     */
    public static class PredictionResult {
        private final double predictedAmount;
        private final double confidence;
        
        public PredictionResult(double predictedAmount, double confidence) {
            this.predictedAmount = predictedAmount;
            this.confidence = confidence;
        }
        
        public double getPredictedAmount() {
            return predictedAmount;
        }
        
        public double getConfidence() {
            return confidence;
        }
        
        @Override
        public String toString() {
            return String.format("Predicted amount: %.2f (confidence: %.2f%%)", 
                    predictedAmount, confidence * 100);
        }
    }
}
