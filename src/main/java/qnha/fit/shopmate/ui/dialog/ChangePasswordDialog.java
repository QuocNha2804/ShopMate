package qnha.fit.shopmate.ui.dialog;

import qnha.fit.shopmate.config.SessionManager;
import qnha.fit.shopmate.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ChangePasswordDialog extends JDialog {
    private final JPasswordField txtCurrentPass = new JPasswordField(20);
    private final JPasswordField txtNewPass = new JPasswordField(20);
    private final JPasswordField txtConfirmPass = new JPasswordField(20);
    private final AuthService authService = new AuthService();

    public ChangePasswordDialog(Frame owner) {
        super(owner, "Đổi mật khẩu tài khoản", true);
        setSize(420, 290);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(18, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.35;
        form.add(new JLabel("Mật khẩu hiện tại:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        form.add(txtCurrentPass, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.35;
        form.add(new JLabel("Mật khẩu mới:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        form.add(txtNewPass, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.35;
        form.add(new JLabel("Xác nhận mật khẩu:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        form.add(txtConfirmPass, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        JButton btnCancel = new JButton("Hủy");
        JButton btnSave = new JButton("Lưu mật khẩu");
        btnSave.setBackground(new Color(37, 99, 235));
        btnSave.setForeground(Color.WHITE);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> handleChangePassword());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        add(form, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void handleChangePassword() {
        try {
            if (!SessionManager.isLoggedIn()) return;
            String cur = new String(txtCurrentPass.getPassword());
            String nw = new String(txtNewPass.getPassword());
            String cf = new String(txtConfirmPass.getPassword());
            authService.changePassword(SessionManager.getCurrentUser().getId(), cur, nw, cf);
            JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi đổi mật khẩu", JOptionPane.ERROR_MESSAGE);
        }
    }
}
