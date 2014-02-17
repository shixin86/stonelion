package com.xiaomi.stonelion.zooviewer.model;

import org.apache.zookeeper.data.Stat;

import java.beans.PropertyChangeListener;

/**
 * A ZVNode is a data fit requierements to be a ZooKeeper node.
 */
public interface ZVNode {
    final String PROPERTY_DATA = "data";
    final String PROPERTY_EXISTS = "exists";
    final String PROPERTY_CHILDREN = "children";
    final String PROPERTY_STAT = "stat";

    /**
     * Returns this node's path.
     *
     * @return the path
     */
    String getPath();

    /**
     * Returns this node's name.
     *
     * @return the name
     */
    String getName();

    /**
     * Returns this node's data.
     *
     * @return the data
     */
    byte[] getData();

    /**
     * Returns this node's stats.
     *
     * @return the stats
     */
    Stat getStat();

    /**
     * Checks if this node exists in the ZooKeeper model.
     *
     * @return
     */
    boolean exists();

    /**
     * Adds a {@link java.beans.PropertyChangeListener} to this node's listeners list.
     *
     * @param listener the listener
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Adds a {@link java.beans.PropertyChangeListener} to this node's listeners list.
     *
     * @param propertyName the property name
     * @param listener     the listener
     */
    void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

    /**
     * Remove the specified {@link java.beans.PropertyChangeListener} from this node's
     * listeners list.
     *
     * @param listener the listener
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Remove the specified {@link java.beans.PropertyChangeListener} from this node's
     * listeners list.
     *
     * @param the      property name
     * @param listener the listener
     */
    void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);
}
