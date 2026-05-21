package io.github.promptt001.coordinatecracker.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

import io.github.promptt001.coordinatecracker.data.EnumBlockType;
import io.github.promptt001.coordinatecracker.io.TextureManager;

/**
 * Creates tile previews for the pattern editor, including texture rotation and fallbacks.
 */
public final class TileIconRenderer {
    private final TextureManager textureManager;

    public TileIconRenderer(TextureManager textureManager) {
        this.textureManager = textureManager;
    }

    public ImageIcon createUnknownTileIcon(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(232, 232, 232));
        g.fillRect(0, 0, size, size);
        g.setColor(new Color(205, 205, 205));
        for(int x = -size; x < size * 2; x += Math.max(10, size / 6)) {
            g.drawLine(x, 0, x + size / 3, size);
        }
        g.setColor(new Color(120, 120, 120));
        g.setFont(g.getFont().deriveFont(Font.BOLD, Math.max(14f, size / 3.2f)));
        drawCenteredText(g, "?", size);
        g.dispose();
        return new ImageIcon(image);
    }

    /**
     * Compose a tile preview from the loaded texture. Loaded textures are drawn
     * without labels, borders, tinting, or direction dots so their pixels remain
     * faithful to the source PNG after nearest-neighbor scaling.
     */
    public ImageIcon createTileIcon(EnumBlockType blockType, int rotationValue, int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

        BufferedImage texture = this.textureManager.getTexture(blockType);
        if(texture != null) {
            drawTextureVariant(g, texture, rotationValue, size);
        } else {
            paintMissingTextureFallback(g, blockType, rotationValue, size);
        }

        g.dispose();
        return new ImageIcon(image);
    }

    private void drawTextureVariant(Graphics2D g, BufferedImage texture, int rotationValue, int size) {
        Graphics2D tx = (Graphics2D) g.create();
        tx.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        tx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        tx.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

        switch(rotationValue) {
        case 1:
            tx.drawImage(texture, size, 0, -size, size, null);
            break;
        case 2:
            tx.rotate(Math.PI, size / 2.0, size / 2.0);
            tx.drawImage(texture, 0, 0, size, size, null);
            break;
        case 3:
            tx.rotate(Math.PI, size / 2.0, size / 2.0);
            tx.drawImage(texture, size, 0, -size, size, null);
            break;
        case 0:
        default:
            tx.drawImage(texture, 0, 0, size, size, null);
            break;
        }

        tx.dispose();
    }

    private void paintMissingTextureFallback(Graphics2D g, EnumBlockType blockType, int rotationValue, int size) {
        int cell = Math.max(4, size / 8);
        Color a = new Color(38, 38, 38);
        Color b = new Color(210, 0, 210);
        for(int y = 0; y < size; y += cell) {
            for(int x = 0; x < size; x += cell) {
                g.setColor(((x / cell) + (y / cell)) % 2 == 0 ? a : b);
                g.fillRect(x, y, cell, cell);
            }
        }
        g.setColor(new Color(0, 0, 0, 170));
        int labelHeight = Math.max(18, size / 3);
        g.fillRect(0, size - labelHeight, size, labelHeight);
        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(Font.BOLD, Math.max(9f, size / 6.5f)));
        String text = blockType.getAbbreviation() + " " + rotationValue;
        int textWidth = g.getFontMetrics().stringWidth(text);
        g.drawString(text, Math.max(2, (size - textWidth) / 2), size - Math.max(5, labelHeight / 3));
    }

    public Color colorForBlockType(EnumBlockType blockType) {
        EnumBlockType safeType = blockType == null ? EnumBlockType.DEEPSLATE : blockType;
        int hash = safeType.getFileToken().hashCode();
        int r = 80 + Math.floorMod(hash, 120);
        int g = 80 + Math.floorMod(hash / 31, 120);
        int b = 80 + Math.floorMod(hash / 997, 120);
        return new Color(r, g, b);
    }

    public boolean shouldUseLightText(EnumBlockType blockType) {
        Color color = colorForBlockType(blockType);
        int luminance = (int) (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue());
        return luminance < 140;
    }

    private void drawCenteredText(Graphics2D g, String text, int size) {
        int textWidth = g.getFontMetrics().stringWidth(text);
        int textHeight = g.getFontMetrics().getAscent();
        g.drawString(text, (size - textWidth) / 2, (size + textHeight) / 2 - Math.max(3, size / 10));
    }
}
