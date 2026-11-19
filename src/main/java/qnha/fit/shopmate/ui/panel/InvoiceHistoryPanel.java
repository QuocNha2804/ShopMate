package qnha.fit.shopmate.ui.panel;

import qnha.fit.shopmate.config.SessionManager;
import qnha.fit.shopmate.model.Invoice;
import qnha.fit.shopmate.model.User;
import qnha.fit.shopmate.service.AuthService;
import qnha.fit.shopmate.service.InvoiceService;
import qnha.fit.shopmate.ui.dialog.InvoicePreviewDialog;
import qnha.fit.shopmate.util.ExcelExporter;
import qnha.fit.shopmate.util.FormatUtil;
import qnha.fit.shopmate.util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.time.LocalDate;
import java.util.List;

public class InvoiceHistoryPanel extends JPanel {
    private final InvoiceService invoiceService = new InvoiceService();
    private final AuthService authService = new AuthService();

    private final JTextField txtKeyword = new JTextField(14);
    private final JTextField txtFromDate = new JTextField(10);
    private final JTextField txtToDate = new JTextField(10);
    private final JComboBox<User> cboEmployee = new JComboBox<>();
    private final JComboBox<String> cboStatus = new JComboBox<>(new String[]{"ALL", "PAID", "CANCELLED"});

    private final DefaultTableModel invoiceModel;
    private final JTable tblInvoices;
    private List<Invoice> currentInvoices;

