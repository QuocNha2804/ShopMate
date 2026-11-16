package qnha.fit.shopmate.ui.dialog;

import qnha.fit.shopmate.model.Customer;
import qnha.fit.shopmate.service.CustomerService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CustomerDialog extends JDialog {
    private final CustomerService customerService;
    private final Customer customer;
    private boolean saved = false;

    private final JTextField txtName = new JTextField(22);
    private final JTextField txtPhone = new JTextField(22);
    private final JTextField txtEmail = new JTextField(22);
    private final JTextField txtAddress = new JTextField(22);
    private final JSpinner spnPoints = new JSpinner(new SpinnerNumberModel(0, 0, 1_000_000, 1));

    public CustomerDialog(Window owner, CustomerService customerService, Customer existingCustomer) {
        super(owner, existingCustomer == null ? "Thêm Khách hàng mới" : "Cập nhật Khách hàng", ModalityType.APPLICATION_MODAL);
        this.customerService = customerService;
        this.customer = existingCustomer != null ? existingCustomer : new Customer();

        setSize(460, 340);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(16, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 7, 7, 7);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addRow(form, gbc, 0, "Họ và tên (*):", txtName);
        addRow(form, gbc, 1, "Số điện thoại (*):", txtPhone);
        addRow(form, gbc, 2, "Email:", txtEmail);
        addRow(form, gbc, 3, "Địa chỉ:", txtAddress);
        addRow(form, gbc, 4, "Điểm tích lũy:", spnPoints);

        if (customer.getId() != 0) {
            txtName.setText(customer.getName());
            txtPhone.setText(customer.getPhone());
            txtEmail.setText(customer.getEmail());
            txtAddress.setText(customer.getAddress());
            spnPoints.setValue(customer.getPoints());
        }

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        JButton btnCancel = new JButton("Hủy");
        JButton btnSave = new JButton("Lưu khách hàng");
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
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        panel.add(comp, gbc);
    }

    private void handleSave() {
        try {
            customer.setName(txtName.getText().trim());
            customer.setPhone(txtPhone.getText().trim());
            customer.setEmail(txtEmail.getText().trim());
            customer.setAddress(txtAddress.getText().trim());
            customer.setPoints((Integer) spnPoints.getValue());

            customerService.saveCustomer(customer);
            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public Customer getCustomer() {
        return customer;
    }
}
