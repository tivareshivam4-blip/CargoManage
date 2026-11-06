package cargomanage.ui;

import cargomanage.dao.*;
import cargomanage.model.Stock;
import cargomanage.security.SessionContext;
import cargomanage.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DashboardPanel extends JPanel {
    private JTable stockTable = new JTable();
    private JPanel cardsPanel = new JPanel();

    private ProductDAO productDAO = new ProductDAO();
    private BranchDAO branchDAO = new BranchDAO();
    private StockDAO stockDAO = new StockDAO();
    private PaymentDAO paymentDAO = new PaymentDAO();
    private TransferDAO transferDAO = new TransferDAO();

    public DashboardPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UIHelper.LIGHT);

        JPanel header = UIHelper.sectionHeader("Enterprise Operations Dashboard",
                "Manage. Track. Control. – Real-time warehouse metrics, inventory valuation, and monitoring");
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(0, 12, 10, 12));

        cardsPanel.setOpaque(false);
        center.add(cardsPanel, BorderLayout.NORTH);

        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(UIHelper.WHITE);
        tableContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIHelper.BORDER),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JLabel tableTitle = new JLabel("Live Warehouse Inventory Status & Low-Stock Alerts");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableTitle.setForeground(UIHelper.NAVY);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        tableContainer.add(tableTitle, BorderLayout.NORTH);

        JScrollPane sp = new JScrollPane(stockTable);
        sp.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER));
        tableContainer.add(sp, BorderLayout.CENTER);
        UIHelper.styleTable(stockTable);

        center.add(tableContainer, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);

        load();
    }

    public void load() {
        cardsPanel.removeAll();
        Integer branchFilter = SessionContext.isBranchStaff() ? SessionContext.getBranchId() : null;

        int prodCount = productDAO.count();
        int branchCount = branchDAO.count();
        int totalStock = stockDAO.getTotalStock(branchFilter);
        int lowStockCount = stockDAO.getLowStockCount(branchFilter);
        double totalInvValue = productDAO.getTotalInventoryValue();
        int pendingTransfers = transferDAO.getPendingCount(branchFilter);

        double totalIncoming = paymentDAO.getTotalIncoming();
        double totalOutgoing = paymentDAO.getTotalOutgoing();
        double pendingPayments = paymentDAO.getTotalPending();

        if (SessionContext.isAccountant()) {
            cardsPanel.setLayout(new GridLayout(2, 4, 10, 10));
            cardsPanel.add(UIHelper.createCard("Total Incoming", String.format("INR %,.2f", totalIncoming), UIHelper.GREEN));
            cardsPanel.add(UIHelper.createCard("Total Outgoing", String.format("INR %,.2f", totalOutgoing), UIHelper.RED));
            cardsPanel.add(UIHelper.createCard("Pending Payments", String.format("INR %,.2f", pendingPayments), UIHelper.ORANGE));
            cardsPanel.add(UIHelper.createCard("Net Cashflow", String.format("INR %,.2f", totalIncoming - totalOutgoing), UIHelper.BLUE));

            cardsPanel.add(UIHelper.createCard("Total Products", String.valueOf(prodCount), UIHelper.NAVY));
            cardsPanel.add(UIHelper.createCard("Total Branches", String.valueOf(branchCount), UIHelper.CYAN));
            cardsPanel.add(UIHelper.createCard("Inventory Valuation", String.format("INR %,.2f", totalInvValue), UIHelper.BLUE));
            cardsPanel.add(UIHelper.createCard("Total Units", String.valueOf(totalStock), UIHelper.NAVY));
        } else if (SessionContext.isBranchStaff()) {
            cardsPanel.setLayout(new GridLayout(1, 4, 10, 10));
            cardsPanel.add(UIHelper.createCard("Branch Stock", String.valueOf(totalStock) + " Units", UIHelper.BLUE));
            cardsPanel.add(UIHelper.createCard("Low Stock Items", String.valueOf(lowStockCount), lowStockCount > 0 ? UIHelper.RED : UIHelper.GREEN));
            cardsPanel.add(UIHelper.createCard("Pending Transfers", String.valueOf(pendingTransfers), UIHelper.ORANGE));
            cardsPanel.add(UIHelper.createCard("Catalog Products", String.valueOf(prodCount), UIHelper.NAVY));
        } else {
            cardsPanel.setLayout(new GridLayout(2, 5, 10, 10));
            cardsPanel.add(UIHelper.createCard("Total Products", String.valueOf(prodCount), UIHelper.NAVY));
            cardsPanel.add(UIHelper.createCard("Total Branches", String.valueOf(branchCount), UIHelper.CYAN));
            cardsPanel.add(UIHelper.createCard("Total Stock", String.valueOf(totalStock) + " Units", UIHelper.BLUE));
            cardsPanel.add(UIHelper.createCard("Low Stock Alerts", String.valueOf(lowStockCount), lowStockCount > 0 ? UIHelper.RED : UIHelper.GREEN));
            cardsPanel.add(UIHelper.createCard("Pending Transfers", String.valueOf(pendingTransfers), UIHelper.ORANGE));

            cardsPanel.add(UIHelper.createCard("Inventory Valuation", String.format("INR %,.2f", totalInvValue), UIHelper.NAVY));
            cardsPanel.add(UIHelper.createCard("Total Incoming", String.format("INR %,.2f", totalIncoming), UIHelper.GREEN));
            cardsPanel.add(UIHelper.createCard("Total Outgoing", String.format("INR %,.2f", totalOutgoing), UIHelper.RED));
            cardsPanel.add(UIHelper.createCard("Pending Payments", String.format("INR %,.2f", pendingPayments), UIHelper.ORANGE));
            cardsPanel.add(UIHelper.createCard("Net Financial Balance", String.format("INR %,.2f", totalIncoming - totalOutgoing), UIHelper.BLUE));
        }

        cardsPanel.revalidate();
        cardsPanel.repaint();

        DefaultTableModel m = new DefaultTableModel(
                new String[]{"Branch", "Product Name", "Category", "Quantity", "Reorder Level", "Unit Price", "Stock Value", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        List<Stock> stocks = stockDAO.findAllStock(branchFilter);
        for (Stock s : stocks) {
            boolean isLow = s.isLowStock();
            double val = s.getQuantity() * s.getPurchasePrice();
            m.addRow(new Object[]{
                    s.getBranchName(),
                    s.getProductName(),
                    s.getProductCategory(),
                    s.getQuantity() + " " + s.getUnit(),
                    s.getReorderLevel() + " " + s.getUnit(),
                    String.format("INR %.2f", s.getPurchasePrice()),
                    String.format("INR %.2f", val),
                    isLow ? "LOW STOCK" : "OPTIMAL"
            });
        }
        stockTable.setModel(m);
    }
}

