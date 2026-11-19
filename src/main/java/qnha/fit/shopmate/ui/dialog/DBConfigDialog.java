package qnha.fit.shopmate.ui.dialog;

import qnha.fit.shopmate.config.AppConfig;
import qnha.fit.shopmate.config.DBConnection;
import qnha.fit.shopmate.util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBConfigDialog extends JDialog {
    private final JTextField txtHost = new JTextField("localhost", 18);
    private final JTextField txtPort = new JTextField("3306", 8);
    private final JTextField txtDbName = new JTextField("shopmate_db", 18);
    private final JTextField txtUsername = new JTextField("root", 18);
    private final JPasswordField txtPassword = new JPasswordField(18);
    private final JLabel lblStatus = new JLabel(" ");
    private boolean connectedAndSaved = false;

    public DBConfigDialog(Window owner) {
        super(owner, "Cấu hình Kết nối Cơ sở dữ liệu (MariaDB / MySQL)", ModalityType.APPLICATION_MODAL);
        setSize(490, 400);
        setLocationRelativeTo(owner);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        parseCurrentUrl();
        initUI();
    }

    private void parseCurrentUrl() {
        String url = AppConfig.getDbUrl();
        Pattern p = Pattern.compile("jdbc:mariadb://([^:/]+):(\\d+)/([^?]+)");
        Matcher m = p.matcher(url);
        if (m.find()) {
            txtHost.setText(m.group(1));
            txtPort.setText(m.group(2));
            txtDbName.setText(m.group(3));
        }
        txtUsername.setText(AppConfig.getDbUsername());
        txtPassword.setText(AppConfig.getDbPassword());
    }

    private void initUI() {
        JPanel header = new JPanel(new GridLayout(2, 1, 2, 2));
        header.setBorder(new EmptyBorder(14, 20, 4, 20));
        JLabel lblTitle = new JLabel("Thiết lập Kết nối MariaDB / MySQL");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(UIUtil.PRIMARY);
        JLabel lblHint = new JLabel("Nhập tài khoản & mật khẩu MariaDB trên máy của bạn để hệ thống khởi tạo dữ liệu.");
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblHint.setForeground(UIUtil.SLATE_500);
        header.add(lblTitle);
        header.add(lblHint);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(10, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addRow(form, gbc, 0, "Host / Server:", txtHost);
        addRow(form, gbc, 1, "Port:", txtPort);
        addRow(form, gbc, 2, "Tên Database:", txtDbName);
        addRow(form, gbc, 3, "Tài khoản (User):", txtUsername);
        UIUtil.stylePasswordField(txtPassword, "Nhập mật khẩu DB (để trống nếu không có)");
        addRow(form, gbc, 4, "Mật khẩu DB:", txtPassword);

        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        form.add(lblStatus, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        JButton btnTest = new JButton("Kiểm tra kết nối");
        UIUtil.styleSecondaryButton(btnTest);

        JButton btnSave = new JButton("Lưu & Khởi tạo CSDL");
        UIUtil.stylePrimaryButton(btnSave);

        JButton btnCancel = new JButton("Đóng");
        UIUtil.styleSecondaryButton(btnCancel);

        btnTest.addActionListener(e -> testConnectionAsync());
        btnSave.addActionListener(e -> saveAndInitAsync());
        btnCancel.addActionListener(e -> dispose());

        btnPanel.add(btnTest);
        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        add(header, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, Component comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35; gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        panel.add(comp, gbc);
    }

    private String buildJdbcUrl() {
        String host = txtHost.getText().trim();
        String port = txtPort.getText().trim();
        String db = txtDbName.getText().trim();
        return String.format("jdbc:mariadb://%s:%s/%s?useUnicode=true&characterEncoding=UTF-8&createDatabaseIfNotExist=true",
                host, port, db);
    }

    private void testConnectionAsync() {
        String url = buildJdbcUrl();
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());

        lblStatus.setText("⏳ Đang kiểm tra kết nối...");
        lblStatus.setForeground(UIUtil.PRIMARY);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                DBConnection.testConnection(url, user, pass);
                return null;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    get();
                    lblStatus.setText("✅ Kết nối thành công tới MariaDB!");
                    lblStatus.setForeground(UIUtil.SUCCESS);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    lblStatus.setText("❌ Lỗi: " + cause.getMessage());
                    lblStatus.setForeground(UIUtil.DANGER);
                }
            }
        }.execute();
    }

    private void saveAndInitAsync() {
        String url = buildJdbcUrl();
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());

        lblStatus.setText("⏳ Đang kết nối và khởi tạo dữ liệu mẫu...");
        lblStatus.setForeground(UIUtil.PRIMARY);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<Boolean, Void>() {
            private String errorMsg = "";

            @Override
            protected Boolean doInBackground() {
                try {
                    DBConnection.testConnection(url, user, pass);
                    AppConfig.set("db.url", url);
                    AppConfig.set("db.username", user);
                    AppConfig.set("db.password", pass);
                    AppConfig.saveConfig();

                    DBConnection.reinitializePool();
                    return DBConnection.initializeDatabaseIfNeeded();
                } catch (Exception e) {
                    errorMsg = e.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    if (get()) {
                        connectedAndSaved = true;
                        JOptionPane.showMessageDialog(DBConfigDialog.this,
                                "Kết nối và khởi tạo Cơ sở dữ liệu thành công!\nBạn có thể đăng nhập ngay với tài khoản: admin / 123456",
                                "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } else {
                        String msg = errorMsg.isEmpty() ? DBConnection.getLastInitError() : errorMsg;
                        lblStatus.setText("❌ Thất bại: " + msg);
                        lblStatus.setForeground(UIUtil.DANGER);
                    }
                } catch (Exception ex) {
                    lblStatus.setText("❌ Lỗi: " + ex.getMessage());
                    lblStatus.setForeground(UIUtil.DANGER);
                }
            }
        }.execute();
    }

    public boolean isConnectedAndSaved() {
        return connectedAndSaved;
    }
}
