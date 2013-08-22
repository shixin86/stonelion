
package com.xiaomi.stonelion.kafka;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.javaapi.producer.ProducerData;
import kafka.producer.ProducerConfig;

/**
 * http://kafka.apache.org/07/quickstart.html<br>
 * http://wiki.n.miliao.com/xmg/KafkaExamples<br>
 * 
 * @author shixin
 * @date Aug 7, 20138:06:30 PM
 * @Description 测试发送消息
 */
public class TestProducer {
    private static final String TOPIC = "sensei-staging";
    private static final int MESSGAE_COUNT = 1;

    public static void main(String[] args) throws JSONException {
        Properties properties = new Properties();
        properties.put("zk.connect", "10.101.10.40:2181/kafka");
        properties.put("serializer.class", "kafka.serializer.StringEncoder");

        ProducerConfig producerConfig = new ProducerConfig(properties);

        Producer<Integer, String> producer = new Producer<Integer, String>(producerConfig);

        // 循环发送数据
        for (int i = 0; i < MESSGAE_COUNT; i++) {
            JSONObject j = new JSONObject();
            j.put("changeKey", "SpatialChanged");
            j.put("userId", 1781049);
            j.put("lng", 116.31614);
            j.put("lat", 39.959661);

            String message = j.toString();
            List<String> data = new ArrayList<String>();
            data.add(message);

            ProducerData<Integer, String> producerData = new ProducerData<Integer, String>(TOPIC, new Integer(i), data);
            producer.send(producerData);
        }

        producer.close();
    }
}
