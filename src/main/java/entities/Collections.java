package entities;

import enums.CollectionStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Collections {

    private Integer id = null;
    private String title = null;
    private LocalDateTime creationDate = LocalDateTime.now();
    private String image = null;
    private String description = null;
    private Double goal = null;
    private CollectionStatus status = CollectionStatus.NO_GOAL;
    private Double currentAmount = 0.0;

    private User user = null;
    private List<Donation> donations = new ArrayList<>();
    private List<Artwork> artworks = new ArrayList<>();

    public Collections() {}

    public Collections(String title, String description, Double goal, CollectionStatus status, User user) {
        this.title = title;
        this.description = description;
        this.goal = goal;
        this.status = status;
        this.user = user;
    }
    
    /**
     * Constructor that automatically determines the status based on goal and current amount
     */
    public Collections(String title, String description, Double goal, User user) {
        this.title = title;
        this.description = description;
        this.goal = goal;
        this.user = user;
        this.status = CollectionStatus.determineStatus(goal, currentAmount);
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getGoal() { return goal; }
    public void setGoal(Double goal) { 
        this.goal = goal; 
        // Update status when goal changes
        this.status = CollectionStatus.determineStatus(goal, currentAmount);
    }

    public CollectionStatus getStatus() { return status; }
    public void setStatus(CollectionStatus status) { this.status = status; }
    
    /**
     * Get the string representation of the status
     * @return The string value of the status
     */
    public String getStatusValue() { 
        return status != null ? status.getValue() : CollectionStatus.NO_GOAL.getValue(); 
    }
    
    /**
     * Set status from a string value
     * @param statusValue The string value of the status
     */
    public void setStatusFromString(String statusValue) { 
        this.status = CollectionStatus.fromString(statusValue); 
    }
    
    /**
     * Update the status based on the current goal and amount
     */
    public void updateStatus() {
        this.status = CollectionStatus.determineStatus(goal, currentAmount);
    }

    public Double getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(Double currentAmount) { 
        this.currentAmount = currentAmount; 
        // Update status when current amount changes
        this.status = CollectionStatus.determineStatus(goal, currentAmount);
    }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<Donation> getDonations() { return donations; }
    public void setDonations(List<Donation> donations) { this.donations = donations; }

    public List<Artwork> getArtworks() { return artworks; }
    public void setArtworks(List<Artwork> artworks) { this.artworks = artworks; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Collections that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
