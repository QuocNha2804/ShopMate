package qnha.fit.shopmate.dto;

import java.math.BigDecimal;

public class DashboardStatsDTO {
    private BigDecimal todayRevenue = BigDecimal.ZERO;
    private BigDecimal todayProfit = BigDecimal.ZERO;
    private int todayOrderCount;
    private int lowStockCount;
    private int newCustomerCount;

    public DashboardStatsDTO() {
    }

    public BigDecimal getTodayRevenue() {
        return todayRevenue;
    }

    public void setTodayRevenue(BigDecimal todayRevenue) {
        this.todayRevenue = todayRevenue != null ? todayRevenue : BigDecimal.ZERO;
    }

    public BigDecimal getTodayProfit() {
        return todayProfit;
    }

    public void setTodayProfit(BigDecimal todayProfit) {
        this.todayProfit = todayProfit != null ? todayProfit : BigDecimal.ZERO;
    }

    public int getTodayOrderCount() {
        return todayOrderCount;
    }

    public void setTodayOrderCount(int todayOrderCount) {
        this.todayOrderCount = todayOrderCount;
    }

    public int getLowStockCount() {
        return lowStockCount;
    }

    public void setLowStockCount(int lowStockCount) {
        this.lowStockCount = lowStockCount;
    }

    public int getNewCustomerCount() {
        return newCustomerCount;
    }

    public void setNewCustomerCount(int newCustomerCount) {
        this.newCustomerCount = newCustomerCount;
    }
}
