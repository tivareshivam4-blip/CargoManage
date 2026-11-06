package cargomanage.ui;

import cargomanage.dao.AuditDAO;
import cargomanage.dao.UserDAO;
import cargomanage.model.User;
import cargomanage.security.PasswordUtil;
import cargomanage.security.SessionContext;
import cargomanage.util.UIHelper;

import javax.swing.*;
import java.awt.*;

public class ChangePasswordDialog extends JDialog {
    private JPasswordField currentPassField = new JPasswordField();
    private JPasswordField newPassField = new JPasswordField();
    private JPasswordField confirmPassField = new JPasswordField();
    private UserDAO userDAO = new UserDAO();
    private AuditDAO auditDAO = new AuditDAO();

    public ChangePasswordDialog(Frame parent) {
        super(parent, "CargoManage - Change Password", true);
        setSize(380, 290);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(UIHelper.LIGHT);

        JPanel header = UIHelper.sectionHeader("Change Password", "Update your account password securely");
        add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 10));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        form.add(new JLabel("Current Password:"));
        form.add(currentPassField);
        form.add(new JLabel("New Password:"));
        form.add(newPassField);
        form.add(new JLabel("Confirm New Password:"));
        form.add(confirmPassField);

        add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(UIHelper.WHITE);
        bottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER));

        JButton updateBtn = UIHelper.button("Update Password", UIHelper.BLUE);
        JButton cancelBtn = UIHelper.secondaryButton("Cancel");

        updateBtn.addActionListener(e -> doUpdate());
        cancelBtn.addActionListener(e -> dispose());

        bottom.add(updateBtn);
        bottom.add(cancelBtn);
        add(bottom, BorderLayout.SOUTH);
    }

    private void doUpdate() {
        User user = SessionContext.getCurrentUser();
        if (user == null) {
            UIHelper.error(this, "No active session.");
            dispose();
            return;
        }

        String current = new String(currentPassField.getPassword());
        String newPass = new String(newPassField.getPassword());
        String confirm = new String(confirmPassField.getPassword());

        if (current.isBlank() || newPass.isBlank() || confirm.isBlank()) {
            UIHelper.error(this, "Please fill in all fields.");
            return;
        }

        User freshUser = userDAO.findById(user.getId());
        if (freshUser == null || !PasswordUtil.verify(current, freshUser.getPasswordHash())) {
            UIHelper.error(this, "Current password is incorrect.");
            return;
        }

        if (newPass.length() < 6) {
            UIHelper.error(this, "New password must be at least 6 characters long.");
            return;
        }

        if (!newPass.equals(confirm)) {
            UIHelper.error(this, "New password and confirmation do not match.");
            return;
        }

        boolean ok = userDAO.updatePassword(user.getId(), newPass);
        if (ok) {
            auditDAO.log(user.getUsername(), "PASSWORD CHANGED", "User updated their password successfully");
            UIHelper.info(this, "Password updated successfully!");
            dispose();
        } else {
            UIHelper.error(this, "Failed to update password. Please try again.");
        }
    }
}

