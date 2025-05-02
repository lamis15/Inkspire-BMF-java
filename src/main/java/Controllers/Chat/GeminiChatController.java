package Controllers.Chat;

import entities.Session;
import entities.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import service.GeminiChatService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Controller for the Gemini AI Art Assistant chatbot interface
 */
public class GeminiChatController {

    @FXML
    private ListView<HBox> chatListView;

    @FXML
    private TextArea messageInput;

    @FXML
    private Button sendButton;

    @FXML
    private Label statusLabel;

    @FXML
    private ScrollPane scrollPane;

    private GeminiChatService geminiService;
    private User currentUser;
    private ObservableList<HBox> chatMessages = FXCollections.observableArrayList();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private boolean isConnected = false;
    private final ScheduledExecutorService connectionChecker = Executors.newSingleThreadScheduledExecutor();

    /**
     * Initializes the controller
     */
    @FXML
    public void initialize() {
        // Set up the chat list
        chatListView.setItems(chatMessages);
        chatListView.setCellFactory(param -> new ListCell<HBox>() {
            @Override
            protected void updateItem(HBox item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setGraphic(item);
                }
            }
        });

        // Initialize the Gemini service
        geminiService = new GeminiChatService();

        // Get current user from session
        currentUser = Session.getCurrentUser();

        // Set up the send button action
        sendButton.setOnAction(event -> handleSendMessage());

