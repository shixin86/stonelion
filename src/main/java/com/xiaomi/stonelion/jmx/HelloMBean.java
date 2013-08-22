
package com.xiaomi.stonelion.jmx;

public interface HelloMBean {
    public void sayHello();

    public int add(int x, int y);

    public String getName();

    public int getCacheSize();

    public void setCacheSize(int cacheSize);
}
