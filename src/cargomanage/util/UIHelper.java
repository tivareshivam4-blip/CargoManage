package cargomanage.util;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class UIHelper {
    public static final Color NAVY = new Color(23, 55, 86);
    public static final Color BLUE = new Color(37, 99, 235);
    public static final Color CYAN = new Color(14, 165, 233);
    public static final Color GREEN = new Color(22, 163, 74);
    public static final Color RED = new Color(220, 38, 38);
    public static final Color ORANGE = new Color(234, 88, 12);
    public static final Color LIGHT = new Color(244, 247, 251);
    public static final Color WHITE = Color.WHITE;
    public static final Color TEXT = new Color(30, 41, 59);
    public static final Color TEXT_MUTED = new Color(100, 116, 139);
    public static final Color BORDER = new Color(226, 232, 240);
    public static final Color ROW_ALT = new Color(248, 250, 252);
    public static final Color HIGHLIGHT = new Color(226, 232, 240); // Neutral solid selection, not light-blue hover

    public static Color darken(Color c, float factor) {
        return new Color(
            Math.max(0, (int) (c.getRed() * (1.0f - factor))),
            Math.max(0, (int) (c.getGreen() * (1.0f - factor))),
            Math.max(0, (int) (c.getBlue() * (1.0f - factor))),
            c.getAlpha()
        );
    }

    public static JButton button(String text) {
        return button(text, BLUE);
    }

    /**
     * Primary action button:
     * - Visually stable on hover (no scaling, bounce, float, glow, or elevation).
     * - Subtle darkening on click / press for instant tactile feedback.
     * - Keeps original colors, font, padding, and shape.
     */
    public static JButton button(String text, Color bg) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Subtle darkening only when pressed; stable on hover
                if (getModel().isPressed()) {
                    g2.setColor(darken(bg, 0.14f));
                } else {
                    g2.setColor(bg);
                }
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                // Stable: no hover, glow, or elevation borders
            }
        };
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setFocusPainted(false);
        b.setRolloverEnabled(false);
        b.setBorderPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setForeground(Color.WHITE);
        b.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    /**
     * Secondary action button (Cancel, Close, Dismiss):
     * - Visually stable on hover.
     * - Subtle darkening on click / press.
     */
    public static JButton secondaryButton(String text) {
        Color normalBg = new Color(241, 245, 249);
        Color pressedBg = new Color(226, 232, 240);
        Color border = new Color(203, 213, 225);
        Color fg = new Color(51, 65, 85);

        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(pressedBg);
                } else {
                    g2.setColor(normalBg);
                }
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(border);
                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                // Stable: border drawn in paintComponent, no LookAndFeel hover effects
            }
        };
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setFocusPainted(false);
        b.setRolloverEnabled(false);
        b.setBorderPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setForeground(fg);
        b.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static void styleTable(JTable t) {
        t.setRowHeight(34);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setForeground(TEXT);
        t.setBackground(Color.WHITE);
        t.setGridColor(BORDER);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setSelectionBackground(HIGHLIGHT);
        t.setSelectionForeground(NAVY);
        t.putClientProperty("Table.isRollover", Boolean.FALSE);

        JTableHeader th = t.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 13));
        th.setForeground(Color.WHITE);
        th.setBackground(NAVY);
        th.setPreferredSize(new Dimension(0, 36));
        th.setReorderingAllowed(false);
        th.putClientProperty("TableHeader.rollover", Boolean.FALSE);

        // Custom static header renderer: eliminates Windows uxtheme HIS_HOT light-blue hover
        th.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = new JLabel(value != null ? value.toString() : "");
                l.setFont(new Font("Segoe UI", Font.BOLD, 13));
                l.setForeground(Color.WHITE);
                l.setBackground(NAVY);
                l.setOpaque(true);
                l.setHorizontalAlignment(SwingConstants.LEFT);
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(30, 68, 105)),
                        BorderFactory.createEmptyBorder(0, 12, 0, 12)
                ));
                return l;
            }
        });

        // Static cell renderer across all column types: prevents light-blue hover highlighting
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    l.setBackground(row % 2 == 0 ? WHITE : ROW_ALT);
                    l.setForeground(TEXT);
                } else {
                    l.setBackground(HIGHLIGHT); // Solid neutral selection on user click only
                    l.setForeground(NAVY);
                }
                l.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return l;
            }
        };

        t.setDefaultRenderer(Object.class, cellRenderer);
        t.setDefaultRenderer(String.class, cellRenderer);
        t.setDefaultRenderer(Integer.class, cellRenderer);
        t.setDefaultRenderer(Double.class, cellRenderer);
        t.setDefaultRenderer(Float.class, cellRenderer);
        t.setDefaultRenderer(Number.class, cellRenderer);
        t.setDefaultRenderer(Boolean.class, cellRenderer);
    }

    public static void styleTextField(JTextField tf) {
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setForeground(TEXT);
        tf.setBackground(WHITE);
        tf.setCaretColor(TEXT);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
    }

    public static void stylePasswordField(JPasswordField pf) {
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pf.setForeground(TEXT);
        pf.setBackground(WHITE);
        pf.setCaretColor(TEXT);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
    }

    public static JPanel sectionHeader(String title, String subtitle) {
        JPanel p = new JPanel(new BorderLayout(8, 3));
        p.setBackground(WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        JLabel a = new JLabel(title);
        a.setFont(new Font("Segoe UI", Font.BOLD, 22));
        a.setForeground(NAVY);
        JLabel b = new JLabel(subtitle);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.setForeground(TEXT_MUTED);
        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(a); texts.add(b);
        p.add(texts, BorderLayout.WEST);
        return p;
    }

    public static JPanel createCard(String title, String value, Color accentColor) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        JLabel titleLbl = new JLabel(title.toUpperCase());
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLbl.setForeground(TEXT_MUTED);

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valLbl.setForeground(accentColor);

        p.add(titleLbl, BorderLayout.NORTH);
        p.add(valLbl, BorderLayout.CENTER);
        return p;
    }

    public static void info(Component c, String s) {
        JOptionPane.showMessageDialog(c, s, "CargoManage", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component c, String s) {
        JOptionPane.showMessageDialog(c, s, "CargoManage", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(Component c, String s, String title) {
        return JOptionPane.showConfirmDialog(c, s, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public static int selectedId(JComboBox<String> box) {
        if (box == null || box.getSelectedItem() == null) return -1;
        String s = box.getSelectedItem().toString();
        try {
            return Integer.parseInt(s.split(" - ")[0].trim());
        } catch (Exception e) {
            return -1;
        }
    }
}
