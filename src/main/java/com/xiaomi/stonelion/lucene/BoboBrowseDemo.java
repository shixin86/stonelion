
package com.xiaomi.stonelion.lucene;

import com.browseengine.bobo.api.BoboBrowser;
import com.browseengine.bobo.api.BoboIndexReader;
import com.browseengine.bobo.api.Browsable;
import com.browseengine.bobo.api.BrowseException;
import com.browseengine.bobo.api.BrowseFacet;
import com.browseengine.bobo.api.BrowseHit;
import com.browseengine.bobo.api.BrowseRequest;
import com.browseengine.bobo.api.BrowseResult;
import com.browseengine.bobo.api.FacetAccessible;
import com.browseengine.bobo.api.FacetSpec;
import com.browseengine.bobo.api.FacetSpec.FacetSortSpec;
import com.browseengine.bobo.facets.FacetHandler;
import com.browseengine.bobo.facets.impl.SimpleFacetHandler;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.SortField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BoboBrowseDemo {
    public static void main(String[] args) {
        LuceneUtil.run(BoboBrowseDemo.class);
    }

    @RunQuery
    public void testBoboBrowse() throws CorruptIndexException, LockObtainFailedException, IOException, BrowseException, ParseException {
        // lucene
        Directory directory = LuceneUtil.createRAMDirectory(new IKAnalyzer());
        IndexReader indexReader = IndexReader.open(directory, true);

        // bobo request
        SortField sortField = new SortField("index", SortField.STRING, false);

        FacetSpec citySpec = new FacetSpec();
        citySpec.setOrderBy(FacetSortSpec.OrderHitsDesc);

        BrowseRequest browseRequest = new BrowseRequest();
        browseRequest.setFetchStoredFields(false);
        // browseRequest.setShowExplanation(true);
        browseRequest.setCount(10);
        browseRequest.setOffset(0);

        browseRequest.setQuery(new MatchAllDocsQuery());
        // browseRequest.setQuery(new TermQuery(new Term("index", "a")));

        // BrowseSelection selection = new BrowseSelection("index");
        // selection.addValue("a");
        // browseRequest.addSelection(selection);

        browseRequest.setSort(new SortField[] {
            sortField
        });
        browseRequest.setFacetSpec("city", citySpec);

        // boob indexReader
        SimpleFacetHandler cityHandler = new SimpleFacetHandler("city");
        SimpleFacetHandler indexHandler = new SimpleFacetHandler("index");
        List<FacetHandler<?>> handlerList = Arrays.asList(new FacetHandler<?>[] {
            cityHandler, indexHandler
        });
        BoboIndexReader boboIndexReader = BoboIndexReader.getInstance(indexReader, handlerList);
        Browsable browser = new BoboBrowser(boboIndexReader);

        // bobo result
        BrowseResult browseResult = browser.browse(browseRequest);

        int totalHits = browseResult.getNumHits();
        System.out.println("命中的文档数：" + totalHits);
        BrowseHit[] hits = browseResult.getHits();
        for (BrowseHit browseHit : hits) {
            System.out.println(browseHit.getScore() + " " + browseHit.toString());
        }

        Map<String, FacetAccessible> facetMap = browseResult.getFacetMap();
        FacetAccessible cityFacets = facetMap.get("city");
        List<BrowseFacet> facetVals = cityFacets.getFacets();

        System.out.println("按城市统计");
        for (BrowseFacet facetVal : facetVals) {
            System.out.println("city name : " + facetVal.getValue() + ", count : " + facetVal.getFacetValueHitCount());
        }
    }
}
