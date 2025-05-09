package entities;

public class Comment {
    private int id;
    private User user;
    private Artwork artwork;
    private String content;

    // Default constructor
    public Comment() {
    }

    public Artwork getArtwork() {
        return artwork;
    }

    public void setArtwork(Artwork artwork) {
        this.artwork = artwork;
    }


    public Comment(User user, Artwork artwork, String content) {
        this.user = user;
        this.artwork= artwork;
        this.content = content;
    }

    public Comment(int id, User user, Artwork artwork, String content) {
        this.id = id;
        this.user = user;
        this.artwork = artwork;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        this.user = user;
    }




    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }
        this.content = content;
    }

    @Override
    public String toString() {
        return "Comment{id=" + id + ", user=" + user.getFirstName() + ", artworkId=" + artwork.getId() + ", content='" + content + "'}";
    }
}
