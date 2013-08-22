
package com.xiaomi.stonelion.hadoop;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetColleaguesOrSchoolmatesMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {

    private static final String CORPORATIONJSONSTRINGPREFIX = "[{\"name\":";

    public void map(LongWritable key, Text value, OutputCollector<Text, Text> context, Reporter arg3) throws IOException {
        String line = value.toString();
        if (Validate.isEmpty(line)) {
            return;
        }

        String[] values = line.split(Constants.COLUMN_DELIMITER);
        if (null == values || values.length < 2 || Validate.isEmpty(values[0])) {
            return;
        }

        CorporationManager corporationManager = null;
        SchoolManager schoolManager = null;

        if (3 == values.length) {
            corporationManager = CorporationManager.get(values[1]);
            schoolManager = SchoolManager.get(values[2]);
        } else if (2 == values.length && !Validate.isEmpty(values[1])) {
            boolean isCorporationJSONString = isCorporationJSONString(values[1]);
            if (isCorporationJSONString) {
                corporationManager = CorporationManager.get(values[1]);
            } else {
                schoolManager = SchoolManager.get(values[1]);
            }
        } else {
            return;
        }

        if (null != corporationManager) {
            for (String temp : corporationManager.getCorporationNames()) {
                context.collect(new Text(Constants.MUTUAL_CORPORATION_PREFIX + temp), new Text(values[0]));
            }
        }
        if (null != schoolManager) {
            for (String temp : schoolManager.getSchoolNames()) {
                context.collect(new Text(Constants.MUTUAL_SCHOOL_PREFIX + temp), new Text(values[0]));
            }
        }
    }

    private boolean isCorporationJSONString(String jsonString) {
        return jsonString.startsWith(CORPORATIONJSONSTRINGPREFIX);
    }

    private static class SchoolManager {
        private static final String KEY1 = "list";
        private static final String KEY2 = "name";
        private List<String> schoolNames = new ArrayList<String>();

        private SchoolManager() {
        }

        public static SchoolManager get(String jsonString) {
            try {
                SchoolManager manager = new SchoolManager();
                JSONArray jsonArray = new JSONArray(jsonString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.has(KEY1)) {
                        JSONArray jsonArray2 = jsonObject.getJSONArray(KEY1);
                        for (int j = 0; j < jsonArray2.length(); j++) {
                            JSONObject jsonObject2 = jsonArray2.getJSONObject(j);
                            if (jsonObject2.has(KEY2)) {
                                manager.schoolNames.add(jsonObject2.getString(KEY2));
                            }
                        }
                    }
                }
                return manager;
            } catch (JSONException e) {
                return null;
            }
        }

        public List<String> getSchoolNames() {
            return this.schoolNames;
        }
    }

    private static class CorporationManager {
        private static final String KEY = "name";
        private List<String> corporationNames = new ArrayList<String>();

        private CorporationManager() {
        }

        public static CorporationManager get(String jsonString) {
            try {
                CorporationManager manager = new CorporationManager();
                JSONArray jsonArray = new JSONArray(jsonString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.has(KEY)) {
                        manager.corporationNames.add(jsonObject.getString(KEY));
                    }
                }
                return manager;
            } catch (JSONException e) {
                return null;
            }
        }

        public List<String> getCorporationNames() {
            return this.corporationNames;
        }
    }
}
