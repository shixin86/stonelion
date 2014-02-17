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

package com.xiaomi.stonelion.zooviewer.model;

import com.xiaomi.stonelion.zooviewer.util.ConsoleLog;
import com.xiaomi.stonelion.zooviewer.util.ConsoleLog.Color;
import org.apache.zookeeper.AsyncCallback.ChildrenCallback;
import org.apache.zookeeper.*;
import org.apache.zookeeper.common.PathUtils;
import org.apache.zookeeper.data.Stat;

import javax.swing.event.EventListenerList;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Implementation of the ZooViewer model.
 *
 * @author franck
 */
public class ZVModelImpl implements ZVModel {
    private final ZkWatcher watcher;
    private final ZooKeeper zk;

    protected final EventListenerList listenerList = new EventListenerList();
    private final ExecutorService watcherExecutor = Executors.newSingleThreadExecutor();
    private final Map<String, ZVNodeImpl> nodes = new ConcurrentHashMap<String, ZVNodeImpl>();
    private final Map<ZVNodeImpl, List<ZVNodeImpl>> children = new ConcurrentHashMap<ZVNodeImpl, List<ZVNodeImpl>>();

    private ExecutorService es = Executors.newSingleThreadExecutor();

    public ZVModelImpl(String connectString) throws IOException {
        this.watcher = new ZkWatcher();
        this.zk = new ZooKeeper(connectString, 3000, this.watcher);

        ConsoleLog.println(ConsoleLog.Color.CYAN, "[" + Thread.currentThread() + "] AFTER ZK INIT");

        synchronized (watcher.lock) {
            while (watcher.dead) {
                try {
                    ConsoleLog.println(Color.CYAN, "[" + Thread.currentThread() + "Awaiting lock notification");
                    watcher.lock.wait();
                    ConsoleLog.println(Color.CYAN, "[" + Thread.currentThread() + "Lock notification, watcher.dead = " + watcher.dead);
                } catch (InterruptedException e) {
                    ConsoleLog.printException(e);
                }
            }
        }

        // Recursively call functions to get zookeeper data model
        populateRoot();
    }

    private final class ZkWatcher implements Watcher {
        private final Object lock = new Object();
        private volatile boolean dead = true;

        @Override
        public void process(WatchedEvent event) {
            ConsoleLog.println(Color.CYAN, "[" + Thread.currentThread() + "event : " + event);

            switch (event.getType()) {
                case None:
                    switch (event.getState()) {
                        case Disconnected:
                        case Expired:
                            ConsoleLog.println(Color.YELLOW, "[" + Thread.currentThread() + "Session has expired");

                            synchronized (lock) {
                                dead = true;
                                lock.notifyAll();
                            }
                            break;
                        case SyncConnected:
                            ConsoleLog.println(Color.GREEN, "[" + Thread.currentThread() + "Connected to the server");

                            synchronized (lock) {
                                dead = false;
                                lock.notifyAll();
                            }
                            break;
                    }
                    zk.register(this);
                    break;
                case NodeCreated:
                    ConsoleLog.println(Color.GREEN, "Node " + event.getPath() + " created");
                    break;
                case NodeChildrenChanged:
                    ConsoleLog.println(Color.CYAN, "Children changed for node " + event.getPath());
                    populateChildren(event.getPath(), 0);
                    break;
                case NodeDeleted:
                    ConsoleLog.println(Color.CYAN, "Node " + event.getPath() + " deleted");
                    nodeDeleted(event.getPath());
                    break;
                case NodeDataChanged:
                    ConsoleLog.println(Color.CYAN, "Data changed for node " + event.getPath());
                    nodeDataChanged(event.getPath());
                    break;
            }
        }
    }


    /*
     * (non-Javadoc)
     * @see net.isammoc.zooviewer.model.ZVModel#close()
     */
    @Override
    public void close() throws InterruptedException {
        ConsoleLog.println(Color.CYAN, "Closing ZooKeeper client...");
        zk.close();
        synchronized (watcher.lock) {
            watcher.dead = true;
            watcher.lock.notifyAll();
        }
        ConsoleLog.println(Color.CYAN, "Shutting down watcher...");
        watcherExecutor.shutdown();
        ConsoleLog.println(Color.CYAN, "Removing listeners...");
        ZVModelListener[] listeners = listenerList.getListeners(ZVModelListener.class);
        for (int i = 0; i < listeners.length; i++) {
            listenerList.remove(ZVModelListener.class, listeners[i]);
        }

        ConsoleLog.println(Color.CYAN, "Resetting models...");
        nodes.clear();
        children.clear();
        ConsoleLog.println(Color.GREEN, "Close done.");
    }

