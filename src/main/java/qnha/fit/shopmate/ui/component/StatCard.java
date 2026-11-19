package qnha.fit.shopmate.ui.component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StatCard extends JPanel {
    private final JLabel lblTitle;
    private final JLabel lblValue;
    private final JLabel lblSub;
    private final Color accentColor;
    private final String iconText;

    public StatCard(String title, String value, String subtitle, Color accentColor) {
        this(title, value, subtitle, accentColor, "●");
    }

    public StatCard(String title, String value, String subtitle, Color accentColor, String iconText) {
        this.accentColor = accentColor;
        this.iconText = iconText;
        setOpaque(false);
        setLayout(new BorderLayout(10, 6));
        setBorder(new EmptyBorder(16, 20, 16, 18));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(new Color(100, 116, 139));

        JLabel lblIcon = new JLabel(this.iconText, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 30));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblIcon.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblIcon.setForeground(accentColor);
        lblIcon.setPreferredSize(new Dimension(32, 32));

        topRow.add(lblTitle, BorderLayout.CENTER);
        topRow.add(lblIcon, BorderLayout.EAST);

        lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValue.setForeground(accentColor);

        lblSub = new JLabel(subtitle);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(100, 116, 139));

        add(topRow, BorderLayout.NORTH);
        add(lblValue, BorderLayout.CENTER);
        add(lblSub, BorderLayout.SOUTH);
    }

    public void updateValues(String value, String subtitle) {
        lblValue.setText(value);
        lblSub.setText(subtitle);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color panelBg = UIManager.getColor("TextField.background");
        if (panelBg == null) panelBg = Color.WHITE;

        // Bóng đổ nhẹ
        g2.setColor(new Color(15, 23, 42, 12));
        g2.fillRoundRect(2, 3, getWidth() - 4, getHeight() - 4, 16, 16);

        // Nền card
        g2.setColor(panelBg);
        g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 16, 16);

        // Viền card
        Color borderColor = UIManager.getColor("Component.borderColor");
        g2.setColor(borderColor != null ? borderColor : new Color(226, 232, 240));
        g2.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 16, 16);

        // Thanh nhấn bên trái
        g2.setColor(accentColor);
        g2.fillRoundRect(0, 10, 5, getHeight() - 22, 5, 5);

        g2.dispose();
        super.paintComponent(g);
    }
}
