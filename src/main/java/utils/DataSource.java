package utils;
import java.sql.*;

public class DataSource {
    final String URL = "jdbc:mysql://127.0.0.1:3306/inktest";
    final String USER="root";
    final String PASS="";
    private Connection connection;
    private static DataSource instance;

    private DataSource(){
        try {
            connection = DriverManager.getConnection(URL,USER,PASS);
            System.out.println("Connected");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    public static DataSource getInstance() {
        if (instance == null)
            instance = new DataSource();
        return instance;

    }
    public Connection getConnection(){
        return connection;
    }


}
