package qnha.fit.shopmate.ui.frame;

import qnha.fit.shopmate.config.AppConfig;
import qnha.fit.shopmate.config.DBConnection;
import qnha.fit.shopmate.model.User;
import qnha.fit.shopmate.service.AuthService;
import qnha.fit.shopmate.ui.dialog.DBConfigDialog;
import qnha.fit.shopmate.util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class LoginFrame extends JFrame {
    private final AuthService authService = new AuthService();

    private final JTextField txtUsername = new JTextField(20);
    private final JPasswordField txtPassword = new JPasswordField(20);
    private final JCheckBox chkRememberMe = new JCheckBox("Ghi nhớ đăng nhập (Remember me)");
    private final JButton btnLogin = new JButton("ĐĂNG NHẬP HỆ THỐNG");
    private final JLabel lblDbStatus = new JLabel();

    public LoginFrame() {
        this(true);
    }

    public LoginFrame(boolean initialDbReady) {
        setTitle("Đăng nhập — " + AppConfig.getStoreName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(860, 510);
        setLocationRelativeTo(null);
        setResizable(false);

        initUI();
        loadRememberedCredentials();
        updateDbStatusBadge(initialDbReady);
    }

    private void initUI() {
        JPanel root = new JPanel(new GridLayout(1, 2));

        // ================= BÊN TRÁI: BANNER THƯƠNG HIỆU GRADIENT =================
        JPanel leftBanner = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(15, 23, 42),
                        getWidth(), getHeight(), new Color(37, 99, 235)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Họa tiết vòng tròn mờ hiện đại
                g2.setColor(new Color(255, 255, 255, 18));
                g2.fillOval(-60, -40, 240, 240);
                g2.fillOval(getWidth() - 140, getHeight() - 150, 230, 230);
                g2.setColor(new Color(255, 255, 255, 12));
                g2.fillOval(getWidth() - 90, 60, 120, 120);

                g2.dispose();
            }
        };
        leftBanner.setLayout(new BoxLayout(leftBanner, BoxLayout.Y_AXIS));
        leftBanner.setBorder(new EmptyBorder(46, 36, 36, 36));

        JLabel logoBadge = new JLabel("SM", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 35));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 18, 18));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoBadge.setFont(new Font("Segoe UI", Font.BOLD, 24));
        logoBadge.setForeground(Color.WHITE);
        logoBadge.setPreferredSize(new Dimension(60, 60));
        logoBadge.setMaximumSize(new Dimension(60, 60));
        logoBadge.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblBrand = new JLabel("ShopMate POS");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblBrand.setForeground(Color.WHITE);
        lblBrand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTagline = new JLabel("<html>Giải pháp quản lý bán hàng toàn diện, nhanh chóng và chính xác cho cửa hàng bán lẻ hiện đại.</html>");
        lblTagline.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTagline.setForeground(new Color(203, 213, 225));
        lblTagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblFeatures = new JLabel("""
                <html>
                <div style='line-height: 1.9; color: #e2e8f0; font-size: 11px;'>
                    ✓ &nbsp;Bán hàng POS 2 cột &amp; Quét mã SKU nhanh<br/>
                    ✓ &nbsp;In hóa đơn PDF tiếng Việt (K80) &amp; Quản lý kho<br/>
                    ✓ &nbsp;Tích điểm khách hàng &amp; Mã giảm giá Voucher<br/>
                    ✓ &nbsp;Báo cáo Doanh thu - Lợi nhuận trực quan
                </div>
                </html>
                """);
        lblFeatures.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblVersion = new JLabel("Phiên bản 1.0  •  Java 21 + MariaDB");
        lblVersion.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblVersion.setForeground(new Color(148, 163, 184));
        lblVersion.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftBanner.add(logoBadge);
        leftBanner.add(Box.createVerticalStrut(18));
        leftBanner.add(lblBrand);
        leftBanner.add(Box.createVerticalStrut(8));
        leftBanner.add(lblTagline);
        leftBanner.add(Box.createVerticalStrut(28));
        leftBanner.add(lblFeatures);
        leftBanner.add(Box.createVerticalGlue());
        leftBanner.add(lblVersion);

        // ================= BÊN PHẢI: FORM ĐĂNG NHẬP =================
        JPanel rightForm = new JPanel(new BorderLayout());
        rightForm.setBorder(new EmptyBorder(36, 40, 24, 40));

        JPanel topTitle = new JPanel(new GridLayout(3, 1, 2, 4));
        topTitle.setOpaque(false);
        JLabel lblWelcome = new JLabel("Đăng nhập Hệ thống");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JLabel lblSub = new JLabel("Vui lòng nhập thông tin tài khoản để bắt đầu ca làm việc");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(UIUtil.SLATE_500);

        JLabel lblHint = new JLabel("Mặc định: admin / 123456  hoặc  staff / 123456");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHint.setForeground(UIUtil.SUCCESS);

        topTitle.add(lblWelcome);
        topTitle.add(lblSub);
        topTitle.add(lblHint);

        JPanel formFields = new JPanel(new GridBagLayout());
        formFields.setOpaque(false);
        formFields.setBorder(new EmptyBorder(18, 0, 12, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblUser = new JLabel("Tên đăng nhập");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formFields.add(lblUser, gbc);

        gbc.gridy = 1;
        txtUsername.setPreferredSize(new Dimension(0, 40));
        UIUtil.styleSearchField(txtUsername, "Nhập username (VD: admin)");
        formFields.add(txtUsername, gbc);

        gbc.gridy = 2;
        JLabel lblPass = new JLabel("Mật khẩu");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formFields.add(lblPass, gbc);

        gbc.gridy = 3;
        txtPassword.setPreferredSize(new Dimension(0, 40));
        UIUtil.stylePasswordField(txtPassword, "Nhập mật khẩu của bạn");
        formFields.add(txtPassword, gbc);

        gbc.gridy = 4;
        chkRememberMe.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formFields.add(chkRememberMe, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(14, 0, 6, 0);
        UIUtil.stylePrimaryButton(btnLogin);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setPreferredSize(new Dimension(0, 44));
        formFields.add(btnLogin, gbc);

        // Thanh trạng thái Database & nút Cấu hình CSDL ở dưới cùng
        JPanel bottomBar = new JPanel(new BorderLayout(8, 0));
        bottomBar.setOpaque(false);
        bottomBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIUtil.SLATE_200),
                new EmptyBorder(10, 0, 0, 0)
        ));

        lblDbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JButton btnConfigDb = new JButton("⚙ Cấu hình CSDL");
        UIUtil.styleSecondaryButton(btnConfigDb);
        btnConfigDb.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btnConfigDb.addActionListener(e -> openDbConfigDialog());

        bottomBar.add(lblDbStatus, BorderLayout.CENTER);
        bottomBar.add(btnConfigDb, BorderLayout.EAST);

        rightForm.add(topTitle, BorderLayout.NORTH);
        rightForm.add(formFields, BorderLayout.CENTER);
        rightForm.add(bottomBar, BorderLayout.SOUTH);

        root.add(leftBanner);
        root.add(rightForm);
        setContentPane(root);

        // Events
        btnLogin.addActionListener(e -> performLoginAsync());
        txtPassword.addActionListener(e -> performLoginAsync());
        txtUsername.addActionListener(e -> txtPassword.requestFocusInWindow());
    }

    private void updateDbStatusBadge(boolean isReady) {
        if (isReady && DBConnection.getLastInitError() == null) {
            lblDbStatus.setText("● MariaDB: Đã kết nối");
            lblDbStatus.setForeground(UIUtil.SUCCESS);
        } else {
            lblDbStatus.setText("● MariaDB: Chưa kết nối (Bấm Cấu hình)");
            lblDbStatus.setForeground(UIUtil.DANGER);
        }
    }

    private void openDbConfigDialog() {
        DBConfigDialog dlg = new DBConfigDialog(this);
        dlg.setVisible(true);
        if (dlg.isConnectedAndSaved()) {
            updateDbStatusBadge(true);
        }
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

    private void performLoginAsync() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        btnLogin.setEnabled(false);
        btnLogin.setText("ĐANG XÁC THỰC...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() {
                DBConnection.initializeDatabaseIfNeeded();
                return authService.login(username, password);
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                btnLogin.setEnabled(true);
                btnLogin.setText("ĐĂNG NHẬP HỆ THỐNG");
                try {
                    User loggedIn = get();
                    updateDbStatusBadge(true);
                    AppConfig.saveRememberMe(chkRememberMe.isSelected(), username, password);
                    MainFrame mainFrame = new MainFrame(loggedIn);
                    mainFrame.setVisible(true);
                    dispose();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    String msg = cause.getMessage() != null ? cause.getMessage() : "Lỗi không xác định";
                    boolean isDbError = msg.contains("Access denied")
                            || msg.contains("Could not connect")
                            || msg.contains("Connection refused")
                            || msg.contains("Hikari")
                            || msg.contains("Lỗi truy vấn");

                    if (isDbError) {
                        updateDbStatusBadge(false);
                        int choice = JOptionPane.showConfirmDialog(LoginFrame.this,
                                "Không thể kết nối Cơ sở dữ liệu MariaDB!\nChi tiết: " + msg
                                        + "\n\nBạn có muốn mở bảng Cấu hình CSDL để nhập mật khẩu/tài khoản MariaDB ngay bây giờ không?",
                                "Lỗi kết nối Cơ sở dữ liệu",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.ERROR_MESSAGE);
                        if (choice == JOptionPane.YES_OPTION) {
                            openDbConfigDialog();
                        }
                    } else {
                        JOptionPane.showMessageDialog(LoginFrame.this, msg, "Đăng nhập thất bại", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }.execute();
    }
}
