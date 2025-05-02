package entities;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity class representing a message in the system.
 * Maps to the message database table.
 */
public class Message {

    private Integer id = null;
    private Integer authorId = null;
    private Integer recipientId = null;
    private String content = null;
    private LocalDateTime createdAt = null;
    private Boolean isRead = false;
    
    // User objects for relationships
    private User author;
    private User recipient;

    /**
     * Default constructor
     */
    public Message() {}

    /**
     * Constructor with essential fields
     */
    public Message(Integer authorId, Integer recipientId, String content) {
        this.authorId = authorId;
        this.recipientId = recipientId;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }

    /**
     * Full constructor
     */
    public Message(Integer id, Integer authorId, Integer recipientId, String content, 
                  LocalDateTime createdAt, Boolean isRead) {
        this.id = id;
        this.authorId = authorId;
        this.recipientId = recipientId;
        this.content = content;
        this.createdAt = createdAt;
        this.isRead = isRead;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getAuthorId() { return authorId; }
    public void setAuthorId(Integer authorId) { this.authorId = authorId; }

    public Integer getRecipientId() { return recipientId; }
    public void setRecipientId(Integer recipientId) { this.recipientId = recipientId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    // Relationship getters and setters
    public User getAuthor() { return author; }
    public void setAuthor(User author) { 
        this.author = author;
        if (author != null) {
            this.authorId = author.getId();
        }
    }

    public User getRecipient() { return recipient; }
    public void setRecipient(User recipient) { 
        this.recipient = recipient;
        if (recipient != null) {
            this.recipientId = recipient.getId();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(id, message.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", authorId=" + authorId +
                ", recipientId=" + recipientId +
                ", content='" + (content != null ? content.substring(0, Math.min(content.length(), 20)) + "..." : null) + '\'' +
                ", createdAt=" + createdAt +
                ", isRead=" + isRead +
                '}';
    }
}
