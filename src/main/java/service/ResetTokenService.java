package service;

import entities.ResetToken;
import utils.DataSource;

import java.sql.*;
import java.time.LocalDateTime;



public class ResetTokenService {
    private Connection connection;
    public ResetTokenService() {
        connection = DataSource.getInstance().getConnection();
    }

    public void CreateToken(Integer userid, String token, LocalDateTime requested_at, LocalDateTime expires_at) {
        String query = "INSERT INTO reset_password_request(user_id, selector, requested_at, expires_at) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, userid);
            ps.setString(2, token);
            ps.setObject(3, requested_at);
            ps.setObject(4, expires_at);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
