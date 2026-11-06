package cargomanage.ui;

import cargomanage.dao.*;
import cargomanage.model.*;
import cargomanage.security.SessionContext;
import cargomanage.util.PdfGenerator;
import cargomanage.util.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class ReportPanel extends JPanel {
    private JComboBox<String> reportTypeCombo = new JComboBox<>(new String[]{
            "Product Stock Report",
            "Branch-wise Inventory Report",
            "Purchase Procurement Report",
            "Inter-Warehouse Transfer Report",
            "Payment & Financial Ledger",
            "Supplier Payment Status Report",
            "Critical Low-Stock Report",
            "Executive Financial Summary"
    });

    private JTextArea area = new JTextArea();
    private String currentReportTitle = "Product Stock Report";

    private ProductDAO productDAO = new ProductDAO();
    private BranchDAO branchDAO = new BranchDAO();
    private StockDAO stockDAO = new StockDAO();
    private PurchaseDAO purchaseDAO = new PurchaseDAO();
    private TransferDAO transferDAO = new TransferDAO();
    private PaymentDAO paymentDAO = new PaymentDAO();

    public ReportPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UIHelper.LIGHT);

        JPanel header = UIHelper.sectionHeader("Enterprise Business Reports & Analytics",
                "Generate, review, and export warehouse stock, inventory movement, and financial statements");
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(0, 12, 10, 12));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        top.setBackground(UIHelper.WHITE);
        top.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIHelper.BORDER),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        top.add(new JLabel("Select Report:"));
        top.add(reportTypeCombo);

        JButton generateBtn = UIHelper.button("Generate Report", UIHelper.BLUE);
        top.add(generateBtn);

        center.add(top, BorderLayout.NORTH);

        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setEditable(false);
        area.setBackground(Color.WHITE);
        area.setForeground(UIHelper.TEXT);
        area.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JScrollPane sp = new JScrollPane(area);
        sp.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER));
        center.add(sp, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bottom.setBackground(UIHelper.WHITE);
        bottom.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIHelper.BORDER),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        JButton pdfBtn = UIHelper.button("Export to PDF", UIHelper.NAVY);
        JButton csvBtn = UIHelper.button("Export to CSV", UIHelper.GREEN);
        bottom.add(pdfBtn);
        bottom.add(csvBtn);

        center.add(bottom, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        generateBtn.addActionListener(e -> generateReport());
        reportTypeCombo.addActionListener(e -> generateReport());
        pdfBtn.addActionListener(e -> exportPdf());
        csvBtn.addActionListener(e -> exportCsv());

        generateReport();
    }

    private void generateReport() {
        String type = (String) reportTypeCombo.getSelectedItem();
        currentReportTitle = type;
        StringBuilder sb = new StringBuilder();

        sb.append("======================================================================================================\n");
        sb.append(String.format("                                    CARGOMANAGE ENTERPRISE REPORT\n"));
        sb.append(String.format("                                       Manage. Track. Control.\n"));
        sb.append(String.format("                                     %s\n", type.toUpperCase()));
        sb.append(String.format("                               Generated at: %s by %s\n", DatabaseManager.now(), SessionContext.getUsername()));
        sb.append("======================================================================================================\n\n");

        Integer branchFilter = SessionContext.isBranchStaff() ? SessionContext.getBranchId() : null;

        if ("Product Stock Report".equals(type)) {
            List<Product> products = productDAO.findAll();
            sb.append(String.format("%-6s | %-24s | %-14s | %-8s | %-12s | %-12s | %-10s%n",
                    "ID", "PRODUCT NAME", "CATEGORY", "UNIT", "PURCHASE PR", "SELLING PR", "STOCK"));
            sb.append("------------------------------------------------------------------------------------------------------\n");
            double totalVal = 0.0;
            for (Product p : products) {
                totalVal += p.getTotalStock() * p.getPurchasePrice();
                sb.append(String.format("%-6d | %-24s | %-14s | %-8s | INR %8.2f | INR %8.2f | %6d %s%n",
                        p.getId(), p.getName(), p.getCategory(), p.getUnit(), p.getPurchasePrice(), p.getSellingPrice(), p.getTotalStock(), p.getUnit()));
            }
            sb.append("------------------------------------------------------------------------------------------------------\n");
            sb.append(String.format("Total Catalog Items: %d   |   Estimated Inventory Valuation: INR %,.2f%n", products.size(), totalVal));

        } else if ("Branch-wise Inventory Report".equals(type)) {
            List<Branch> branches = branchDAO.findAll();
            for (Branch b : branches) {
                if (branchFilter != null && b.getId() != branchFilter) continue;
                sb.append(String.format("\nBRANCH: %s [%s] - %s%n", b.getName(), b.getCode(), b.getLocation()));
                sb.append("------------------------------------------------------------------------------------------------------\n");
                List<Stock> stocks = stockDAO.findAllStock(b.getId());
                int branchTotal = 0;
                for (Stock s : stocks) {
                    branchTotal += s.getQuantity();
                    sb.append(String.format("  * %-25s : %6d %-4s (Reorder: %3d) [%s]%n",
                            s.getProductName(), s.getQuantity(), s.getUnit(), s.getReorderLevel(), s.isLowStock() ? "LOW" : "OK"));
                }
                sb.append(String.format("  Total Branch Units: %d%n", branchTotal));
            }

        } else if ("Purchase Procurement Report".equals(type)) {
            List<Purchase> purchases = purchaseDAO.findAll(branchFilter);
            sb.append(String.format("%-6s | %-22s | %-20s | %-10s | %-12s | %-10s | %-10s%n",
                    "ID", "SUPPLIER", "PRODUCT", "QUANTITY", "TOTAL (INR)", "DATE", "PAYMENT"));
            sb.append("------------------------------------------------------------------------------------------------------\n");
            double totalPurch = 0.0;
            for (Purchase p : purchases) {
                totalPurch += p.getTotalAmount();
                sb.append(String.format("%-6d | %-22s | %-20s | %6d %-3s | %10.2f | %-10s | %-10s%n",
                        p.getId(), p.getSupplierName(), p.getProductName(), p.getQuantity(), p.getUnit(),
                        p.getTotalAmount(), p.getPurchaseDate().substring(0, 10), p.getPaymentStatus()));
            }
            sb.append("------------------------------------------------------------------------------------------------------\n");
            sb.append(String.format("Total Purchases: %d   |   Cumulative Inbound Cost: INR %,.2f%n", purchases.size(), totalPurch));

        } else if ("Inter-Warehouse Transfer Report".equals(type)) {
            List<Transfer> transfers = transferDAO.findAll(branchFilter);
            sb.append(String.format("%-6s | %-20s | %-18s | %-18s | %-8s | %-10s%n",
                    "ID", "PRODUCT", "SOURCE", "DESTINATION", "QTY", "STATUS"));
            sb.append("------------------------------------------------------------------------------------------------------\n");
            for (Transfer t : transfers) {
                sb.append(String.format("%-6d | %-20s | %-18s | %-18s | %5d %-2s | %-10s%n",
                        t.getId(), t.getProductName(), t.getSourceBranchName(), t.getDestBranchName(), t.getQuantity(), t.getProductUnit(), t.getStatus()));
            }

        } else if ("Payment & Financial Ledger".equals(type)) {
            List<Payment> payments = paymentDAO.findAll("All", "All", null);
            sb.append(String.format("%-14s | %-16s | %-22s | %-10s | %-14s | %-8s | %-10s%n",
                    "RECEIPT #", "TXN ID", "PARTY NAME", "DIRECTION", "AMOUNT (INR)", "STATUS", "METHOD"));
            sb.append("------------------------------------------------------------------------------------------------------\n");
            for (Payment p : payments) {
                sb.append(String.format("%-14s | %-16s | %-22s | %-10s | %14.2f | %-8s | %-10s%n",
                        p.getReceiptNo(), p.getTransactionId(), p.getPartyName(), p.getDirection(), p.getAmount(), p.getStatus(), p.getPaymentMethod()));
            }

        } else if ("Supplier Payment Status Report".equals(type)) {
            List<Payment> payments = paymentDAO.findAll("Outgoing", "All", null);
            sb.append(String.format("%-14s | %-22s | %-14s | %-12s | %-14s | %-10s%n",
                    "RECEIPT #", "SUPPLIER NAME", "AMOUNT (INR)", "STATUS", "REFERENCE #", "DATE"));
            sb.append("------------------------------------------------------------------------------------------------------\n");
            for (Payment p : payments) {
                sb.append(String.format("%-14s | %-22s | %14.2f | %-12s | %-14s | %-10s%n",
                        p.getReceiptNo(), p.getPartyName(), p.getAmount(), p.getStatus(), p.getReferenceNo(), p.getPaymentDate().substring(0, 10)));
            }

        } else if ("Critical Low-Stock Report".equals(type)) {
            List<Stock> lowStocks = stockDAO.getLowStockItems(branchFilter);
            sb.append(String.format("%-22s | %-24s | %-10s | %-14s | %-12s%n",
                    "BRANCH", "PRODUCT", "CURRENT", "REORDER LEVEL", "STATUS"));
            sb.append("------------------------------------------------------------------------------------------------------\n");
            for (Stock s : lowStocks) {
                sb.append(String.format("%-22s | %-24s | %6d %-3s | %8d %-4s | CRITICAL LOW%n",
                        s.getBranchName(), s.getProductName(), s.getQuantity(), s.getUnit(), s.getReorderLevel(), s.getUnit()));
            }
            sb.append("------------------------------------------------------------------------------------------------------\n");
            sb.append(String.format("Total Low Stock Alerts: %d items requiring replenishment.%n", lowStocks.size()));

        } else if ("Executive Financial Summary".equals(type)) {
            double incoming = paymentDAO.getTotalIncoming();
            double outgoing = paymentDAO.getTotalOutgoing();
            double pending = paymentDAO.getTotalPending();
            double invVal = productDAO.getTotalInventoryValue();
            int totalStock = stockDAO.getTotalStock(null);

            sb.append("FINANCIAL HEALTH & SETTLEMENT SUMMARY:\n");
            sb.append("------------------------------------------------------------------------------------------------------\n");
            sb.append(String.format("  Total Incoming Revenues Paid        : INR %,16.2f%n", incoming));
            sb.append(String.format("  Total Outgoing Vendor Costs Paid    : INR %,16.2f%n", outgoing));
            sb.append(String.format("  Net Operational Cash Balance        : INR %,16.2f%n", incoming - outgoing));
            sb.append(String.format("  Pending Inbound/Outbound Payments   : INR %,16.2f%n", pending));
            sb.append("------------------------------------------------------------------------------------------------------\n");
            sb.append("WAREHOUSE INVENTORY VALUATION:\n");
            sb.append("------------------------------------------------------------------------------------------------------\n");
            sb.append(String.format("  Active Warehouses                   : %10d%n", branchDAO.count()));
            sb.append(String.format("  Active Product Lines                : %10d%n", productDAO.count()));
            sb.append(String.format("  Total Stock Inventory Units         : %10d%n", totalStock));
            sb.append(String.format("  Total Asset Value (Purchase Cost)   : INR %,16.2f%n", invVal));
            sb.append("======================================================================================================\n");
        }

        area.setText(sb.toString());
        area.setCaretPosition(0);
    }

    private void exportPdf() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(currentReportTitle.replace(" ", "_") + ".pdf"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            try {
                PdfGenerator.generateReportPdf(currentReportTitle, area.getText(), f);
                UIHelper.info(this, "Report exported to PDF successfully:\n" + f.getAbsolutePath());
            } catch (Exception ex) {
                UIHelper.error(this, "Error exporting PDF: " + ex.getMessage());
            }
        }
    }

    private void exportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(currentReportTitle.replace(" ", "_") + ".csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            try (FileWriter fw = new FileWriter(f)) {
                String content = area.getText();
                String[] lines = content.split("\n");
                for (String line : lines) {
                    if (line.contains("|")) {
                        String[] parts = line.split("\\|");
                        StringBuilder csvLine = new StringBuilder();
                        for (int i = 0; i < parts.length; i++) {
                            if (i > 0) csvLine.append(",");
                            csvLine.append("\"").append(parts[i].trim().replace("\"", "\"\"")).append("\"");
                        }
                        fw.write(csvLine.toString() + "\n");
                    }
                }
                UIHelper.info(this, "Report exported to CSV successfully:\n" + f.getAbsolutePath());
            } catch (Exception ex) {
                UIHelper.error(this, "Error exporting CSV: " + ex.getMessage());
            }
        }
    }
}

