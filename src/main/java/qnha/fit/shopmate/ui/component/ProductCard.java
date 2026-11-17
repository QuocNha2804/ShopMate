package qnha.fit.shopmate.ui.component;

import qnha.fit.shopmate.model.Product;
import qnha.fit.shopmate.util.FormatUtil;
import qnha.fit.shopmate.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class ProductCard extends JPanel {
    private final Product product;
    private boolean hovered = false;

    public ProductCard(Product product, Consumer<Product> onClick) {
        this.product = product;
        setOpaque(false);
        setLayout(new BorderLayout(6, 6));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setPreferredSize(new Dimension(165, 195));

        boolean available = !product.isOutOfStock() && product.isActive();
        if (available) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        // Ảnh sản phẩm + Mã SP ở góc trên
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JLabel lblCode = new JLabel(product.getCode());
        lblCode.setFont(new Font("SansSerif", Font.BOLD, 10));
        lblCode.setForeground(new Color(100, 116, 139));

        JLabel lblStock = new JLabel();
        lblStock.setFont(new Font("SansSerif", Font.BOLD, 10));
        if (product.isOutOfStock()) {
            lblStock.setText("Hết hàng");
            lblStock.setForeground(new Color(220, 38, 38));
        } else if (product.isLowStock()) {
            lblStock.setText("Còn: " + product.getStockQuantity());
            lblStock.setForeground(new Color(217, 119, 6));
        } else {
            lblStock.setText("Tồn: " + product.getStockQuantity());
            lblStock.setForeground(new Color(22, 163, 74));
        }

        topPanel.add(lblCode, BorderLayout.WEST);
        topPanel.add(lblStock, BorderLayout.EAST);

        JLabel lblImg = new JLabel(ImageUtil.loadProductImage(product.getImagePath(), product.getName(), 72, 72));
        lblImg.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel centerBox = new JPanel(new BorderLayout(0, 4));
        centerBox.setOpaque(false);
        centerBox.add(topPanel, BorderLayout.NORTH);
        centerBox.add(lblImg, BorderLayout.CENTER);

        // Tên & Giá bán
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        infoPanel.setOpaque(false);

        JLabel lblName = new JLabel("<html><div style='text-align:center;width:125px;'>" + product.getName() + "</div></html>");
        lblName.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblName.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblPrice = new JLabel(FormatUtil.formatCurrency(product.getSellingPrice()) + " / " + product.getUnit());
        lblPrice.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblPrice.setForeground(new Color(37, 99, 235));
        lblPrice.setHorizontalAlignment(SwingConstants.CENTER);

        infoPanel.add(lblName);
        infoPanel.add(lblPrice);

        add(centerBox, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);

        MouseAdapter clickAdapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (available) {
                    hovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (available && onClick != null) {
                    onClick.accept(ProductCard.this.product);
                }
            }
        };

        addMouseListener(clickAdapter);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (product.isOutOfStock()) {
            g2.setColor(new Color(241, 245, 249));
        } else if (hovered) {
            g2.setColor(new Color(239, 246, 255));
        } else {
            Color bg = UIManager.getColor("Panel.background");
            g2.setColor(bg != null ? bg.brighter() : Color.WHITE);
        }
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);

        if (hovered) {
            g2.setColor(new Color(59, 130, 246));
            g2.setStroke(new BasicStroke(1.8f));
        } else if (product.isLowStock()) {
            g2.setColor(new Color(251, 191, 36));
        } else {
            g2.setColor(new Color(203, 213, 225));
        }
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);

        g2.dispose();
        super.paintComponent(g);
    }
}
