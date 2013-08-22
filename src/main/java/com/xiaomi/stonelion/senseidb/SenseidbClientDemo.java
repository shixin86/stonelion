
package com.xiaomi.stonelion.senseidb;

import com.senseidb.search.client.SenseiServiceProxy;
import com.senseidb.search.client.json.JsonSerializer;
import com.senseidb.search.client.req.Selection;
import com.senseidb.search.client.req.SenseiClientRequest;
import com.senseidb.search.client.req.Sort;
import com.senseidb.search.client.req.filter.Filters;
import com.senseidb.search.client.req.query.Queries;
import com.senseidb.search.client.res.FacetResult;
import com.senseidb.search.client.res.FieldValue;
import com.senseidb.search.client.res.SenseiHit;
import com.senseidb.search.client.res.SenseiResult;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SenseidbClientDemo {
    private static final String SENSEI_SERVER_HOST = "10.101.10.40";
    private static final int SENSEI_SERVER_PORT = 8080;

    public static void main(String[] args) throws Exception {
        SenseiServiceProxy proxy = new SenseiServiceProxy(SENSEI_SERVER_HOST, SENSEI_SERVER_PORT);
        SenseiClientRequest senseiClientRequest = getSenseiClientRequest();
        testSendSearchRequest(proxy, senseiClientRequest);
    }

    private static SenseiClientRequest getSenseiClientRequest() {
        return SenseiClientRequest.builder().paging(10, 0).query(Queries.stringQuery(getMultiLuceneQueryLanguage("45852297")))
                .fetchStored(true).explain(true).addSort(Sort.desc("activeness")).addSort(Sort.byRelevance()).build();
    }

    private static String getMultiLuceneQueryLanguage(String query) {
        String[] queryFields = query.split("\\s+");
        if (null != queryFields && queryFields.length > 1) {
            StringBuilder sb = new StringBuilder();
            for (String queryField : queryFields) {
                sb.append("(" + getLuceneQueryLanguage(queryField.trim()) + ") AND ");
            }
            return StringUtils.removeEnd(sb.toString(), " AND ");
        }
        return getLuceneQueryLanguage(query);
    }

    private static String getLuceneQueryLanguage(String query) {
        String format = "((idstr:%s nickname:\"%s\"~99 school:\"%s\"~99 corporation:\"%s\"~99 city:\"%s\"~99) AND searchable:0) OR (idstr:%s AND searchable:1)";
        return String.format(format, query, query, query, query, query, query);
    }

    /**
     * 通过SenseiClientRequest查询
     * 
     * @param proxy
     * @param request
     * @throws JSONException
     */
    private static void testSendSearchRequest(SenseiServiceProxy proxy, SenseiClientRequest request) throws JSONException {
        SenseiResult result = proxy.sendSearchRequest(request);

        System.out.println(String.format("parsed query : %s", result.getParsedQuery()));
        System.out.println(String.format("number hists : %d", result.getNumhits()));
        System.out.println(String.format("total docs : %d", result.getTotaldocs()));
        System.out.println(String.format("time : %d", result.getTime()));

        List<SenseiHit> hits = result.getHits();
        if (null != hits) {
            System.out.println("1******************************");
            for (SenseiHit hit : hits) {
                Map<String, List<String>> map = hit.getFieldValues();
                for (Entry<String, List<String>> entry : map.entrySet()) {
                    System.out.print("field name is: " + entry.getKey() + ", and field values is:");
                    for (String fieldValue : entry.getValue()) {
                        System.out.print(fieldValue + ", ");
                    }
                    System.out.println();
                }
            }
        }

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

        Map<String, List<FacetResult>> facets = result.getFacets();
        if (null != facets) {
            System.out.println("4******************************");
            for (Entry<String, List<FacetResult>> entry : facets.entrySet()) {
                for (FacetResult facetResult : entry.getValue()) {
                    System.out.println(String.format("%s : %s", entry.getKey(), facetResult.getValue()));
                }
            }
        }

        if (null != hits) {
            for (SenseiHit hit : hits) {
                System.out.println(hit.getExplanation());
            }
        }
    }

    /**
     * 通过document uid 取得整个document
     * 
     * @param proxy
     * @param uids
     * @throws IOException
     * @throws JSONException
     */
    @SuppressWarnings("unused")
    private static void testSendGetRequest(SenseiServiceProxy proxy, long... uids) throws IOException, JSONException {
        Map<Long, JSONObject> result = proxy.sendGetRequest(uids);
        if (null != result) {
            for (Entry<Long, JSONObject> entry : result.entrySet()) {
                System.out.println(result.get(entry.getKey()).toString());
            }
        }
    }

    /**
     * 通过BQL查询
     * 
     * @param proxy
     * @param bql
     */
    @SuppressWarnings("unused")
    private static void testSendBQL(SenseiServiceProxy proxy, String bql) {
        SenseiResult result = proxy.sendBQL(bql);

        System.out.println(String.format("parsed query : %s", result.getParsedQuery()));
        System.out.println(String.format("number hists : %d", result.getNumhits()));
        System.out.println(String.format("total docs : %d", result.getTotaldocs()));
        System.out.println(String.format("time : %d", result.getTime()));

        List<SenseiHit> hits = result.getHits();
        if (null != hits) {
            for (SenseiHit hit : hits) {
                System.out.println(String.format("uid : %d", hit.getUid()));
            }
        }

        Map<String, List<FacetResult>> facets = result.getFacets();
        if (null != facets) {
            for (Entry<String, List<FacetResult>> entry : facets.entrySet()) {
                for (FacetResult facetResult : entry.getValue()) {
                    System.out.println(String.format("%s : %s", entry.getKey(), facetResult.getValue()));
                }
            }
        }
    }

}
