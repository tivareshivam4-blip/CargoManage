package cargomanage.ui;

import cargomanage.dao.AuditDAO;
import cargomanage.dao.DatabaseManager;
import cargomanage.dao.UserDAO;
import cargomanage.model.User;
import cargomanage.security.SessionContext;
import cargomanage.util.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LoginFrame extends JFrame {
    private JTextField userField = new JTextField();
    private JPasswordField passField = new JPasswordField();
    private UserDAO userDAO = new UserDAO();
    private AuditDAO auditDAO = new AuditDAO();

    public LoginFrame() {
        setTitle("CargoManage - Enterprise Login");
        setSize(600, 720);
        setMinimumSize(new Dimension(560, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(true);

        // Root responsive container
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(UIHelper.LIGHT);

        // Centered Login Card
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(540, 650));
        card.setMaximumSize(new Dimension(540, 650));
        card.setBackground(UIHelper.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIHelper.BORDER, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        // Header Banner
        JPanel banner = new JPanel(new GridLayout(3, 1, 3, 3));
        banner.setBackground(UIHelper.NAVY);
        banner.setBorder(BorderFactory.createEmptyBorder(22, 20, 22, 20));

        JLabel title = new JLabel("CARGOMANAGE", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Enterprise Warehouse, Inventory & Business Management System", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(191, 219, 254));

        JLabel tagline = new JLabel("Manage. Track. Control.", SwingConstants.CENTER);
        tagline.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tagline.setForeground(new Color(147, 197, 253));

        banner.add(title);
        banner.add(sub);
        banner.add(tagline);
        card.add(banner, BorderLayout.NORTH);

        // Form Section (Dimensions: 460px width, 50px height)
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(24, 40, 16, 40));

        JLabel userLbl = new JLabel("Username");
        userLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userLbl.setForeground(UIHelper.TEXT);
        userLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        userField.setPreferredSize(new Dimension(460, 50));
        userField.setMinimumSize(new Dimension(460, 50));
        userField.setMaximumSize(new Dimension(460, 50));
        userField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        userField.setForeground(UIHelper.TEXT);
        userField.setBackground(Color.WHITE);
        userField.setCaretColor(UIHelper.TEXT);
        userField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));
        userField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel passLbl = new JLabel("Password");
        passLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        passLbl.setForeground(UIHelper.TEXT);
        passLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        passField.setPreferredSize(new Dimension(460, 50));
        passField.setMinimumSize(new Dimension(460, 50));
        passField.setMaximumSize(new Dimension(460, 50));
        passField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        passField.setForeground(UIHelper.TEXT);
        passField.setBackground(Color.WHITE);
        passField.setCaretColor(UIHelper.TEXT);
        passField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));
        passField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton loginBtn = UIHelper.button("SECURE LOGIN", UIHelper.BLUE);
        loginBtn.setPreferredSize(new Dimension(460, 50));
        loginBtn.setMinimumSize(new Dimension(460, 50));
        loginBtn.setMaximumSize(new Dimension(460, 50));
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton dbBtn = new JButton("⚙ Database Connection Settings") {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isPressed()) {
                    setForeground(UIHelper.TEXT);
                } else {
                    setForeground(UIHelper.TEXT_MUTED);
                }
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                // Stable: no hover or focus border
            }
        };
        dbBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dbBtn.setForeground(UIHelper.TEXT_MUTED);
        dbBtn.setContentAreaFilled(false);
        dbBtn.setBorderPainted(false);
        dbBtn.setFocusPainted(false);
        dbBtn.setRolloverEnabled(false);
        dbBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel dbBox = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        dbBox.setOpaque(false);
        dbBox.setMaximumSize(new Dimension(460, 26));
        dbBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        dbBox.add(dbBtn);

        form.add(userLbl);
        form.add(Box.createVerticalStrut(6));
        form.add(userField);
        form.add(Box.createVerticalStrut(14));
        form.add(passLbl);
        form.add(Box.createVerticalStrut(6));
        form.add(passField);
        form.add(Box.createVerticalStrut(20));
        form.add(loginBtn);
        form.add(Box.createVerticalStrut(12));
        form.add(dbBox);

        card.add(form, BorderLayout.CENTER);

        // Demo Credentials Hint Box
        JPanel hints = new JPanel(new GridLayout(5, 1, 3, 3));
        hints.setBackground(new Color(248, 250, 252));
        hints.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER),
                BorderFactory.createEmptyBorder(12, 20, 14, 20)
        ));

        JLabel h0 = new JLabel("DEFAULT DEMO ACCOUNTS (ROLE-BASED PERMISSIONS):", SwingConstants.CENTER);
        h0.setFont(new Font("Segoe UI", Font.BOLD, 11));
        h0.setForeground(UIHelper.NAVY);

        JLabel h1 = new JLabel("Admin: admin  /  admin123  (Full Access)", SwingConstants.CENTER);
        h1.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        h1.setForeground(UIHelper.TEXT);

        JLabel h2 = new JLabel("Manager: manager  /  manager123  (Operations & Stock)", SwingConstants.CENTER);
        h2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        h2.setForeground(UIHelper.TEXT);

        JLabel h3 = new JLabel("Accountant: accountant  /  account123  (Financials & Payments)", SwingConstants.CENTER);
        h3.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        h3.setForeground(UIHelper.TEXT);

        JLabel h4 = new JLabel("Branch Staff: murbad  /  staff123  (Murbad Warehouse)", SwingConstants.CENTER);
        h4.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        h4.setForeground(UIHelper.TEXT);

        hints.add(h0); hints.add(h1); hints.add(h2); hints.add(h3); hints.add(h4);
        card.add(hints, BorderLayout.SOUTH);

        root.add(card);
        add(root);

        loginBtn.addActionListener(e -> doLogin());
        passField.addActionListener(e -> doLogin());
        dbBtn.addActionListener(e -> openDbConfig());

        userField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) passField.requestFocus();
            }
        });

        SwingUtilities.invokeLater(this::checkDatabaseConnection);
    }

    private void checkDatabaseConnection() {
        try {
            DatabaseManager.initializeDatabase();
        } catch (Exception e) {
            int choice = JOptionPane.showOptionDialog(
                    this,
                    "Could not connect to MySQL database at localhost:3306.\n\n" +
                    "Error: " + e.getMessage() + "\n\n" +
                    "Please make sure MySQL is started (e.g. from XAMPP Control Panel or MySQL service).\n" +
                    "Would you like to configure connection settings now?",
                    "CargoManage - MySQL Notice",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    new Object[]{"Configure Database", "Continue Anyway"},
                    "Configure Database"
            );
            if (choice == JOptionPane.YES_OPTION) {
                openDbConfig();
            }
        }
    }

    private void openDbConfig() {
        DbConfigDialog dlg = new DbConfigDialog(this);
        dlg.setVisible(true);
    }

    private void doLogin() {
        String u = userField.getText().trim();
        String pw = new String(passField.getPassword());

        if (u.isBlank() || pw.isBlank()) {
            UIHelper.error(this, "Please enter your username and password.");
            return;
        }

        try {
            User user = userDAO.authenticate(u, pw);
            if (user != null) {
                if (!user.isActive()) {
                    UIHelper.error(this, "This account has been deactivated. Please contact the administrator.");
                    return;
                }
                SessionContext.setCurrentUser(user);
                auditDAO.log(user.getUsername(), "LOGIN", "User logged in successfully (Role: " + user.getRole() + ")");
                dispose();
                new MainFrame().setVisible(true);
            } else {
                UIHelper.error(this, "Invalid username or password.\n\nPlease check your credentials and try again.");
            }
        } catch (Exception ex) {
            UIHelper.error(this, "Database Error during authentication:\n" + ex.getMessage() +
                    "\n\nPlease ensure MySQL is running and configured.");
        }
    }
}

