package qnha.fit.shopmate.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import qnha.fit.shopmate.dto.EmployeeRevenueDTO;
import qnha.fit.shopmate.dto.RevenueReportDTO;
import qnha.fit.shopmate.dto.TopCustomerDTO;
import qnha.fit.shopmate.dto.TopProductDTO;
import qnha.fit.shopmate.model.Invoice;
import qnha.fit.shopmate.model.Product;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public final class ExcelExporter {

    private ExcelExporter() {
    }

    public static void exportProductsToExcel(List<Product> products, File file) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Danh sách Sản phẩm");
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle moneyStyle = createMoneyStyle(workbook);

            String[] headers = {"Mã SP", "Tên sản phẩm", "Danh mục", "Đơn vị", "Giá nhập", "Giá bán", "Tồn kho", "Trạng thái"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Product p : products) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(p.getCode());
                row.createCell(1).setCellValue(p.getName());
                row.createCell(2).setCellValue(p.getCategoryName() != null ? p.getCategoryName() : "");
                row.createCell(3).setCellValue(p.getUnit());

                Cell costCell = row.createCell(4);
                costCell.setCellValue(p.getCostPrice().doubleValue());
                costCell.setCellStyle(moneyStyle);

                Cell priceCell = row.createCell(5);
                priceCell.setCellValue(p.getSellingPrice().doubleValue());
                priceCell.setCellStyle(moneyStyle);

                row.createCell(6).setCellValue(p.getStockQuantity());
                row.createCell(7).setCellValue(p.isActive() ? "Đang bán" : "Ngừng KD");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
        }
    }

    public static void exportInvoicesToExcel(List<Invoice> invoices, File file) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Lịch sử Hóa đơn");
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle moneyStyle = createMoneyStyle(workbook);

            String[] headers = {"Mã HĐ", "Ngày tạo", "Khách hàng", "Thu ngân", "Tạm tính", "Giảm giá", "VAT", "Tổng thanh toán", "Phương thức", "Trạng thái"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Invoice inv : invoices) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(inv.getInvoiceCode());
                row.createCell(1).setCellValue(FormatUtil.formatDateTime(inv.getCreatedAt()));
                row.createCell(2).setCellValue(inv.getCustomerName());
                row.createCell(3).setCellValue(inv.getEmployeeName() != null ? inv.getEmployeeName() : "");

                createMoneyCell(row, 4, inv.getSubtotal().doubleValue(), moneyStyle);
                createMoneyCell(row, 5, inv.getDiscountAmount().doubleValue(), moneyStyle);
                createMoneyCell(row, 6, inv.getVatAmount().doubleValue(), moneyStyle);
                createMoneyCell(row, 7, inv.getTotalAmount().doubleValue(), moneyStyle);

                row.createCell(8).setCellValue("CASH".equalsIgnoreCase(inv.getPaymentMethod()) ? "Tiền mặt" : "Chuyển khoản");
                row.createCell(9).setCellValue("PAID".equalsIgnoreCase(inv.getStatus()) ? "Đã thanh toán" : "Đã hủy");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
        }
    }

    public static void exportFullReportToExcel(
            List<RevenueReportDTO> revenueList,
            List<TopProductDTO> topProducts,
            List<TopCustomerDTO> topCustomers,
            List<EmployeeRevenueDTO> employeeStats,
            File file) throws Exception {

        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle moneyStyle = createMoneyStyle(workbook);

            // Sheet 1: Doanh thu & Lợi nhuận
            Sheet revSheet = workbook.createSheet("Doanh thu & Lợi nhuận");
            String[] revHeaders = {"Kỳ thống kê", "Số đơn hàng", "Doanh thu (VNĐ)", "Giá vốn (VNĐ)", "Giảm giá (VNĐ)", "Lợi nhuận gộp (VNĐ)"};
            createHeaderRow(revSheet, revHeaders, headerStyle);
            int rIdx = 1;
            for (RevenueReportDTO r : revenueList) {
                Row row = revSheet.createRow(rIdx++);
                row.createCell(0).setCellValue(r.getPeriodLabel());
                row.createCell(1).setCellValue(r.getOrderCount());
                createMoneyCell(row, 2, r.getRevenue().doubleValue(), moneyStyle);
                createMoneyCell(row, 3, r.getCostOfGoods().doubleValue(), moneyStyle);
                createMoneyCell(row, 4, r.getDiscountTotal().doubleValue(), moneyStyle);
                createMoneyCell(row, 5, r.getProfit().doubleValue(), moneyStyle);
            }
            autoSizeColumns(revSheet, revHeaders.length);

            // Sheet 2: Top Sản phẩm
            Sheet prodSheet = workbook.createSheet("Top Sản phẩm bán chạy");
            String[] prodHeaders = {"Mã SP", "Tên sản phẩm", "Danh mục", "Tổng SL bán", "Doanh thu (VNĐ)", "Lợi nhuận (VNĐ)"};
            createHeaderRow(prodSheet, prodHeaders, headerStyle);
            int pIdx = 1;
            for (TopProductDTO p : topProducts) {
                Row row = prodSheet.createRow(pIdx++);
                row.createCell(0).setCellValue(p.getProductCode());
                row.createCell(1).setCellValue(p.getProductName());
                row.createCell(2).setCellValue(p.getCategoryName() != null ? p.getCategoryName() : "");
                row.createCell(3).setCellValue(p.getTotalQuantitySold());
                createMoneyCell(row, 4, p.getTotalRevenue().doubleValue(), moneyStyle);
                createMoneyCell(row, 5, p.getTotalProfit().doubleValue(), moneyStyle);
            }
            autoSizeColumns(prodSheet, prodHeaders.length);

            // Sheet 3: Top Khách hàng
            Sheet custSheet = workbook.createSheet("Top Khách hàng thân thiết");
            String[] custHeaders = {"Họ tên khách hàng", "Số điện thoại", "Điểm tích lũy", "Số đơn đã mua", "Tổng chi tiêu (VNĐ)"};
            createHeaderRow(custSheet, custHeaders, headerStyle);
            int cIdx = 1;
            for (TopCustomerDTO c : topCustomers) {
                Row row = custSheet.createRow(cIdx++);
                row.createCell(0).setCellValue(c.getCustomerName());
                row.createCell(1).setCellValue(c.getPhone());
                row.createCell(2).setCellValue(c.getPoints());
                row.createCell(3).setCellValue(c.getOrderCount());
                createMoneyCell(row, 4, c.getTotalSpent().doubleValue(), moneyStyle);
            }
            autoSizeColumns(custSheet, custHeaders.length);

            // Sheet 4: Doanh thu Nhân viên
            Sheet empSheet = workbook.createSheet("Doanh thu Nhân viên");
            String[] empHeaders = {"Mã NV", "Họ và tên", "Vai trò", "Số đơn lập", "Tổng doanh thu (VNĐ)"};
            createHeaderRow(empSheet, empHeaders, headerStyle);
            int eIdx = 1;
            if (employeeStats != null) {
                for (EmployeeRevenueDTO e : employeeStats) {
                    Row row = empSheet.createRow(eIdx++);
                    row.createCell(0).setCellValue(e.getEmployeeCode());
                    row.createCell(1).setCellValue(e.getFullName());
                    row.createCell(2).setCellValue(e.getRole());
                    row.createCell(3).setCellValue(e.getOrderCount());
                    createMoneyCell(row, 4, e.getTotalRevenue().doubleValue(), moneyStyle);
                }
            }
            autoSizeColumns(empSheet, empHeaders.length);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
        }
    }

    private static void createHeaderRow(Sheet sheet, String[] headers, CellStyle style) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    private static void createMoneyCell(Row row, int col, double val, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(val);
        cell.setCellStyle(style);
    }

    private static void autoSizeColumns(Sheet sheet, int count) {
        for (int i = 0; i < count; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private static CellStyle createMoneyStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        DataFormat format = wb.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0"));
        return style;
    }
}
