package qnha.fit.shopmate.dao;

import qnha.fit.shopmate.config.DBConnection;
import qnha.fit.shopmate.exception.DataAccessException;
import qnha.fit.shopmate.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public List<Product> findAll() {
        String sql = """
                SELECT p.*, c.name AS category_name
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.id
                ORDER BY p.id ASC
                """;
        List<Product> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi lấy danh sách sản phẩm: " + e.getMessage(), e);
        }
    }

    public List<Product> searchAndFilter(String keyword, Integer categoryId, String sortOption, boolean activeOnly) {
        StringBuilder sql = new StringBuilder("""
                SELECT p.*, c.name AS category_name
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.id
                WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();

        if (activeOnly) {
            sql.append(" AND p.status = 'ACTIVE' ");
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (p.name LIKE ? OR p.code LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
        }
        if (categoryId != null && categoryId > 0) {
            sql.append(" AND p.category_id = ? ");
            params.add(categoryId);
        }

        if ("PRICE_ASC".equalsIgnoreCase(sortOption)) {
            sql.append(" ORDER BY p.selling_price ASC");
        } else if ("PRICE_DESC".equalsIgnoreCase(sortOption)) {
            sql.append(" ORDER BY p.selling_price DESC");
        } else if ("STOCK_ASC".equalsIgnoreCase(sortOption)) {
            sql.append(" ORDER BY p.stock_quantity ASC");
        } else if ("STOCK_DESC".equalsIgnoreCase(sortOption)) {
            sql.append(" ORDER BY p.stock_quantity DESC");
        } else {
            sql.append(" ORDER BY p.id ASC");
        }

        List<Product> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi tìm kiếm/lọc sản phẩm: " + e.getMessage(), e);
        }
    }

    public Product findById(int id) {
        String sql = """
                SELECT p.*, c.name AS category_name
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.id
                WHERE p.id = ?
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi tìm sản phẩm theo ID: " + e.getMessage(), e);
        }
    }

    public Product findByCode(String code) {
        String sql = """
                SELECT p.*, c.name AS category_name
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.id
                WHERE p.code = ?
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi tìm sản phẩm theo mã: " + e.getMessage(), e);
        }
    }

    public void insert(Product p) {
        String sql = """
                INSERT INTO products (code, category_id, name, unit, cost_price, selling_price, stock_quantity, image_path, description, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getCode());
            ps.setInt(2, p.getCategoryId());
            ps.setString(3, p.getName());
            ps.setString(4, p.getUnit());
            ps.setBigDecimal(5, p.getCostPrice());
            ps.setBigDecimal(6, p.getSellingPrice());
            ps.setInt(7, p.getStockQuantity());
            ps.setString(8, p.getImagePath());
            ps.setString(9, p.getDescription());
            ps.setString(10, p.getStatus());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể thêm sản phẩm (Mã sản phẩm có thể đã trùng): " + e.getMessage(), e);
        }
    }

    public void update(Product p) {
        String sql = """
                UPDATE products
                SET code = ?, category_id = ?, name = ?, unit = ?, cost_price = ?, selling_price = ?,
                    stock_quantity = ?, image_path = ?, description = ?, status = ?
                WHERE id = ?
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getCode());
            ps.setInt(2, p.getCategoryId());
            ps.setString(3, p.getName());
            ps.setString(4, p.getUnit());
            ps.setBigDecimal(5, p.getCostPrice());
            ps.setBigDecimal(6, p.getSellingPrice());
            ps.setInt(7, p.getStockQuantity());
            ps.setString(8, p.getImagePath());
            ps.setString(9, p.getDescription());
            ps.setString(10, p.getStatus());
            ps.setInt(11, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Không thể cập nhật sản phẩm: " + e.getMessage(), e);
        }
    }

    public void updateStockAndCostWithConnection(Connection conn, int productId, int deltaQuantity, java.math.BigDecimal newCostPrice) throws SQLException {
        String sql = "UPDATE products SET stock_quantity = stock_quantity + ?, cost_price = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, deltaQuantity);
            ps.setBigDecimal(2, newCostPrice);
            ps.setInt(3, productId);
            ps.executeUpdate();
        }
    }

    public boolean deductStockWithConnection(Connection conn, int productId, int quantity) throws SQLException {
        String sql = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE id = ? AND stock_quantity >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            return ps.executeUpdate() > 0;
        }
    }

    public void restoreStockWithConnection(Connection conn, int productId, int quantity) throws SQLException {
        String sql = "UPDATE products SET stock_quantity = stock_quantity + ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.executeUpdate();
        }
    }

    public boolean hasTransactions(int productId) {
        String sql1 = "SELECT COUNT(*) FROM invoice_details WHERE product_id = ?";
        String sql2 = "SELECT COUNT(*) FROM stock_imports WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql1)) {
                ps.setInt(1, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return true;
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(sql2)) {
                ps.setInt(1, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return true;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi kiểm tra giao dịch sản phẩm: " + e.getMessage(), e);
        }
    }

    public void updateStatus(int productId, String status) {
        String sql = "UPDATE products SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi cập nhật trạng thái sản phẩm: " + e.getMessage(), e);
        }
    }

    public void deleteById(int productId) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Không thể xóa sản phẩm: " + e.getMessage(), e);
        }
    }

    public String generateNextCode() {
        String sql = "SELECT MAX(id) FROM products";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int next = 1;
            if (rs.next()) {
                next = rs.getInt(1) + 1;
            }
            return String.format("SP%03d", next);
        } catch (SQLException e) {
            return "SP" + (System.currentTimeMillis() % 1000);
        }
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setCode(rs.getString("code"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setCategoryName(rs.getString("category_name"));
        p.setName(rs.getString("name"));
        p.setUnit(rs.getString("unit"));
        p.setCostPrice(rs.getBigDecimal("cost_price"));
        p.setSellingPrice(rs.getBigDecimal("selling_price"));
        p.setStockQuantity(rs.getInt("stock_quantity"));
        p.setImagePath(rs.getString("image_path"));
        p.setDescription(rs.getString("description"));
        p.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            p.setCreatedAt(ts.toLocalDateTime());
        }
        return p;
    }
}
