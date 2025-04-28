package service;

import javafx.scene.image.Image;
import okhttp3.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for generating art using Pollinations.AI API
 */
public class ArtGenerationService {
    private static final Logger LOGGER = Logger.getLogger(ArtGenerationService.class.getName());
    // Use a specific model for more reliable results (sdxl is Stable Diffusion XL)
    private static final String IMAGE_API_URL = "https://image.pollinations.ai/prompt/";
    private static final String DEFAULT_MODEL = "sdxl";
    private static final int DEFAULT_WIDTH = 1024;
    private static final int DEFAULT_HEIGHT = 768;
    private static final String TEXT_API_URL = "https://text.pollinations.ai/";
    private static final String MODELS_API_URL = "https://image.pollinations.ai/models";
    
    private final OkHttpClient httpClient;
    private final String imageStoragePath;

    public ArtGenerationService(String apiKey) {
        // Note: Pollinations.AI doesn't require an API key
        this.httpClient = new OkHttpClient.Builder()
                .followRedirects(true)
                .build();
        
        // Create images directory if it doesn't exist
        this.imageStoragePath = "src/main/resources/generated_art/";
        File directory = new File(imageStoragePath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            LOGGER.info("Created image directory: " + created);
        }
    }

    /**
     * Generate art based on a text prompt
     * @param prompt The text description of what to generate
     * @param style Optional style (e.g., "digital art", "oil painting")
     * @return CompletableFuture with the path to the generated image
     */
    public CompletableFuture<String> generateArt(String prompt, String style) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String fullPrompt = style != null && !style.isEmpty() 
                    ? String.format("%s of %s", style, prompt)
                    : prompt;
                
                LOGGER.info("Generating art with prompt: " + fullPrompt);
                
                // URL encode the prompt
                String encodedPrompt = URLEncoder.encode(fullPrompt, StandardCharsets.UTF_8.toString());
                // Add specific model parameter for better results and set dimensions
                String imageUrl = IMAGE_API_URL + encodedPrompt + 
                    "?model=" + DEFAULT_MODEL + 
                    "&width=" + DEFAULT_WIDTH + 
                    "&height=" + DEFAULT_HEIGHT;
                
                LOGGER.info("Using Pollinations.AI URL: " + imageUrl);
                
                // Create the request
                Request request = new Request.Builder()
                        .url(imageUrl)
                        .header("User-Agent", "JavaFX-Client")
                        .get()
                        .build();
                
                // Execute the request
                Response response = httpClient.newCall(request).execute();
                
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    LOGGER.log(Level.SEVERE, "API error: " + errorBody);
                    throw new RuntimeException("Failed to generate art: " + errorBody + " (Status code: " + response.code() + ")");
                }
                
                // Log response headers
                LOGGER.info("Response headers: " + response.headers());
                
                // Save the image
                String fileName = "pollinations_art_" + UUID.randomUUID().toString() + ".png";
                String filePath = imageStoragePath + fileName;
                
                // Save image bytes to file
                byte[] imageBytes = response.body().bytes();
                LOGGER.info("Received image data size: " + imageBytes.length + " bytes");
                
                if (imageBytes.length == 0) {
                    throw new RuntimeException("Received empty image data from Pollinations.AI");
                }
                
                Files.write(Paths.get(filePath), imageBytes);
                LOGGER.info("Saved image to: " + filePath);
                
                // Verify the file exists and has content
                File imageFile = new File(filePath);
                if (!imageFile.exists() || imageFile.length() == 0) {
                    throw new RuntimeException("Failed to save the image or the image is empty");
                }
                
                return filePath;
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error generating art", e);
                throw new RuntimeException("Error generating art: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Load a generated image as a JavaFX Image object
     * @param imagePath Path to the image file
     * @return JavaFX Image object
     */
    public Image loadGeneratedImage(String imagePath) {
        try {
            LOGGER.info("Loading image from: " + imagePath);
            Path path = Paths.get(imagePath);
            
            if (!Files.exists(path)) {
                LOGGER.severe("Image file does not exist: " + imagePath);
                throw new IOException("Image file does not exist: " + imagePath);
            }
            
            byte[] imageData = Files.readAllBytes(path);
            LOGGER.info("Image size: " + imageData.length + " bytes");
            
            if (imageData.length == 0) {
                LOGGER.severe("Image file is empty: " + imagePath);
                throw new IOException("Image file is empty: " + imagePath);
            }
            
            Image image = new Image(new ByteArrayInputStream(imageData));
            LOGGER.info("Image loaded successfully. Width: " + image.getWidth() + ", Height: " + image.getHeight());
            return image;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading image", e);
            throw new RuntimeException("Error loading image: " + e.getMessage(), e);
        }
    }
    
    /**
     * Alternative method to generate art - direct download from URL
     * This can be used if the other method isn't working
     */
    public CompletableFuture<String> generateArtDirect(String prompt, String style) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String fullPrompt = style != null && !style.isEmpty() 
                    ? String.format("%s of %s", style, prompt)
                    : prompt;
                
                LOGGER.info("Generating art with direct method, prompt: " + fullPrompt);
                
                // URL encode the prompt
                String encodedPrompt = URLEncoder.encode(fullPrompt, StandardCharsets.UTF_8.toString());
                // Add specific model parameter for better results and set dimensions
                String imageUrl = IMAGE_API_URL + encodedPrompt + 
                    "?model=" + DEFAULT_MODEL + 
                    "&width=" + DEFAULT_WIDTH + 
                    "&height=" + DEFAULT_HEIGHT;
                
                LOGGER.info("Using Pollinations.AI URL: " + imageUrl);
                
                // Create file destination
                String fileName = "pollinations_art_" + UUID.randomUUID().toString() + ".png";
                String filePath = imageStoragePath + fileName;
                
                // Directly open a connection to the URL and download
                URL url = new URL(imageUrl);
                byte[] imageData = url.openStream().readAllBytes();
                LOGGER.info("Downloaded image size: " + imageData.length + " bytes");
                
                // Save the image
                Files.write(Paths.get(filePath), imageData);
                LOGGER.info("Saved image to: " + filePath);
                
                return filePath;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error in direct image generation", e);
                throw new RuntimeException("Error in direct image generation: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Get available image generation models
     * @return CompletableFuture with the list of available models
     */
    public CompletableFuture<String> getAvailableModels() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Request request = new Request.Builder()
                        .url(MODELS_API_URL)
                        .get()
                        .build();
                
                Response response = httpClient.newCall(request).execute();
                
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    LOGGER.log(Level.SEVERE, "API error: " + errorBody);
                    throw new RuntimeException("Failed to get models: " + errorBody);
                }
                
                return response.body().string();
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error getting models", e);
                throw new RuntimeException("Error getting models: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Generate a chat response (text only) using Pollinations.AI text service
     * @param prompt The user's message
     * @return CompletableFuture with the AI response
     */
    public CompletableFuture<String> generateChatResponse(String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // URL encode the prompt
                String encodedPrompt = URLEncoder.encode(prompt, StandardCharsets.UTF_8.toString());
                
                // Create the request
                Request request = new Request.Builder()
                        .url(TEXT_API_URL + encodedPrompt)
                        .get()
                        .build();
                
                // Execute the request
                Response response = httpClient.newCall(request).execute();
                
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    LOGGER.log(Level.SEVERE, "API error: " + errorBody);
                    throw new RuntimeException("Failed to generate text response: " + errorBody);
                }
                
                // Return the response text
                return response.body().string();
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error generating chat response", e);
                throw new RuntimeException("Error generating chat response: " + e.getMessage(), e);
            }
        });
    }
}
