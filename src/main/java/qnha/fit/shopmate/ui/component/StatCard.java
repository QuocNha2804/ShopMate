package qnha.fit.shopmate.ui.component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StatCard extends JPanel {
    private final JLabel lblTitle;
    private final JLabel lblValue;
    private final JLabel lblSub;
    private final Color accentColor;

    public StatCard(String title, String value, String subtitle, Color accentColor) {
        this.accentColor = accentColor;
        setOpaque(false);
        setLayout(new BorderLayout(8, 6));
        setBorder(new EmptyBorder(16, 20, 16, 18));

        lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblTitle.setForeground(new Color(100, 116, 139));

        lblValue = new JLabel(value);
        lblValue.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblValue.setForeground(accentColor.darker());

        lblSub = new JLabel(subtitle);
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSub.setForeground(new Color(100, 116, 139));

        add(lblTitle, BorderLayout.NORTH);
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

        g2.setColor(UIManager.getColor("Panel.background") != null
                ? UIManager.getColor("Panel.background").brighter()
                : Color.WHITE);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

        g2.setColor(new Color(226, 232, 240));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

        g2.setColor(accentColor);
        g2.fillRoundRect(0, 0, 6, getHeight(), 8, 8);

        g2.dispose();
        super.paintComponent(g);
    }
}
