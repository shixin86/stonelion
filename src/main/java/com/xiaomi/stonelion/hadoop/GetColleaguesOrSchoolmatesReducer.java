
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

public class GetColleaguesOrSchoolmatesReducer extends MapReduceBase implements Reducer<Text, Text, Text, Text> {

    public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> arg2, Reporter arg3) throws IOException {
        // TODO Auto-generated method stub
        List<String> userIds = new ArrayList<String>();

        while (values.hasNext()) {
            userIds.add(values.next().toString());
        }
        if (userIds.size() <= 1) {
            return;
        }

        String[] userIdsArray = userIds.toArray(new String[0]);
        for (int i = 0; i < userIdsArray.length - 1; i++) {
            for (int j = i + 1; j < userIdsArray.length; j++) {
                arg2.collect(new Text(userIdsArray[i] + Constants.COLUMN_DELIMITER + userIdsArray[j]), key);
                arg2.collect(new Text(userIdsArray[j] + Constants.COLUMN_DELIMITER + userIdsArray[i]), key);
            }
        }
    }

}
