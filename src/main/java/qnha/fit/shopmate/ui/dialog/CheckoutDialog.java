package qnha.fit.shopmate.ui.dialog;

import qnha.fit.shopmate.config.AppConfig;
import qnha.fit.shopmate.model.Invoice;
import qnha.fit.shopmate.util.FormatUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.math.BigDecimal;

public class CheckoutDialog extends JDialog {
    private final Invoice draftInvoice;
    private boolean confirmed = false;

    private final JRadioButton radCash = new JRadioButton("Tiền mặt (CASH)", true);
    private final JRadioButton radTransfer = new JRadioButton("Chuyển khoản ngân hàng (QR / TRANSFER)");
    private final JTextField txtCashReceived = new JTextField(18);
    private final JLabel lblChangeAmount = new JLabel("0 ₫");
    private final JLabel lblBankInfo = new JLabel();

    public CheckoutDialog(Frame owner, Invoice draftInvoice) {
        super(owner, "Xác nhận Thanh toán Đơn hàng", true);
        this.draftInvoice = draftInvoice;

        setSize(520, 470);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        initUI();
    }

    private void initUI() {
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(new EmptyBorder(16, 20, 10, 20));

        // Bảng tóm tắt số tiền
        JPanel summaryBox = new JPanel(new GridLayout(4, 2, 8, 8));
        summaryBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Chi tiết thanh toán"),
                new EmptyBorder(8, 12, 8, 12)
        ));

        summaryBox.add(new JLabel("Khách hàng:"));
        JLabel lblCust = new JLabel(draftInvoice.getCustomerName());
        lblCust.setFont(new Font("SansSerif", Font.BOLD, 13));
        summaryBox.add(lblCust);

        summaryBox.add(new JLabel("Tạm tính / Giảm giá:"));
        summaryBox.add(new JLabel(FormatUtil.formatCurrency(draftInvoice.getSubtotal())
                + "  (-" + FormatUtil.formatCurrency(draftInvoice.getDiscountAmount()) + ")"));

        summaryBox.add(new JLabel("Thuế VAT:"));
        summaryBox.add(new JLabel(FormatUtil.formatCurrency(draftInvoice.getVatAmount())));

        JLabel lblTotalTitle = new JLabel("TỔNG CẦN THANH TOÁN:");
        lblTotalTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        summaryBox.add(lblTotalTitle);

        JLabel lblTotalVal = new JLabel(FormatUtil.formatCurrency(draftInvoice.getTotalAmount()));
        lblTotalVal.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTotalVal.setForeground(new Color(220, 38, 38));
        summaryBox.add(lblTotalVal);

        // Phương thức & Tiền khách đưa
        JPanel paymentPanel = new JPanel(new GridBagLayout());
        paymentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Phương thức & Tiền khách đưa"),
                new EmptyBorder(8, 12, 8, 12)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        ButtonGroup grp = new ButtonGroup();
        grp.add(radCash);
        grp.add(radTransfer);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        paymentPanel.add(radCash, gbc);
        gbc.gridy = 1;
        paymentPanel.add(radTransfer, gbc);

        lblBankInfo.setText("<html><b>TK nhận:</b> " + AppConfig.getStoreBankInfo() + "</html>");
        lblBankInfo.setForeground(new Color(30, 64, 175));
        lblBankInfo.setVisible(false);
        gbc.gridy = 2;
        paymentPanel.add(lblBankInfo, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.4;
        paymentPanel.add(new JLabel("Tiền khách đưa (VNĐ):"), gbc);

        txtCashReceived.setFont(new Font("SansSerif", Font.BOLD, 15));
        txtCashReceived.setText(draftInvoice.getTotalAmount().toPlainString());
        gbc.gridx = 1; gbc.weightx = 0.6;
        paymentPanel.add(txtCashReceived, gbc);

        // Nút chọn mệnh giá nhanh
        JPanel quickCashPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JButton btnExact = new JButton("Vừa đủ");
        JButton btn100k = new JButton("100.000");
        JButton btn200k = new JButton("200.000");
        JButton btn500k = new JButton("500.000");

        btnExact.addActionListener(e -> txtCashReceived.setText(draftInvoice.getTotalAmount().toPlainString()));
        btn100k.addActionListener(e -> txtCashReceived.setText("100000"));
        btn200k.addActionListener(e -> txtCashReceived.setText("200000"));
        btn500k.addActionListener(e -> txtCashReceived.setText("500000"));

        quickCashPanel.add(btnExact);
        quickCashPanel.add(btn100k);
        quickCashPanel.add(btn200k);
        quickCashPanel.add(btn500k);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        paymentPanel.add(quickCashPanel, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 5;
        paymentPanel.add(new JLabel("Tiền thừa trả khách:"), gbc);

        lblChangeAmount.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblChangeAmount.setForeground(new Color(22, 163, 74));
        gbc.gridx = 1;
        paymentPanel.add(lblChangeAmount, gbc);

        radCash.addActionListener(e -> {
            lblBankInfo.setVisible(false);
            txtCashReceived.setEditable(true);
            recalculateChange();
        });
        radTransfer.addActionListener(e -> {
            lblBankInfo.setVisible(true);
            txtCashReceived.setText(draftInvoice.getTotalAmount().toPlainString());
            txtCashReceived.setEditable(false);
            recalculateChange();
        });

        txtCashReceived.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { recalculateChange(); }
            @Override public void removeUpdate(DocumentEvent e) { recalculateChange(); }
            @Override public void changedUpdate(DocumentEvent e) { recalculateChange(); }
        });

        content.add(summaryBox, BorderLayout.NORTH);
        content.add(paymentPanel, BorderLayout.CENTER);

        // Nút xác nhận
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        JButton btnCancel = new JButton("Quay lại");
        JButton btnConfirm = new JButton("Hoàn tất & In hóa đơn");
        btnConfirm.setBackground(new Color(22, 163, 74));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFont(new Font("SansSerif", Font.BOLD, 14));

        btnCancel.addActionListener(e -> dispose());
        btnConfirm.addActionListener(e -> handleConfirm());

        btnPanel.add(btnCancel);
        btnPanel.add(btnConfirm);

        add(content, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        recalculateChange();
    }

    private void recalculateChange() {
        BigDecimal cash = FormatUtil.parseMoney(txtCashReceived.getText());
        BigDecimal change = cash.subtract(draftInvoice.getTotalAmount());
        if (change.compareTo(BigDecimal.ZERO) < 0) {
            lblChangeAmount.setText("Còn thiếu: " + FormatUtil.formatCurrency(change.abs()));
            lblChangeAmount.setForeground(new Color(220, 38, 38));
        } else {
            lblChangeAmount.setText(FormatUtil.formatCurrency(change));
            lblChangeAmount.setForeground(new Color(22, 163, 74));
        }
    }

    private void handleConfirm() {
        BigDecimal cash = FormatUtil.parseMoney(txtCashReceived.getText());
        if (cash.compareTo(draftInvoice.getTotalAmount()) < 0) {
            JOptionPane.showMessageDialog(this, "Số tiền khách đưa chưa đủ tổng thanh toán!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        draftInvoice.setPaymentMethod(radCash.isSelected() ? "CASH" : "TRANSFER");
        draftInvoice.setCashReceived(cash);
        draftInvoice.setChangeAmount(cash.subtract(draftInvoice.getTotalAmount()));
        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
