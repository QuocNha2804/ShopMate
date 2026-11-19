package qnha.fit.shopmate.ui.component;

import qnha.fit.shopmate.model.Product;
import qnha.fit.shopmate.util.FormatUtil;
import qnha.fit.shopmate.util.ImageUtil;
import qnha.fit.shopmate.util.UIUtil;

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
        setBorder(new EmptyBorder(10, 12, 10, 12));
        setPreferredSize(new Dimension(175, 205));

        boolean available = !product.isOutOfStock() && product.isActive();
        if (available) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        // Hàng trên: Mã SP + Badge Tồn kho
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JLabel lblCode = new JLabel(product.getCode());
        lblCode.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblCode.setForeground(UIUtil.SLATE_500);

        JLabel lblStock = new JLabel();
        lblStock.setFont(new Font("Segoe UI", Font.BOLD, 11));
        if (product.isOutOfStock()) {
            lblStock.setText("Hết hàng");
            lblStock.setForeground(UIUtil.DANGER);
        } else if (product.isLowStock()) {
            lblStock.setText("Còn " + product.getStockQuantity());
            lblStock.setForeground(UIUtil.WARNING);
        } else {
            lblStock.setText("Tồn " + product.getStockQuantity());
            lblStock.setForeground(UIUtil.SUCCESS);
        }

        topPanel.add(lblCode, BorderLayout.WEST);
        topPanel.add(lblStock, BorderLayout.EAST);

        JLabel lblImg = new JLabel(ImageUtil.loadProductImage(product.getImagePath(), product.getName(), 76, 76));
        lblImg.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel centerBox = new JPanel(new BorderLayout(0, 6));
        centerBox.setOpaque(false);
        centerBox.add(topPanel, BorderLayout.NORTH);
        centerBox.add(lblImg, BorderLayout.CENTER);

        // Tên & Giá bán
        JPanel infoPanel = new JPanel(new BorderLayout(0, 4));
        infoPanel.setOpaque(false);

        JLabel lblName = new JLabel("<html><div style='text-align:center;width:138px;'>" + product.getName() + "</div></html>");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblName.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblPrice = new JLabel(FormatUtil.formatCurrency(product.getSellingPrice()) + " / " + product.getUnit());
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPrice.setForeground(UIUtil.PRIMARY);
        lblPrice.setHorizontalAlignment(SwingConstants.CENTER);

        infoPanel.add(lblName, BorderLayout.CENTER);
        infoPanel.add(lblPrice, BorderLayout.SOUTH);

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

        Color cardBg = UIManager.getColor("TextField.background");
        if (cardBg == null) cardBg = Color.WHITE;

        if (product.isOutOfStock()) {
            g2.setColor(new Color(148, 163, 184, 45));
        } else if (hovered) {
            g2.setColor(new Color(37, 99, 235, 18));
        } else {
            g2.setColor(cardBg);
        }
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);

        if (hovered) {
            g2.setColor(UIUtil.PRIMARY);
            g2.setStroke(new BasicStroke(1.8f));
        } else if (product.isLowStock()) {
            g2.setColor(new Color(245, 158, 11, 180));
        } else {
            Color borderColor = UIManager.getColor("Component.borderColor");
            g2.setColor(borderColor != null ? borderColor : UIUtil.SLATE_200);
        }
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);

        g2.dispose();
        super.paintComponent(g);
    }
}
