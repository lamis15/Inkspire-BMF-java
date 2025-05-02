package service;

import entities.Collections;
import entities.User;
import enums.CollectionStatus;
import utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

        preparedStatement.setString(6, collection.getStatusValue());
        if (collection.getUser() == null) {
            throw new IllegalArgumentException("Collection has no associated user (user is null)");
        }
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

        preparedStatement.setString(6, collection.getStatusValue());
        if (collection.getUser() == null) {
            throw new IllegalArgumentException("Collection has no associated user (user is null)");
        }
        preparedStatement.setInt(7, collection.getUser().getId());
        preparedStatement.setDouble(8, collection.getCurrentAmount());
        preparedStatement.setInt(9, collection.getId());

        int rowsAffected = preparedStatement.executeUpdate();
        return rowsAffected > 0;
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
        String sql = "SELECT c.*, u.first_name, u.last_name FROM collections c JOIN user u ON c.user_id = u.id";
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

            c.setStatusFromString(rs.getString("status"));

            // Create user with first and last name
            User user = new User();
            user.setId(rs.getInt("user_id"));
            user.setFirstName(rs.getString("first_name"));
            user.setLastName(rs.getString("last_name"));
            c.setUser(user);

            c.setCurrentAmount(rs.getDouble("current_amount"));

            list.add(c);
        }
        return list;
    }

    /**
     * Retrieve a single collection by ID with complete user details
     */
    public Collections recupererById(int id) throws SQLException {
        String sql = "SELECT c.*, u.first_name, u.last_name FROM collections c JOIN user u ON c.user_id = u.id WHERE c.id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, id);

        ResultSet rs = preparedStatement.executeQuery();

        if (rs.next()) {
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

            c.setStatusFromString(rs.getString("status"));

            // Create user with first and last name
            User user = new User();
            user.setId(rs.getInt("user_id"));
            user.setFirstName(rs.getString("first_name"));
            user.setLastName(rs.getString("last_name"));
            c.setUser(user);

            c.setCurrentAmount(rs.getDouble("current_amount"));

            return c;
        }

        return null;
    }

    /**
     * Get the most recently created collection by user ID and title
     * This is useful after creating a new collection to get its ID
     * @param userId The user ID who owns the collection
     * @param title The title of the collection
     * @return The most recently created collection matching the criteria, or null if not found
     */
    public Collections getRecentlyCreatedCollection(int userId, String title) throws SQLException {
        String sql = "SELECT c.*, u.first_name, u.last_name FROM collections c JOIN user u ON c.user_id = u.id " +
                "WHERE c.user_id = ? AND c.title = ? ORDER BY c.creation_date DESC LIMIT 1";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, userId);
        preparedStatement.setString(2, title);

        ResultSet rs = preparedStatement.executeQuery();

        if (rs.next()) {
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

            c.setStatusFromString(rs.getString("status"));

            // Create user with first and last name
            User user = new User();
            user.setId(rs.getInt("user_id"));
            user.setFirstName(rs.getString("first_name"));
            user.setLastName(rs.getString("last_name"));
            c.setUser(user);

            c.setCurrentAmount(rs.getDouble("current_amount"));

            return c;
        }

        return null;
    }

    /**
     * Filter collections based on search text
     * 
     * @param collections The list of collections to filter
     * @param searchText The text to search for
     * @return Filtered list of collections
     */
    public List<Collections> filterCollections(List<Collections> collections, String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            // If search text is empty, return all collections
            return new ArrayList<>(collections);
        }
        
        // Convert search text to lowercase for case-insensitive search
        String searchLower = searchText.toLowerCase();
        
        // Filter collections based on title, description, owner name, status, or goal
        return collections.stream()
                .filter(c -> 
                    (c.getTitle() != null && c.getTitle().toLowerCase().contains(searchLower)) ||
                    (c.getDescription() != null && c.getDescription().toLowerCase().contains(searchLower)) ||
                    (c.getUser() != null && 
                        ((c.getUser().getFirstName() != null && c.getUser().getFirstName().toLowerCase().contains(searchLower)) ||
                         (c.getUser().getLastName() != null && c.getUser().getLastName().toLowerCase().contains(searchLower)))) ||
                    (c.getStatusValue() != null && c.getStatusValue().toLowerCase().contains(searchLower)) ||
                    (c.getGoal() != null && c.getGoal().toString().contains(searchLower))
                )
                .collect(Collectors.toList());
    }
    
    /**
     * Sort collections based on the specified sort option
     * 
     * @param collections The list of collections to sort
     * @param sortOption The sort option to apply
     * @return Sorted list of collections
     */
    public List<Collections> sortCollections(List<Collections> collections, String sortOption) {
        if (sortOption == null || collections.isEmpty()) {
            // Nothing to sort
            return collections;
        }
        
        List<Collections> sortedList = new ArrayList<>(collections);
        
        // Apply the appropriate sort based on the selected option
        switch (sortOption) {
            case "A-Z":
                sortedList.sort(Comparator.comparing((Collections c) -> c.getTitle() != null ? c.getTitle().toLowerCase() : ""));
                break;
                
            case "Z-A":
                sortedList.sort(Comparator.comparing((Collections c) -> c.getTitle() != null ? c.getTitle().toLowerCase() : "").reversed());
                break;
                
            case "Newest":
                sortedList.sort(Comparator.comparing(Collections::getCreationDate).reversed());
                break;
                
            case "Oldest":
                sortedList.sort(Comparator.comparing(Collections::getCreationDate));
                break;
                
            case "Goal ↑":
                sortedList.sort(Comparator.comparing((Collections c) -> c.getGoal() != null ? c.getGoal() : 0.0).reversed());
                break;
                
            case "Goal ↓":
                sortedList.sort(Comparator.comparing((Collections c) -> c.getGoal() != null ? c.getGoal() : Double.MAX_VALUE));
                break;
                
            case "Amount ↑":
                sortedList.sort(Comparator.comparing((Collections c) -> c.getCurrentAmount() != null ? c.getCurrentAmount() : 0.0).reversed());
                break;
                
            case "Amount ↓":
                sortedList.sort(Comparator.comparing((Collections c) -> c.getCurrentAmount() != null ? c.getCurrentAmount() : Double.MAX_VALUE));
                break;
                
            case "Status":
                // Sort by status priority: REACHED > IN_PROGRESS > NO_GOAL
                sortedList.sort((Collections c1, Collections c2) -> {
                    if (c1.getStatus() == null) return 1;
                    if (c2.getStatus() == null) return -1;
                    
                    // Define status priority
                    int priority1 = getStatusPriority(c1.getStatus());
                    int priority2 = getStatusPriority(c2.getStatus());
                    
                    return Integer.compare(priority1, priority2);
                });
                break;
                
            default:
                // Default sort by newest first
                sortedList.sort(Comparator.comparing(Collections::getCreationDate).reversed());
                break;
        }
        
        return sortedList;
    }
    
    /**
     * Get the priority value for a collection status
     * @param status The collection status
     * @return The priority value (lower is higher priority)
     */
    private int getStatusPriority(CollectionStatus status) {
        if (status == CollectionStatus.REACHED) return 1;
        if (status == CollectionStatus.IN_PROGRESS) return 2;
        return 3; // NO_GOAL
    }
    
    /**
     * Filter and sort collections in one operation
     * 
     * @param collections The list of collections to process
     * @param searchText The text to search for
     * @param sortOption The sort option to apply
     * @return Filtered and sorted list of collections
     */
    public List<Collections> filterAndSortCollections(List<Collections> collections, String searchText, String sortOption) {
        // First filter
        List<Collections> filtered = filterCollections(collections, searchText);
        
        // Then sort
        return sortCollections(filtered, sortOption);
    }
    
    /**
     * Get collections belonging to a specific user
     * 
     * @param collections The list of all collections
     * @param userId The ID of the user
     * @return List of collections belonging to the user
     */
    public List<Collections> getUserCollections(List<Collections> collections, int userId) {
        return collections.stream()
                .filter(c -> c.getUser() != null && c.getUser().getId() == userId)
                .collect(Collectors.toList());
    }
}
