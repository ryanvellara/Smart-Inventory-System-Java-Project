package com.inventory.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a complete generated bill / invoice.
 */
public class Bill {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private final int billNumber;
    private final List<BillItem> items;
    private final LocalDateTime generatedAt;
    private final String customerName;

    public Bill(int billNumber, List<BillItem> items, String customerName) {
        this.billNumber   = billNumber;
        this.items        = new ArrayList<>(items);
        this.generatedAt  = LocalDateTime.now();
        this.customerName = customerName == null || customerName.isBlank()
                            ? "Walk-in Customer" : customerName;
    }

    public int getBillNumber()          { return billNumber; }
    public List<BillItem> getItems()    { return Collections.unmodifiableList(items); }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public String getCustomerName()     { return customerName; }

    public double getGrandTotal() {
        return items.stream().mapToDouble(BillItem::getSubtotal).sum();
    }

    public int getTotalItemCount() {
        return items.stream().mapToInt(BillItem::getQuantity).sum();
    }

    public String getFormattedDate() {
        return generatedAt.format(FMT);
    }

    /** Produce a plain-text receipt string */
    public String toReceiptText() {
        StringBuilder sb = new StringBuilder();
        String line = "─".repeat(44);
        sb.append(line).append("\n");
        sb.append("       SMART INVENTORY SYSTEM\n");
        sb.append("            SALES RECEIPT\n");
        sb.append(line).append("\n");
        sb.append(String.format("Bill No : #%04d%n", billNumber));
        sb.append(String.format("Date    : %s%n", getFormattedDate()));
        sb.append(String.format("Customer: %s%n", customerName));
        sb.append(line).append("\n");
        sb.append(String.format("%-20s %4s %8s %10s%n",
                "Item", "Qty", "Price", "Subtotal"));
        sb.append(line).append("\n");
        for (BillItem bi : items) {
            sb.append(String.format("%-20s %4d %8.2f %10.2f%n",
                    truncate(bi.getProduct().getName(), 20),
                    bi.getQuantity(),
                    bi.getPriceAtSale(),
                    bi.getSubtotal()));
        }
        sb.append(line).append("\n");
        sb.append(String.format("%-28s %14.2f%n", "GRAND TOTAL (₹)", getGrandTotal()));
        sb.append(line).append("\n");
        sb.append("        Thank you for shopping!\n");
        sb.append(line).append("\n");
        return sb.toString();
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
