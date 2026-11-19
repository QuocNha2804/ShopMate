package qnha.fit.shopmate.ui.panel;

import qnha.fit.shopmate.config.AppConfig;
import qnha.fit.shopmate.config.SessionManager;
import qnha.fit.shopmate.model.*;
import qnha.fit.shopmate.service.CustomerService;
import qnha.fit.shopmate.service.InvoiceService;
import qnha.fit.shopmate.service.ProductService;
import qnha.fit.shopmate.ui.component.ProductCard;
import qnha.fit.shopmate.ui.dialog.CheckoutDialog;
import qnha.fit.shopmate.ui.dialog.CustomerDialog;
import qnha.fit.shopmate.ui.dialog.InvoicePreviewDialog;
import qnha.fit.shopmate.util.FormatUtil;
import qnha.fit.shopmate.util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class POSPanel extends JPanel {
    private final ProductService productService = new ProductService();
    private final CustomerService customerService = new CustomerService();
    private final InvoiceService invoiceService = new InvoiceService();

    private final JTextField txtSearchProduct = new JTextField(18);
    private final JComboBox<Category> cboCategory = new JComboBox<>();
    private final JPanel productGridPanel = new JPanel(new GridLayout(0, 3, 12, 12));

    private final List<CartItem> cartItems = new ArrayList<>();
    private final DefaultTableModel cartTableModel;
    private final JTable tblCart;

    private Customer selectedCustomer = null;
    private final JTextField txtCustomerPhone = new JTextField(13);
    private final JLabel lblCustomerInfo = new JLabel("Khách lẻ (Chưa chọn thành viên)");

    private Voucher appliedVoucher = null;
    private final JTextField txtVoucherCode = new JTextField(10);
    private final JComboBox<String> cboManualDiscountType = new JComboBox<>(new String[]{"Mã Voucher", "Giảm %", "Giảm Tiền (VNĐ)"});
    private final JCheckBox chkApplyVat = new JCheckBox("Tính VAT", true);

    private final JLabel lblSubtotal = new JLabel("0 ₫");
    private final JLabel lblDiscount = new JLabel("-0 ₫");
    private final JLabel lblVat = new JLabel("0 ₫");
    private final JLabel lblTotalAmount = new JLabel("0 ₫");

    public POSPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(12, 14, 12, 14));

        // ================= CỘT TRÁI: DANH SÁCH SẢN PHẨM =================
        JPanel leftPanel = new JPanel(new BorderLayout(8, 8));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Danh mục Mặt hàng (F2: Tìm / Quét mã SKU)"));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        UIUtil.styleSearchField(txtSearchProduct, "Tìm tên hoặc quét mã SKU + Enter...");
        txtSearchProduct.setPreferredSize(new Dimension(230, 34));
        searchBar.add(new JLabel("Tìm SP:"));
        searchBar.add(txtSearchProduct);
        JButton btnSearch = new JButton("Tìm");
        UIUtil.stylePrimaryButton(btnSearch);
        searchBar.add(btnSearch);
        searchBar.add(new JLabel("Danh mục:"));
        searchBar.add(cboCategory);

        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setBorder(new EmptyBorder(8, 8, 8, 8));
        gridWrapper.add(productGridPanel, BorderLayout.NORTH);

        JScrollPane productScroll = new JScrollPane(gridWrapper);
        productScroll.getVerticalScrollBar().setUnitIncrement(18);
        productScroll.setBorder(null);

        leftPanel.add(searchBar, BorderLayout.NORTH);
        leftPanel.add(productScroll, BorderLayout.CENTER);

        // ================= CỘT PHẢI: GIỎ HÀNG & THANH TOÁN =================
        JPanel rightPanel = new JPanel(new BorderLayout(8, 8));
        rightPanel.setPreferredSize(new Dimension(500, 0));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Giỏ hàng & Thanh toán (Double-click dòng để nhập SL)"));

        String[] cartCols = {"Mã SP", "Tên mặt hàng", "SL", "Đơn giá", "Thành tiền"};
        cartTableModel = new DefaultTableModel(cartCols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tblCart = new JTable(cartTableModel);
        UIUtil.styleTable(tblCart);
        UIUtil.alignColumnCenter(tblCart, 0, 2);
        UIUtil.alignColumnRight(tblCart, 3, 4);
        tblCart.getColumnModel().getColumn(0).setPreferredWidth(62);
        tblCart.getColumnModel().getColumn(1).setPreferredWidth(165);
        tblCart.getColumnModel().getColumn(2).setPreferredWidth(45);

        tblCart.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblCart.getSelectedRow() >= 0) {
                    promptQuantityForSelectedRow();
                }
            }
        });

        JPanel cartActionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton btnPlus = new JButton("+ Tăng SL");
        JButton btnMinus = new JButton("- Giảm SL");
        JButton btnSetQty = new JButton("Nhập SL...");
        JButton btnRemoveRow = new JButton("Xóa món");
        UIUtil.styleSecondaryButton(btnPlus);
        UIUtil.styleSecondaryButton(btnMinus);
        UIUtil.styleSecondaryButton(btnSetQty);
        UIUtil.styleDangerButton(btnRemoveRow);

        cartActionPanel.add(btnPlus);
        cartActionPanel.add(btnMinus);
        cartActionPanel.add(btnSetQty);
        cartActionPanel.add(btnRemoveRow);

        JPanel cartCenterBox = new JPanel(new BorderLayout(4, 4));
        cartCenterBox.add(new JScrollPane(tblCart), BorderLayout.CENTER);
        cartCenterBox.add(cartActionPanel, BorderLayout.SOUTH);

        JPanel checkoutBox = new JPanel(new BorderLayout(6, 6));

        JPanel customerPanel = new JPanel(new GridBagLayout());
        customerPanel.setBorder(BorderFactory.createTitledBorder("Khách hàng"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        customerPanel.add(new JLabel("SĐT:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        UIUtil.styleSearchField(txtCustomerPhone, "Nhập SĐT khách...");
        customerPanel.add(txtCustomerPhone, gbc);

        JButton btnFindCust = new JButton("Tìm KH");
        JButton btnNewCust = new JButton("+ Mới");
        JButton btnRetailCust = new JButton("Khách lẻ");
        UIUtil.styleSecondaryButton(btnFindCust);
        UIUtil.styleSecondaryButton(btnNewCust);
        UIUtil.styleSecondaryButton(btnRetailCust);

        gbc.gridx = 2; gbc.weightx = 0;
        customerPanel.add(btnFindCust, gbc);
        gbc.gridx = 3;
        customerPanel.add(btnNewCust, gbc);
        gbc.gridx = 4;
        customerPanel.add(btnRetailCust, gbc);

        lblCustomerInfo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCustomerInfo.setForeground(UIUtil.PRIMARY);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 5;
        customerPanel.add(lblCustomerInfo, gbc);

        JPanel summaryPanel = new JPanel(new GridBagLayout());
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Khuyến mãi & Tổng thanh toán"));
        GridBagConstraints sgbc = new GridBagConstraints();
        sgbc.insets = new Insets(4, 6, 4, 6);
        sgbc.fill = GridBagConstraints.HORIZONTAL;

        sgbc.gridx = 0; sgbc.gridy = 0;
        summaryPanel.add(cboManualDiscountType, sgbc);
        sgbc.gridx = 1; sgbc.weightx = 1.0;
        UIUtil.styleSearchField(txtVoucherCode, "Mã / Giá trị...");
        summaryPanel.add(txtVoucherCode, sgbc);
        JButton btnApplyDiscount = new JButton("Áp dụng");
        JButton btnClearDiscount = new JButton("Bỏ");
        UIUtil.styleSecondaryButton(btnApplyDiscount);
        UIUtil.styleSecondaryButton(btnClearDiscount);
        sgbc.gridx = 2; sgbc.weightx = 0;
        summaryPanel.add(btnApplyDiscount, sgbc);
        sgbc.gridx = 3;
        summaryPanel.add(btnClearDiscount, sgbc);

        chkApplyVat.setText("Tính thuế VAT (" + AppConfig.getVatPercent() + "%)");
        sgbc.gridx = 0; sgbc.gridy = 1; sgbc.gridwidth = 4;
        summaryPanel.add(chkApplyVat, sgbc);

        sgbc.gridwidth = 2;
        sgbc.gridx = 0; sgbc.gridy = 2;
        summaryPanel.add(new JLabel("Tạm tính:"), sgbc);
        lblSubtotal.setHorizontalAlignment(SwingConstants.RIGHT);
        lblSubtotal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sgbc.gridx = 2;
        summaryPanel.add(lblSubtotal, sgbc);

        sgbc.gridx = 0; sgbc.gridy = 3;
        summaryPanel.add(new JLabel("Giảm giá:"), sgbc);
        lblDiscount.setHorizontalAlignment(SwingConstants.RIGHT);
        lblDiscount.setForeground(UIUtil.SUCCESS);
        lblDiscount.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sgbc.gridx = 2;
        summaryPanel.add(lblDiscount, sgbc);

        sgbc.gridx = 0; sgbc.gridy = 4;
        summaryPanel.add(new JLabel("Thuế VAT:"), sgbc);
        lblVat.setHorizontalAlignment(SwingConstants.RIGHT);
        sgbc.gridx = 2;
        summaryPanel.add(lblVat, sgbc);

        JLabel lblTotalTitle = new JLabel("THÀNH TIỀN:");
        lblTotalTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sgbc.gridx = 0; sgbc.gridy = 5;
        summaryPanel.add(lblTotalTitle, sgbc);

        lblTotalAmount.setHorizontalAlignment(SwingConstants.RIGHT);
        lblTotalAmount.setFont(new Font("Segoe UI", Font.BOLD, 21));
        lblTotalAmount.setForeground(UIUtil.DANGER);
        sgbc.gridx = 2;
        summaryPanel.add(lblTotalAmount, sgbc);

        JPanel bottomButtons = new JPanel(new GridLayout(1, 2, 10, 0));
        bottomButtons.setBorder(new EmptyBorder(6, 0, 0, 0));
        JButton btnCancelOrder = new JButton("HỦY ĐƠN HÀNG");
        UIUtil.styleDangerButton(btnCancelOrder);
        btnCancelOrder.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancelOrder.setPreferredSize(new Dimension(0, 46));

        JButton btnCheckout = new JButton("THANH TOÁN (F9)");
        UIUtil.styleSuccessButton(btnCheckout);
        btnCheckout.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnCheckout.setPreferredSize(new Dimension(0, 46));

        bottomButtons.add(btnCancelOrder);
        bottomButtons.add(btnCheckout);

        checkoutBox.add(customerPanel, BorderLayout.NORTH);
        checkoutBox.add(summaryPanel, BorderLayout.CENTER);
        checkoutBox.add(bottomButtons, BorderLayout.SOUTH);

        rightPanel.add(cartCenterBox, BorderLayout.CENTER);
        rightPanel.add(checkoutBox, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setResizeWeight(0.60);
        add(splitPane, BorderLayout.CENTER);

        // ================= SỰ KIỆN & PHÍM TẮT =================
        btnSearch.addActionListener(e -> loadProductGrid());
        txtSearchProduct.addActionListener(e -> handleBarcodeOrEnterSearch());
        txtSearchProduct.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { loadProductGrid(); }
            @Override public void removeUpdate(DocumentEvent e) { loadProductGrid(); }
            @Override public void changedUpdate(DocumentEvent e) { loadProductGrid(); }
        });
        cboCategory.addActionListener(e -> loadProductGrid());

        btnPlus.addActionListener(e -> adjustSelectedQuantity(1));
        btnMinus.addActionListener(e -> adjustSelectedQuantity(-1));
        btnSetQty.addActionListener(e -> promptQuantityForSelectedRow());
        btnRemoveRow.addActionListener(e -> removeSelectedRow());

        btnFindCust.addActionListener(e -> findCustomerByPhone());
        txtCustomerPhone.addActionListener(e -> findCustomerByPhone());
        btnNewCust.addActionListener(e -> openNewCustomerDialog());
        btnRetailCust.addActionListener(e -> {
            selectedCustomer = null;
            txtCustomerPhone.setText("");
            lblCustomerInfo.setText("Khách lẻ (Chưa chọn thành viên)");
        });

        btnApplyDiscount.addActionListener(e -> handleApplyDiscount());
        btnClearDiscount.addActionListener(e -> {
            appliedVoucher = null;
            txtVoucherCode.setText("");
            updateCartTotals();
        });
        chkApplyVat.addActionListener(e -> updateCartTotals());

        btnCancelOrder.addActionListener(e -> handleCancelOrder());
        btnCheckout.addActionListener(e -> handleCheckout());

        registerKeyboardShortcuts();
        refreshData();
    }

    private void registerKeyboardShortcuts() {
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "focusSearch");
        am.put("focusSearch", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                txtSearchProduct.requestFocusInWindow();
                txtSearchProduct.selectAll();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0), "checkout");
        am.put("checkout", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                handleCheckout();
            }
        });
    }

    private void handleBarcodeOrEnterSearch() {
        String kw = txtSearchProduct.getText().trim();
        if (!kw.isEmpty()) {
            List<Product> matches = productService.searchProducts(kw, null, "DEFAULT", true);
            for (Product p : matches) {
                if (p.getCode().equalsIgnoreCase(kw)) {
                    addProductToCart(p);
                    txtSearchProduct.setText("");
                    return;
                }
            }
        }
        loadProductGrid();
    }

    public void refreshData() {
        chkApplyVat.setText("Tính thuế VAT (" + AppConfig.getVatPercent() + "%)");
        loadCategoryCombo();
        loadProductGrid();
        updateCartTotals();
    }

    private void loadCategoryCombo() {
        ActionListener[] listeners = cboCategory.getActionListeners();
        for (ActionListener l : listeners) cboCategory.removeActionListener(l);

        cboCategory.removeAllItems();
        cboCategory.addItem(new Category(0, "-- Tất cả --", "", true));
        for (Category c : productService.getActiveCategories()) {
            cboCategory.addItem(c);
        }

        for (ActionListener l : listeners) cboCategory.addActionListener(l);
    }

    private void loadProductGrid() {
        productGridPanel.removeAll();
        String kw = txtSearchProduct.getText().trim();
        Category cat = (Category) cboCategory.getSelectedItem();
        Integer catId = (cat != null && cat.getId() > 0) ? cat.getId() : null;

        List<Product> products = productService.searchProducts(kw, catId, "DEFAULT", true);
        for (Product p : products) {
            ProductCard card = new ProductCard(p, this::addProductToCart);
            productGridPanel.add(card);
        }
        productGridPanel.revalidate();
        productGridPanel.repaint();
    }

    private void addProductToCart(Product product) {
        if (product.isOutOfStock()) {
            JOptionPane.showMessageDialog(this, "Sản phẩm '" + product.getName() + "' đã hết hàng!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        for (CartItem item : cartItems) {
            if (item.getProduct().getId() == product.getId()) {
                if (item.getQuantity() + 1 > product.getStockQuantity()) {
                    JOptionPane.showMessageDialog(this,
                            "Số lượng trong giỏ đã đạt tối đa tồn kho (" + product.getStockQuantity() + ")!",
                            "Cảnh báo tồn kho", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                item.setQuantity(item.getQuantity() + 1);
                refreshCartTable();
                return;
            }
        }
        cartItems.add(new CartItem(product, 1));
        refreshCartTable();
    }

    private void adjustSelectedQuantity(int delta) {
        int row = tblCart.getSelectedRow();
        if (row < 0 || row >= cartItems.size()) return;
        CartItem item = cartItems.get(row);
        int newQty = item.getQuantity() + delta;
        if (newQty <= 0) {
            cartItems.remove(row);
        } else if (newQty > item.getProduct().getStockQuantity()) {
            JOptionPane.showMessageDialog(this,
                    "Không vượt quá tồn kho hiện tại (" + item.getProduct().getStockQuantity() + ")!",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        } else {
            item.setQuantity(newQty);
        }
        refreshCartTable();
        if (row < cartItems.size()) {
            tblCart.setRowSelectionInterval(row, row);
        }
    }

    private void promptQuantityForSelectedRow() {
        int row = tblCart.getSelectedRow();
        if (row < 0 || row >= cartItems.size()) return;
        CartItem item = cartItems.get(row);
        String input = JOptionPane.showInputDialog(this,
                "Nhập số lượng cho '" + item.getProduct().getName() + "' (Tồn kho: " + item.getProduct().getStockQuantity() + "):",
                item.getQuantity());
        if (input != null && !input.isBlank()) {
            try {
                int qty = Integer.parseInt(input.trim());
                if (qty <= 0) {
                    cartItems.remove(row);
                } else if (qty > item.getProduct().getStockQuantity()) {
                    JOptionPane.showMessageDialog(this, "Vượt quá số lượng tồn kho (" + item.getProduct().getStockQuantity() + ")!",
                            "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                } else {
                    item.setQuantity(qty);
                }
                refreshCartTable();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Số lượng không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void removeSelectedRow() {
        int row = tblCart.getSelectedRow();
        if (row >= 0 && row < cartItems.size()) {
            cartItems.remove(row);
            refreshCartTable();
        }
    }

    private void refreshCartTable() {
        cartTableModel.setRowCount(0);
        for (CartItem item : cartItems) {
            cartTableModel.addRow(new Object[]{
                    item.getProduct().getCode(),
                    item.getProduct().getName(),
                    item.getQuantity(),
                    FormatUtil.formatCurrency(item.getUnitPrice()),
                    FormatUtil.formatCurrency(item.getLineTotal())
            });
        }
        updateCartTotals();
    }

    private void findCustomerByPhone() {
        String phone = txtCustomerPhone.getText().trim();
        if (phone.isEmpty()) {
            selectedCustomer = null;
            lblCustomerInfo.setText("Khách lẻ (Chưa chọn thành viên)");
            return;
        }
        Customer c = customerService.findByPhone(phone);
        if (c != null) {
            selectedCustomer = c;
            lblCustomerInfo.setText("KH: " + c.getName() + " (" + c.getPhone() + ") - Điểm TL: " + c.getPoints());
        } else {
            int ans = JOptionPane.showConfirmDialog(this,
                    "Không tìm thấy SĐT '" + phone + "'. Bạn có muốn thêm khách hàng mới không?",
                    "Khách hàng mới", JOptionPane.YES_NO_OPTION);
            if (ans == JOptionPane.YES_OPTION) {
                openNewCustomerDialog();
            }
        }
    }

    private void openNewCustomerDialog() {
        Customer newCust = new Customer();
        newCust.setPhone(txtCustomerPhone.getText().trim());
        CustomerDialog dlg = new CustomerDialog(SwingUtilities.getWindowAncestor(this), customerService, newCust);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            selectedCustomer = dlg.getCustomer();
            txtCustomerPhone.setText(selectedCustomer.getPhone());
            lblCustomerInfo.setText("KH: " + selectedCustomer.getName() + " (" + selectedCustomer.getPhone() + ") - Điểm TL: " + selectedCustomer.getPoints());
        }
    }

    private void handleApplyDiscount() {
        BigDecimal subtotal = calculateSubtotal();
        if (subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng thêm sản phẩm vào giỏ hàng trước!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int mode = cboManualDiscountType.getSelectedIndex();
        if (mode == 0) {
            try {
                appliedVoucher = customerService.validateAndGetVoucher(txtVoucherCode.getText().trim(), subtotal);
                updateCartTotals();
                JOptionPane.showMessageDialog(this, "Đã áp dụng mã giảm giá: " + appliedVoucher.getCode(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                appliedVoucher = null;
                updateCartTotals();
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể áp dụng mã", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            appliedVoucher = null;
            updateCartTotals();
        }
    }

    private BigDecimal calculateSubtotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            sum = sum.add(item.getLineTotal());
        }
        return sum;
    }

    private BigDecimal calculateDiscount(BigDecimal subtotal) {
        if (subtotal.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

        int mode = cboManualDiscountType.getSelectedIndex();
        if (mode == 0) {
            if (appliedVoucher != null) {
                return appliedVoucher.calculateDiscount(subtotal);
            }
            return BigDecimal.ZERO;
        } else if (mode == 1) {
            BigDecimal pct = FormatUtil.parseMoney(txtVoucherCode.getText());
            if (pct.compareTo(BigDecimal.valueOf(100)) > 0) pct = BigDecimal.valueOf(100);
            if (pct.compareTo(BigDecimal.ZERO) < 0) pct = BigDecimal.ZERO;
            return subtotal.multiply(pct).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        } else {
            BigDecimal amt = FormatUtil.parseMoney(txtVoucherCode.getText());
            if (amt.compareTo(subtotal) > 0) amt = subtotal;
            if (amt.compareTo(BigDecimal.ZERO) < 0) amt = BigDecimal.ZERO;
            return amt;
        }
    }

    private void updateCartTotals() {
        BigDecimal subtotal = calculateSubtotal();
        BigDecimal discount = calculateDiscount(subtotal);
        BigDecimal afterDiscount = subtotal.subtract(discount).max(BigDecimal.ZERO);

        BigDecimal vat = BigDecimal.ZERO;
        if (chkApplyVat.isSelected()) {
            vat = afterDiscount.multiply(BigDecimal.valueOf(AppConfig.getVatPercent()))
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        }
        BigDecimal total = afterDiscount.add(vat);

        lblSubtotal.setText(FormatUtil.formatCurrency(subtotal));
        lblDiscount.setText("-" + FormatUtil.formatCurrency(discount));
        lblVat.setText(FormatUtil.formatCurrency(vat));
        lblTotalAmount.setText(FormatUtil.formatCurrency(total));
    }

    private void handleCancelOrder() {
        if (cartItems.isEmpty()) return;
        int cf = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn hủy toàn bộ giỏ hàng hiện tại?",
                "Xác nhận hủy đơn", JOptionPane.YES_NO_OPTION);
        if (cf == JOptionPane.YES_OPTION) {
            clearCurrentOrder();
        }
    }

    private void handleCheckout() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng đang trống! Vui lòng chọn sản phẩm trước.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal subtotal = calculateSubtotal();
        BigDecimal discount = calculateDiscount(subtotal);
        BigDecimal afterDiscount = subtotal.subtract(discount).max(BigDecimal.ZERO);
        BigDecimal vat = chkApplyVat.isSelected()
                ? afterDiscount.multiply(BigDecimal.valueOf(AppConfig.getVatPercent())).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal total = afterDiscount.add(vat);

        Invoice draft = new Invoice();
        User currentUser = SessionManager.getCurrentUser();
        draft.setUserId(currentUser != null ? currentUser.getId() : 1);
        draft.setEmployeeName(currentUser != null ? currentUser.getFullName() : "Admin");

        if (selectedCustomer != null) {
            draft.setCustomerId(selectedCustomer.getId());
            draft.setCustomerName(selectedCustomer.getName());
            draft.setCustomerPhone(selectedCustomer.getPhone());
        } else {
            draft.setCustomerName("Khách lẻ");
        }

        if (appliedVoucher != null) {
            draft.setVoucherId(appliedVoucher.getId());
            draft.setVoucherCode(appliedVoucher.getCode());
        }

        draft.setSubtotal(subtotal);
        draft.setDiscountAmount(discount);
        draft.setVatAmount(vat);
        draft.setTotalAmount(total);

        CheckoutDialog checkoutDialog = new CheckoutDialog((Frame) SwingUtilities.getWindowAncestor(this), draft);
        checkoutDialog.setVisible(true);

        if (checkoutDialog.isConfirmed()) {
            try {
                Invoice savedInvoice = invoiceService.checkout(draft, cartItems);
                clearCurrentOrder();
                loadProductGrid();

                InvoicePreviewDialog previewDialog = new InvoicePreviewDialog(SwingUtilities.getWindowAncestor(this), savedInvoice);
                previewDialog.setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi thanh toán", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearCurrentOrder() {
        cartItems.clear();
        appliedVoucher = null;
        selectedCustomer = null;
        txtVoucherCode.setText("");
        txtCustomerPhone.setText("");
        lblCustomerInfo.setText("Khách lẻ (Chưa chọn thành viên)");
        refreshCartTable();
    }
}
