package cargomanage.dao;

import cargomanage.model.Purchase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PurchaseDAO {

    public List<Purchase> findAll(Integer branchIdFilter) {
        List<Purchase> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT pu.*, pr.name AS product_name, pr.unit AS product_unit, b.name AS branch_name
            FROM purchases pu
            JOIN products pr ON pu.product_id = pr.id
            JOIN branches b ON pu.branch_id = b.id
        """);

        if (branchIdFilter != null && branchIdFilter > 0) {
            sql.append(" WHERE pu.branch_id = ?");
        }
        sql.append(" ORDER BY pu.id DESC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (branchIdFilter != null && branchIdFilter > 0) {
                ps.setInt(1, branchIdFilter);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Purchase findById(int id) {
        String sql = """
            SELECT pu.*, pr.name AS product_name, pr.unit AS product_unit, b.name AS branch_name
            FROM purchases pu
            JOIN products pr ON pu.product_id = pr.id
            JOIN branches b ON pu.branch_id = b.id
            WHERE pu.id = ?
        """;
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

    public synchronized boolean create(Purchase p, boolean addStockImmediately) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String sql = """
                    INSERT INTO purchases (supplier_name, supplier_contact, product_id, branch_id,
                                          quantity, purchase_price, total_amount, purchase_date,
                                          payment_status, payment_ref, received_qty, status, created_by)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
                try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, p.getSupplierName().trim());
                    ps.setString(2, p.getSupplierContact() != null ? p.getSupplierContact().trim() : "");
                    ps.setInt(3, p.getProductId());
                    ps.setInt(4, p.getBranchId());
                    ps.setInt(5, p.getQuantity());
                    ps.setDouble(6, p.getPurchasePrice());
                    ps.setDouble(7, p.getTotalAmount() > 0 ? p.getTotalAmount() : (p.getQuantity() * p.getPurchasePrice()));
                    ps.setString(8, p.getPurchaseDate() != null ? p.getPurchaseDate() : DatabaseManager.now());
                    ps.setString(9, p.getPaymentStatus() != null ? p.getPaymentStatus() : "PENDING");
                    ps.setString(10, p.getPaymentRef() != null ? p.getPaymentRef() : "");
                    ps.setInt(11, addStockImmediately ? p.getQuantity() : 0);
                    ps.setString(12, addStockImmediately ? "RECEIVED" : "ORDERED");
                    ps.setString(13, p.getCreatedBy());

                    int aff = ps.executeUpdate();
                    if (aff > 0) {
                        try (ResultSet rs = ps.getGeneratedKeys()) {
                            if (rs.next()) p.setId(rs.getInt(1));
                        }
                    }
                }

                if (addStockImmediately) {
                    String stockSql = """
                        INSERT INTO stocks (branch_id, product_id, quantity)
                        VALUES (?, ?, ?)
                        ON DUPLICATE KEY UPDATE quantity = quantity + ?
                    """;
                    try (PreparedStatement ps = conn.prepareStatement(stockSql)) {
                        ps.setInt(1, p.getBranchId());
                        ps.setInt(2, p.getProductId());
                        ps.setInt(3, p.getQuantity());
                        ps.setInt(4, p.getQuantity());
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                if (e instanceof SQLException) throw (SQLException) e;
                throw new SQLException(e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public boolean updatePaymentStatus(int purchaseId, String paymentStatus, String paymentRef) throws SQLException {
        String sql = "UPDATE purchases SET payment_status = ?, payment_ref = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, paymentStatus);
            ps.setString(2, paymentRef);
            ps.setInt(3, purchaseId);
            return ps.executeUpdate() > 0;
        }
    }

    private Purchase mapRow(ResultSet rs) throws SQLException {
        Purchase p = new Purchase();
        p.setId(rs.getInt("id"));
        p.setSupplierName(rs.getString("supplier_name"));
        p.setSupplierContact(rs.getString("supplier_contact"));
        p.setProductId(rs.getInt("product_id"));
        p.setBranchId(rs.getInt("branch_id"));
        p.setQuantity(rs.getInt("quantity"));
        p.setPurchasePrice(rs.getDouble("purchase_price"));
        p.setTotalAmount(rs.getDouble("total_amount"));
        p.setPurchaseDate(rs.getString("purchase_date"));
        p.setPaymentStatus(rs.getString("payment_status"));
        p.setPaymentRef(rs.getString("payment_ref"));
        p.setReceivedQty(rs.getInt("received_qty"));
        p.setStatus(rs.getString("status"));
        p.setCreatedBy(rs.getString("created_by"));
        p.setProductName(rs.getString("product_name"));
        p.setUnit(rs.getString("product_unit"));
        p.setBranchName(rs.getString("branch_name"));
        return p;
    }
}

