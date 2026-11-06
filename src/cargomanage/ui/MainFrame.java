package cargomanage.ui;

import cargomanage.config.DatabaseConfig;
import cargomanage.dao.AuditDAO;
import cargomanage.dao.BranchDAO;
import cargomanage.model.Branch;
import cargomanage.model.User;
import cargomanage.security.SessionContext;
import cargomanage.util.UIHelper;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private JTabbedPane tabs = new JTabbedPane();
    private AuditDAO auditDAO = new AuditDAO();
    private BranchDAO branchDAO = new BranchDAO();

    public MainFrame() {
        setTitle("CargoManage – Enterprise Management System");
        setSize(1280, 780);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(UIHelper.LIGHT);

        UIManager.put("TabbedPane.selected", UIHelper.WHITE);
        UIManager.put("TabbedPane.background", new Color(226, 232, 240));
        UIManager.put("TabbedPane.foreground", UIHelper.TEXT);
        UIManager.put("TabbedPane.font", new Font("Segoe UI", Font.BOLD, 12));
        UIManager.put("Button.rollover", Boolean.FALSE);
        UIManager.put("Button.defaultButtonFollowsFocus", Boolean.FALSE);
        UIManager.put("Table.isRollover", Boolean.FALSE);
        UIManager.put("TableHeader.rollover", Boolean.FALSE);
        UIManager.put("ComboBox.isRollover", Boolean.FALSE);
        UIManager.put("ScrollBar.isRollover", Boolean.FALSE);
        UIManager.put("TabbedPane.tabsOpaque", Boolean.TRUE);

        User user = SessionContext.getCurrentUser();
        String branchName = "HQ / All Branches";
        if (user.getBranchId() != null && user.getBranchId() > 0) {
            Branch b = branchDAO.findById(user.getBranchId());
            if (b != null) branchName = b.getName();
        }

        // ================= TOP HEADER =================
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UIHelper.NAVY);
        top.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        // Brand on Left
        JLabel brand = new JLabel("CARGOMANAGE");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 25));
        brand.setForeground(Color.WHITE);

        JLabel sub = new JLabel("  Manage. Track. Control.  |  Enterprise Warehouse, Inventory & Business Management");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(191, 219, 254));

        JPanel brandBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        brandBox.setOpaque(false);
        brandBox.add(brand);
        brandBox.add(sub);
        top.add(brandBox, BorderLayout.WEST);

        // User Info & Session Controls on Right
        JPanel userBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userBox.setOpaque(false);

        JLabel userLbl = new JLabel(user.getFullName() + " [" + user.getRole() + "]");
        userLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userLbl.setForeground(new Color(241, 245, 249));

        JButton chgPassBtn = UIHelper.button("Change Password", new Color(51, 65, 85));
        chgPassBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        chgPassBtn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        JButton logoutBtn = UIHelper.button("LOGOUT", UIHelper.RED);
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));

        userBox.add(userLbl);
        userBox.add(chgPassBtn);
        userBox.add(logoutBtn);
        top.add(userBox, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        // ================= NAVIGATION TABS =================
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabs.setFocusable(false);
        tabs.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected int getRolloverTab() {
                return -1; // Never report any tab as hovered
            }

            @Override
            protected void setRolloverTab(int index) {
                // No-op: prevents hover state changes
            }

            @Override
            protected void installDefaults() {
                super.installDefaults();
                tabInsets = new Insets(8, 16, 8, 16);
                selectedTabPadInsets = new Insets(0, 0, 0, 0);
            }

            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected) {
                    g2.setColor(Color.WHITE);
                    g2.fillRect(x, y, w, h);
                    g2.setColor(UIHelper.BLUE);
                    g2.fillRect(x, y, w, 3);
                } else {
                    g2.setColor(new Color(241, 245, 249));
                    g2.fillRect(x, y, w, h);
                }
                g2.setColor(UIHelper.BORDER);
                g2.drawRect(x, y, w - 1, h - 1);
                g2.dispose();
            }

            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                // Border handled cleanly in paintTabBackground, avoiding Windows native hot border
            }

            @Override
            protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
                // No dashed focus outline
            }

            @Override
            protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setFont(font);
                if (isSelected) {
                    g2.setColor(UIHelper.NAVY);
                } else {
                    g2.setColor(UIHelper.TEXT_MUTED);
                }
                g2.drawString(title, textRect.x, textRect.y + metrics.getAscent());
                g2.dispose();
            }
        });

        tabs.addTab("Dashboard", new DashboardPanel());

        if (SessionContext.canManageBranches()) {
            tabs.addTab("Branches", new BranchPanel());
        }

        if (SessionContext.canManageUsers()) {
            tabs.addTab("User Accounts", new UserManagementPanel());
        }

        tabs.addTab("Products", new ProductPanel());

        if (!SessionContext.isAccountant()) {
            tabs.addTab("Stock", new StockPanel());
        }

        tabs.addTab("Transfers", new TransferPanel());
        tabs.addTab("Purchases", new PurchasePanel());
        tabs.addTab("Shipments", new ShipmentPanel());

        if (SessionContext.canViewPayments()) {
            tabs.addTab("Payments", new PaymentPanel());
        }

        if (!SessionContext.isBranchStaff()) {
            tabs.addTab("Reports", new ReportPanel());
        }

        if (SessionContext.isAdmin() || SessionContext.isManager()) {
            tabs.addTab("Audit Log", new AuditPanel());
        }

        add(tabs, BorderLayout.CENTER);

        // ================= BOTTOM STATUS BAR =================
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(UIHelper.WHITE);
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER),
                BorderFactory.createEmptyBorder(7, 14, 7, 14)
        ));

        JLabel userStatus = new JLabel("● Logged in as: " + user.getUsername() +
                "   |   Role: " + user.getRole() +
                "   |   Assigned Branch: " + branchName);
        userStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userStatus.setForeground(new Color(51, 65, 85));

        JLabel dbStatus = new JLabel("MySQL Database: " + DatabaseConfig.getDbName() + "@" + DatabaseConfig.getHost() + " (Connected)  ● ");
        dbStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dbStatus.setForeground(UIHelper.GREEN);

        statusPanel.add(userStatus, BorderLayout.WEST);
        statusPanel.add(dbStatus, BorderLayout.EAST);
        add(statusPanel, BorderLayout.SOUTH);

        tabs.addChangeListener(e -> {
            Component selected = tabs.getSelectedComponent();
            if (selected instanceof DashboardPanel) ((DashboardPanel) selected).load();
            else if (selected instanceof ProductPanel) ((ProductPanel) selected).load();
            else if (selected instanceof StockPanel) { ((StockPanel) selected).refreshCombos(); ((StockPanel) selected).load(); }
            else if (selected instanceof TransferPanel) { ((TransferPanel) selected).refreshCombos(); ((TransferPanel) selected).load(); }
            else if (selected instanceof PurchasePanel) { ((PurchasePanel) selected).refreshCombos(); ((PurchasePanel) selected).load(); }
            else if (selected instanceof ShipmentPanel) ((ShipmentPanel) selected).load();
            else if (selected instanceof PaymentPanel) ((PaymentPanel) selected).load();
            else if (selected instanceof UserManagementPanel) { ((UserManagementPanel) selected).refreshBranches(); ((UserManagementPanel) selected).load(); }
            else if (selected instanceof BranchPanel) ((BranchPanel) selected).load();
            else if (selected instanceof AuditPanel) ((AuditPanel) selected).load();
        });

        chgPassBtn.addActionListener(e -> new ChangePasswordDialog(this).setVisible(true));
        logoutBtn.addActionListener(e -> doLogout());
    }

    private void doLogout() {
        if (UIHelper.confirm(this, "Are you sure you want to log out of CargoManage?", "Logout Confirmation")) {
            auditDAO.log(SessionContext.getUsername(), "LOGOUT", "User logged out");
            SessionContext.clear();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}

