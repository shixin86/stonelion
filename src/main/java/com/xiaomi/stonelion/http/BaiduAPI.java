
package com.xiaomi.stonelion.http;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class BaiduAPI {
    private static String PRIVATE_KEY = "18c46abf0d32d25ff1838f4a2e57d727";
    private static String URL_PREFIX = "http://api.map.baidu.com/geocoder";
    private static String OUTPUT_JASON = "json";
    private static String OUTPUT_XML = "xml";

    public static void main(String[] args) throws MalformedURLException, IOException {
        double latitude = 39.94263;
        double longitude = 116.459172;
        String address = "北京市房山区";

        URL url = getURL(address, OUTPUT_JASON);
        String content = getHttpResponseContent(url.openStream());
        System.out.println(content);
        System.out.println(getAddress(content));

    }

    /**
     * { "status":"OK","result":{"location":{"lng":116.459172,"lat":39.94263},"formatted_address":"北京市朝阳区","business":
     * "三里屯"
     * ,"addressComponent":{"city":"北京市","district":"朝阳区","province":"北京市","street":"","street_number":""},"cityCode"
     * :131}}
     * 
     * @param jsonStr
     * @return
     */
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

        System.out.println(String.format("getProtocol %s", url.getProtocol()));
        System.out.println(String.format("getHost %s", url.getHost()));
        System.out.println(String.format("getPath %s", url.getPath()));
        System.out.println(String.format("getPort %s", url.getPort()));
        System.out.println(String.format("getDefaultPort %s", url.getDefaultPort()));
        System.out.println(String.format("getQuery %s", url.getQuery()));
        System.out.println(String.format("getAuthority %s", url.getAuthority()));
        System.out.println(String.format("getRef %s", url.getRef()));
        System.out.println(String.format("getUserInfo %s", url.getUserInfo()));
        System.out.println(String.format("getFile %s", url.getFile()));
        System.out.println(String.format("getContent %s", url.getContent()));
        System.out.println(String.format("toExternalForm %s", url.toExternalForm()));
        System.out.println("---------------");

        return url;
    }

    public static URL getURL(String address, String output) throws MalformedURLException, IOException {
        StringBuilder stringBuilder = new StringBuilder(URL_PREFIX);
        stringBuilder.append("?");
        stringBuilder.append("output=" + output + "&");
        stringBuilder.append("address=" + address + "&");
        stringBuilder.append("key=" + PRIVATE_KEY);

        URL url = new URL(stringBuilder.toString());

        System.out.println(String.format("getProtocol %s", url.getProtocol()));
        System.out.println(String.format("getHost %s", url.getHost()));
        System.out.println(String.format("getPath %s", url.getPath()));
        System.out.println(String.format("getPort %s", url.getPort()));
        System.out.println(String.format("getDefaultPort %s", url.getDefaultPort()));
        System.out.println(String.format("getQuery %s", url.getQuery()));
        System.out.println(String.format("getAuthority %s", url.getAuthority()));
        System.out.println(String.format("getRef %s", url.getRef()));
        System.out.println(String.format("getUserInfo %s", url.getUserInfo()));
        System.out.println(String.format("getFile %s", url.getFile()));
        System.out.println(String.format("getContent %s", url.getContent()));
        System.out.println(String.format("toExternalForm %s", url.toExternalForm()));
        System.out.println("---------------");

        return url;
    }
}
