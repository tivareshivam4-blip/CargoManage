package cargomanage.ui;

import cargomanage.dao.AuditDAO;
import cargomanage.dao.BranchDAO;
import cargomanage.dao.ProductDAO;
import cargomanage.dao.StockDAO;
import cargomanage.model.Branch;
import cargomanage.model.Product;
import cargomanage.model.Stock;
import cargomanage.security.SessionContext;
import cargomanage.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StockPanel extends JPanel {
    private JComboBox<String> branchCombo = new JComboBox<>();
    private JComboBox<String> productCombo = new JComboBox<>();
    private JTextField qty = new JTextField();
    private JTable table = new JTable();

    private BranchDAO branchDAO = new BranchDAO();
    private ProductDAO productDAO = new ProductDAO();
    private StockDAO stockDAO = new StockDAO();
    private AuditDAO auditDAO = new AuditDAO();

    public StockPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UIHelper.LIGHT);

        JPanel header = UIHelper.sectionHeader("Warehouse Stock Inventory", "Live branch inventory levels, batch stock updates, and threshold management");
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(0, 12, 10, 12));

        if (SessionContext.canUpdateStock()) {
            JPanel form = new JPanel(new GridLayout(2, 4, 8, 8));
            form.setBackground(UIHelper.WHITE);
            form.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UIHelper.BORDER),
                    BorderFactory.createEmptyBorder(10, 14, 10, 14)
            ));

            form.add(new JLabel("Select Warehouse:"));
            form.add(new JLabel("Select Product:"));
            form.add(new JLabel("Set Stock Quantity:"));
            JButton updateBtn = UIHelper.button("Update Stock", UIHelper.BLUE);
            form.add(updateBtn);

            form.add(branchCombo);
            form.add(productCombo);
            form.add(qty);
            JButton refreshBtn = UIHelper.button("Refresh", UIHelper.NAVY);
            form.add(refreshBtn);

            center.add(form, BorderLayout.NORTH);

            updateBtn.addActionListener(e -> updateStock());
            refreshBtn.addActionListener(e -> { refreshCombos(); load(); });
        }

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER));
        center.add(sp, BorderLayout.CENTER);
        UIHelper.styleTable(table);

        add(center, BorderLayout.CENTER);

        refreshCombos();
        load();
    }

    public void refreshCombos() {
        branchCombo.removeAllItems();
        productCombo.removeAllItems();

        List<Branch> branches = branchDAO.findAll();
        for (Branch b : branches) {
            if (!SessionContext.isBranchStaff() || b.getId() == SessionContext.getBranchId()) {
                branchCombo.addItem(b.getId() + " - " + b.getName());
            }
        }

        List<Product> products = productDAO.findAll();
        for (Product p : products) {
            productCombo.addItem(p.getId() + " - " + p.getName());
        }
    }

    public void load() {
        DefaultTableModel m = new DefaultTableModel(
                new String[]{"Branch ID", "Branch Name", "Product Name", "Category", "Quantity", "Unit", "Reorder Level", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        Integer branchFilter = SessionContext.isBranchStaff() ? SessionContext.getBranchId() : null;
        List<Stock> list = stockDAO.findAllStock(branchFilter);

        for (Stock s : list) {
            m.addRow(new Object[]{
                    s.getBranchId(),
                    s.getBranchName(),
                    s.getProductName(),
                    s.getProductCategory(),
                    s.getQuantity(),
                    s.getUnit(),
                    s.getReorderLevel(),
                    s.isLowStock() ? "LOW STOCK" : "OPTIMAL"
            });
        }
        table.setModel(m);
    }

    private void updateStock() {
        try {
            int b = UIHelper.selectedId(branchCombo);
            int p = UIHelper.selectedId(productCombo);
            int q = Integer.parseInt(qty.getText().trim());

            if (b <= 0 || p <= 0) {
                UIHelper.error(this, "Please select both a branch and a product.");
                return;
            }
            if (q < 0) {
                UIHelper.error(this, "Stock quantity cannot be negative.");
                return;
            }

            boolean ok = stockDAO.setStock(b, p, q);
            if (ok) {
                auditDAO.log(SessionContext.getUsername(), "STOCK ADJUSTMENT", "Updated stock for Branch #" + b + ", Product #" + p + " to " + q);
                UIHelper.info(this, "Stock updated successfully!");
                qty.setText("");
                load();
            }
        } catch (NumberFormatException e) {
            UIHelper.error(this, "Quantity must be a valid integer number.");
        } catch (Exception e) {
            UIHelper.error(this, "Error updating stock: " + e.getMessage());
        }
    }
}

