package qnha.fit.shopmate.service;

import qnha.fit.shopmate.config.DBConnection;
import qnha.fit.shopmate.exception.DataAccessException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BackupService {

    private static final String[] TABLES_IN_ORDER = {
            "users", "categories", "products", "customers",
            "vouchers", "invoices", "invoice_details", "stock_imports"
    };

    public void backupDatabaseToSql(File outputFile) {
        try (Connection conn = DBConnection.getConnection();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {

            writer.println("-- ==========================================================");
            writer.println("-- SHOPMATE POS - SQL BACKUP DUMP");
            writer.println("-- Created at: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.println("-- ==========================================================");
            writer.println("SET FOREIGN_KEY_CHECKS = 0;");
            writer.println();

            for (String table : TABLES_IN_ORDER) {
                writer.println("-- Table: " + table);
                writer.println("TRUNCATE TABLE " + table + ";");

                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM " + table)) {
                    ResultSetMetaData meta = rs.getMetaData();
                    int colCount = meta.getColumnCount();

                    while (rs.next()) {
                        StringBuilder ins = new StringBuilder("INSERT INTO ").append(table).append(" (");
                        for (int i = 1; i <= colCount; i++) {
                            ins.append(meta.getColumnName(i));
                            if (i < colCount) ins.append(", ");
                        }
                        ins.append(") VALUES (");

                        for (int i = 1; i <= colCount; i++) {
                            Object val = rs.getObject(i);
                            if (val == null) {
                                ins.append("NULL");
                            } else if (val instanceof Number) {
                                ins.append(val);
                            } else {
                                String escaped = val.toString()
                                        .replace("\\", "\\\\")
                                        .replace("'", "''")
                                        .replace("\r", "\\r")
                                        .replace("\n", "\\n");
                                ins.append("'").append(escaped).append("'");
                            }
                            if (i < colCount) ins.append(", ");
                        }
                        ins.append(");");
                        writer.println(ins);
                    }
                }
                writer.println();
            }

            writer.println("SET FOREIGN_KEY_CHECKS = 1;");
            writer.flush();
        } catch (Exception e) {
            throw new DataAccessException("Lỗi khi sao lưu Database: " + e.getMessage(), e);
        }
    }

    public void restoreDatabaseFromSql(File sqlFile) {
        try (Connection conn = DBConnection.getConnection();
             InputStream fis = new FileInputStream(sqlFile)) {
            DBConnection.executeSqlStream(conn, fis);
        } catch (Exception e) {
            throw new DataAccessException("Lỗi khi phục hồi Database từ file SQL: " + e.getMessage(), e);
        }
    }

    public void resetSampleData() {
        try (Connection conn = DBConnection.getConnection()) {
            DBConnection.executeResourceSql(conn, "schema.sql");
            DBConnection.executeResourceSql(conn, "data.sql");
        } catch (Exception e) {
            throw new DataAccessException("Lỗi khi khôi phục dữ liệu mẫu: " + e.getMessage(), e);
        }
    }
}
