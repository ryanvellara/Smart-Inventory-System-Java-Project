package com.inventory.model;

public class Product {
    private int id;
    private String name;
    private String category;
    private int quantity;
    private double price;
    private int lowStockThreshold;

    public Product() {}

    public Product(int id, String name, String category, int quantity, double price, int lowStockThreshold) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.price = price;
        this.lowStockThreshold = lowStockThreshold;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(int lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }

    public boolean isLowStock() {
        return quantity <= lowStockThreshold;
    }

    public double getTotalValue() {
        return price * quantity;
    }

    @Override
    public String toString() {
        return name + " (" + category + ") - Qty: " + quantity;
    }
}
