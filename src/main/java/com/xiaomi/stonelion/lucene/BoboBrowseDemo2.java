
package com.xiaomi.stonelion.lucene;

import com.browseengine.bobo.api.BoboBrowser;
import com.browseengine.bobo.api.BoboIndexReader;
import com.browseengine.bobo.api.Browsable;
import com.browseengine.bobo.api.BrowseException;
import com.browseengine.bobo.api.BrowseHit;
import com.browseengine.bobo.api.BrowseRequest;
import com.browseengine.bobo.api.BrowseResult;
import com.browseengine.bobo.api.FacetAccessible;
import com.browseengine.bobo.facets.FacetHandler;
import com.browseengine.bobo.facets.data.PredefinedTermListFactory;
import com.browseengine.bobo.facets.impl.RangeFacetHandler;
import com.browseengine.bobo.facets.impl.SimpleFacetHandler;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class BoboBrowseDemo2 {
    private Browsable browser;

    public static void main(String[] args) throws CorruptIndexException, LockObtainFailedException, IOException, BrowseException {
        BoboBrowseDemo2 demo = new BoboBrowseDemo2();
        demo.setup();

        demo.test();
    }

    public void test() throws BrowseException {
        assert null != browser;
        BrowseRequest request = getBrowseRequest(false);
        BrowseResult result = browser.browse(request);
        printFieldValues(result);
        printFacet(result);
    }

    /**
     * 构建BrowseRequest
     * 
     * @param fetchStoredFields 是否获取Store.YES的域值
     * @return
     */
    private BrowseRequest getBrowseRequest(boolean fetchStoredFields) {
        BrowseRequest request = new BrowseRequest();
        request.setCount(10);
        request.setOffset(0);
        request.setFetchStoredFields(fetchStoredFields);

        return request;
    }

    /**
     * 打印搜索結果的文档域值
     * 
     * @param result
     */
    private void printFieldValues(BrowseResult result) {
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
                i++;
            }
        }
    }

    /**
     * 打印搜索结果的分组统计结果
     * 
     * @param result
     */
    private void printFacet(BrowseResult result) {
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

    private void f(String stringFormat) {
        System.out.println(stringFormat);
    }

    private static String KEY_ID = "id";
    private static String KEY_COLOR = "color";
    private static String KEY_SIZE = "size";
    private static String KEY_DATE = "date";
    private static List<String> KEYS = new ArrayList<String>();
    static {
        KEYS.add(KEY_ID);
        KEYS.add(KEY_COLOR);
        KEYS.add(KEY_SIZE);
        KEYS.add(KEY_DATE);
    }

    /**
     * 构建索引
     * 
     * @throws CorruptIndexException
     * @throws LockObtainFailedException
     * @throws IOException
     */
    public void setup() throws CorruptIndexException, LockObtainFailedException, IOException {
        Directory d = new RAMDirectory();
        IndexWriter indexWriter = new IndexWriter(d, new SimpleAnalyzer(), MaxFieldLength.UNLIMITED);

        // documents
        Map<String, String> fieldMap = new HashMap<String, String>();
        fieldMap.put(KEY_ID, "1");
        fieldMap.put(KEY_COLOR, "red");
        fieldMap.put(KEY_SIZE, "3");
        fieldMap.put(KEY_DATE, "2000/01/01");
        buildMetaField(indexWriter, fieldMap);

        fieldMap.clear();
        fieldMap.put(KEY_ID, "2");
        fieldMap.put(KEY_COLOR, "red");
        fieldMap.put(KEY_SIZE, "4");
        fieldMap.put(KEY_DATE, "2001/01/01");
        buildMetaField(indexWriter, fieldMap);

        fieldMap.clear();
        fieldMap.put(KEY_ID, "3");
        fieldMap.put(KEY_COLOR, "green");
        fieldMap.put(KEY_SIZE, "6");
        fieldMap.put(KEY_DATE, "2002/01/01");
        buildMetaField(indexWriter, fieldMap);

        fieldMap.clear();
        fieldMap.put(KEY_ID, "4");
        fieldMap.put(KEY_COLOR, "blue");
        fieldMap.put(KEY_SIZE, "7");
        fieldMap.put(KEY_DATE, "2003/01/01");
        buildMetaField(indexWriter, fieldMap);

        fieldMap.clear();
        fieldMap.put(KEY_ID, "5");
        fieldMap.put(KEY_COLOR, "blue");
        fieldMap.put(KEY_SIZE, "8");
        fieldMap.put(KEY_DATE, "2004/01/01");
        buildMetaField(indexWriter, fieldMap);

        indexWriter.close();
        IndexReader indexReader = IndexReader.open(d, true); // readonly
        List<FacetHandler<?>> facetHandlers = buildFacetHandlers();
        BoboIndexReader boboIndexReader = BoboIndexReader.getInstance(indexReader, facetHandlers);
        browser = new BoboBrowser(boboIndexReader);
    }

    /**
     * 构建FacetHandler list
     * 
     * @return
     */
    private List<FacetHandler<?>> buildFacetHandlers() {
        List<FacetHandler<?>> facetHandlers = new ArrayList<FacetHandler<?>>();
        facetHandlers.add(new SimpleFacetHandler(KEY_ID));
        facetHandlers.add(new SimpleFacetHandler(KEY_COLOR));
        facetHandlers.add(new RangeFacetHandler(KEY_SIZE, Arrays.asList(new String[] {
            "[* TO 4]", "[5 TO 7]", "[8 TO *]"
        })));
        facetHandlers.add(new RangeFacetHandler(KEY_DATE, new PredefinedTermListFactory(Date.class, "yyyy/MM/dd"), Arrays
                .asList(new String[] {
                    "[2000/01/01 TO 2003/01/01]", "[2004/01/01 TO *]"
                })));
        return facetHandlers;
    }

    /**
     * 添加field, document
     * 
     * @param indexWriter
     * @param fieldMap
     * @throws CorruptIndexException
     * @throws IOException
     */
    private void buildMetaField(IndexWriter indexWriter, Map<String, String> fieldMap) throws CorruptIndexException, IOException {
        assert indexWriter != null && fieldMap != null;
        Document document = new Document();
        for (Entry<String, String> entry : fieldMap.entrySet()) {
            Field field = new Field(entry.getKey(), entry.getValue(), Store.YES, Index.NOT_ANALYZED_NO_NORMS);
            field.setOmitTermFreqAndPositions(true);
            document.add(field);
        }
        indexWriter.addDocument(document);
    }

}
