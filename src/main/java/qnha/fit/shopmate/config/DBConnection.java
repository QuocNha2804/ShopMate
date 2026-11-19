package qnha.fit.shopmate.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import qnha.fit.shopmate.exception.DataAccessException;
import qnha.fit.shopmate.util.AppLogger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DBConnection {
    private static HikariDataSource dataSource;
    private static String lastInitError = null;

    private DBConnection() {
    }

    public static synchronized void initPool() {
        if (dataSource != null && !dataSource.isClosed()) {
            return;
        }
        try {
            ensureDatabaseExists(AppConfig.getDbUrl(), AppConfig.getDbUsername(), AppConfig.getDbPassword());

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(AppConfig.getDbUrl());
            config.setUsername(AppConfig.getDbUsername());
            config.setPassword(AppConfig.getDbPassword());
            config.setDriverClassName(AppConfig.get("db.driver", "org.mariadb.jdbc.Driver"));
            config.setMaximumPoolSize(Integer.parseInt(AppConfig.get("db.pool.size", "10")));
            config.setMinimumIdle(1);
            config.setConnectionTimeout(4000);
            config.setInitializationFailTimeout(2000);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);
            lastInitError = null;
            AppLogger.info("Khởi tạo HikariCP Connection Pool thành công.");
        } catch (Exception e) {
            lastInitError = e.getMessage();
            AppLogger.error("Không thể khởi tạo HikariCP Pool: " + e.getMessage(), e);
        }
    }

    public static void ensureDatabaseExists(String jdbcUrl, String username, String password) throws SQLException {
        Pattern p = Pattern.compile("^(jdbc:mariadb://[^/]+)/([^?]+)(.*)$");
        Matcher m = p.matcher(jdbcUrl);
        if (m.find()) {
            String serverUrl = m.group(1) + "/" + m.group(3);
            String dbName = m.group(2);
            try (Connection conn = DriverManager.getConnection(serverUrl, username, password);
                 Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE DATABASE IF NOT EXISTS `" + dbName + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            }
        }
    }

    public static synchronized void reinitializePool() {
        closePool();
        initPool();
    }

    public static String getLastInitError() {
        return lastInitError;
    }

    public static void testConnection(String jdbcUrl, String username, String password) throws SQLException {
        try {
            Class.forName(AppConfig.get("db.driver", "org.mariadb.jdbc.Driver"));
        } catch (ClassNotFoundException ignored) {
        }
        ensureDatabaseExists(jdbcUrl, username, password);
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            if (!conn.isValid(3)) {
                throw new SQLException("Kết nối không hợp lệ hoặc quá thời gian chờ.");
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            initPool();
        }
        if (dataSource != null && !dataSource.isClosed()) {
            return dataSource.getConnection();
        }
        return DriverManager.getConnection(
                AppConfig.getDbUrl(),
                AppConfig.getDbUsername(),
                AppConfig.getDbPassword()
        );
    }

    /**
     * Tự động kiểm tra và chạy schema.sql + data.sql nếu database mới tạo chưa có bảng users.
     */
    public static boolean initializeDatabaseIfNeeded() {
        try (Connection conn = getConnection()) {
            boolean hasUsersTable = false;
            try (ResultSet rs = conn.getMetaData().getTables(conn.getCatalog(), null, "users", null)) {
                if (rs.next()) {
                    hasUsersTable = true;
                }
            }
            if (!hasUsersTable) {
                AppLogger.info("Chưa tìm thấy bảng dữ liệu trong DB. Đang tự động khởi tạo schema.sql và data.sql...");
                executeResourceSql(conn, "schema.sql");
                executeResourceSql(conn, "data.sql");
                AppLogger.info("Khởi tạo Database và dữ liệu mẫu thành công!");
            }
            lastInitError = null;
            return true;
        } catch (Exception e) {
            lastInitError = e.getMessage();
            AppLogger.error("Lỗi khi kiểm tra/khởi tạo cơ sở dữ liệu: " + e.getMessage(), e);
            return false;
        }
    }

    public static void executeResourceSql(Connection conn, String resourceName) {
        try (InputStream is = DBConnection.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new DataAccessException("Không tìm thấy file SQL trong resources: " + resourceName);
            }
            executeSqlStream(conn, is);
        } catch (Exception e) {
            throw new DataAccessException("Lỗi thực thi script " + resourceName + ": " + e.getMessage(), e);
        }
    }

    public static void executeSqlStream(Connection conn, InputStream inputStream) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            try (Statement stmt = conn.createStatement()) {
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("--") || trimmed.startsWith("//")) {
                        continue;
                    }
                    sb.append(line).append("\n");
                    if (trimmed.endsWith(";")) {
                        String sql = sb.toString().trim();
                        if (sql.endsWith(";")) {
                            sql = sql.substring(0, sql.length() - 1).trim();
                        }
                        if (!sql.isEmpty()) {
                            stmt.execute(sql);
                        }
                        sb.setLength(0);
                    }
                }
            }
        }
    }

    public static synchronized void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            dataSource = null;
        }
    }
}
