package cargomanage.dao;

import cargomanage.model.Branch;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BranchDAO {

    public List<Branch> findAll() {
        List<Branch> list = new ArrayList<>();
        String sql = "SELECT * FROM branches ORDER BY id ASC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Branch findById(int id) {
        String sql = "SELECT * FROM branches WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean create(Branch b) throws SQLException {
        String sql = "INSERT INTO branches (name, location, code, phone) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, b.getName().trim());
            ps.setString(2, b.getLocation().trim());
            ps.setString(3, b.getCode() != null ? b.getCode().trim() : "BR-" + System.currentTimeMillis() % 10000);
            ps.setString(4, b.getPhone() != null ? b.getPhone().trim() : "");
            int aff = ps.executeUpdate();
            if (aff > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) b.setId(rs.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(Branch b) throws SQLException {
        String sql = "UPDATE branches SET name = ?, location = ?, code = ?, phone = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, b.getName().trim());
            ps.setString(2, b.getLocation().trim());
            ps.setString(3, b.getCode());
            ps.setString(4, b.getPhone());
            ps.setInt(5, b.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM branches";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Branch mapRow(ResultSet rs) throws SQLException {
        Branch b = new Branch();
        b.setId(rs.getInt("id"));
        b.setName(rs.getString("name"));
        b.setLocation(rs.getString("location"));
        b.setCode(rs.getString("code"));
        b.setPhone(rs.getString("phone"));
        return b;
    }
}

