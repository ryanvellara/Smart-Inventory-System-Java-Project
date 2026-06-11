package com.inventory.dao;

import com.inventory.model.Bill;
import com.inventory.model.BillItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * BillDAO — in-memory store for generated bills.
 * Singleton pattern, same as ProductDAO.
 */
public class BillDAO {

    private static BillDAO instance;
    private final List<Bill> bills = new ArrayList<>();
    private int nextBillNumber = 1001;

    private BillDAO() {}

    public static synchronized BillDAO getInstance() {
        if (instance == null) instance = new BillDAO();
        return instance;
    }

    /** Save a new bill and return the assigned bill number */
    public synchronized Bill saveBill(List<BillItem> items, String customerName) {
        Bill bill = new Bill(nextBillNumber++, items, customerName);
        bills.add(bill);
        return bill;
    }

    public synchronized List<Bill> getAllBills() {
        return Collections.unmodifiableList(new ArrayList<>(bills));
    }

    public synchronized int getTotalBillsCount() {
        return bills.size();
    }

    public synchronized double getTotalRevenue() {
        return bills.stream().mapToDouble(Bill::getGrandTotal).sum();
    }
}
