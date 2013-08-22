
package com.xiaomi.stonelion.hadoop;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



public class CombineAllUsersReducer extends MapReduceBase implements Reducer<Text, Text, Text, Text> {

    public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> context, Reporter arg3) throws IOException {
      String keyValue = key.toString();
      if (Validate.isEmpty(keyValue)) {
          return;
      }

      String refused = null;
      String lastActivityTime = null;
      List<String> normalValues = new ArrayList<String>();
      while (values.hasNext()) {
          Text one = values.next();
          if (one.toString().startsWith(Constants.REFUSE_PREFIX)) {
              refused = one.toString();
          } else if (one.toString().startsWith(Constants.LASTACTIVITYTIME_PREFIX)) {
              lastActivityTime = one.toString();
          } else {
              normalValues.add(one.toString());
          }
      }

      for (String normalValue : normalValues) {
          String userId = normalValue.substring(0, normalValue.indexOf(Constants.COLUMN_DELIMITER));
          String userIdEnd = normalValue.substring(normalValue.indexOf(Constants.COLUMN_DELIMITER));
          StringBuilder sb = new StringBuilder(key + userIdEnd);
          if (null != refused) {
              sb.append(Constants.COLUMN_DELIMITER + refused);
          }
          if (null != lastActivityTime) {
              sb.append(Constants.COLUMN_DELIMITER + lastActivityTime);
          }
          context.collect(new Text(userId), new Text(sb.toString()));
      }
    }
}
