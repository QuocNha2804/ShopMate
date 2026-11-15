package qnha.fit.shopmate.dto;

import java.math.BigDecimal;

public class RevenueReportDTO {
    private String periodLabel; // e.g., "15/11/2026" or "11/2026" or "2026"
    private int orderCount;
    private BigDecimal revenue = BigDecimal.ZERO;
    private BigDecimal costOfGoods = BigDecimal.ZERO;
    private BigDecimal discountTotal = BigDecimal.ZERO;
    private BigDecimal profit = BigDecimal.ZERO;

    public RevenueReportDTO() {
    }

    public RevenueReportDTO(String periodLabel, int orderCount, BigDecimal revenue,
                            BigDecimal costOfGoods, BigDecimal discountTotal, BigDecimal profit) {
        this.periodLabel = periodLabel;
        this.orderCount = orderCount;
        this.revenue = revenue != null ? revenue : BigDecimal.ZERO;
        this.costOfGoods = costOfGoods != null ? costOfGoods : BigDecimal.ZERO;
        this.discountTotal = discountTotal != null ? discountTotal : BigDecimal.ZERO;
        this.profit = profit != null ? profit : BigDecimal.ZERO;
    }

    public String getPeriodLabel() {
        return periodLabel;
    }

    public void setPeriodLabel(String periodLabel) {
        this.periodLabel = periodLabel;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public BigDecimal getCostOfGoods() {
        return costOfGoods;
    }

    public void setCostOfGoods(BigDecimal costOfGoods) {
        this.costOfGoods = costOfGoods;
    }

    public BigDecimal getDiscountTotal() {
        return discountTotal;
    }

    public void setDiscountTotal(BigDecimal discountTotal) {
        this.discountTotal = discountTotal;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }
}
