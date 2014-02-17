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
import com.xiaomi.stonelion.zooviewer.model.ZVModelListener;
import com.xiaomi.stonelion.zooviewer.model.ZVNode;
import org.apache.commons.lang.StringUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import seibuaa.qesat.gui.AlignmentGridPanel;
import seibuaa.qesat.gui.CButtonPane;
import seibuaa.qesat.gui.VerticalFlowLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.charset.Charset;
import java.util.ResourceBundle;

/**
 * Editor panel for a node.
 *
 * @author franck
 */
public class JZVNodePanel extends JPanel {
    private static final String ADD_CHILD_NODE_KEY = "btn.add.child";
    private static final String UPDATE_NODE_KEY = "btn.update";
    private static final String DELETE_NODE_KEY = "btn.delete";
    private static final String EDITABLE_NODE_KEY = "btn.editable";
    private static final String REFRESH_NODE_KEY = "btn.refresh";

    /** */
    private static final long serialVersionUID = 1L;

    private static final ResourceBundle bundle = ResourceBundle.getBundle(JZVNodePanel.class.getCanonicalName());


    private ZVNode[] nodes;
    private final ZVModel model;

    private final JButton jbNewChild = new JButton();
    private final JButton jbUpdate = new JButton();
    private final JButton jbDelete = new JButton();
    private final JButton jbRefresh = new JButton();
    private final JCheckBox jbEnableEdit = new JCheckBox();

    private final JZVStat jzvStat = new JZVStat();
    private final RSyntaxTextArea taUpdate = new RSyntaxTextArea();

    private String childName = "";
    private String childText = "";

    private boolean editable = false;
    private boolean editCheckboxEnable = false;

    private Action addChildAction = null;
    private Action updateAction = null;
    private Action deleteAction = null;
    private Action editableAction = null;
    private Action refreshAction = null;

    private final JTextField pathText = new JTextField();

