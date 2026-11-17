package qnha.fit.shopmate.ui.dialog;

import qnha.fit.shopmate.model.Category;
import qnha.fit.shopmate.model.Product;
import qnha.fit.shopmate.service.ProductService;
import qnha.fit.shopmate.util.FormatUtil;
import qnha.fit.shopmate.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.util.List;

public class ProductDialog extends JDialog {
    private final ProductService productService;
    private final Product product;
    private boolean saved = false;

    private final JTextField txtCode = new JTextField(18);
    private final JTextField txtName = new JTextField(22);
    private final JComboBox<Category> cboCategory = new JComboBox<>();
    private final JTextField txtUnit = new JTextField("Cái", 12);
    private final JTextField txtCostPrice = new JTextField("0", 15);
    private final JTextField txtSellingPrice = new JTextField("0", 15);
    private final JSpinner spnStock = new JSpinner(new SpinnerNumberModel(0, 0, 1_000_000, 1));
    private final JComboBox<String> cboStatus = new JComboBox<>(new String[]{"ACTIVE", "DISCONTINUED"});
    private final JTextArea txtDescription = new JTextArea(3, 20);
    private final JLabel lblImagePreview = new JLabel();
    private String uploadedImageFileName;

    public ProductDialog(Frame owner, ProductService productService, Product existingProduct) {
        super(owner, existingProduct == null ? "Thêm Sản phẩm mới" : "Cập nhật Sản phẩm", true);
        this.productService = productService;
        this.product = existingProduct != null ? existingProduct : new Product();

        setSize(680, 520);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(12, 12));

        loadCategories();
        initUI();
        populateData();
    }

    private void loadCategories() {
        List<Category> categories = productService.getAllCategories();
        for (Category c : categories) {
            cboCategory.addItem(c);
        }
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(16, 10));
        mainPanel.setBorder(new EmptyBorder(16, 18, 10, 18));

        // Cột trái: Form nhập liệu
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addFormRow(formPanel, gbc, row++, "Mã sản phẩm (SKU):", txtCode);
        addFormRow(formPanel, gbc, row++, "Tên sản phẩm (*):", txtName);
        addFormRow(formPanel, gbc, row++, "Danh mục (*):", cboCategory);
        addFormRow(formPanel, gbc, row++, "Đơn vị tính:", txtUnit);
        addFormRow(formPanel, gbc, row++, "Giá nhập (VNĐ):", txtCostPrice);
        addFormRow(formPanel, gbc, row++, "Giá bán (VNĐ):", txtSellingPrice);
        addFormRow(formPanel, gbc, row++, "Số lượng tồn kho:", spnStock);
        addFormRow(formPanel, gbc, row++, "Trạng thái:", cboStatus);

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(new JLabel("Mô tả sản phẩm:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(txtDescription), gbc);

        // Cột phải: Ảnh sản phẩm & nút Upload
        JPanel imagePanel = new JPanel(new BorderLayout(8, 8));
        imagePanel.setPreferredSize(new Dimension(190, 260));
        imagePanel.setBorder(BorderFactory.createTitledBorder("Ảnh sản phẩm"));

        lblImagePreview.setHorizontalAlignment(SwingConstants.CENTER);
        lblImagePreview.setPreferredSize(new Dimension(150, 150));

        JButton btnUpload = new JButton("Chọn ảnh (Upload)...");
        btnUpload.addActionListener(e -> chooseImageFile());

        imagePanel.add(lblImagePreview, BorderLayout.CENTER);
        imagePanel.add(btnUpload, BorderLayout.SOUTH);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(imagePanel, BorderLayout.EAST);

        // Nút Lưu / Hủy
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        JButton btnCancel = new JButton("Hủy bỏ");
        JButton btnSave = new JButton("Lưu sản phẩm");
        btnSave.setBackground(new Color(37, 99, 235));
        btnSave.setForeground(Color.WHITE);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> saveProduct());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        add(mainPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, Component comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.32;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.68;
        panel.add(comp, gbc);
    }

    private void populateData() {
        if (product.getId() == 0) {
            txtCode.setText(productService.generateNextProductCode());
            lblImagePreview.setIcon(ImageUtil.createPlaceholderIcon("SP", 130, 130));
        } else {
            txtCode.setText(product.getCode());
            txtName.setText(product.getName());
            txtUnit.setText(product.getUnit());
            txtCostPrice.setText(product.getCostPrice().toPlainString());
            txtSellingPrice.setText(product.getSellingPrice().toPlainString());
            spnStock.setValue(product.getStockQuantity());
            cboStatus.setSelectedItem(product.getStatus());
            txtDescription.setText(product.getDescription());
            uploadedImageFileName = product.getImagePath();

            for (int i = 0; i < cboCategory.getItemCount(); i++) {
                if (cboCategory.getItemAt(i).getId() == product.getCategoryId()) {
                    cboCategory.setSelectedIndex(i);
                    break;
                }
            }
            lblImagePreview.setIcon(ImageUtil.loadProductImage(uploadedImageFileName, product.getName(), 130, 130));
        }
    }

    private void chooseImageFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn hình ảnh sản phẩm");
        chooser.setFileFilter(new FileNameExtensionFilter("Hình ảnh (*.png, *.jpg, *.jpeg)", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            try {
                uploadedImageFileName = ImageUtil.copyUploadedImage(selectedFile, txtCode.getText());
                lblImagePreview.setIcon(ImageUtil.loadProductImage(uploadedImageFileName, txtName.getText(), 130, 130));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi tải ảnh: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveProduct() {
        try {
            Category selectedCat = (Category) cboCategory.getSelectedItem();
            if (selectedCat == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng tạo và chọn danh mục trước!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            BigDecimal costPrice = FormatUtil.parseMoney(txtCostPrice.getText());
            BigDecimal sellingPrice = FormatUtil.parseMoney(txtSellingPrice.getText());

            product.setCode(txtCode.getText().trim().toUpperCase());
            product.setName(txtName.getText().trim());
            product.setCategoryId(selectedCat.getId());
            product.setUnit(txtUnit.getText().trim());
            product.setCostPrice(costPrice);
            product.setSellingPrice(sellingPrice);
            product.setStockQuantity((Integer) spnStock.getValue());
            product.setStatus((String) cboStatus.getSelectedItem());
            product.setDescription(txtDescription.getText().trim());
            product.setImagePath(uploadedImageFileName);

            productService.saveProduct(product);
            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
