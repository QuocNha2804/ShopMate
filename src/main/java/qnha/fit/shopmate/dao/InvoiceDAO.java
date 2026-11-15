package qnha.fit.shopmate.dao;

import qnha.fit.shopmate.config.DBConnection;
import qnha.fit.shopmate.exception.DataAccessException;
import qnha.fit.shopmate.model.Invoice;
import qnha.fit.shopmate.model.InvoiceDetail;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDAO {

    private static final String BASE_SELECT = """
            SELECT i.*, c.name AS customer_name, c.phone AS customer_phone,
                   u.full_name AS employee_name, v.code AS voucher_code
            FROM invoices i
            LEFT JOIN customers c ON i.customer_id = c.id
            LEFT JOIN users u ON i.user_id = u.id
            LEFT JOIN vouchers v ON i.voucher_id = v.id
            """;

    public void insertInvoiceWithConnection(Connection conn, Invoice inv) throws SQLException {
        String sql = """
                INSERT INTO invoices (invoice_code, customer_id, user_id, voucher_id, subtotal, discount_amount,
                                      vat_amount, total_amount, cash_received, change_amount, payment_method, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, inv.getInvoiceCode());
            if (inv.getCustomerId() != null && inv.getCustomerId() > 0) {
                ps.setInt(2, inv.getCustomerId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setInt(3, inv.getUserId());
            if (inv.getVoucherId() != null && inv.getVoucherId() > 0) {
                ps.setInt(4, inv.getVoucherId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setBigDecimal(5, inv.getSubtotal());
            ps.setBigDecimal(6, inv.getDiscountAmount());
            ps.setBigDecimal(7, inv.getVatAmount());
            ps.setBigDecimal(8, inv.getTotalAmount());
            ps.setBigDecimal(9, inv.getCashReceived());
            ps.setBigDecimal(10, inv.getChangeAmount());
            ps.setString(11, inv.getPaymentMethod());
            ps.setString(12, inv.getStatus());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    inv.setId(rs.getInt(1));
                }
            }
        }
    }

    public void insertDetailWithConnection(Connection conn, InvoiceDetail detail) throws SQLException {
        String sql = """
                INSERT INTO invoice_details (invoice_id, product_id, quantity, cost_price, unit_price, line_total)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, detail.getInvoiceId());
            ps.setInt(2, detail.getProductId());
            ps.setInt(3, detail.getQuantity());
            ps.setBigDecimal(4, detail.getCostPrice());
            ps.setBigDecimal(5, detail.getUnitPrice());
            ps.setBigDecimal(6, detail.getLineTotal());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    detail.setId(rs.getInt(1));
                }
            }
        }
    }

    public Invoice findByIdWithDetails(int invoiceId) {
        String sql = BASE_SELECT + " WHERE i.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Invoice inv = mapRow(rs);
                    inv.setDetails(findDetailsByInvoiceId(conn, invoiceId));
                    return inv;
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi lấy thông tin chi tiết hóa đơn: " + e.getMessage(), e);
        }
    }

    public List<InvoiceDetail> findDetailsByInvoiceId(Connection conn, int invoiceId) throws SQLException {
        String sql = """
                SELECT d.*, p.code AS product_code, p.name AS product_name, p.unit
                FROM invoice_details d
                LEFT JOIN products p ON d.product_id = p.id
                WHERE d.invoice_id = ?
                ORDER BY d.id ASC
                """;
        List<InvoiceDetail> details = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InvoiceDetail d = new InvoiceDetail();
                    d.setId(rs.getInt("id"));
                    d.setInvoiceId(rs.getInt("invoice_id"));
                    d.setProductId(rs.getInt("product_id"));
                    d.setProductCode(rs.getString("product_code"));
                    d.setProductName(rs.getString("product_name"));
                    d.setUnit(rs.getString("unit"));
                    d.setQuantity(rs.getInt("quantity"));
                    d.setCostPrice(rs.getBigDecimal("cost_price"));
                    d.setUnitPrice(rs.getBigDecimal("unit_price"));
                    d.setLineTotal(rs.getBigDecimal("line_total"));
                    details.add(d);
                }
            }
        }
        return details;
    }

    public List<Invoice> findRecentInvoices(int limit) {
        String sql = BASE_SELECT + " ORDER BY i.created_at DESC, i.id DESC LIMIT ?";
        List<Invoice> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi lấy danh sách đơn hàng gần đây: " + e.getMessage(), e);
        }
    }

    public List<Invoice> findByCustomerId(int customerId) {
        String sql = BASE_SELECT + " WHERE i.customer_id = ? ORDER BY i.created_at DESC";
        List<Invoice> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi lấy lịch sử mua hàng của khách: " + e.getMessage(), e);
        }
    }

    public List<Invoice> searchInvoices(String keyword, LocalDate fromDate, LocalDate toDate, Integer userId, String status) {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (i.invoice_code LIKE ? OR c.name LIKE ? OR c.phone LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        if (fromDate != null) {
            sql.append(" AND DATE(i.created_at) >= ? ");
            params.add(Date.valueOf(fromDate));
        }
        if (toDate != null) {
            sql.append(" AND DATE(i.created_at) <= ? ");
            params.add(Date.valueOf(toDate));
        }
        if (userId != null && userId > 0) {
            sql.append(" AND i.user_id = ? ");
            params.add(userId);
        }
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND i.status = ? ");
            params.add(status);
        }

        sql.append(" ORDER BY i.created_at DESC, i.id DESC");

        List<Invoice> list = new ArrayList<>();
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
            throw new DataAccessException("Lỗi lọc danh sách hóa đơn: " + e.getMessage(), e);
        }
    }

    public void updateCancelStatusWithConnection(Connection conn, int invoiceId, String cancelReason) throws SQLException {
        String sql = "UPDATE invoices SET status = 'CANCELLED', cancel_reason = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cancelReason);
            ps.setInt(2, invoiceId);
            ps.executeUpdate();
        }
    }

    public String generateNextInvoiceCode() {
        String sql = "SELECT MAX(id) FROM invoices";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int next = 1;
            if (rs.next()) {
                next = rs.getInt(1) + 1;
            }
            return String.format("HD%05d", next);
        } catch (SQLException e) {
            return "HD" + (System.currentTimeMillis() % 100000);
        }
    }

    private Invoice mapRow(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice();
        inv.setId(rs.getInt("id"));
        inv.setInvoiceCode(rs.getString("invoice_code"));
        int cId = rs.getInt("customer_id");
        inv.setCustomerId(rs.wasNull() ? null : cId);
        inv.setCustomerName(rs.getString("customer_name"));
        inv.setCustomerPhone(rs.getString("customer_phone"));
        inv.setUserId(rs.getInt("user_id"));
        inv.setEmployeeName(rs.getString("employee_name"));
        int vId = rs.getInt("voucher_id");
        inv.setVoucherId(rs.wasNull() ? null : vId);
        inv.setVoucherCode(rs.getString("voucher_code"));
        inv.setSubtotal(rs.getBigDecimal("subtotal"));
        inv.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        inv.setVatAmount(rs.getBigDecimal("vat_amount"));
        inv.setTotalAmount(rs.getBigDecimal("total_amount"));
        inv.setCashReceived(rs.getBigDecimal("cash_received"));
        inv.setChangeAmount(rs.getBigDecimal("change_amount"));
        inv.setPaymentMethod(rs.getString("payment_method"));
        inv.setStatus(rs.getString("status"));
        inv.setCancelReason(rs.getString("cancel_reason"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            inv.setCreatedAt(ts.toLocalDateTime());
        }
        return inv;
    }
}
