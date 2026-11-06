package cargomanage.ui;

import cargomanage.model.Transfer;
import cargomanage.util.PdfGenerator;
import cargomanage.util.PrintManager;
import cargomanage.util.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class TransferReceiptDialog extends JDialog {
    private JPanel printableCard = new JPanel();

    public TransferReceiptDialog(Frame parent, Transfer t) {
        super(parent, "CargoManage - Transfer Dispatch Document - #" + t.getId(), true);
        setSize(580, 560);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(UIHelper.LIGHT);

        JPanel header = UIHelper.sectionHeader("Stock Transfer Note", "Official Warehouse Dispatch and Transit Document");
        add(header, BorderLayout.NORTH);

        printableCard.setLayout(new BorderLayout(10, 10));
        printableCard.setBackground(UIHelper.WHITE);
        printableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(15, 20, 15, 20),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UIHelper.NAVY, 2),
                        BorderFactory.createEmptyBorder(15, 20, 15, 20)
                )
        ));

        JPanel banner = new JPanel(new GridLayout(3, 1));
        banner.setOpaque(false);
        JLabel cName = new JLabel("CARGOMANAGE ENTERPRISE LOGISTICS", SwingConstants.CENTER);
        cName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        cName.setForeground(UIHelper.NAVY);
        JLabel cSub = new JLabel("Manage. Track. Control.  |  Inter-Warehouse Stock Transfer & Dispatch Order", SwingConstants.CENTER);
        cSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cSub.setForeground(UIHelper.TEXT_MUTED);
        JLabel orderId = new JLabel("TRANSFER ORDER #" + t.getId() + "   |   STATUS: " + t.getStatus(), SwingConstants.CENTER);
        orderId.setFont(new Font("Segoe UI", Font.BOLD, 13));
        orderId.setForeground(UIHelper.BLUE);

        banner.add(cName); banner.add(cSub); banner.add(orderId);
        printableCard.add(banner, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(7, 2, 8, 8));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        addDocRow(grid, "Source Warehouse:", t.getSourceBranchName());
        addDocRow(grid, "Destination Warehouse:", t.getDestBranchName());
        addDocRow(grid, "Product Name:", t.getProductName());
        addDocRow(grid, "Transfer Quantity:", t.getQuantity() + " " + t.getProductUnit());
        addDocRow(grid, "Requested By:", t.getRequestedBy() + " (" + t.getRequestDate() + ")");
        addDocRow(grid, "Approved By:", t.getApprovedBy() != null ? t.getApprovedBy() : "Pending");
        addDocRow(grid, "Dispatch Date:", t.getDispatchDate() != null ? t.getDispatchDate() : "Not Dispatched");

        printableCard.add(grid, BorderLayout.CENTER);

        JPanel sigPanel = new JPanel(new GridLayout(2, 2, 20, 4));
        sigPanel.setOpaque(false);
        sigPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER),
                BorderFactory.createEmptyBorder(10, 0, 0, 0)
        ));

        sigPanel.add(new JLabel("Dispatch Authorized: " + (t.getApprovedBy() != null ? t.getApprovedBy() : "__________")));
        sigPanel.add(new JLabel("Destination Received: " + (t.getReceivedBy() != null ? t.getReceivedBy() : "__________")));
        sigPanel.add(new JLabel("Date: " + (t.getDispatchDate() != null ? t.getDispatchDate() : "__________")));
        sigPanel.add(new JLabel("Date: " + (t.getReceivedDate() != null ? t.getReceivedDate() : "__________")));

        printableCard.add(sigPanel, BorderLayout.SOUTH);

        JScrollPane scroll = new JScrollPane(printableCard);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(UIHelper.WHITE);
        bottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER));

        JButton printBtn = UIHelper.button("Print Document", UIHelper.NAVY);
        JButton pdfBtn = UIHelper.button("Export PDF", UIHelper.BLUE);
        JButton closeBtn = UIHelper.secondaryButton("Close");

        printBtn.addActionListener(e -> PrintManager.printComponent(printableCard, "Transfer_Order_" + t.getId()));
        pdfBtn.addActionListener(e -> exportPdf(t));
        closeBtn.addActionListener(e -> dispose());

        bottom.add(printBtn);
        bottom.add(pdfBtn);
        bottom.add(closeBtn);
        add(bottom, BorderLayout.SOUTH);
    }

    private void addDocRow(JPanel p, String l, String v) {
        JLabel lbl = new JLabel(l);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(UIHelper.TEXT_MUTED);

        JLabel val = new JLabel(v != null ? v : "-");
        val.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        val.setForeground(UIHelper.TEXT);

        p.add(lbl); p.add(val);
    }

    private void exportPdf(Transfer t) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Transfer_Note_" + t.getId() + ".pdf"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            try {
                PdfGenerator.generateTransferDispatchPdf(t, f);
                UIHelper.info(this, "Transfer dispatch note exported successfully to:\n" + f.getAbsolutePath());
            } catch (Exception ex) {
                UIHelper.error(this, "Error generating PDF: " + ex.getMessage());
            }
        }
    }
}

