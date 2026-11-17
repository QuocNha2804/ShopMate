package qnha.fit.shopmate.ui.dialog;

import qnha.fit.shopmate.config.SessionManager;
import qnha.fit.shopmate.model.Product;
import qnha.fit.shopmate.model.StockImport;
import qnha.fit.shopmate.service.ProductService;
import qnha.fit.shopmate.util.FormatUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class StockImportDialog extends JDialog {
    private final ProductService productService;
    private boolean imported = false;

    private final JComboBox<Product> cboProduct = new JComboBox<>();
    private final JSpinner spnQuantity = new JSpinner(new SpinnerNumberModel(10, 1, 100000, 5));
    private final JTextField txtImportPrice = new JTextField(15);
    private final JTextField txtNote = new JTextField(25);
    private final DefaultTableModel logTableModel;

    public StockImportDialog(Frame owner, ProductService productService, Product preselectedProduct) {
        super(owner, "Nhập Hàng Vào Kho & Lịch Sử Nhập", true);
        this.productService = productService;
        setSize(780, 540);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // Form nhập hàng
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(12, 14, 6, 14),
                BorderFactory.createTitledBorder("Tạo phiếu nhập kho mới")
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        List<Product> products = productService.getAllProducts();
        for (Product p : products) {
            cboProduct.addItem(p);
        }
        cboProduct.addActionListener(e -> {
            Product sel = (Product) cboProduct.getSelectedItem();
            if (sel != null) {
                txtImportPrice.setText(sel.getCostPrice().toPlainString());
            }
        });

        if (preselectedProduct != null) {
            for (int i = 0; i < cboProduct.getItemCount(); i++) {
                if (cboProduct.getItemAt(i).getId() == preselectedProduct.getId()) {
                    cboProduct.setSelectedIndex(i);
                    break;
                }
            }
            txtImportPrice.setText(preselectedProduct.getCostPrice().toPlainString());
        } else if (cboProduct.getItemCount() > 0) {
            txtImportPrice.setText(cboProduct.getItemAt(0).getCostPrice().toPlainString());
        }

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.2;
        formPanel.add(new JLabel("Chọn sản phẩm:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.8; gbc.gridwidth = 3;
        formPanel.add(cboProduct, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.2;
        formPanel.add(new JLabel("Số lượng nhập:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.3;
        formPanel.add(spnQuantity, gbc);

        gbc.gridx = 2; gbc.weightx = 0.2;
        formPanel.add(new JLabel("Đơn giá nhập (VNĐ):"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.3;
        formPanel.add(txtImportPrice, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.2;
        formPanel.add(new JLabel("Ghi chú lô hàng:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5; gbc.gridwidth = 2;
        formPanel.add(txtNote, gbc);

        JButton btnImport = new JButton("+ Xác nhận Nhập kho");
        btnImport.setBackground(new Color(22, 163, 74));
        btnImport.setForeground(Color.WHITE);
        btnImport.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnImport.addActionListener(e -> performImport());

        gbc.gridx = 3; gbc.weightx = 0.3; gbc.gridwidth = 1;
        formPanel.add(btnImport, gbc);

        // Bảng lịch sử nhập kho
        String[] cols = {"Mã phiếu", "Thời gian", "Mã SP", "Tên sản phẩm", "SL Nhập", "Giá nhập", "Thành tiền", "Người nhập", "Ghi chú"};
        logTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tblLogs = new JTable(logTableModel);
        tblLogs.setRowHeight(28);
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(4, 14, 12, 14),
                BorderFactory.createTitledBorder("Nhật ký nhập kho gần đây")
        ));
        logPanel.add(new JScrollPane(tblLogs), BorderLayout.CENTER);

        add(formPanel, BorderLayout.NORTH);
        add(logPanel, BorderLayout.CENTER);

        loadImportLogs();
    }

    private void performImport() {
        try {
            Product selected = (Product) cboProduct.getSelectedItem();
            if (selected == null) return;
            int qty = (Integer) spnQuantity.getValue();
            BigDecimal importPrice = FormatUtil.parseMoney(txtImportPrice.getText());
            String note = txtNote.getText().trim();
            int userId = SessionManager.isLoggedIn() ? SessionManager.getCurrentUser().getId() : 1;

            productService.importStock(userId, selected.getId(), qty, importPrice, note);
            imported = true;
            txtNote.setText("");
            loadImportLogs();
            JOptionPane.showMessageDialog(this,
                    "Đã nhập thêm " + qty + " " + selected.getUnit() + " cho sản phẩm '" + selected.getName() + "' thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nhập kho", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadImportLogs() {
        logTableModel.setRowCount(0);
        List<StockImport> logs = productService.getAllStockImports();
        for (StockImport si : logs) {
            logTableModel.addRow(new Object[]{
                    si.getImportCode(),
                    FormatUtil.formatDateTime(si.getCreatedAt()),
                    si.getProductCode(),
                    si.getProductName(),
                    si.getQuantity(),
                    FormatUtil.formatCurrency(si.getImportPrice()),
                    FormatUtil.formatCurrency(si.getTotalCost()),
                    si.getEmployeeName(),
                    si.getNote()
            });
        }
    }

    public boolean isImported() {
        return imported;
    }
}
