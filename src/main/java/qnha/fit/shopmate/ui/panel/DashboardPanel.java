package qnha.fit.shopmate.ui.panel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import qnha.fit.shopmate.dto.DashboardStatsDTO;
import qnha.fit.shopmate.dto.TopProductDTO;
import qnha.fit.shopmate.model.Invoice;
import qnha.fit.shopmate.service.InvoiceService;
import qnha.fit.shopmate.service.ReportService;
import qnha.fit.shopmate.ui.component.StatCard;
import qnha.fit.shopmate.ui.dialog.InvoicePreviewDialog;
import qnha.fit.shopmate.util.FormatUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DashboardPanel extends JPanel {
    private final ReportService reportService = new ReportService();
    private final InvoiceService invoiceService = new InvoiceService();

    private final StatCard cardRevenue = new StatCard("DOANH THU HÔM NAY", "0 ₫", "Lợi nhuận: 0 ₫", new Color(37, 99, 235));
    private final StatCard cardOrders = new StatCard("SỐ ĐƠN HÔM NAY", "0", "Đơn đã thanh toán", new Color(16, 185, 129));
    private final StatCard cardLowStock = new StatCard("SP SẮP HẾT HÀNG", "0", "Tồn kho < 10 cần nhập", new Color(239, 68, 68));
    private final StatCard cardNewCustomers = new StatCard("KHÁCH HÀNG MỚI", "0", "Trong 30 ngày qua", new Color(245, 158, 11));

    private final DefaultCategoryDataset chartDataset = new DefaultCategoryDataset();
    private final DefaultTableModel topProductModel;
    private final DefaultTableModel recentOrdersModel;
    private List<Invoice> recentInvoicesList;

    public DashboardPanel() {
        setLayout(new BorderLayout(14, 14));
        setBorder(new EmptyBorder(16, 18, 16, 18));

        // 1. Hàng trên: 4 thẻ thống kê
        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 14, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setPreferredSize(new Dimension(0, 112));
        cardsPanel.add(cardRevenue);
        cardsPanel.add(cardOrders);
        cardsPanel.add(cardLowStock);
        cardsPanel.add(cardNewCustomers);

        // 2. Giữa: Biểu đồ 7 ngày (Trái) + Top 5 SP bán chạy (Phải)
        JPanel middlePanel = new JPanel(new GridLayout(1, 2, 14, 0));
        middlePanel.setOpaque(false);

        JFreeChart barChart = ChartFactory.createBarChart(
                "Doanh thu 7 ngày gần nhất (VNĐ)",
                "Ngày",
                "Doanh thu (VNĐ)",
                chartDataset,
                PlotOrientation.VERTICAL,
                false, true, false
        );
        styleChart(barChart);
        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setBorder(BorderFactory.createTitledBorder("Biểu đồ Doanh thu 7 ngày"));

        String[] topCols = {"Hạng", "Mã SP", "Tên sản phẩm", "SL Bán", "Doanh thu"};
        topProductModel = new DefaultTableModel(topCols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable tblTopProducts = new JTable(topProductModel);
        tblTopProducts.setRowHeight(28);
        JScrollPane topScroll = new JScrollPane(tblTopProducts);
        topScroll.setBorder(BorderFactory.createTitledBorder("Top 5 Sản phẩm bán chạy nhất"));

        middlePanel.add(chartPanel);
        middlePanel.add(topScroll);

        // 3. Dưới: Đơn hàng gần đây (Double click xem hóa đơn)
        String[] orderCols = {"Mã HĐ", "Thời gian", "Khách hàng", "Nhân viên", "Tổng tiền", "Phương thức", "Trạng thái"};
        recentOrdersModel = new DefaultTableModel(orderCols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable tblRecentOrders = new JTable(recentOrdersModel);
        tblRecentOrders.setRowHeight(28);
        tblRecentOrders.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblRecentOrders.getSelectedRow() >= 0) {
                    int row = tblRecentOrders.getSelectedRow();
                    if (recentInvoicesList != null && row < recentInvoicesList.size()) {
                        Invoice full = invoiceService.getInvoiceWithDetails(recentInvoicesList.get(row).getId());
                        if (full != null) {
                            new InvoicePreviewDialog(SwingUtilities.getWindowAncestor(DashboardPanel.this), full).setVisible(true);
                        }
                    }
                }
            }
        });

        JScrollPane recentScroll = new JScrollPane(tblRecentOrders);
        recentScroll.setPreferredSize(new Dimension(0, 195));
        recentScroll.setBorder(BorderFactory.createTitledBorder("Đơn hàng gần đây (Double-click để xem & in hóa đơn)"));

        add(cardsPanel, BorderLayout.NORTH);
        add(middlePanel, BorderLayout.CENTER);
        add(recentScroll, BorderLayout.SOUTH);

        refreshData();
    }

    private void styleChart(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(248, 250, 252));
        plot.setRangeGridlinePaint(new Color(203, 213, 225));
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(37, 99, 235));
    }

    public void refreshData() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            DashboardStatsDTO stats = reportService.getDashboardStats();
            cardRevenue.updateValues(
                    FormatUtil.formatCurrency(stats.getTodayRevenue()),
                    "Lợi nhuận gộp: " + FormatUtil.formatCurrency(stats.getTodayProfit())
            );
            cardOrders.updateValues(String.valueOf(stats.getTodayOrderCount()), "Đơn hoàn tất hôm nay");
            cardLowStock.updateValues(String.valueOf(stats.getLowStockCount()), "Sản phẩm có tồn < 10");
            cardNewCustomers.updateValues(String.valueOf(stats.getNewCustomerCount()), "Khách đăng ký mới (30 ngày)");

            chartDataset.clear();
            Map<String, BigDecimal> last7Days = reportService.getLast7DaysRevenue();
            for (Map.Entry<String, BigDecimal> entry : last7Days.entrySet()) {
                chartDataset.addValue(entry.getValue(), "Doanh thu", entry.getKey());
            }

            topProductModel.setRowCount(0);
            List<TopProductDTO> top5 = reportService.getTopSellingProducts(5, null, null);
            int rank = 1;
            for (TopProductDTO p : top5) {
                topProductModel.addRow(new Object[]{
                        "#" + (rank++),
                        p.getProductCode(),
                        p.getProductName(),
                        p.getTotalQuantitySold(),
                        FormatUtil.formatCurrency(p.getTotalRevenue())
                });
            }

            recentOrdersModel.setRowCount(0);
            recentInvoicesList = invoiceService.getRecentInvoices(10);
            for (Invoice inv : recentInvoicesList) {
                recentOrdersModel.addRow(new Object[]{
                        inv.getInvoiceCode(),
                        FormatUtil.formatDateTime(inv.getCreatedAt()),
                        inv.getCustomerName(),
                        inv.getEmployeeName(),
                        FormatUtil.formatCurrency(inv.getTotalAmount()),
                        "CASH".equalsIgnoreCase(inv.getPaymentMethod()) ? "Tiền mặt" : "Chuyển khoản",
                        "PAID".equalsIgnoreCase(inv.getStatus()) ? "Đã thanh toán" : "Đã hủy"
                });
            }
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
}
