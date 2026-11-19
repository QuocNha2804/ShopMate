package qnha.fit.shopmate.ui.panel;

import qnha.fit.shopmate.config.SessionManager;
import qnha.fit.shopmate.model.Voucher;
import qnha.fit.shopmate.service.CustomerService;
import qnha.fit.shopmate.ui.dialog.VoucherDialog;
import qnha.fit.shopmate.util.FormatUtil;
import qnha.fit.shopmate.util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VoucherPanel extends JPanel {
    private final CustomerService customerService = new CustomerService();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private List<Voucher> vouchers;

    public VoucherPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel topBar = new JPanel(new BorderLayout());
        JLabel lblTitle = new JLabel("Quản lý Chương trình Khuyến mãi & Mã giảm giá (Voucher)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btnAdd = new JButton("+ Thêm Mã giảm giá");
        UIUtil.stylePrimaryButton(btnAdd);
        JButton btnEdit = new JButton("Sửa Mã");
        UIUtil.styleSecondaryButton(btnEdit);
        JButton btnDelete = new JButton("Xóa");
        UIUtil.styleDangerButton(btnDelete);

        boolean isAdmin = SessionManager.isAdmin();
        btnAdd.setEnabled(isAdmin);
        btnEdit.setEnabled(isAdmin);
        btnDelete.setEnabled(isAdmin);

        actions.add(btnAdd);
        actions.add(btnEdit);
        actions.add(btnDelete);

        topBar.add(lblTitle, BorderLayout.WEST);
        topBar.add(actions, BorderLayout.EAST);

        String[] cols = {"Mã Code", "Loại giảm", "Giá trị", "Đơn tối thiểu", "Giảm tối đa", "Đã dùng / Giới hạn", "Từ ngày", "Đến ngày", "Trạng thái"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIUtil.styleTable(table);
        UIUtil.alignColumnCenter(table, 0, 1, 5, 6, 7, 8);
        UIUtil.alignColumnRight(table, 2, 3, 4);

        btnAdd.addActionListener(e -> openDialog(null));
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || vouchers == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn mã giảm giá cần sửa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            openDialog(vouchers.get(row));
        });
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || vouchers == null) return;
            Voucher v = vouchers.get(row);
            int cf = JOptionPane.showConfirmDialog(this, "Xóa mã giảm giá '" + v.getCode() + "'?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (cf == JOptionPane.YES_OPTION) {
                customerService.deleteVoucher(v.getId());
                refreshData();
            }
        });

        add(topBar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        refreshData();
    }

    public void refreshData() {
        vouchers = customerService.getAllVouchers();
        tableModel.setRowCount(0);
        for (Voucher v : vouchers) {
            String valStr = "PERCENT".equalsIgnoreCase(v.getDiscountType())
                    ? v.getDiscountValue().stripTrailingZeros().toPlainString() + " %"
                    : FormatUtil.formatCurrency(v.getDiscountValue());
            tableModel.addRow(new Object[]{
                    v.getCode(),
                    "PERCENT".equalsIgnoreCase(v.getDiscountType()) ? "Theo %" : "Số tiền cố định",
                    valStr,
                    FormatUtil.formatCurrency(v.getMinOrderAmount()),
                    FormatUtil.formatCurrency(v.getMaxDiscount()),
                    v.getUsedCount() + " / " + v.getUsageLimit(),
                    FormatUtil.formatDate(v.getStartDate()),
                    FormatUtil.formatDate(v.getEndDate()),
                    v.isActive() ? "Đang hoạt động" : "Tạm ngưng"
            });
        }
    }

    private void openDialog(Voucher v) {
        VoucherDialog dlg = new VoucherDialog((Frame) SwingUtilities.getWindowAncestor(this), customerService, v);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            refreshData();
        }
    }
}
