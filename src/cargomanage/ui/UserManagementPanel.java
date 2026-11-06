package cargomanage.ui;

import cargomanage.dao.AuditDAO;
import cargomanage.dao.BranchDAO;
import cargomanage.dao.UserDAO;
import cargomanage.model.Branch;
import cargomanage.model.User;
import cargomanage.security.SessionContext;
import cargomanage.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserManagementPanel extends JPanel {
    private JTable table = new JTable();
    private UserDAO userDAO = new UserDAO();
    private BranchDAO branchDAO = new BranchDAO();
    private AuditDAO auditDAO = new AuditDAO();

    private JTextField usernameField = new JTextField();
    private JTextField fullNameField = new JTextField();
    private JPasswordField passField = new JPasswordField();
    private JComboBox<String> roleCombo = new JComboBox<>(new String[]{"ADMIN", "MANAGER", "ACCOUNTANT", "BRANCH_STAFF"});
    private JComboBox<String> branchCombo = new JComboBox<>();

    public UserManagementPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UIHelper.LIGHT);

        JPanel header = UIHelper.sectionHeader("User & Role Management", "Manage user credentials, system roles, and assigned branches");
        add(header, BorderLayout.NORTH);

        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setOpaque(false);
        formWrapper.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

        JPanel form = new JPanel(new GridLayout(2, 6, 8, 8));
        form.setBackground(UIHelper.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIHelper.BORDER),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        form.add(new JLabel("Username:"));
        form.add(new JLabel("Full Name:"));
        form.add(new JLabel("Password:"));
        form.add(new JLabel("Role:"));
        form.add(new JLabel("Assigned Branch:"));
        JButton addBtn = UIHelper.button("Create User", UIHelper.BLUE);
        form.add(addBtn);

        form.add(usernameField);
        form.add(fullNameField);
        form.add(passField);
        form.add(roleCombo);
        form.add(branchCombo);

        JButton refreshBtn = UIHelper.button("Refresh List", UIHelper.NAVY);
        form.add(refreshBtn);

        formWrapper.add(form, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(0, 12, 10, 12));
        center.add(formWrapper, BorderLayout.NORTH);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER));
        center.add(sp, BorderLayout.CENTER);
        UIHelper.styleTable(table);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        actions.setBackground(UIHelper.WHITE);
        actions.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIHelper.BORDER),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        JButton resetPassBtn = UIHelper.button("Reset Password", UIHelper.ORANGE);
        JButton toggleStatusBtn = UIHelper.button("Toggle Active/Inactive", UIHelper.NAVY);

        actions.add(resetPassBtn);
        actions.add(toggleStatusBtn);
        center.add(actions, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);

        addBtn.addActionListener(e -> createUser());
        refreshBtn.addActionListener(e -> { refreshBranches(); load(); });
        resetPassBtn.addActionListener(e -> resetUserPassword());
        toggleStatusBtn.addActionListener(e -> toggleUserStatus());

        roleCombo.addActionListener(e -> updateBranchComboState());

        refreshBranches();
        load();
    }

    private void updateBranchComboState() {
        String role = (String) roleCombo.getSelectedItem();
        branchCombo.setEnabled("BRANCH_STAFF".equals(role));
    }

    public void refreshBranches() {
        branchCombo.removeAllItems();
        branchCombo.addItem("0 - None (Headquarters / All)");
        List<Branch> branches = branchDAO.findAll();
        for (Branch b : branches) {
            branchCombo.addItem(b.getId() + " - " + b.getName());
        }
    }

    public void load() {
        DefaultTableModel m = new DefaultTableModel(
                new String[]{"ID", "Username", "Full Name", "Role", "Branch", "Status", "Last Login", "Created At"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        List<User> users = userDAO.findAll();
        for (User u : users) {
            String branchName = "HQ / All";
            if (u.getBranchId() != null && u.getBranchId() > 0) {
                Branch b = branchDAO.findById(u.getBranchId());
                if (b != null) branchName = b.getName();
            }
            m.addRow(new Object[]{
                    u.getId(),
                    u.getUsername(),
                    u.getFullName(),
                    u.getRole(),
                    branchName,
                    u.getStatus(),
                    u.getLastLogin() != null ? u.getLastLogin() : "Never",
                    u.getCreatedAt()
            });
        }
        table.setModel(m);
    }

    private void createUser() {
        String username = usernameField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String pass = new String(passField.getPassword());
        String role = (String) roleCombo.getSelectedItem();
        int bId = UIHelper.selectedId(branchCombo);

        if (username.isBlank() || fullName.isBlank() || pass.isBlank()) {
            UIHelper.error(this, "Please enter username, full name, and password.");
            return;
        }

        if (pass.length() < 6) {
            UIHelper.error(this, "Password must be at least 6 characters.");
            return;
        }

        if (userDAO.findByUsername(username) != null) {
            UIHelper.error(this, "Username '" + username + "' is already taken.");
            return;
        }

        User u = new User();
        u.setUsername(username);
        u.setFullName(fullName);
        u.setRole(role);
        u.setBranchId(bId > 0 ? bId : null);
        u.setStatus("ACTIVE");

        try {
            boolean ok = userDAO.create(u, pass);
            if (ok) {
                auditDAO.log(SessionContext.getUsername(), "USER CREATED", "Created user " + username + " with role " + role);
                UIHelper.info(this, "User created successfully!");
                usernameField.setText("");
                fullNameField.setText("");
                passField.setText("");
                load();
            }
        } catch (Exception e) {
            UIHelper.error(this, "Error creating user: " + e.getMessage());
        }
    }

    private void resetUserPassword() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIHelper.error(this, "Please select a user to reset password.");
            return;
        }
        int id = (int) table.getValueAt(row, 0);
        String username = (String) table.getValueAt(row, 1);

        String newPass = JOptionPane.showInputDialog(this, "Enter new password for " + username + " (min 6 chars):", "Reset Password", JOptionPane.QUESTION_MESSAGE);
        if (newPass != null && !newPass.isBlank()) {
            if (newPass.trim().length() < 6) {
                UIHelper.error(this, "Password must be at least 6 characters.");
                return;
            }
            boolean ok = userDAO.updatePassword(id, newPass.trim());
            if (ok) {
                auditDAO.log(SessionContext.getUsername(), "PASSWORD RESET", "Reset password for user " + username);
                UIHelper.info(this, "Password for " + username + " reset successfully!");
            } else {
                UIHelper.error(this, "Failed to reset password.");
            }
        }
    }

    private void toggleUserStatus() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIHelper.error(this, "Please select a user.");
            return;
        }
        int id = (int) table.getValueAt(row, 0);
        String username = (String) table.getValueAt(row, 1);
        String currentStatus = (String) table.getValueAt(row, 5);

        if (username.equalsIgnoreCase(SessionContext.getUsername())) {
            UIHelper.error(this, "You cannot deactivate your own account!");
            return;
        }

        String newStatus = "ACTIVE".equalsIgnoreCase(currentStatus) ? "INACTIVE" : "ACTIVE";
        boolean ok = userDAO.setStatus(id, newStatus);
        if (ok) {
            auditDAO.log(SessionContext.getUsername(), "USER STATUS CHANGED", "Changed status of " + username + " to " + newStatus);
            UIHelper.info(this, "User " + username + " status changed to " + newStatus);
            load();
        } else {
            UIHelper.error(this, "Failed to update status.");
        }
    }
}

