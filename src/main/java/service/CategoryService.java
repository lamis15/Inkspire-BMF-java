package service;

import entities.Category;
import utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryService {
    private final Connection conn;

    public CategoryService() {
        conn = DataSource.getInstance().getConnection();  // Changé de getCnx() à getConnection()
    }
    public List<Category> getCategoriesForEvent(int eventId) throws SQLException {
        System.out.println("DEBUG: Querying categories for event " + eventId);

        String sql = "SELECT c.* FROM category c " +
                "JOIN event_category ec ON c.id = ec.category_id " +
                "WHERE ec.event_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, eventId);
            ResultSet rs = stmt.executeQuery();

            List<Category> categories = new ArrayList<>();
            while (rs.next()) {
                Category cat = new Category();
                cat.setId(rs.getInt("id"));
                cat.setName(rs.getString("name"));
                categories.add(cat);
                System.out.println("DEBUG: Found category: " + cat.getName());
            }
            return categories;
        }
    }

    public void ajouter(Category category) throws SQLException {
        String query = "INSERT INTO category (name, description, statut) VALUES (?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, category.getName());
            pst.setString(2, category.getDescription());
            pst.setString(3, category.getStatut());
            pst.executeUpdate();
        }
    }

    public void modifier(Category category) throws SQLException {
        String query = "UPDATE category SET name=?, description=?, statut=? WHERE id=?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, category.getName());
            pst.setString(2, category.getDescription());
            pst.setString(3, category.getStatut());
            pst.setInt(4, category.getId());
            pst.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        // Supprimer d'abord les événements associés à la catégorie
        String queryDeleteEvents = "DELETE FROM event WHERE category_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(queryDeleteEvents)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }

        // Ensuite, supprimer la catégorie
        String queryDeleteCategory = "DELETE FROM category WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(queryDeleteCategory)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }


    public List<Category> afficher() throws SQLException {
        List<Category> categories = new ArrayList<>();
        String query = "SELECT * FROM category";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                Category category = new Category(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("statut")
                );
                categories.add(category);
            }
        }
        return categories;
    }

    public Category getById(int id) throws SQLException {
        String query = "SELECT * FROM category WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new Category(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("statut")
                    );
                }
            }
        }
        return null;
    }
}