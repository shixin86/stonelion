
package com.xiaomi.stonelion.hadoop;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;

public class CombineMutualFriendsMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {

    public void map(LongWritable arg0, Text arg1, OutputCollector<Text, Text> arg2, Reporter arg3) throws IOException {
      String line = arg1.toString();
      if (Validate.isEmpty(line)) {
          return;
      }

      String[] values = line.split("@");

      arg2.collect(new Text(values[0]), new Text(values[1]));
    }
}
