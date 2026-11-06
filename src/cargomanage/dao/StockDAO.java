package cargomanage.dao;

import cargomanage.model.Stock;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockDAO {

    public int getStockQuantity(int branchId, int productId) {
        String sql = "SELECT quantity FROM stocks WHERE branch_id = ? AND product_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, branchId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("quantity");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean setStock(int branchId, int productId, int quantity) throws SQLException {
        String sql = """
            INSERT INTO stocks (branch_id, product_id, quantity)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE quantity = VALUES(quantity)
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, branchId);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean addStock(int branchId, int productId, int delta) throws SQLException {
        String sql = """
            INSERT INTO stocks (branch_id, product_id, quantity)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE quantity = quantity + ?
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, branchId);
            ps.setInt(2, productId);
            ps.setInt(3, delta);
            ps.setInt(4, delta);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Stock> findAllStock(Integer branchIdFilter) {
        List<Stock> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT s.id, s.branch_id, s.product_id, s.quantity, s.updated_at,
                   b.name AS branch_name, p.name AS product_name, p.category AS product_category,
                   p.unit, p.reorder_level, p.purchase_price
            FROM stocks s
            JOIN branches b ON s.branch_id = b.id
            JOIN products p ON s.product_id = p.id
        """);

        if (branchIdFilter != null && branchIdFilter > 0) {
            sql.append(" WHERE s.branch_id = ? ");
        }
        sql.append(" ORDER BY b.name ASC, p.name ASC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (branchIdFilter != null && branchIdFilter > 0) {
                ps.setInt(1, branchIdFilter);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Stock s = new Stock();
                    s.setId(rs.getInt("id"));
                    s.setBranchId(rs.getInt("branch_id"));
                    s.setProductId(rs.getInt("product_id"));
                    s.setQuantity(rs.getInt("quantity"));
                    s.setUpdatedAt(rs.getString("updated_at"));
                    s.setBranchName(rs.getString("branch_name"));
                    s.setProductName(rs.getString("product_name"));
                    s.setProductCategory(rs.getString("product_category"));
                    s.setUnit(rs.getString("unit"));
                    s.setReorderLevel(rs.getInt("reorder_level"));
                    s.setPurchasePrice(rs.getDouble("purchase_price"));
                    list.add(s);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int getTotalStock(Integer branchIdFilter) {
        StringBuilder sql = new StringBuilder("SELECT COALESCE(SUM(quantity), 0) FROM stocks");
        if (branchIdFilter != null && branchIdFilter > 0) {
            sql.append(" WHERE branch_id = ?");
        }
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (branchIdFilter != null && branchIdFilter > 0) {
                ps.setInt(1, branchIdFilter);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getLowStockCount(Integer branchIdFilter) {
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(*)
            FROM stocks s
            JOIN products p ON s.product_id = p.id
            WHERE s.quantity <= p.reorder_level
        """);
        if (branchIdFilter != null && branchIdFilter > 0) {
            sql.append(" AND s.branch_id = ?");
        }
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (branchIdFilter != null && branchIdFilter > 0) {
                ps.setInt(1, branchIdFilter);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Stock> getLowStockItems(Integer branchIdFilter) {
        List<Stock> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT s.id, s.branch_id, s.product_id, s.quantity, s.updated_at,
                   b.name AS branch_name, p.name AS product_name, p.category AS product_category,
                   p.unit, p.reorder_level, p.purchase_price
            FROM stocks s
            JOIN branches b ON s.branch_id = b.id
            JOIN products p ON s.product_id = p.id
            WHERE s.quantity <= p.reorder_level
        """);
        if (branchIdFilter != null && branchIdFilter > 0) {
            sql.append(" AND s.branch_id = ?");
        }
        sql.append(" ORDER BY s.quantity ASC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (branchIdFilter != null && branchIdFilter > 0) {
                ps.setInt(1, branchIdFilter);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Stock s = new Stock();
                    s.setId(rs.getInt("id"));
                    s.setBranchId(rs.getInt("branch_id"));
                    s.setProductId(rs.getInt("product_id"));
                    s.setQuantity(rs.getInt("quantity"));
                    s.setBranchName(rs.getString("branch_name"));
                    s.setProductName(rs.getString("product_name"));
                    s.setProductCategory(rs.getString("product_category"));
                    s.setUnit(rs.getString("unit"));
                    s.setReorderLevel(rs.getInt("reorder_level"));
                    s.setPurchasePrice(rs.getDouble("purchase_price"));
                    list.add(s);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}

