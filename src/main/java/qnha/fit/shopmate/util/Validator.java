package qnha.fit.shopmate.util;

import qnha.fit.shopmate.exception.BusinessException;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public final class Validator {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9,10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{2,30}$");

    private Validator() {
    }

    public static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(fieldName + " không được để trống!");
        }
    }

    public static void validateCode(String code, String fieldName) {
        requireNonBlank(code, fieldName);
        if (!CODE_PATTERN.matcher(code.trim()).matches()) {
            throw new BusinessException(fieldName + " chỉ được chứa chữ cái, chữ số, gạch ngang/dưới (2-30 ký tự)!");
        }
    }

    public static void validatePhone(String phone) {
        requireNonBlank(phone, "Số điện thoại");
        if (!PHONE_PATTERN.matcher(phone.trim()).matches()) {
            throw new BusinessException("Số điện thoại không hợp lệ (bắt đầu bằng số 0, gồm 10-11 chữ số)!");
        }
    }

    public static void validateEmailOptional(String email) {
        if (email != null && !email.trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
                throw new BusinessException("Địa chỉ Email không đúng định dạng!");
            }
        }
    }

    public static void validatePositivePrice(BigDecimal price, String fieldName) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(fieldName + " phải lớn hơn hoặc bằng 0!");
        }
    }

    public static void validateProductPrices(BigDecimal costPrice, BigDecimal sellingPrice) {
        validatePositivePrice(costPrice, "Giá nhập");
        if (sellingPrice == null || sellingPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Giá bán phải lớn hơn 0!");
        }
        if (sellingPrice.compareTo(costPrice) < 0) {
            throw new BusinessException("Cảnh báo: Giá bán không được thấp hơn giá nhập (" + FormatUtil.formatCurrency(costPrice) + ")!");
        }
    }
}
