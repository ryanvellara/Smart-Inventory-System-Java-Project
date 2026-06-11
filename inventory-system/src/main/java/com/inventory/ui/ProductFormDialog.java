package com.inventory.ui;

import com.inventory.model.Product;
import com.inventory.servlet.InventoryController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Modal dialog for Add / Edit product.
 */
public class ProductFormDialog extends JDialog {

    private final InventoryController ctrl;
    private final Product existing;     // null = ADD mode
    private boolean saved = false;

    private JTextField nameField;
    private JComboBox<String> categoryCombo;
    private JTextField newCategoryField;
    private JSpinner quantitySpinner;
    private JTextField priceField;
    private JSpinner thresholdSpinner;

    public ProductFormDialog(Frame owner, InventoryController ctrl, Product existing) {
        super(owner, existing == null ? "Add New Product" : "Edit Product", true);
        this.ctrl     = ctrl;
        this.existing = existing;
        buildUI();
        if (existing != null) populate();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(20, 24, 16, 24));
        root.setBackground(Color.WHITE);

        // ── Title bar ──
        JLabel title = new JLabel(existing == null ? "➕  New Product" : "✏️  Edit Product");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(30, 41, 59));
        root.add(title, BorderLayout.NORTH);

        // ── Form grid ──
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints lc = labelGbc(), fc = fieldGbc();

        // Name
        form.add(label("Product Name *"), lc(lc, 0));
        nameField = styledField();
        form.add(nameField, fc(fc, 0));

        // Category
        form.add(label("Category *"), lc(lc, 1));
        List<String> cats = ctrl.doGetCategories();
        cats.add(0, "-- Select Category --");
        cats.add("+ New category…");
        categoryCombo = new JComboBox<>(cats.toArray(new String[0]));
        styleCombo(categoryCombo);
        newCategoryField = styledField();
        newCategoryField.setVisible(false);
        newCategoryField.setToolTipText("Type new category name");
        JPanel catPanel = new JPanel(new BorderLayout(6, 0));
        catPanel.setOpaque(false);
        catPanel.add(categoryCombo, BorderLayout.CENTER);
        catPanel.add(newCategoryField, BorderLayout.SOUTH);
        categoryCombo.addActionListener(e -> {
            boolean isNew = "+ New category…".equals(categoryCombo.getSelectedItem());
            newCategoryField.setVisible(isNew);
            pack();
        });
        form.add(catPanel, fc(fc, 1));

        // Quantity
        form.add(label("Quantity *"), lc(lc, 2));
        quantitySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 999999, 1));
        styleSpinner(quantitySpinner);
        form.add(quantitySpinner, fc(fc, 2));

        // Price
        form.add(label("Price (₹) *"), lc(lc, 3));
        priceField = styledField();
        priceField.setText("0.00");
        form.add(priceField, fc(fc, 3));

        // Low-stock threshold
        form.add(label("Low Stock Alert"), lc(lc, 4));
        thresholdSpinner = new JSpinner(new SpinnerNumberModel(10, 0, 999999, 1));
        styleSpinner(thresholdSpinner);
        form.add(thresholdSpinner, fc(fc, 4));

        root.add(form, BorderLayout.CENTER);

        // ── Buttons ──
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);

        JButton cancel = new JButton("Cancel");
        cancel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancel.setForeground(new Color(100, 116, 139));
        cancel.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        cancel.setFocusPainted(false);
        cancel.addActionListener(e -> dispose());

        JButton save = new JButton(existing == null ? "Add Product" : "Save Changes");
        save.setFont(new Font("Segoe UI", Font.BOLD, 13));
        save.setBackground(new Color(59, 130, 246));
        save.setForeground(Color.WHITE);
        save.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        save.setFocusPainted(false);
        save.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        save.addActionListener(e -> onSave());

        btns.add(cancel);
        btns.add(save);
        root.add(btns, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void onSave() {
        try {
            String name = nameField.getText().trim();
            String category;
            if ("+ New category…".equals(categoryCombo.getSelectedItem())) {
                category = newCategoryField.getText().trim();
            } else {
                category = (String) categoryCombo.getSelectedItem();
                if (category.startsWith("--")) category = "";
            }
            int qty   = (Integer) quantitySpinner.getValue();
            double price;
            try { price = Double.parseDouble(priceField.getText().trim()); }
            catch (NumberFormatException ex) { throw new IllegalArgumentException("Price must be a valid number."); }
            int threshold = (Integer) thresholdSpinner.getValue();

            if (existing == null) {
                ctrl.doCreate(name, category, qty, price, threshold);
            } else {
                ctrl.doUpdate(existing.getId(), name, category, qty, price, threshold);
            }
            saved = true;
            dispose();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void populate() {
        nameField.setText(existing.getName());
        categoryCombo.setSelectedItem(existing.getCategory());
        quantitySpinner.setValue(existing.getQuantity());
        priceField.setText(String.format("%.2f", existing.getPrice()));
        thresholdSpinner.setValue(existing.getLowStockThreshold());
    }

    public boolean isSaved() { return saved; }

    // ── Styling helpers ──────────────────────────────────────────────────────

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(new Color(71, 85, 105));
        return l;
    }

    private JTextField styledField() {
        JTextField f = new JTextField(20);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        return f;
    }

    private void styleCombo(JComboBox<String> cb) {
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setBackground(Color.WHITE);
    }

    private void styleSpinner(JSpinner sp) {
        sp.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ((JSpinner.DefaultEditor) sp.getEditor()).getTextField()
                .setFont(new Font("Segoe UI", Font.PLAIN, 13));
    }

    private GridBagConstraints labelGbc() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(6, 0, 6, 14);
        gc.gridx = 0;
        return gc;
    }

    private GridBagConstraints fieldGbc() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.insets = new Insets(6, 0, 6, 0);
        gc.gridx = 1;
        return gc;
    }

    private GridBagConstraints lc(GridBagConstraints gc, int row) {
        gc.gridy = row; return gc;
    }
    private GridBagConstraints fc(GridBagConstraints gc, int row) {
        gc.gridy = row; return gc;
    }
}
