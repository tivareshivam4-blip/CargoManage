package cargomanage.ui;

import cargomanage.dao.*;
import cargomanage.model.Branch;
import cargomanage.model.Payment;
import cargomanage.model.Product;
import cargomanage.model.Purchase;
import cargomanage.security.SessionContext;
import cargomanage.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PurchasePanel extends JPanel {
    private JTextField supplier = new JTextField();
    private JTextField contact = new JTextField();
    private JComboBox<String> productCombo = new JComboBox<>();
    private JComboBox<String> branchCombo = new JComboBox<>();
    private JTextField qty = new JTextField();
    private JTextField price = new JTextField("0.00");
    private JComboBox<String> paymentStatusCombo = new JComboBox<>(new String[]{"PENDING", "PAID"});
    private JTable table = new JTable();

    private PurchaseDAO purchaseDAO = new PurchaseDAO();
    private ProductDAO productDAO = new ProductDAO();
    private BranchDAO branchDAO = new BranchDAO();
    private PaymentDAO paymentDAO = new PaymentDAO();
    private AuditDAO auditDAO = new AuditDAO();

    public PurchasePanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UIHelper.LIGHT);

        JPanel header = UIHelper.sectionHeader("Procurement & Supplier Purchases",
                "Record vendor purchases, inbound stock deliveries, unit cost tracking, and financial reconciliation");
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(0, 12, 10, 12));

        JPanel form = new JPanel(new GridLayout(2, 7, 6, 6));
        form.setBackground(UIHelper.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIHelper.BORDER),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        form.add(new JLabel("Supplier Name:"));
        form.add(new JLabel("Supplier Contact:"));
        form.add(new JLabel("Select Product:"));
        form.add(new JLabel("Target Branch:"));
        form.add(new JLabel("Quantity:"));
        form.add(new JLabel("Unit Price (INR):"));
        form.add(new JLabel("Payment Status:"));

        form.add(supplier);
        form.add(contact);
        form.add(productCombo);
        form.add(branchCombo);
        form.add(qty);
        form.add(price);
        form.add(paymentStatusCombo);

        JPanel formBtnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        formBtnBar.setOpaque(false);
        JButton receiveBtn = UIHelper.button("Receive Stock & Log Purchase", UIHelper.BLUE);
        JButton refreshBtn = UIHelper.button("Refresh", UIHelper.NAVY);
        formBtnBar.add(receiveBtn);
        formBtnBar.add(refreshBtn);

        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setOpaque(false);
        formWrapper.add(form, BorderLayout.CENTER);
        formWrapper.add(formBtnBar, BorderLayout.SOUTH);

        center.add(formWrapper, BorderLayout.NORTH);

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

        JButton markPaidBtn = UIHelper.button("Record / Settle Payment", UIHelper.GREEN);
        actions.add(markPaidBtn);
        center.add(actions, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);

        receiveBtn.addActionListener(e -> receiveStock());
        refreshBtn.addActionListener(e -> { refreshCombos(); load(); });
        markPaidBtn.addActionListener(e -> settlePurchasePayment());

        productCombo.addActionListener(e -> autoFillPrice());

        refreshCombos();
        load();
    }

    private void autoFillPrice() {
        int pId = UIHelper.selectedId(productCombo);
        if (pId > 0) {
            Product p = productDAO.findById(pId);
            if (p != null) {
                price.setText(String.format("%.2f", p.getPurchasePrice()));
            }
        }
    }

    public void refreshCombos() {
        productCombo.removeAllItems();
        branchCombo.removeAllItems();

        List<Product> products = productDAO.findAll();
        for (Product p : products) {
            productCombo.addItem(p.getId() + " - " + p.getName());
        }

        List<Branch> branches = branchDAO.findAll();
        for (Branch b : branches) {
            if (!SessionContext.isBranchStaff() || b.getId() == SessionContext.getBranchId()) {
                branchCombo.addItem(b.getId() + " - " + b.getName());
            }
        }
    }

    public void load() {
        DefaultTableModel m = new DefaultTableModel(
                new String[]{"ID", "Supplier Name", "Product", "Branch", "Quantity", "Unit Cost", "Total Amount", "Date", "Status", "Payment"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        Integer branchFilter = SessionContext.isBranchStaff() ? SessionContext.getBranchId() : null;
        List<Purchase> list = purchaseDAO.findAll(branchFilter);

        for (Purchase pu : list) {
            m.addRow(new Object[]{
                    pu.getId(),
                    pu.getSupplierName(),
                    pu.getProductName(),
                    pu.getBranchName(),
                    pu.getQuantity() + " " + pu.getUnit(),
                    String.format("INR %.2f", pu.getPurchasePrice()),
                    String.format("INR %.2f", pu.getTotalAmount()),
                    pu.getPurchaseDate(),
                    pu.getStatus(),
                    pu.getPaymentStatus()
            });
        }
        table.setModel(m);
    }

    private void receiveStock() {
        String sup = supplier.getText().trim();
        String con = contact.getText().trim();
        int pId = UIHelper.selectedId(productCombo);
        int bId = UIHelper.selectedId(branchCombo);
        String qStr = qty.getText().trim();
        String pStr = price.getText().trim();
        String payStat = (String) paymentStatusCombo.getSelectedItem();

        if (sup.isBlank() || pId <= 0 || bId <= 0) {
            UIHelper.error(this, "Please enter supplier name and select product & branch.");
            return;
        }

        try {
            int q = Integer.parseInt(qStr);
            double pr = Double.parseDouble(pStr);

            if (q <= 0) {
                UIHelper.error(this, "Quantity must be greater than zero.");
                return;
            }

            double total = q * pr;
            Purchase pu = new Purchase(0, sup, pId, bId, q, DatabaseManager.now());
            pu.setSupplierContact(con);
            pu.setPurchasePrice(pr);
            pu.setTotalAmount(total);
            pu.setPaymentStatus(payStat);
            pu.setCreatedBy(SessionContext.getUsername());

            boolean ok = purchaseDAO.create(pu, true);
            if (ok) {
                auditDAO.log(SessionContext.getUsername(), "PURCHASE RECEIVED",
                        "Purchase #" + pu.getId() + " received: " + q + " units from " + sup);

                if ("PAID".equalsIgnoreCase(payStat)) {
                    Payment pay = new Payment();
                    pay.setPartyName(sup);
                    pay.setPartyType("SUPPLIER");
                    pay.setRelatedType("PURCHASE");
                    pay.setRelatedId(pu.getId());
                    pay.setPaymentMethod("Bank Transfer");
                    pay.setDirection("Outgoing");
                    pay.setAmount(total);
                    pay.setStatus("Paid");
                    pay.setDescription("Payment for Purchase #" + pu.getId() + " (" + q + " units)");
                    pay.setRecordedBy(SessionContext.getUsername());
                    pay.setRemarks("Auto-generated from Purchase module");
                    paymentDAO.create(pay);
                }

                UIHelper.info(this, "Purchase #" + pu.getId() + " recorded! Stock added to warehouse inventory.");
                supplier.setText("");
                contact.setText("");
                qty.setText("");
                load();
            }
        } catch (NumberFormatException e) {
            UIHelper.error(this, "Quantity and price must be valid numbers.");
        } catch (Exception e) {
            UIHelper.error(this, "Error recording purchase: " + e.getMessage());
        }
    }

    private void settlePurchasePayment() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIHelper.error(this, "Please select a purchase from the table.");
            return;
        }
        int id = (int) table.getValueAt(row, 0);
        Purchase pu = purchaseDAO.findById(id);
        if (pu == null) return;

        if ("PAID".equalsIgnoreCase(pu.getPaymentStatus())) {
            UIHelper.info(this, "This purchase is already settled as PAID.");
            return;
        }

        String ref = JOptionPane.showInputDialog(this,
                "Enter payment reference / UTR number for Purchase #" + id + " (Amount: INR " + String.format("%.2f", pu.getTotalAmount()) + "):",
                "Settle Purchase Payment", JOptionPane.QUESTION_MESSAGE);

        if (ref != null && !ref.isBlank()) {
            try {
                purchaseDAO.updatePaymentStatus(id, "PAID", ref.trim());

                Payment p = new Payment();
                p.setPartyName(pu.getSupplierName());
                p.setPartyType("SUPPLIER");
                p.setRelatedType("PURCHASE");
                p.setRelatedId(pu.getId());
                p.setPaymentMethod("Bank Transfer");
                p.setDirection("Outgoing");
                p.setAmount(pu.getTotalAmount());
                p.setStatus("Paid");
                p.setReferenceNo(ref.trim());
                p.setDescription("Settlement for Purchase #" + pu.getId() + " (" + pu.getQuantity() + " " + pu.getUnit() + " " + pu.getProductName() + ")");
                p.setRecordedBy(SessionContext.getUsername());
                p.setRemarks("Settled via Purchase module");
                paymentDAO.create(p);

                auditDAO.log(SessionContext.getUsername(), "PURCHASE PAID", "Purchase #" + id + " settled with Ref " + ref.trim());
                UIHelper.info(this, "Purchase #" + id + " marked as PAID! Official payment receipt #" + p.getReceiptNo() + " generated.");
                load();
            } catch (Exception ex) {
                UIHelper.error(this, "Error settling payment: " + ex.getMessage());
            }
        }
    }
}

