package entities;

import java.time.LocalDate;

public class Event {
    private Integer id;
    private String title;
    private LocalDate starting_date;
    private LocalDate ending_date;
    private String location;
    private double latitude;
    private double longitude;
    private String image;
    private int categoryId;

    public Event() {
    }

    public Event(Event event) {
        this.id = event.id;
        this.title = event.title;
        this.starting_date = event.starting_date;
        this.ending_date = event.ending_date;
        this.location = event.location;
        this.latitude = event.latitude;
        this.longitude = event.longitude;
        this.image = event.image;
        this.categoryId = event.categoryId;
    }

    public Event(String title, LocalDate starting_date, LocalDate ending_date, String location, double latitude, double longitude, String image) {
        this.title = title;
        this.starting_date = starting_date;
        this.ending_date = ending_date;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.image = image;
    }

    public Event(int id, String title, LocalDate starting_date, LocalDate ending_date, String location, double latitude, double longitude, String image) {
        this.id = id;
        this.title = title;
        this.starting_date = starting_date;
        this.ending_date = ending_date;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.image = image;
    }

    // Getters and Setters
    public int getId() {
        return id != null ? id : 0;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getStartingDate() {
        return starting_date;
    }

    public void setStartingDate(LocalDate starting_date) {
        this.starting_date = starting_date;
    }

    public LocalDate getEndingDate() {
        return ending_date;
    }

    public void setEndingDate(LocalDate ending_date) {
        this.ending_date = ending_date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getStatus() {
        if (ending_date == null) {
            return "Unknown";
        }
        LocalDate today = LocalDate.now();
        return ending_date.isBefore(today) ? "Inactive" : "Active";
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", startDate=" + starting_date +
                ", endDate=" + ending_date +
                ", location='" + location + '\'' +
                ", categoryId=" + categoryId +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", image='" + image + '\'' +
                ", status='" + getStatus() + '\'' +
                '}';
    }
}