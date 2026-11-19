package qnha.fit.shopmate;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import qnha.fit.shopmate.config.AppConfig;
import qnha.fit.shopmate.config.DBConnection;
import qnha.fit.shopmate.ui.frame.LoginFrame;
import qnha.fit.shopmate.util.AppLogger;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        // 1. Thiết lập giao diện FlatLaf hiện đại
        setupLookAndFeel();

        // 2. Khởi tạo kết nối MariaDB & Tự động nạp schema.sql + data.sql nếu chạy lần đầu
        boolean dbReady = false;
        try {
            DBConnection.initPool();
            dbReady = DBConnection.initializeDatabaseIfNeeded();
        } catch (Exception e) {
            AppLogger.error("Cảnh báo khởi tạo DB khi mở ứng dụng: " + e.getMessage(), e);
        }

        final boolean initialDbReady = dbReady;
        // 3. Khởi chạy màn hình Đăng nhập trên Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame(initialDbReady);
            loginFrame.setVisible(true);
        });
    }

    private static void setupLookAndFeel() {
        try {
            String theme = AppConfig.getTheme();
            if ("DARK".equalsIgnoreCase(theme)) {
                FlatDarkLaf.setup();
            } else if ("INTELLIJ".equalsIgnoreCase(theme)) {
                FlatIntelliJLaf.setup();
            } else {
                FlatLightLaf.setup();
            }
            UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("CheckBox.arc", 6);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.width", 10);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("TabbedPane.showTabSeparators", true);
            UIManager.put("TitlePane.unifiedBackground", true);
        } catch (Exception e) {
            AppLogger.error("Không thể khởi tạo giao diện FlatLaf", e);
        }
    }
}
