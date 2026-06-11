package com.inventory.ui;

import com.inventory.servlet.BillController;
import com.inventory.servlet.InventoryController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Main application window.
 * Uses a JTabbedPane to switch between Inventory and Billing tabs.
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        super("Smart Inventory System");

        InventoryController invCtrl  = new InventoryController();
        BillController      billCtrl = new BillController();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1150, 700));
        setPreferredSize(new Dimension(1280, 780));

        // ── Root panel ──
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(new Color(241, 245, 249));

        // ── Sidebar ──
        root.add(buildSidebar(), BorderLayout.WEST);

        // ── Shared dashboard (shown above both tabs) ──
        DashboardPanel dashboard = new DashboardPanel(invCtrl);

        // ── Inventory tab content ──
        ProductTablePanel tablePanel = new ProductTablePanel(invCtrl, dashboard);

        // ── Billing tab content ──
        BillPanel billPanel = new BillPanel(billCtrl, invCtrl, dashboard);

        // ── Tabbed pane ──
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.setBackground(new Color(241, 245, 249));

        tabs.addTab("  📦  Inventory  ", tablePanel);
        tabs.addTab("  🧾  Billing    ", billPanel);

        // When switching to Billing tab, refresh the product combo with latest stock
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 1) {
                billPanel.refreshProductCombo();
            }
            if (tabs.getSelectedIndex() == 0) {
                dashboard.refresh();
            }
        });

        // ── Main content area ──
        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 24, 20, 24));
        content.add(dashboard, BorderLayout.NORTH);
        content.add(tabs,      BorderLayout.CENTER);

        root.add(content, BorderLayout.CENTER);

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setPreferredSize(new Dimension(56, 0));
        side.setBackground(new Color(30, 41, 59));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(new EmptyBorder(20, 0, 20, 0));

        JLabel logo = new JLabel("📦");
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        logo.setAlignmentX(CENTER_ALIGNMENT);
        logo.setBorder(new EmptyBorder(0, 0, 16, 0));
        side.add(logo);

        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setForeground(new Color(51, 65, 85));
        sep.setMaximumSize(new Dimension(40, 1));
        side.add(sep);

        return side;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        System.setProperty("sun.java2d.uiScale", "1");
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
