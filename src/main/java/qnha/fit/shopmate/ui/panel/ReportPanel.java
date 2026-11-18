package qnha.fit.shopmate.ui.panel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import qnha.fit.shopmate.dto.EmployeeRevenueDTO;
import qnha.fit.shopmate.dto.RevenueReportDTO;
import qnha.fit.shopmate.dto.TopCustomerDTO;
import qnha.fit.shopmate.dto.TopProductDTO;
import qnha.fit.shopmate.service.ReportService;
import qnha.fit.shopmate.util.ExcelExporter;
import qnha.fit.shopmate.util.FormatUtil;
import qnha.fit.shopmate.util.PDFExporter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.util.List;

public class ReportPanel extends JPanel {
    private final ReportService reportService = new ReportService();

    private final JComboBox<String> cboGroupBy = new JComboBox<>(new String[]{"Theo Ngày (DAY)", "Theo Tháng (MONTH)", "Theo Năm (YEAR)"});
    private final JTextField txtFromDate = new JTextField(10);
    private final JTextField txtToDate = new JTextField(10);

    private final DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
    private final DefaultCategoryDataset lineDataset = new DefaultCategoryDataset();

    private final DefaultTableModel revTableModel;
    private final DefaultTableModel topProdTableModel;
    private final DefaultTableModel topCustTableModel;
    private final DefaultTableModel empRevTableModel;

    private List<RevenueReportDTO> currentRevenueList;
    private List<TopProductDTO> currentTopProducts;
    private List<TopCustomerDTO> currentTopCustomers;
    private List<EmployeeRevenueDTO> currentEmployeeStats;

