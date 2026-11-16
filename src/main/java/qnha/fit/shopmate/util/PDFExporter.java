package qnha.fit.shopmate.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import qnha.fit.shopmate.config.AppConfig;
import qnha.fit.shopmate.dto.EmployeeRevenueDTO;
import qnha.fit.shopmate.dto.RevenueReportDTO;
import qnha.fit.shopmate.dto.TopCustomerDTO;
import qnha.fit.shopmate.dto.TopProductDTO;
import qnha.fit.shopmate.model.Invoice;
import qnha.fit.shopmate.model.InvoiceDetail;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class PDFExporter {

    private PDFExporter() {
    }

    private static BaseFont loadVietnameseBaseFont() {
        String[] candidatePaths = {
                "C:/Windows/Fonts/arial.ttf",
                "C:/Windows/Fonts/tahoma.ttf",
                "C:/Windows/Fonts/segoeui.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
        };
        for (String path : candidatePaths) {
            try {
                File f = new File(path);
                if (f.exists()) {
                    return BaseFont.createFont(f.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                }
            } catch (Exception ignored) {
            }
        }
        try {
            return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
        } catch (Exception e) {
            throw new RuntimeException("Không thể khởi tạo font PDF", e);
        }
    }

    public static void exportInvoiceToPdf(Invoice invoice, File outputFile) throws Exception {
        Rectangle receiptSize = new Rectangle(280, 680);
        Document document = new Document(receiptSize, 14, 14, 16, 16);
        PdfWriter.getInstance(document, new FileOutputStream(outputFile));
        document.open();

        BaseFont bf = loadVietnameseBaseFont();
        Font fontStoreName = new Font(bf, 13, Font.BOLD, BaseColor.BLACK);
        Font fontTitle = new Font(bf, 12, Font.BOLD, BaseColor.BLACK);
        Font fontBold = new Font(bf, 8.5f, Font.BOLD, BaseColor.BLACK);
        Font fontNormal = new Font(bf, 8.5f, Font.NORMAL, BaseColor.BLACK);
        Font fontSmall = new Font(bf, 7.5f, Font.ITALIC, BaseColor.DARK_GRAY);

        // Header cửa hàng
        Paragraph storeName = new Paragraph(AppConfig.getStoreName().toUpperCase(), fontStoreName);
        storeName.setAlignment(Element.ALIGN_CENTER);
        document.add(storeName);

        Paragraph storeAddr = new Paragraph("Đ/c: " + AppConfig.getStoreAddress(), fontNormal);
        storeAddr.setAlignment(Element.ALIGN_CENTER);
        document.add(storeAddr);

        Paragraph storePhone = new Paragraph("SĐT: " + AppConfig.getStorePhone(), fontNormal);
        storePhone.setAlignment(Element.ALIGN_CENTER);
        storePhone.setSpacingAfter(6f);
        document.add(storePhone);

        document.add(new LineSeparator(0.5f, 100, BaseColor.GRAY, Element.ALIGN_CENTER, -2));

        Paragraph invoiceTitle = new Paragraph("HÓA ĐƠN BÁN HÀNG", fontTitle);
        invoiceTitle.setAlignment(Element.ALIGN_CENTER);
        invoiceTitle.setSpacingBefore(6f);
        invoiceTitle.setSpacingAfter(6f);
        document.add(invoiceTitle);

        // Thông tin chung
        document.add(new Paragraph("Số HĐ: " + invoice.getInvoiceCode(), fontBold));
        document.add(new Paragraph("Ngày: " + FormatUtil.formatDateTime(invoice.getCreatedAt()), fontNormal));
        document.add(new Paragraph("Thu ngân: " + (invoice.getEmployeeName() != null ? invoice.getEmployeeName() : "NV #" + invoice.getUserId()), fontNormal));
        document.add(new Paragraph("Khách hàng: " + invoice.getCustomerName()
                + (invoice.getCustomerPhone() != null && !invoice.getCustomerPhone().isBlank() ? " - " + invoice.getCustomerPhone() : ""), fontNormal));

        Paragraph spacer = new Paragraph(" ");
        spacer.setLeading(6f);
        document.add(spacer);

        // Bảng chi tiết sản phẩm
        PdfPTable table = new PdfPTable(new float[]{4.2f, 1.2f, 2.2f, 2.4f});
        table.setWidthPercentage(100);
        table.setSpacingBefore(4f);
        table.setSpacingAfter(6f);

        addCell(table, "Mặt hàng", fontBold, Element.ALIGN_LEFT, true);
        addCell(table, "SL", fontBold, Element.ALIGN_CENTER, true);
        addCell(table, "Đơn giá", fontBold, Element.ALIGN_RIGHT, true);
        addCell(table, "T.Tiền", fontBold, Element.ALIGN_RIGHT, true);

        for (InvoiceDetail item : invoice.getDetails()) {
            addCell(table, item.getProductName(), fontNormal, Element.ALIGN_LEFT, false);
            addCell(table, String.valueOf(item.getQuantity()), fontNormal, Element.ALIGN_CENTER, false);
            addCell(table, FormatUtil.formatNumber(item.getUnitPrice()), fontNormal, Element.ALIGN_RIGHT, false);
            addCell(table, FormatUtil.formatNumber(item.getLineTotal()), fontNormal, Element.ALIGN_RIGHT, false);
        }
        document.add(table);

        document.add(new LineSeparator(0.5f, 100, BaseColor.GRAY, Element.ALIGN_CENTER, -2));

        // Bảng tổng kết tiền
        PdfPTable summary = new PdfPTable(new float[]{5.5f, 4.5f});
        summary.setWidthPercentage(100);
        summary.setSpacingBefore(4f);

        addSummaryRow(summary, "Tạm tính:", FormatUtil.formatCurrency(invoice.getSubtotal()), fontNormal);
        addSummaryRow(summary, "Giảm giá" + (invoice.getVoucherCode() != null ? " (" + invoice.getVoucherCode() + ")" : "") + ":",
                "-" + FormatUtil.formatCurrency(invoice.getDiscountAmount()), fontNormal);
        addSummaryRow(summary, "Thuế VAT:", FormatUtil.formatCurrency(invoice.getVatAmount()), fontNormal);
        addSummaryRow(summary, "TỔNG THANH TOÁN:", FormatUtil.formatCurrency(invoice.getTotalAmount()), fontBold);
        addSummaryRow(summary, "Phương thức:", "CASH".equalsIgnoreCase(invoice.getPaymentMethod()) ? "Tiền mặt" : "Chuyển khoản", fontNormal);
        addSummaryRow(summary, "Tiền khách đưa:", FormatUtil.formatCurrency(invoice.getCashReceived()), fontNormal);
        addSummaryRow(summary, "Tiền thừa trả khách:", FormatUtil.formatCurrency(invoice.getChangeAmount()), fontBold);

        document.add(summary);

        if ("TRANSFER".equalsIgnoreCase(invoice.getPaymentMethod())) {
            Paragraph bankNote = new Paragraph("TT Chuyển khoản: " + AppConfig.getStoreBankInfo(), fontSmall);
            bankNote.setAlignment(Element.ALIGN_CENTER);
            bankNote.setSpacingBefore(6f);
            document.add(bankNote);
        }

        Paragraph thanks = new Paragraph("\nCảm ơn Quý khách & Hẹn gặp lại!", fontSmall);
        thanks.setAlignment(Element.ALIGN_CENTER);
        document.add(thanks);

        document.close();
    }

    public static void exportFullReportToPdf(
            String reportTitle,
            List<RevenueReportDTO> revenueList,
            List<TopProductDTO> topProducts,
            List<TopCustomerDTO> topCustomers,
            List<EmployeeRevenueDTO> employeeStats,
            File outputFile) throws Exception {

        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, new FileOutputStream(outputFile));
        document.open();

        BaseFont bf = loadVietnameseBaseFont();
        Font fontHeader = new Font(bf, 16, Font.BOLD, new BaseColor(30, 58, 138));
        Font fontSection = new Font(bf, 12, Font.BOLD, new BaseColor(15, 23, 42));
        Font fontBold = new Font(bf, 9.5f, Font.BOLD, BaseColor.BLACK);
        Font fontNormal = new Font(bf, 9.5f, Font.NORMAL, BaseColor.BLACK);

        Paragraph title = new Paragraph(AppConfig.getStoreName().toUpperCase() + " - " + reportTitle, fontHeader);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph sub = new Paragraph("Ngày xuất báo cáo: " + FormatUtil.formatDateTime(LocalDateTime.now()), fontNormal);
        sub.setAlignment(Element.ALIGN_CENTER);
        sub.setSpacingAfter(14f);
        document.add(sub);

        // 1. Doanh thu & Lợi nhuận
        Paragraph sec1 = new Paragraph("1. Thống kê Doanh thu & Lợi nhuận", fontSection);
        sec1.setSpacingBefore(8f);
        sec1.setSpacingAfter(6f);
        document.add(sec1);

        PdfPTable revTable = new PdfPTable(new float[]{2.2f, 1.3f, 2.2f, 2.2f, 2.1f});
        revTable.setWidthPercentage(100);
        addReportCell(revTable, "Kỳ thống kê", fontBold, true);
        addReportCell(revTable, "Số đơn", fontBold, true);
        addReportCell(revTable, "Doanh thu", fontBold, true);
        addReportCell(revTable, "Giá vốn", fontBold, true);
        addReportCell(revTable, "Lợi nhuận gộp", fontBold, true);

        BigDecimal totalRev = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;
        int totalOrders = 0;
        for (RevenueReportDTO r : revenueList) {
            addReportCell(revTable, r.getPeriodLabel(), fontNormal, false);
            addReportCell(revTable, String.valueOf(r.getOrderCount()), fontNormal, false);
            addReportCell(revTable, FormatUtil.formatCurrency(r.getRevenue()), fontNormal, false);
            addReportCell(revTable, FormatUtil.formatCurrency(r.getCostOfGoods()), fontNormal, false);
            addReportCell(revTable, FormatUtil.formatCurrency(r.getProfit()), fontNormal, false);
            totalRev = totalRev.add(r.getRevenue());
            totalProfit = totalProfit.add(r.getProfit());
            totalOrders += r.getOrderCount();
        }
        addReportCell(revTable, "TỔNG CỘNG", fontBold, true);
        addReportCell(revTable, String.valueOf(totalOrders), fontBold, true);
        addReportCell(revTable, FormatUtil.formatCurrency(totalRev), fontBold, true);
        addReportCell(revTable, "-", fontBold, true);
        addReportCell(revTable, FormatUtil.formatCurrency(totalProfit), fontBold, true);
        document.add(revTable);

        // 2. Top sản phẩm bán chạy
        Paragraph sec2 = new Paragraph("2. Top Sản phẩm bán chạy", fontSection);
        sec2.setSpacingBefore(14f);
        sec2.setSpacingAfter(6f);
        document.add(sec2);

        PdfPTable prodTable = new PdfPTable(new float[]{1.5f, 3.5f, 2.0f, 1.3f, 2.2f});
        prodTable.setWidthPercentage(100);
        addReportCell(prodTable, "Mã SP", fontBold, true);
        addReportCell(prodTable, "Tên sản phẩm", fontBold, true);
        addReportCell(prodTable, "Danh mục", fontBold, true);
        addReportCell(prodTable, "SL Bán", fontBold, true);
        addReportCell(prodTable, "Doanh thu", fontBold, true);

        for (TopProductDTO p : topProducts) {
            addReportCell(prodTable, p.getProductCode(), fontNormal, false);
            addReportCell(prodTable, p.getProductName(), fontNormal, false);
            addReportCell(prodTable, p.getCategoryName(), fontNormal, false);
            addReportCell(prodTable, String.valueOf(p.getTotalQuantitySold()), fontNormal, false);
            addReportCell(prodTable, FormatUtil.formatCurrency(p.getTotalRevenue()), fontNormal, false);
        }
        document.add(prodTable);

        // 3. Top khách hàng thân thiết
        Paragraph sec3 = new Paragraph("3. Top Khách hàng thân thiết", fontSection);
        sec3.setSpacingBefore(14f);
        sec3.setSpacingAfter(6f);
        document.add(sec3);

        PdfPTable custTable = new PdfPTable(new float[]{3.2f, 2.2f, 1.5f, 1.5f, 2.4f});
        custTable.setWidthPercentage(100);
        addReportCell(custTable, "Họ tên khách hàng", fontBold, true);
        addReportCell(custTable, "Số điện thoại", fontBold, true);
        addReportCell(custTable, "Điểm TL", fontBold, true);
        addReportCell(custTable, "Số đơn", fontBold, true);
        addReportCell(custTable, "Tổng chi tiêu", fontBold, true);

        for (TopCustomerDTO c : topCustomers) {
            addReportCell(custTable, c.getCustomerName(), fontNormal, false);
            addReportCell(custTable, c.getPhone(), fontNormal, false);
            addReportCell(custTable, String.valueOf(c.getPoints()), fontNormal, false);
            addReportCell(custTable, String.valueOf(c.getOrderCount()), fontNormal, false);
            addReportCell(custTable, FormatUtil.formatCurrency(c.getTotalSpent()), fontNormal, false);
        }
        document.add(custTable);

        // 4. Doanh thu theo nhân viên
        if (employeeStats != null && !employeeStats.isEmpty()) {
            Paragraph sec4 = new Paragraph("4. Doanh thu theo Nhân viên", fontSection);
            sec4.setSpacingBefore(14f);
            sec4.setSpacingAfter(6f);
            document.add(sec4);

            PdfPTable empTable = new PdfPTable(new float[]{1.8f, 3.4f, 1.6f, 1.5f, 2.5f});
            empTable.setWidthPercentage(100);
            addReportCell(empTable, "Mã NV", fontBold, true);
            addReportCell(empTable, "Họ tên nhân viên", fontBold, true);
            addReportCell(empTable, "Vai trò", fontBold, true);
            addReportCell(empTable, "Số đơn", fontBold, true);
            addReportCell(empTable, "Tổng doanh thu", fontBold, true);

            for (EmployeeRevenueDTO e : employeeStats) {
                addReportCell(empTable, e.getEmployeeCode(), fontNormal, false);
                addReportCell(empTable, e.getFullName(), fontNormal, false);
                addReportCell(empTable, e.getRole(), fontNormal, false);
                addReportCell(empTable, String.valueOf(e.getOrderCount()), fontNormal, false);
                addReportCell(empTable, FormatUtil.formatCurrency(e.getTotalRevenue()), fontNormal, false);
            }
            document.add(empTable);
        }

        document.close();
    }

    private static void addCell(PdfPTable table, String text, Font font, int align, boolean header) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setHorizontalAlignment(align);
        cell.setBorder(header ? Rectangle.BOTTOM : Rectangle.NO_BORDER);
        cell.setPaddingTop(3f);
        cell.setPaddingBottom(4f);
        table.addCell(cell);
    }

    private static void addSummaryRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, font));
        c1.setBorder(Rectangle.NO_BORDER);
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        c1.setPaddingTop(2f);
        c1.setPaddingBottom(2f);

        PdfPCell c2 = new PdfPCell(new Phrase(value, font));
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c2.setPaddingTop(2f);
        c2.setPaddingBottom(2f);

        table.addCell(c1);
        table.addCell(c2);
    }

    private static void addReportCell(PdfPTable table, String text, Font font, boolean header) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setPadding(5f);
        if (header) {
            cell.setBackgroundColor(new BaseColor(241, 245, 249));
        }
        table.addCell(cell);
    }
}
