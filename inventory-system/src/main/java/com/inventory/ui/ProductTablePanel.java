package com.inventory.ui;

import com.inventory.model.Product;
import com.inventory.servlet.InventoryController;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Central product table with toolbar (search, filter, CRUD buttons).
 */
public class ProductTablePanel extends JPanel {

    private final InventoryController ctrl;
    private final DashboardPanel dashboard;

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    private JLabel statusLabel;

    private static final String[] COLUMNS = {
        "ID", "Name", "Category", "Qty", "Price (₹)", "Total Value (₹)", "Status"
    };

    public ProductTablePanel(InventoryController ctrl, DashboardPanel dashboard) {
        this.ctrl      = ctrl;
        this.dashboard = dashboard;
        setLayout(new BorderLayout(0, 10));
        setOpaque(false);
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTable(),   BorderLayout.CENTER);
        add(buildStatus(),  BorderLayout.SOUTH);
        loadData(ctrl.doGetAll());
    }

    // ── Toolbar ──────────────────────────────────────────────────────────────

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setOpaque(false);

        // Left: search + filter
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        searchField = new JTextField(18);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.putClientProperty("JTextField.placeholderText", "Search products…");
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(6, 10, 6, 10)));

        JButton searchBtn = iconButton("🔍 Search", new Color(59, 130, 246));
        searchBtn.addActionListener(e -> onSearch());
        searchField.addActionListener(e -> onSearch());

        List<String> cats = ctrl.doGetCategories();
        cats.add(0, "All Categories");
        categoryFilter = new JComboBox<>(cats.toArray(new String[0]));
        categoryFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        categoryFilter.addActionListener(e -> onFilter());

        JButton lowStockBtn = iconButton("⚠ Low Stock", new Color(245, 158, 11));
        lowStockBtn.addActionListener(e -> onShowLowStock());

        JButton refreshBtn = iconButton("↺ Refresh", new Color(100, 116, 139));
        refreshBtn.addActionListener(e -> refresh());

        left.add(searchField);
        left.add(searchBtn);
        left.add(new JSeparator(SwingConstants.VERTICAL));
        left.add(new JLabel("Filter:"));
        left.add(categoryFilter);
        left.add(lowStockBtn);
        left.add(refreshBtn);

        // Right: action buttons
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JButton addBtn    = iconButton("➕ Add",     new Color(16,  185, 129));
        JButton editBtn   = iconButton("✏ Edit",    new Color(59,  130, 246));
        JButton deleteBtn = iconButton("🗑 Delete",  new Color(239, 68,  68));
        JButton restockBtn= iconButton("📦 Restock", new Color(139, 92,  246));

        addBtn.addActionListener(e    -> onAdd());
        editBtn.addActionListener(e   -> onEdit());
        deleteBtn.addActionListener(e -> onDelete());
        restockBtn.addActionListener(e-> onRestock());

        right.add(addBtn);
        right.add(editBtn);
        right.add(restockBtn);
        right.add(deleteBtn);

        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ── Table ─────────────────────────────────────────────────────────────────

    private JScrollPane buildTable() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 0 || c == 3 ? Integer.class : Object.class;
            }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(32);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(241, 245, 249));
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(new Color(30, 41, 59));
        table.setFocusable(false);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(new Color(71, 85, 105));
        header.setBorder(new MatteBorder(0, 0, 2, 0, new Color(203, 213, 225)));
        header.setReorderingAllowed(false);

        // Column widths
        int[] widths = {50, 200, 130, 70, 110, 130, 90};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Custom renderer for Status column and row colours
        table.setDefaultRenderer(Object.class, new ProductTableRenderer());

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) onEdit();
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(0, 0, 0, 0)));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    private JPanel buildStatus() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        statusLabel = new JLabel("Loading…");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setForeground(new Color(100, 116, 139));
        p.add(statusLabel);
        return p;
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    private void loadData(List<Product> products) {
        tableModel.setRowCount(0);
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("en", "IN"));
        for (Product p : products) {
            tableModel.addRow(new Object[]{
                p.getId(),
                p.getName(),
                p.getCategory(),
                p.getQuantity(),
                "₹" + nf.format(p.getPrice()),
                "₹" + nf.format((long) p.getTotalValue()),
                p.isLowStock() ? "LOW STOCK" : "OK"
            });
        }
        statusLabel.setText(products.size() + " product" + (products.size() != 1 ? "s" : "") + " shown");
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void onSearch() {
        loadData(ctrl.doSearch(searchField.getText()));
    }

    private void onFilter() {
        String sel = (String) categoryFilter.getSelectedItem();
        loadData(ctrl.doFilterByCategory("All Categories".equals(sel) ? "All" : sel));
    }

    private void onShowLowStock() {
        loadData(ctrl.doGetLowStock());
        statusLabel.setText("Showing low-stock items only");
    }

    private void refresh() {
        searchField.setText("");
        categoryFilter.setSelectedIndex(0);
        // Rebuild category list
        List<String> cats = ctrl.doGetCategories();
        cats.add(0, "All Categories");
        categoryFilter.setModel(new DefaultComboBoxModel<>(cats.toArray(new String[0])));
        loadData(ctrl.doGetAll());
        dashboard.refresh();
    }

    private void onAdd() {
        ProductFormDialog dlg = new ProductFormDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), ctrl, null);
        dlg.setVisible(true);
        if (dlg.isSaved()) refresh();
    }

    private void onEdit() {
        Product p = getSelectedProduct();
        if (p == null) { warn("Please select a product to edit."); return; }
        ProductFormDialog dlg = new ProductFormDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), ctrl, p);
        dlg.setVisible(true);
        if (dlg.isSaved()) refresh();
    }

    private void onDelete() {
        Product p = getSelectedProduct();
        if (p == null) { warn("Please select a product to delete."); return; }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete \"" + p.getName() + "\"?  This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            ctrl.doDelete(p.getId());
            refresh();
        }
    }

    private void onRestock() {
        Product p = getSelectedProduct();
        if (p == null) { warn("Please select a product to restock."); return; }
        String input = JOptionPane.showInputDialog(this,
                "Add stock for: " + p.getName() + "\nCurrent qty: " + p.getQuantity(),
                "Restock", JOptionPane.QUESTION_MESSAGE);
        if (input == null || input.isBlank()) return;
        try {
            int qty = Integer.parseInt(input.trim());
            ctrl.doRestock(p.getId(), qty);
            refresh();
            JOptionPane.showMessageDialog(this,
                    "Restocked " + qty + " units.\nNew quantity: " + (p.getQuantity() + qty),
                    "Restock Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            warn("Please enter a valid whole number.");
        } catch (IllegalArgumentException ex) {
            warn(ex.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Product getSelectedProduct() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int id = (Integer) tableModel.getValueAt(row, 0);
        return ctrl.doGetById(id).orElse(null);
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Notice", JOptionPane.INFORMATION_MESSAGE);
    }

    private JButton iconButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        return btn;
    }

    // ── Custom cell renderer ──────────────────────────────────────────────────

    private static class ProductTableRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                String status = (String) table.getModel().getValueAt(row, 6);
                c.setBackground("LOW STOCK".equals(status)
                        ? new Color(254, 242, 242)
                        : (row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));
            }
            if (column == 6) {
                String s = value == null ? "" : value.toString();
                setForeground(isSelected ? Color.WHITE :
                        ("LOW STOCK".equals(s) ? new Color(239, 68, 68) : new Color(16, 185, 129)));
                setFont(getFont().deriveFont(Font.BOLD));
            } else {
                setForeground(isSelected ? Color.WHITE : new Color(30, 41, 59));
                setFont(table.getFont());
            }
            setBorder(new EmptyBorder(0, 8, 0, 8));
            return c;
        }
    }
}
