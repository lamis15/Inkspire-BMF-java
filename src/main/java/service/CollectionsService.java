package service;

import entities.Collections;
import entities.User;
import utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CollectionsService implements IService<Collections> {

    private Connection connection;

    public CollectionsService() {
        connection = DataSource.getInstance().getConnection();
    }

    @Override
    public boolean ajouter(Collections collection) throws SQLException {
        String sql = "INSERT INTO collections (title, creation_date, image, description, goal, status, user_id, current_amount) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, collection.getTitle());
        preparedStatement.setTimestamp(2, Timestamp.valueOf(collection.getCreationDate()));
        preparedStatement.setString(3, collection.getImage());
        preparedStatement.setString(4, collection.getDescription());
        
        // Handle null goal values
        if (collection.getGoal() != null) {
            preparedStatement.setDouble(5, collection.getGoal());
        } else {
            preparedStatement.setNull(5, Types.DOUBLE);
        }
        
        preparedStatement.setString(6, collection.getStatus());
        preparedStatement.setInt(7, collection.getUser().getId());
        preparedStatement.setDouble(8, collection.getCurrentAmount());

        preparedStatement.executeUpdate();
        return false;
    }

    @Override
    public boolean modifier(Collections collection) throws SQLException {
        String sql = "UPDATE collections SET title=?, creation_date=?, image=?, description=?, goal=?, status=?, user_id=?, current_amount=? WHERE id=?";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, collection.getTitle());
        preparedStatement.setTimestamp(2, Timestamp.valueOf(collection.getCreationDate()));
        preparedStatement.setString(3, collection.getImage());
        preparedStatement.setString(4, collection.getDescription());
        
        // Handle null goal values
        if (collection.getGoal() != null) {
            preparedStatement.setDouble(5, collection.getGoal());
        } else {
            preparedStatement.setNull(5, Types.DOUBLE);
        }
        
        preparedStatement.setString(6, collection.getStatus());
        preparedStatement.setInt(7, collection.getUser().getId());
        preparedStatement.setDouble(8, collection.getCurrentAmount());
        preparedStatement.setInt(9, collection.getId());

        preparedStatement.executeUpdate();
        return false;
    }

    @Override
    public boolean supprimer(int id) throws SQLException {
        String sql = "DELETE FROM collections WHERE id=?";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, id);
        preparedStatement.executeUpdate();
        return false;
    }

    @Override
    public List<Collections> recuperer() throws SQLException {
        String sql = "SELECT * FROM collections";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        List<Collections> list = new ArrayList<>();

        while (rs.next()) {
            Collections c = new Collections();
            c.setId(rs.getInt("id"));
            c.setTitle(rs.getString("title"));
            c.setCreationDate(rs.getTimestamp("creation_date").toLocalDateTime());
            c.setImage(rs.getString("image"));
            c.setDescription(rs.getString("description"));

            // Handle null goal values in the result set
            double goal = rs.getDouble("goal");
            if (rs.wasNull()) {
                c.setGoal(null);
            } else {
                c.setGoal(goal);
            }

            c.setStatus(rs.getString("status"));

            User user = new User();
            user.setId(rs.getInt("user_id"));
            c.setUser(user);

            c.setCurrentAmount(rs.getDouble("current_amount"));

            list.add(c);
        }
        return list;
    }
}
