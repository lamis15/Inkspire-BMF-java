package entities;

public class Category {
    private int id;
    private String name;
    private String description;
    private String statut;

    // Constructeur par défaut
    public Category() {
    }

    // Constructeur sans id (pour l'insertion)
    public Category(String name, String description, String statut) {
        this.name = name;
        this.description = description;
        this.statut = statut;
    }

    // Constructeur complet
    public Category(int id, String name, String description, String statut) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.statut = statut;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", statut='" + statut + '\'' +
                '}';
    }
}