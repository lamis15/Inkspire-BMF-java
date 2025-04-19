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
        // Start transaction
        connection.setAutoCommit(false);

        try {
            // 1. Insert the event
            String eventSql = "INSERT INTO event (title, starting_date, ending_date, location, latitude, longitude, image) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = connection.prepareStatement(eventSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, event.getTitle());
                ps.setTimestamp(2, Timestamp.valueOf(event.getStartingDate().atStartOfDay()));
                ps.setTimestamp(3, Timestamp.valueOf(event.getEndingDate().atStartOfDay()));
                ps.setString(4, event.getLocation());
                ps.setDouble(5, event.getLatitude());
                ps.setDouble(6, event.getLongitude());
                ps.setString(7, event.getImage());
                ps.executeUpdate();

                // 2. Get generated event ID
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int eventId = generatedKeys.getInt(1);

                        // 3. Save category association if exists
                        if (event.getCategoryId() > 0) {
                            String assocSql = "INSERT INTO event_category (event_id, category_id) VALUES (?, ?)";
                            try (PreparedStatement assocPs = connection.prepareStatement(assocSql)) {
                                assocPs.setInt(1, eventId);
                                assocPs.setInt(2, event.getCategoryId());
                                assocPs.executeUpdate();
                                System.out.println("Saved association: event " + eventId + " -> category " + event.getCategoryId());
                            }
                        }

                        connection.commit();
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
        return false;
    }
    @Override
    public boolean modifier(Event event) throws SQLException {
        String sql = "UPDATE event SET title=?, starting_date=?, ending_date=?, location=?, latitude=?, longitude=?, image=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, event.getTitle());
        ps.setTimestamp(2, Timestamp.valueOf(event.getStartingDate().atStartOfDay()));
        ps.setTimestamp(3, Timestamp.valueOf(event.getEndingDate().atStartOfDay()));
        ps.setString(4, event.getLocation());
        ps.setDouble(5, event.getLatitude());
        ps.setDouble(6, event.getLongitude());
        ps.setString(7, event.getImage());
        ps.setLong(8, event.getId());
        ps.executeUpdate();
        return true;
    }

    public boolean supprimer(int id) throws SQLException {
        String sql = "DELETE FROM event WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
        return false;
    }

    public void assignCategoryToEvent(int eventId, int categoryId) throws SQLException {
        String sql = "INSERT INTO event_category (event_id, category_id) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, categoryId);
            ps.executeUpdate();
        }
    }


    @Override
    public List<Event> recuperer() throws SQLException {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM event";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            try {
                Event event = new Event();
                event.setId((int) rs.getLong("id"));
                event.setTitle(rs.getString("title"));
                event.setStartingDate(rs.getTimestamp("starting_date").toLocalDateTime().toLocalDate());
                event.setEndingDate(rs.getTimestamp("ending_date").toLocalDateTime().toLocalDate());
                event.setLocation(rs.getString("location"));
                event.setLatitude(rs.getDouble("latitude"));
                event.setLongitude(rs.getDouble("longitude"));
                event.setImage(rs.getString("image"));

                list.add(event);
            } catch (SQLException e) {
                // Tu peux garder ce log d'erreur si tu veux être informé en cas de problème
                logger.log(Level.SEVERE, "Erreur lors de la récupération d'un événement", e);
            }
        }

        return list;
    }


    public Event getById(int id) throws SQLException {
        String sql = "SELECT * FROM event WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setLong(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Event event = new Event();
            event.setId((int) rs.getLong("id"));
            event.setTitle(rs.getString("title"));
            event.setStartingDate(rs.getTimestamp("starting_date").toLocalDateTime().toLocalDate());
            event.setEndingDate(rs.getTimestamp("ending_date").toLocalDateTime().toLocalDate());
            event.setLocation(rs.getString("location"));
            event.setLatitude(rs.getDouble("latitude"));
            event.setLongitude(rs.getDouble("longitude"));
            event.setImage(rs.getString("image"));
            return event;
        }
        return null;
    }
}
