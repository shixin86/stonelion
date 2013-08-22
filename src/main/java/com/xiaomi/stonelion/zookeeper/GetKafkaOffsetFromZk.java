
package com.xiaomi.stonelion.zookeeper;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.BytesPushThroughSerializer;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 * 项目中用到的一个类，从zk上读取kafka的offset.
 * 
 * @author shixin
 * @date Aug 22, 20131:42:27 PM
 * @Description TODO
 */
public class GetKafkaOffsetFromZk {
    private static final Logger logger = LoggerFactory.getLogger(GetKafkaOffsetFromZk.class);

    private static boolean isDebug = true;

    private static final String KAFKA_RESOURCE = "kafka.properties";
    private static final String RES_KEY_KAFKA_TOPIC = "kafka.topic";
    private static final String RES_KEY_KAFKA_CONSUMER_GROUP_ID = "kafka.consumer.group.id";
    private static final String RES_KEY_KAFKA_ZK_SERVERS = "kafka.zk.servers";

    private static String kafkaZkServers;
    private static String kafkaConsumerGroupId;
    private static String kafkaTopic;

    private static final int ZKCLIENT_SESSION_TIMEOUT = 30000;
    private static final int ZKCLIENT_CONNECTION_TIMEOUT = 30000;

    private static ZkClient zkClient;

    public static void main(String[] args) {
        // load properties.
        Properties properties = new Properties();
        if (!isDebug) {
            try {
                properties.load(new FileReader(new File(KAFKA_RESOURCE)));
            } catch (Exception e) {
                throw new RuntimeException("Read properties file error.");
            }
        }

        kafkaZkServers = properties.getProperty(RES_KEY_KAFKA_ZK_SERVERS, "10.101.10.40:2181");
        kafkaConsumerGroupId = properties.getProperty(RES_KEY_KAFKA_CONSUMER_GROUP_ID, "SenseiGatewayConsumerGroupId1");
        kafkaTopic = properties.getProperty(RES_KEY_KAFKA_TOPIC, "sensei-staging");

        logger.info("Kafka zk servers:{}, topic:{}, consumer group id:{}", new Object[] {
            kafkaZkServers, kafkaTopic, kafkaConsumerGroupId
        });

        // create zk client.
        zkClient = new ZkClient(kafkaZkServers, ZKCLIENT_SESSION_TIMEOUT, ZKCLIENT_CONNECTION_TIMEOUT, new BytesPushThroughSerializer());
        Validate.notNull(zkClient, "Cant create zk client instance.");

        Map<String, String> offsets = new HashMap<String, String>();
        Set<String> paths = getKafkaOffsetPaths(kafkaConsumerGroupId, kafkaTopic);
        for (String path : paths) {
            if (zkClient.exists(path)) {
                byte[] bytes = zkClient.readData(path);
                String value = (String) ZKSerializeHelper.find(String.class).deserialize(bytes);
                offsets.put(path.substring(path.lastIndexOf("/")), value);
            }
        }

        File outputFile = new File("/data/soft/senseiUsers/offset.txt");
        BufferedWriter br = null;
        try {
            br = new BufferedWriter(new FileWriter(outputFile));
            for (Entry<String, String> entry : offsets.entrySet()) {
                br.write(entry.getKey() + ":" + entry.getValue() + "\n\r");
            }
        } catch (IOException e) {
            throw new RuntimeException("Wrirte offset file error.");
        } finally {
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    throw new RuntimeException("Close file writer error.");
                }
            }
        }
    }

    private static Set<String> getKafkaOffsetPaths(String consumerGroupId, String topic) {
        Set<String> paths = new HashSet<String>();
        for (int node = 1; node < 3; node++) {
            for (int partition = 0; partition < 10; partition++) {
                paths.add("/kafka/consumers/" + consumerGroupId + "/offsets/" + topic + "/" + node + "-" + partition);
            }
        }
        return paths;
    }
}
