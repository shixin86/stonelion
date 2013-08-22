
package com.xiaomi.stonelion.senseidb;

import com.senseidb.search.client.SenseiServiceProxy;
import com.senseidb.search.client.json.JsonSerializer;
import com.senseidb.search.client.req.Selection;
import com.senseidb.search.client.req.SenseiClientRequest;
import com.senseidb.search.client.res.SenseiResult;

import org.json.JSONException;
import org.json.JSONObject;

public class TestGenerateBrowseRequest {
    public static void main(String[] args) throws JSONException {
        SenseiClientRequest senseiClientRequest = buildSenseiClientRequest();

        printJSONSenseiClientRequest(senseiClientRequest);
        printRequestProgressInSensei(senseiClientRequest);
        /**
         * SenseiServiceProxy senseiServiceProxy = new SenseiServiceProxy("localhost", 8080); SenseiResult senseiResult
         * = senseiServiceProxy.sendSearchRequest(senseiClientRequest); printSenseiResult(senseiResult);
         */
    }

    private static SenseiClientRequest buildSenseiClientRequest() throws JSONException {
        SenseiClientRequest.Builder builder = SenseiClientRequest.builder();
        builder.explain(false);
        builder.fetchStored(true);
        builder.paging(10, 0);
        
        // selection
        builder.addSelection(Selection.custom(new JSONObject().put("arg1", "value1")));
        builder.addSelection(Selection.path("city", "/a/b", true, 1));
        builder.addSelection(Selection.range("rangeFieldName", "v1", "v2", true, false));
        builder.addSelection(Selection.terms("termsFieldName", "1", "2", "3"));
        
        return builder.build();
    }

    private static void printJSONSenseiClientRequest(SenseiClientRequest request) {
        String requestStr = JsonSerializer.serialize(request).toString();
        System.out.println("SenseiClientRequest toString : ");
        System.out.println(requestStr);
    }

    private static void printRequestProgressInSensei(SenseiClientRequest request) {
    }

    private static void printSenseiResult(SenseiResult senseiResult) {

    }
}
