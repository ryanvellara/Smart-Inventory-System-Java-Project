package com.inventory.ui;

import com.inventory.model.Bill;
import com.inventory.model.BillItem;
import com.inventory.model.Product;
import com.inventory.servlet.BillController;
import com.inventory.servlet.InventoryController;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * BillPanel — the complete billing UI.
 *
 * Layout:
 *  LEFT  → Build the current bill (add items, set customer, generate)
 *  RIGHT → Receipt preview + Bill history
 */
public class BillPanel extends JPanel {

    private final BillController   billCtrl;
    private final InventoryController invCtrl;
    private final DashboardPanel   dashboard;

    // Current bill items being built
    private final List<BillItem> currentItems = new ArrayList<>();

    // ── Left panel widgets ──
    private JComboBox<Product> productCombo;
    private JSpinner           qtySpinner;
    private JTextField         customerField;
    private DefaultTableModel  billTableModel;
    private JLabel             grandTotalLabel;

    // ── Right panel widgets ──
    private JTextArea          receiptArea;
    private DefaultTableModel  historyTableModel;

    private static final NumberFormat NF =
            NumberFormat.getNumberInstance(new Locale("en", "IN"));

    public BillPanel(BillController billCtrl,
                     InventoryController invCtrl,
                     DashboardPanel dashboard) {
        this.billCtrl  = billCtrl;
        this.invCtrl   = invCtrl;
        this.dashboard = dashboard;

        setLayout(new BorderLayout(16, 0));
        setOpaque(false);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        add(buildLeft(),  BorderLayout.CENTER);
        add(buildRight(), BorderLayout.EAST);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  LEFT — Bill Builder
    // ════════════════════════════════════════════════════════════════════════

    private JPanel buildLeft() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);

