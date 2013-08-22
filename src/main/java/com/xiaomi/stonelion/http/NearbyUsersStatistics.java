
package com.xiaomi.stonelion.http;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class NearbyUsersStatistics {
    private static String PRIVATE_KEY = "18c46abf0d32d25ff1838f4a2e57d727";
    private static String URL_PREFIX = "http://192.168.1.187/geocoder";
    private static String OUTPUT_JASON = "json";
    private static String RAW_DATA = "/home/shixin/workspace/stonelion/nearbyUsers.data";
    private static String RESULT = "/home/shixin/workspace/stonelion/result.data";

    private static Map<String, Long> map = new HashMap<String, Long>();

    public static void main(String[] args) throws IOException {
        File file1 = new File(RAW_DATA);
        BufferedReader br = new BufferedReader(new FileReader(file1));
        String line = null;
        while ((line = br.readLine()) != null) {
            String[] fields = line.split(";");
            if (3 != fields.length)
                continue;

            double latitude = 0;
            double longitude = 0;
            try {
                latitude = Double.parseDouble(fields[1]);
                longitude = Double.parseDouble(fields[2]);

                URL url = getURL(latitude, longitude, OUTPUT_JASON);
                String content = null;
                try {
                    content = getHttpResponseContent(url.openStream());
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }

                String location = getAddress(content);

                if (!StringUtils.isEmpty(location)) {
                    if (map.containsKey(location)) {
                        map.put(location, map.get(location) + 1);
                    } else {
                        map.put(location, new Long(1));
                    }
                    System.out.println("Get location " + location + " and number " + map.get(location));
                }
            } catch (NumberFormatException e) {
                continue;
            }
        }
        br.close();

        System.out.println("start write file");

        File outputFile = new File(RESULT);
        if (!outputFile.exists()) {
            outputFile.createNewFile();
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
        for (Entry<String, Long> entry : map.entrySet()) {
            if (entry.getValue() <= 10) {
                continue;
            }
            bw.write(entry.getKey() + " : " + entry.getValue() + "\n");
            System.out.println("write file : " + entry.getKey() + " : " + entry.getValue() + "\n");
        }
        bw.close();
    }

    public static String getAddress(String jsonStr) {
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.has("status") && "OK".equalsIgnoreCase(jsonObject.getString("status").trim()) && jsonObject.has("result")) {
                JSONObject result = new JSONObject(jsonObject.getString("result"));
                StringBuilder sb = new StringBuilder();
                if (result.has("formatted_address") && !StringUtils.isEmpty(result.getString("formatted_address"))) {
                    sb.append(result.get("formatted_address"));
                }
                if (result.has("business") && !StringUtils.isEmpty(result.getString("business"))) {
                    sb.append(result.get("business"));
                }
                return sb.toString();
            }
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getHttpResponseContent(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String content = null;
        while (null != (content = bufferedReader.readLine())) {
            stringBuilder.append(content);
        }
        bufferedReader.close();
        return stringBuilder.toString();
    }

    public static URL getURL(double latitude, double longitude, String output) throws MalformedURLException, IOException {
        StringBuilder stringBuilder = new StringBuilder(URL_PREFIX);
        stringBuilder.append("?");
        stringBuilder.append("output=" + output + "&");
        stringBuilder.append("location=" + latitude + "," + longitude + "&");
        stringBuilder.append("key=" + PRIVATE_KEY);
        URL url = new URL(stringBuilder.toString());
        return url;
    }
}
