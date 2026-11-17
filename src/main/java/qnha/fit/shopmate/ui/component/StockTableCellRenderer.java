package qnha.fit.shopmate.ui.component;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Renderer highlight đỏ cho các dòng sản phẩm có tồn kho < 10 (sắp hết hàng).
 */
public class StockTableCellRenderer extends DefaultTableCellRenderer {
    private final int stockColumnIndex;

    public StockTableCellRenderer(int stockColumnIndex) {
        this.stockColumnIndex = stockColumnIndex;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        int modelRow = table.convertRowIndexToModel(row);
        Object stockObj = table.getModel().getValueAt(modelRow, stockColumnIndex);
        int stock = 0;
        if (stockObj instanceof Number n) {
            stock = n.intValue();
        } else if (stockObj != null) {
            try {
                stock = Integer.parseInt(stockObj.toString().replaceAll("[^0-9-]", ""));
            } catch (NumberFormatException ignored) {
            }
        }

        if (!isSelected) {
            if (stock < 10) {
                c.setBackground(new Color(254, 226, 226)); // Đỏ nhạt cảnh báo tồn < 10
                c.setForeground(new Color(185, 28, 28));
                if (column == stockColumnIndex) {
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                }
            } else {
                c.setBackground(table.getBackground());
                c.setForeground(table.getForeground());
            }
        }
        return c;
    }
}
