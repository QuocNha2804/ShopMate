package qnha.fit.shopmate.ui.panel;

import qnha.fit.shopmate.config.SessionManager;
import qnha.fit.shopmate.model.Category;
import qnha.fit.shopmate.service.ProductService;
import qnha.fit.shopmate.util.FormatUtil;
import qnha.fit.shopmate.util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CategoryPanel extends JPanel {
    private final ProductService productService = new ProductService();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private List<Category> categories;

    private final JTextField txtName = new JTextField(20);
    private final JTextField txtDescription = new JTextField(25);
    private final JCheckBox chkActive = new JCheckBox("Đang hoạt động", true);
    private Category selectedCategory = null;

    public CategoryPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBorder(BorderFactory.createTitledBorder("Thông tin Danh mục sản phẩm"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formCard.add(new JLabel("Tên danh mục (*):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.35;
        UIUtil.styleSearchField(txtName, "Nhập tên danh mục...");
        formCard.add(txtName, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        formCard.add(new JLabel("Mô tả:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.45;
        UIUtil.styleSearchField(txtDescription, "Mô tả ngắn...");
        formCard.add(txtDescription, gbc);

        gbc.gridx = 4; gbc.weightx = 0.2;
        formCard.add(chkActive, gbc);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        JButton btnAdd = new JButton("+ Thêm mới");
        UIUtil.stylePrimaryButton(btnAdd);
        JButton btnUpdate = new JButton("Cập nhật");
        UIUtil.styleSecondaryButton(btnUpdate);
        JButton btnDelete = new JButton("Xóa danh mục");
        UIUtil.styleDangerButton(btnDelete);
        JButton btnClear = new JButton("Làm mới form");
        UIUtil.styleSecondaryButton(btnClear);

        boolean isAdmin = SessionManager.isAdmin();
        btnAdd.setEnabled(isAdmin);
        btnUpdate.setEnabled(isAdmin);
        btnDelete.setEnabled(isAdmin);

        btnBar.add(btnAdd);
        btnBar.add(btnUpdate);
        btnBar.add(btnDelete);
        btnBar.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 5;
        formCard.add(btnBar, gbc);

        String[] cols = {"ID", "Tên danh mục", "Mô tả", "Số lượng SP", "Trạng thái", "Ngày tạo"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIUtil.styleTable(table);
        UIUtil.alignColumnCenter(table, 0, 3, 4);

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (!e.getValueIsAdjusting() && row >= 0 && categories != null && row < categories.size()) {
                selectedCategory = categories.get(row);
                txtName.setText(selectedCategory.getName());
                txtDescription.setText(selectedCategory.getDescription());
                chkActive.setSelected(selectedCategory.isActive());
            }
        });

        btnAdd.addActionListener(e -> handleAdd());
        btnUpdate.addActionListener(e -> handleUpdate());
        btnDelete.addActionListener(e -> handleDelete());
        btnClear.addActionListener(e -> clearForm());

        add(formCard, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        refreshData();
    }

    public void refreshData() {
        categories = productService.getAllCategories();
        tableModel.setRowCount(0);
        for (Category c : categories) {
            tableModel.addRow(new Object[]{
                    c.getId(),
                    c.getName(),
                    c.getDescription(),
                    c.getProductCount(),
                    c.isActive() ? "Đang hoạt động" : "Tạm ẩn",
                    FormatUtil.formatDateTime(c.getCreatedAt())
            });
        }
    }

    private void handleAdd() {
        try {
            Category c = new Category();
            c.setName(txtName.getText().trim());
            c.setDescription(txtDescription.getText().trim());
            c.setActive(chkActive.isSelected());
            productService.saveCategory(c);
            clearForm();
            refreshData();
            JOptionPane.showMessageDialog(this, "Thêm danh mục thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdate() {
        if (selectedCategory == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 danh mục trên bảng để cập nhật!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            selectedCategory.setName(txtName.getText().trim());
            selectedCategory.setDescription(txtDescription.getText().trim());
            selectedCategory.setActive(chkActive.isSelected());
            productService.saveCategory(selectedCategory);
            clearForm();
            refreshData();
            JOptionPane.showMessageDialog(this, "Cập nhật danh mục thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDelete() {
        if (selectedCategory == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn danh mục cần xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa danh mục '" + selectedCategory.getName() + "'?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                productService.deleteCategory(selectedCategory.getId());
                clearForm();
                refreshData();
                JOptionPane.showMessageDialog(this, "Đã xóa danh mục thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể xóa danh mục", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void clearForm() {
        selectedCategory = null;
        txtName.setText("");
        txtDescription.setText("");
        chkActive.setSelected(true);
        table.clearSelection();
    }
}
