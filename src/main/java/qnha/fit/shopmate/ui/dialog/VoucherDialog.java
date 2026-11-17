package qnha.fit.shopmate.ui.dialog;

import qnha.fit.shopmate.model.Voucher;
import qnha.fit.shopmate.service.CustomerService;
import qnha.fit.shopmate.util.FormatUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;

public class VoucherDialog extends JDialog {
    private final CustomerService customerService;
    private final Voucher voucher;
    private boolean saved = false;

    private final JTextField txtCode = new JTextField(16);
    private final JComboBox<String> cboType = new JComboBox<>(new String[]{"PERCENT", "FIXED"});
    private final JTextField txtValue = new JTextField("10", 16);
    private final JTextField txtMinOrder = new JTextField("100000", 16);
    private final JTextField txtMaxDiscount = new JTextField("50000", 16);
    private final JSpinner spnLimit = new JSpinner(new SpinnerNumberModel(100, 1, 100000, 10));
    private final JTextField txtStartDate = new JTextField(16);
    private final JTextField txtEndDate = new JTextField(16);
    private final JCheckBox chkActive = new JCheckBox("Đang kích hoạt", true);

    public VoucherDialog(Frame owner, CustomerService customerService, Voucher existingVoucher) {
        super(owner, existingVoucher == null ? "Thêm Mã giảm giá (Voucher)" : "Cập nhật Mã giảm giá", true);
        this.customerService = customerService;
        this.voucher = existingVoucher != null ? existingVoucher : new Voucher();

        setSize(480, 440);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(14, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addRow(form, gbc, row++, "Mã giảm giá (Code):", txtCode);
        addRow(form, gbc, row++, "Loại giảm giá:", cboType);
        addRow(form, gbc, row++, "Giá trị giảm (% hoặc VNĐ):", txtValue);
        addRow(form, gbc, row++, "Đơn hàng tối thiểu (VNĐ):", txtMinOrder);
        addRow(form, gbc, row++, "Giảm tối đa (VNĐ):", txtMaxDiscount);
        addRow(form, gbc, row++, "Giới hạn số lượt dùng:", spnLimit);
        addRow(form, gbc, row++, "Ngày bắt đầu (yyyy-MM-dd):", txtStartDate);
        addRow(form, gbc, row++, "Ngày kết thúc (yyyy-MM-dd):", txtEndDate);
        addRow(form, gbc, row++, "Trạng thái:", chkActive);

        if (voucher.getId() == 0) {
            txtStartDate.setText(LocalDate.now().toString());
            txtEndDate.setText(LocalDate.now().plusDays(30).toString());
        } else {
            txtCode.setText(voucher.getCode());
            cboType.setSelectedItem(voucher.getDiscountType());
            txtValue.setText(voucher.getDiscountValue().toPlainString());
            txtMinOrder.setText(voucher.getMinOrderAmount().toPlainString());
            txtMaxDiscount.setText(voucher.getMaxDiscount().toPlainString());
            spnLimit.setValue(voucher.getUsageLimit());
            txtStartDate.setText(voucher.getStartDate() != null ? voucher.getStartDate().toString() : LocalDate.now().toString());
            txtEndDate.setText(voucher.getEndDate() != null ? voucher.getEndDate().toString() : LocalDate.now().plusDays(30).toString());
            chkActive.setSelected(voucher.isActive());
        }

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        JButton btnCancel = new JButton("Hủy");
        JButton btnSave = new JButton("Lưu mã giảm giá");
        btnSave.setBackground(new Color(37, 99, 235));
        btnSave.setForeground(Color.WHITE);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> handleSave());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        add(form, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, Component comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.42;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 0.58;
        panel.add(comp, gbc);
    }

    private void handleSave() {
        try {
            voucher.setCode(txtCode.getText().trim().toUpperCase());
            voucher.setDiscountType((String) cboType.getSelectedItem());
            voucher.setDiscountValue(FormatUtil.parseMoney(txtValue.getText()));
            voucher.setMinOrderAmount(FormatUtil.parseMoney(txtMinOrder.getText()));
            voucher.setMaxDiscount(FormatUtil.parseMoney(txtMaxDiscount.getText()));
            voucher.setUsageLimit((Integer) spnLimit.getValue());
            voucher.setStartDate(LocalDate.parse(txtStartDate.getText().trim()));
            voucher.setEndDate(LocalDate.parse(txtEndDate.getText().trim()));
            voucher.setActive(chkActive.isSelected());

            customerService.saveVoucher(voucher);
            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
