/**
 * [Copyright] 
 * @author oujinliang
 * Jun 13, 2011 8:28:21 PM
 */
package com.xiaomi.stonelion.zooviewer.ui;

import javax.swing.*;
import java.net.URL;

/**
 *
 */
public final class Icons {
    public static final Icon FOLDER_OPEN = createIcon("/icon/folder_open.gif");
    public static final Icon FOLDER_CLOSE = createIcon("/icon/folder_close.gif");
    public static final Icon FILE = createIcon("/icon/file.gif");
    public static final Icon LIST = createIcon("/icon/list.gif");
    public static final Icon TREE = createIcon("/icon/tree.gif");
    public static final Icon NODE = createIcon("/icon/node.gif");

    public static final Icon ADD = createIcon("/icon/add.gif");
    public static final Icon DELETE = createIcon("/icon/delete.gif");
    public static final Icon REFRESH = createIcon("/icon/refresh.gif");    
    public static final Icon UPDATE = createIcon("/icon/update.gif");    

    public static final Icon COLLAPASE_ALL = createIcon("/icon/collapseall.gif");
    public static final Icon EXPAND_ALL = createIcon("/icon/expandall.gif");
    
    private static ImageIcon createIcon(String path) {
        URL imageUrl = Icons.class.getResource(path);
        if(imageUrl==null)
        	System.out.println("null");
        return new ImageIcon(imageUrl);
    }
}
