
package com.xiaomi.stonelion.hadoop;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetMutualFriendsReducer extends Reducer<Text, Text, Text, Text> {

    @Override
    protected void reduce(Text key, java.lang.Iterable<Text> values, Reducer<Text, Text, Text, Text>.Context context) throws IOException,
                                                                                                                     InterruptedException {
        List<String> friendsUserIds = new ArrayList<String>();
        for (Text one : values) {
            friendsUserIds.add(one.toString());
        }
        if (friendsUserIds.size() <= 1) {
            return;
        }

        String[] userIdsArray = friendsUserIds.toArray(new String[0]);
        for (int i = 0; i < userIdsArray.length - 1; i++) {
            for (int j = i + 1; j < userIdsArray.length; j++) {
                context.write(new Text(userIdsArray[i] + Constants.COLUMN_DELIMITER + userIdsArray[j]), new Text("1"));
                context.write(new Text(userIdsArray[j] + Constants.COLUMN_DELIMITER + userIdsArray[i]), new Text("1"));
            }
        }
    };
}
