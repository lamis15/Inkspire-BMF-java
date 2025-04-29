package service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for interacting with the Gemini AI API for art-related conversations
 */
public class GeminiChatService {
    private static final Logger LOGGER = Logger.getLogger(GeminiChatService.class.getName());
    private static final String API_KEY = "AIzaSyBdV8mTewpssGGVHCf3VfL7FsvDHv2QPd8";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;
    
    private static final String ART_SYSTEM_PROMPT = 
        "You are Inkspire Art Assistant, an AI specialized in art-related topics. " +
        "Your expertise covers fine art, digital art, art history, techniques, artists, " +
        "movements, collecting, valuation, conservation, exhibitions, and art education. " +
        "Provide thoughtful, accurate, and educational responses about art. " +
        "If asked about non-art topics, politely redirect the conversation to art-related subjects. " +
        "For example, if asked about politics, you might say: 'I'm specialized in art topics. " +
        "Instead, would you like to discuss political art movements or how politics has influenced art throughout history?' " +
        "Be engaging, informative, and inspiring to help users deepen their appreciation and understanding of art. " +
        "Provide specific examples, artist references, and historical context when relevant. " +
        "When discussing techniques, be clear and instructive. " +
        "When discussing art history, be accurate and educational. " +
        "When discussing contemporary art, be balanced and thoughtful. " +
        "Always maintain a positive, encouraging tone that inspires creativity.";
    
    // Fallback responses for when the API is unavailable
    private static final String[] FALLBACK_RESPONSES = {
        "I'd love to discuss art with you, but I'm currently having trouble connecting to my knowledge database. Please try again in a moment.",
        "As an art assistant, I'm experiencing a temporary connection issue. Please try your question again shortly.",
        "I'm currently unable to access my art knowledge database. This is likely a temporary network issue. Please try again soon.",
        "I apologize, but I'm having trouble connecting to the art information service. Please check your internet connection and try again.",
        "My connection to the art database seems to be interrupted. This is usually temporary. Please try again in a few moments."
    };
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final List<Message> conversationHistory;
    private int fallbackResponseIndex = 0;
    
    /**
     * Message class for conversation history
     */
    private static class Message {
        private final String role;
        private final String content;
        
        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
        
        public String getRole() {
            return role;
        }
        
        public String getContent() {
            return content;
        }
    }
    
    /**
     * Constructor
     */
    public GeminiChatService() {
        // Configure HTTP client with timeouts to prevent long hanging connections
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        this.conversationHistory = new ArrayList<>();
        
        // Store system prompt as the first message, but use "model" role instead of "system"
        conversationHistory.add(new Message("model", ART_SYSTEM_PROMPT));
    }
    
