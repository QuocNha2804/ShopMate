package qnha.fit.shopmate.dao;

import qnha.fit.shopmate.config.DBConnection;
import qnha.fit.shopmate.exception.DataAccessException;
import qnha.fit.shopmate.model.Voucher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VoucherDAO {

    public List<Voucher> findAll() {
        String sql = "SELECT * FROM vouchers ORDER BY id DESC";
        List<Voucher> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi lấy danh sách mã giảm giá: " + e.getMessage(), e);
        }
    }

    public Voucher findByCode(String code) {
        String sql = "SELECT * FROM vouchers WHERE UPPER(code) = UPPER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi tìm mã giảm giá: " + e.getMessage(), e);
        }
    }

    public void insert(Voucher v) {
        String sql = """
                INSERT INTO vouchers (code, discount_type, discount_value, min_order_amount, max_discount, usage_limit, used_count, start_date, end_date, is_active)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, v.getCode().toUpperCase().trim());
            ps.setString(2, v.getDiscountType());
            ps.setBigDecimal(3, v.getDiscountValue());
            ps.setBigDecimal(4, v.getMinOrderAmount());
            ps.setBigDecimal(5, v.getMaxDiscount());
            ps.setInt(6, v.getUsageLimit());
            ps.setInt(7, v.getUsedCount());
            ps.setDate(8, Date.valueOf(v.getStartDate()));
            ps.setDate(9, Date.valueOf(v.getEndDate()));
            ps.setInt(10, v.isActive() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    v.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không thể thêm mã giảm giá (Mã có thể đã tồn tại): " + e.getMessage(), e);
        }
    }

    public void update(Voucher v) {
        String sql = """
                UPDATE vouchers
                SET code = ?, discount_type = ?, discount_value = ?, min_order_amount = ?, max_discount = ?,
                    usage_limit = ?, start_date = ?, end_date = ?, is_active = ?
                WHERE id = ?
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, v.getCode().toUpperCase().trim());
            ps.setString(2, v.getDiscountType());
            ps.setBigDecimal(3, v.getDiscountValue());
            ps.setBigDecimal(4, v.getMinOrderAmount());
            ps.setBigDecimal(5, v.getMaxDiscount());
            ps.setInt(6, v.getUsageLimit());
            ps.setDate(7, Date.valueOf(v.getStartDate()));
            ps.setDate(8, Date.valueOf(v.getEndDate()));
            ps.setInt(9, v.isActive() ? 1 : 0);
            ps.setInt(10, v.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Không thể cập nhật mã giảm giá: " + e.getMessage(), e);
        }
    }

    public void incrementUsedCountWithConnection(Connection conn, int voucherId) throws SQLException {
        String sql = "UPDATE vouchers SET used_count = used_count + 1 WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, voucherId);
            ps.executeUpdate();
        }
    }

    public void decrementUsedCountWithConnection(Connection conn, int voucherId) throws SQLException {
        String sql = "UPDATE vouchers SET used_count = GREATEST(0, used_count - 1) WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, voucherId);
            ps.executeUpdate();
        }
    }

    public void deleteById(int id) {
        String sql = "DELETE FROM vouchers WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Không thể xóa mã giảm giá: " + e.getMessage(), e);
        }
    }

    private Voucher mapRow(ResultSet rs) throws SQLException {
        Voucher v = new Voucher();
        v.setId(rs.getInt("id"));
        v.setCode(rs.getString("code"));
        v.setDiscountType(rs.getString("discount_type"));
        v.setDiscountValue(rs.getBigDecimal("discount_value"));
        v.setMinOrderAmount(rs.getBigDecimal("min_order_amount"));
        v.setMaxDiscount(rs.getBigDecimal("max_discount"));
        v.setUsageLimit(rs.getInt("usage_limit"));
        v.setUsedCount(rs.getInt("used_count"));
        Date sDate = rs.getDate("start_date");
        if (sDate != null) v.setStartDate(sDate.toLocalDate());
        Date eDate = rs.getDate("end_date");
        if (eDate != null) v.setEndDate(eDate.toLocalDate());
        v.setActive(rs.getInt("is_active") == 1);
        return v;
    }
}
