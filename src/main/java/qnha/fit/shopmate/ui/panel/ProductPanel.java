package qnha.fit.shopmate.ui.panel;

import qnha.fit.shopmate.config.SessionManager;
import qnha.fit.shopmate.model.Category;
import qnha.fit.shopmate.model.Product;
import qnha.fit.shopmate.service.ProductService;
import qnha.fit.shopmate.ui.component.StockTableCellRenderer;
import qnha.fit.shopmate.ui.dialog.ProductDialog;
import qnha.fit.shopmate.ui.dialog.StockImportDialog;
import qnha.fit.shopmate.util.ExcelExporter;
import qnha.fit.shopmate.util.FormatUtil;
import qnha.fit.shopmate.util.ImageUtil;
import qnha.fit.shopmate.util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

public class ProductPanel extends JPanel {
    private final ProductService productService = new ProductService();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private List<Product> currentProducts;

    private final JTextField txtSearch = new JTextField(18);
    private final JComboBox<Category> cboFilterCategory = new JComboBox<>();
    private final JComboBox<String> cboSort = new JComboBox<>(new String[]{
            "Mặc định (Mã SP)",
            "Giá bán: Thấp -> Cao",
            "Giá bán: Cao -> Thấp",
            "Tồn kho: Thấp -> Cao (Cần nhập)",
            "Tồn kho: Cao -> Thấp"
    });
    private final JLabel lblPreviewImg = new JLabel();
    private final JLabel lblPreviewName = new JLabel("Chọn 1 sản phẩm để xem chi tiết");
    private final JTextArea txtPreviewDesc = new JTextArea(4, 16);