        // Set up enter key to send message
        messageInput.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER") && !event.isShiftDown()) {
                event.consume(); // Prevent newline
                handleSendMessage();
            }
        });

        // Add welcome message
        Platform.runLater(() -> {
            addAIMessage("Hello! I'm your Inkspire Art Assistant. I can help you with questions about art styles, techniques, history, famous artists, and more. How can I assist you with your art journey today?");
        });

        // Test API connection
        updateConnectionStatus();
        
        // Set up periodic connection checking (every 30 seconds)
        connectionChecker.scheduleAtFixedRate(this::updateConnectionStatus, 30, 30, TimeUnit.SECONDS);
    }
    
    /**
     * Updates the connection status and UI
     */
    private void updateConnectionStatus() {
        new Thread(() -> {
            boolean wasConnected = isConnected;
            isConnected = geminiService.testApiConnection();
            
            Platform.runLater(() -> {
                if (isConnected) {
                    statusLabel.setText("Connected to Art Assistant");
                    statusLabel.setTextFill(Color.valueOf("#7efad5"));
                    sendButton.setDisable(false);
                    messageInput.setDisable(false);
                    messageInput.setPromptText("Ask about art styles, techniques, history...");
                    
                    // If we were previously disconnected, show reconnection message
                    if (!wasConnected && chatMessages.size() > 1) {
                        addSystemMessage("Connection to Art Assistant restored. You can continue your conversation.");
                    }
                } else {
                    statusLabel.setText("⚠️ Offline Mode");
                    statusLabel.setTextFill(Color.valueOf("#ff9966"));
                    
                    // Don't disable input - we'll use fallback responses
                    messageInput.setPromptText("Art Assistant is offline but will provide basic responses...");
                    
                    // If we were previously connected, show disconnection message
                    if (wasConnected && chatMessages.size() > 1) {
                        addSystemMessage("Connection to Art Assistant lost. Basic responses will be provided while offline.");
                    }
                    
                    // Show alert on first connection failure only
                    if (!wasConnected && chatMessages.size() <= 1) {
                        showAlert("Network Connection Issue", 
                                 "Could not connect to the Gemini AI service. The Art Assistant will operate in offline mode with limited responses. Please check your internet connection.");
                    }
                }
            });
        }).start();
    }

    /**
     * Handles sending a message to the AI
     */
    @FXML
    private void handleSendMessage() {
        String message = messageInput.getText().trim();
        if (message.isEmpty()) {
            return;
        }

        // Clear the input field
        messageInput.clear();

        // Add user message to the chat
        addUserMessage(message);

        // Show typing indicator
        HBox typingIndicator = createTypingIndicator();
        chatMessages.add(typingIndicator);
        scrollToBottom();

        // Send message to Gemini API
        CompletableFuture<String> responseFuture = geminiService.sendMessage(message);
        
        responseFuture.thenAccept(response -> {
            Platform.runLater(() -> {
                // Remove typing indicator
                chatMessages.remove(typingIndicator);
                
                // Add AI response to the chat
                addAIMessage(response);
                
                // Update connection status after each interaction
                updateConnectionStatus();
            });
        });
    }

    /**
     * Adds a user message to the chat
     * 
     * @param message The message to add
     */
    private void addUserMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_RIGHT);
        messageBox.setPadding(new Insets(5, 10, 5, 10));
        messageBox.setSpacing(10);

        VBox messageContainer = new VBox();
        messageContainer.setStyle("-fx-background-color: #4D81F7; -fx-background-radius: 15; -fx-padding: 10;");
        messageContainer.setMaxWidth(400);

        Text messageText = new Text(message);
        messageText.setStyle("-fx-fill: white; -fx-font-size: 14px;");
        messageText.setWrappingWidth(380);

        TextFlow textFlow = new TextFlow(messageText);
        
        Label timeLabel = new Label(LocalDateTime.now().format(timeFormatter));
        timeLabel.setStyle("-fx-text-fill: #7a7a7a; -fx-font-size: 10px;");
        
        messageContainer.getChildren().addAll(textFlow, timeLabel);
        messageBox.getChildren().add(messageContainer);

        chatMessages.add(messageBox);
        scrollToBottom();
    }

    /**
     * Adds an AI message to the chat
     * 
     * @param message The message to add
     */
    private void addAIMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(5, 10, 5, 10));
        messageBox.setSpacing(10);

        // AI avatar
        ImageView avatar = new ImageView(new Image(getClass().getResourceAsStream("/assets/icons/chat.png")));
        avatar.setFitHeight(30);
        avatar.setFitWidth(30);

        VBox messageContainer = new VBox();
        messageContainer.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 15; -fx-padding: 10;");
        messageContainer.setMaxWidth(400);

        Text messageText = new Text(message);
        messageText.setStyle("-fx-fill: #333333; -fx-font-size: 14px;");
        messageText.setWrappingWidth(380);

        TextFlow textFlow = new TextFlow(messageText);
        
        Label timeLabel = new Label(LocalDateTime.now().format(timeFormatter));
        timeLabel.setStyle("-fx-text-fill: #7a7a7a; -fx-font-size: 10px;");
        
        messageContainer.getChildren().addAll(textFlow, timeLabel);
        messageBox.getChildren().addAll(avatar, messageContainer);

        chatMessages.add(messageBox);
        scrollToBottom();
    }
    
    /**
     * Adds a system message to the chat (for connection status updates)
     * 
     * @param message The message to add
     */
    private void addSystemMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPadding(new Insets(5, 10, 5, 10));

        Label systemLabel = new Label(message);
        systemLabel.setStyle("-fx-background-color: #f8f8f8; -fx-background-radius: 10; -fx-padding: 5 10; -fx-text-fill: #666; -fx-font-size: 12px; -fx-border-color: #ddd; -fx-border-radius: 10; -fx-border-width: 1;");
        
        messageBox.getChildren().add(systemLabel);
        chatMessages.add(messageBox);
        scrollToBottom();
    }

    /**
     * Creates a typing indicator to show while waiting for AI response
     * 
     * @return HBox containing the typing indicator
     */
    private HBox createTypingIndicator() {
        HBox indicatorBox = new HBox();
        indicatorBox.setAlignment(Pos.CENTER_LEFT);
        indicatorBox.setPadding(new Insets(5, 10, 5, 10));
        indicatorBox.setSpacing(10);

        // AI avatar
        ImageView avatar = new ImageView(new Image(getClass().getResourceAsStream("/assets/icons/chat.png")));
        avatar.setFitHeight(30);
        avatar.setFitWidth(30);

        Label typingLabel = new Label("Thinking about art...");
        typingLabel.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 15; -fx-padding: 10; -fx-text-fill: #666;");

        indicatorBox.getChildren().addAll(avatar, typingLabel);
        return indicatorBox;
    }

    /**
     * Scrolls the chat to the bottom
     */
    private void scrollToBottom() {
        Platform.runLater(() -> {
            chatListView.scrollTo(chatMessages.size() - 1);
        });
    }

    /**
     * Shows an alert dialog
     * 
     * @param title The alert title
     * @param message The alert message
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Cleanup resources when the controller is no longer needed
     */
    public void shutdown() {
        connectionChecker.shutdown();
    }
}
