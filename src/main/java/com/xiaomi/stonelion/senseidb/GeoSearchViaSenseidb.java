
package com.xiaomi.stonelion.senseidb;

import com.senseidb.search.client.SenseiServiceProxy;
import com.senseidb.search.client.req.Selection;
import com.senseidb.search.client.req.SenseiClientRequest;
import com.senseidb.search.client.req.filter.Filters;
import com.senseidb.search.client.req.query.CustomQuery;
import com.senseidb.search.client.res.FieldValue;
import com.senseidb.search.client.res.SenseiHit;
import com.senseidb.search.client.res.SenseiResult;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 在本地连接sensei集群测试地理位置搜索<br>
 * 
 * @author shixin
 * @date Jul 8, 20133:26:21 PM
 * @Description TODO
 */
public class GeoSearchViaSenseidb {
    private static final String SENSEI_SERVER_HOST = "10.101.10.40";
    private static final int SENSEI_SERVER_PORT = 8080;

    public static void main(String[] args) {
        SenseiServiceProxy proxy = new SenseiServiceProxy(SENSEI_SERVER_HOST, SENSEI_SERVER_PORT);

        // 构建请求
        int offset = 0;
        int count = 300;
        // double lat = 39.959661;
        // double lng = 116.31614;
        double lat = 40.0294;
        double lng = 116.329975;

        double distance = 100;
        String sex = "F";
        long timespan = 7 * 24 * 60 * 60 * 1000l;
        SenseiClientRequest request = buildRequest(offset, count, lat, lng, distance, sex, timespan);

        // 查詢
        SenseiResult result = proxy.sendSearchRequest(request);

        // 打印结果
        System.out.println(String.format("parsed query : %s", result.getParsedQuery()));
        System.out.println(String.format("number hists : %d", result.getNumhits()));
        System.out.println(String.format("total docs : %d", result.getTotaldocs()));
        System.out.println(String.format("time : %d", result.getTime()));

        List<SenseiHit> hits = result.getHits();
//        if (null != hits) {
//            System.out.println("1******************************");
//            for (SenseiHit hit : hits) {
//                Map<String, List<String>> map = hit.getFieldValues();
//                for (Entry<String, List<String>> entry : map.entrySet()) {
//                    System.out.print("field name is: " + entry.getKey() + ", and field values is:");
//                    for (String fieldValue : entry.getValue()) {
//                        System.out.print(fieldValue + ", ");
//                    }
//                    System.out.println();
//                }
//            }
//        }

        if (null != hits) {
            System.out.println("2******************************");
            for (SenseiHit hit : hits) {
                List<FieldValue> fieldValues = hit.getStoredFields();
                for (FieldValue fieldValue : fieldValues) {
                    System.out.print(fieldValue.getFieldName() + ":" + fieldValue.getFieldValues());
                }
            }
        }

        if (null != hits) {
            System.out.println("3******************************");
            for (SenseiHit hit : hits) {
                System.out.println(hit.getScore() + " * " + hit.getSrcdata());
            }
        }
    }

    public static SenseiClientRequest buildRequest(int offset, int count, double lat, double lng, double distance, String sex, long timespan) {
        SenseiClientRequest.Builder builder = new SenseiClientRequest.Builder();

        builder.paging(count, offset);
        builder.fetchStored(true);

        // query
        builder.query(buildSenseiClientCustomQuery(lat, lng, distance));

        // selection
        Selection sexSelection = buildSenseiClientSexSelection(sex);
        if (null != sexSelection) {
            builder.addSelection(sexSelection);
        }
        builder.addSelection(buildSenseiClientTimespanSelection(timespan));
        builder.addSelection(buildSenseiClientLBSVisibilitySelection());

        List<String> excludes = new ArrayList<String>();
        excludes.add("1675181");
        excludes.add("1675159");
        excludes.add("1675938");

        builder.filter(Filters.ids(null, excludes));

        return builder.build();
    }

    /**
     * Construct sensei client CustomQuery
     * 
     * @param request
     * @return
     * @throws SpatialSearchException
     */
    private static CustomQuery buildSenseiClientCustomQuery(double lat, double lng, double distance) {
        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put("geo_facet", "correctDistance");
        initParams.put("latitude_facet", "latitude");
        initParams.put("longitude_facet", "longitude");
        initParams.put("latitude", String.valueOf(lat));
        initParams.put("longitude", String.valueOf(lng));
        initParams.put("distance", String.valueOf(distance));
        initParams.put("directly", Boolean.TRUE.toString());
        String className = "com.xiaomi.sensei.plugin.query.geo.GeoQuery";
        return new CustomQuery(className, initParams, 1.0);
    }

    /**
     * sex selection
     * 
     * @param request
     * @return
     */
    private static Selection buildSenseiClientSexSelection(String sex) {
        if (StringUtils.isNotBlank(sex)) {
            sex = sex.trim().toUpperCase();
            return Selection.terms("sex", sex);
        }
        return null;
    }

    /**
     * timespan selection
     * 
     * @param request
     * @return
     */
    private static Selection buildSenseiClientTimespanSelection(long timespan) {
        String from = String.valueOf(System.currentTimeMillis() - timespan);
        String to = String.valueOf(System.currentTimeMillis());
        return Selection.range("lbsRegTime", from, to, true, true);
    }

    /**
     * only search the visible user
     * 
     * @return
     */
    private static Selection buildSenseiClientLBSVisibilitySelection() {
        return Selection.terms("lbsVisibility", "1");
    }
}
