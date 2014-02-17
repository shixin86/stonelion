package com.xiaomi.stonelion.zooviewer;

import com.xiaomi.stonelion.zooviewer.model.ZVModel;
import com.xiaomi.stonelion.zooviewer.model.ZVModelImpl;
import com.xiaomi.stonelion.zooviewer.ui.JZVMainFrame;
import com.xiaomi.stonelion.zooviewer.util.ConsoleLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seibuaa.platform.gui.container.AbstractContainer;
import seibuaa.platform.gui.container.FlowContainer;
import seibuaa.qesat.gui.CButtonPane;
import seibuaa.qesat.gui.VerticalFlowLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class App {
    private static enum Env {
        ONEBOX(true, "zookeeper.n.miliao.com:2181"),
        STAGING(true, "10.237.12.2:2181"),
        SHANGDIPRODUCTION(true, "192.168.1.11:2181", "192.168.1.10:2181"),
        LUGUPRODUCTION(true, "lg-com-master01.bj:11000", "lg-com-master02.bj:11000"),
        GUIGUPRODUCTION(true, "10.20.2.13:2181", "10.20.2.36:2181"),
        STRESS(false, "10.237.14.213:2181", "10.237.14.214:2181");

        final boolean editable;
        final String[] connections;

        Env(boolean editable, String... connections) {
            this.editable = editable;
            this.connections = connections;
        }
    }

    private static Logger logger = LoggerFactory.getLogger(App.class);

    private static final String DEFAULT_CONNECTION_STRING = "192.168.98.89:2181";

    private static ResourceBundle bundle = ResourceBundle.getBundle(App.class.getCanonicalName());

    /**
     * @param args Accept only argument: the connection string of zookeeper server.
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        setLookAndFeel();

        Object[] connexionString = getConnectionString();
        String connectionStr = (String) connexionString[0];
        Env env = (Env) connexionString[1];
        boolean editable = isEditable(args, env);

        final ZVModel model = new ZVModelImpl((String) connexionString[0]);
        final JZVMainFrame mainFrame = new JZVMainFrame("ZooKeeper Editor - " + connectionStr + " " + env, model, editable);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1280, 800);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    logger.info("Closing the model...");
                    model.close();
                } catch (InterruptedException e1) {
                    logger.error("Close model failed.", e1);
                }
                mainFrame.dispose();
            }
        });
        mainFrame.setVisible(true);
    }

    private static Object[] getConnectionString() {
        ChooseConnectionStringDialog dialog = new ChooseConnectionStringDialog();
        dialog.setTitle("Select ZooKeeper Connection String");
        dialog.setSize(500, 400);
        dialog.setVisible(true);

        String connection = dialog.getConnectionString();
        Env env = dialog.getSelectedEnv();
        return new Object[]{connection, env};
    }

    private static boolean isEditable(String[] args, Env env) {
        if (env.editable) {
            return true;
        }
        if (args.length == 2 && args[0].equals("-p")) {
            return args[1].equals(getMd5());
        }
        return false;
    }

    static String getMd5(Date date) {
        String month = new SimpleDateFormat("yyyy-MM").format(date);
        String guid = "{EEA3F346-078B-44fb-9ED3-BCC30632D5D9}";
        byte[] source = (month + guid).getBytes();
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            md.update(source);
            byte tmp[] = md.digest();
            char str[] = new char[16 * 2];
            int k = 0;
            for (int i = 0; i < 16; i++) {
                byte byte0 = tmp[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            printException(e);
        }
        return "";
    }

    static String getMd5() {
        return getMd5(new Date());
    }

    /**
     * Currently we set it to GTK look and feel. if failed, it'll use Java default Metal look and feel.
     */
    private static void setLookAndFeel() {
        try {
            String osName = System.getProperty("os.name");

            if (osName.contains("Windows")) {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            } else if (osName.contains("Mac OS X")) {
                UIManager.setLookAndFeel("com.apple.laf.AquaLookAndFeel");
            } else {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
            }
        } catch (Exception e) {
            printException(e);
        }

        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
    }

    private static void printException(Throwable e) {
        ConsoleLog.printException(e);
    }

    @SuppressWarnings("serial")
    private static class ChooseConnectionStringDialog extends JDialog {
        JList envList;
        JList connList;
        JTextField connectionTextField;
        String connection;

        ChooseConnectionStringDialog() {
            setModal(true);
            initUI();
            envList.setSelectedIndex(0);

            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    setVisible(false);
                }
            });
        }

        private void initUI() {
            connectionTextField = new JTextField(DEFAULT_CONNECTION_STRING);

            envList = new JList(Env.values());
            envList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            envList.addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    Env env = (Env) envList.getSelectedValue();
                    connList.setListData(env.connections);
                    connList.setSelectedIndex(0);
                }
            });

            connList = new JList();
            connList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            connList.addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    String conn = (String) connList.getSelectedValue();
                    connectionTextField.setText(conn);
                }
            });

            JPanel content = new JPanel(new BorderLayout());

            // top
            FlowContainer top = new FlowContainer(AbstractContainer.HORIZONTAL_TYPE);
            top.setGap(10);

            JPanel topLeft = new JPanel(new BorderLayout(10, 10));
            topLeft.add(new JLabel("Environment"), BorderLayout.NORTH);
            topLeft.add(new JScrollPane(envList), BorderLayout.CENTER);

            JPanel topRight = new JPanel(new BorderLayout(10, 10));
            topRight.add(new JLabel("Connections"), BorderLayout.NORTH);
            topRight.add(new JScrollPane(connList), BorderLayout.CENTER);

            top.add(topLeft);
            top.add(topRight);
            top.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            content.add(top, BorderLayout.CENTER);

            JPanel south = new JPanel(new VerticalFlowLayout(VerticalFlowLayout.FILL, 10, 10));
            south.add(new JSeparator());
            south.add(new JLabel("Input the connection string"));
            south.add(connectionTextField);

            CButtonPane btnPane = new CButtonPane(CButtonPane.TAIL);

            JButton ok = new JButton("Ok");
            ok.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    connection = connectionTextField.getText();
                    setVisible(false);
                }
            });

            JButton cancel = new JButton("Cancel");
            cancel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                    System.exit(0);
                }
            });

            btnPane.add(cancel);
            btnPane.add(ok);
            south.add(btnPane);

            content.add(south, BorderLayout.SOUTH);

            this.setContentPane(content);
        }

        public Env getSelectedEnv() {
            return (Env) envList.getSelectedValue();
        }

        public String getConnectionString() {
            return connection == null || connection.isEmpty() ? DEFAULT_CONNECTION_STRING : connection;
        }
    }
}
