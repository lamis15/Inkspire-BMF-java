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
        String query = "DELETE FROM category WHERE id=?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
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