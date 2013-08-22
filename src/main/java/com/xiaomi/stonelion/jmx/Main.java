
package com.xiaomi.stonelion.jmx;

import java.lang.management.ManagementFactory;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import javax.management.MBeanServer;
import javax.management.ObjectName;

public class Main {
    public static void main(String[] args) throws Exception {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

        // MBean
        ObjectName name = new ObjectName("com.xiaomi.stonelion.jmx:type=Hello");
        Hello mbean = new Hello();
        mBeanServer.registerMBean(mbean, name);

        // MXBean
        name = new ObjectName("com.xiaomi.stonelion.jmx:type=QueueSampler");
        Queue<String> queue = new ArrayBlockingQueue<String>(10);
        queue.add("request-1");
        queue.add("request-2");
        queue.add("request-3");
        QueueSampler mxbean = new QueueSampler(queue);
        mBeanServer.registerMBean(mxbean, name);

        System.out.println("Wait forever...");
        Thread.sleep(Integer.MAX_VALUE);
    }
}
