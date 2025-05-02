package Controllers.Messages;

import entities.Message;
import entities.User;
import entities.Session;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import service.MessageService;
import service.TwilioMessageService;
import service.UserService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for the main Messages view
 */
public class MessagesController {

    @FXML
    private ListView<StackPane> channelListView;

    @FXML
    private ListView<HBox> messageListView;

    @FXML
    private TextArea messageInput;

    @FXML
    private Button sendButton;

    @FXML
    private Label currentChannelLabel;

    @FXML
    private HBox messageInputContainer;

    @FXML
    private VBox noSelectionPlaceholder;

    private User currentUser;
    private User selectedUser;
    private Map<Integer, List<Message>> messagesByUser = new HashMap<>();
    private List<User> availableUsers = new ArrayList<>();
    
    private UserService userService;
    private MessageService messageService;
    private TwilioMessageService twilioMessageService;
    
    // Observable lists for JavaFX controls
    private ObservableList<StackPane> channelItems = FXCollections.observableArrayList();
    private ObservableList<HBox> messageItems = FXCollections.observableArrayList();

    /**
     * Initializes the controller
     */
    @FXML
    public void initialize() {
        // Set the items for the list views using our observable lists
        channelListView.setItems(channelItems);
        messageListView.setItems(messageItems);
        
        // Disable auto selection initially
        channelListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        // Initialize services
        userService = new UserService();
        messageService = new MessageService();
        twilioMessageService = new TwilioMessageService();
        
        // Get current user from session
        currentUser = Session.getCurrentUser();
        
        if (currentUser == null) {
            // Handle case where user is not logged in
            showAlert("Error", "You must be logged in to use the messaging feature.");
            return;
        }

        // Hide message input initially and show placeholder
        messageInputContainer.setVisible(false);
        messageInputContainer.setManaged(false);
        noSelectionPlaceholder.setVisible(true);
        noSelectionPlaceholder.setManaged(true);
        
        // Set up channel selection listener - only add this after the list is populated
        channelListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                int index = channelListView.getSelectionModel().getSelectedIndex();
                if (index >= 0 && index < availableUsers.size()) {
                    selectedUser = availableUsers.get(index);
                    String userName = "Unknown User";
                    if (selectedUser.getFirstName() != null && selectedUser.getLastName() != null) {
                        userName = selectedUser.getFirstName() + " " + selectedUser.getLastName();
                    } else if (selectedUser.getFirstName() != null) {
                        userName = selectedUser.getFirstName();
                    } else if (selectedUser.getEmail() != null) {
                        userName = selectedUser.getEmail();
                    }
                    currentChannelLabel.setText(userName);
                    
                    // Show message input and hide placeholder
                    messageInputContainer.setVisible(true);
                    messageInputContainer.setManaged(true);
                    noSelectionPlaceholder.setVisible(false);
                    noSelectionPlaceholder.setManaged(false);
                    
                    try {
                        loadMessages(selectedUser.getId());
                    } catch (SQLException e) {
                        System.err.println("Failed to load messages: " + e.getMessage());
                    }
                }
            }
        });
        
        // Load data in a separate thread to avoid blocking the UI
        new Thread(() -> {
            try {
                // Load available users from database
                loadAvailableUsers();
                
                // Load channels (conversations with other users)
                loadChannels();
                
                // Don't select any chat initially, just show a welcome message
                Platform.runLater(() -> {
                    try {
                        // Clear any selection
                        channelListView.getSelectionModel().clearSelection();
                        
                        // Set welcome message
                        currentChannelLabel.setText("Select a conversation");
                        
                        // Hide message input and show placeholder
                        messageInputContainer.setVisible(false);
                        messageInputContainer.setManaged(false);
                        noSelectionPlaceholder.setVisible(true);
                        noSelectionPlaceholder.setManaged(true);
                        
                        // Clear message list
                        messageItems.clear();
                    } catch (Exception e) {
                        System.err.println("Error initializing chat view: " + e.getMessage());
                    }
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    showAlert("Error", "Failed to initialize messaging: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Loads available users for messaging from the database
     */
    private void loadAvailableUsers() throws SQLException {
        availableUsers.clear();
        messagesByUser.clear();
        
        try {
            // Get all users from database
            List<User> allUsers = userService.recuperer();
            
            if (allUsers == null || allUsers.isEmpty()) {
                System.out.println("No users found in database");
                return;
            }
            
            // Get users that the current user has conversations with
            List<Integer> conversationUserIds;
            try {
                conversationUserIds = twilioMessageService.getUsersWithConversations(currentUser.getId());
            } catch (SQLException e) {
                System.err.println("Error getting conversation users: " + e.getMessage());
                conversationUserIds = new ArrayList<>();
            }
            
            // Add users that the current user has conversations with
            for (User user : allUsers) {
                // Skip invalid users or the current user
                if (user == null || user.getId() == null) {
                    continue;
                }
                
                if (!user.getId().equals(currentUser.getId())) {
                    // If the user has a conversation with the current user, or we want to show all users
                    // Set to 'true' to show all users, 'false' to only show users with conversations
                    if (conversationUserIds.contains(user.getId()) || true) {
                        availableUsers.add(user);
                    }
                }
            }
            
            // Load messages for each user
            for (User user : availableUsers) {
                try {
                    if (user.getId() != null) {
                        List<Message> messages = twilioMessageService.getConversationMessages(currentUser.getId(), user.getId());
                        if (messages != null) {
                            messagesByUser.put(user.getId(), messages);
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Error loading messages for user " + user.getId() + ": " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error in loadAvailableUsers: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Unexpected error in loadAvailableUsers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads channels (conversations with other users)
     */
    private void loadChannels() throws SQLException {
        // Clear the observable list first
        Platform.runLater(() -> channelItems.clear());
        
        // If no available users, just return
        if (availableUsers == null || availableUsers.isEmpty()) {
            return;
        }
        
        // Temporary list to hold channel items
        List<StackPane> tempChannelItems = new ArrayList<>();
        
        for (User user : availableUsers) {
            try {
                // Check if user is valid
                if (user == null || user.getId() == null) {
                    continue;
                }
                
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/messages/ChannelItem.fxml"));
                StackPane channelItem = loader.load();
                
                // Set channel data
                Circle userAvatar = (Circle) channelItem.lookup("#userAvatar");
                Label usernameLabel = (Label) channelItem.lookup("#usernameLabel");
                Label lastMessageLabel = (Label) channelItem.lookup("#lastMessageLabel");
                Circle unreadIndicator = (Circle) channelItem.lookup("#unreadIndicator");
                
                // Set username (with null check)
                String userName = "Unknown User";
                if (user.getFirstName() != null && user.getLastName() != null) {
                    userName = user.getFirstName() + " " + user.getLastName();
                } else if (user.getFirstName() != null) {
                    userName = user.getFirstName();
                } else if (user.getEmail() != null) {
                    userName = user.getEmail();
                }
                usernameLabel.setText(userName);
                
                // Try to get the last message between these users
                Message lastMessage = null;
                try {
                    lastMessage = twilioMessageService.getLastMessage(currentUser.getId(), user.getId());
                } catch (SQLException e) {
                    System.err.println("Error getting last message: " + e.getMessage());
                }
                
                if (lastMessage != null) {
                    String preview = lastMessage.getContent();
                    if (preview != null) {
                        if (preview.length() > 25) {
                            preview = preview.substring(0, 22) + "...";
                        }
                        lastMessageLabel.setText(preview);
                    } else {
                        lastMessageLabel.setText("No content");
                    }
                    
                    // Get unread message count
                    try {
                        int unreadCount = twilioMessageService.getUnreadMessageCount(user.getId(), currentUser.getId());
                        
                        // Show unread indicator and count
                        Label unreadCountLabel = (Label) channelItem.lookup("#unreadCount");
                        Circle unreadBadge = (Circle) channelItem.lookup("#unreadIndicator");
                        
                        if (unreadCount > 0) {
                            // Show unread count
                            unreadCountLabel.setText(String.valueOf(unreadCount));
                            unreadBadge.setVisible(true);
                            unreadCountLabel.setVisible(true);
                        } else {
                            // Hide unread indicators
                            unreadBadge.setVisible(false);
                            unreadCountLabel.setVisible(false);
                        }
                    } catch (SQLException e) {
                        System.err.println("Error getting unread message count: " + e.getMessage());
                    }
                } else {
                    lastMessageLabel.setText("No messages yet");
                    
                    // Hide unread indicators
                    Label unreadCountLabel = (Label) channelItem.lookup("#unreadCount");
                    Circle unreadBadge = (Circle) channelItem.lookup("#unreadIndicator");
                    unreadBadge.setVisible(false);
                    unreadCountLabel.setVisible(false);
                }
                
                // Add to temporary list
                tempChannelItems.add(channelItem);
                
            } catch (IOException e) {
                System.err.println("Error loading channel item: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("Unexpected error processing user: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Update the observable list on the JavaFX thread
        final List<StackPane> finalChannelItems = tempChannelItems;
        Platform.runLater(() -> {
            try {
                channelItems.setAll(finalChannelItems);
                
                // Add click event handlers to each channel item
                for (int i = 0; i < channelItems.size(); i++) {
                    final User user = availableUsers.get(i);
                    final StackPane channelItem = channelItems.get(i);
                    
                    channelItem.setOnMouseClicked(event -> {
                        selectedUser = user;
                        // Use firstName + lastName as the display name
                        String displayName = user.getFirstName() + " " + user.getLastName();
                        currentChannelLabel.setText(displayName);
                        
                        // Show message input and hide placeholder
                        messageInputContainer.setVisible(true);
                        messageInputContainer.setManaged(true);
                        noSelectionPlaceholder.setVisible(false);
                        noSelectionPlaceholder.setManaged(false);
                        
                        try {
                            loadMessages(user.getId());
                        } catch (SQLException e) {
                            System.err.println("Error loading messages: " + e.getMessage());
                        }
                    });
                }
            } catch (Exception e) {
                System.err.println("Error updating channel items: " + e.getMessage());
            }
        });
    }

    /**
     * Loads messages for a specific user from the database
     * @param userId The user ID to load messages for
     */
    private void loadMessages(Integer userId) throws SQLException {
        if (userId == null) {
            return;
        }
        
        // Clear the message list on the JavaFX thread
        Platform.runLater(() -> messageItems.clear());
        
        try {
            // Mark all messages from this user as read
            twilioMessageService.markMessagesAsRead(userId, currentUser.getId());
            
            // Get messages between these users
            List<Message> messages = twilioMessageService.getConversationMessages(currentUser.getId(), userId);
            
            // Temporary list to hold message items
            List<HBox> tempMessageItems = new ArrayList<>();
            
            if (messages != null && !messages.isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                
                for (Message message : messages) {
                    try {
                        // Skip invalid messages
                        if (message == null || message.getAuthorId() == null || message.getContent() == null) {
                            continue;
                        }
                        
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/messages/MessageItem.fxml"));
                        HBox messageItem = loader.load();
                        
                        // Set message data
                        Circle userAvatar = (Circle) messageItem.lookup("#userAvatar");
                        Circle userAvatarRight = (Circle) messageItem.lookup("#userAvatarRight");
                        Label usernameLabel = (Label) messageItem.lookup("#usernameLabel");
                        Label timestampLabel = (Label) messageItem.lookup("#timestampLabel");
                        Label messageContentLabel = (Label) messageItem.lookup("#messageContentLabel");
                        
                        // Determine if message is from current user or the other person
                        boolean isFromCurrentUser = message.getAuthorId().equals(currentUser.getId());
                        
                        // Set username based on sender
                        if (isFromCurrentUser) {
                            usernameLabel.setText("You");
                            userAvatar.setFill(Color.valueOf("#3498db"));
                        } else {
                            try {
                                User sender = userService.getById(message.getAuthorId());
                                if (sender != null) {
                                    String senderName = "Unknown User";
                                    if (sender.getFirstName() != null && sender.getLastName() != null) {
                                        senderName = sender.getFirstName() + " " + sender.getLastName();
                                    } else if (sender.getFirstName() != null) {
                                        senderName = sender.getFirstName();
                                    } else if (sender.getEmail() != null) {
                                        senderName = sender.getEmail();
                                    }
                                    usernameLabel.setText(senderName);
                                    userAvatar.setFill(Color.valueOf("#e74c3c"));
                                } else {
                                    usernameLabel.setText("Unknown User");
                                    userAvatar.setFill(Color.valueOf("#e74c3c"));
                                }
                            } catch (SQLException e) {
                                usernameLabel.setText("Unknown User");
                                userAvatar.setFill(Color.valueOf("#e74c3c"));
                            }
                        }
                        
                        // Set timestamp
                        if (message.getCreatedAt() != null) {
                            timestampLabel.setText(message.getCreatedAt().format(formatter));
                        } else {
                            timestampLabel.setText("");
                        }
                        
                        // Set message content
                        messageContentLabel.setText(message.getContent());
                        
                        // Add styling for sent vs received messages
                        HBox messageContainer = (HBox) messageItem.lookup("#messageContainer");
                        if (isFromCurrentUser) {
                            messageContainer.getStyleClass().add("sent");
                            // Set the same color for both avatars
                            Color userColor = Color.valueOf("#4D81F7");
                            userAvatar.setFill(userColor);
                            userAvatarRight.setFill(userColor);
                        } else {
                            messageContainer.getStyleClass().add("received");
                            // Set color for received message avatar
                            userAvatar.setFill(Color.valueOf("#F05454"));
                        }
                        
                        // Add to temporary list
                        tempMessageItems.add(messageItem);
                        
                    } catch (IOException e) {
                        System.err.println("Error loading message item: " + e.getMessage());
                    } catch (Exception e) {
                        System.err.println("Unexpected error processing message: " + e.getMessage());
                    }
                }
            }
            
            // Update the observable list on the JavaFX thread
            final List<HBox> finalMessageItems = tempMessageItems;
            Platform.runLater(() -> {
                try {
                    messageItems.setAll(finalMessageItems);
                    
                    // Scroll to the bottom of the list after a short delay to ensure UI is updated
                    PauseTransition pause = new PauseTransition(Duration.millis(100));
                    pause.setOnFinished(event -> {
                        if (messageListView.getItems().size() > 0) {
                            messageListView.scrollTo(messageListView.getItems().size() - 1);
                        }
                    });
                    pause.play();
                } catch (Exception e) {
                    System.err.println("Error updating message items: " + e.getMessage());
                }
            });
            
            // Update channels to reflect read status changes
            try {
                loadChannels();
            } catch (SQLException e) {
                System.err.println("Error reloading channels: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("Database error loading messages: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Unexpected error in loadMessages: " + e.getMessage());
        }
    }

    /**
     * Handles sending a new message
     */
    @FXML
    public void handleSendMessage() {
        if (selectedUser == null) {
            return;
        }
        
        String content = messageInput.getText().trim();
        if (content.isEmpty()) {
            return;
        }
        
        // Get the content and clear input immediately for better UX
        final String finalContent = content;
        Platform.runLater(() -> messageInput.clear());
        
        // Create new message
        Message newMessage = new Message(
            currentUser.getId(),
            selectedUser.getId(),
            finalContent
        );
        
        // Run database operations in a background thread
        new Thread(() -> {
            try {
                // Save message to database and send via Twilio
                boolean success = twilioMessageService.sendMessage(newMessage);
                
                if (success) {
                    // Reload messages
                    loadMessages(selectedUser.getId());
                    
                    // Ensure we scroll to the bottom after sending a message
                    Platform.runLater(() -> {
                        PauseTransition pause = new PauseTransition(Duration.millis(200));
                        pause.setOnFinished(event -> {
                            if (messageListView.getItems().size() > 0) {
                                messageListView.scrollTo(messageListView.getItems().size() - 1);
                            }
                        });
                        pause.play();
                    });
                } else {
                    Platform.runLater(() -> {
                        showAlert("Error", "Failed to send message.");
                    });
                }
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    showAlert("Error", "Failed to send message: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /**
     * Shows an alert dialog
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}