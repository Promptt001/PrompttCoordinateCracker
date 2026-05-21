package io.github.promptt001.coordinatecracker.gui;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JPanel;

/**
 * Grid panel that keeps every child square when the parent is resized.
 */
public final class SquareGridPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final int cells;
    private final int gap;

    public SquareGridPanel(int cells, int gap) {
        this.cells = cells;
        this.gap = gap;
        setLayout(null);
    }

    @Override
    public void doLayout() {
        int width = Math.max(0, getWidth() - getInsets().left - getInsets().right);
        int height = Math.max(0, getHeight() - getInsets().top - getInsets().bottom);
        int usable = Math.max(1, Math.min(width, height));
        int cellSize = Math.max(26, (usable - ((this.cells - 1) * this.gap)) / this.cells);
        int gridSize = (cellSize * this.cells) + ((this.cells - 1) * this.gap);
        int startX = getInsets().left + Math.max(0, (width - gridSize) / 2);
        int startY = getInsets().top + Math.max(0, (height - gridSize) / 2);

        Component[] components = getComponents();
        for(int index = 0; index < components.length; index++) {
            int row = index / this.cells;
            int column = index % this.cells;
            components[index].setBounds(startX + (column * (cellSize + this.gap)), startY + (row * (cellSize + this.gap)), cellSize, cellSize);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(420, 420);
    }

    @Override
    public Dimension getMinimumSize() {
        int size = (this.cells * 34) + (this.gap * (this.cells - 1));
        return new Dimension(size, size);
    }
}
