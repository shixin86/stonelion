
package com.xiaomi.stonelion.kafka;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.Message;
import kafka.message.MessageAndMetadata;

/**
 * WIKI: http://wiki.n.miliao.com/xmg/KafkaExamples<br>
 * 
 * @author shixin
 * @date Aug 7, 20135:04:18 PM
 * @Description 测试消費消息
 */
public class TestConsumer {
    private static final String GROUP_ID = "shixin-consumer";
    private static final String TOPIC = "sensei-staging";
    private static final int PARTITION = 10;
    private static ExecutorService executor = Executors.newFixedThreadPool(PARTITION);

    private static ConsumerConfig createConsumerConfig() {
        Properties props = new Properties();
        // zk connection
        props.put("zk.connect", "10.101.10.40:2181/kafka");
        // Same group id means consume message separately
        props.put("groupid", GROUP_ID);
        // The zookeeper session timeout.
        props.put("zk.sessiontimeout.ms", "400");
        // Max time for how far a ZK follower can be behind a ZK leader
        props.put("zk.synctime.ms", "200");
        // The time interval at which to save the current offset in ms
        props.put("autocommit.interval.ms", "1000");

        // Consumer timeout, if not set consumer will wait forever
        // props.put("consumer.timeout.ms", "5000");

        return new ConsumerConfig(props);
    }

    public static void main(String[] args) {
        ConsumerConfig consumerConfig = createConsumerConfig();
        ConsumerConnector consumerConnector = Consumer.createJavaConsumerConnector(consumerConfig);

        // reset offset
        Map<String, Long> partitionOffsetMap = new HashMap<String, Long>();
        for (int i = 0; i < PARTITION; i++) {
            partitionOffsetMap.put("1-" + i, 0l);
            partitionOffsetMap.put("2-" + i, 0l);
        }
        consumerConnector.commitOffsets(TOPIC, partitionOffsetMap);

        // consumeWithSingleThread(consumerConnector, "Topic1");
        consumeWithMultiThreads(consumerConnector, TOPIC, PARTITION);
    }

    /**
     * topicCountMap设置了对某个topic需要读取几个stream，多个stream的话适合多线程工作。
     * 这里只设置了一个stream，还有partition是kafka的数据处理单元，所以stream的number一定要小于等于partition number否则会有些stream无数据可读
     * 
     * @param consumerConnector
     * @param topic
     */
    private static void consumeWithSingleThread(ConsumerConnector consumerConnector, String topic) {
        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        // get all message in one stream
        topicCountMap.put(topic, new Integer(1));
        Map<String, List<KafkaStream<Message>>> consumerMap = consumerConnector.createMessageStreams(topicCountMap);
        KafkaStream<Message> stream = consumerMap.get(topic).get(0);
        ConsumerIterator<Message> it = stream.iterator();
        while (true) {
            try {
                if (!it.hasNext()) {
                    break;
                }
            } catch (Exception e) {
                // Most likely timeout exception
                break;
            }
            MessageAndMetadata<Message> messageAndMetadata = it.next();
            System.out.println(getMessage(messageAndMetadata.message()));
        }

        consumerConnector.shutdown();

    }

    /**
     * 给每个partition一个单独的stream
     * 
     * @param consumerConnector
     * @param topic
     */
    private static void consumeWithMultiThreads(ConsumerConnector consumerConnector, String topic, int partitionCount) {
        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(topic, partitionCount);

        Map<String, List<KafkaStream<Message>>> topicMessageStreams = consumerConnector.createMessageStreams(topicCountMap);
        List<KafkaStream<Message>> streams = topicMessageStreams.get(topic);

        for (final KafkaStream<Message> stream : streams) {
            executor.execute(new MessageConsumer(stream));
        }
    }

    private static class MessageConsumer implements Runnable {
        private KafkaStream<Message> messageStream;

        public MessageConsumer(KafkaStream<Message> messageStream) {
            this.messageStream = messageStream;
        }

        @Override
        public void run() {
            System.out.println("Kafka consumer thread started: " + Thread.currentThread().getId());

            ConsumerIterator<Message> consumerIterator = this.messageStream.iterator();
            if (consumerIterator != null) {
                while (consumerIterator.hasNext()) {
                    MessageAndMetadata<Message> messageAndMetadata = consumerIterator.next();

                    int size = messageAndMetadata.message().payloadSize();
                    ByteBuffer byteBuffer = messageAndMetadata.message().payload();
                    byte[] bytes = new byte[size];
                    byteBuffer.get(bytes, 0, size);

                    System.out.println("Kafka consumer thread " + Thread.currentThread().getId() + " get message " + new String(bytes));
                }
            }
        }
    }

    private static String getMessage(Message message) {
        ByteBuffer buffer = message.payload();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return new String(bytes);
    }
}
