package entities;

public class Artworklike {

    private Artwork artwork;
    private User user;

    public Artworklike() {}

    public Artworklike(Artwork artwork, User user) {
        this.artwork = artwork;
        this.user = user;
    }

    public Artwork getArtwork() {
        return artwork;
    }

    public void setArtwork(Artwork artwork) {
        this.artwork = artwork;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "ArtworkLike{" +
                "artwork=" + artwork +
                ", user=" + user +
                '}';
    }
}