    /**
     * Called when a node has been deleted in the ZooKeeper model.
     *
     * @param path the node path
     */
    private synchronized void nodeDeleted(String path) {
        ZVNodeImpl oldNode = nodes.get(path);
        if (oldNode != null) {
            oldNode.setExists(false);
            oldNode.setStat(null);
            ZVNodeImpl parent = nodes.get(getParent(path));
            int oldIndex = children.get(parent).indexOf(oldNode);
            children.get(parent).remove(oldNode);
            fireNodeDeleted(oldNode, oldIndex);
        }
    }

    /**
     * Called when a node has been updated in the ZooKeeper model.
     *
     * @param path the node path
     */
    private synchronized void nodeDataChanged(String path) {
        ZVNodeImpl node = nodes.get(path);
        try {
            Stat stat = new Stat();
            node.setData(zk.getData(path, watcher, stat));
            node.setStat(stat);
            fireNodeDataChanged(node);
        } catch (KeeperException e) {
            ConsoleLog.printException(e);
        } catch (InterruptedException e) {
            ConsoleLog.printException(e);
        }
    }

    /**
     * Populates the root in this model.
     *
     * @param path the node path
     */
    private synchronized void populateRoot() {
        if (nodes.get("/") == null) {
            try {
                ConsoleLog.println(Color.CYAN, "[" + Thread.currentThread() + "Populating root..");

                Stat stat = new Stat();
                ZVNodeImpl root = new ZVNodeImpl("/", zk.getData("/", watcher, stat));
                root.setStat(stat);

                nodes.put("/", root);
                children.put(root, new ArrayList<ZVNodeImpl>());

                fireNodeCreated(root);

                populateChildren("/", 0);
            } catch (Exception e) {
                ConsoleLog.printException(e);
            }
        }
    }

