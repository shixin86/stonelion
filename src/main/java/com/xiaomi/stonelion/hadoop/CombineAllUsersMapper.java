
package com.xiaomi.stonelion.hadoop;


import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;
import java.util.regex.Pattern;

public class CombineAllUsersMapper  extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {
    public void map(LongWritable arg0, Text value, OutputCollector<Text, Text> context, Reporter arg3) throws IOException {
      String line = value.toString();
      if (Validate.isEmpty(line)) {
          return;
      }

      FileType fileType = FileType.getFileType(line);
      if (null == fileType) {
          return;
      }

      switch (fileType) {
      case MutualFriends:
      case ColleaguesOrSchoolmates:
          String[] values = line.split("\t");
          String userId2 = values[1].substring(0, values[1].indexOf(Constants.COLUMN_DELIMITER));
          String userId2End = values[1].substring(values[1].indexOf(Constants.COLUMN_DELIMITER));
          context.collect(new Text(userId2), new Text(values[0] + userId2End));
          break;
      case Relations:
          values = line.split(Constants.COLUMN_DELIMITER);
          context.collect(new Text(values[1]), new Text(values[0] + Constants.COLUMN_DELIMITER + values[2]));
          break;
      case RefuseToBeRecommendedUsers:
          context.collect(value, new Text(Constants.REFUSE_PREFIX));
          break;
      case LastActivityTimes:
          values = line.split(Constants.COLUMN_DELIMITER);
          context.collect(new Text(values[0]), new Text(Constants.LASTACTIVITYTIME_PREFIX + values[1]));
          break;
      default:
          return;
      }
    }
    
    private static enum FileType {
        MutualFriends("^\\d+\\s+\\d+;mf,\\d+$"), 
        ColleaguesOrSchoolmates("^\\d+\\s+\\d+;m[cs],"), 
        Relations("^\\d+;\\d+;\\d+$"), 
        RefuseToBeRecommendedUsers("^\\d+$"), 
        LastActivityTimes("^\\d+;\\d+$");

        private String patternFormat;

        private FileType(String patternFormat) {
            this.patternFormat = patternFormat;
        }

        public String getPatternFormat() {
            return patternFormat;
        }

        public static FileType getFileType(String line) {
            Pattern pattern = Pattern.compile(Relations.getPatternFormat());
            if (pattern.matcher(line).matches()) {
                return Relations;
            }
            pattern = Pattern.compile(RefuseToBeRecommendedUsers.getPatternFormat());
            if (pattern.matcher(line).matches()) {
                return RefuseToBeRecommendedUsers;
            }
            pattern = Pattern.compile(LastActivityTimes.getPatternFormat());
            if (pattern.matcher(line).matches()) {
                return LastActivityTimes;
            }
            pattern = Pattern.compile(MutualFriends.getPatternFormat());
            if (pattern.matcher(line).matches()) {
                return MutualFriends;
            }
            pattern = Pattern.compile(ColleaguesOrSchoolmates.getPatternFormat());
            if (pattern.matcher(line).find()) {
                return ColleaguesOrSchoolmates;
            }
            return null;
        }
    }
}
