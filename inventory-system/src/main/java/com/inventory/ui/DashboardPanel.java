package com.inventory.ui;

import com.inventory.servlet.InventoryController;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Top dashboard panel showing 4 KPI cards.
 */
public class DashboardPanel extends JPanel {

    private final InventoryController ctrl;

    private JLabel totalProductsVal;
    private JLabel totalItemsVal;
    private JLabel totalValueVal;
    private JLabel lowStockVal;

    public DashboardPanel(InventoryController ctrl) {
        this.ctrl = ctrl;
        setLayout(new BorderLayout(0, 12));
        setOpaque(false);
        setBorder(new EmptyBorder(0, 0, 8, 0));
        add(buildTitle(), BorderLayout.NORTH);
        add(buildCards(), BorderLayout.CENTER);
        refresh();
    }

    private JLabel buildTitle() {
        JLabel lbl = new JLabel("Inventory Dashboard");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbl.setForeground(new Color(30, 41, 59));
        lbl.setBorder(new EmptyBorder(0, 0, 4, 0));
        return lbl;
    }

    private JPanel buildCards() {
        JPanel p = new JPanel(new GridLayout(1, 4, 12, 0));
        p.setOpaque(false);

        totalProductsVal = new JLabel("0");
        totalItemsVal    = new JLabel("0");
        totalValueVal    = new JLabel("₹0");
        lowStockVal      = new JLabel("0");

        p.add(card("📦 Products",   totalProductsVal, new Color(59,  130, 246)));
        p.add(card("🗃 Total Items", totalItemsVal,    new Color(16,  185, 129)));
        p.add(card("💰 Value",       totalValueVal,    new Color(245, 158,  11)));
        p.add(card("⚠ Low Stock",   lowStockVal,      new Color(239,  68,  68)));
        return p;
    }

    private JPanel card(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(14, 18, 14, 18)
        ));

        // Accent bar on left
        JPanel bar = new JPanel();
        bar.setPreferredSize(new Dimension(4, 0));
        bar.setBackground(accent);
        card.add(bar, BorderLayout.WEST);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLbl.setForeground(new Color(100, 116, 139));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(accent);

        JPanel inner = new JPanel(new GridLayout(2, 1, 0, 2));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(0, 10, 0, 0));
        inner.add(titleLbl);
        inner.add(valueLabel);
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    public void refresh() {
        totalProductsVal.setText(String.valueOf(ctrl.getTotalProducts()));
        totalItemsVal.setText(String.valueOf(ctrl.getTotalItems()));
        NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("en", "IN"));
        totalValueVal.setText("₹" + fmt.format((long) ctrl.getTotalValue()));
        int ls = ctrl.getLowStockCount();
        lowStockVal.setText(String.valueOf(ls));
        lowStockVal.setForeground(ls > 0 ? new Color(239, 68, 68) : new Color(16, 185, 129));
    }
}