        p.add(buildItemSelector(), BorderLayout.NORTH);
        p.add(buildBillTable(),    BorderLayout.CENTER);
        p.add(buildFooter(),       BorderLayout.SOUTH);
        return p;
    }

    /** Top part: product picker + qty + Add button */
    private JPanel buildItemSelector() {
        JPanel card = card();
        card.setLayout(new BorderLayout(0, 12));

        // Title
        JLabel title = new JLabel("🧾  New Bill");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(30, 41, 59));
        card.add(title, BorderLayout.NORTH);

        // Customer name row
        JPanel custRow = new JPanel(new BorderLayout(8, 0));
        custRow.setOpaque(false);
        custRow.add(fieldLabel("Customer Name:"), BorderLayout.WEST);
        customerField = styledField(20);
        customerField.putClientProperty("JTextField.placeholderText", "Walk-in Customer");
        custRow.add(customerField, BorderLayout.CENTER);

        // Product selection row
        JPanel selRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        selRow.setOpaque(false);

        selRow.add(fieldLabel("Product:"));
        productCombo = new JComboBox<>();
        productCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        productCombo.setPreferredSize(new Dimension(220, 32));
        productCombo.setRenderer(new ProductComboRenderer());
        refreshProductCombo();
        selRow.add(productCombo);

        selRow.add(fieldLabel("  Qty:"));
        qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        qtySpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        qtySpinner.setPreferredSize(new Dimension(70, 32));
        selRow.add(qtySpinner);

        JButton addBtn = actionButton("➕  Add to Bill", new Color(16, 185, 129));
        addBtn.addActionListener(e -> onAddItem());
        selRow.add(addBtn);

        JPanel rows = new JPanel(new GridLayout(2, 1, 0, 8));
        rows.setOpaque(false);
        rows.add(custRow);
        rows.add(selRow);
        card.add(rows, BorderLayout.CENTER);
        return card;
    }

    /** Middle: table of items added to current bill */
    private JScrollPane buildBillTable() {
        billTableModel = new DefaultTableModel(
                new String[]{"#", "Product", "Category", "Unit Price", "Qty", "Subtotal"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable t = new JTable(billTableModel);
        styleTable(t);

        // Column widths
        int[] w = {30, 180, 110, 100, 50, 110};
        for (int i = 0; i < w.length; i++)
            t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);

        // Right-align numeric cols
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int c : new int[]{3, 4, 5}) t.getColumnModel().getColumn(c).setCellRenderer(right);

        // Double-click to remove item
        t.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) onRemoveItem(t.getSelectedRow());
            }
        });

        JScrollPane sp = styledScrollPane(t);
        sp.setBorder(BorderFactory.createCompoundBorder(
                new TitledBorder(new LineBorder(new Color(226, 232, 240), 1, true),
                        " Bill Items  (double-click a row to remove) ",
                        TitledBorder.LEFT, TitledBorder.TOP,
                        new Font("Segoe UI", Font.PLAIN, 11),
                        new Color(100, 116, 139)),
                new EmptyBorder(4, 4, 4, 4)));
        return sp;
    }

    /** Bottom: grand total + action buttons */
    private JPanel buildFooter() {
        JPanel card = card();
        card.setLayout(new BorderLayout(12, 0));

        // Grand total
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        totalPanel.setOpaque(false);
        JLabel totalLbl = new JLabel("Grand Total:  ");
        totalLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        totalLbl.setForeground(new Color(71, 85, 105));
        grandTotalLabel = new JLabel("₹0");
        grandTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        grandTotalLabel.setForeground(new Color(16, 185, 129));
        totalPanel.add(totalLbl);
        totalPanel.add(grandTotalLabel);
        card.add(totalPanel, BorderLayout.CENTER);

        // Buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);

        JButton clearBtn = actionButton("🗑  Clear Bill", new Color(239, 68, 68));
        clearBtn.addActionListener(e -> onClearBill());

        JButton generateBtn = actionButton("✅  Generate Bill", new Color(59, 130, 246));
        generateBtn.addActionListener(e -> onGenerateBill());

        btns.add(clearBtn);
        btns.add(generateBtn);
        card.add(btns, BorderLayout.EAST);
        return card;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  RIGHT — Receipt + History
    // ════════════════════════════════════════════════════════════════════════

    private JPanel buildRight() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(400, 0));

        p.add(buildReceiptPanel(), BorderLayout.CENTER);
        p.add(buildHistoryPanel(), BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildReceiptPanel() {
        JPanel card = card();
        card.setLayout(new BorderLayout(0, 8));

        JLabel title = new JLabel("🖨  Receipt Preview");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(new Color(30, 41, 59));
        card.add(title, BorderLayout.NORTH);

        receiptArea = new JTextArea(16, 36);
        receiptArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        receiptArea.setEditable(false);
        receiptArea.setBackground(new Color(248, 250, 252));
        receiptArea.setForeground(new Color(30, 41, 59));
        receiptArea.setBorder(new EmptyBorder(8, 10, 8, 10));
        receiptArea.setText("  Generate a bill to see the receipt here.");

        JScrollPane sp = styledScrollPane(receiptArea);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildHistoryPanel() {
        JPanel card = card();
        card.setLayout(new BorderLayout(0, 8));
        card.setPreferredSize(new Dimension(0, 200));

        JLabel title = new JLabel("📋  Bill History");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(new Color(30, 41, 59));
        card.add(title, BorderLayout.NORTH);

        historyTableModel = new DefaultTableModel(
                new String[]{"Bill #", "Customer", "Items", "Total", "Date"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable ht = new JTable(historyTableModel);
        styleTable(ht);
        int[] w = {60, 120, 50, 90, 140};
        for (int i = 0; i < w.length; i++)
            ht.getColumnModel().getColumn(i).setPreferredWidth(w[i]);

        // Click history row → show that bill's receipt
        ht.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && ht.getSelectedRow() >= 0) {
                List<Bill> bills = billCtrl.doGetAllBills();
                int idx = bills.size() - 1 - ht.getSelectedRow(); // newest first
                if (idx >= 0) receiptArea.setText(bills.get(idx).toReceiptText());
            }
        });

        card.add(styledScrollPane(ht), BorderLayout.CENTER);
        return card;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Actions
    // ════════════════════════════════════════════════════════════════════════

    private void onAddItem() {
        Product selected = (Product) productCombo.getSelectedItem();
        if (selected == null) { warn("Please select a product."); return; }

        int qty = (Integer) qtySpinner.getValue();

        // Check if product is already in the bill — if so, increase qty
        for (BillItem bi : currentItems) {
            if (bi.getProduct().getId() == selected.getId()) {
                int combined = bi.getQuantity() + qty;
                if (combined > selected.getQuantity()) {
                    warn("Only " + selected.getQuantity() + " units available for \"" +
                            selected.getName() + "\".");
                    return;
                }
                currentItems.remove(bi);
                currentItems.add(new BillItem(selected, combined));
                rebuildBillTable();
                return;
            }
        }

        // New item — check stock
        if (qty > selected.getQuantity()) {
            warn("Only " + selected.getQuantity() + " units available for \"" +
                    selected.getName() + "\".");
            return;
        }

        currentItems.add(new BillItem(selected, qty));
        rebuildBillTable();
    }

    private void onRemoveItem(int row) {
        if (row < 0 || row >= currentItems.size()) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Remove \"" + currentItems.get(row).getProduct().getName() + "\" from bill?",
                "Remove Item", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            currentItems.remove(row);
            rebuildBillTable();
        }
    }

    private void onClearBill() {
        if (currentItems.isEmpty()) return;
        int c = JOptionPane.showConfirmDialog(this,
                "Clear all items from the current bill?",
                "Clear Bill", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c == JOptionPane.YES_OPTION) {
            currentItems.clear();
            rebuildBillTable();
            receiptArea.setText("  Generate a bill to see the receipt here.");
        }
    }

    private void onGenerateBill() {
        if (currentItems.isEmpty()) { warn("Add at least one item to the bill."); return; }
        String customer = customerField.getText().trim();

        try {
            Bill bill = billCtrl.doGenerateBill(new ArrayList<>(currentItems), customer);

            // Show receipt
            receiptArea.setText(bill.toReceiptText());

            // Add to history table (newest at top)
            historyTableModel.insertRow(0, new Object[]{
                String.format("#%04d", bill.getBillNumber()),
                bill.getCustomerName(),
                bill.getTotalItemCount(),
                "₹" + NF.format((long) bill.getGrandTotal()),
                bill.getFormattedDate()
            });

            // Clear current bill
            currentItems.clear();
            customerField.setText("");
            rebuildBillTable();

            // Refresh inventory data everywhere
            refreshProductCombo();
            dashboard.refresh();

            JOptionPane.showMessageDialog(this,
                    "Bill #" + String.format("%04d", bill.getBillNumber()) +
                    " generated successfully!\nInventory has been updated.",
                    "Bill Generated", JOptionPane.INFORMATION_MESSAGE);

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Cannot Generate Bill", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Helpers
    // ════════════════════════════════════════════════════════════════════════

    private void rebuildBillTable() {
        billTableModel.setRowCount(0);
        double total = 0;
        int row = 1;
        for (BillItem bi : currentItems) {
            billTableModel.addRow(new Object[]{
                row++,
                bi.getProduct().getName(),
                bi.getProduct().getCategory(),
                "₹" + NF.format(bi.getPriceAtSale()),
                bi.getQuantity(),
                "₹" + NF.format((long) bi.getSubtotal())
            });
            total += bi.getSubtotal();
        }
        grandTotalLabel.setText("₹" + NF.format((long) total));
    }

    public void refreshProductCombo() {
        Product selected = (Product) productCombo.getSelectedItem();
        productCombo.removeAllItems();
        for (Product p : invCtrl.doGetAll()) {
            if (p.getQuantity() > 0) productCombo.addItem(p);
        }
        if (selected != null) productCombo.setSelectedItem(selected);
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    private JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(14, 16, 14, 16)));
        return p;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(new Color(71, 85, 105));
        return l;
    }

    private JTextField styledField(int cols) {
        JTextField f = new JTextField(cols);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(5, 9, 5, 9)));
        return f;
    }

    private JButton actionButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setBorder(new EmptyBorder(7, 14, 7, 14));
        b.setFocusPainted(false);
        b.setContentAreaFilled(true);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void styleTable(JTable t) {
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setRowHeight(28);
        t.setShowHorizontalLines(true);
        t.setGridColor(new Color(241, 245, 249));
        t.setSelectionBackground(new Color(219, 234, 254));
        t.setSelectionForeground(new Color(30, 41, 59));
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.getTableHeader().setBackground(new Color(248, 250, 252));
        t.getTableHeader().setForeground(new Color(71, 85, 105));
        t.getTableHeader().setReorderingAllowed(false);
    }

    private JScrollPane styledScrollPane(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(new LineBorder(new Color(226, 232, 240), 1, true));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Notice", JOptionPane.WARNING_MESSAGE);
    }

    // ── Custom combo renderer showing name + stock ────────────────────────────

    private static class ProductComboRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Product p) {
                setText(p.getName() + "  [Stock: " + p.getQuantity() + "]  ₹" +
                        NumberFormat.getNumberInstance(new Locale("en","IN"))
                                .format(p.getPrice()));
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
            }
            return this;
        }
    }
}
