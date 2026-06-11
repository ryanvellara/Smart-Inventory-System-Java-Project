# Smart Inventory System
### Java Swing · AWT · Servlet-Pattern Architecture

A clean, fully self-contained desktop inventory management application built with
**Java Swing** (UI), **AWT** (layout/graphics), and a **Servlet-style MVC pattern**
(Controller → DAO → Model). No external libraries or databases required.

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                   │
│  (Java Swing / AWT)                                     │
│                                                         │
│  MainFrame.java          ← JFrame application shell     │
│  DashboardPanel.java     ← KPI summary cards            │
│  ProductTablePanel.java  ← Search, filter, CRUD table   │
│  ProductFormDialog.java  ← Add/Edit modal dialog        │
└────────────────────────┬────────────────────────────────┘
                         │  calls
┌────────────────────────▼────────────────────────────────┐
│                  Controller Layer                       │
│  (Servlet Pattern — mirrors HttpServlet design)         │
│                                                         │
│  InventoryController.java                               │
│    doGet*()  → read / query operations                  │
│    doPost*() → create / update / delete operations      │
│    validate()→ input validation                         │
└────────────────────────┬────────────────────────────────┘
                         │  delegates to
┌────────────────────────▼────────────────────────────────┐
│                     DAO Layer                           │
│  (Repository / Data Access Object)                      │
│                                                         │
│  ProductDAO.java   ← Singleton; in-memory LinkedHashMap │
│    addProduct()    doGet → getAllProducts()              │
│    updateProduct() doPost → CRUD                        │
│    deleteProduct() search(), filterByCategory()         │
│    getLowStockProducts() / getCategories()              │
└────────────────────────┬────────────────────────────────┘
                         │  maps to
┌────────────────────────▼────────────────────────────────┐
│                    Model Layer                          │
│  Product.java  { id, name, category, qty, price,       │
│                  lowStockThreshold }                    │
│  + isLowStock() + getTotalValue()                       │
└─────────────────────────────────────────────────────────┘
```

---

## Features

| Feature | Description |
|---|---|
| 📊 Dashboard | 4 live KPI cards: Products, Total Items, Inventory Value, Low Stock count |
| ➕ Add Product | Modal form with validation; supports new category creation |
| ✏️ Edit Product | Pre-filled form; double-click row shortcut |
| 🗑 Delete Product | Confirmation dialog before removal |
| 📦 Restock | Quick-add stock dialog from the toolbar |
| 🔍 Search | Real-time search by name or category |
| 🔽 Filter | Dropdown filter by category |
| ⚠ Low Stock Alert | One-click view of below-threshold items; rows highlighted red |
| 🔁 Refresh | Resets all filters and reloads data |

---

## Project Structure

```
inventory-system/
├── run.sh                          ← Build + run script
├── README.md
└── src/main/java/com/inventory/
    ├── model/
    │   └── Product.java            ← Domain model (POJO)
    ├── dao/
    │   └── ProductDAO.java         ← Data access (Singleton, in-memory)
    ├── servlet/
    │   └── InventoryController.java← Servlet-pattern controller
    └── ui/
        ├── MainFrame.java          ← Entry point (main method here)
        ├── DashboardPanel.java     ← KPI cards panel
        ├── ProductTablePanel.java  ← Table + toolbar
        └── ProductFormDialog.java  ← Add/Edit modal
```

---

## Requirements

- **Java 11+** (Java 17 or 21 recommended)
- No third-party dependencies

---

## Build & Run

### Option 1 — Shell script (Linux/macOS)
```bash
chmod +x run.sh
./run.sh
```

### Option 2 — Manual compile & run
```bash
# Compile
mkdir -p out
find src -name "*.java" > sources.txt
javac -d out @sources.txt

# Run
java -cp out com.inventory.ui.MainFrame
```

### Option 3 — IDE (IntelliJ IDEA / Eclipse / NetBeans)
1. Open the project root as a Java project
2. Mark `src/main/java` as the sources root
3. Run `com.inventory.ui.MainFrame`

### Option 4 — Create an executable JAR
```bash
# After compiling to out/
jar cfe InventorySystem.jar com.inventory.ui.MainFrame -C out .
java -jar InventorySystem.jar
```

---

## How It Maps to Servlet Concepts

| Servlet Concept | This Project |
|---|---|
| `HttpServlet.doGet()` | `InventoryController.doGetAll()`, `doSearch()`, `doFilter()` |
| `HttpServlet.doPost()` | `InventoryController.doCreate()`, `doUpdate()`, `doDelete()` |
| Request validation | `InventoryController.validate()` |
| `HttpSession` / state | `ProductDAO` Singleton (in-memory store) |
| JSP / View | Swing panels (`DashboardPanel`, `ProductTablePanel`) |
| DAO pattern | `ProductDAO` (would connect to JDBC in a real web app) |

---

## Extending with a Real Database

Replace the in-memory `LinkedHashMap` in `ProductDAO` with JDBC calls:

```java
// In ProductDAO — swap the store with a DB connection
public List<Product> getAllProducts() {
    Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
    PreparedStatement ps = conn.prepareStatement("SELECT * FROM products");
    ResultSet rs = ps.executeQuery();
    // map ResultSet → Product objects
}
```

The controller and UI layers require **no changes** — the DAO interface stays the same.
