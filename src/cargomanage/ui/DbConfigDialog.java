package cargomanage.ui;

import cargomanage.config.DatabaseConfig;
import cargomanage.dao.DatabaseManager;
import cargomanage.util.UIHelper;

import javax.swing.*;
import java.awt.*;

public class DbConfigDialog extends JDialog {
    private JTextField hostField = new JTextField(DatabaseConfig.getHost());
    private JTextField portField = new JTextField(DatabaseConfig.getPort());
    private JTextField dbField = new JTextField(DatabaseConfig.getDbName());
    private JTextField userField = new JTextField(DatabaseConfig.getUser());
    private JPasswordField passField = new JPasswordField(DatabaseConfig.getPassword());
    private boolean connectedSuccessfully = false;

    public DbConfigDialog(Frame parent) {
        super(parent, "CargoManage - MySQL Database Configuration", true);
        setSize(420, 360);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(UIHelper.LIGHT);

        JPanel header = UIHelper.sectionHeader("Database Settings", "Configure CargoManage MySQL connection parameters");
        add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        form.add(new JLabel("MySQL Host:"));
        form.add(hostField);
        form.add(new JLabel("Port:"));
        form.add(portField);
        form.add(new JLabel("Database Name:"));
        form.add(dbField);
        form.add(new JLabel("Username:"));
        form.add(userField);
        form.add(new JLabel("Password:"));
        form.add(passField);

        add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(UIHelper.WHITE);
        bottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER));

        JButton testBtn = UIHelper.button("Test Connection", UIHelper.NAVY);
        JButton saveBtn = UIHelper.button("Save & Connect", UIHelper.BLUE);
        JButton cancelBtn = UIHelper.secondaryButton("Cancel");

        testBtn.addActionListener(e -> testConn());
        saveBtn.addActionListener(e -> saveAndClose());
        cancelBtn.addActionListener(e -> dispose());

        bottom.add(testBtn);
        bottom.add(saveBtn);
        bottom.add(cancelBtn);
        add(bottom, BorderLayout.SOUTH);
    }

    private void applyFieldsToConfig() {
        DatabaseConfig.setHost(hostField.getText().trim());
        DatabaseConfig.setPort(portField.getText().trim());
        DatabaseConfig.setDbName(dbField.getText().trim());
        DatabaseConfig.setUser(userField.getText().trim());
        DatabaseConfig.setPassword(new String(passField.getPassword()));
    }

    private void testConn() {
        applyFieldsToConfig();
        try {
            DatabaseManager.initializeDatabase();
            UIHelper.info(this, "Success! Connected to MySQL database successfully.");
        } catch (Exception e) {
            UIHelper.error(this, "Connection failed:\n" + e.getMessage());
        }
    }

    private void saveAndClose() {
        applyFieldsToConfig();
        DatabaseConfig.save();
        try {
            DatabaseManager.initializeDatabase();
            connectedSuccessfully = true;
            UIHelper.info(this, "Database configured and connected successfully!");
            dispose();
        } catch (Exception e) {
            UIHelper.error(this, "Could not initialize database:\n" + e.getMessage());
        }
    }

    public boolean isConnectedSuccessfully() {
        return connectedSuccessfully;
    }
}

