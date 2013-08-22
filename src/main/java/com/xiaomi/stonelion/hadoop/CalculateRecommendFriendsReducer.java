
package com.xiaomi.stonelion.hadoop;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;



public class CalculateRecommendFriendsReducer  extends MapReduceBase implements Reducer<Text, Text, Text, Text> {
    public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> context, Reporter arg3) throws IOException {
        // TODO Auto-generated method stub
      CandidatesManager candidatesManager = new CandidatesManager();
      
      while(values.hasNext()){
          Text one = values.next();
          if (Validate.isEmpty(one.toString())) {
              return;
          }
          candidatesManager.addCandidate(one.toString());
      }

      candidatesManager.removeIllegalCandidates();

      StringBuilder sb = new StringBuilder();
      List<Candidate> formattedCandidates = candidatesManager.getFormattedCandidates();
      for (Candidate candidate : formattedCandidates) {
          sb.append(candidate.getFormattedDescription() + Constants.COLUMN_DELIMITER);
      }
      if (sb.length() > 0) {
          sb.deleteCharAt(sb.length() - 1);
      }

      context.collect(key, new Text(sb.toString()));
    }
    

    private static class CandidatesManager {
        private Map<Long, Candidate> candidatesMap = new HashMap<Long, Candidate>();

        public void addCandidate(String description) {
            long userId = -1;
            try {
                userId = Long.parseLong(description.substring(0, description.indexOf(Constants.COLUMN_DELIMITER)));
            } catch (NumberFormatException e) {
                return;
            }

            if (candidatesMap.containsKey(userId)) {
                Candidate candidate = candidatesMap.get(userId);
                candidate.addInformation(description);
            } else {
                Candidate candidate = Candidate.get(userId, description);
                if (null != candidate) {
                    candidatesMap.put(userId, candidate);
                }
            }
        }

        public void removeIllegalCandidates() {
            List<Long> illegalCandidateUserIds = new ArrayList<Long>();
            for (Entry<Long, Candidate> entry : candidatesMap.entrySet()) {
                Candidate candidate = entry.getValue();
                if (candidate.isRefuseToBeRecommended()) {
                    illegalCandidateUserIds.add(entry.getKey());
                    continue;
                }
                // only relation = 0/2/12
                if (candidate.getRelation() != 0 && candidate.getRelation() != 2
                        && candidate.getRelation() != 12) {
                    illegalCandidateUserIds.add(entry.getKey());
                    continue;
                }
                // have no mutual friends or school or corporation
                if (candidate.getRelation() == 0 && 0 == candidate.getMutualFriendsCount()
                        && candidate.getSchools().isEmpty() && candidate.getCorporations().isEmpty()) {
                    illegalCandidateUserIds.add(entry.getKey());
                    continue;
                }
            }
            for (Long illeaglCandidateUserId : illegalCandidateUserIds) {
                candidatesMap.remove(illeaglCandidateUserId);
            }
        }

        public List<Candidate> getFormattedCandidates() {
            List<Candidate> candidates = new ArrayList<Candidate>();
            for (Entry<Long, Candidate> entry : candidatesMap.entrySet()) {
                Candidate candidate = entry.getValue();
                StringBuilder sb = new StringBuilder();
                sb.append(candidate.getUserId() + Constants.ATTRIBUTE_DELIMITER);
                sb.append(Constants.RESULT_OLD_RELATOIN + Constants.KEY_VALUE_DELIMITER + candidate.getRelation());
                if (0 != candidate.getMutualFriendsCount()) {
                    sb.append(Constants.ATTRIBUTE_DELIMITER);
                    sb.append(Constants.RESULT_MUTUAL_FRINENDS + Constants.KEY_VALUE_DELIMITER + candidate.getMutualFriendsCount());
                }
                if (!candidate.getCorporations().isEmpty()) {
                    sb.append(Constants.ATTRIBUTE_DELIMITER + Constants.RESULT_MUTUAL_CORPORATION);
                    for (String corporation : candidate.getCorporations()) {
                        sb.append(Constants.KEY_VALUE_DELIMITER + corporation);
                    }
                }
                if (!candidate.getSchools().isEmpty()) {
                    sb.append(Constants.ATTRIBUTE_DELIMITER + Constants.RESULT_MUTUAL_SCHOOL);
                    for (String school : candidate.getSchools()) {
                        sb.append(Constants.KEY_VALUE_DELIMITER + school);
                    }
                }
                if (0 < candidate.getLastActivityTime()) {
                    sb.append(Constants.ATTRIBUTE_DELIMITER + Constants.RESULT_LAST_ACTIVITY_TIME);
                    sb.append(Constants.KEY_VALUE_DELIMITER + candidate.getLastActivityTime());
                }
                candidate.setFormattedDescription(sb.toString());
                candidates.add(candidate);
            }
            return candidates;
        }

    }

    private static class Candidate {
        private long userId;
        private int relation;
        private long lastActivityTime;
        private boolean refuseToBeRecommended;
        private int mutualFriendsCount;
        private List<String> corporations = new ArrayList<String>();
        private List<String> schools = new ArrayList<String>();
        private String formattedDescription;

        private Candidate() {
        }

        public static Candidate get(long userId, String description) {
            Candidate candidate = new Candidate();
            candidate.userId = userId;
            parseToCandidate(candidate, description);
            return candidate;
        }

        public void addInformation(String description) {
            parseToCandidate(this, description);
        }

        private static void parseToCandidate(Candidate candidate, String description) {
            String[] values = description.split(Constants.COLUMN_DELIMITER);

            if (values.length < 2) {
                return;
            }

            for (int i = 1; i < values.length; i++) {
                if (Validate.isEmpty(values[i])) {
                    continue;
                }
                if (values[i].startsWith(Constants.MUTUAL_FRIENDS_PREFIX)) {
                    String mutualFriendsCountStr = values[i].substring(Constants.MUTUAL_FRIENDS_PREFIX.length());
                    try {
                        candidate.mutualFriendsCount = Integer.parseInt(mutualFriendsCountStr);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                } else if (values[i].startsWith(Constants.MUTUAL_CORPORATION_PREFIX)) {
                    String corporation = values[i].substring(Constants.MUTUAL_CORPORATION_PREFIX.length());
                    if (Validate.isEmpty(corporation)) {
                        continue;
                    }
                    candidate.corporations.add(corporation);
                } else if (values[i].startsWith(Constants.MUTUAL_SCHOOL_PREFIX)) {
                    String school = values[i].substring(Constants.MUTUAL_SCHOOL_PREFIX.length());
                    if (Validate.isEmpty(school)) {
                        continue;
                    }
                    candidate.schools.add(school);
                } else if (values[i].startsWith(Constants.LASTACTIVITYTIME_PREFIX)) {
                    String lastActivityTime = values[i].substring(Constants.LASTACTIVITYTIME_PREFIX.length());
                    try {
                        candidate.lastActivityTime = Long.parseLong(lastActivityTime);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                } else if (values[i].startsWith(Constants.REFUSE_PREFIX)) {
                    candidate.refuseToBeRecommended = true;
                } else {
                    try {
                        candidate.relation = Integer.parseInt(values[i]);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }
            }
        }

        public long getUserId() {
            return userId;
        }

        public int getRelation() {
            return relation;
        }

        public long getLastActivityTime() {
            return lastActivityTime;
        }

        public boolean isRefuseToBeRecommended() {
            return refuseToBeRecommended;
        }

        public int getMutualFriendsCount() {
            return mutualFriendsCount;
        }

        public List<String> getCorporations() {
            return corporations;
        }

        public List<String> getSchools() {
            return schools;
        }

        public String getFormattedDescription() {
            return formattedDescription;
        }

        public void setFormattedDescription(String formattedDescription) {
            this.formattedDescription = formattedDescription;
        }
    }
}
