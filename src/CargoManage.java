import cargomanage.dao.DatabaseManager;
import cargomanage.ui.LoginFrame;

import javax.swing.*;

/**
 * CargoManage – Enterprise Warehouse, Inventory & Business Management System
 * Tagline: Manage. Track. Control.
 * Main Application Entry Point
 */
public class CargoManage {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Complete UI Stability: Disable all hover effects across all Swing components
        UIManager.put("Button.rollover", Boolean.FALSE);
        UIManager.put("Button.defaultButtonFollowsFocus", Boolean.FALSE);
        UIManager.put("Table.isRollover", Boolean.FALSE);
        UIManager.put("TableHeader.rollover", Boolean.FALSE);
        UIManager.put("ComboBox.isRollover", Boolean.FALSE);
        UIManager.put("ScrollBar.isRollover", Boolean.FALSE);
        UIManager.put("TabbedPane.tabsOpaque", Boolean.TRUE);

        // Global neutral borders preventing Windows native light-blue hover glow
        javax.swing.border.Border neutralFieldBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(203, 213, 225), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        );
        UIManager.put("TextField.border", neutralFieldBorder);
        UIManager.put("PasswordField.border", neutralFieldBorder);
        UIManager.put("FormattedTextField.border", neutralFieldBorder);

        SwingUtilities.invokeLater(() -> {
            try {
                DatabaseManager.initializeDatabase();
            } catch (Exception e) {
                System.err.println("Database Notice: " + e.getMessage());
            }

            new LoginFrame().setVisible(true);
        });
    }
}

