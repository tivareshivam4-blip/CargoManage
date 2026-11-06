package cargomanage.ui;

import cargomanage.dao.AuditDAO;
import cargomanage.dao.ProductDAO;
import cargomanage.model.Product;
import cargomanage.security.SessionContext;
import cargomanage.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ProductPanel extends JPanel {
    private JTextField searchField = new JTextField(15);
    private JTextField name = new JTextField();
    private JTextField cat = new JTextField();
    private JTextField unit = new JTextField("KG");
    private JTextField reorder = new JTextField("20");
    private JTextField barcode = new JTextField();
    private JTextField brand = new JTextField("Generic");
    private JTextField buyPrice = new JTextField("0.00");
    private JTextField sellPrice = new JTextField("0.00");
    private JTextField supplier = new JTextField();
    private JTextField location = new JTextField("Rack A-1");

    private JTable table = new JTable();
    private ProductDAO productDAO = new ProductDAO();
    private AuditDAO auditDAO = new AuditDAO();

    public ProductPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UIHelper.LIGHT);

        JPanel header = UIHelper.sectionHeader("Enterprise Product Catalog", "Comprehensive item catalog, pricing, reorder thresholds, and cross-warehouse availability");
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(0, 12, 10, 12));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        searchBar.setBackground(UIHelper.WHITE);
        searchBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIHelper.BORDER),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));

        JLabel searchLbl = new JLabel("Search Catalog:");
        searchLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchBar.add(searchLbl);
        searchBar.add(searchField);

        JButton searchBtn = UIHelper.button("Search", UIHelper.NAVY);
        JButton clearSearchBtn = UIHelper.button("Clear", UIHelper.BLUE);
        searchBar.add(searchBtn);
        searchBar.add(clearSearchBtn);

        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setOpaque(false);
        formContainer.add(searchBar, BorderLayout.NORTH);

        if (SessionContext.canModifyProducts()) {
            JPanel form = new JPanel(new GridLayout(2, 6, 6, 6));
            form.setBackground(UIHelper.WHITE);
            form.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UIHelper.BORDER),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));

            form.add(new JLabel("Product Name:"));
            form.add(new JLabel("Category:"));
            form.add(new JLabel("Brand:"));
            form.add(new JLabel("Unit:"));
            form.add(new JLabel("Purchase Price (INR):"));
            form.add(new JLabel("Selling Price (INR):"));

            form.add(name);
            form.add(cat);
            form.add(brand);
            form.add(unit);
            form.add(buyPrice);
            form.add(sellPrice);

            JPanel form2 = new JPanel(new GridLayout(2, 6, 6, 6));
            form2.setBackground(UIHelper.WHITE);
            form2.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UIHelper.BORDER),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));

            form2.add(new JLabel("Reorder Level:"));
            form2.add(new JLabel("SKU / Barcode:"));
            form2.add(new JLabel("Supplier:"));
            form2.add(new JLabel("Shelf Location:"));
            JButton addBtn = UIHelper.button("Add Product", UIHelper.BLUE);
            form2.add(addBtn);
            JButton resetFormBtn = UIHelper.button("Reset Form", UIHelper.NAVY);
            form2.add(resetFormBtn);

            form2.add(reorder);
            form2.add(barcode);
            form2.add(supplier);
            form2.add(location);
            form2.add(new JLabel());
            form2.add(new JLabel());

            JPanel formsCombined = new JPanel(new GridLayout(2, 1, 4, 4));
            formsCombined.setOpaque(false);
            formsCombined.add(form);
            formsCombined.add(form2);

            formContainer.add(formsCombined, BorderLayout.CENTER);

            addBtn.addActionListener(e -> addProduct());
            resetFormBtn.addActionListener(e -> clearForm());
        }

        center.add(formContainer, BorderLayout.NORTH);

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

        JButton viewDetailsBtn = UIHelper.button("View Full Details", UIHelper.BLUE);
        bottom.add(viewDetailsBtn);

        if (SessionContext.canModifyProducts()) {
            JButton editBtn = UIHelper.button("Edit Selected Product", UIHelper.ORANGE);
            bottom.add(editBtn);
            editBtn.addActionListener(e -> editSelectedProduct());
        }

        JButton refreshBtn = UIHelper.button("Refresh List", UIHelper.NAVY);
        bottom.add(refreshBtn);

        center.add(bottom, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);

        searchBtn.addActionListener(e -> doSearch());
        searchField.addActionListener(e -> doSearch());
        clearSearchBtn.addActionListener(e -> { searchField.setText(""); load(); });
        refreshBtn.addActionListener(e -> load());
        viewDetailsBtn.addActionListener(e -> viewSelectedProduct());

        load();
    }

    private void clearForm() {
        name.setText("");
        cat.setText("");
        brand.setText("Generic");
        unit.setText("KG");
        reorder.setText("20");
        barcode.setText("");
        buyPrice.setText("0.00");
        sellPrice.setText("0.00");
        supplier.setText("");
        location.setText("Rack A-1");
    }

    private void doSearch() {
        String q = searchField.getText().trim();
        List<Product> list = productDAO.search(q);
        populateTable(list);
    }

    public void load() {
        List<Product> list = productDAO.findAll();
        populateTable(list);
    }

    private void populateTable(List<Product> list) {
        DefaultTableModel m = new DefaultTableModel(
                new String[]{"ID", "Product Name", "Category", "Brand", "Unit", "Total Stock", "Reorder Level", "Buy Price", "Sell Price", "SKU / Barcode", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Product p : list) {
            m.addRow(new Object[]{
                    p.getId(),
                    p.getName(),
                    p.getCategory(),
                    p.getBrand(),
                    p.getUnit(),
                    p.getTotalStock() + " " + p.getUnit(),
                    p.getReorderLevel() + " " + p.getUnit(),
                    String.format("INR %.2f", p.getPurchasePrice()),
                    String.format("INR %.2f", p.getSellingPrice()),
                    p.getSku(),
                    p.getStatus()
            });
        }
        table.setModel(m);
    }

    private void addProduct() {
        String n = name.getText().trim();
        String c = cat.getText().trim();
        String u = unit.getText().trim();
        String rStr = reorder.getText().trim();
        String bc = barcode.getText().trim();
        String br = brand.getText().trim();
        String bpStr = buyPrice.getText().trim();
        String spStr = sellPrice.getText().trim();
        String sup = supplier.getText().trim();
        String loc = location.getText().trim();

        if (n.isBlank() || c.isBlank()) {
            UIHelper.error(this, "Product name and category are required.");
            return;
        }

        try {
            int r = Integer.parseInt(rStr);
            double bp = Double.parseDouble(bpStr);
            double sp = Double.parseDouble(spStr);

            Product p = new Product(0, n, c, br, "", bc.isBlank() ? "CM-SKU-" + (System.currentTimeMillis() % 100000) : bc, bc, u.isBlank() ? "KG" : u, bp, sp, r, sup, loc, "", "", "Active");
            boolean ok = productDAO.create(p);
            if (ok) {
                auditDAO.log(SessionContext.getUsername(), "PRODUCT ADDED", "Added product " + n + " (ID: " + p.getId() + ")");
                UIHelper.info(this, "Product created successfully!");
                clearForm();
                load();
            }
        } catch (NumberFormatException e) {
            UIHelper.error(this, "Please enter valid numeric values for prices and reorder level.");
        } catch (Exception e) {
            UIHelper.error(this, "Error creating product: " + e.getMessage());
        }
    }

    private void viewSelectedProduct() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIHelper.error(this, "Please select a product from the table.");
            return;
        }
        int id = (int) table.getValueAt(row, 0);
        Product p = productDAO.findById(id);
        if (p != null) {
            new ProductDetailDialog((Frame) SwingUtilities.getWindowAncestor(this), p, productDAO).setVisible(true);
        }
    }

    private void editSelectedProduct() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIHelper.error(this, "Please select a product to edit.");
            return;
        }
        int id = (int) table.getValueAt(row, 0);
        Product p = productDAO.findById(id);
        if (p == null) return;

        JTextField editName = new JTextField(p.getName());
        JTextField editCat = new JTextField(p.getCategory());
        JTextField editBrand = new JTextField(p.getBrand());
        JTextField editUnit = new JTextField(p.getUnit());
        JTextField editBuyPrice = new JTextField(String.valueOf(p.getPurchasePrice()));
        JTextField editSellPrice = new JTextField(String.valueOf(p.getSellingPrice()));
        JTextField editReorder = new JTextField(String.valueOf(p.getReorderLevel()));
        JTextField editSku = new JTextField(p.getSku());
        JTextField editSupplier = new JTextField(p.getSupplierName());
        JTextField editLoc = new JTextField(p.getLocation());
        JComboBox<String> editStatus = new JComboBox<>(new String[]{"Active", "Inactive"});
        editStatus.setSelectedItem(p.getStatus());

        JPanel editPanel = new JPanel(new GridLayout(11, 2, 6, 6));
        editPanel.add(new JLabel("Product Name:")); editPanel.add(editName);
        editPanel.add(new JLabel("Category:")); editPanel.add(editCat);
        editPanel.add(new JLabel("Brand:")); editPanel.add(editBrand);
        editPanel.add(new JLabel("Unit:")); editPanel.add(editUnit);
        editPanel.add(new JLabel("Purchase Price (INR):")); editPanel.add(editBuyPrice);
        editPanel.add(new JLabel("Selling Price (INR):")); editPanel.add(editSellPrice);
        editPanel.add(new JLabel("Reorder Level:")); editPanel.add(editReorder);
        editPanel.add(new JLabel("SKU / Barcode:")); editPanel.add(editSku);
        editPanel.add(new JLabel("Supplier:")); editPanel.add(editSupplier);
        editPanel.add(new JLabel("Location:")); editPanel.add(editLoc);
        editPanel.add(new JLabel("Status:")); editPanel.add(editStatus);

        int result = JOptionPane.showConfirmDialog(this, editPanel, "Edit Product #" + p.getId(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                p.setName(editName.getText().trim());
                p.setCategory(editCat.getText().trim());
                p.setBrand(editBrand.getText().trim());
                p.setUnit(editUnit.getText().trim());
                p.setPurchasePrice(Double.parseDouble(editBuyPrice.getText().trim()));
                p.setSellingPrice(Double.parseDouble(editSellPrice.getText().trim()));
                p.setReorderLevel(Integer.parseInt(editReorder.getText().trim()));
                p.setSku(editSku.getText().trim());
                p.setBarcode(editSku.getText().trim());
                p.setSupplierName(editSupplier.getText().trim());
                p.setLocation(editLoc.getText().trim());
                p.setStatus((String) editStatus.getSelectedItem());

                boolean ok = productDAO.update(p);
                if (ok) {
                    auditDAO.log(SessionContext.getUsername(), "PRODUCT UPDATED", "Updated product #" + p.getId() + " (" + p.getName() + ")");
                    UIHelper.info(this, "Product updated successfully!");
                    load();
                }
            } catch (Exception ex) {
                UIHelper.error(this, "Error updating product: " + ex.getMessage());
            }
        }
    }
}