    public ProductPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(16, 18, 16, 18));

        // 1. Thanh công cụ phía trên
        JPanel topBar = new JPanel(new BorderLayout(10, 8));
        topBar.setOpaque(false);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterPanel.add(new JLabel("Tìm tên/mã:"));
        UIUtil.styleSearchField(txtSearch, "Nhập tên hoặc mã SKU...");
        txtSearch.setPreferredSize(new Dimension(210, 32));
        filterPanel.add(txtSearch);
        JButton btnSearch = new JButton("Tìm kiếm");
        UIUtil.styleSecondaryButton(btnSearch);
        filterPanel.add(btnSearch);

        filterPanel.add(new JLabel("Danh mục:"));
        filterPanel.add(cboFilterCategory);

        filterPanel.add(new JLabel("Sắp xếp:"));
        filterPanel.add(cboSort);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        JButton btnAdd = new JButton("+ Thêm SP");
        UIUtil.stylePrimaryButton(btnAdd);
        JButton btnEdit = new JButton("Sửa SP");
        UIUtil.styleSecondaryButton(btnEdit);
        JButton btnDelete = new JButton("Xóa / Ngừng KD");
        UIUtil.styleDangerButton(btnDelete);
        JButton btnImportStock = new JButton("Nhập hàng vào kho");
        UIUtil.styleSuccessButton(btnImportStock);
        JButton btnExportExcel = new JButton("Xuất Excel");
        UIUtil.styleSecondaryButton(btnExportExcel);

        boolean isAdmin = SessionManager.isAdmin();
        btnAdd.setEnabled(isAdmin);
        btnEdit.setEnabled(isAdmin);
        btnDelete.setEnabled(isAdmin);
        btnImportStock.setEnabled(isAdmin);

        actionPanel.add(btnAdd);
        actionPanel.add(btnEdit);
        actionPanel.add(btnDelete);
        actionPanel.add(btnImportStock);
        actionPanel.add(btnExportExcel);

        topBar.add(filterPanel, BorderLayout.NORTH);
        topBar.add(actionPanel, BorderLayout.SOUTH);

        // 2. Bảng sản phẩm (Highlight đỏ các dòng có tồn kho < 10)
        String[] cols = {"Mã SP", "Tên sản phẩm", "Danh mục", "Đơn vị", "Giá nhập", "Giá bán", "Tồn kho", "Trạng thái"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        UIUtil.styleTable(table);

        StockTableCellRenderer stockRenderer = new StockTableCellRenderer(6);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(stockRenderer);
        }

        // 3. Khung preview ảnh và mô tả bên phải
        JPanel previewPanel = new JPanel(new BorderLayout(8, 8));
        previewPanel.setPreferredSize(new Dimension(240, 0));
        previewPanel.setBorder(BorderFactory.createTitledBorder("Chi tiết & Ảnh Sản phẩm"));

        lblPreviewImg.setHorizontalAlignment(SwingConstants.CENTER);
        lblPreviewImg.setPreferredSize(new Dimension(160, 160));
        lblPreviewImg.setIcon(ImageUtil.createPlaceholderIcon("SP", 140, 140));

        lblPreviewName.setHorizontalAlignment(SwingConstants.CENTER);
        lblPreviewName.setFont(new Font("Segoe UI", Font.BOLD, 13));

        txtPreviewDesc.setEditable(false);
        txtPreviewDesc.setLineWrap(true);
        txtPreviewDesc.setWrapStyleWord(true);
        txtPreviewDesc.setBorder(new EmptyBorder(6, 6, 6, 6));

        JPanel noteWarn = new JPanel(new BorderLayout());
        JLabel lblLegend = new JLabel("<html><b style='color:#b91c1c;'>* Dòng tô đỏ:</b> Tồn kho &lt; 10 (Sắp hết hàng)</html>");
        lblLegend.setBorder(new EmptyBorder(6, 8, 6, 8));
        noteWarn.add(lblLegend, BorderLayout.CENTER);

        JPanel centerPreview = new JPanel(new BorderLayout(6, 6));
        centerPreview.add(lblPreviewImg, BorderLayout.NORTH);
        centerPreview.add(lblPreviewName, BorderLayout.CENTER);
        centerPreview.add(new JScrollPane(txtPreviewDesc), BorderLayout.SOUTH);

        previewPanel.add(centerPreview, BorderLayout.CENTER);
        previewPanel.add(noteWarn, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (!e.getValueIsAdjusting() && row >= 0 && currentProducts != null && row < currentProducts.size()) {
                Product p = currentProducts.get(table.convertRowIndexToModel(row));
                lblPreviewImg.setIcon(ImageUtil.loadProductImage(p.getImagePath(), p.getName(), 140, 140));
                lblPreviewName.setText("<html><div style='text-align:center;'>" + p.getName() + "</div></html>");
                txtPreviewDesc.setText("Mã SKU: " + p.getCode() + "\n"
                        + "Giá bán: " + FormatUtil.formatCurrency(p.getSellingPrice()) + "\n"
                        + "Tồn kho: " + p.getStockQuantity() + " " + p.getUnit() + "\n"
                        + "Mô tả: " + (p.getDescription() != null ? p.getDescription() : ""));
            }
        });

        // Events
        btnSearch.addActionListener(e -> loadProducts());
        txtSearch.addActionListener(e -> loadProducts());
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { loadProducts(); }
            @Override public void removeUpdate(DocumentEvent e) { loadProducts(); }
            @Override public void changedUpdate(DocumentEvent e) { loadProducts(); }
        });
        cboFilterCategory.addActionListener(e -> loadProducts());
        cboSort.addActionListener(e -> loadProducts());

        btnAdd.addActionListener(e -> openProductDialog(null));
        btnEdit.addActionListener(e -> {
            Product sel = getSelectedProduct();
            if (sel == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm cần sửa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            openProductDialog(sel);
        });
        btnDelete.addActionListener(e -> handleDelete());
        btnImportStock.addActionListener(e -> {
            StockImportDialog dlg = new StockImportDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    productService,
                    getSelectedProduct()
            );
            dlg.setVisible(true);
            if (dlg.isImported()) {
                loadProducts();
            }
        });
        btnExportExcel.addActionListener(e -> handleExportExcel());

        add(topBar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(previewPanel, BorderLayout.EAST);

        refreshData();
    }

    public void refreshData() {
        loadCategoryFilter();
        loadProducts();
    }

    private void loadCategoryFilter() {
        ActionListener[] listeners = cboFilterCategory.getActionListeners();
        for (ActionListener l : listeners) cboFilterCategory.removeActionListener(l);

        cboFilterCategory.removeAllItems();
        cboFilterCategory.addItem(new Category(0, "-- Tất cả danh mục --", "", true));
        for (Category c : productService.getAllCategories()) {
            cboFilterCategory.addItem(c);
        }

        for (ActionListener l : listeners) cboFilterCategory.addActionListener(l);
    }

    private void loadProducts() {
        String kw = txtSearch.getText().trim();
        Category cat = (Category) cboFilterCategory.getSelectedItem();
        Integer catId = (cat != null && cat.getId() > 0) ? cat.getId() : null;

        String sortCode = switch (cboSort.getSelectedIndex()) {
            case 1 -> "PRICE_ASC";
            case 2 -> "PRICE_DESC";
            case 3 -> "STOCK_ASC";
            case 4 -> "STOCK_DESC";
            default -> "DEFAULT";
        };

        currentProducts = productService.searchProducts(kw, catId, sortCode, false);
        tableModel.setRowCount(0);
        for (Product p : currentProducts) {
            tableModel.addRow(new Object[]{
                    p.getCode(),
                    p.getName(),
                    p.getCategoryName(),
                    p.getUnit(),
                    FormatUtil.formatCurrency(p.getCostPrice()),
                    FormatUtil.formatCurrency(p.getSellingPrice()),
                    p.getStockQuantity(),
                    p.isActive() ? "Đang bán" : "Ngừng KD"
            });
        }
    }

    private Product getSelectedProduct() {
        int row = table.getSelectedRow();
        if (row < 0 || currentProducts == null) return null;
        return currentProducts.get(table.convertRowIndexToModel(row));
    }

    private void openProductDialog(Product product) {
        ProductDialog dlg = new ProductDialog((Frame) SwingUtilities.getWindowAncestor(this), productService, product);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            loadProducts();
        }
    }

    private void handleDelete() {
        Product sel = getSelectedProduct();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm cần xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa / ngừng kinh doanh sản phẩm '" + sel.getName() + "'?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String msg = productService.deleteOrDiscontinueProduct(sel.getId());
                loadProducts();
                JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleExportExcel() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Xuất Danh sách Sản phẩm ra Excel");
        chooser.setSelectedFile(new File("DanhSachSanPham_ShopMate.xlsx"));
        chooser.setFileFilter(new FileNameExtensionFilter("Excel Workbook (*.xlsx)", "xlsx"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".xlsx")) {
                file = new File(file.getAbsolutePath() + ".xlsx");
            }
            try {
                ExcelExporter.exportProductsToExcel(currentProducts, file);
                JOptionPane.showMessageDialog(this, "Xuất file Excel thành công:\n" + file.getAbsolutePath(),
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi xuất Excel: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
