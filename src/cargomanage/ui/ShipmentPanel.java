package cargomanage.ui;

import cargomanage.dao.AuditDAO;
import cargomanage.dao.ShipmentDAO;
import cargomanage.model.Shipment;
import cargomanage.security.SessionContext;
import cargomanage.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ShipmentPanel extends JPanel {
    private JTable table = new JTable();
    private ShipmentDAO shipmentDAO = new ShipmentDAO();
    private AuditDAO auditDAO = new AuditDAO();

    public ShipmentPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UIHelper.LIGHT);

        JPanel header = UIHelper.sectionHeader("Logistics & Transit Shipments",
                "Monitor inter-branch vehicle dispatches, in-transit status, and expected delivery tracking");
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(0, 12, 10, 12));

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER));
        center.add(sp, BorderLayout.CENTER);
        UIHelper.styleTable(table);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        actions.setBackground(UIHelper.WHITE);
        actions.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIHelper.BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        if (SessionContext.canUpdateShipmentStatus()) {
            JButton disBtn = UIHelper.button("Mark Dispatched", UIHelper.BLUE);
            JButton tranBtn = UIHelper.button("Mark In Transit", UIHelper.ORANGE);
            JButton delBtn = UIHelper.button("Mark Delivered", UIHelper.GREEN);

            actions.add(disBtn);
            actions.add(tranBtn);
            actions.add(delBtn);

            disBtn.addActionListener(e -> updateStatus("DISPATCHED"));
            tranBtn.addActionListener(e -> updateStatus("IN TRANSIT"));
            delBtn.addActionListener(e -> updateStatus("DELIVERED"));
        }

        JButton refreshBtn = UIHelper.button("Refresh", UIHelper.NAVY);
        actions.add(refreshBtn);
        refreshBtn.addActionListener(e -> load());

        center.add(actions, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        load();
    }

    public void load() {
        DefaultTableModel m = new DefaultTableModel(
                new String[]{"Shipment ID", "Transfer ID", "Tracking Number", "Carrier", "Product", "Quantity", "From Warehouse", "To Warehouse", "Status", "ETA"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        Integer branchFilter = SessionContext.isBranchStaff() ? SessionContext.getBranchId() : null;
        List<Shipment> list = shipmentDAO.findAll(branchFilter);

        for (Shipment s : list) {
            m.addRow(new Object[]{
                    s.getId(),
                    s.getTransferId(),
                    s.getTrackingNumber(),
                    s.getCarrier(),
                    s.getProductName(),
                    s.getQuantity(),
                    s.getSourceBranchName(),
                    s.getDestBranchName(),
                    s.getStatus(),
                    s.getEta()
            });
        }
        table.setModel(m);
    }

    private void updateStatus(String status) {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIHelper.error(this, "Please select a shipment from the table.");
            return;
        }
        int id = (int) table.getValueAt(row, 0);
        try {
            boolean ok = shipmentDAO.updateStatus(id, status);
            if (ok) {
                auditDAO.log(SessionContext.getUsername(), "SHIPMENT STATUS UPDATE", "Updated shipment #" + id + " to " + status);
                UIHelper.info(this, "Shipment #" + id + " updated to " + status);
                load();
            }
        } catch (Exception e) {
            UIHelper.error(this, "Error updating status: " + e.getMessage());
        }
    }
}

