/**
 * [Copyright] 
 * @author oujinliang
 * Jun 13, 2011 4:38:18 PM
 */
package com.xiaomi.stonelion.zooviewer.ui;


import com.xiaomi.stonelion.zooviewer.model.ZVModel;
import com.xiaomi.stonelion.zooviewer.model.ZVNode;
import seibuaa.platform.gui.AbstractView;
import seibuaa.platform.gui.container.BorderContainer;
import seibuaa.platform.gui.container.TitledComponent;
import seibuaa.qesat.gui.ShadowBorder;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This is the main frame of the J Zookeeper viewer.
 */
public class JZVMainFrame extends JFrame {
    private ZVModel model;

    private JZVNodePanel nodeView;
    private JZVTree tree;
    private ZVTreeModel tmodel;

    private JTextField text;
    private MyTreeExpansionListener mtel;


    public JZVMainFrame(String title, ZVModel model, boolean editable) {
        super(title);
        this.model = model;

        createNodeView(editable);
        createTree();
        buildUI();
    }

    private void createNodeView(boolean editable) {
        nodeView = new JZVNodePanel(model, editable);
    }

    private void createTree() {
        tmodel = new ZVTreeModel(model);
        tree = new JZVTree(tmodel);

        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                // Create the array of selections
                TreePath[] selPaths = tree.getSelectionModel().getSelectionPaths();
                if (selPaths == null) {
                    return;
                }
                ZVNode[] nodes = new ZVNode[selPaths.length];
                for (int i = 0; i < selPaths.length; i++) {
                    nodes[i] = (ZVNode) selPaths[i].getLastPathComponent();
                }
                nodeView.setNodes(nodes);
            }
        });
        mtel = new MyTreeExpansionListener();
        tree.addTreeExpansionListener(mtel);

        tree.setCellRenderer(new ZVTreeRenderer());
    }

    private void buildUI() {
        JPanel contentPanel = new ConetentPanel();
        this.setContentPane(contentPanel);
    }

    private class ConetentPanel extends JPanel {
        private static final long serialVersionUID = 8481594096344753787L;
        private BorderContainer bc;

        ConetentPanel() {
            setLayout(new BorderLayout());

            buildBorderContainer();

            text = new JTextField();
            text.addKeyListener(new MyKeyListener());
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(text, BorderLayout.NORTH);
            panel.add(new JScrollPane(tree), BorderLayout.CENTER);

            JComponent treePanel = buildTitlePane("Tree", Icons.TREE, panel, tree.getActions());
            JComponent nodePanel = buildTitlePane("Node Details", Icons.NODE, nodeView);

            add(bc, BorderLayout.CENTER);

            bc.add(treePanel, BorderLayout.WEST);
            bc.add(nodePanel, BorderLayout.CENTER);
        }

        private JComponent buildTitlePane(String name, Icon icon, JComponent comp, final Action... actions) {
            AbstractView view = new AbstractView(name, icon, comp) {
                @Override
                public Action[] getActions() {
                    return actions;
                }
            };

            TitledComponent p = new TitledComponent(view, false);

            p.setBorder(ShadowBorder.getBorder());
            return p;
        }

        private void buildBorderContainer() {
            bc = new BorderContainer();
            bc.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        }
    }

    private class MyTreeExpansionListener implements TreeExpansionListener {

        @Override
        public void treeExpanded(TreeExpansionEvent event) {
            // TODO Auto-generated method stub
            String path = event.getPath().getLastPathComponent().toString();
            int from = path.indexOf("'");
            int to = path.indexOf("'", from + 1);
            //System.out.println(path.substring(from+1, to));
            model.refresh(path.substring(from + 1, to), false);
        }

        @Override
        public void treeCollapsed(TreeExpansionEvent event) {
            // TODO Auto-generated method stub

        }

    }

    private class MyKeyListener implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {
            // TODO Auto-generated method stub

        }

        @Override
        public void keyPressed(KeyEvent e) {
            // TODO Auto-generated method stub

        }

        @Override
        public void keyReleased(KeyEvent e) {
            // TODO Auto-generated method stub
            if (e.getKeyCode() == 10) {

                // Get path and normalize the path
                String path = text.getText();
                path = path.replaceAll("/+", "/");
                if (!path.equals("/") && path.endsWith("/")) {
                    path = path.substring(0, path.length() - 1);
                }

                // Match the path against regex to test if its valid
                Pattern pattern = Pattern.compile("(/[a-zA-Z0-9\\._-]+)+");
                Matcher matcher = pattern.matcher(path);
                if (matcher.matches()) {

                    // Disable the tree expansion listeners;
                    TreeExpansionListener[] listeners = tree.getTreeExpansionListeners();
                    for (TreeExpansionListener listener : listeners) {
                        if (listener.equals(mtel))
                            tree.removeTreeExpansionListener(mtel);
                    }

                    pattern = Pattern.compile("(/[a-zA-Z0-9:\\._-]+)");
                    matcher = pattern.matcher(path);
                    String match = "";
                    while (matcher.find()) {
                        match += path.substring(matcher.start(), matcher.end());
                        //System.out.println(match);
                        try {
                            TreePath cur = tmodel.getTreePath(match);
                            tree.expandPath(cur);
                            Thread t = new RefreshThread(match);
                            t.start();
                            t.join(5000);
                            Thread.sleep(100);
                            tree.setSelectionPath(cur);
                        } catch (NullPointerException ex) {
                            ex.printStackTrace();
                            break;
                        } catch (InterruptedException ex) {
                            // TODO Auto-generated catch block
                            ex.printStackTrace();
                        }
                    }

                    // Restore listeners
                    tree.addTreeExpansionListener(mtel);

                } else {
                    System.out.println("no match found!");
                }
            }
        }
    }

    private class RefreshThread extends Thread {
        private String path;

        public RefreshThread(String path) {
            this.path = path;
        }

        @Override
        public void run() {
            model.refresh(path, false);
        }
    }
    ////10.235.153.3/db/msg_thread/matrix/p0/lock

}
