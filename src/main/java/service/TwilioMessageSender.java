package service;

import com.twilio.Twilio;
import com.twilio.rest.conversations.v1.Conversation;
import com.twilio.rest.conversations.v1.conversation.Participant;
import com.twilio.rest.conversations.v1.User;
import entities.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for sending messages via Twilio Conversations API
 * This implementation uses Twilio for chat messaging (not SMS)
 */
public class TwilioMessageSender {
    // Your Twilio credentials - in production, these should be stored securely
    private static final String ACCOUNT_SID = "AC671c573554e95a4652a4742341a97437";
    private static final String AUTH_TOKEN = "205cb9bfdc9c683085947c53693d4642";
    
    // Thread pool for asynchronous operations
    private final ExecutorService executorService;
    
    // Cache of conversation SIDs for user pairs
    private final Map<String, String> conversationCache = new HashMap<>();
    
    // Singleton instance
    private static TwilioMessageSender instance;
    
    /**
     * Get the singleton instance of TwilioMessageSender
     */
    public static synchronized TwilioMessageSender getInstance() {
        if (instance == null) {
            instance = new TwilioMessageSender();
        }
        return instance;
    }
    
    /**
     * Private constructor to enforce singleton pattern
     */
    private TwilioMessageSender() {
        // Initialize Twilio with your credentials
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        
        // Create a thread pool for asynchronous operations
        executorService = Executors.newFixedThreadPool(5);
    }
    
    /**
     * Send a message from one user to another using Twilio Conversations
     * @param senderUserId The ID of the sender
     * @param recipientUserId The ID of the recipient
     * @param content The message content
     * @return true if the message was sent successfully, false otherwise
     */
    public boolean sendMessage(Integer senderUserId, Integer recipientUserId, String content) {
        try {
            // Run the operation asynchronously
            executorService.submit(() -> {
                try {
                    // Get or create a conversation between these two users
                    String conversationSid = getOrCreateConversation(senderUserId, recipientUserId);
                    
                    // Create the message in the conversation
                    com.twilio.rest.conversations.v1.conversation.Message message = 
                            com.twilio.rest.conversations.v1.conversation.Message.creator(conversationSid)
                            .setAuthor(senderUserId.toString())
                            .setBody(content)
                            .create();
                    
                    System.out.println("Message sent via Twilio: " + message.getSid());
                } catch (Exception e) {
                    System.err.println("Error sending message via Twilio: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
            return true;
        } catch (Exception e) {
            System.err.println("Error submitting message task: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get an existing conversation or create a new one for two users
     * @param user1Id First user ID
     * @param user2Id Second user ID
     * @return The conversation SID
     */
    private String getOrCreateConversation(Integer user1Id, Integer user2Id) {
        // Create a unique key for this user pair (order doesn't matter)
        String cacheKey = getCacheKey(user1Id, user2Id);
        
        // Check if we already have a conversation for these users
        if (conversationCache.containsKey(cacheKey)) {
            return conversationCache.get(cacheKey);
        }
        
        try {
            // Create a new conversation
            Conversation conversation = Conversation.creator()
                    .setFriendlyName("Conversation between " + user1Id + " and " + user2Id)
                    .create();
            
            String conversationSid = conversation.getSid();
            
            // Add both users as participants
            ensureUserExists(user1Id.toString());
            ensureUserExists(user2Id.toString());
            
            Participant.creator(conversationSid)
                    .setIdentity(user1Id.toString())
                    .create();
            
            Participant.creator(conversationSid)
                    .setIdentity(user2Id.toString())
                    .create();
            
            // Cache the conversation SID
            conversationCache.put(cacheKey, conversationSid);
            
            return conversationSid;
        } catch (Exception e) {
            System.err.println("Error creating conversation: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Ensure a user exists in Twilio Conversations
     * @param userId The user ID to check/create
     */
    private void ensureUserExists(String userId) {
        try {
            // Try to fetch the user first
            User.fetcher(userId).fetch();
        } catch (Exception e) {
            // User doesn't exist, create them
            try {
                User.creator(userId)
                        .setFriendlyName("User " + userId)
                        .create();
            } catch (Exception createError) {
                System.err.println("Error creating user: " + createError.getMessage());
            }
        }
    }
    
    /**
     * Create a cache key for a user pair (order doesn't matter)
     * @param user1Id First user ID
     * @param user2Id Second user ID
     * @return A unique cache key
     */
    private String getCacheKey(Integer user1Id, Integer user2Id) {
        // Ensure consistent ordering regardless of which user is which
        return user1Id < user2Id 
                ? user1Id + "_" + user2Id 
                : user2Id + "_" + user1Id;
    }
    
    /**
     * Shutdown the executor service
     */
    public void shutdown() {
        executorService.shutdown();
    }
}
