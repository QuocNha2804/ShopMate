package qnha.fit.shopmate.dao;

import qnha.fit.shopmate.config.DBConnection;
import qnha.fit.shopmate.exception.DataAccessException;
import qnha.fit.shopmate.model.StockImport;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockImportDAO {

    public List<StockImport> findAll() {
        String sql = """
                SELECT si.*, u.full_name AS employee_name, p.code AS product_code, p.name AS product_name
                FROM stock_imports si
                LEFT JOIN users u ON si.user_id = u.id
                LEFT JOIN products p ON si.product_id = p.id
                ORDER BY si.created_at DESC, si.id DESC
                """;
        List<StockImport> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi lấy lịch sử nhập kho: " + e.getMessage(), e);
        }
    }

    public void insertWithConnection(Connection conn, StockImport si) throws SQLException {
        String sql = "INSERT INTO stock_imports (import_code, user_id, product_id, quantity, import_price, note) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, si.getImportCode());
            ps.setInt(2, si.getUserId());
            ps.setInt(3, si.getProductId());
            ps.setInt(4, si.getQuantity());
            ps.setBigDecimal(5, si.getImportPrice());
            ps.setString(6, si.getNote());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    si.setId(rs.getInt(1));
                }
            }
        }
    }

    public String generateNextCode() {
        String sql = "SELECT MAX(id) FROM stock_imports";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int next = 1;
            if (rs.next()) {
                next = rs.getInt(1) + 1;
            }
            return String.format("NK%05d", next);
        } catch (SQLException e) {
            return "NK" + (System.currentTimeMillis() % 100000);
        }
    }

    private StockImport mapRow(ResultSet rs) throws SQLException {
        StockImport si = new StockImport();
        si.setId(rs.getInt("id"));
        si.setImportCode(rs.getString("import_code"));
        si.setUserId(rs.getInt("user_id"));
        si.setEmployeeName(rs.getString("employee_name"));
        si.setProductId(rs.getInt("product_id"));
        si.setProductCode(rs.getString("product_code"));
        si.setProductName(rs.getString("product_name"));
        si.setQuantity(rs.getInt("quantity"));
        si.setImportPrice(rs.getBigDecimal("import_price"));
        si.setNote(rs.getString("note"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            si.setCreatedAt(ts.toLocalDateTime());
        }
        return si;
    }
}
