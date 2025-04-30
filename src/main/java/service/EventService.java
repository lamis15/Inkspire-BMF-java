package service;

import entities.Event;
import utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EventService implements IService<Event> {

    private final Connection connection;
    private static final Logger logger = Logger.getLogger(EventService.class.getName());

    public EventService() {
        connection = DataSource.getInstance().getConnection();
    }

    @Override
    public boolean ajouter(Event event) throws SQLException {
         String sqlEvent = "INSERT INTO event (title, starting_date, ending_date, location, latitude, longitude, image) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sqlEvent, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, event.getTitle());
            ps.setTimestamp(2, Timestamp.valueOf(event.getStartingDate().atStartOfDay()));
            ps.setTimestamp(3, Timestamp.valueOf(event.getEndingDate().atStartOfDay()));
            ps.setString(4, event.getLocation());
            ps.setDouble(5, event.getLatitude());
            ps.setDouble(6, event.getLongitude());
            ps.setString(7, event.getImage());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int eventId = rs.getInt(1);
                    String sqlEventCategory = "INSERT INTO event_category (event_id, category_id) VALUES (?, ?)";
                    try (PreparedStatement psCategory = connection.prepareStatement(sqlEventCategory)) {
                        psCategory.setInt(1, eventId);
                        psCategory.setInt(2, event.getCategoryId());
                        psCategory.executeUpdate();
                    }
                } else {
                    throw new SQLException("Échec de la récupération de l'ID de l'événement.");
                }
            }
        }
        return true;
    }

    @Override
    public boolean modifier(Event event) throws SQLException {

        String sql = "UPDATE event SET title=?, starting_date=?, ending_date=?, location=?, latitude=?, longitude=?, image=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, event.getTitle());
            ps.setTimestamp(2, Timestamp.valueOf(event.getStartingDate().atStartOfDay()));
            ps.setTimestamp(3, Timestamp.valueOf(event.getEndingDate().atStartOfDay()));
            ps.setString(4, event.getLocation());
            ps.setDouble(5, event.getLatitude());
            ps.setDouble(6, event.getLongitude());
            ps.setString(7, event.getImage());
            ps.setInt(8, event.getId());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                logger.log(Level.WARNING, "No event found with id: " + event.getId());
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean supprimer(int id) throws SQLException {
        String sqlEventCategory = "DELETE FROM event_category WHERE event_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sqlEventCategory)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }

        String sql = "DELETE FROM event WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
        return true;
    }

    @Override
    public List<Event> recuperer() throws SQLException {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT e.*, ec.category_id FROM event e LEFT JOIN event_category ec ON e.id = ec.event_id";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                try {
                    Event event = new Event();
                    event.setId(rs.getInt("id"));
                    event.setTitle(rs.getString("title"));
                    event.setStartingDate(rs.getTimestamp("starting_date").toLocalDateTime().toLocalDate());
                    event.setEndingDate(rs.getTimestamp("ending_date").toLocalDateTime().toLocalDate());
                    event.setLocation(rs.getString("location"));
                    double latitude = rs.getDouble("latitude");
                    double longitude = rs.getDouble("longitude");
                    event.setLatitude(latitude);
                    event.setLongitude(longitude);
                    event.setImage(rs.getString("image"));
                    event.setCategoryId(rs.getInt("category_id"));
                    list.add(event);
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Erreur lors de la récupération d'un événement", e);
                }
            }
        }
        return list;
    }

    public Event getEventById(int id) throws SQLException {
        String sql = "SELECT e.*, ec.category_id FROM event e LEFT JOIN event_category ec ON e.id = ec.event_id WHERE e.id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Event event = new Event();
                    event.setId(rs.getInt("id"));
                    event.setTitle(rs.getString("title"));
                    event.setStartingDate(rs.getTimestamp("starting_date").toLocalDateTime().toLocalDate());
                    event.setEndingDate(rs.getTimestamp("ending_date").toLocalDateTime().toLocalDate());
                    String location = rs.getString("location");
                    double latitude = rs.getDouble("latitude");
                    double longitude = rs.getDouble("longitude");
                    event.setLocation(location);
                    event.setLatitude(latitude);
                    event.setLongitude(longitude);
                    event.setImage(rs.getString("image"));
                    event.setCategoryId(rs.getInt("category_id"));
                    return event;
                } else {
                    logger.log(Level.WARNING, "No event found with id: " + id);
                }
            }
        }
        return null;
    }
}