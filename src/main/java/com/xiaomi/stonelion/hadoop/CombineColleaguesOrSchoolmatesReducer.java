
package com.xiaomi.stonelion.hadoop;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;
import java.util.Iterator;


public class CombineColleaguesOrSchoolmatesReducer extends MapReduceBase implements Reducer<Text, Text, Text, Text> {

    public void reduce(Text arg0, Iterator<Text> arg1, OutputCollector<Text, Text> arg2, Reporter arg3) throws IOException {
      String line = arg0.toString();
      if (Validate.isEmpty(line)) {
          return;
      }
      String[] userIds = line.split(Constants.COLUMN_DELIMITER);
      if (!Validate.isEqualsToLength(userIds, 2)) {
          return;
      }

      StringBuilder sb = new StringBuilder();
      while(arg1.hasNext()){
          sb.append(arg1.next().toString() + Constants.COLUMN_DELIMITER);
      }
      if (0 == sb.length()) {
          return;
      } else {
          sb.deleteCharAt(sb.length() - 1);
      }

      arg2.collect(new Text(userIds[0]), new Text(userIds[1] + Constants.COLUMN_DELIMITER + sb.toString()));
    }
}
