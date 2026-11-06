package cargomanage.dao;

import cargomanage.model.Payment;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaymentDAO {

    public List<Payment> findAll(String directionFilter, String statusFilter, String searchQuery) {
        List<Payment> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM payments WHERE 1=1 ");

        if (directionFilter != null && !directionFilter.equalsIgnoreCase("All")) {
            sql.append(" AND direction = ? ");
        }
        if (statusFilter != null && !statusFilter.equalsIgnoreCase("All")) {
            sql.append(" AND status = ? ");
        }
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            sql.append(" AND (transaction_id LIKE ? OR receipt_no LIKE ? OR party_name LIKE ? OR reference_no LIKE ? OR description LIKE ?) ");
        }
        sql.append(" ORDER BY id DESC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (directionFilter != null && !directionFilter.equalsIgnoreCase("All")) {
                ps.setString(idx++, directionFilter);
            }
            if (statusFilter != null && !statusFilter.equalsIgnoreCase("All")) {
                ps.setString(idx++, statusFilter);
            }
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                String pat = "%" + searchQuery.trim() + "%";
                ps.setString(idx++, pat);
                ps.setString(idx++, pat);
                ps.setString(idx++, pat);
                ps.setString(idx++, pat);
                ps.setString(idx++, pat);
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

    public Payment findById(int id) {
        String sql = "SELECT * FROM payments WHERE id = ?";
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

    public Payment findByReceiptNo(String receiptNo) {
        String sql = "SELECT * FROM payments WHERE receipt_no = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, receiptNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized String generateNextReceiptNo() {
        String prefix = "RCP-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-";
        int count = 1;
        String sql = "SELECT COUNT(*) FROM payments WHERE receipt_no LIKE ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) count = rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return String.format("%s%04d", prefix, count);
    }

    public synchronized String generateNextTransactionId() {
        String prefix = "TXN-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        int count = 1;
        String sql = "SELECT COUNT(*) FROM payments WHERE transaction_id LIKE ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) count = rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return String.format("%s%04d", prefix, count);
    }

    public boolean create(Payment p) throws SQLException {
        if (p.getTransactionId() == null || p.getTransactionId().isBlank()) {
            p.setTransactionId(generateNextTransactionId());
        }
        if (p.getReceiptNo() == null || p.getReceiptNo().isBlank()) {
            p.setReceiptNo(generateNextReceiptNo());
        }
        if (p.getPaymentDate() == null || p.getPaymentDate().isBlank()) {
            p.setPaymentDate(DatabaseManager.now());
        }

        String sql = """
            INSERT INTO payments (transaction_id, receipt_no, payment_date, party_name, party_type,
                                 related_type, related_id, payment_method, direction, amount,
                                 status, reference_no, description, recorded_by, remarks)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getTransactionId().trim());
            ps.setString(2, p.getReceiptNo().trim());
            ps.setString(3, p.getPaymentDate());
            ps.setString(4, p.getPartyName().trim());
            ps.setString(5, p.getPartyType() != null ? p.getPartyType() : "SUPPLIER");
            ps.setString(6, p.getRelatedType());
            if (p.getRelatedId() != null && p.getRelatedId() > 0) {
                ps.setInt(7, p.getRelatedId());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setString(8, p.getPaymentMethod());
            ps.setString(9, p.getDirection());
            ps.setDouble(10, p.getAmount());
            ps.setString(11, p.getStatus());
            ps.setString(12, p.getReferenceNo() != null ? p.getReferenceNo().trim() : "");
            ps.setString(13, p.getDescription() != null ? p.getDescription().trim() : "");
            ps.setString(14, p.getRecordedBy() != null ? p.getRecordedBy() : "system");
            ps.setString(15, p.getRemarks() != null ? p.getRemarks().trim() : "");

            int aff = ps.executeUpdate();
            if (aff > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) p.setId(rs.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(Payment p) throws SQLException {
        String sql = """
            UPDATE payments SET party_name = ?, party_type = ?, related_type = ?,
                               related_id = ?, payment_method = ?, direction = ?,
                               amount = ?, status = ?, reference_no = ?, description = ?,
                               remarks = ?
            WHERE id = ?
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getPartyName().trim());
            ps.setString(2, p.getPartyType());
            ps.setString(3, p.getRelatedType());
            if (p.getRelatedId() != null && p.getRelatedId() > 0) {
                ps.setInt(4, p.getRelatedId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setString(5, p.getPaymentMethod());
            ps.setString(6, p.getDirection());
            ps.setDouble(7, p.getAmount());
            ps.setString(8, p.getStatus());
            ps.setString(9, p.getReferenceNo());
            ps.setString(10, p.getDescription());
            ps.setString(11, p.getRemarks());
            ps.setInt(12, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public double getTotalIncoming() {
        String sql = "SELECT COALESCE(SUM(amount), 0.0) FROM payments WHERE direction = 'Incoming' AND status = 'Paid'";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public double getTotalOutgoing() {
        String sql = "SELECT COALESCE(SUM(amount), 0.0) FROM payments WHERE direction = 'Outgoing' AND status = 'Paid'";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public double getTotalPending() {
        String sql = "SELECT COALESCE(SUM(amount), 0.0) FROM payments WHERE status = 'Pending'";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public Map<String, Object> getFinancialSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("incoming", getTotalIncoming());
        summary.put("outgoing", getTotalOutgoing());
        summary.put("pending", getTotalPending());
        summary.put("netBalance", getTotalIncoming() - getTotalOutgoing());
        return summary;
    }

    private Payment mapRow(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getInt("id"));
        p.setTransactionId(rs.getString("transaction_id"));
        p.setReceiptNo(rs.getString("receipt_no"));
        p.setPaymentDate(rs.getString("payment_date"));
        p.setPartyName(rs.getString("party_name"));
        p.setPartyType(rs.getString("party_type"));
        p.setRelatedType(rs.getString("related_type"));
        int relId = rs.getInt("related_id");
        p.setRelatedId(rs.wasNull() ? null : relId);
        p.setPaymentMethod(rs.getString("payment_method"));
        p.setDirection(rs.getString("direction"));
        p.setAmount(rs.getDouble("amount"));
        p.setStatus(rs.getString("status"));
        p.setReferenceNo(rs.getString("reference_no"));
        p.setDescription(rs.getString("description"));
        p.setRecordedBy(rs.getString("recorded_by"));
        p.setRemarks(rs.getString("remarks"));
        return p;
    }
}

