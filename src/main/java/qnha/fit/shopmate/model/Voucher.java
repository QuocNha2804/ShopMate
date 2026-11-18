package qnha.fit.shopmate.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class Voucher {
    private int id;
    private String code;
    private String discountType = "PERCENT"; // PERCENT or FIXED
    private BigDecimal discountValue = BigDecimal.ZERO;
    private BigDecimal minOrderAmount = BigDecimal.ZERO;
    private BigDecimal maxDiscount = BigDecimal.ZERO;
    private int usageLimit = 100;
    private int usedCount = 0;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active = true;

    public Voucher() {
    }

    public boolean isValidForOrder(BigDecimal subtotal) {
        if (!active) return false;
        if (usedCount >= usageLimit) return false;
        LocalDate today = LocalDate.now();
        if (startDate != null && today.isBefore(startDate)) return false;
        if (endDate != null && today.isAfter(endDate)) return false;
        return subtotal.compareTo(minOrderAmount) >= 0;
    }

    public BigDecimal calculateDiscount(BigDecimal subtotal) {
        if (!isValidForOrder(subtotal)) {
            return BigDecimal.ZERO;
        }
        BigDecimal discount;
        if ("PERCENT".equalsIgnoreCase(discountType)) {
            discount = subtotal.multiply(discountValue)
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
            if (maxDiscount != null && maxDiscount.compareTo(BigDecimal.ZERO) > 0
                    && discount.compareTo(maxDiscount) > 0) {
                discount = maxDiscount;
            }
        } else {
            discount = discountValue;
        }
        if (discount.compareTo(subtotal) > 0) {
            discount = subtotal;
        }
        return discount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public BigDecimal getMinOrderAmount() {
        return minOrderAmount;
    }

    public void setMinOrderAmount(BigDecimal minOrderAmount) {
        this.minOrderAmount = minOrderAmount;
    }

    public BigDecimal getMaxDiscount() {
        return maxDiscount;
    }

    public void setMaxDiscount(BigDecimal maxDiscount) {
        this.maxDiscount = maxDiscount;
    }

    public int getUsageLimit() {
        return usageLimit;
    }

    public void setUsageLimit(int usageLimit) {
        this.usageLimit = usageLimit;
    }

    public int getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(int usedCount) {
        this.usedCount = usedCount;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
