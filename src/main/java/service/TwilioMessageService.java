package service;

import entities.Message;
import entities.User;

import java.sql.SQLException;
import java.util.List;

/**
 * Service for handling messages using Twilio as the backend
 * This integrates with the existing MessageService but uses Twilio for message delivery
 */
public class TwilioMessageService {
    private final MessageService messageService;
    private final TwilioMessageSender twilioSender;
    
    /**
     * Constructor
     */
    public TwilioMessageService() {
        this.messageService = new MessageService();
        this.twilioSender = TwilioMessageSender.getInstance();
    }
    
    /**
     * Send a message using both the local database and Twilio
     * @param message The message to send
     * @return true if the message was sent successfully
     */
    public boolean sendMessage(Message message) throws SQLException {
        // First, save the message to the local database
        boolean localSuccess = messageService.ajouter(message);
        
        // Then send it via Twilio
        boolean twilioSuccess = twilioSender.sendMessage(
                message.getAuthorId(),
                message.getRecipientId(),
                message.getContent()
        );
        
        // Return true only if both operations succeeded
        return localSuccess && twilioSuccess;
    }
    
    /**
     * Get all messages between two users
     * @param user1Id First user ID
     * @param user2Id Second user ID
     * @return List of messages
     */
    public List<Message> getConversationMessages(Integer user1Id, Integer user2Id) throws SQLException {
        // For now, we're still retrieving messages from the local database
        return messageService.getConversationMessages(user1Id, user2Id);
    }
    
    /**
     * Mark messages as read
     * @param senderId The ID of the message sender
     * @param recipientId The ID of the message recipient
     */
    public void markMessagesAsRead(Integer senderId, Integer recipientId) throws SQLException {
        messageService.markMessagesAsRead(senderId, recipientId);
    }
    
    /**
     * Get the last message between two users
     * @param user1Id First user ID
     * @param user2Id Second user ID
     * @return The last message, or null if none exists
     */
    public Message getLastMessage(Integer user1Id, Integer user2Id) throws SQLException {
        return messageService.getLastMessage(user1Id, user2Id);
    }
    
    /**
     * Get the number of unread messages from one user to another
     * @param senderId The ID of the sender
     * @param recipientId The ID of the recipient
     * @return The number of unread messages
     */
    public int getUnreadMessageCount(Integer senderId, Integer recipientId) throws SQLException {
        return messageService.getUnreadMessageCount(senderId, recipientId);
    }
    
    /**
     * Get all users that have conversations with the given user
     * @param userId The user ID
     * @return List of user IDs
     */
    public List<Integer> getUsersWithConversations(Integer userId) throws SQLException {
        return messageService.getUsersWithConversations(userId);
    }
}
