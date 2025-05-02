package service;

import entities.Collections;
import entities.Donation;
import entities.User;
import utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DonationService implements IService<Donation> {

    private Connection connection;

    public DonationService() {
        connection = DataSource.getInstance().getConnection();
    }

    @Override
    public boolean ajouter(Donation donation) throws SQLException {
        String sql = "INSERT INTO donation (date, amount, collections_id, user_id) VALUES (?, ?, ?, ?)";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setTimestamp(1, Timestamp.valueOf(donation.getDate()));
        preparedStatement.setDouble(2, donation.getAmount());
        preparedStatement.setInt(3, donation.getCollections().getId());
        preparedStatement.setInt(4, donation.getUser().getId());

        int rowsInserted = preparedStatement.executeUpdate();
        return rowsInserted > 0;
    }


    @Override
    public boolean modifier(Donation donation) throws SQLException {
        String sql = "UPDATE donation SET date=?, amount=?, collections_id=?, user_id=? WHERE id=?";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setTimestamp(1, Timestamp.valueOf(donation.getDate()));
        preparedStatement.setDouble(2, donation.getAmount());
        preparedStatement.setInt(3, donation.getCollections().getId());
        preparedStatement.setInt(4, donation.getUser().getId());
        preparedStatement.setInt(5, donation.getId());

        int rowsUpdated = preparedStatement.executeUpdate();
        return rowsUpdated > 0;
    }


    @Override
    public boolean supprimer(int id) throws SQLException {
        String sql = "DELETE FROM donation WHERE id=?";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, id);

        int rowsDeleted = preparedStatement.executeUpdate();
        return rowsDeleted > 0;
    }


    @Override
    public List<Donation> recuperer() throws SQLException {
        String sql = "SELECT d.id, d.date, d.amount, d.collections_id, d.user_id, c.title as collection_title, " +
                "u.first_name, u.last_name FROM donation d " +
                "JOIN collections c ON d.collections_id = c.id " +
                "JOIN user u ON d.user_id = u.id";
        Statement statement = connection.createStatement();

        ResultSet rs = statement.executeQuery(sql);
        List<Donation> list = new ArrayList<>();

        while (rs.next()) {
            Donation d = new Donation();
            d.setId(rs.getInt("id"));
            d.setDate(rs.getTimestamp("date").toLocalDateTime());
            d.setAmount(rs.getDouble("amount"));

            // Set collection
            Collections collection = new Collections();
            collection.setId(rs.getInt("collections_id"));
            collection.setTitle(rs.getString("collection_title"));
            d.setCollections(collection);

            // Set user with first and last name
            User user = new User();
            user.setId(rs.getInt("user_id"));
            user.setFirstName(rs.getString("first_name"));
            user.setLastName(rs.getString("last_name"));
            d.setUser(user);

            list.add(d);
        }
        return list;
    }

    /**
     * Get donations by collection ID
     * @param collectionId The collection ID to filter by
     * @return List of donations for the collection
     */
    public List<Donation> getDonationsByCollectionId(int collectionId) throws SQLException {
        String sql = "SELECT d.id, d.date, d.amount, d.collections_id, d.user_id, u.first_name, u.last_name FROM donation d " +
                "JOIN user u ON d.user_id = u.id " +
                "WHERE d.collections_id = ?";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, collectionId);

        ResultSet rs = preparedStatement.executeQuery();
        List<Donation> list = new ArrayList<>();

        while (rs.next()) {
            Donation d = new Donation();
            d.setId(rs.getInt("id"));
            d.setDate(rs.getTimestamp("date").toLocalDateTime());
            d.setAmount(rs.getDouble("amount"));

            // Set collection
            Collections collection = new Collections();
            collection.setId(collectionId);
            d.setCollections(collection);

            // Set user
            User user = new User();
            user.setId(rs.getInt("user_id"));
            user.setFirstName(rs.getString("first_name"));
            user.setLastName(rs.getString("last_name"));
            d.setUser(user);

            list.add(d);
        }
        return list;
    }

    /**
     * Get donations by user ID
     * @param userId The user ID to filter by
     * @return List of donations made by the user
     */
    public List<Donation> getDonationsByUserId(int userId) throws SQLException {
        String sql = "SELECT d.id, d.date, d.amount, d.collections_id, d.user_id, c.title as collection_title FROM donation d " +
                "JOIN collections c ON d.collections_id = c.id " +
                "WHERE d.user_id = ?";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, userId);

        ResultSet rs = preparedStatement.executeQuery();
        List<Donation> list = new ArrayList<>();

        while (rs.next()) {
            Donation d = new Donation();
            d.setId(rs.getInt("id"));
            d.setDate(rs.getTimestamp("date").toLocalDateTime());
            d.setAmount(rs.getDouble("amount"));

            // Set collection
            Collections collection = new Collections();
            collection.setId(rs.getInt("collections_id"));
            collection.setTitle(rs.getString("collection_title"));
            d.setCollections(collection);

            // Set user
            User user = new User();
            user.setId(userId);
            d.setUser(user);

            list.add(d);
        }
        return list;
    }

    /**
     * Calculate total donations for a collection
     * @param collectionId The collection ID
     * @return Total amount donated
     */
    public double getTotalDonationsForCollection(int collectionId) throws SQLException {
        String sql = "SELECT SUM(amount) as total FROM donation WHERE collections_id = ?";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, collectionId);

        ResultSet rs = preparedStatement.executeQuery();
        if (rs.next()) {
            return rs.getDouble("total");
        }
        return 0.0;
    }

    /**
     * Filter donations based on search text
     * 
     * @param donations The list of donations to filter
     * @param searchText The text to search for
     * @return Filtered list of donations
     */
    public List<Donation> filterDonations(List<Donation> donations, String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            // If search text is empty, return all donations
            return new ArrayList<>(donations);
        }
        
        // Convert search text to lowercase for case-insensitive search
        String searchLower = searchText.toLowerCase();
        
        // Filter donations based on amount, collection title, or donor name
        return donations.stream()
                .filter(d -> 
                    (String.valueOf(d.getAmount()).contains(searchLower)) ||
                    (d.getCollections() != null && d.getCollections().getTitle() != null && 
                     d.getCollections().getTitle().toLowerCase().contains(searchLower)) ||
                    (d.getUser() != null && 
                        ((d.getUser().getFirstName() != null && d.getUser().getFirstName().toLowerCase().contains(searchLower)) ||
                         (d.getUser().getLastName() != null && d.getUser().getLastName().toLowerCase().contains(searchLower))))
                )
                .collect(Collectors.toList());
    }
    
    /**
     * Sort donations based on the specified sort option
     * 
     * @param donations The list of donations to sort
     * @param sortOption The sort option to apply
     * @return Sorted list of donations
     */
    public List<Donation> sortDonations(List<Donation> donations, String sortOption) {
        if (sortOption == null || donations.isEmpty()) {
            // Nothing to sort
            return donations;
        }
        
        List<Donation> sortedList = new ArrayList<>(donations);
        
        // Apply the appropriate sort based on the selected option
        switch (sortOption) {
            case "Newest":
                sortedList.sort(Comparator.comparing((Donation d) -> d.getDate()).reversed());
                break;
                
            case "Oldest":
                sortedList.sort(Comparator.comparing((Donation d) -> d.getDate()));
                break;
                
            case "Amount ↑":
                sortedList.sort(Comparator.comparing((Donation d) -> d.getAmount()).reversed());
                break;
                
            case "Amount ↓":
                sortedList.sort(Comparator.comparing((Donation d) -> d.getAmount()));
                break;
                
            case "Collection A-Z":
                sortedList.sort(Comparator.comparing((Donation d) -> 
                    d.getCollections() != null && d.getCollections().getTitle() != null ? 
                    d.getCollections().getTitle().toLowerCase() : ""));
                break;
                
            case "Collection Z-A":
                sortedList.sort(Comparator.comparing((Donation d) -> 
                    d.getCollections() != null && d.getCollections().getTitle() != null ? 
                    d.getCollections().getTitle().toLowerCase() : "").reversed());
                break;
                
            case "Donor A-Z":
                sortedList.sort(Comparator.comparing((Donation d) -> {
                    if (d.getUser() == null) return "";
                    String firstName = d.getUser().getFirstName() != null ? d.getUser().getFirstName() : "";
                    String lastName = d.getUser().getLastName() != null ? d.getUser().getLastName() : "";
                    return (firstName + " " + lastName).toLowerCase();
                }));
                break;
                
            case "Donor Z-A":
                sortedList.sort(Comparator.comparing((Donation d) -> {
                    if (d.getUser() == null) return "";
                    String firstName = d.getUser().getFirstName() != null ? d.getUser().getFirstName() : "";
                    String lastName = d.getUser().getLastName() != null ? d.getUser().getLastName() : "";
                    return (firstName + " " + lastName).toLowerCase();
                }).reversed());
                break;
                
            default:
                // Default sort by newest first
                sortedList.sort(Comparator.comparing((Donation d) -> d.getDate()).reversed());
                break;
        }
        
        return sortedList;
    }
    
    /**
     * Filter and sort donations in one operation
     * 
     * @param donations The list of donations to process
     * @param searchText The text to search for
     * @param sortOption The sort option to apply
     * @return Filtered and sorted list of donations
     */
    public List<Donation> filterAndSortDonations(List<Donation> donations, String searchText, String sortOption) {
        // First filter
        List<Donation> filtered = filterDonations(donations, searchText);
        
        // Then sort
        return sortDonations(filtered, sortOption);
    }
}
