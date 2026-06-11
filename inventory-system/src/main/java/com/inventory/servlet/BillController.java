package com.inventory.servlet;

import com.inventory.dao.BillDAO;
import com.inventory.dao.ProductDAO;
import com.inventory.model.Bill;
import com.inventory.model.BillItem;
import com.inventory.model.Product;

import java.util.List;
import java.util.Optional;

/**
 * BillController — Servlet-pattern controller for billing operations.
 *
 *  doPost / action=generateBill  → validates stock, deducts inventory, saves bill
 *  doGet  / action=getBills      → returns all past bills
 *
 * Stock deduction is done here (not in BillDAO) so the controller owns
 * all business logic, exactly like a real Servlet would.
 */
public class BillController {

    private final BillDAO  billDAO  = BillDAO.getInstance();
    private final ProductDAO productDAO = ProductDAO.getInstance();

    // ── GET operations ────────────────────────────────────────────────────────

    public List<Bill> doGetAllBills() {
        return billDAO.getAllBills();
    }

    public int getTotalBillsCount()  { return billDAO.getTotalBillsCount(); }
    public double getTotalRevenue()  { return billDAO.getTotalRevenue(); }

    // ── POST / action=generateBill ────────────────────────────────────────────

    /**
     * Validates that every item has sufficient stock,
     * then deducts from inventory and saves the bill atomically.
     *
     * @throws IllegalArgumentException if any item is out of stock or list is empty
     */
    public Bill doGenerateBill(List<BillItem> items, String customerName) {
        if (items == null || items.isEmpty())
            throw new IllegalArgumentException("Cannot generate an empty bill.");

        // ── Step 1: validate all items first (fail fast before touching inventory)
        for (BillItem bi : items) {
            Optional<Product> opt = productDAO.getById(bi.getProduct().getId());
            if (opt.isEmpty())
                throw new IllegalArgumentException(
                        "Product not found: " + bi.getProduct().getName());

            Product current = opt.get();
            if (current.getQuantity() < bi.getQuantity()) {
                throw new IllegalArgumentException(
                        "Insufficient stock for \"" + current.getName() + "\".\n" +
                        "Requested: " + bi.getQuantity() +
                        "  |  Available: " + current.getQuantity());
            }
        }

        // ── Step 2: deduct stock for each item
        for (BillItem bi : items) {
            Product p = productDAO.getById(bi.getProduct().getId()).get();
            p.setQuantity(p.getQuantity() - bi.getQuantity());
            productDAO.updateProduct(p);
        }

        // ── Step 3: persist the bill and return it
        return billDAO.saveBill(items, customerName);
    }
}
