package cargomanage.dao;

import cargomanage.model.Transfer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransferDAO {

    public List<Transfer> findAll(Integer branchIdFilter) {
        List<Transfer> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT t.*, p.name AS product_name, p.unit AS product_unit,
                   sb.name AS source_branch_name, db.name AS dest_branch_name
            FROM transfers t
            JOIN products p ON t.product_id = p.id
            JOIN branches sb ON t.source_branch_id = sb.id
            JOIN branches db ON t.dest_branch_id = db.id
        """);

        if (branchIdFilter != null && branchIdFilter > 0) {
            sql.append(" WHERE t.source_branch_id = ? OR t.dest_branch_id = ?");
        }
        sql.append(" ORDER BY t.id DESC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (branchIdFilter != null && branchIdFilter > 0) {
                ps.setInt(1, branchIdFilter);
                ps.setInt(2, branchIdFilter);
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

    public Transfer findById(int id) {
        String sql = """
            SELECT t.*, p.name AS product_name, p.unit AS product_unit,
                   sb.name AS source_branch_name, db.name AS dest_branch_name
            FROM transfers t
            JOIN products p ON t.product_id = p.id
            JOIN branches sb ON t.source_branch_id = sb.id
            JOIN branches db ON t.dest_branch_id = db.id
            WHERE t.id = ?
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

    public boolean create(Transfer t) throws SQLException {
        String sql = """
            INSERT INTO transfers (product_id, source_branch_id, dest_branch_id, quantity,
                                  status, requested_by, request_date, remarks)
            VALUES (?, ?, ?, ?, 'PENDING', ?, ?, ?)
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, t.getProductId());
            ps.setInt(2, t.getSourceBranchId());
            ps.setInt(3, t.getDestBranchId());
            ps.setInt(4, t.getQuantity());
            ps.setString(5, t.getRequestedBy());
            ps.setString(6, t.getRequestDate());
            ps.setString(7, t.getRemarks());

            int aff = ps.executeUpdate();
            if (aff > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) t.setId(rs.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    public synchronized boolean approve(int transferId, String approverUsername) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String selectTransfer = "SELECT * FROM transfers WHERE id = ? FOR UPDATE";
                int productId = 0, sourceId = 0, qty = 0;
                String currentStatus = null;

                try (PreparedStatement ps = conn.prepareStatement(selectTransfer)) {
                    ps.setInt(1, transferId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            throw new SQLException("Transfer #" + transferId + " not found.");
                        }
                        currentStatus = rs.getString("status");
                        productId = rs.getInt("product_id");
                        sourceId = rs.getInt("source_branch_id");
                        qty = rs.getInt("quantity");
                    }
                }

                if (!"PENDING".equalsIgnoreCase(currentStatus)) {
                    conn.rollback();
                    throw new SQLException("Transfer is not in PENDING status (current: " + currentStatus + ")");
                }

                String checkStock = "SELECT quantity FROM stocks WHERE branch_id = ? AND product_id = ? FOR UPDATE";
                int currentSourceQty = 0;
                try (PreparedStatement ps = conn.prepareStatement(checkStock)) {
                    ps.setInt(1, sourceId);
                    ps.setInt(2, productId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            currentSourceQty = rs.getInt("quantity");
                        }
                    }
                }

                if (currentSourceQty < qty) {
                    conn.rollback();
                    throw new SQLException("Insufficient stock at source warehouse! Available: " + currentSourceQty + ", Requested: " + qty);
                }

                String deductStock = "UPDATE stocks SET quantity = quantity - ? WHERE branch_id = ? AND product_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deductStock)) {
                    ps.setInt(1, qty);
                    ps.setInt(2, sourceId);
                    ps.setInt(3, productId);
                    ps.executeUpdate();
                }

                String now = DatabaseManager.now();
                String updateTransfer = "UPDATE transfers SET status = 'APPROVED', approved_by = ?, dispatch_date = ? WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateTransfer)) {
                    ps.setString(1, approverUsername);
                    ps.setString(2, now);
                    ps.setInt(3, transferId);
                    ps.executeUpdate();
                }

                String insertShipment = "INSERT INTO shipments (transfer_id, tracking_number, carrier, status, eta, dispatch_date) VALUES (?, ?, ?, 'DISPATCHED', '3-5 business days', ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertShipment)) {
                    ps.setInt(1, transferId);
                    ps.setString(2, "CM-TRK-" + (1000 + transferId));
                    ps.setString(3, "CargoManage Express Fleet");
                    ps.setString(4, now);
                    ps.executeUpdate();
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

    public synchronized boolean reject(int transferId, String approverUsername, String reason) throws SQLException {
        String sql = "UPDATE transfers SET status = 'REJECTED', approved_by = ?, remarks = CONCAT(COALESCE(remarks, ''), ' | Rejected: ', ?) WHERE id = ? AND status = 'PENDING'";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, approverUsername);
            ps.setString(2, reason != null ? reason : "Rejected by manager");
            ps.setInt(3, transferId);
            return ps.executeUpdate() > 0;
        }
    }

    public synchronized boolean receive(int transferId, String receiverUsername) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String selectTransfer = "SELECT * FROM transfers WHERE id = ? FOR UPDATE";
                int productId = 0, destId = 0, qty = 0;
                String currentStatus = null;

                try (PreparedStatement ps = conn.prepareStatement(selectTransfer)) {
                    ps.setInt(1, transferId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            throw new SQLException("Transfer #" + transferId + " not found.");
                        }
                        currentStatus = rs.getString("status");
                        productId = rs.getInt("product_id");
                        destId = rs.getInt("dest_branch_id");
                        qty = rs.getInt("quantity");
                    }
                }

                if (!"APPROVED".equalsIgnoreCase(currentStatus) && !"DISPATCHED".equalsIgnoreCase(currentStatus)) {
                    conn.rollback();
                    throw new SQLException("Cannot receive transfer in status: " + currentStatus + " (must be APPROVED or DISPATCHED)");
                }

                String addStock = """
                    INSERT INTO stocks (branch_id, product_id, quantity)
                    VALUES (?, ?, ?)
                    ON DUPLICATE KEY UPDATE quantity = quantity + ?
                """;
                try (PreparedStatement ps = conn.prepareStatement(addStock)) {
                    ps.setInt(1, destId);
                    ps.setInt(2, productId);
                    ps.setInt(3, qty);
                    ps.setInt(4, qty);
                    ps.executeUpdate();
                }

                String now = DatabaseManager.now();
                String updateTransfer = "UPDATE transfers SET status = 'RECEIVED', received_by = ?, received_date = ? WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateTransfer)) {
                    ps.setString(1, receiverUsername);
                    ps.setString(2, now);
                    ps.setInt(3, transferId);
                    ps.executeUpdate();
                }

                String updateShipment = "UPDATE shipments SET status = 'DELIVERED', delivery_date = ? WHERE transfer_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateShipment)) {
                    ps.setString(1, now);
                    ps.setInt(2, transferId);
                    ps.executeUpdate();
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

    public int getPendingCount(Integer branchIdFilter) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM transfers WHERE status = 'PENDING'");
        if (branchIdFilter != null && branchIdFilter > 0) {
            sql.append(" AND (source_branch_id = ? OR dest_branch_id = ?)");
        }
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (branchIdFilter != null && branchIdFilter > 0) {
                ps.setInt(1, branchIdFilter);
                ps.setInt(2, branchIdFilter);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Transfer mapRow(ResultSet rs) throws SQLException {
        Transfer t = new Transfer();
        t.setId(rs.getInt("id"));
        t.setProductId(rs.getInt("product_id"));
        t.setSourceBranchId(rs.getInt("source_branch_id"));
        t.setDestBranchId(rs.getInt("dest_branch_id"));
        t.setQuantity(rs.getInt("quantity"));
        t.setStatus(rs.getString("status"));
        t.setRequestedBy(rs.getString("requested_by"));
        t.setApprovedBy(rs.getString("approved_by"));
        t.setReceivedBy(rs.getString("received_by"));
        t.setRequestDate(rs.getString("request_date"));
        t.setDispatchDate(rs.getString("dispatch_date"));
        t.setReceivedDate(rs.getString("received_date"));
        t.setRemarks(rs.getString("remarks"));
        t.setProductName(rs.getString("product_name"));
        t.setProductUnit(rs.getString("product_unit"));
        t.setSourceBranchName(rs.getString("source_branch_name"));
        t.setDestBranchName(rs.getString("dest_branch_name"));
        return t;
    }
}

