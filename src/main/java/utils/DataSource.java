package utils;
import java.sql.*;

import static java.lang.Class.forName;

public class DataSource {
    final String URL = "jdbc:mysql://127.0.0.1:3306/crud";
    final String USER = "root";
    final String PASS = "";
    private Connection connection;
    private static DataSource instance;

    private DataSource() {
        try {
            // Modern JDBC drivers auto-register, no need for Class.forName(
            connection = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Connected to database successfully");
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }

    public static DataSource getInstance() {
        if (instance == null) {
            instance = new DataSource();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}