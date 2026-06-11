package com.inventory.dao;

import com.inventory.model.Product;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ProductDAO — simulates the DAO/Repository layer typically backed by a
 * database in a Servlet-based web app. Here we use an in-memory store so
 * the Swing desktop app is self-contained and needs no external DB.
 */
public class ProductDAO {

    private static ProductDAO instance;
    private final Map<Integer, Product> store = new LinkedHashMap<>();
    private int nextId = 1;

    private ProductDAO() {
        // Seed demo data
        addProduct(new Product(0, "Laptop Pro 15",    "Electronics", 42,  75000.00, 10));
        addProduct(new Product(0, "Wireless Mouse",   "Electronics", 8,   1299.00,  10));
        addProduct(new Product(0, "Office Chair",     "Furniture",   15,  12500.00, 5));
        addProduct(new Product(0, "Notebook A4 Pack", "Stationery",  200, 150.00,   20));
        addProduct(new Product(0, "USB-C Hub 7-in-1", "Electronics", 5,   2499.00,  10));
        addProduct(new Product(0, "Standing Desk",    "Furniture",   3,   28000.00, 5));
        addProduct(new Product(0, "Ballpoint Pens x10","Stationery", 95,  80.00,    30));
    }

    public static synchronized ProductDAO getInstance() {
        if (instance == null) instance = new ProductDAO();
        return instance;
    }

    // CREATE
    public synchronized Product addProduct(Product p) {
        p.setId(nextId++);
        store.put(p.getId(), p);
        return p;
    }

    // READ ALL
    public synchronized List<Product> getAllProducts() {
        return new ArrayList<>(store.values());
    }

    // READ by ID
    public synchronized Optional<Product> getById(int id) {
        return Optional.ofNullable(store.get(id));
    }

    // UPDATE
    public synchronized boolean updateProduct(Product p) {
        if (!store.containsKey(p.getId())) return false;
        store.put(p.getId(), p);
        return true;
    }

    // DELETE
    public synchronized boolean deleteProduct(int id) {
        return store.remove(id) != null;
    }

    // SEARCH
    public synchronized List<Product> search(String query) {
        String q = query.toLowerCase().trim();
        return store.values().stream()
                .filter(p -> p.getName().toLowerCase().contains(q)
                          || p.getCategory().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    // FILTER by category
    public synchronized List<Product> filterByCategory(String category) {
        if (category == null || category.isEmpty() || category.equals("All")) {
            return getAllProducts();
        }
        return store.values().stream()
                .filter(p -> p.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    // LOW STOCK
    public synchronized List<Product> getLowStockProducts() {
        return store.values().stream()
                .filter(Product::isLowStock)
                .collect(Collectors.toList());
    }

    // CATEGORIES
    public synchronized List<String> getCategories() {
        return store.values().stream()
                .map(Product::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    // STATS
    public synchronized int getTotalProducts() { return store.size(); }

    public synchronized int getTotalItems() {
        return store.values().stream().mapToInt(Product::getQuantity).sum();
    }

    public synchronized double getTotalInventoryValue() {
        return store.values().stream().mapToDouble(Product::getTotalValue).sum();
    }

    public synchronized int getLowStockCount() {
        return (int) store.values().stream().filter(Product::isLowStock).count();
    }
}
