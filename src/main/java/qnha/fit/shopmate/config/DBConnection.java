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

public final class DBConnection {
    private static HikariDataSource dataSource;

    private DBConnection() {
    }

    public static synchronized void initPool() {
        if (dataSource != null && !dataSource.isClosed()) {
            return;
        }
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(AppConfig.getDbUrl());
            config.setUsername(AppConfig.getDbUsername());
            config.setPassword(AppConfig.getDbPassword());
            config.setDriverClassName(AppConfig.get("db.driver", "org.mariadb.jdbc.Driver"));
            config.setMaximumPoolSize(Integer.parseInt(AppConfig.get("db.pool.size", "10")));
            config.setMinimumIdle(2);
            config.setConnectionTimeout(10000);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);
            AppLogger.info("Khởi tạo HikariCP Connection Pool thành công.");
        } catch (Exception e) {
            AppLogger.error("Không thể khởi tạo HikariCP Pool, chuyển sang kết nối trực tiếp khi cần.", e);
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
    public static void initializeDatabaseIfNeeded() {
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
        } catch (Exception e) {
            AppLogger.error("Lỗi khi kiểm tra/khởi tạo cơ sở dữ liệu: " + e.getMessage(), e);
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
        }
    }
}
