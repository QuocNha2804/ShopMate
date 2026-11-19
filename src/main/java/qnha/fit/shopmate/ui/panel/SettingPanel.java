package qnha.fit.shopmate.ui.panel;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import qnha.fit.shopmate.config.AppConfig;
import qnha.fit.shopmate.config.SessionManager;
import qnha.fit.shopmate.service.BackupService;
import qnha.fit.shopmate.ui.dialog.DBConfigDialog;
import qnha.fit.shopmate.util.ImageUtil;
import qnha.fit.shopmate.util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class SettingPanel extends JPanel {
    private final BackupService backupService = new BackupService();

    private final JTextField txtStoreName = new JTextField(25);
    private final JTextField txtStoreAddress = new JTextField(25);
    private final JTextField txtStorePhone = new JTextField(20);
    private final JTextField txtStoreEmail = new JTextField(20);
    private final JTextField txtVatPercent = new JTextField(10);
    private final JTextField txtPointsRate = new JTextField(12);
    private final JTextField txtBankInfo = new JTextField(25);
    private final JLabel lblLogoPreview = new JLabel();
    private String logoFileName = "";

    private final JComboBox<String> cboTheme = new JComboBox<>(new String[]{
            "LIGHT (FlatLaf Light)",
            "INTELLIJ (FlatLaf IntelliJ)",
            "DARK (FlatLaf Dark)"
    });

    public SettingPanel() {
        setLayout(new BorderLayout(14, 14));
        setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel storeCard = new JPanel(new BorderLayout(16, 10));
        storeCard.setBorder(BorderFactory.createTitledBorder("1. Thông tin Cửa hàng (Hiển thị trên Hóa đơn & Báo cáo)"));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(10, 14, 10, 14));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int r = 0;
        addRow(form, gbc, r++, "Tên cửa hàng (*):", txtStoreName);
        addRow(form, gbc, r++, "Địa chỉ in hóa đơn (*):", txtStoreAddress);
        addRow(form, gbc, r++, "Số điện thoại (*):", txtStorePhone);
        addRow(form, gbc, r++, "Email cửa hàng:", txtStoreEmail);
        addRow(form, gbc, r++, "Thuế VAT mặc định (%):", txtVatPercent);
        addRow(form, gbc, r++, "Tỷ lệ cộng điểm (VNĐ / 1 điểm):", txtPointsRate);
        addRow(form, gbc, r++, "Thông tin TK Ngân hàng (QR):", txtBankInfo);

        JButton btnSaveStore = new JButton("Lưu Thông tin Cửa hàng");
        UIUtil.stylePrimaryButton(btnSaveStore);
        btnSaveStore.setEnabled(SessionManager.isAdmin());
        gbc.gridx = 1; gbc.gridy = r;
        form.add(btnSaveStore, gbc);

        JPanel logoBox = new JPanel(new BorderLayout(6, 6));
        logoBox.setPreferredSize(new Dimension(190, 200));
        logoBox.setBorder(BorderFactory.createTitledBorder("Logo Cửa hàng"));
        lblLogoPreview.setHorizontalAlignment(SwingConstants.CENTER);
        JButton btnChooseLogo = new JButton("Chọn Logo...");
        UIUtil.styleSecondaryButton(btnChooseLogo);
        btnChooseLogo.setEnabled(SessionManager.isAdmin());
        logoBox.add(lblLogoPreview, BorderLayout.CENTER);
        logoBox.add(btnChooseLogo, BorderLayout.SOUTH);

        storeCard.add(form, BorderLayout.CENTER);
        storeCard.add(logoBox, BorderLayout.EAST);

        JPanel bottomGrid = new JPanel(new GridLayout(1, 2, 14, 0));
        bottomGrid.setPreferredSize(new Dimension(0, 195));

        JPanel themeCard = new JPanel(new GridBagLayout());
        themeCard.setBorder(BorderFactory.createTitledBorder("2. Giao diện & Kết nối Database"));
        GridBagConstraints tgbc = new GridBagConstraints();
        tgbc.insets = new Insets(6, 10, 6, 10);
        tgbc.fill = GridBagConstraints.HORIZONTAL;

        tgbc.gridx = 0; tgbc.gridy = 0;
        themeCard.add(new JLabel("Chủ đề (Theme):"), tgbc);
        tgbc.gridx = 1;
        themeCard.add(cboTheme, tgbc);
        JButton btnApplyTheme = new JButton("Áp dụng Theme");
        UIUtil.stylePrimaryButton(btnApplyTheme);
        tgbc.gridx = 2;
        themeCard.add(btnApplyTheme, tgbc);

        JButton btnDbConfig = new JButton("⚙ Cấu hình Kết nối MariaDB / MySQL");
        UIUtil.styleSecondaryButton(btnDbConfig);
        btnDbConfig.setEnabled(SessionManager.isAdmin());
        tgbc.gridx = 0; tgbc.gridy = 1; tgbc.gridwidth = 3;
        themeCard.add(btnDbConfig, tgbc);

        JPanel backupCard = new JPanel(new GridLayout(3, 1, 8, 8));
        backupCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("3. Sao lưu & Phục hồi Cơ sở dữ liệu (MariaDB SQL)"),
                new EmptyBorder(8, 14, 8, 14)
        ));
        JButton btnBackup = new JButton("Sao lưu Database ra file .sql (Backup)");
        UIUtil.styleSuccessButton(btnBackup);
        JButton btnRestore = new JButton("Phục hồi Database từ file .sql (Restore)");
        UIUtil.styleSecondaryButton(btnRestore);
        JButton btnResetDemo = new JButton("Khôi phục Dữ liệu mẫu gốc (schema.sql + data.sql)");
        UIUtil.styleDangerButton(btnResetDemo);

        boolean isAdmin = SessionManager.isAdmin();
        btnBackup.setEnabled(isAdmin);
        btnRestore.setEnabled(isAdmin);
        btnResetDemo.setEnabled(isAdmin);

        backupCard.add(btnBackup);
        backupCard.add(btnRestore);
        backupCard.add(btnResetDemo);

        bottomGrid.add(themeCard);
        bottomGrid.add(backupCard);

        add(storeCard, BorderLayout.CENTER);
        add(bottomGrid, BorderLayout.SOUTH);

        btnChooseLogo.addActionListener(e -> handleChooseLogo());
        btnSaveStore.addActionListener(e -> handleSaveStoreInfo());
        btnApplyTheme.addActionListener(e -> handleApplyTheme());
        btnDbConfig.addActionListener(e -> new DBConfigDialog(SwingUtilities.getWindowAncestor(this)).setVisible(true));
        btnBackup.addActionListener(e -> handleBackupSql());
        btnRestore.addActionListener(e -> handleRestoreSql());
        btnResetDemo.addActionListener(e -> handleResetSampleData());

        refreshData();
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, Component comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        panel.add(comp, gbc);
    }

    public void refreshData() {
        txtStoreName.setText(AppConfig.getStoreName());
        txtStoreAddress.setText(AppConfig.getStoreAddress());
        txtStorePhone.setText(AppConfig.getStorePhone());
        txtStoreEmail.setText(AppConfig.getStoreEmail());
        txtVatPercent.setText(String.valueOf(AppConfig.getVatPercent()));
        txtPointsRate.setText(String.valueOf(AppConfig.getPointsRate()));
        txtBankInfo.setText(AppConfig.getStoreBankInfo());
        logoFileName = AppConfig.getStoreLogo();
        lblLogoPreview.setIcon(ImageUtil.loadProductImage(logoFileName, AppConfig.getStoreName(), 110, 110));

        String theme = AppConfig.getTheme();
        if ("DARK".equalsIgnoreCase(theme)) {
            cboTheme.setSelectedIndex(2);
        } else if ("INTELLIJ".equalsIgnoreCase(theme)) {
            cboTheme.setSelectedIndex(1);
        } else {
            cboTheme.setSelectedIndex(0);
        }
    }

    private void handleChooseLogo() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Hình ảnh (*.png, *.jpg)", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                logoFileName = ImageUtil.copyUploadedImage(chooser.getSelectedFile(), "store_logo");
                lblLogoPreview.setIcon(ImageUtil.loadProductImage(logoFileName, txtStoreName.getText(), 110, 110));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi tải logo: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleSaveStoreInfo() {
        AppConfig.set("store.name", txtStoreName.getText().trim());
        AppConfig.set("store.address", txtStoreAddress.getText().trim());
        AppConfig.set("store.phone", txtStorePhone.getText().trim());
        AppConfig.set("store.email", txtStoreEmail.getText().trim());
        AppConfig.set("store.vat.percent", txtVatPercent.getText().trim());
        AppConfig.set("store.points.rate", txtPointsRate.getText().trim());
        AppConfig.set("store.bank.info", txtBankInfo.getText().trim());
        AppConfig.set("store.logo", logoFileName);
        AppConfig.saveConfig();
        JOptionPane.showMessageDialog(this, "Đã lưu thông tin cửa hàng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleApplyTheme() {
        try {
            int idx = cboTheme.getSelectedIndex();
            if (idx == 2) {
                UIManager.setLookAndFeel(new FlatDarkLaf());
                AppConfig.setTheme("DARK");
            } else if (idx == 1) {
                UIManager.setLookAndFeel(new FlatIntelliJLaf());
                AppConfig.setTheme("INTELLIJ");
            } else {
                UIManager.setLookAndFeel(new FlatLightLaf());
                AppConfig.setTheme("LIGHT");
            }
            for (Window window : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(window);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể đổi theme: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleBackupSql() {
        File backupDir = new File("data" + File.separator + "backups");
        if (!backupDir.exists()) backupDir.mkdirs();

        JFileChooser chooser = new JFileChooser(backupDir);
        chooser.setDialogTitle("Lưu file Backup SQL");
        chooser.setSelectedFile(new File(backupDir, "shopmate_backup_" + System.currentTimeMillis() + ".sql"));
        chooser.setFileFilter(new FileNameExtensionFilter("SQL Script (*.sql)", "sql"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".sql")) {
                file = new File(file.getAbsolutePath() + ".sql");
            }
            try {
                backupService.backupDatabaseToSql(file);
                JOptionPane.showMessageDialog(this, "Đã sao lưu Database thành công tại:\n" + file.getAbsolutePath(),
                        "Backup thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi Backup", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleRestoreSql() {
        File backupDir = new File("data" + File.separator + "backups");
        JFileChooser chooser = new JFileChooser(backupDir.exists() ? backupDir : new File("."));
        chooser.setDialogTitle("Chọn file SQL để Phục hồi (Restore)");
        chooser.setFileFilter(new FileNameExtensionFilter("SQL Script (*.sql)", "sql"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            int cf = JOptionPane.showConfirmDialog(this,
                    "Cảnh báo: Phục hồi dữ liệu sẽ ghi đè dữ liệu hiện tại bằng file SQL đã chọn.\nBạn có chắc chắn muốn tiếp tục?",
                    "Xác nhận Restore", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (cf == JOptionPane.YES_OPTION) {
                try {
                    backupService.restoreDatabaseFromSql(chooser.getSelectedFile());
                    JOptionPane.showMessageDialog(this, "Phục hồi Database thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi Restore", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void handleResetSampleData() {
        int cf = JOptionPane.showConfirmDialog(this,
                "Khôi phục lại toàn bộ dữ liệu mẫu ban đầu từ schema.sql và data.sql?",
                "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (cf == JOptionPane.YES_OPTION) {
            try {
                backupService.resetSampleData();
                JOptionPane.showMessageDialog(this, "Đã nạp lại dữ liệu mẫu thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