    private final PropertyChangeListener propertyListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            updateView();
        }
    };

    /**
     * Constructs a new editor panel.
     *
     * @param model the model
     */
    public JZVNodePanel(ZVModel model, boolean editable) {
        super(new BorderLayout());

        this.editCheckboxEnable = editable;
        this.model = model;
        this.model.addModelListener(new RefreshZVModelListener());

        // Actions
        this.jbDelete.setAction(getDeleteAction());
        this.jbNewChild.setAction(getAddChildAction());
        this.jbUpdate.setAction(getUpdateAction());
        this.jbEnableEdit.setAction(getEditableAction());
        this.jbRefresh.setAction(getRefreshAction());

        // Main content
        this.initUI();
        this.updateView();

        Dimension prefSize = this.jbNewChild.getPreferredSize();
        this.jbDelete.setPreferredSize(prefSize);
        this.jbNewChild.setPreferredSize(prefSize);
        this.jbUpdate.setPreferredSize(prefSize);

        initListeners();
    }

    /**
     * Returns the main node panel.
     *
     * @return the panel
     */
    private void initUI() {

        // TOP;
        JPanel top = new JPanel();
        top.setLayout(new VerticalFlowLayout(VerticalFlowLayout.FILL));
        top.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 10));
        // Top node path.
        AlignmentGridPanel pathPanel = new AlignmentGridPanel();
        pathText.setEditable(false);
        pathPanel.addLine(new Component[]{new JLabel("Path: "), pathText});

        // Top buttons
        CButtonPane topButtons = new CButtonPane(CButtonPane.HORIZONTAL, CButtonPane.HEAD);
        topButtons.add(jbDelete);
        topButtons.add(jbNewChild);
        topButtons.add(jbUpdate);
        topButtons.add(jbRefresh);

        topButtons.add(jbEnableEdit, CButtonPane.TAIL);
        top.add(pathPanel);
        top.add(new JSeparator());
        top.add(this.jzvStat);
        top.add(new JSeparator());
        top.add(topButtons);
        //top.add(new JSeparator());

        // Center.
        JPanel center = new JPanel(new BorderLayout(5, 5));
        center.add(new RTextScrollPane(this.taUpdate), "Center");

        center.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        this.add(top, "North");
        this.add(center, "Center");
    }

    @SuppressWarnings("serial")
    private Action getEditableAction() {
        if (editableAction == null) {
            String actionCommand = bundle.getString(EDITABLE_NODE_KEY);
            String actionKey = bundle.getString(EDITABLE_NODE_KEY + ".action");
            editableAction = new AbstractAction(actionCommand) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (editCheckboxEnable) {
                        editable = !editable;
                        updateView();
                    }
                }
            };
            editableAction.setEnabled(editCheckboxEnable);
            editableAction.putValue(Action.ACTION_COMMAND_KEY, actionKey);
        }
        return this.editableAction;
    }

    /**
     * Returns the 'Add child' action.
     *
     * @return the action
     */
    @SuppressWarnings("serial")
    private Action getAddChildAction() {
        if (addChildAction == null) {
            String actionCommand = bundle.getString(ADD_CHILD_NODE_KEY);
            String actionKey = bundle.getString(ADD_CHILD_NODE_KEY + ".action");
            addChildAction = new AbstractAction(actionCommand, Icons.ADD) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("actionPerformed(): action = " + e.getActionCommand());
                    if (checkAction()) {
                        model.addNode(StringUtils.removeEnd(nodes[0].getPath(), "/") + "/" + childName, childText.getBytes());
                    }
                    childName = childText = "";
                }

                private boolean checkAction() {
                    JDialog dlg = createAddChildDialog();

                    dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    dlg.setSize(800, 600);
                    dlg.setLocationRelativeTo(null);
                    dlg.setVisible(true);

                    boolean nameIsEmpty = childName == null || childName.isEmpty();
                    boolean dataIsEmpty = childText == null;
                    return !nameIsEmpty && !dataIsEmpty;
                }
            };
            addChildAction.putValue(Action.ACTION_COMMAND_KEY, actionKey);
        }
        return this.addChildAction;
    }

    /**
     * Returns the 'Update' action.
     *
     * @return the action
     */
    @SuppressWarnings("serial")
    private Action getUpdateAction() {
        if (updateAction == null) {
            String actionCommand = bundle.getString(UPDATE_NODE_KEY);
            String actionKey = bundle.getString(UPDATE_NODE_KEY + ".action");
            updateAction = new AbstractAction(actionCommand, Icons.UPDATE) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("actionPerformed(): action = " + e.getActionCommand());
                    if (checkAction()) {
                        model.updateData(nodes[0].getPath(), taUpdate.getText().getBytes(getCharset()));
                    }
                }

                private boolean checkAction() {
                    // No node or several nodes selected
                    if (nodes == null || nodes.length > 1) {
                        return false;
                    }
                    // No parent
                    if (nodes == null || nodes.length != 1) {
                        JOptionPane.showMessageDialog(JZVNodePanel.this, bundle.getString("dlg.error.updateWithoutParent"),
                                bundle.getString("dlg.error.title"), JOptionPane.ERROR_MESSAGE);
                        return false;
                    }

                    return (JOptionPane.showConfirmDialog(JZVNodePanel.this, bundle.getString("dlg.confirm.update"))
                            == JOptionPane.YES_OPTION);
                }
            };
            updateAction.putValue(Action.ACTION_COMMAND_KEY, actionKey);
        }
        return updateAction;
    }

    /**
     * Returns the 'Delete node(s)' action.
     * <p>
     * The action is created and mapped to the [Delete] key stroke
     * </p>
     *
     * @return the action
     */
    @SuppressWarnings("serial")
    private Action getDeleteAction() {
        if (this.deleteAction == null) {
            String actionCommand = bundle.getString(DELETE_NODE_KEY);
            String actionKey = bundle.getString(DELETE_NODE_KEY + ".action");
            this.deleteAction = new AbstractAction(actionCommand, Icons.DELETE) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("actionPerformed(): action = " + e.getActionCommand());
                    if (checkAction()) {
                        // Checks if several nodes will be deleted
                        if (nodes.length > 1) {
                            model.deleteNodes(nodes);
                        } else {
                            model.deleteNode(nodes[0]);
                        }
                    }
                }

                private boolean checkAction() {
                    // No node selected
                    if (nodes == null) {
                        JOptionPane.showMessageDialog(JZVNodePanel.this, bundle
                                .getString("dlg.error.deleteWithoutSelection"), bundle.getString("dlg.error.title"),
                                JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                    return (JOptionPane.showConfirmDialog(JZVNodePanel.this, bundle.getString("dlg.confirm.update"))
                            == JOptionPane.YES_OPTION);
                }
            };
            this.deleteAction.putValue(Action.ACTION_COMMAND_KEY, actionKey);

            this.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), actionKey);
            this.getActionMap().put(actionKey, this.deleteAction);
        }
        return this.deleteAction;
    }

    private Action getRefreshAction() {
        if (this.refreshAction == null) {
            String actionCommand = bundle.getString(REFRESH_NODE_KEY);
            String actionKey = bundle.getString(REFRESH_NODE_KEY + ".action");
            this.refreshAction = new AbstractAction(actionCommand, Icons.REFRESH) {
                /**
                 *
                 */
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    model.refresh(nodes[0].getPath(), true);
                }
            };

            this.refreshAction.putValue(Action.ACTION_COMMAND_KEY, actionKey);
            this.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), actionKey);
            this.getActionMap().put(actionKey, this.refreshAction);
        }
        return this.refreshAction;
    }

    /**
     * Defines the list of selected nodes.
     *
     * @param nodes the selected nodes
     */
    public void setNodes(ZVNode[] nodes) {
        if (this.nodes != null) {
            for (int i = 0; i < this.nodes.length; i++) {
                this.nodes[i].removePropertyChangeListener(ZVNode.PROPERTY_EXISTS, this.propertyListener);
            }
        }
        this.nodes = nodes;
        if (this.nodes != null) {
            for (int i = 0; i < this.nodes.length; i++) {
                this.nodes[i].addPropertyChangeListener(ZVNode.PROPERTY_EXISTS, this.propertyListener);
            }
        }
        this.updateView();
    }

    private void initListeners() {
        taUpdate.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                enableAction(e);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                enableAction(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                enableAction(e);
            }

            private void enableAction(DocumentEvent e) {
                boolean enabled = e.getDocument().getLength() > 0;
                getUpdateAction().setEnabled(enabled);
            }
        });
    }

    /**
     * Updates the view.
     * <p>
     * If a node is selected, its data & stats are displayed. If no node is
     * selected (or several nodes are selected), the view is cleared.
     * </p>
     */
    private void updateView() {
        if (this.nodes == null || this.nodes.length > 1 || !this.nodes[0].exists()) {
            this.pathText.setText("-");
            this.jzvStat.setStat(null);
            this.taUpdate.setText("");
            //this.taChildData.setText("");
            this.jbUpdate.setEnabled(false);
            this.jbNewChild.setEnabled(false);
            this.jbDelete.setEnabled(editable && this.nodes != null);
        } else {
            this.pathText.setText(this.nodes[0].getPath());
            this.jzvStat.setStat(this.nodes[0].getStat());
            byte[] data = this.nodes[0].getData();

            String content = new String(data == null ? "null".getBytes() : data, getCharset());
            this.taUpdate.setSyntaxEditingStyle(getContentType(nodes[0].getName(), content));
            //this.taUpdate.setDocument(this.taUpdate.createDefaultDocument());
            this.taUpdate.setText(content);
            //this.taChildData.setText("");
            this.jbUpdate.setEnabled(editable && !this.taUpdate.getText().trim().equals(""));
            this.jbNewChild.setEnabled(editable);
            this.jbDelete.setEnabled(editable);
        }
        taUpdate.setEditable(editable);
        this.repaint();
    }

    private String getContentType(String name, String content) {
        String lowername = name.toLowerCase();
        if (lowername.endsWith(".xml")) {
            return SyntaxConstants.SYNTAX_STYLE_XML;
        }
        if (lowername.endsWith(".java")) {
            return SyntaxConstants.SYNTAX_STYLE_JAVA;
        }

        String firstWord = findFirstWord(content);
        if (firstWord.matches("^<\\??\\w+")) {
            return SyntaxConstants.SYNTAX_STYLE_XML;
        }
        return SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE;
    }

    /**
     * @param content
     * @return
     */
    private String findFirstWord(String content) {
        int start = 0;
        while (start < content.length() && Character.isWhitespace(content.charAt(start))) {
            ++start;
        }
        int end = start;
        while (end < content.length() && !Character.isWhitespace(content.charAt(end))) {
            end++;
        }
        return content.substring(start, end);
    }

    private JDialog createAddChildDialog() {
        final JDialog dlg = new JDialog();
        dlg.setModal(true);

        // Content
        final JPanel content = new JPanel();
        content.setLayout(new BorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // child name
        AlignmentGridPanel pathPanel = new AlignmentGridPanel();

        final JTextField childNameText = new JTextField();
        final RSyntaxTextArea childDataArea = new RSyntaxTextArea();

        final JMenuBar bar = new JMenuBar();
        final JMenu menu = new JMenu("Syntax Highlight");
        bar.add(menu);

        setMenuItem(menu, childDataArea, SyntaxConstants.SYNTAX_STYLE_XML);
        setMenuItem(menu, childDataArea, SyntaxConstants.SYNTAX_STYLE_JAVA);
        setMenuItem(menu, childDataArea, SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE);

        pathPanel.addLine(new Component[]{new JLabel("Select:"), bar});
        pathPanel.addLine(new Component[]{new JLabel("Name: "), childNameText});
        pathPanel.addLine(new Component[]{new JLabel("Data: ")});

        content.add(pathPanel, BorderLayout.NORTH);
        content.add(new RTextScrollPane(childDataArea), BorderLayout.CENTER);

        childDataArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE);
        // Buttons.
        final CButtonPane btnPanel = new CButtonPane(CButtonPane.TAIL);
        final JButton btnOk = new JButton("Ok");
        btnOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = childNameText.getText();
                String data = childDataArea.getText();

                if ((name == null || name.isEmpty()) || (data == null || data.isEmpty())) {
                    JOptionPane.showMessageDialog(null, "Name or Data is empty", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                childName = name;
                childText = data;
                dlg.setVisible(false);
                dlg.dispose();
            }
        });

        final JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                childName = "";
                childText = "";
                dlg.setVisible(false);
                dlg.dispose();
            }
        });
        btnPanel.add(btnCancel);
        btnPanel.add(btnOk);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Container con = dlg.getContentPane();
        con.setLayout(new BorderLayout());

        con.add(content, BorderLayout.CENTER);
        con.add(btnPanel, BorderLayout.SOUTH);

        return dlg;
    }

    private Charset getCharset() {
        return Charset.forName("UTF-8");
    }

    private void setMenuItem(final JMenu menu, final RSyntaxTextArea childDataArea, final String type) {
        JMenuItem item = new JMenuItem(type);
        menu.add(item);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String content = childDataArea.getText();
                childDataArea.setSyntaxEditingStyle(type);
                childDataArea.setText(content);
            }
        });
    }

    /**
     * Class managing events in order to update the view.
     */
    private final class RefreshZVModelListener implements ZVModelListener {
        @Override
        public void nodeDeleted(ZVNode oldNode, int oldIndex) {
            if (nodes != null) {
                for (int i = 0; i < nodes.length; i++) {
                    if ((nodes[i] == oldNode) || (nodes[i] == model.getParent(oldNode))) {
                        updateView();
                        break;
                    }
                }
            }
        }

        @Override
        public void nodeDataChanged(ZVNode node) {
            boolean updateView = false;
            if (nodes != null) {
                for (int i = 0; i < nodes.length; i++) {
                    if ((nodes[i] == node)) {
                        updateView = true;
                    }
                }
            }
            if (updateView) {
                updateView();
            }
        }

        @Override
        public void nodeCreated(ZVNode newNode) {
            boolean updateView = false;
            if (nodes != null) {
                for (int i = 0; i < nodes.length; i++) {
                    if ((nodes[i] == newNode)) {
                        updateView = true;
                    }
                }
            }
            if (updateView) {
                updateView();
            }
        }
    }
}