    public InvoiceHistoryPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(16, 18, 16, 18));

        txtFromDate.setText(LocalDate.now().minusDays(30).toString());
        txtToDate.setText(LocalDate.now().toString());

        JPanel topBar = new JPanel(new BorderLayout(8, 8));
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterPanel.add(new JLabel("Mã HĐ / Khách:"));
        UIUtil.styleSearchField(txtKeyword, "Mã HĐ, tên/SĐT khách...");
        txtKeyword.setPreferredSize(new Dimension(170, 32));
        filterPanel.add(txtKeyword);
        filterPanel.add(new JLabel("Từ:"));
        filterPanel.add(txtFromDate);
        filterPanel.add(new JLabel("Đến:"));
        filterPanel.add(txtToDate);
        filterPanel.add(new JLabel("NV:"));
        filterPanel.add(cboEmployee);
        filterPanel.add(new JLabel("Trạng thái:"));
        filterPanel.add(cboStatus);

        JButton btnFilter = new JButton("Lọc hóa đơn");
        UIUtil.stylePrimaryButton(btnFilter);
        filterPanel.add(btnFilter);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        JButton btnViewPrint = new JButton("Xem chi tiết & In lại");
        UIUtil.styleSecondaryButton(btnViewPrint);
        JButton btnCancelInvoice = new JButton("Hủy hóa đơn & Hoàn tồn kho (ADMIN)");
        UIUtil.styleDangerButton(btnCancelInvoice);
        btnCancelInvoice.setEnabled(SessionManager.isAdmin());
        JButton btnExportExcel = new JButton("Xuất Excel");
        UIUtil.styleSuccessButton(btnExportExcel);

        actionPanel.add(btnViewPrint);
        actionPanel.add(btnCancelInvoice);
        actionPanel.add(btnExportExcel);

        topBar.add(filterPanel, BorderLayout.NORTH);
        topBar.add(actionPanel, BorderLayout.SOUTH);

        String[] cols = {"Mã HĐ", "Ngày giờ", "Khách hàng", "SĐT Khách", "Thu ngân", "Tạm tính", "Giảm giá", "VAT", "Tổng thanh toán", "Phương thức", "Trạng thái", "Ghi chú hủy"};
        invoiceModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblInvoices = new JTable(invoiceModel);
        UIUtil.styleTable(tblInvoices);
        UIUtil.alignColumnCenter(tblInvoices, 0, 9, 10);
        UIUtil.alignColumnRight(tblInvoices, 5, 6, 7, 8);

        tblInvoices.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleViewInvoice();
                }
            }
        });

        btnFilter.addActionListener(e -> loadInvoices());
        txtKeyword.addActionListener(e -> loadInvoices());
        btnViewPrint.addActionListener(e -> handleViewInvoice());
        btnCancelInvoice.addActionListener(e -> handleCancelInvoice());
        btnExportExcel.addActionListener(e -> handleExportExcel());

        add(topBar, BorderLayout.NORTH);
        add(new JScrollPane(tblInvoices), BorderLayout.CENTER);

        refreshData();
    }

    public void refreshData() {
        cboEmployee.removeAllItems();
        User allUser = new User();
        allUser.setId(0);
        allUser.setFullName("-- Tất cả NV --");
        allUser.setCode("ALL");
        cboEmployee.addItem(allUser);
        for (User u : authService.getAllUsers()) {
            cboEmployee.addItem(u);
        }
        loadInvoices();
    }

    private void loadInvoices() {
        try {
            LocalDate from = txtFromDate.getText().isBlank() ? null : LocalDate.parse(txtFromDate.getText().trim());
            LocalDate to = txtToDate.getText().isBlank() ? null : LocalDate.parse(txtToDate.getText().trim());
            User emp = (User) cboEmployee.getSelectedItem();
            Integer userId = (emp != null && emp.getId() > 0) ? emp.getId() : null;
            String status = (String) cboStatus.getSelectedItem();

            currentInvoices = invoiceService.searchInvoices(txtKeyword.getText().trim(), from, to, userId, status);
            invoiceModel.setRowCount(0);
            for (Invoice inv : currentInvoices) {
                invoiceModel.addRow(new Object[]{
                        inv.getInvoiceCode(),
                        FormatUtil.formatDateTime(inv.getCreatedAt()),
                        inv.getCustomerName(),
                        inv.getCustomerPhone() != null ? inv.getCustomerPhone() : "",
                        inv.getEmployeeName(),
                        FormatUtil.formatCurrency(inv.getSubtotal()),
                        FormatUtil.formatCurrency(inv.getDiscountAmount()),
                        FormatUtil.formatCurrency(inv.getVatAmount()),
                        FormatUtil.formatCurrency(inv.getTotalAmount()),
                        "CASH".equalsIgnoreCase(inv.getPaymentMethod()) ? "Tiền mặt" : "Chuyển khoản",
                        "PAID".equalsIgnoreCase(inv.getStatus()) ? "Đã thanh toán" : "ĐÃ HỦY",
                        inv.getCancelReason() != null ? inv.getCancelReason() : ""
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi lọc ngày tháng (Định dạng chuẩn yyyy-MM-dd): " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Invoice getSelectedInvoice() {
        int row = tblInvoices.getSelectedRow();
        if (row < 0 || currentInvoices == null || row >= currentInvoices.size()) return null;
        return currentInvoices.get(row);
    }

    private void handleViewInvoice() {
        Invoice sel = getSelectedInvoice();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 hóa đơn để xem chi tiết!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Invoice full = invoiceService.getInvoiceWithDetails(sel.getId());
        if (full != null) {
            new InvoicePreviewDialog(SwingUtilities.getWindowAncestor(this), full).setVisible(true);
        }
    }

    private void handleCancelInvoice() {
        if (!SessionManager.isAdmin()) {
            JOptionPane.showMessageDialog(this, "Chỉ Quản trị viên (ADMIN) mới có quyền hủy hóa đơn!", "Từ chối truy cập", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Invoice sel = getSelectedInvoice();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn cần hủy!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if ("CANCELLED".equalsIgnoreCase(sel.getStatus())) {
            JOptionPane.showMessageDialog(this, "Hóa đơn này đã ở trạng thái ĐÃ HỦY!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String reason = JOptionPane.showInputDialog(this,
                "Nhập lý do hủy hóa đơn " + sel.getInvoiceCode() + " (Tồn kho các mặt hàng sẽ được hoàn lại tự động):",
                "Khách đổi trả hàng");
        if (reason != null && !reason.trim().isEmpty()) {
            try {
                invoiceService.cancelInvoice(sel.getId(), reason.trim());
                loadInvoices();
                JOptionPane.showMessageDialog(this,
                        "Đã hủy hóa đơn " + sel.getInvoiceCode() + " và hoàn lại tồn kho sản phẩm thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi hủy hóa đơn", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleExportExcel() {
        if (currentInvoices == null || currentInvoices.isEmpty()) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Xuất Lịch sử Hóa đơn ra Excel");
        chooser.setSelectedFile(new File("LichSuHoaDon_ShopMate.xlsx"));
        chooser.setFileFilter(new FileNameExtensionFilter("Excel Workbook (*.xlsx)", "xlsx"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".xlsx")) {
                file = new File(file.getAbsolutePath() + ".xlsx");
            }
            try {
                ExcelExporter.exportInvoicesToExcel(currentInvoices, file);
                JOptionPane.showMessageDialog(this, "Đã xuất file Excel thành công:\n" + file.getAbsolutePath(),
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi xuất Excel: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
