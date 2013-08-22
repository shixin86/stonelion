
package com.xiaomi.stonelion.hadoop;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class GetMutualFriendsMapper extends Mapper<LongWritable, Text, Text, Text> {

    @Override
    protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, Text>.Context context) throws IOException,
                                                                                                            InterruptedException {
        String line = value.toString();
        if (Validate.isEmpty(line)) {
            return;
        }

        String[] userIds = line.split(Constants.COLUMN_DELIMITER);

        if (!Validate.isEqualsToLength(userIds, 2)) {
            return;
        }

        context.write(new Text(userIds[0]), new Text(userIds[1]));
    };
}
