
package com.xiaomi.stonelion.senseidb;

import com.browseengine.bobo.api.BoboBrowser;
import com.browseengine.bobo.api.BoboIndexReader;
import com.browseengine.bobo.api.Browsable;
import com.browseengine.bobo.api.BrowseException;
import com.browseengine.bobo.api.BrowseHit;
import com.browseengine.bobo.api.BrowseRequest;
import com.browseengine.bobo.api.BrowseResult;
import com.browseengine.bobo.facets.FacetHandler;
import com.browseengine.bobo.facets.impl.SimpleFacetHandler;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SearchUsersViaBobo {
    private static Browsable browsable;
    private static String INDEX_DIR = "/home/shixin/soft/search-miliao-users/index/shard0";

    private static void setup() throws IOException {
        Directory d = FSDirectory.open(new File(INDEX_DIR));
        IndexReader indexRearder = IndexReader.open(d);

        List<FacetHandler<?>> facetHandlers = new ArrayList<FacetHandler<?>>();
        facetHandlers.add(new SimpleFacetHandler("idstr"));
        facetHandlers.add(new SimpleFacetHandler("activeness"));
        facetHandlers.add(new SimpleFacetHandler("searchable"));
        facetHandlers.add(new SimpleFacetHandler("online"));

        BoboIndexReader boboR = BoboIndexReader.getInstance(indexRearder, facetHandlers);
        browsable = new BoboBrowser(boboR);
    }

    private static String KEY_IDSTR = "idstr";
    private static String KEY_ACTIVENESS = "activeness";
    private static String KEY_SEARCHABLE = "searchable";
    private static String KEY_ONLINE = "online";
    private static List<String> KEYS = new ArrayList<String>();
    static {
        KEYS.add(KEY_IDSTR);
        KEYS.add(KEY_ACTIVENESS);
        KEYS.add(KEY_SEARCHABLE);
        KEYS.add(KEY_ONLINE);
    }

    private static void printFieldValues(BrowseResult result) {
        if (null != result) {
            f(String.format("Totol docs : %d, hits : %d", result.getTotalDocs(), result.getNumHits()));
            f(new String("-------------------------------------------------------------------------"));

            int i = 1;
            BrowseHit[] hits = result.getHits();
            for (BrowseHit hit : hits) {
                // 打印FieldValues
                Map<String, String[]> fieldValueMap = hit.getFieldValues();
                for (Entry<String, String[]> entry : fieldValueMap.entrySet()) {
                    f(String.format("Hit %d, field name : %s, field values : %s", i, entry.getKey(), Arrays.asList(entry.getValue())));
                }

                // 打印StoredFields
                Document storedFields = hit.getStoredFields();
                if (null != storedFields) {
                    for (String key : KEYS) {
                        f(String.format("Hit %d, storedField name : %s, storedField values : %s", i, key, storedFields.get(key)));
                    }
                }

                // 打印rawFieldValues
                Map<String, Object[]> rawFieldValues = hit.getRawFieldValues();
                if (null != rawFieldValues) {
                    for (Entry<String, Object[]> entry : rawFieldValues.entrySet()) {
                        f(String.format("Hit %d, rawField name : %s, rawField values : %s", i, entry.getKey(),
                            Arrays.asList(entry.getValue())));
                    }
                }

                System.out.println(hit.getExplanation());
                i++;
                f(new String("-------------------------------------------------------------------------"));
            }
        }
    }

    private static void f(String stringFormat) {
        System.out.println(stringFormat);
    }

    private static Query getQuery(String queryStr) throws ParseException {
        QueryParser queryParser = new QueryParser(Version.LUCENE_35, "", new IKAnalyzer());
        String format = "((idstr:%s nickname:\"%s\"~99 school:\"%s\"~99 corporation:\"%s\"~99 city:\"%s\"~99) AND searchable:0) OR (idstr:%s AND searchable:1)";
        Query query = queryParser.parse(String.format(format, queryStr, queryStr, queryStr, queryStr, queryStr, queryStr));
        return query;
    }

    public static void main(String[] args) throws BrowseException, IOException, ParseException {
        setup();

        BrowseRequest request = new BrowseRequest();
        request.setOffset(0);
        request.setCount(10);
        request.setFetchStoredFields(false);
        request.setShowExplanation(true);

        request.setQuery(getQuery("北京理工大学"));

        BrowseResult browseResult = browsable.browse(request);
        printFieldValues(browseResult);
    }
}
