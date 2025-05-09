package entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Artwork {

    private Integer id = null;
    private String name = null;
    private String theme = null;
    private String description = null;
    private String picture = null;
    private Boolean status = null;

    private User user = null;
    /*private List<Comment> comments = new ArrayList<>();
    private List<ArtworkLike> likes = new ArrayList<>();*/
    private List<Collections> collections = new ArrayList<>();

    public Artwork() {}

    public Artwork(String name, String theme, String description, String picture, Boolean status, User user) {
        this.name = name;
        this.theme = theme;
        this.description = description;
        this.picture = picture;
        this.status = status;
        this.user = user;
    }


    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPicture() { return picture; }
    public void setPicture(String picture) { this.picture = picture; }

    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    /*public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }

    public List<ArtworkLike> getLikes() { return likes; }
    public void setLikes(List<ArtworkLike> likes) { this.likes = likes; }*/

    public List<Collections> getCollections() { return collections; }
    public void setCollections(List<Collections> collections) { this.collections = collections; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Artwork artwork)) return false;
        return Objects.equals(id, artwork.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


}
