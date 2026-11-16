package qnha.fit.shopmate.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class FormatUtil {
    private static final DecimalFormat CURRENCY_FORMAT;
    private static final DecimalFormat NUMBER_FORMAT;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.of("vi", "VN"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        CURRENCY_FORMAT = new DecimalFormat("#,##0 ₫", symbols);
        NUMBER_FORMAT = new DecimalFormat("#,##0", symbols);
    }

    private FormatUtil() {
    }

    public static String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0 ₫";
        }
        return CURRENCY_FORMAT.format(amount);
    }

    public static String formatCurrency(double amount) {
        return CURRENCY_FORMAT.format(amount);
    }

    public static String formatNumber(BigDecimal amount) {
        if (amount == null) {
            return "0";
        }
        return NUMBER_FORMAT.format(amount);
    }

    public static BigDecimal parseMoney(String text) {
        if (text == null || text.isBlank()) {
            return BigDecimal.ZERO;
        }
        String cleaned = text.replaceAll("[^0-9-]", "");
        if (cleaned.isEmpty() || "-".equals(cleaned)) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(cleaned);
    }

    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMAT) : "";
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMAT) : "";
    }
}
