package cargomanage.ui;

import cargomanage.dao.AuditDAO;
import cargomanage.dao.PaymentDAO;
import cargomanage.model.Payment;
import cargomanage.security.SessionContext;
import cargomanage.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class PaymentPanel extends JPanel {
    private JTable table = new JTable();
    private PaymentDAO paymentDAO = new PaymentDAO();
    private AuditDAO auditDAO = new AuditDAO();

    private JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 10, 10));

    private JTextField searchField = new JTextField(15);
    private JComboBox<String> directionFilter = new JComboBox<>(new String[]{"All", "Incoming", "Outgoing"});
    private JComboBox<String> statusFilter = new JComboBox<>(new String[]{"All", "Paid", "Pending", "Failed"});

    private JTextField partyField = new JTextField();
    private JComboBox<String> partyTypeCombo = new JComboBox<>(new String[]{"SUPPLIER", "CUSTOMER", "BRANCH", "OTHER"});
    private JComboBox<String> directionCombo = new JComboBox<>(new String[]{"Outgoing", "Incoming"});
    private JComboBox<String> methodCombo = new JComboBox<>(new String[]{"Bank Transfer", "UPI", "Cash", "Card", "Cheque"});
    private JTextField amountField = new JTextField();
    private JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Paid", "Pending", "Failed"});
    private JTextField refField = new JTextField();
    private JTextField descField = new JTextField();

    public PaymentPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UIHelper.LIGHT);

        JPanel header = UIHelper.sectionHeader("Accountant – Payment Management Module",
                "Record incoming/outgoing financial transactions, manage supplier settlements, and generate official receipts");
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(0, 12, 10, 12));

        JPanel topWrapper = new JPanel(new BorderLayout(8, 8));
        topWrapper.setOpaque(false);

        cardsPanel.setOpaque(false);
        topWrapper.add(cardsPanel, BorderLayout.NORTH);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        filterBar.setBackground(UIHelper.WHITE);
        filterBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIHelper.BORDER),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));

        filterBar.add(new JLabel("Search Payments:"));
        filterBar.add(searchField);
        filterBar.add(new JLabel("Direction:"));
        filterBar.add(directionFilter);
        filterBar.add(new JLabel("Status:"));
        filterBar.add(statusFilter);

        JButton filterBtn = UIHelper.button("Apply Filter", UIHelper.NAVY);
        JButton resetFilterBtn = UIHelper.button("Reset", UIHelper.BLUE);
        filterBar.add(filterBtn);
        filterBar.add(resetFilterBtn);

        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setOpaque(false);
        formContainer.add(filterBar, BorderLayout.NORTH);

        if (SessionContext.canManagePayments()) {
            JPanel form = new JPanel(new GridLayout(2, 8, 6, 6));
            form.setBackground(UIHelper.WHITE);
            form.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UIHelper.BORDER),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));

            form.add(new JLabel("Party Name:"));
            form.add(new JLabel("Party Type:"));
            form.add(new JLabel("Direction:"));
            form.add(new JLabel("Payment Method:"));
            form.add(new JLabel("Amount (INR):"));
            form.add(new JLabel("Status:"));
            form.add(new JLabel("Reference / UTR:"));
            form.add(new JLabel("Description:"));

            form.add(partyField);
            form.add(partyTypeCombo);
            form.add(directionCombo);
            form.add(methodCombo);
            form.add(amountField);
            form.add(statusCombo);
            form.add(refField);
            form.add(descField);

            JPanel formBtnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
            formBtnBar.setOpaque(false);
            JButton addPaymentBtn = UIHelper.button("Record Payment", UIHelper.BLUE);
            JButton clearFormBtn = UIHelper.button("Clear Form", UIHelper.NAVY);
            formBtnBar.add(addPaymentBtn);
            formBtnBar.add(clearFormBtn);

            addPaymentBtn.addActionListener(e -> recordPayment());
            clearFormBtn.addActionListener(e -> clearForm());

            JPanel formCombined = new JPanel(new BorderLayout());
            formCombined.setOpaque(false);
            formCombined.add(form, BorderLayout.CENTER);
            formCombined.add(formBtnBar, BorderLayout.SOUTH);

            formContainer.add(formCombined, BorderLayout.CENTER);
        }

        topWrapper.add(formContainer, BorderLayout.CENTER);
        center.add(topWrapper, BorderLayout.NORTH);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER));
        center.add(sp, BorderLayout.CENTER);
        UIHelper.styleTable(table);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bottom.setBackground(UIHelper.WHITE);
        bottom.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIHelper.BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        JButton viewReceiptBtn = UIHelper.button("Generate / View Receipt", UIHelper.BLUE);
        bottom.add(viewReceiptBtn);

        if (SessionContext.canManagePayments()) {
            JButton markPaidBtn = UIHelper.button("Mark as Paid", UIHelper.GREEN);
            JButton editPaymentBtn = UIHelper.button("Edit Payment", UIHelper.ORANGE);
            bottom.add(markPaidBtn);
            bottom.add(editPaymentBtn);

            markPaidBtn.addActionListener(e -> markSelectedPaid());
            editPaymentBtn.addActionListener(e -> editSelectedPayment());
        }

        JButton summaryBtn = UIHelper.button("Financial Summary Report", UIHelper.NAVY);
        JButton refreshBtn = UIHelper.button("Refresh List", UIHelper.NAVY);
        bottom.add(summaryBtn);
        bottom.add(refreshBtn);

        center.add(bottom, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        filterBtn.addActionListener(e -> load());
        resetFilterBtn.addActionListener(e -> {
            searchField.setText("");
            directionFilter.setSelectedIndex(0);
            statusFilter.setSelectedIndex(0);
            load();
        });
        viewReceiptBtn.addActionListener(e -> viewReceipt());
        summaryBtn.addActionListener(e -> showSummaryDialog());
        refreshBtn.addActionListener(e -> load());

        load();
    }

    private void clearForm() {
        partyField.setText("");
        amountField.setText("");
        refField.setText("");
        descField.setText("");
    }

    public void load() {
        cardsPanel.removeAll();
        double incoming = paymentDAO.getTotalIncoming();
        double outgoing = paymentDAO.getTotalOutgoing();
        double pending = paymentDAO.getTotalPending();
        double net = incoming - outgoing;

        cardsPanel.add(UIHelper.createCard("Total Incoming Paid", String.format("INR %,.2f", incoming), UIHelper.GREEN));
        cardsPanel.add(UIHelper.createCard("Total Outgoing Paid", String.format("INR %,.2f", outgoing), UIHelper.RED));
        cardsPanel.add(UIHelper.createCard("Pending Clearance", String.format("INR %,.2f", pending), UIHelper.ORANGE));
        cardsPanel.add(UIHelper.createCard("Net Cash Balance", String.format("INR %,.2f", net), net >= 0 ? UIHelper.BLUE : UIHelper.RED));

        cardsPanel.revalidate();
        cardsPanel.repaint();

        String dir = (String) directionFilter.getSelectedItem();
        String stat = (String) statusFilter.getSelectedItem();
        String q = searchField.getText().trim();

        List<Payment> list = paymentDAO.findAll(dir, stat, q);

        DefaultTableModel m = new DefaultTableModel(
                new String[]{"ID", "Receipt #", "Transaction ID", "Date", "Party Name", "Type", "Direction", "Method", "Amount", "Status", "Reference #", "Recorded By"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Payment p : list) {
            m.addRow(new Object[]{
                    p.getId(),
                    p.getReceiptNo(),
                    p.getTransactionId(),
                    p.getPaymentDate(),
                    p.getPartyName(),
                    p.getPartyType(),
                    p.getDirection(),
                    p.getPaymentMethod(),
                    String.format("INR %,.2f", p.getAmount()),
                    p.getStatus(),
                    p.getReferenceNo() != null ? p.getReferenceNo() : "",
                    p.getRecordedBy()
            });
        }
        table.setModel(m);
    }

    private void recordPayment() {
        String party = partyField.getText().trim();
        String pType = (String) partyTypeCombo.getSelectedItem();
        String dir = (String) directionCombo.getSelectedItem();
        String meth = (String) methodCombo.getSelectedItem();
        String amtStr = amountField.getText().trim();
        String stat = (String) statusCombo.getSelectedItem();
        String ref = refField.getText().trim();
        String desc = descField.getText().trim();

        if (party.isBlank() || amtStr.isBlank()) {
            UIHelper.error(this, "Please enter customer/supplier party name and amount.");
            return;
        }

        try {
            double amt = Double.parseDouble(amtStr);
            if (amt <= 0) {
                UIHelper.error(this, "Amount must be greater than zero.");
                return;
            }

            Payment p = new Payment();
            p.setPartyName(party);
            p.setPartyType(pType);
            p.setDirection(dir);
            p.setPaymentMethod(meth);
            p.setAmount(amt);
            p.setStatus(stat);
            p.setReferenceNo(ref);
            p.setDescription(desc);
            p.setRecordedBy(SessionContext.getUsername());

            boolean ok = paymentDAO.create(p);
            if (ok) {
                auditDAO.log(SessionContext.getUsername(), "PAYMENT RECORDED",
                        "Recorded " + dir + " payment #" + p.getId() + " (" + p.getReceiptNo() + ") for INR " + amt + " - " + party);
                UIHelper.info(this, "Payment recorded successfully! Receipt #" + p.getReceiptNo() + " generated.");
                clearForm();
                load();

                new PaymentReceiptDialog((Frame) SwingUtilities.getWindowAncestor(this), p).setVisible(true);
            }
        } catch (NumberFormatException e) {
            UIHelper.error(this, "Please enter a valid numeric payment amount.");
        } catch (Exception e) {
            UIHelper.error(this, "Error recording payment: " + e.getMessage());
        }
    }

    private void viewReceipt() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIHelper.error(this, "Please select a payment from the table to view receipt.");
            return;
        }
        int id = (int) table.getValueAt(row, 0);
        Payment p = paymentDAO.findById(id);
        if (p != null) {
            new PaymentReceiptDialog((Frame) SwingUtilities.getWindowAncestor(this), p).setVisible(true);
        }
    }

    private void markSelectedPaid() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIHelper.error(this, "Please select a pending payment.");
            return;
        }
        int id = (int) table.getValueAt(row, 0);
        Payment p = paymentDAO.findById(id);
        if (p == null) return;

        if ("Paid".equalsIgnoreCase(p.getStatus())) {
            UIHelper.info(this, "This payment is already marked as Paid.");
            return;
        }

        String ref = JOptionPane.showInputDialog(this, "Enter Payment Reference / UTR Number:", p.getReferenceNo());
        if (ref != null) {
            p.setStatus("Paid");
            p.setReferenceNo(ref.trim());
            try {
                boolean ok = paymentDAO.update(p);
                if (ok) {
                    auditDAO.log(SessionContext.getUsername(), "PAYMENT SETTLED", "Payment #" + p.getId() + " marked as Paid (Ref: " + ref.trim() + ")");
                    UIHelper.info(this, "Payment marked as Paid successfully!");
                    load();
                }
            } catch (Exception ex) {
                UIHelper.error(this, "Error updating payment: " + ex.getMessage());
            }
        }
    }

    private void editSelectedPayment() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIHelper.error(this, "Please select a payment to edit.");
            return;
        }
        int id = (int) table.getValueAt(row, 0);
        Payment p = paymentDAO.findById(id);
        if (p == null) return;

        JTextField editParty = new JTextField(p.getPartyName());
        JTextField editAmount = new JTextField(String.valueOf(p.getAmount()));
        JComboBox<String> editMethod = new JComboBox<>(new String[]{"Bank Transfer", "UPI", "Cash", "Card", "Cheque"});
        editMethod.setSelectedItem(p.getPaymentMethod());
        JComboBox<String> editStatus = new JComboBox<>(new String[]{"Paid", "Pending", "Failed"});
        editStatus.setSelectedItem(p.getStatus());
        JTextField editRef = new JTextField(p.getReferenceNo());
        JTextField editDesc = new JTextField(p.getDescription());

        JPanel editForm = new JPanel(new GridLayout(6, 2, 6, 6));
        editForm.add(new JLabel("Party Name:")); editForm.add(editParty);
        editForm.add(new JLabel("Amount (INR):")); editForm.add(editAmount);
        editForm.add(new JLabel("Payment Method:")); editForm.add(editMethod);
        editForm.add(new JLabel("Payment Status:")); editForm.add(editStatus);
        editForm.add(new JLabel("Reference / UTR:")); editForm.add(editRef);
        editForm.add(new JLabel("Description:")); editForm.add(editDesc);

        int res = JOptionPane.showConfirmDialog(this, editForm, "Edit Payment #" + p.getId(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            try {
                p.setPartyName(editParty.getText().trim());
                p.setAmount(Double.parseDouble(editAmount.getText().trim()));
                p.setPaymentMethod((String) editMethod.getSelectedItem());
                p.setStatus((String) editStatus.getSelectedItem());
                p.setReferenceNo(editRef.getText().trim());
                p.setDescription(editDesc.getText().trim());

                boolean ok = paymentDAO.update(p);
                if (ok) {
                    auditDAO.log(SessionContext.getUsername(), "PAYMENT UPDATED", "Updated payment details for #" + p.getId());
                    UIHelper.info(this, "Payment updated successfully!");
                    load();
                }
            } catch (Exception ex) {
                UIHelper.error(this, "Error updating payment: " + ex.getMessage());
            }
        }
    }

    private void showSummaryDialog() {
        Map<String, Object> summary = paymentDAO.getFinancialSummary();
        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append("        CARGOMANAGE FINANCIAL SUMMARY REPORT        \n");
        sb.append("               Manage. Track. Control.              \n");
        sb.append("====================================================\n\n");
        sb.append(String.format("Total Incoming Paid  : INR %,15.2f\n", summary.get("incoming")));
        sb.append(String.format("Total Outgoing Paid  : INR %,15.2f\n", summary.get("outgoing")));
        sb.append(String.format("Pending Clearance    : INR %,15.2f\n", summary.get("pending")));
        sb.append("----------------------------------------------------\n");
        sb.append(String.format("Net Cashflow Balance : INR %,15.2f\n", summary.get("netBalance")));
        sb.append("====================================================\n");

        JTextArea area = new JTextArea(sb.toString());
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setEditable(false);

        JOptionPane.showMessageDialog(this, new JScrollPane(area), "CargoManage - Payment Summary", JOptionPane.INFORMATION_MESSAGE);
    }
}

