package qnha.fit.shopmate.dao;

import qnha.fit.shopmate.config.DBConnection;
import qnha.fit.shopmate.dto.*;
import qnha.fit.shopmate.exception.DataAccessException;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportDAO {

    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO dto = new DashboardStatsDTO();
        String sqlTodayRev = """
                SELECT COUNT(id) AS order_count,
                       COALESCE(SUM(total_amount), 0) AS revenue
                FROM invoices
                WHERE status = 'PAID' AND DATE(created_at) = CURDATE()
                """;
        String sqlTodayCost = """
                SELECT COALESCE(SUM(d.quantity * d.cost_price), 0) AS total_cost
                FROM invoice_details d
                JOIN invoices i ON d.invoice_id = i.id
                WHERE i.status = 'PAID' AND DATE(i.created_at) = CURDATE()
                """;
        String sqlLowStock = "SELECT COUNT(*) FROM products WHERE status = 'ACTIVE' AND stock_quantity < 10";
        String sqlNewCust = "SELECT COUNT(*) FROM customers WHERE DATE(created_at) >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)";

        try (Connection conn = DBConnection.getConnection()) {
            BigDecimal rev = BigDecimal.ZERO;
            BigDecimal cost = BigDecimal.ZERO;

            try (PreparedStatement ps = conn.prepareStatement(sqlTodayRev);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dto.setTodayOrderCount(rs.getInt("order_count"));
                    rev = rs.getBigDecimal("revenue");
                    dto.setTodayRevenue(rev);
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlTodayCost);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    cost = rs.getBigDecimal("total_cost");
                }
            }
            dto.setTodayProfit(rev.subtract(cost));

            try (PreparedStatement ps = conn.prepareStatement(sqlLowStock);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dto.setLowStockCount(rs.getInt(1));
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlNewCust);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dto.setNewCustomerCount(rs.getInt(1));
                }
            }
            return dto;
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi thống kê Dashboard: " + e.getMessage(), e);
        }
    }

    public Map<String, BigDecimal> getLast7DaysRevenue() {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            result.put(today.minusDays(i).format(fmt), BigDecimal.ZERO);
        }

        String sql = """
                SELECT DATE(created_at) AS order_date, COALESCE(SUM(total_amount), 0) AS daily_rev
                FROM invoices
                WHERE status = 'PAID' AND DATE(created_at) >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
                GROUP BY DATE(created_at)
                ORDER BY order_date ASC
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Date d = rs.getDate("order_date");
                if (d != null) {
                    String key = d.toLocalDate().format(fmt);
                    result.put(key, rs.getBigDecimal("daily_rev"));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi lấy biểu đồ doanh thu 7 ngày: " + e.getMessage(), e);
        }
    }

    public List<RevenueReportDTO> getRevenueReport(String groupByMode, LocalDate fromDate, LocalDate toDate) {
        String periodSelect;
        if ("MONTH".equalsIgnoreCase(groupByMode)) {
            periodSelect = "DATE_FORMAT(i.created_at, '%m/%Y')";
        } else if ("YEAR".equalsIgnoreCase(groupByMode)) {
            periodSelect = "DATE_FORMAT(i.created_at, '%Y')";
        } else {
            periodSelect = "DATE_FORMAT(i.created_at, '%d/%m/%Y')";
        }

        String sql = String.format("""
                SELECT %s AS period_label,
                       COUNT(DISTINCT i.id) AS order_count,
                       COALESCE(SUM(i.total_amount), 0) AS revenue,
                       COALESCE(SUM(i.discount_amount), 0) AS discount_total,
                       COALESCE((
                           SELECT SUM(d.quantity * d.cost_price)
                           FROM invoice_details d
                           JOIN invoices i2 ON d.invoice_id = i2.id
                           WHERE i2.status = 'PAID'
                             AND %s = %s
                             AND DATE(i2.created_at) BETWEEN ? AND ?
                       ), 0) AS cost_of_goods
                FROM invoices i
                WHERE i.status = 'PAID'
                  AND DATE(i.created_at) BETWEEN ? AND ?
                GROUP BY period_label
                ORDER BY MIN(i.created_at) ASC
                """,
                periodSelect,
                periodSelect.replace("i.", "i2."),
                periodSelect
        );

        List<RevenueReportDTO> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            Date start = Date.valueOf(fromDate);
            Date end = Date.valueOf(toDate);
            ps.setDate(1, start);
            ps.setDate(2, end);
            ps.setDate(3, start);
            ps.setDate(4, end);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String label = rs.getString("period_label");
                    int count = rs.getInt("order_count");
                    BigDecimal rev = rs.getBigDecimal("revenue");
                    BigDecimal disc = rs.getBigDecimal("discount_total");
                    BigDecimal cost = rs.getBigDecimal("cost_of_goods");
                    BigDecimal profit = rev.subtract(cost);
                    list.add(new RevenueReportDTO(label, count, rev, cost, disc, profit));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi truy vấn báo cáo doanh thu: " + e.getMessage(), e);
        }
    }

    public List<TopProductDTO> getTopSellingProducts(int limit, LocalDate fromDate, LocalDate toDate) {
        StringBuilder sql = new StringBuilder("""
                SELECT p.id, p.code, p.name, c.name AS category_name,
                       COALESCE(SUM(d.quantity), 0) AS total_qty,
                       COALESCE(SUM(d.line_total), 0) AS total_rev,
                       COALESCE(SUM(d.line_total - (d.quantity * d.cost_price)), 0) AS total_profit
                FROM invoice_details d
                JOIN invoices i ON d.invoice_id = i.id
                JOIN products p ON d.product_id = p.id
                LEFT JOIN categories c ON p.category_id = c.id
                WHERE i.status = 'PAID'
                """);
        if (fromDate != null && toDate != null) {
            sql.append(" AND DATE(i.created_at) BETWEEN ? AND ? ");
        }
        sql.append("""
                GROUP BY p.id, p.code, p.name, c.name
                ORDER BY total_qty DESC, total_rev DESC
                LIMIT ?
                """);

        List<TopProductDTO> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (fromDate != null && toDate != null) {
                ps.setDate(idx++, Date.valueOf(fromDate));
                ps.setDate(idx++, Date.valueOf(toDate));
            }
            ps.setInt(idx, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TopProductDTO dto = new TopProductDTO();
                    dto.setProductId(rs.getInt("id"));
                    dto.setProductCode(rs.getString("code"));
                    dto.setProductName(rs.getString("name"));
                    dto.setCategoryName(rs.getString("category_name"));
                    dto.setTotalQuantitySold(rs.getInt("total_qty"));
                    dto.setTotalRevenue(rs.getBigDecimal("total_rev"));
                    dto.setTotalProfit(rs.getBigDecimal("total_profit"));
                    list.add(dto);
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi lấy top sản phẩm bán chạy: " + e.getMessage(), e);
        }
    }

    public List<TopCustomerDTO> getTopCustomers(int limit) {
        String sql = """
                SELECT c.id, c.name, c.phone, c.points,
                       COUNT(i.id) AS order_count,
                       COALESCE(SUM(i.total_amount), 0) AS total_spent
                FROM customers c
                LEFT JOIN invoices i ON c.id = i.customer_id AND i.status = 'PAID'
                GROUP BY c.id, c.name, c.phone, c.points
                ORDER BY total_spent DESC, c.points DESC
                LIMIT ?
                """;
        List<TopCustomerDTO> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TopCustomerDTO dto = new TopCustomerDTO();
                    dto.setCustomerId(rs.getInt("id"));
                    dto.setCustomerName(rs.getString("name"));
                    dto.setPhone(rs.getString("phone"));
                    dto.setPoints(rs.getInt("points"));
                    dto.setOrderCount(rs.getInt("order_count"));
                    dto.setTotalSpent(rs.getBigDecimal("total_spent"));
                    list.add(dto);
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi lấy top khách hàng thân thiết: " + e.getMessage(), e);
        }
    }

    public List<EmployeeRevenueDTO> getEmployeeRevenueReport(LocalDate fromDate, LocalDate toDate) {
        StringBuilder sql = new StringBuilder("""
                SELECT u.id, u.code, u.full_name, u.role,
                       COUNT(i.id) AS order_count,
                       COALESCE(SUM(i.total_amount), 0) AS total_revenue
                FROM users u
                LEFT JOIN invoices i ON u.id = i.user_id AND i.status = 'PAID'
                """);
        if (fromDate != null && toDate != null) {
            sql.append(" AND DATE(i.created_at) BETWEEN ? AND ? ");
        }
        sql.append("""
                GROUP BY u.id, u.code, u.full_name, u.role
                ORDER BY total_revenue DESC
                """);

        List<EmployeeRevenueDTO> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (fromDate != null && toDate != null) {
                ps.setDate(1, Date.valueOf(fromDate));
                ps.setDate(2, Date.valueOf(toDate));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EmployeeRevenueDTO dto = new EmployeeRevenueDTO();
                    dto.setUserId(rs.getInt("id"));
                    dto.setEmployeeCode(rs.getString("code"));
                    dto.setFullName(rs.getString("full_name"));
                    dto.setRole(rs.getString("role"));
                    dto.setOrderCount(rs.getInt("order_count"));
                    dto.setTotalRevenue(rs.getBigDecimal("total_revenue"));
                    list.add(dto);
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi lấy doanh thu theo nhân viên: " + e.getMessage(), e);
        }
    }
}
