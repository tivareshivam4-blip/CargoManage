package cargomanage.dao;

import cargomanage.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductDAO {

    public List<Product> findAll() {
        List<Product> list = new ArrayList<>();
        String sql = """
            SELECT p.*, COALESCE(SUM(s.quantity), 0) AS total_stock
            FROM products p
            LEFT JOIN stocks s ON p.id = s.product_id
            GROUP BY p.id
            ORDER BY p.id ASC
        """;
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Product p = mapRow(rs);
                p.setTotalStock(rs.getInt("total_stock"));
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Product> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return findAll();
        }
        List<Product> list = new ArrayList<>();
        String sql = """
            SELECT p.*, COALESCE(SUM(s.quantity), 0) AS total_stock
            FROM products p
            LEFT JOIN stocks s ON p.id = s.product_id
            WHERE p.name LIKE ? OR p.sku LIKE ? OR p.barcode LIKE ? OR p.category LIKE ? OR CAST(p.id AS CHAR) = ?
            GROUP BY p.id
            ORDER BY p.id ASC
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + query.trim() + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            ps.setString(5, query.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product p = mapRow(rs);
                    p.setTotalStock(rs.getInt("total_stock"));
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Product findById(int id) {
        String sql = """
            SELECT p.*, COALESCE(SUM(s.quantity), 0) AS total_stock
            FROM products p
            LEFT JOIN stocks s ON p.id = s.product_id
            WHERE p.id = ?
            GROUP BY p.id
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Product p = mapRow(rs);
                    p.setTotalStock(rs.getInt("total_stock"));
                    return p;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean create(Product p) throws SQLException {
        String sql = """
            INSERT INTO products (name, category, brand, description, sku, barcode, unit,
                                 purchase_price, selling_price, reorder_level, supplier_name,
                                 location, mfg_date, exp_date, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getName().trim());
            ps.setString(2, p.getCategory().trim());
            ps.setString(3, p.getBrand() != null ? p.getBrand().trim() : "Generic");
            ps.setString(4, p.getDescription() != null ? p.getDescription().trim() : "");
            ps.setString(5, p.getSku() != null && !p.getSku().isBlank() ? p.getSku().trim() : "CM-SKU-" + System.currentTimeMillis() % 100000);
            ps.setString(6, p.getBarcode() != null ? p.getBarcode().trim() : "");
            ps.setString(7, p.getUnit() != null ? p.getUnit().trim() : "KG");
            ps.setDouble(8, p.getPurchasePrice());
            ps.setDouble(9, p.getSellingPrice());
            ps.setInt(10, p.getReorderLevel());
            ps.setString(11, p.getSupplierName() != null ? p.getSupplierName().trim() : "");
            ps.setString(12, p.getLocation() != null ? p.getLocation().trim() : "Rack A-1");
            ps.setString(13, p.getMfgDate());
            ps.setString(14, p.getExpDate());
            ps.setString(15, p.getStatus() != null ? p.getStatus() : "Active");

            int aff = ps.executeUpdate();
            if (aff > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) p.setId(rs.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    public boolean update(Product p) throws SQLException {
        String sql = """
            UPDATE products SET name = ?, category = ?, brand = ?, description = ?,
                               sku = ?, barcode = ?, unit = ?, purchase_price = ?,
                               selling_price = ?, reorder_level = ?, supplier_name = ?,
                               location = ?, mfg_date = ?, exp_date = ?, status = ?
            WHERE id = ?
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName().trim());
            ps.setString(2, p.getCategory().trim());
            ps.setString(3, p.getBrand());
            ps.setString(4, p.getDescription());
            ps.setString(5, p.getSku());
            ps.setString(6, p.getBarcode());
            ps.setString(7, p.getUnit());
            ps.setDouble(8, p.getPurchasePrice());
            ps.setDouble(9, p.getSellingPrice());
            ps.setInt(10, p.getReorderLevel());
            ps.setString(11, p.getSupplierName());
            ps.setString(12, p.getLocation());
            ps.setString(13, p.getMfgDate());
            ps.setString(14, p.getExpDate());
            ps.setString(15, p.getStatus());
            ps.setInt(16, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM products";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getTotalInventoryValue() {
        String sql = """
            SELECT COALESCE(SUM(s.quantity * p.purchase_price), 0.0) AS total_val
            FROM stocks s
            JOIN products p ON s.product_id = p.id
        """;
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public List<Map<String, Object>> getBranchStockDistribution(int productId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = """
            SELECT b.id AS branch_id, b.name AS branch_name, b.location, COALESCE(s.quantity, 0) AS quantity
            FROM branches b
            LEFT JOIN stocks s ON b.id = s.branch_id AND s.product_id = ?
            ORDER BY b.id ASC
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("branchId", rs.getInt("branch_id"));
                    map.put("branchName", rs.getString("branch_name"));
                    map.put("location", rs.getString("location"));
                    map.put("quantity", rs.getInt("quantity"));
                    list.add(map);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setName(rs.getString("name"));
        p.setCategory(rs.getString("category"));
        p.setBrand(rs.getString("brand"));
        p.setDescription(rs.getString("description"));
        p.setSku(rs.getString("sku"));
        p.setBarcode(rs.getString("barcode"));
        p.setUnit(rs.getString("unit"));
        p.setPurchasePrice(rs.getDouble("purchase_price"));
        p.setSellingPrice(rs.getDouble("selling_price"));
        p.setReorderLevel(rs.getInt("reorder_level"));
        p.setSupplierName(rs.getString("supplier_name"));
        p.setLocation(rs.getString("location"));
        p.setMfgDate(rs.getString("mfg_date"));
        p.setExpDate(rs.getString("exp_date"));
        p.setStatus(rs.getString("status"));
        return p;
    }
}

