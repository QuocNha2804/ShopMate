package qnha.fit.shopmate.ui.frame;

import qnha.fit.shopmate.config.AppConfig;
import qnha.fit.shopmate.model.User;
import qnha.fit.shopmate.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final AuthService authService = new AuthService();

    private final JTextField txtUsername = new JTextField(20);
    private final JPasswordField txtPassword = new JPasswordField(20);
    private final JCheckBox chkRememberMe = new JCheckBox("Ghi nhớ đăng nhập (Remember me)");

    public LoginFrame() {
        setTitle("Đăng nhập Hệ thống — " + AppConfig.getStoreName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 410);
        setLocationRelativeTo(null);
        setResizable(false);

        initUI();
        loadRememberedCredentials();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(24, 32, 24, 32));

        // Header
        JPanel header = new JPanel(new GridLayout(3, 1, 2, 4));
        header.setOpaque(false);
        JLabel lblBrand = new JLabel("SHOPMATE POS", SwingConstants.CENTER);
        lblBrand.setFont(new Font("SansSerif", Font.BOLD, 26));
        lblBrand.setForeground(new Color(37, 99, 235));

        JLabel lblSubtitle = new JLabel("Phần mềm Quản lý Bán hàng Desktop", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblSubtitle.setForeground(new Color(100, 116, 139));

        JLabel lblDemoHint = new JLabel("TK mẫu: admin / 123456  |  staff / 123456", SwingConstants.CENTER);
        lblDemoHint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblDemoHint.setForeground(new Color(22, 163, 74));

        header.add(lblBrand);
        header.add(lblSubtitle);
        header.add(lblDemoHint);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(16, 0, 10, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblUser = new JLabel("Tên đăng nhập (Username):");
        lblUser.setFont(new Font("SansSerif", Font.BOLD, 13));
        form.add(lblUser, gbc);

        gbc.gridy = 1;
        txtUsername.setPreferredSize(new Dimension(0, 36));
        form.add(txtUsername, gbc);

        gbc.gridy = 2;
        JLabel lblPass = new JLabel("Mật khẩu (Password):");
        lblPass.setFont(new Font("SansSerif", Font.BOLD, 13));
        form.add(lblPass, gbc);

        gbc.gridy = 3;
        txtPassword.setPreferredSize(new Dimension(0, 36));
        form.add(txtPassword, gbc);

        gbc.gridy = 4;
        form.add(chkRememberMe, gbc);

        // Nút Đăng nhập
        JButton btnLogin = new JButton("ĐĂNG NHẬP HỆ THỐNG");
        btnLogin.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnLogin.setBackground(new Color(37, 99, 235));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setPreferredSize(new Dimension(0, 42));
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnLogin.addActionListener(e -> performLogin());
        txtPassword.addActionListener(e -> performLogin());
        txtUsername.addActionListener(e -> txtPassword.requestFocusInWindow());

        root.add(header, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(btnLogin, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void loadRememberedCredentials() {
        if (AppConfig.isRememberMe()) {
            chkRememberMe.setSelected(true);
            txtUsername.setText(AppConfig.getRememberedUsername());
            txtPassword.setText(AppConfig.getRememberedPassword());
        } else {
            txtUsername.setText("admin");
            txtPassword.setText("123456");
        }
    }

    private void performLogin() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword());

            User loggedIn = authService.login(username, password);
            AppConfig.saveRememberMe(chkRememberMe.isSelected(), username, password);

            MainFrame mainFrame = new MainFrame(loggedIn);
            mainFrame.setVisible(true);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Đăng nhập thất bại", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
}
