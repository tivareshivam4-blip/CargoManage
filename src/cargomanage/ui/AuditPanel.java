package cargomanage.ui;

import cargomanage.dao.AuditDAO;
import cargomanage.model.AuditLog;
import cargomanage.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AuditPanel extends JPanel {
    private JTable table = new JTable();
    private JTextField searchField = new JTextField(15);
    private AuditDAO auditDAO = new AuditDAO();

    public AuditPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UIHelper.LIGHT);

        JPanel header = UIHelper.sectionHeader("System Security & Audit Trail",
                "Immutable trace of authentication events, product changes, stock updates, financial settlements, and approvals");
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(0, 12, 10, 12));

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        filterBar.setBackground(UIHelper.WHITE);
        filterBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIHelper.BORDER),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));

        filterBar.add(new JLabel("Search Audit Trail:"));
        filterBar.add(searchField);

        JButton searchBtn = UIHelper.button("Search", UIHelper.NAVY);
        JButton refreshBtn = UIHelper.button("Refresh", UIHelper.BLUE);
        filterBar.add(searchBtn);
        filterBar.add(refreshBtn);

        center.add(filterBar, BorderLayout.NORTH);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER));
        center.add(sp, BorderLayout.CENTER);
        UIHelper.styleTable(table);

        add(center, BorderLayout.CENTER);

        searchBtn.addActionListener(e -> doSearch());
        searchField.addActionListener(e -> doSearch());
        refreshBtn.addActionListener(e -> { searchField.setText(""); load(); });

        load();
    }

    private void doSearch() {
        String q = searchField.getText().trim();
        List<AuditLog> list = auditDAO.search(q, 300);
        populateTable(list);
    }

    public void load() {
        List<AuditLog> list = auditDAO.findAll(300);
        populateTable(list);
    }

    private void populateTable(List<AuditLog> list) {
        DefaultTableModel m = new DefaultTableModel(
                new String[]{"ID", "Username", "Action Performed", "Details / Event Summary", "Timestamp"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for (AuditLog a : list) {
            m.addRow(new Object[]{
                    a.getId(),
                    a.getUsername(),
                    a.getAction(),
                    a.getDetails(),
                    a.getTimestamp()
            });
        }
        table.setModel(m);
    }
}

