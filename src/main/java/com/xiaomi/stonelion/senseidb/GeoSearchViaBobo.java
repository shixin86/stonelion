
package com.xiaomi.stonelion.senseidb;

import com.browseengine.bobo.api.BoboBrowser;
import com.browseengine.bobo.api.BoboIndexReader;
import com.browseengine.bobo.api.Browsable;
import com.browseengine.bobo.api.BrowseException;
import com.browseengine.bobo.api.BrowseHit;
import com.browseengine.bobo.api.BrowseRequest;
import com.browseengine.bobo.api.BrowseResult;
import com.browseengine.bobo.api.BrowseSelection;
import com.browseengine.bobo.api.FacetAccessible;
import com.browseengine.bobo.api.FacetSpec;
import com.browseengine.bobo.api.FacetSpec.FacetSortSpec;
import com.browseengine.bobo.facets.FacetHandler;
import com.browseengine.bobo.facets.impl.GeoFacetHandler;
import com.browseengine.bobo.facets.impl.RangeFacetHandler;
import com.browseengine.bobo.facets.impl.SimpleFacetHandler;
import com.browseengine.bobo.query.scoring.FacetTermQuery;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;

import scala.actors.threadpool.Arrays;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class GeoSearchViaBobo {
    public static void main(String[] args) throws CorruptIndexException, LockObtainFailedException, IOException, BrowseException {
        setup();

        BrowseRequest browseRequest = new BrowseRequest();
        browseRequest.setCount(10);
        browseRequest.setOffset(0);

        BrowseSelection browseSelection = new BrowseSelection("correctDistance");
        browseSelection.addValue("39.959661,116.31614:10");
        browseRequest.addSelection(browseSelection);

        FacetSpec geoSpec = new FacetSpec();
        geoSpec.setMinHitCount(0);
        geoSpec.setOrderBy(FacetSortSpec.OrderValueAsc);
        browseRequest.setFacetSpec("correctDistance", geoSpec);

        BrowseResult result = browser.browse(browseRequest);
        printFacet(result);

        // 2
        BrowseRequest browseRequest2 = new BrowseRequest();
        browseRequest2.setCount(10);
        browseRequest2.setOffset(0);

        BrowseSelection browseSelection2 = new BrowseSelection("correctDistance");
        browseSelection2.addValue("39.959661,116.31614:0.3");
        browseRequest2.addSelection(browseSelection2);

        Map<String, Float> map = new HashMap<String, Float>();
        map.put("39.960395,116.319820:1", 3.0f);
        FacetTermQuery geoQ = new FacetTermQuery(browseSelection2, map);
        browseRequest2.setQuery(geoQ);

        result = browser.browse(browseRequest2);
        printFacet(result);
        printFieldValues(result);
    }

    private static void printFacet(BrowseResult result) {
        if (null != result) {
            Map<String, FacetAccessible> facets = result.getFacetMap();
            if (null != facets) {
                f(String.format("Facet num : %d", facets.size()));
                for (Entry<String, FacetAccessible> entry : facets.entrySet()) {
                    f(String.format("Facet name : %s, facet values : %s", entry.getKey(), entry.getValue().getFacets()));
                }
            }
        }
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

                // 打印rawFieldValues
                Map<String, Object[]> rawFieldValues = hit.getRawFieldValues();
                if (null != rawFieldValues) {
                    for (Entry<String, Object[]> entry : rawFieldValues.entrySet()) {
                        f(String.format("Hit %d, rawField name : %s, rawField values : %s", i, entry.getKey(),
                            Arrays.asList(entry.getValue())));
                    }
                }
                i++;
            }
        }
    }

    private static void f(String stringFormat) {
        System.out.println(stringFormat);
    }

    private static Browsable browser;
    private static String KEY_ID = "uid";
    private static String KEY_LAT = "latitude";
    private static String KEY_LON = "longitude";

    private static void setup() throws CorruptIndexException, LockObtainFailedException, IOException {
        Directory d = new RAMDirectory();
        IndexWriter indexWriter = new IndexWriter(d, new SimpleAnalyzer(), MaxFieldLength.UNLIMITED);

        // documents
        Map<String, String> fieldMap = new HashMap<String, String>();
        fieldMap.put(KEY_ID, "1");
        fieldMap.put(KEY_LAT, "39.959661");
        fieldMap.put(KEY_LON, "116.31614");
        buildMetaField(indexWriter, fieldMap);

        fieldMap.clear();
        fieldMap.put(KEY_ID, "2");
        fieldMap.put(KEY_LAT, "39.9604572");
        fieldMap.put(KEY_LON, "116.3185113");
        buildMetaField(indexWriter, fieldMap);

        fieldMap.clear();
        fieldMap.put(KEY_ID, "3");
        fieldMap.put(KEY_LAT, "39.960396");
        fieldMap.put(KEY_LON, "116.319819");
        buildMetaField(indexWriter, fieldMap);

        fieldMap.clear();
        fieldMap.put(KEY_ID, "4");
        fieldMap.put(KEY_LAT, "39.960395");
        fieldMap.put(KEY_LON, "116.319820");
        buildMetaField(indexWriter, fieldMap);

        fieldMap.clear();
        fieldMap.put(KEY_ID, "5");
        fieldMap.put(KEY_LAT, "39.982703");
        fieldMap.put(KEY_LON, "116.464011");
        buildMetaField(indexWriter, fieldMap);

        indexWriter.close();
        IndexReader indexReader = IndexReader.open(d, true); // readonly
        List<FacetHandler<?>> facetHandlers = buildFacetHandlers();
        BoboIndexReader boboIndexReader = BoboIndexReader.getInstance(indexReader, facetHandlers);
        browser = new BoboBrowser(boboIndexReader);
    }

    private static List<FacetHandler<?>> buildFacetHandlers() {
        List<FacetHandler<?>> facetHandlers = new ArrayList<FacetHandler<?>>();
        facetHandlers.add(new SimpleFacetHandler(KEY_ID));
        facetHandlers.add(new RangeFacetHandler(KEY_LAT, Arrays.asList(new String[] {
            "[* TO 1]", "[2 TO *]"
        })));
        facetHandlers.add(new RangeFacetHandler(KEY_LON, Arrays.asList(new String[] {
            "[* TO 1]", "[2 TO *]"
        })));
        facetHandlers.add(new GeoFacetHandler("correctDistance", KEY_LAT, KEY_LON));
        return facetHandlers;
    }

    private static void buildMetaField(IndexWriter indexWriter, Map<String, String> fieldMap) throws CorruptIndexException, IOException {
        assert indexWriter != null && fieldMap != null;
        Document document = new Document();
        for (Entry<String, String> entry : fieldMap.entrySet()) {
            Field field = new Field(entry.getKey(), entry.getValue(), Store.NO, Index.NOT_ANALYZED_NO_NORMS);
            field.setOmitTermFreqAndPositions(true);
            document.add(field);
        }
        indexWriter.addDocument(document);
    }

}
