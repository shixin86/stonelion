package com.xiaomi.stonelion.zooviewer.model;

import java.util.List;


/**
 * Interface to the ZooViewer model.
 *
 * @author franck
 */
public interface ZVModel {
    /**
     * Adds a {@link ZVModelListener} to the listener list.
     *
     * @param listener the {@link ZVModelListener} to be added
     */
    void addModelListener(ZVModelListener listener);

    /**
     * Removes a {@link ZVModelListener} from the listener list.
     *
     * @param listener the {@link ZVModelListener} to be added
     */
    void removeModelListener(ZVModelListener listener);

    /**
     * Adds a node to the ZooKeeper model.
     *
     * @param path the node path
     * @param data the node data
     */
    void addNode(String path, byte[] data);

    /**
     * Updates a node's data in the ZooKeeper model.
     *
     * @param path the node path
     * @param data the node data
     */
    void updateData(String path, byte[] data);

    /**
     * Deletes a node and his children.
     *
     * @param node the node to be deleted
     */
    void deleteNode(ZVNode node);

    /**
     * Deletes a list of nodes and their children.
     *
     * @param paths the nodes to be deleted
     */
    void deleteNodes(ZVNode[] nodes);

    /**
     * @param path
     * @param changed If data ever changed
     */
    void refresh(String path, boolean changed);

    /**
     * Returns a {@link ZVNode} corresponding to the specified path.
     *
     * @param path the node path
     * @return the {@link ZVNode} instance, or <code>null</code> if the node doesn't exist
     */
    ZVNode getNode(String path);

    /**
     * Returns the parent of the specified {@link ZVNode}.
     *
     * @param node the node
     * @return the parent {@link ZVNode}
     */
    ZVNode getParent(ZVNode node);

    /**
     * Returns the list of child nodes under the specified parent.
     *
     * @param parent the parent node
     * @return the list of child nodes, or an empty list if parent has no children
     */
    List<ZVNode> getChildren(ZVNode parent);

    /**
     * Returns a full path from a parent node and name of child.
     *
     * @param parentPath Parent path
     * @param childName  Child name
     * @return full path
     */
    String getFullPath(String parentPath, String childName);

    /**
     * Closes the ZooKeeper connection.
     *
     * @throws InterruptedException
     */
    void close() throws InterruptedException;
}