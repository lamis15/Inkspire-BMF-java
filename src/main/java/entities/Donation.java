package entities;

import java.time.LocalDateTime;
import java.util.Objects;

public class Donation {

    private Integer id = null;
    private LocalDateTime date = LocalDateTime.now();
    private Double amount = null;

    private Collections collections = null;
    private User user = null;

    public Donation() {}

    public Donation(LocalDateTime date, Double amount, Collections collections, User user) {
        this.date = date;
        this.amount = amount;
        this.collections = collections;
        this.user = user;
    }

    public Donation(Double amount, Collections collections, User user) {
        this.amount = amount;
        this.collections = collections;
        this.user = user;
        this.date = LocalDateTime.now();
    }

    // Getters et Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Collections getCollections() {
        return collections;
    }

    public void setCollections(Collections collections) {
        this.collections = collections;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Donation donation)) return false;
        return Objects.equals(id, donation.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Donation{" +
                "id=" + id +
                ", date=" + date +
                ", amount=" + amount +
                ", collections=" + (collections != null ? collections.getId() : null) +
                ", user=" + (user != null ? user.getId() : null) +
                '}';
    }
}
