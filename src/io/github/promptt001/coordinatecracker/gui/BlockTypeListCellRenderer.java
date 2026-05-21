package io.github.promptt001.coordinatecracker.gui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import io.github.promptt001.coordinatecracker.data.EnumBlockType;

/**
 * Renders block profile choices with the display name and supported surface hint.
 */
public final class BlockTypeListCellRenderer extends DefaultListCellRenderer {
    private static final long serialVersionUID = 1L;

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if(value instanceof EnumBlockType) {
            EnumBlockType type = (EnumBlockType) value;
            label.setText("<html><b>" + htmlEscape(type.getDisplayName()) + "</b> — " + htmlEscape(type.getSurfaceDescription()) + " — " + htmlEscape(type.getGuiStateDescription()) + "</html>");
            label.setToolTipText(type.getDropdownDescription());
        }
        return label;
    }

    private static String htmlEscape(String value) {
        if(value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
}