    /**
     * Populates the children of the specified path. This function is always executed in one thread, why use
     * synchronized modifier?
     *
     * @param path
     */
    private void populateChildren(String path, final int layer) {
        /** Load just two layers at a time */
        if (layer > 1)
            return;

        ChildrenCallback cb = new ChildrenCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, List<String> childrenNames) {
                ZVNodeImpl parent = nodes.get(path);
                Stat stat = new Stat();
                try {
                    /** The stat is replicated set here */
                    parent.setStat(zk.exists(path, false));
                } catch (Exception ignore) {
                    ConsoleLog.printException(ignore);
                }

                Collections.sort(childrenNames);
                for (String childName : childrenNames) {
                    try {
                        final String childPath = getFullPath(path, childName);
                        ZVNodeImpl child = nodes.get(childPath);

                        if (child != null) {
                            if (!child.exists()) {
                                child.setData(zk.getData(childPath, watcher, stat));
                                child.setStat(stat);
                                child.setExists(true);

                                children.put(child, new ArrayList<ZVNodeImpl>());
                                children.get(parent).add(child);
                                fireNodeCreated(child);
                                populateChildren(childPath, layer + 1);
                            }

                            if (children.get(child) == null || children.get(child).size() == 0) {
                                populateChildren(childPath, layer + 1);
                            }
                        } else {
                            child = new ZVNodeImpl(childPath, null);

                            // Must set an empty stat to avoid NullPointerException
                            child.setStat(new Stat());

                            /** 推迟每个znode的value和stat实际获取的时间，优先抓取目录 */
                            scheduleDataFetch(childPath, child, layer);

                            nodes.put(childPath, child);
                            children.put(child, new ArrayList<ZVNodeImpl>());
                            children.get(parent).add(child);
                            fireNodeCreated(child);

                            Thread t = new Thread() {
                                public void run() {
                                    populateChildren(childPath, layer + 1);
                                }
                            };
                            t.start();
                        }
                    } catch (Exception ignore) {
                        ConsoleLog.printException(ignore);
                    }
                }
            }
        };

        zk.getChildren(path, watcher, cb, null);
    }

    /**
     * Use a single thread {@link java.util.concurrent.ExecutorService} to fetch the data, the priority of fetching data is lower than
     * fetching dir
     *
     * @param path
     * @param node
     */
    private void scheduleDataFetch(final String path, final ZVNodeImpl node, int priority) {

        Runnable run = new Runnable() {
            public void run() {
                Stat stat = new Stat();
                try {
                    node.setData(zk.getData(path, watcher, stat));
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        es.execute(run);
    }

    /*
     * (non-Javadoc)
     * @see net.isammoc.zooviewer.model.ZVModel#getFullPath(java.lang.String, java.lang.String)
     */
    @Override
    public String getFullPath(String parentPath, String childName) {
        return ("/".equals(parentPath) ? "/" : (parentPath + "/")) + childName;
    }

    /**
     * @param path
     * @return
     */
    private String getParent(String path) {
        if ("/".equals(path)) {
            return null;
        }
        int lastIndex = path.lastIndexOf("/");
        if (lastIndex > 0) {
            return path.substring(0, lastIndex);
        }

        return "/";
    }

    @Override
    public void addNode(String path, byte[] data) {
        if ((nodes.get(path) != null) && nodes.get(path).exists()) {
            throw new IllegalStateException("Node '" + path + "' already exists");
        }

        if ((nodes.get(getParent(path)) == null) || !nodes.get(getParent(path)).exists()) {
            throw new IllegalArgumentException("Node '" + path + "' can't be created. Its parent node doesn't exist");
        }

        try {
            zk.create(path, data, org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (Exception e) {
            ConsoleLog.printException(e);
        }
    }

    @Override
    public void deleteNode(ZVNode node) {
        String path = node.getPath();
        ConsoleLog.println(Color.YELLOW, "Delete requested on node " + path);

        PathUtils.validatePath(path);
        try {
            // Checks if the node has children
            List<String> childNodes = zk.getChildren(path, false);
            if (childNodes != null && childNodes.size() > 0) {
                // if the node has children, delete them recursively
                for (Iterator<String> iterator = childNodes.iterator(); iterator.hasNext(); ) {
                    String nodeName = iterator.next();
                    String childPath = path + (path.endsWith("/") ? "" : "/") + nodeName;
                    deleteNode(getNode(childPath));
                }
            }
            // finally, delete the node itself
            Stat stat = zk.exists(path, false);
            ConsoleLog.println(Color.YELLOW, "Deleting node " + path + "(stat =  " + stat);
            zk.delete(path, -1);
            Stat stat2 = zk.exists(path, false);
            ConsoleLog.println(Color.YELLOW, "Deleting node " + path + "(stat = " + stat2);
        } catch (Exception e) {
            ConsoleLog.printException(e);
        }
    }

    @Override
    public void deleteNodes(ZVNode[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            deleteNode(nodes[i]);
        }

    }

    @Override
    public void refresh(String path, boolean changed) {
        if (changed)
            nodeDataChanged(path);
        else
            populateChildren(path, 0);
    }

    @Override
    public void updateData(String path, byte[] data) {
        try {
            Stat stat = zk.setData(path, data, -1);
            nodes.get(path).setStat(stat);
        } catch (Exception e) {
            ConsoleLog.printException(e);
        }
    }

    @Override
    public ZVNode getNode(String path) {
        return nodes.get(path);
    }

    @Override
    public ZVNode getParent(ZVNode node) {
        return getNode(getParent(node.getPath()));
    }

    @Override
    public List<ZVNode> getChildren(ZVNode parent) {
        List<ZVNode> nodes = new java.util.concurrent.CopyOnWriteArrayList<ZVNode>();
        /** read while the map is been modified, leading to concurrent modification exception */
        List<ZVNodeImpl> list = new ArrayList<ZVNodeImpl>(children.get(parent));
        for (ZVNode node : list) {
            if (node.exists()) {
                nodes.add(node);
            }
        }
        return nodes;
    }

    @Override
    public void addModelListener(ZVModelListener listener) {
        listenerList.add(ZVModelListener.class, listener);
    }

    @Override
    public void removeModelListener(ZVModelListener listener) {
        listenerList.remove(ZVModelListener.class, listener);
    }

    protected void fireNodeCreated(ZVNode newNode) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ZVModelListener.class) {
                ((ZVModelListener) listeners[i + 1]).nodeCreated(newNode);
            }
        }
    }

    protected void fireNodeDeleted(ZVNode oldNode, int oldIndex) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ZVModelListener.class) {
                ((ZVModelListener) listeners[i + 1]).nodeDeleted(oldNode, oldIndex);
            }
        }
    }

    protected void fireNodeDataChanged(ZVNode node) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ZVModelListener.class) {
                ((ZVModelListener) listeners[i + 1]).nodeDataChanged(node);
            }
        }
    }
}
