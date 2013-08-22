
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


public class CombineMutualFriendsReducer extends MapReduceBase implements Reducer<Text, Text, Text, Text> {

    public void reduce(Text arg0, Iterator<Text> arg1, OutputCollector<Text, Text> arg2, Reporter arg3) throws IOException {
        // TODO Auto-generated method stub
      String line = arg0.toString();
      if (Validate.isEmpty(line)) {
          return;
      }

      List<Text> mutualFriends = new ArrayList<Text>();
      while(arg1.hasNext()){
          mutualFriends.add(arg1.next());
      }

      if (mutualFriends.size() == 0) {
          return;
      }

      String[] userIds = line.split(Constants.COLUMN_DELIMITER);
      if (!Validate.isEqualsToLength(userIds, 2)) {
          return;
      }

      arg2.collect(new Text(userIds[0]), new Text(userIds[1] + Constants.COLUMN_DELIMITER + Constants.MUTUAL_FRIENDS_PREFIX + 
              + mutualFriends.size()));
    }
}
