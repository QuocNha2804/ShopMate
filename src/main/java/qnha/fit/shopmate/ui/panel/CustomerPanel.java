package qnha.fit.shopmate.ui.panel;

import qnha.fit.shopmate.model.Customer;
import qnha.fit.shopmate.model.Invoice;
import qnha.fit.shopmate.service.CustomerService;
import qnha.fit.shopmate.service.InvoiceService;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class CustomerPanel extends JPanel {
    private final CustomerService customerService = new CustomerService();
    private final InvoiceService invoiceService = new InvoiceService();

    private final JTextField txtSearch = new JTextField(20);
    private final DefaultTableModel customerModel;
    private final JTable tblCustomers;
    private final DefaultTableModel historyModel;
    private final JTable tblHistory;

    private List<Customer> customers;
    private List<Invoice> customerInvoices;

    public CustomerPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel toolbar = new JPanel(new BorderLayout(10, 6));
        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBox.add(new JLabel("Tìm theo SĐT / Tên KH:"));
        UIUtil.styleSearchField(txtSearch, "Nhập SĐT, tên hoặc email...");
        txtSearch.setPreferredSize(new Dimension(230, 32));
        searchBox.add(txtSearch);
        JButton btnSearch = new JButton("Tìm kiếm");
        UIUtil.styleSecondaryButton(btnSearch);
        searchBox.add(btnSearch);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btnAdd = new JButton("+ Thêm Khách hàng");
        UIUtil.stylePrimaryButton(btnAdd);
        JButton btnEdit = new JButton("Sửa TT Khách");
        UIUtil.styleSecondaryButton(btnEdit);
        JButton btnDelete = new JButton("Xóa");
        UIUtil.styleDangerButton(btnDelete);

        actions.add(btnAdd);
        actions.add(btnEdit);
        actions.add(btnDelete);

        toolbar.add(searchBox, BorderLayout.WEST);
        toolbar.add(actions, BorderLayout.EAST);

        String[] custCols = {"ID", "Họ và tên", "Số điện thoại", "Email", "Địa chỉ", "Điểm tích lũy", "Ngày đăng ký"};
        customerModel = new DefaultTableModel(custCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblCustomers = new JTable(customerModel);
        UIUtil.styleTable(tblCustomers);
        UIUtil.alignColumnCenter(tblCustomers, 0, 2, 5);

        String[] histCols = {"Mã HĐ", "Ngày mua", "Thu ngân", "Tạm tính", "Giảm giá", "Tổng thanh toán", "Phương thức", "Trạng thái"};
        historyModel = new DefaultTableModel(histCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblHistory = new JTable(historyModel);
        UIUtil.styleTable(tblHistory);
        UIUtil.alignColumnRight(tblHistory, 3, 4, 5);
        UIUtil.alignColumnCenter(tblHistory, 0, 6, 7);

        tblCustomers.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Customer sel = getSelectedCustomer();
                if (sel != null) {
                    loadCustomerHistory(sel.getId());
                } else {
                    historyModel.setRowCount(0);
                }
            }
        });

        tblHistory.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblHistory.getSelectedRow() >= 0) {
                    int row = tblHistory.getSelectedRow();
                    if (customerInvoices != null && row < customerInvoices.size()) {
                        Invoice full = invoiceService.getInvoiceWithDetails(customerInvoices.get(row).getId());
                        if (full != null) {
                            new InvoicePreviewDialog(SwingUtilities.getWindowAncestor(CustomerPanel.this), full).setVisible(true);
                        }
                    }
                }
            }
        });

        JScrollPane custScroll = new JScrollPane(tblCustomers);
        custScroll.setBorder(BorderFactory.createTitledBorder("Danh sách Khách hàng"));

        JScrollPane histScroll = new JScrollPane(tblHistory);
        histScroll.setBorder(BorderFactory.createTitledBorder("Lịch sử mua hàng của khách đang chọn (Double-click xem hóa đơn)"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, custScroll, histScroll);
        splitPane.setResizeWeight(0.58);

        btnSearch.addActionListener(e -> refreshData());
        txtSearch.addActionListener(e -> refreshData());
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refreshData(); }
            @Override public void removeUpdate(DocumentEvent e) { refreshData(); }
            @Override public void changedUpdate(DocumentEvent e) { refreshData(); }
        });
        btnAdd.addActionListener(e -> openCustomerDialog(null));
        btnEdit.addActionListener(e -> {
            Customer sel = getSelectedCustomer();
            if (sel == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần sửa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            openCustomerDialog(sel);
        });
        btnDelete.addActionListener(e -> handleDelete());

        add(toolbar, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        refreshData();
    }

    public void refreshData() {
        customers = customerService.searchCustomers(txtSearch.getText().trim());
        customerModel.setRowCount(0);
        for (Customer c : customers) {
            customerModel.addRow(new Object[]{
                    c.getId(),
                    c.getName(),
                    c.getPhone(),
                    c.getEmail(),
                    c.getAddress(),
                    c.getPoints(),
                    FormatUtil.formatDateTime(c.getCreatedAt())
            });
        }
        historyModel.setRowCount(0);
    }

    private void loadCustomerHistory(int customerId) {
        customerInvoices = invoiceService.getInvoicesByCustomer(customerId);
        historyModel.setRowCount(0);
        for (Invoice inv : customerInvoices) {
            historyModel.addRow(new Object[]{
                    inv.getInvoiceCode(),
                    FormatUtil.formatDateTime(inv.getCreatedAt()),
                    inv.getEmployeeName(),
                    FormatUtil.formatCurrency(inv.getSubtotal()),
                    FormatUtil.formatCurrency(inv.getDiscountAmount()),
                    FormatUtil.formatCurrency(inv.getTotalAmount()),
                    "CASH".equalsIgnoreCase(inv.getPaymentMethod()) ? "Tiền mặt" : "Chuyển khoản",
                    "PAID".equalsIgnoreCase(inv.getStatus()) ? "Đã thanh toán" : "Đã hủy"
            });
        }
    }

    private Customer getSelectedCustomer() {
        int row = tblCustomers.getSelectedRow();
        if (row < 0 || customers == null || row >= customers.size()) return null;
        return customers.get(row);
    }

    private void openCustomerDialog(Customer customer) {
        CustomerDialog dlg = new CustomerDialog(SwingUtilities.getWindowAncestor(this), customerService, customer);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            refreshData();
        }
    }

    private void handleDelete() {
        Customer sel = getSelectedCustomer();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int cf = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa khách hàng '" + sel.getName() + "'?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (cf == JOptionPane.YES_OPTION) {
            try {
                customerService.deleteCustomer(sel.getId());
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
