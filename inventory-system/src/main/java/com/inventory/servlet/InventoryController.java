package com.inventory.servlet;

import com.inventory.dao.ProductDAO;
import com.inventory.model.Product;
import java.util.List;
import java.util.Optional;

/**
 * InventoryController — follows the Servlet request-handling pattern:
 *   doGet()  → read/query operations
 *   doPost() → create/update/delete operations
 *
 * In a real Servlet app this would extend HttpServlet; here the same
 * design is applied in-process for the Swing client.
 */
public class InventoryController {

    private final ProductDAO dao = ProductDAO.getInstance();

    // ── GET operations ───────────────────────────────────────────────────────

    public List<Product> doGetAll() {
        return dao.getAllProducts();
    }

    public Optional<Product> doGetById(int id) {
        return dao.getById(id);
    }

    public List<Product> doSearch(String query) {
        if (query == null || query.isBlank()) return dao.getAllProducts();
        return dao.search(query);
    }

    public List<Product> doFilterByCategory(String category) {
        return dao.filterByCategory(category);
    }

    public List<Product> doGetLowStock() {
        return dao.getLowStockProducts();
    }

    public List<String> doGetCategories() {
        return dao.getCategories();
    }

    // Summary stats (used by dashboard panel)
    public int getTotalProducts()       { return dao.getTotalProducts(); }
    public int getTotalItems()          { return dao.getTotalItems(); }
    public double getTotalValue()       { return dao.getTotalInventoryValue(); }
    public int getLowStockCount()       { return dao.getLowStockCount(); }

    // ── POST operations ──────────────────────────────────────────────────────

    /** doPost / action=create */
    public Product doCreate(String name, String category,
                             int quantity, double price, int threshold) {
        validate(name, category, quantity, price, threshold);
        Product p = new Product(0, name, category, quantity, price, threshold);
        return dao.addProduct(p);
    }

    /** doPost / action=update */
    public boolean doUpdate(int id, String name, String category,
                             int quantity, double price, int threshold) {
        validate(name, category, quantity, price, threshold);
        Product p = new Product(id, name, category, quantity, price, threshold);
        return dao.updateProduct(p);
    }

    /** doPost / action=delete */
    public boolean doDelete(int id) {
        if (id <= 0) throw new IllegalArgumentException("Invalid product ID.");
        return dao.deleteProduct(id);
    }

    /** doPost / action=restock */
    public boolean doRestock(int id, int addQty) {
        if (addQty <= 0) throw new IllegalArgumentException("Restock quantity must be > 0.");
        Optional<Product> opt = dao.getById(id);
        if (opt.isEmpty()) return false;
        Product p = opt.get();
        p.setQuantity(p.getQuantity() + addQty);
        return dao.updateProduct(p);
    }

    /** doPost / action=reduceStock — called by BillController after sale */
    public boolean doReduceStock(int id, int reduceQty) {
        if (reduceQty <= 0) throw new IllegalArgumentException("Reduce quantity must be > 0.");
        Optional<Product> opt = dao.getById(id);
        if (opt.isEmpty()) return false;
        Product p = opt.get();
        if (p.getQuantity() < reduceQty)
            throw new IllegalArgumentException(
                "Insufficient stock for \"" + p.getName() + "\".");
        p.setQuantity(p.getQuantity() - reduceQty);
        return dao.updateProduct(p);
    }

    // ── Validation ───────────────────────────────────────────────────────────

    private void validate(String name, String category,
                           int quantity, double price, int threshold) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Product name cannot be empty.");
        if (category == null || category.isBlank())
            throw new IllegalArgumentException("Category cannot be empty.");
        if (quantity < 0)
            throw new IllegalArgumentException("Quantity cannot be negative.");
        if (price < 0)
            throw new IllegalArgumentException("Price cannot be negative.");
        if (threshold < 0)
            throw new IllegalArgumentException("Low-stock threshold cannot be negative.");
    }
}
