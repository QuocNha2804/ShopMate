package qnha.fit.shopmate.util;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public final class UIUtil {

    public static final Color PRIMARY = new Color(37, 99, 235);
    public static final Color PRIMARY_DARK = new Color(29, 78, 216);
    public static final Color SUCCESS = new Color(22, 163, 74);
    public static final Color DANGER = new Color(220, 38, 38);
    public static final Color WARNING = new Color(217, 119, 6);
    public static final Color SLATE_50 = new Color(248, 250, 252);
    public static final Color SLATE_200 = new Color(226, 232, 240);
    public static final Color SLATE_500 = new Color(100, 116, 139);
    public static final Color SLATE_800 = new Color(30, 41, 59);
    public static final Color SLATE_900 = new Color(15, 23, 42);

    private UIUtil() {
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(34);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JTableHeader header = table.getTableHeader();
        if (header != null) {
            header.setFont(new Font("Segoe UI", Font.BOLD, 13));
            header.setPreferredSize(new Dimension(0, 38));
            header.setReorderingAllowed(false);
        }
    }

    public static void alignColumnRight(JTable table, int... colIndices) {
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int idx : colIndices) {
            if (idx >= 0 && idx < table.getColumnCount()) {
                table.getColumnModel().getColumn(idx).setCellRenderer(rightRenderer);
            }
        }
    }

    public static void alignColumnCenter(JTable table, int... colIndices) {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int idx : colIndices) {
            if (idx >= 0 && idx < table.getColumnCount()) {
                table.getColumnModel().getColumn(idx).setCellRenderer(centerRenderer);
            }
        }
    }

    public static void stylePrimaryButton(JButton btn) {
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 10; borderWidth: 0; focusWidth: 0;");
    }

    public static void styleSuccessButton(JButton btn) {
        btn.setBackground(SUCCESS);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 10; borderWidth: 0; focusWidth: 0;");
    }

    public static void styleDangerButton(JButton btn) {
        btn.setForeground(DANGER);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 10;");
    }

    public static void styleSecondaryButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 10;");
    }

    public static void styleSearchField(JTextField field, String placeholder) {
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        field.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        field.putClientProperty(FlatClientProperties.STYLE, "arc: 10;");
    }

    public static void stylePasswordField(JPasswordField field, String placeholder) {
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        field.putClientProperty(FlatClientProperties.STYLE, "arc: 10; showRevealButton: true;");
    }

    public static void styleCardPanel(JPanel panel, String title) {
        panel.putClientProperty(FlatClientProperties.STYLE, "arc: 14;");
        if (title != null && !title.isEmpty()) {
            panel.setBorder(BorderFactory.createTitledBorder(title));
        }
    }
}
