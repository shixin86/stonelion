/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.xiaomi.stonelion.zooviewer.ui;



import com.xiaomi.stonelion.zooviewer.model.ZVModel;
import com.xiaomi.stonelion.zooviewer.model.ZVNode;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;

public class JZVTree extends JTree {
    /** */
    private static final long serialVersionUID = 1L;

    public JZVTree(ZVModel model) {
        super(new ZVTreeModel(model));
    }

    public JZVTree(ZVTreeModel model) {
        super(model);
    }

    @Override
    public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row,
        boolean hasFocus) {
        if (value instanceof ZVNode) {
            return ((ZVNode) value).getName();
        }
        return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
    }
    
    public Action[] getActions() {
        AbstractAction expandAll =  new AbstractAction("Expand All", Icons.EXPAND_ALL) {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            public void actionPerformed(ActionEvent e) {
                Object root = getModel().getRoot();
                expandTree(new TreePath(root), root);
            }
        };
        
        AbstractAction collapseAll =  new AbstractAction("Collapse All", Icons.COLLAPASE_ALL) {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            public void actionPerformed(ActionEvent e) {
                Object root = getModel().getRoot();
                collapseTree(new TreePath(root), root);
            }
        };
        
        AbstractAction expandSelected =  new AbstractAction("Expand Selected", Icons.TREE) {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            public void actionPerformed(ActionEvent e) {
                TreePath path = getSelectionPath();
                if (path == null) {
                    return;
                }
                expandTree(path, path.getLastPathComponent());
            }
        };
        
        return new Action[] { expandAll, collapseAll, expandSelected };
    }

    
    private void expandTree(TreePath parentPath, Object parent) {
        this.expandPath(parentPath);
        
        for (int i = 0; i < this.getModel().getChildCount(parent); ++i) {
            Object child = this.getModel().getChild(parent, i);
            this.expandTree(parentPath.pathByAddingChild(child), child);
        }
        
    }
    
    private void collapseTree(TreePath parentPath, Object parent) {
        for (int i = 0; i < this.getModel().getChildCount(parent); ++i) {
            Object child = this.getModel().getChild(parent, i);
            this.collapseTree(parentPath.pathByAddingChild(child), child);
        }
        this.collapsePath(parentPath);
    }
  
}
