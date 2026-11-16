package qnha.fit.shopmate.ui.dialog;

import qnha.fit.shopmate.model.User;
import qnha.fit.shopmate.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class EmployeeDialog extends JDialog {
    private final AuthService authService;
    private final User user;
    private boolean saved = false;

    private final JTextField txtCode = new JTextField(18);
    private final JTextField txtUsername = new JTextField(18);
    private final JPasswordField txtPassword = new JPasswordField(18);
    private final JTextField txtFullName = new JTextField(22);
    private final JTextField txtPhone = new JTextField(18);
    private final JTextField txtEmail = new JTextField(22);
    private final JComboBox<String> cboRole = new JComboBox<>(new String[]{"STAFF", "ADMIN"});
    private final JComboBox<String> cboStatus = new JComboBox<>(new String[]{"ACTIVE", "LOCKED"});

    public EmployeeDialog(Frame owner, AuthService authService, User existingUser) {
        super(owner, existingUser == null ? "Thêm Nhân viên mới" : "Cập nhật Nhân viên", true);
        this.authService = authService;
        this.user = existingUser != null ? existingUser : new User();

        setSize(480, 420);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(16, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addRow(form, gbc, row++, "Mã nhân viên (*):", txtCode);
        addRow(form, gbc, row++, "Tài khoản (Username) (*):", txtUsername);
        if (user.getId() == 0) {
            txtPassword.setText(AuthService.DEFAULT_PASSWORD);
            addRow(form, gbc, row++, "Mật khẩu khởi tạo:", txtPassword);
        }
        addRow(form, gbc, row++, "Họ và tên (*):", txtFullName);
        addRow(form, gbc, row++, "Số điện thoại:", txtPhone);
        addRow(form, gbc, row++, "Email:", txtEmail);
        addRow(form, gbc, row++, "Phân quyền (Role):", cboRole);
        addRow(form, gbc, row++, "Trạng thái:", cboStatus);

        if (user.getId() == 0) {
            txtCode.setText(authService.generateNextEmployeeCode());
        } else {
            txtCode.setText(user.getCode());
            txtUsername.setText(user.getUsername());
            txtFullName.setText(user.getFullName());
            txtPhone.setText(user.getPhone());
            txtEmail.setText(user.getEmail());
            cboRole.setSelectedItem(user.getRole());
            cboStatus.setSelectedItem(user.getStatus());
        }

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        JButton btnCancel = new JButton("Hủy");
        JButton btnSave = new JButton("Lưu nhân viên");
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
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.38;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 0.62;
        panel.add(comp, gbc);
    }

    private void handleSave() {
        try {
            user.setCode(txtCode.getText().trim().toUpperCase());
            user.setUsername(txtUsername.getText().trim());
            user.setFullName(txtFullName.getText().trim());
            user.setPhone(txtPhone.getText().trim());
            user.setEmail(txtEmail.getText().trim());
            user.setRole((String) cboRole.getSelectedItem());
            user.setStatus((String) cboStatus.getSelectedItem());

            String initPwd = new String(txtPassword.getPassword());
            authService.saveUser(user, initPwd);
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
