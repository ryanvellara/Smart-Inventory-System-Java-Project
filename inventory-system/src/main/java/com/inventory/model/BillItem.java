package com.inventory.model;

/**
 * Represents a single line item in a bill.
 * e.g. "Laptop Pro 15 x 2 @ ₹75,000 = ₹1,50,000"
 */
public class BillItem {

    private final Product product;
    private final int quantity;
    private final double priceAtSale; // snapshot price at time of billing

    public BillItem(Product product, int quantity) {
        this.product     = product;
        this.quantity    = quantity;
        this.priceAtSale = product.getPrice();
    }

    public Product getProduct()      { return product; }
    public int getQuantity()         { return quantity; }
    public double getPriceAtSale()   { return priceAtSale; }
    public double getSubtotal()      { return priceAtSale * quantity; }

    @Override
    public String toString() {
        return product.getName() + " x" + quantity + " @ ₹" + priceAtSale;
    }
}
