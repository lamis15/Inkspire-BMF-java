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
        String sql = "INSERT INTO event (title, starting_date, ending_date, location, latitude, longitude, image) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, event.getTitle());
        ps.setTimestamp(2, Timestamp.valueOf(event.getStartingDate().atStartOfDay()));
        ps.setTimestamp(3, Timestamp.valueOf(event.getEndingDate().atStartOfDay()));
        ps.setString(4, event.getLocation());
        ps.setDouble(5, event.getLatitude());
        ps.setDouble(6, event.getLongitude());
        ps.setString(7, event.getImage());
        ps.executeUpdate();
        return true;
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




    @Override
    public List<Event> recuperer() throws SQLException {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM event";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);

        logger.log(Level.INFO, "Début de la récupération des événements");

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
                logger.log(Level.INFO, "Événement récupéré: " + event.getTitle());
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Erreur lors de la récupération d'un événement", e);
            }
        }

        logger.log(Level.INFO, "Nombre d'événements récupérés: " + list.size());
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
