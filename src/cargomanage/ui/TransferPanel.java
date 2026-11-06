package cargomanage.ui;

import cargomanage.dao.*;
import cargomanage.model.Branch;
import cargomanage.model.Product;
import cargomanage.model.Transfer;
import cargomanage.security.SessionContext;
import cargomanage.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TransferPanel extends JPanel {
    private JComboBox<String> productCombo = new JComboBox<>();
    private JComboBox<String> sourceCombo = new JComboBox<>();
    private JComboBox<String> destCombo = new JComboBox<>();
    private JTextField qty = new JTextField();
    private JTextField remarks = new JTextField();
    private JTable table = new JTable();

    private TransferDAO transferDAO = new TransferDAO();
    private ProductDAO productDAO = new ProductDAO();
    private BranchDAO branchDAO = new BranchDAO();
    private StockDAO stockDAO = new StockDAO();
    private AuditDAO auditDAO = new AuditDAO();

    public TransferPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UIHelper.LIGHT);

        JPanel header = UIHelper.sectionHeader("Inter-Warehouse Stock Transfers",
                "Request stock transfers, supervisor review & approvals, dispatch documents, and receiving confirmations");
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(0, 12, 10, 12));

        JPanel form = new JPanel(new GridLayout(2, 6, 8, 8));
        form.setBackground(UIHelper.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIHelper.BORDER),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        form.add(new JLabel("Product:"));
        form.add(new JLabel("Source Warehouse:"));
        form.add(new JLabel("Destination Warehouse:"));
        form.add(new JLabel("Quantity:"));
        form.add(new JLabel("Remarks / Reason:"));
        JButton reqBtn = UIHelper.button("Request Transfer", UIHelper.BLUE);
        form.add(reqBtn);

        form.add(productCombo);
        form.add(sourceCombo);
        form.add(destCombo);
        form.add(qty);
        form.add(remarks);
        JButton refreshBtn = UIHelper.button("Refresh", UIHelper.NAVY);
        form.add(refreshBtn);

        center.add(form, BorderLayout.NORTH);

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

        JButton viewDocBtn = UIHelper.button("View / Print Dispatch Note", UIHelper.BLUE);
        actions.add(viewDocBtn);

        if (SessionContext.canApproveTransfers()) {
            JButton approveBtn = UIHelper.button("Approve & Dispatch", UIHelper.GREEN);
            JButton rejectBtn = UIHelper.button("Reject Transfer", UIHelper.RED);
            actions.add(approveBtn);
            actions.add(rejectBtn);

            approveBtn.addActionListener(e -> approveTransfer());
            rejectBtn.addActionListener(e -> rejectTransfer());
        }

        JButton receiveBtn = UIHelper.button("Confirm Received at Destination", UIHelper.NAVY);
        actions.add(receiveBtn);

        center.add(actions, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        reqBtn.addActionListener(e -> requestTransfer());
        refreshBtn.addActionListener(e -> { refreshCombos(); load(); });
        viewDocBtn.addActionListener(e -> viewDispatchNote());
        receiveBtn.addActionListener(e -> confirmReceived());

        refreshCombos();
        load();
    }

    public void refreshCombos() {
        productCombo.removeAllItems();
        sourceCombo.removeAllItems();
        destCombo.removeAllItems();

        List<Product> products = productDAO.findAll();
        for (Product p : products) {
            productCombo.addItem(p.getId() + " - " + p.getName());
        }

        List<Branch> branches = branchDAO.findAll();
        for (Branch b : branches) {
            if (!SessionContext.isBranchStaff() || b.getId() == SessionContext.getBranchId()) {
                sourceCombo.addItem(b.getId() + " - " + b.getName());
            }
            destCombo.addItem(b.getId() + " - " + b.getName());
        }
    }

    public void load() {
        DefaultTableModel m = new DefaultTableModel(
                new String[]{"ID", "Product", "Source Warehouse", "Destination", "Qty", "Status", "Requested By", "Approved By", "Received By", "Date"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        Integer branchFilter = SessionContext.isBranchStaff() ? SessionContext.getBranchId() : null;
        List<Transfer> list = transferDAO.findAll(branchFilter);

        for (Transfer t : list) {
            m.addRow(new Object[]{
                    t.getId(),
                    t.getProductName(),
                    t.getSourceBranchName(),
                    t.getDestBranchName(),
                    t.getQuantity() + " " + t.getProductUnit(),
                    t.getStatus(),
                    t.getRequestedBy(),
                    t.getApprovedBy() != null ? t.getApprovedBy() : "-",
                    t.getReceivedBy() != null ? t.getReceivedBy() : "-",
                    t.getRequestDate()
            });
        }
        table.setModel(m);
    }

    private void requestTransfer() {
        try {
            int p = UIHelper.selectedId(productCombo);
            int s = UIHelper.selectedId(sourceCombo);
            int d = UIHelper.selectedId(destCombo);
            int q = Integer.parseInt(qty.getText().trim());

            if (p <= 0 || s <= 0 || d <= 0) {
                UIHelper.error(this, "Please select product, source, and destination warehouses.");
                return;
            }
            if (s == d) {
                UIHelper.error(this, "Source and destination warehouses cannot be the same!");
                return;
            }
            if (q <= 0) {
                UIHelper.error(this, "Transfer quantity must be greater than zero.");
                return;
            }

            int available = stockDAO.getStockQuantity(s, p);
            if (available < q) {
                UIHelper.error(this, "Insufficient stock at source warehouse! Available: " + available + ", Requested: " + q);
                return;
            }

            Transfer t = new Transfer(0, p, s, d, q, SessionContext.getUsername(), DatabaseManager.now());
            t.setRemarks(remarks.getText().trim());

            boolean ok = transferDAO.create(t);
            if (ok) {
                auditDAO.log(SessionContext.getUsername(), "TRANSFER REQUESTED", "Transfer #" + t.getId() + " requested (" + q + " units)");
                UIHelper.info(this, "Transfer request #" + t.getId() + " submitted for manager review.");
                qty.setText("");
                remarks.setText("");
                load();
            }
        } catch (NumberFormatException e) {
            UIHelper.error(this, "Please enter a valid numeric quantity.");
        } catch (Exception e) {
            UIHelper.error(this, "Error requesting transfer: " + e.getMessage());
        }
    }

    private void approveTransfer() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIHelper.error(this, "Please select a transfer request to approve.");
            return;
        }
        int id = (int) table.getValueAt(row, 0);
        String status = (String) table.getValueAt(row, 5);

        if (!"PENDING".equalsIgnoreCase(status)) {
            UIHelper.error(this, "Only PENDING transfers can be approved.");
            return;
        }

        if (UIHelper.confirm(this, "Approve transfer #" + id + " and dispatch stock from source warehouse?", "Approve Transfer")) {
            try {
                boolean ok = transferDAO.approve(id, SessionContext.getUsername());
                if (ok) {
                    auditDAO.log(SessionContext.getUsername(), "TRANSFER APPROVED", "Approved transfer #" + id + " and dispatched stock");
                    UIHelper.info(this, "Transfer #" + id + " approved! Stock deducted and shipment initiated.");
                    load();
                }
            } catch (Exception e) {
                UIHelper.error(this, "Approval failed: " + e.getMessage());
            }
        }
    }

    private void rejectTransfer() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIHelper.error(this, "Please select a transfer request to reject.");
            return;
        }
        int id = (int) table.getValueAt(row, 0);
        String status = (String) table.getValueAt(row, 5);

        if (!"PENDING".equalsIgnoreCase(status)) {
            UIHelper.error(this, "Only PENDING transfers can be rejected.");
            return;
        }

        String reason = JOptionPane.showInputDialog(this, "Enter reason for rejection:", "Reject Transfer #" + id, JOptionPane.QUESTION_MESSAGE);
        if (reason != null) {
            try {
                boolean ok = transferDAO.reject(id, SessionContext.getUsername(), reason.trim());
                if (ok) {
                    auditDAO.log(SessionContext.getUsername(), "TRANSFER REJECTED", "Rejected transfer #" + id + ". Reason: " + reason);
                    UIHelper.info(this, "Transfer #" + id + " rejected.");
                    load();
                }
            } catch (Exception e) {
                UIHelper.error(this, "Error rejecting transfer: " + e.getMessage());
            }
        }
    }

    private void confirmReceived() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIHelper.error(this, "Please select an approved transfer to confirm receiving.");
            return;
        }
        int id = (int) table.getValueAt(row, 0);
        String status = (String) table.getValueAt(row, 5);

        if (!"APPROVED".equalsIgnoreCase(status) && !"DISPATCHED".equalsIgnoreCase(status)) {
            UIHelper.error(this, "Only APPROVED/DISPATCHED transfers can be marked as received (current: " + status + ").");
            return;
        }

        if (UIHelper.confirm(this, "Confirm receipt of physical goods for transfer #" + id + "? Stock will be credited to destination warehouse.", "Confirm Receipt")) {
            try {
                boolean ok = transferDAO.receive(id, SessionContext.getUsername());
                if (ok) {
                    auditDAO.log(SessionContext.getUsername(), "TRANSFER RECEIVED", "Transfer #" + id + " confirmed received at destination warehouse");
                    UIHelper.info(this, "Stock received and credited to destination warehouse successfully!");
                    load();
                }
            } catch (Exception e) {
                UIHelper.error(this, "Error confirming receipt: " + e.getMessage());
            }
        }
    }

    private void viewDispatchNote() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIHelper.error(this, "Please select a transfer to view dispatch document.");
            return;
        }
        int id = (int) table.getValueAt(row, 0);
        Transfer t = transferDAO.findById(id);
        if (t != null) {
            new TransferReceiptDialog((Frame) SwingUtilities.getWindowAncestor(this), t).setVisible(true);
        }
    }
}

