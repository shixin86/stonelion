
package com.xiaomi.stonelion.jmx;

public interface QueueSamplerMXBean {
    public QueueSample getQueueSample();

    public void clearQueue();
}
