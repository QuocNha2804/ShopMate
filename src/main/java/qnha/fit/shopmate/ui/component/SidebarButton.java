package qnha.fit.shopmate.ui.component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SidebarButton extends JButton {
    private boolean selectedState = false;
    private boolean hovered = false;

    public SidebarButton(String text) {
        super(text);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setHorizontalAlignment(SwingConstants.LEFT);
        setFont(new Font("SansSerif", Font.BOLD, 14));
        setForeground(new Color(226, 232, 240));
        setBorder(new EmptyBorder(12, 20, 12, 16));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    public void setSelectedState(boolean selected) {
        this.selectedState = selected;
        setForeground(selected ? Color.WHITE : new Color(203, 213, 225));
        repaint();
    }

    public boolean isSelectedState() {
        return selectedState;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (selectedState) {
            g2.setColor(new Color(37, 99, 235));
            g2.fillRoundRect(8, 2, getWidth() - 16, getHeight() - 4, 10, 10);
            g2.setColor(new Color(147, 197, 253));
            g2.fillRoundRect(8, 10, 4, getHeight() - 20, 4, 4);
        } else if (hovered && isEnabled()) {
            g2.setColor(new Color(51, 65, 85));
            g2.fillRoundRect(8, 2, getWidth() - 16, getHeight() - 4, 10, 10);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