    public ReportPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(16, 18, 16, 18));

        txtFromDate.setText(LocalDate.now().minusDays(30).toString());
        txtToDate.setText(LocalDate.now().toString());

        // Toolbar lọc & Xuất báo cáo
        JPanel topBar = new JPanel(new BorderLayout(8, 8));
        JPanel filterBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterBox.add(new JLabel("Thống kê:"));
        filterBox.add(cboGroupBy);
        filterBox.add(new JLabel("Từ ngày (yyyy-MM-dd):"));
        filterBox.add(txtFromDate);
        filterBox.add(new JLabel("Đến ngày:"));
        filterBox.add(txtToDate);

        JButton btnAnalyze = new JButton("Xem thống kê");
        btnAnalyze.setBackground(new Color(37, 99, 235));
        btnAnalyze.setForeground(Color.WHITE);
        filterBox.add(btnAnalyze);

        JPanel exportBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        JButton btnExportExcel = new JButton("Xuất Báo cáo Excel (.xlsx)");
        btnExportExcel.setBackground(new Color(22, 163, 74));
        btnExportExcel.setForeground(Color.WHITE);
        JButton btnExportPdf = new JButton("Xuất Báo cáo PDF (iText)");
        btnExportPdf.setBackground(new Color(220, 38, 38));
        btnExportPdf.setForeground(Color.WHITE);
        exportBox.add(btnExportExcel);
        exportBox.add(btnExportPdf);

        topBar.add(filterBox, BorderLayout.WEST);
        topBar.add(exportBox, BorderLayout.EAST);

        // Tabs hiển thị Biểu đồ & Bảng chi tiết
        JTabbedPane tabbedPane = new JTabbedPane();

        // Tab 1: Biểu đồ Doanh thu & Lợi nhuận (Bar + Line) + Bảng
        JPanel revTab = new JPanel(new BorderLayout(10, 10));
        JPanel chartsGrid = new JPanel(new GridLayout(1, 2, 12, 0));
        chartsGrid.setPreferredSize(new Dimension(0, 300));

        JFreeChart barChart = ChartFactory.createBarChart(
                "Biểu đồ Cột: Doanh thu & Lợi nhuận", "Kỳ", "Số tiền (VNĐ)",
                barDataset, PlotOrientation.VERTICAL, true, true, false);
        stylePlot(barChart);

        JFreeChart lineChart = ChartFactory.createLineChart(
                "Biểu đồ Đường: Xu hướng Doanh thu", "Kỳ", "Doanh thu (VNĐ)",
                lineDataset, PlotOrientation.VERTICAL, true, true, false);
        stylePlot(lineChart);

        chartsGrid.add(new ChartPanel(barChart));
        chartsGrid.add(new ChartPanel(lineChart));

        String[] revCols = {"Kỳ thống kê", "Số đơn hàng", "Doanh thu", "Giá vốn hàng bán", "Tổng giảm giá", "Lợi nhuận gộp"};
        revTableModel = new DefaultTableModel(revCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblRev = new JTable(revTableModel);
        tblRev.setRowHeight(28);

        revTab.add(chartsGrid, BorderLayout.NORTH);
        revTab.add(new JScrollPane(tblRev), BorderLayout.CENTER);

        // Tab 2: Top Sản phẩm bán chạy
        String[] prodCols = {"Hạng", "Mã SP", "Tên sản phẩm", "Danh mục", "Tổng SL đã bán", "Tổng Doanh thu", "Lợi nhuận mang lại"};
        topProdTableModel = new DefaultTableModel(prodCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblTopProd = new JTable(topProdTableModel);
        tblTopProd.setRowHeight(28);

        // Tab 3: Top Khách hàng thân thiết
        String[] custCols = {"Hạng", "Họ và tên khách hàng", "Số điện thoại", "Điểm tích lũy", "Số đơn đã mua", "Tổng chi tiêu"};
        topCustTableModel = new DefaultTableModel(custCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblTopCust = new JTable(topCustTableModel);
        tblTopCust.setRowHeight(28);

        // Tab 4: Doanh thu theo Nhân viên
        String[] empCols = {"Mã NV", "Họ và tên nhân viên", "Vai trò", "Số đơn hàng đã bán", "Tổng doanh thu"};
        empRevTableModel = new DefaultTableModel(empCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblEmpRev = new JTable(empRevTableModel);
        tblEmpRev.setRowHeight(28);

        tabbedPane.addTab("1. Doanh thu & Lợi nhuận (Biểu đồ)", revTab);
        tabbedPane.addTab("2. Top Sản phẩm bán chạy", new JScrollPane(tblTopProd));
        tabbedPane.addTab("3. Top Khách hàng thân thiết", new JScrollPane(tblTopCust));
        tabbedPane.addTab("4. Doanh thu theo Nhân viên", new JScrollPane(tblEmpRev));

        btnAnalyze.addActionListener(e -> refreshData());
        cboGroupBy.addActionListener(e -> refreshData());
        btnExportExcel.addActionListener(e -> handleExportExcel());
        btnExportPdf.addActionListener(e -> handleExportPdf());

        add(topBar, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);

        refreshData();
    }

    private void stylePlot(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(248, 250, 252));
        plot.setRangeGridlinePaint(new Color(203, 213, 225));
    }

    public void refreshData() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            LocalDate from = LocalDate.parse(txtFromDate.getText().trim());
            LocalDate to = LocalDate.parse(txtToDate.getText().trim());
            String mode = switch (cboGroupBy.getSelectedIndex()) {
                case 1 -> "MONTH";
                case 2 -> "YEAR";
                default -> "DAY";
            };

            currentRevenueList = reportService.getRevenueReport(mode, from, to);
            currentTopProducts = reportService.getTopSellingProducts(15, from, to);
            currentTopCustomers = reportService.getTopCustomers(15);
            currentEmployeeStats = reportService.getEmployeeRevenueReport(from, to);

            barDataset.clear();
            lineDataset.clear();
            revTableModel.setRowCount(0);

            for (RevenueReportDTO r : currentRevenueList) {
                barDataset.addValue(r.getRevenue(), "Doanh thu", r.getPeriodLabel());
                barDataset.addValue(r.getProfit(), "Lợi nhuận", r.getPeriodLabel());
                lineDataset.addValue(r.getRevenue(), "Doanh thu", r.getPeriodLabel());

                revTableModel.addRow(new Object[]{
                        r.getPeriodLabel(),
                        r.getOrderCount(),
                        FormatUtil.formatCurrency(r.getRevenue()),
                        FormatUtil.formatCurrency(r.getCostOfGoods()),
                        FormatUtil.formatCurrency(r.getDiscountTotal()),
                        FormatUtil.formatCurrency(r.getProfit())
                });
            }

            topProdTableModel.setRowCount(0);
            int pRank = 1;
            for (TopProductDTO p : currentTopProducts) {
                topProdTableModel.addRow(new Object[]{
                        "#" + (pRank++),
                        p.getProductCode(),
                        p.getProductName(),
                        p.getCategoryName(),
                        p.getTotalQuantitySold(),
                        FormatUtil.formatCurrency(p.getTotalRevenue()),
                        FormatUtil.formatCurrency(p.getTotalProfit())
                });
            }

            topCustTableModel.setRowCount(0);
            int cRank = 1;
            for (TopCustomerDTO c : currentTopCustomers) {
                topCustTableModel.addRow(new Object[]{
                        "#" + (cRank++),
                        c.getCustomerName(),
                        c.getPhone(),
                        c.getPoints(),
                        c.getOrderCount(),
                        FormatUtil.formatCurrency(c.getTotalSpent())
                });
            }

            empRevTableModel.setRowCount(0);
            for (EmployeeRevenueDTO e : currentEmployeeStats) {
                empRevTableModel.addRow(new Object[]{
                        e.getEmployeeCode(),
                        e.getFullName(),
                        e.getRole(),
                        e.getOrderCount(),
                        FormatUtil.formatCurrency(e.getTotalRevenue())
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải báo cáo: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void handleExportExcel() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Xuất Báo cáo Thống kê ra Excel");
        chooser.setSelectedFile(new File("BaoCaoThongKe_ShopMate.xlsx"));
        chooser.setFileFilter(new FileNameExtensionFilter("Excel Workbook (*.xlsx)", "xlsx"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".xlsx")) {
                file = new File(file.getAbsolutePath() + ".xlsx");
            }
            try {
                ExcelExporter.exportFullReportToExcel(
                        currentRevenueList, currentTopProducts, currentTopCustomers, currentEmployeeStats, file);
                JOptionPane.showMessageDialog(this, "Đã xuất Báo cáo Excel (4 Sheets) thành công:\n" + file.getAbsolutePath(),
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi xuất Excel: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleExportPdf() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Xuất Báo cáo Thống kê ra PDF");
        chooser.setSelectedFile(new File("BaoCaoThongKe_ShopMate.pdf"));
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Document (*.pdf)", "pdf"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getAbsolutePath() + ".pdf");
            }
            try {
                PDFExporter.exportFullReportToPdf(
                        "BÁO CÁO DOANH THU & HOẠT ĐỘNG KINH DOANH",
                        currentRevenueList, currentTopProducts, currentTopCustomers, currentEmployeeStats, file);
                JOptionPane.showMessageDialog(this, "Đã xuất Báo cáo PDF thành công:\n" + file.getAbsolutePath(),
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi xuất PDF: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
