package cargomanage.ui;

import cargomanage.dao.AuditDAO;
import cargomanage.dao.BranchDAO;
import cargomanage.model.Branch;
import cargomanage.security.SessionContext;
import cargomanage.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BranchPanel extends JPanel {
    private JTextField name = new JTextField();
    private JTextField location = new JTextField();
    private JTextField code = new JTextField();
    private JTextField phone = new JTextField();
    private JTable table = new JTable();

    private BranchDAO branchDAO = new BranchDAO();
    private AuditDAO auditDAO = new AuditDAO();

    public BranchPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UIHelper.LIGHT);

        JPanel header = UIHelper.sectionHeader("Warehouse & Branch Network", "Manage physical warehouses, depots, and regional fulfillment hubs");
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(0, 12, 10, 12));

        JPanel form = new JPanel(new GridLayout(2, 5, 8, 8));
        form.setBackground(UIHelper.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIHelper.BORDER),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        form.add(new JLabel("Branch Name:"));
        form.add(new JLabel("Location / Address:"));
        form.add(new JLabel("Branch Code:"));
        form.add(new JLabel("Phone / Contact:"));
        JButton addBtn = UIHelper.button("Add Branch", UIHelper.BLUE);
        form.add(addBtn);

        form.add(name);
        form.add(location);
        form.add(code);
        form.add(phone);
        JButton refreshBtn = UIHelper.button("Refresh", UIHelper.NAVY);
        form.add(refreshBtn);

        center.add(form, BorderLayout.NORTH);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER));
        center.add(sp, BorderLayout.CENTER);
        UIHelper.styleTable(table);

        add(center, BorderLayout.CENTER);

        addBtn.addActionListener(e -> addBranch());
        refreshBtn.addActionListener(e -> load());

        load();
    }

    public void load() {
        DefaultTableModel m = new DefaultTableModel(new String[]{"ID", "Branch Name", "Location / Address", "Branch Code", "Phone"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        List<Branch> list = branchDAO.findAll();
        for (Branch b : list) {
            m.addRow(new Object[]{b.getId(), b.getName(), b.getLocation(), b.getCode(), b.getPhone()});
        }
        table.setModel(m);
    }

    private void addBranch() {
        String n = name.getText().trim();
        String l = location.getText().trim();
        String c = code.getText().trim();
        String p = phone.getText().trim();

        if (n.isBlank() || l.isBlank()) {
            UIHelper.error(this, "Branch name and location are required.");
            return;
        }

        Branch b = new Branch(0, n, l, c.isBlank() ? "BR-" + (System.currentTimeMillis() % 10000) : c, p);
        try {
            boolean ok = branchDAO.create(b);
            if (ok) {
                auditDAO.log(SessionContext.getUsername(), "BRANCH ADDED", "Added branch " + n + " (" + l + ")");
                UIHelper.info(this, "Branch added successfully!");
                name.setText("");
                location.setText("");
                code.setText("");
                phone.setText("");
                load();
            }
        } catch (Exception e) {
            UIHelper.error(this, "Error adding branch: " + e.getMessage());
        }
    }
}

