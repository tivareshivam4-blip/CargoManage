package cargomanage.dao;

import cargomanage.model.AuditLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditDAO {

    public void log(String username, String action, String details) {
        String sql = "INSERT INTO audit_logs (username, action, details, timestamp) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username != null ? username : "SYSTEM");
            ps.setString(2, action);
            ps.setString(3, details);
            ps.setString(4, DatabaseManager.now());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<AuditLog> findAll(int limit) {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs ORDER BY id DESC LIMIT ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit > 0 ? limit : 200);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new AuditLog(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("action"),
                            rs.getString("details"),
                            rs.getString("timestamp")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<AuditLog> search(String query, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return findAll(limit);
        }
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs WHERE username LIKE ? OR action LIKE ? OR details LIKE ? ORDER BY id DESC LIMIT ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String p = "%" + query.trim() + "%";
            ps.setString(1, p);
            ps.setString(2, p);
            ps.setString(3, p);
            ps.setInt(4, limit > 0 ? limit : 200);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new AuditLog(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("action"),
                            rs.getString("details"),
                            rs.getString("timestamp")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}

