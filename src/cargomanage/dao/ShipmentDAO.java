package cargomanage.dao;

import cargomanage.model.Shipment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShipmentDAO {

    public List<Shipment> findAll(Integer branchIdFilter) {
        List<Shipment> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT s.*, t.quantity, p.name AS product_name,
                   sb.name AS source_branch_name, db.name AS dest_branch_name
            FROM shipments s
            JOIN transfers t ON s.transfer_id = t.id
            JOIN products p ON t.product_id = p.id
            JOIN branches sb ON t.source_branch_id = sb.id
            JOIN branches db ON t.dest_branch_id = db.id
        """);

        if (branchIdFilter != null && branchIdFilter > 0) {
            sql.append(" WHERE t.source_branch_id = ? OR t.dest_branch_id = ?");
        }
        sql.append(" ORDER BY s.id DESC");

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

    public boolean updateStatus(int shipmentId, String status) throws SQLException {
        String sql = "UPDATE shipments SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, shipmentId);
            return ps.executeUpdate() > 0;
        }
    }

    private Shipment mapRow(ResultSet rs) throws SQLException {
        Shipment s = new Shipment();
        s.setId(rs.getInt("id"));
        s.setTransferId(rs.getInt("transfer_id"));
        s.setTrackingNumber(rs.getString("tracking_number"));
        s.setCarrier(rs.getString("carrier"));
        s.setStatus(rs.getString("status"));
        s.setEta(rs.getString("eta"));
        s.setDispatchDate(rs.getString("dispatch_date"));
        s.setDeliveryDate(rs.getString("delivery_date"));
        s.setQuantity(rs.getInt("quantity"));
        s.setProductName(rs.getString("product_name"));
        s.setSourceBranchName(rs.getString("source_branch_name"));
        s.setDestBranchName(rs.getString("dest_branch_name"));
        return s;
    }
}

