package qnha.fit.shopmate.ui.dialog;

import qnha.fit.shopmate.config.AppConfig;
import qnha.fit.shopmate.model.Invoice;
import qnha.fit.shopmate.model.InvoiceDetail;
import qnha.fit.shopmate.util.FormatUtil;
import qnha.fit.shopmate.util.PDFExporter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class InvoicePreviewDialog extends JDialog {
    private final Invoice invoice;
    private final JEditorPane receiptPane = new JEditorPane();

    public InvoicePreviewDialog(Window owner, Invoice invoice) {
        super(owner, "Chi tiết & In Hóa đơn - " + invoice.getInvoiceCode(), ModalityType.APPLICATION_MODAL);
        this.invoice = invoice;
        setSize(480, 650);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        receiptPane.setContentType("text/html");
        receiptPane.setEditable(false);
        receiptPane.setText(buildReceiptHtml(invoice));
        receiptPane.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(receiptPane);
        scrollPane.setBorder(new EmptyBorder(12, 16, 6, 16));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        JButton btnPrint = new JButton("Gửi Máy In (Print)");
        JButton btnExportPdf = new JButton("Xuất PDF (iText)");
        btnExportPdf.setBackground(new Color(37, 99, 235));
        btnExportPdf.setForeground(Color.WHITE);
        JButton btnClose = new JButton("Đóng");

        btnPrint.addActionListener(e -> handleSystemPrint());
        btnExportPdf.addActionListener(e -> handleExportPdf());
        btnClose.addActionListener(e -> dispose());

        btnPanel.add(btnPrint);
        btnPanel.add(btnExportPdf);
        btnPanel.add(btnClose);

        add(scrollPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private String buildReceiptHtml(Invoice inv) {
        StringBuilder rows = new StringBuilder();
        for (InvoiceDetail d : inv.getDetails()) {
            rows.append(String.format("""
                    <tr>
                        <td style='padding:4px 0;'>%s</td>
                        <td style='text-align:center;'>%d</td>
                        <td style='text-align:right;'>%s</td>
                        <td style='text-align:right;font-weight:bold;'>%s</td>
                    </tr>
                    """,
                    d.getProductName(),
                    d.getQuantity(),
                    FormatUtil.formatNumber(d.getUnitPrice()),
                    FormatUtil.formatNumber(d.getLineTotal())
            ));
        }

        String statusBadge = "CANCELLED".equalsIgnoreCase(inv.getStatus())
                ? "<div style='color:#dc2626;font-weight:bold;text-align:center;'>[ĐÃ HỦY: " + (inv.getCancelReason() != null ? inv.getCancelReason() : "") + "]</div>"
                : "";

        return String.format("""
                <html>
                <body style='font-family:SansSerif; font-size:11px; padding:12px; color:#1e293b;'>
                    <div style='text-align:center;'>
                        <h2 style='margin:0;'>%s</h2>
                        <div>%s</div>
                        <div>SĐT: %s</div>
                        <hr style='border:1px dashed #94a3b8;'/>
                        <h3 style='margin:6px 0;'>HÓA ĐƠN BÁN HÀNG</h3>
                        %s
                    </div>
                    <table width='100%%' style='font-size:11px;'>
                        <tr><td><b>Số HĐ:</b> %s</td><td style='text-align:right;'><b>Ngày:</b> %s</td></tr>
                        <tr><td><b>Thu ngân:</b> %s</td><td style='text-align:right;'><b>TT:</b> %s</td></tr>
                        <tr><td colspan='2'><b>Khách hàng:</b> %s %s</td></tr>
                    </table>
                    <hr style='border:1px dashed #94a3b8;'/>
                    <table width='100%%' style='font-size:11px; border-collapse:collapse;'>
                        <tr style='border-bottom:1px solid #cbd5e1;'>
                            <th style='text-align:left;'>Mặt hàng</th>
                            <th style='text-align:center;'>SL</th>
                            <th style='text-align:right;'>Đơn giá</th>
                            <th style='text-align:right;'>T.Tiền</th>
                        </tr>
                        %s
                    </table>
                    <hr style='border:1px dashed #94a3b8;'/>
                    <table width='100%%' style='font-size:11px;'>
                        <tr><td>Tạm tính:</td><td style='text-align:right;'>%s</td></tr>
                        <tr><td>Giảm giá:</td><td style='text-align:right;'>-%s</td></tr>
                        <tr><td>Thuế VAT:</td><td style='text-align:right;'>%s</td></tr>
                        <tr style='font-size:13px;font-weight:bold;color:#1d4ed8;'>
                            <td>TỔNG CỘNG:</td><td style='text-align:right;'>%s</td>
                        </tr>
                        <tr><td>Tiền khách đưa:</td><td style='text-align:right;'>%s</td></tr>
                        <tr><td><b>Tiền thừa trả khách:</b></td><td style='text-align:right;'><b>%s</b></td></tr>
                    </table>
                    <hr style='border:1px dashed #94a3b8;'/>
                    <div style='text-align:center; font-style:italic; margin-top:8px;'>
                        Cảm ơn Quý khách & Hẹn gặp lại!
                    </div>
                </body>
                </html>
                """,
                AppConfig.getStoreName(),
                AppConfig.getStoreAddress(),
                AppConfig.getStorePhone(),
                statusBadge,
                inv.getInvoiceCode(),
                FormatUtil.formatDateTime(inv.getCreatedAt()),
                inv.getEmployeeName() != null ? inv.getEmployeeName() : "NV #" + inv.getUserId(),
                "CASH".equalsIgnoreCase(inv.getPaymentMethod()) ? "Tiền mặt" : "Chuyển khoản",
                inv.getCustomerName(),
                inv.getCustomerPhone() != null && !inv.getCustomerPhone().isBlank() ? "(" + inv.getCustomerPhone() + ")" : "",
                rows,
                FormatUtil.formatCurrency(inv.getSubtotal()),
                FormatUtil.formatCurrency(inv.getDiscountAmount()),
                FormatUtil.formatCurrency(inv.getVatAmount()),
                FormatUtil.formatCurrency(inv.getTotalAmount()),
                FormatUtil.formatCurrency(inv.getCashReceived()),
                FormatUtil.formatCurrency(inv.getChangeAmount())
        );
    }

    private void handleSystemPrint() {
        try {
            boolean done = receiptPane.print();
            if (done) {
                JOptionPane.showMessageDialog(this, "Đã gửi lệnh in hóa đơn thành công!", "In hóa đơn", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi máy in: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleExportPdf() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu Hóa đơn PDF");
        chooser.setSelectedFile(new File("HoaDon_" + invoice.getInvoiceCode() + ".pdf"));
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Document (*.pdf)", "pdf"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getAbsolutePath() + ".pdf");
            }
            try {
                PDFExporter.exportInvoiceToPdf(invoice, file);
                JOptionPane.showMessageDialog(this, "Đã xuất hóa đơn PDF tại:\n" + file.getAbsolutePath(),
                        "Xuất PDF thành công", JOptionPane.INFORMATION_MESSAGE);
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi xuất PDF: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
