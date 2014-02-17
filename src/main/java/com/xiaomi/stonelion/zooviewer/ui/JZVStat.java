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

import org.apache.zookeeper.data.Stat;
import seibuaa.platform.gui.container.FlowContainer;
import seibuaa.qesat.gui.AlignmentGridPanel;

import javax.swing.*;
import java.awt.*;
import java.text.DateFormat;
import java.util.Date;

public class JZVStat extends FlowContainer {
    /** */
    private static final long serialVersionUID = 1L;

    private final JTextField aversion = new JTextField();
    private final JTextField ctime = new JTextField();
    private final JTextField cversion = new JTextField();
    private final JTextField czxid = new JTextField();
    private final JTextField dataLength = new JTextField();
    private final JTextField ephemeralOwner = new JTextField();
    private final JTextField mtime = new JTextField();
    private final JTextField mzxid = new JTextField();
    private final JTextField numChildren = new JTextField();
    private final JTextField pzxid = new JTextField();
    private final JTextField version = new JTextField();

    private AlignmentGridPanel p1 = new AlignmentGridPanel();
    private AlignmentGridPanel p2 = new AlignmentGridPanel();
    private AlignmentGridPanel p3 = new AlignmentGridPanel();
    
    private final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, this.getLocale());

    public JZVStat() {
        super(FlowContainer.HORIZONTAL_TYPE);
        this.add(p1);
        this.add(p2);
        this.add(p3);
        
        addLine(p1, "aversion", aversion);
        addLine(p1, "ctime", ctime);
        addLine(p1, "cversion", cversion);
        addLine(p1, "czxid", czxid);
        setBorder(p1);
        
        addLine(p2, "dataLength", dataLength);
        addLine(p2, "ephemeralOwner", ephemeralOwner);
        addLine(p2, "mtime", mtime);
        addLine(p2, "mzxid", mzxid);
        setBorder(p2);
        
        addLine(p3, "numChildren", numChildren);
        addLine(p3, "pzxid", pzxid);
        addLine(p3, "version", version);
        setBorder(p3);
    }

    public void setStat(Stat stat) {
        if (stat == null) {
            System.out.println("stat null");
            this.aversion.setText("");
            this.ctime.setText("");
            this.cversion.setText("");
            this.czxid.setText("");
            this.dataLength.setText("");
            this.ephemeralOwner.setText("");
            this.mtime.setText("");
            this.mzxid.setText("");
            this.numChildren.setText("");
            this.pzxid.setText("");
            this.version.setText("");
        } else {
            System.out.println("stat = " + stat);

            this.aversion.setText(String.valueOf(stat.getAversion()));
            this.ctime.setText(this.DATE_FORMAT.format(new Date(stat.getCtime())));
            this.cversion.setText(String.valueOf(stat.getCversion()));
            this.czxid.setText(String.valueOf(stat.getCzxid()));
            this.dataLength.setText(String.valueOf(stat.getDataLength()));
            this.ephemeralOwner.setText(String.valueOf(stat.getEphemeralOwner()));
            this.mtime.setText(this.DATE_FORMAT.format(new Date(stat.getMtime())));
            this.mzxid.setText(String.valueOf(stat.getMzxid()));
            this.numChildren.setText(String.valueOf(stat.getNumChildren()));
            this.pzxid.setText(String.valueOf(stat.getPzxid()));
            this.version.setText(String.valueOf(stat.getVersion()));
        }
    }
    
    private void addLine(AlignmentGridPanel p, String name, JTextField comp) {
        comp.setEditable(false);
        p.addLine(new Component[] { new JLabel(name + ": "), comp} );
    }
    private void setBorder(AlignmentGridPanel p) {
        p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    }
}
