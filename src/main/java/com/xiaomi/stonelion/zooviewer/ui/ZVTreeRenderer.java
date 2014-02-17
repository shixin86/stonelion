package com.xiaomi.stonelion.zooviewer.ui;

import com.xiaomi.stonelion.zooviewer.model.ZVNode;
import org.apache.zookeeper.data.Stat;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 *
 */
final class ZVTreeRenderer extends DefaultTreeCellRenderer {
    /** */
    private static final long serialVersionUID = 1L;

    @Override
    public Component getTreeCellRendererComponent(
        JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        
        Component comp = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        if ((comp instanceof JLabel) && (value instanceof ZVNode)) {
            ZVNode node = (ZVNode) value;
            String text = node.getName();

            Stat stat = node.getStat();
            if (stat == null) {
                return null;
            }
            boolean isEphemeral = stat.getEphemeralOwner() > 0;
            Icon icon = getIcon(expanded, leaf, isEphemeral, text);
            
            JLabel label = (JLabel) comp;
            label.setIcon(icon);
            label.setForeground(isEphemeral ? Color.RED : Color.BLACK);
            label.setText(text);
            
            label.validate();
        }
        return comp;
    }

    /**
     * @param expanded
     * @param leaf
     * @param isEphemeral
     * @return
     */
    private Icon getIcon(boolean expanded, boolean leaf, boolean isEphemeral, String text) {
        Icon icon = null;
        if (leaf) {
            icon = isEphemeral ? Icons.NODE : Icons.LIST;
        } else {
            icon = expanded ? Icons.FOLDER_OPEN : Icons.FOLDER_CLOSE;
        }
        return icon;
    }
}