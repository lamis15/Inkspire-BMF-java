package service;

import entities.Message;
import utils.DataSource;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for handling Message entity operations
 */
public class MessageService implements IService<Message> {

    private Connection connection;

    public MessageService() {
        connection = DataSource.getInstance().getConnection();
    }

    @Override
    public boolean ajouter(Message message) {
        String query = "INSERT INTO message (author_id, recipient_id, content, created_at, is_read) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, message.getAuthorId());
            statement.setInt(2, message.getRecipientId());
            statement.setString(3, message.getContent());
            
            // Convert LocalDateTime to Timestamp
            Timestamp timestamp = Timestamp.valueOf(message.getCreatedAt());
            statement.setTimestamp(4, timestamp);
            
            statement.setBoolean(5, message.getIsRead());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating message failed, no rows affected.");
            }
            
            // Get the generated ID
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    message.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating message failed, no ID obtained.");
                }
            }
            
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding message: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean modifier(Message message) {
        String query = "UPDATE message SET author_id = ?, recipient_id = ?, content = ?, created_at = ?, is_read = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, message.getAuthorId());
            statement.setInt(2, message.getRecipientId());
            statement.setString(3, message.getContent());
            
            // Convert LocalDateTime to Timestamp
            Timestamp timestamp = Timestamp.valueOf(message.getCreatedAt());
            statement.setTimestamp(4, timestamp);
            
            statement.setBoolean(5, message.getIsRead());
            statement.setInt(6, message.getId());
            
            int rows = statement.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating message: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean supprimer(int id) {
        String query = "DELETE FROM message WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            int rows = statement.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting message: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Message> recuperer() throws SQLException {
        List<Message> messages = new ArrayList<>();
        String query = "SELECT * FROM message ORDER BY created_at DESC";
        
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            
            while (resultSet.next()) {
                Message message = extractMessageFromResultSet(resultSet);
                messages.add(message);
            }
        }
        
        return messages;
    }
    
    /**
     * Get messages for a conversation between two users
     * @param userId1 First user ID
     * @param userId2 Second user ID
     * @return List of messages between the two users
     */
    public List<Message> getConversationMessages(int userId1, int userId2) throws SQLException {
        List<Message> messages = new ArrayList<>();
        String query = "SELECT * FROM message WHERE (author_id = ? AND recipient_id = ?) OR (author_id = ? AND recipient_id = ?) ORDER BY created_at ASC";
        
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId1);
            statement.setInt(2, userId2);
            statement.setInt(3, userId2);
            statement.setInt(4, userId1);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Message message = extractMessageFromResultSet(resultSet);
                    messages.add(message);
                }
            }
        }
        
        return messages;
    }
    
    /**
     * Get all users that the current user has conversations with
     * @param userId The current user ID
     * @return List of user IDs that the current user has conversations with
     */
    public List<Integer> getUsersWithConversations(int userId) throws SQLException {
        List<Integer> userIds = new ArrayList<>();
        String query = "SELECT DISTINCT " +
                "CASE " +
                "    WHEN author_id = ? THEN recipient_id " +
                "    ELSE author_id " +
                "END AS other_user_id " +
                "FROM message " +
                "WHERE author_id = ? OR recipient_id = ? " +
                "ORDER BY other_user_id";
        
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            statement.setInt(2, userId);
            statement.setInt(3, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int otherUserId = resultSet.getInt("other_user_id");
                    if (otherUserId != userId) { // Ensure we don't add the current user
                        userIds.add(otherUserId);
                    }
                }
            }
        }
        
        return userIds;
    }
    
    /**
     * Mark all messages from a specific user to the current user as read
     * @param fromUserId The sender user ID
     * @param toUserId The recipient user ID (current user)
     * @return True if successful, false otherwise
     */
    public boolean markMessagesAsRead(int fromUserId, int toUserId) throws SQLException {
        String query = "UPDATE message SET is_read = true WHERE author_id = ? AND recipient_id = ? AND is_read = false";
        
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, fromUserId);
            statement.setInt(2, toUserId);
            
            int rows = statement.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error marking messages as read: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get the count of unread messages from a specific user
     * @param fromUserId The sender user ID
     * @param toUserId The recipient user ID (current user)
     * @return The count of unread messages
     */
    public int getUnreadMessageCount(int fromUserId, int toUserId) throws SQLException {
        String query = "SELECT COUNT(*) FROM message WHERE author_id = ? AND recipient_id = ? AND is_read = false";
        
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, fromUserId);
            statement.setInt(2, toUserId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Get the last message between two users
     * @param userId1 First user ID
     * @param userId2 Second user ID
     * @return The last message or null if no messages
     */
    public Message getLastMessage(int userId1, int userId2) throws SQLException {
        String query = "SELECT * FROM message WHERE (author_id = ? AND recipient_id = ?) OR (author_id = ? AND recipient_id = ?) ORDER BY created_at DESC LIMIT 1";
        
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId1);
            statement.setInt(2, userId2);
            statement.setInt(3, userId2);
            statement.setInt(4, userId1);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractMessageFromResultSet(resultSet);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Helper method to extract a Message object from a ResultSet
     */
    private Message extractMessageFromResultSet(ResultSet resultSet) throws SQLException {
        Message message = new Message();
        message.setId(resultSet.getInt("id"));
        message.setAuthorId(resultSet.getInt("author_id"));
        message.setRecipientId(resultSet.getInt("recipient_id"));
        message.setContent(resultSet.getString("content"));
        
        // Convert Timestamp to LocalDateTime
        Timestamp timestamp = resultSet.getTimestamp("created_at");
        if (timestamp != null) {
            message.setCreatedAt(timestamp.toLocalDateTime());
        } else {
            message.setCreatedAt(LocalDateTime.now());
        }
        
        message.setIsRead(resultSet.getBoolean("is_read"));
        
        return message;
    }
}
