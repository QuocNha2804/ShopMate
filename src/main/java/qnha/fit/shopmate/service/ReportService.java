package qnha.fit.shopmate.service;

import qnha.fit.shopmate.dao.ReportDAO;
import qnha.fit.shopmate.dto.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ReportService {
    private final ReportDAO reportDAO = new ReportDAO();

    public DashboardStatsDTO getDashboardStats() {
        return reportDAO.getDashboardStats();
    }

    public Map<String, BigDecimal> getLast7DaysRevenue() {
        return reportDAO.getLast7DaysRevenue();
    }

    public List<RevenueReportDTO> getRevenueReport(String groupByMode, LocalDate fromDate, LocalDate toDate) {
        return reportDAO.getRevenueReport(groupByMode, fromDate, toDate);
    }

    public List<TopProductDTO> getTopSellingProducts(int limit, LocalDate fromDate, LocalDate toDate) {
        return reportDAO.getTopSellingProducts(limit, fromDate, toDate);
    }

    public List<TopCustomerDTO> getTopCustomers(int limit) {
        return reportDAO.getTopCustomers(limit);
    }

    public List<EmployeeRevenueDTO> getEmployeeRevenueReport(LocalDate fromDate, LocalDate toDate) {
        return reportDAO.getEmployeeRevenueReport(fromDate, toDate);
    }
}
