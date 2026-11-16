package qnha.fit.shopmate.config;

import qnha.fit.shopmate.util.AppLogger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public final class AppConfig {
    private static final String EXTERNAL_CONFIG_FILE = "data" + File.separator + "config.properties";
    private static final Properties PROPS = new Properties();

    static {
        loadConfig();
    }

    private AppConfig() {
    }

    public static synchronized void loadConfig() {
        try (InputStream is = AppConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                PROPS.load(new InputStreamReader(is, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            AppLogger.error("Không thể đọc file db.properties trong resources", e);
        }

        File extFile = new File(EXTERNAL_CONFIG_FILE);
        if (extFile.exists()) {
            try (InputStream fis = new FileInputStream(extFile);
                 InputStreamReader reader = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
                PROPS.load(reader);
            } catch (IOException e) {
                AppLogger.error("Không thể đọc file cấu hình mở rộng: " + EXTERNAL_CONFIG_FILE, e);
            }
        }
    }

    public static synchronized void saveConfig() {
        File extFile = new File(EXTERNAL_CONFIG_FILE);
        if (extFile.getParentFile() != null && !extFile.getParentFile().exists()) {
            extFile.getParentFile().mkdirs();
        }
        try (OutputStream fos = new FileOutputStream(extFile);
             OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
            PROPS.store(writer, "ShopMate POS - User Configuration");
        } catch (IOException e) {
            AppLogger.error("Không thể lưu file cấu hình: " + EXTERNAL_CONFIG_FILE, e);
        }
    }

    public static String get(String key, String defaultValue) {
        return PROPS.getProperty(key, defaultValue);
    }

    public static void set(String key, String value) {
        if (value == null) {
            PROPS.remove(key);
        } else {
            PROPS.setProperty(key, value);
        }
    }

    public static String getDbUrl() {
        return get("db.url", "jdbc:mariadb://localhost:3306/shopmate_db?useUnicode=true&characterEncoding=UTF-8&createDatabaseIfNotExist=true");
    }

    public static String getDbUsername() {
        return get("db.username", "root");
    }

    public static String getDbPassword() {
        return get("db.password", "");
    }

    public static String getStoreName() {
        return get("store.name", "ShopMate MiniMart");
    }

    public static String getStoreAddress() {
        return get("store.address", "12 Nguyễn Văn Bảo, P.4, Q. Gò Vấp, TP.HCM");
    }

    public static String getStorePhone() {
        return get("store.phone", "0901234567");
    }

    public static String getStoreEmail() {
        return get("store.email", "contact@shopmate.vn");
    }

    public static String getStoreLogo() {
        return get("store.logo", "");
    }

    public static String getStoreBankInfo() {
        return get("store.bank.info", "Vietcombank - STK: 1029384756 - CTK: SHOPMATE MINIMART");
    }

    public static double getVatPercent() {
        try {
            return Double.parseDouble(get("store.vat.percent", "8.0"));
        } catch (NumberFormatException e) {
            return 8.0;
        }
    }

    public static int getPointsRate() {
        try {
            int rate = Integer.parseInt(get("store.points.rate", "100000"));
            return rate > 0 ? rate : 100000;
        } catch (NumberFormatException e) {
            return 100000;
        }
    }

    public static String getTheme() {
        return get("app.theme", "LIGHT");
    }

    public static void setTheme(String theme) {
        set("app.theme", theme);
        saveConfig();
    }

    public static String getRememberedUsername() {
        return get("auth.remember.username", "");
    }

    public static String getRememberedPassword() {
        return get("auth.remember.password", "");
    }

    public static boolean isRememberMe() {
        return Boolean.parseBoolean(get("auth.remember.enabled", "false"));
    }

    public static void saveRememberMe(boolean enabled, String username, String plainPassword) {
        set("auth.remember.enabled", String.valueOf(enabled));
        set("auth.remember.username", enabled ? username : "");
        set("auth.remember.password", enabled ? plainPassword : "");
        saveConfig();
    }
}
