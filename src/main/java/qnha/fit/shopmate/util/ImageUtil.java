package qnha.fit.shopmate.util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ImageUtil {
    public static final String EXTERNAL_IMAGE_DIR = "data" + File.separator + "images";

    private static final Color[] PALETTE = {
            new Color(59, 130, 246),
            new Color(16, 185, 129),
            new Color(245, 158, 11),
            new Color(239, 68, 68),
            new Color(139, 92, 246),
            new Color(236, 72, 153),
            new Color(14, 165, 233)
    };

    private ImageUtil() {
    }

    public static String copyUploadedImage(File sourceFile, String preferredPrefix) throws IOException {
        File dir = new File(EXTERNAL_IMAGE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String originalName = sourceFile.getName();
        String ext = ".png";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex >= 0) {
            ext = originalName.substring(dotIndex).toLowerCase();
        }
        String safePrefix = (preferredPrefix != null && !preferredPrefix.isBlank())
                ? preferredPrefix.replaceAll("[^a-zA-Z0-9_-]", "")
                : "img";
        String targetFileName = safePrefix + "_" + System.currentTimeMillis() + ext;
        Path targetPath = new File(dir, targetFileName).toPath();
        Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        return targetFileName;
    }

    public static ImageIcon loadProductImage(String imagePath, String productName, int width, int height) {
        if (imagePath != null && !imagePath.isBlank()) {
            try {
                File extFile = new File(EXTERNAL_IMAGE_DIR, imagePath);
                BufferedImage img = null;
                if (extFile.exists()) {
                    img = ImageIO.read(extFile);
                } else {
                    File directFile = new File(imagePath);
                    if (directFile.exists()) {
                        img = ImageIO.read(directFile);
                    } else {
                        try (InputStream is = ImageUtil.class.getClassLoader().getResourceAsStream("images/" + imagePath)) {
                            if (is != null) {
                                img = ImageIO.read(is);
                            }
                        }
                    }
                }
                if (img != null) {
                    return new ImageIcon(scaleSmooth(img, width, height));
                }
            } catch (Exception e) {
                AppLogger.warn("Không đọc được ảnh " + imagePath + ", dùng ảnh đại diện mặc định.");
            }
        }
        return createPlaceholderIcon(productName, width, height);
    }

    public static BufferedImage scaleSmooth(BufferedImage src, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resized.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setClip(new RoundRectangle2D.Float(0, 0, width, height, 14, 14));
        g2.drawImage(src, 0, 0, width, height, null);
        g2.dispose();
        return resized;
    }

    public static ImageIcon createPlaceholderIcon(String label, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        String text = (label != null && !label.isBlank()) ? label.trim() : "SP";
        Color baseColor = PALETTE[Math.abs(text.hashCode()) % PALETTE.length];

        GradientPaint gp = new GradientPaint(0, 0, baseColor.brighter(), width, height, baseColor.darker());
        g2.setPaint(gp);
        g2.fill(new RoundRectangle2D.Float(0, 0, width, height, 14, 14));

        String initials = extractInitials(text);
        g2.setColor(Color.WHITE);
        int fontSize = Math.max(12, Math.min(width, height) / 3);
        g2.setFont(new Font("SansSerif", Font.BOLD, fontSize));
        FontMetrics fm = g2.getFontMetrics();
        int x = (width - fm.stringWidth(initials)) / 2;
        int y = ((height - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(initials, x, y);

        g2.dispose();
        return new ImageIcon(img);
    }

    private static String extractInitials(String name) {
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }
}
