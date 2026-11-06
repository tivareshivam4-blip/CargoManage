package cargomanage.ui;

import cargomanage.dao.ProductDAO;
import cargomanage.model.Product;
import cargomanage.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class ProductDetailDialog extends JDialog {

    public ProductDetailDialog(Frame parent, Product p, ProductDAO productDAO) {
        super(parent, "CargoManage - Product Details - " + p.getName(), true);
        setSize(650, 620);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(UIHelper.LIGHT);

        JPanel header = UIHelper.sectionHeader(p.getName(), "Category: " + p.getCategory() + "  |  SKU: " + p.getSku() + "  |  Status: " + p.getStatus());
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 15));

        JPanel specs = new JPanel(new GridLayout(8, 2, 8, 6));
        specs.setBackground(UIHelper.WHITE);
        specs.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(UIHelper.BORDER), "Product Specifications"),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        addSpec(specs, "Product ID:", String.valueOf(p.getId()));
        addSpec(specs, "Brand:", p.getBrand());
        addSpec(specs, "Unit of Measure:", p.getUnit());
        addSpec(specs, "Barcode:", p.getBarcode());
        addSpec(specs, "Purchase Price:", String.format("INR %.2f", p.getPurchasePrice()));
        addSpec(specs, "Selling Price:", String.format("INR %.2f", p.getSellingPrice()));
        addSpec(specs, "Reorder Level:", p.getReorderLevel() + " " + p.getUnit());
        addSpec(specs, "Current Total Stock:", p.getTotalStock() + " " + p.getUnit());
        addSpec(specs, "Supplier:", p.getSupplierName());
        addSpec(specs, "Warehouse Location:", p.getLocation());
        addSpec(specs, "Mfg Date:", p.getMfgDate() != null ? p.getMfgDate() : "N/A");
        addSpec(specs, "Exp Date:", p.getExpDate() != null ? p.getExpDate() : "N/A");
        addSpec(specs, "Inventory Value:", String.format("INR %,.2f", p.getTotalStock() * p.getPurchasePrice()));
        addSpec(specs, "Stock Status:", p.getTotalStock() <= p.getReorderLevel() ? "LOW STOCK ALERT" : "SUFFICIENT");

        center.add(specs, BorderLayout.NORTH);

        JPanel distPanel = new JPanel(new BorderLayout());
        distPanel.setBackground(UIHelper.WHITE);
        distPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(UIHelper.BORDER), "Stock Availability Across Branches"),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        JTable distTable = new JTable();
        DefaultTableModel m = new DefaultTableModel(new String[]{"Branch ID", "Branch Name", "Location", "Quantity Available"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        List<Map<String, Object>> dist = productDAO.getBranchStockDistribution(p.getId());
        for (Map<String, Object> map : dist) {
            m.addRow(new Object[]{
                    map.get("branchId"),
                    map.get("branchName"),
                    map.get("location"),
                    map.get("quantity") + " " + p.getUnit()
            });
        }
        distTable.setModel(m);
        UIHelper.styleTable(distTable);

        JScrollPane sp = new JScrollPane(distTable);
        sp.setPreferredSize(new Dimension(500, 180));
        distPanel.add(sp, BorderLayout.CENTER);

        center.add(distPanel, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(UIHelper.WHITE);
        bottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER));
        JButton close = UIHelper.button("Close", UIHelper.NAVY);
        close.addActionListener(e -> dispose());
        bottom.add(close);
        add(bottom, BorderLayout.SOUTH);
    }

    private void addSpec(JPanel panel, String label, String value) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(UIHelper.TEXT_MUTED);

        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        v.setForeground(UIHelper.TEXT);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        row.setOpaque(false);
        row.add(l);
        row.add(v);
        panel.add(row);
    }
}

