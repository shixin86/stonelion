
package com.xiaomi.stonelion.jmx;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;

public class Hello extends NotificationBroadcasterSupport implements HelloMBean {

    private final String name = "shixin";
    private int cacheSize = 200;
    private long sequenceNumber = 1;

    @Override
    public void sayHello() {
        System.out.println("Hello!");
    }

    @Override
    public int add(int x, int y) {
        return x + y;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getCacheSize() {
        return this.cacheSize;
    }

    @Override
    public void setCacheSize(int cacheSize) {
        int oldCacheSize = this.cacheSize;
        this.cacheSize = cacheSize;
        System.out.println("Cache size now : " + this.cacheSize);

        // 发送通知
        Notification notification = new AttributeChangeNotification(this, sequenceNumber++, System.currentTimeMillis(),
                "Cache size changed.", "CacheSize", "int", oldCacheSize, this.cacheSize);
        sendNotification(notification);
    }

    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        String[] types = new String[] {
            AttributeChangeNotification.ATTRIBUTE_CHANGE
        };
        String name = AttributeChangeNotification.class.getName();
        String description = "An attribute of this MBean has changed.";
        MBeanNotificationInfo info = new MBeanNotificationInfo(types, name, description);

        return new MBeanNotificationInfo[] {
            info
        };
    }
}