    /**
     * Sends a message to the Gemini API
     * 
     * @param message The message to send
     * @return CompletableFuture with the response
     */
    public CompletableFuture<String> sendMessage(String message) {
        // Add user message to history
        conversationHistory.add(new Message("user", message));
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                String response = sendRequest(buildConversationRequest());
                
                // Add assistant response to history
                conversationHistory.add(new Message("model", response));
                
                return response;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error sending message to Gemini API", e);
                String fallbackResponse = getNextFallbackResponse();
                
                // Still add the fallback response to history
                conversationHistory.add(new Message("model", fallbackResponse));
                
                return fallbackResponse;
            }
        });
    }
    
    /**
     * Gets the next fallback response in a round-robin fashion
     * 
     * @return A fallback response
     */
    private synchronized String getNextFallbackResponse() {
        String response = FALLBACK_RESPONSES[fallbackResponseIndex];
        fallbackResponseIndex = (fallbackResponseIndex + 1) % FALLBACK_RESPONSES.length;
        return response;
    }
    
    /**
     * Tests the API connection
     * 
     * @return true if the connection is successful
     */
    public boolean testApiConnection() {
        try {
            // Simple test request with short timeout
            ObjectNode requestBody = objectMapper.createObjectNode();
            
            // Create contents array with a simple text part
            ArrayNode contentsArray = objectMapper.createArrayNode();
            ObjectNode contentNode = objectMapper.createObjectNode();
            
            // Create parts array with text
            ArrayNode partsArray = objectMapper.createArrayNode();
            ObjectNode partNode = objectMapper.createObjectNode();
            partNode.put("text", "Hello, art world!");
            partsArray.add(partNode);
            
            contentNode.set("parts", partsArray);
            contentNode.put("role", "user");
            contentsArray.add(contentNode);
            
            requestBody.set("contents", contentsArray);
            
            // Set generation config
            ObjectNode generationConfig = objectMapper.createObjectNode();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("topP", 0.8);
            generationConfig.put("topK", 40);
            requestBody.set("generationConfig", generationConfig);
            
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(5)) // Short timeout for testing
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            return response.statusCode() == 200;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "API connection test failed", e);
            return false;
        }
    }
    
    /**
     * Builds the conversation request JSON
     * 
     * @return The request JSON as a string
     * @throws Exception If the request cannot be built
     */
    private String buildConversationRequest() throws Exception {
        ObjectNode requestBody = objectMapper.createObjectNode();
        ArrayNode contentsArray = objectMapper.createArrayNode();
        
        // Include the system prompt as a user message at the beginning
        ObjectNode systemAsUserNode = objectMapper.createObjectNode();
        systemAsUserNode.put("role", "user");
        
        ArrayNode systemPartsArray = objectMapper.createArrayNode();
        ObjectNode systemPartNode = objectMapper.createObjectNode();
        systemPartNode.put("text", "You are an art assistant. Please follow these instructions for all future responses: " + ART_SYSTEM_PROMPT);
        systemPartsArray.add(systemPartNode);
        
        systemAsUserNode.set("parts", systemPartsArray);
        contentsArray.add(systemAsUserNode);
        
        // Add a model response acknowledging the instructions
        ObjectNode modelAckNode = objectMapper.createObjectNode();
        modelAckNode.put("role", "model");
        
        ArrayNode modelPartsArray = objectMapper.createArrayNode();
        ObjectNode modelPartNode = objectMapper.createObjectNode();
        modelPartNode.put("text", "I understand. I'll be your Inkspire Art Assistant, focusing on art-related topics and providing thoughtful, educational responses about art.");
        modelPartsArray.add(modelPartNode);
        
        modelAckNode.set("parts", modelPartsArray);
        contentsArray.add(modelAckNode);
        
        // Add conversation history (excluding the first system message which we handled differently)
        for (int i = 1; i < conversationHistory.size(); i++) {
            Message message = conversationHistory.get(i);
            String role = message.getRole();
            
            // Convert "system" role to "user" with a prefix
            if (role.equals("system")) {
                role = "user";
            }
            
            ObjectNode contentNode = objectMapper.createObjectNode();
            contentNode.put("role", role);
            
            ArrayNode partsArray = objectMapper.createArrayNode();
            ObjectNode partNode = objectMapper.createObjectNode();
            partNode.put("text", message.getContent());
            partsArray.add(partNode);
            
            contentNode.set("parts", partsArray);
            contentsArray.add(contentNode);
        }
        
        requestBody.set("contents", contentsArray);
        
        // Set generation config for more focused art-related responses
        ObjectNode generationConfig = objectMapper.createObjectNode();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("topP", 0.8);
        generationConfig.put("topK", 40);
        generationConfig.put("maxOutputTokens", 1024);
        requestBody.set("generationConfig", generationConfig);
        
        return objectMapper.writeValueAsString(requestBody);
    }
    
    /**
     * Sends a request to the Gemini API
     * 
     * @param requestBody The request body as a JSON string
     * @return The response text
     * @throws IOException If the request fails
     * @throws InterruptedException If the request is interrupted
     */
    private String sendRequest(String requestBody) throws IOException, InterruptedException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(15)) // Add a reasonable timeout
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                LOGGER.warning("API request failed with status code: " + response.statusCode() + 
                              ", Response: " + response.body());
                throw new IOException("API request failed with status code: " + response.statusCode() + 
                                     ", Response: " + response.body());
            }
            
            return extractTextFromResponse(response.body());
        } catch (SocketException e) {
            LOGGER.warning("Network connection issue: " + e.getMessage());
            throw new IOException("Network connection issue. Please check your internet connection.", e);
        }
    }
    
    /**
     * Extracts the text from the Gemini API response
     * 
     * @param responseJson The response JSON
     * @return The extracted text
     * @throws IOException If the response cannot be parsed
     */
    private String extractTextFromResponse(String responseJson) throws IOException {
        try {
            JsonNode rootNode = objectMapper.readTree(responseJson);
            
            if (!rootNode.has("candidates") || rootNode.get("candidates").isEmpty()) {
                throw new IOException("No candidates in response: " + responseJson);
            }
            
            JsonNode candidate = rootNode.get("candidates").get(0);
            
            if (!candidate.has("content") || 
                !candidate.get("content").has("parts") || 
                candidate.get("content").get("parts").isEmpty()) {
                throw new IOException("No content parts in response: " + responseJson);
            }
            
            return candidate.get("content").get("parts").get(0).get("text").asText();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to parse API response", e);
            throw new IOException("Failed to parse API response", e);
        }
    }
}
