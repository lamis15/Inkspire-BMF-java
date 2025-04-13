package entities;

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
    private String status = null;
    private Double currentAmount = 0.0;

    private User user = null;
    private List<Donation> donations = new ArrayList<>();
    private List<Artwork> artworks = new ArrayList<>();

    public Collections() {}

    public Collections(String title, String description, Double goal, String status, User user) {
        this.title = title;
        this.description = description;
        this.goal = goal;
        this.status = status;
        this.user = user;
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
    public void setGoal(Double goal) { this.goal = goal; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(Double currentAmount) { this.currentAmount = currentAmount; }

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
