package entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User {

    private Integer id = null;
    private String firstName = null;
    private String lastName = null;
    private String address = null;
    private String email = null;
    private String password = null;
    private String bio = null;
    private Integer tokens = 0;
    private String picture = null;
    private String googleAuthenticatorSecret = null;
    private Integer role = 0;
    private Integer status = 0;
    private Integer phoneNumber = null;

    private List<Artwork> artworks = new ArrayList<>();
    /*private List<Comment> comments = new ArrayList<>();
    private List<ArtworkLike> likes = new ArrayList<>();
    private List<Event> participatedEvents = new ArrayList<>();*/

    public User() {}

    public User(String firstName, String lastName, String email, String password, Integer role, Integer status) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
    }

    public User(String lastName, String email, String password) {
    }



    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public Integer getTokens() { return tokens; }
    public void setTokens(Integer tokens) { this.tokens = tokens; }

    public String getPicture() { return picture; }
    public void setPicture(String picture) { this.picture = picture; }

    public String getGoogleAuthenticatorSecret() { return googleAuthenticatorSecret; }
    public void setGoogleAuthenticatorSecret(String googleAuthenticatorSecret) { this.googleAuthenticatorSecret = googleAuthenticatorSecret; }

    public Integer getRole() { return role; }
    public void setRole(Integer role) { this.role = role; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(Integer phoneNumber) { this.phoneNumber = phoneNumber; }

    // Relations Getters and Setters
    public List<Artwork> getArtworks() { return artworks; }
    public void setArtworks(List<Artwork> artworks) { this.artworks = artworks; }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return this.getId() == user.getId(); // or Objects.equals(this.id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    /*public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }

    public List<ArtworkLike> getLikes() { return likes; }
    public void setLikes(List<ArtworkLike> likes) { this.likes = likes; }

    public List<Event> getParticipatedEvents() { return participatedEvents; }
    public void setParticipatedEvents(List<Event> participatedEvents) { this.participatedEvents = participatedEvents; }
*/
    // equals() and hashCode()
}
