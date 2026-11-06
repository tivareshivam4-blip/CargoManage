package cargomanage.ui;

import cargomanage.model.Payment;
import cargomanage.util.PdfGenerator;
import cargomanage.util.PrintManager;
import cargomanage.util.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class PaymentReceiptDialog extends JDialog {
    private JPanel receiptPanel = new JPanel();

    public PaymentReceiptDialog(Frame parent, Payment p) {
        super(parent, "CargoManage - Official Payment Receipt - " + p.getReceiptNo(), true);
        setSize(560, 640);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(UIHelper.LIGHT);

        JPanel top = UIHelper.sectionHeader("Official Business Receipt", "Verified financial settlement document");
        add(top, BorderLayout.NORTH);

        receiptPanel.setLayout(new BorderLayout(12, 12));
        receiptPanel.setBackground(UIHelper.WHITE);
        receiptPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(15, 20, 15, 20),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UIHelper.NAVY, 2),
                        BorderFactory.createEmptyBorder(15, 20, 15, 20)
                )
        ));

        JPanel compBox = new JPanel(new GridLayout(3, 1));
        compBox.setOpaque(false);
        JLabel cName = new JLabel("CARGOMANAGE ENTERPRISE LOGISTICS", SwingConstants.CENTER);
        cName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        cName.setForeground(UIHelper.NAVY);
        JLabel cSub = new JLabel("Manage. Track. Control.  |  Enterprise Warehouse, Inventory & Business Management", SwingConstants.CENTER);
        cSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cSub.setForeground(UIHelper.TEXT_MUTED);
        JLabel title = new JLabel("PAYMENT RECEIPT", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(UIHelper.BLUE);

        compBox.add(cName); compBox.add(cSub); compBox.add(title);
        receiptPanel.add(compBox, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridLayout(10, 2, 8, 6));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        addRow(body, "Receipt Number:", p.getReceiptNo(), true);
        addRow(body, "Transaction ID:", p.getTransactionId(), false);
        addRow(body, "Date & Time:", p.getPaymentDate(), false);
        addRow(body, "Customer / Supplier:", p.getPartyName(), true);
        addRow(body, "Direction:", p.getDirection(), false);
        addRow(body, "Payment Method:", p.getPaymentMethod(), false);
        addRow(body, "Reference / UTR #:", p.getReferenceNo() != null && !p.getReferenceNo().isBlank() ? p.getReferenceNo() : "N/A", false);
        if (p.getRelatedType() != null && !p.getRelatedType().isBlank()) {
            addRow(body, "Related Record:", p.getRelatedType() + " #" + (p.getRelatedId() != null ? p.getRelatedId() : "-"), false);
        } else {
            addRow(body, "Related Record:", "Direct Transaction", false);
        }
        addRow(body, "Description:", p.getDescription() != null ? p.getDescription() : "-", false);
        addRow(body, "Payment Status:", p.getStatus().toUpperCase(), true);

        receiptPanel.add(body, BorderLayout.CENTER);

        JPanel amountCard = new JPanel(new BorderLayout(5, 5));
        amountCard.setBackground(new Color(244, 247, 251));
        amountCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIHelper.BLUE),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JLabel amtLbl = new JLabel("TOTAL AMOUNT:");
        amtLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        amtLbl.setForeground(UIHelper.TEXT_MUTED);

        JLabel amtVal = new JLabel(String.format("INR %,.2f", p.getAmount()), SwingConstants.RIGHT);
        amtVal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        amtVal.setForeground("Incoming".equalsIgnoreCase(p.getDirection()) ? UIHelper.GREEN : UIHelper.NAVY);

        amountCard.add(amtLbl, BorderLayout.WEST);
        amountCard.add(amtVal, BorderLayout.EAST);

        JPanel footerBox = new JPanel(new BorderLayout(5, 5));
        footerBox.setOpaque(false);
        footerBox.add(amountCard, BorderLayout.NORTH);

        JLabel auth = new JLabel("Authorized Accountant: " + p.getRecordedBy() + "  |  System Generated Document", SwingConstants.CENTER);
        auth.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        auth.setForeground(UIHelper.TEXT_MUTED);
        auth.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        footerBox.add(auth, BorderLayout.SOUTH);

        receiptPanel.add(footerBox, BorderLayout.SOUTH);

        JScrollPane sp = new JScrollPane(receiptPanel);
        sp.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        add(sp, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(UIHelper.WHITE);
        bottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER));

        JButton printBtn = UIHelper.button("Print Receipt", UIHelper.NAVY);
        JButton pdfBtn = UIHelper.button("Export as PDF", UIHelper.BLUE);
        JButton closeBtn = UIHelper.secondaryButton("Close");

        printBtn.addActionListener(e -> PrintManager.printComponent(receiptPanel, "Receipt_" + p.getReceiptNo()));
        pdfBtn.addActionListener(e -> exportPdf(p));
        closeBtn.addActionListener(e -> dispose());

        bottom.add(printBtn);
        bottom.add(pdfBtn);
        bottom.add(closeBtn);
        add(bottom, BorderLayout.SOUTH);
    }

    private void addRow(JPanel p, String l, String v, boolean bold) {
        JLabel lbl = new JLabel(l);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(UIHelper.TEXT_MUTED);

        JLabel val = new JLabel(v);
        val.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, 12));
        val.setForeground(UIHelper.TEXT);

        p.add(lbl); p.add(val);
    }

    private void exportPdf(Payment p) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(p.getReceiptNo() + ".pdf"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            try {
                PdfGenerator.generatePaymentReceiptPdf(p, f);
                UIHelper.info(this, "Receipt PDF generated successfully at:\n" + f.getAbsolutePath());
            } catch (Exception ex) {
                UIHelper.error(this, "Error exporting PDF: " + ex.getMessage());
            }
        }
    }
}

