
package com.xiaomi.stonelion.lucene.spatial;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.spatial.tier.DistanceFieldComparatorSource;
import org.apache.lucene.spatial.tier.DistanceQueryBuilder;
import org.apache.lucene.spatial.tier.projections.CartesianTierPlotter;
import org.apache.lucene.spatial.tier.projections.IProjector;
import org.apache.lucene.spatial.tier.projections.SinusoidalProjector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.NumericUtils;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LuceneSpatial {
    private IProjector _iProjector;
    private CartesianTierPlotter _cartesianTierPlotter;

    private Analyzer _anlyzer;
    private Directory _directory;
    private IndexWriter _indexWriter;
    private IndexReader _indexReader;
    private IndexSearcher _indexSearcher;

    private double _MAX_RANGE = 100;
    private double _MIN_RANGE = 1;
    private int _startTier;
    private int _endTier;
    private double _RATE_MILE_TO_KM = 1.609344;

    public LuceneSpatial() throws CorruptIndexException, LockObtainFailedException, IOException {
        _iProjector = new SinusoidalProjector();
        _cartesianTierPlotter = new CartesianTierPlotter(0, _iProjector, CartesianTierPlotter.DEFALT_FIELD_PREFIX);

        _startTier = _cartesianTierPlotter.bestFit(_MAX_RANGE / _RATE_MILE_TO_KM);
        _endTier = _cartesianTierPlotter.bestFit(_MIN_RANGE / _RATE_MILE_TO_KM);
        System.out.println(_startTier + ":" + _endTier);

        _anlyzer = new StandardAnalyzer(Version.LUCENE_35);
        _directory = new RAMDirectory();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_35, _anlyzer);
        indexWriterConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
        _indexWriter = new IndexWriter(_directory, indexWriterConfig);

        // 建立索引
        index(1, 39.959661, 116.31614);
        index(2, 39.9604572, 116.3185113);
        index(3, 39.960396, 116.319819);
        index(4, 39.960395, 116.319820);
        index(5, 39.982703, 116.464011);

        _indexWriter.commit();

        _indexReader = IndexReader.open(_directory, true);
        _indexSearcher = new IndexSearcher(_indexReader);
    }

    private void index(long userId, double latitude, double longitude) throws CorruptIndexException, IOException {
        Document document = new Document();
        document.add(new Field("id", String.valueOf(userId), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
        indexLocation(document, latitude, longitude);
        _indexWriter.addDocument(document);
    }

    private void indexLocation(Document document, double latitude, double longitude) {
        document.add(new Field("lat", NumericUtils.doubleToPrefixCoded(latitude), Store.YES, Index.NOT_ANALYZED));
        document.add(new Field("lng", NumericUtils.doubleToPrefixCoded(longitude), Store.YES, Index.NOT_ANALYZED));

        for (int tier = _startTier; tier <= _endTier; tier++) {
            _cartesianTierPlotter = new CartesianTierPlotter(tier, _iProjector, CartesianTierPlotter.DEFALT_FIELD_PREFIX);
            double boxId = _cartesianTierPlotter.getTierBoxId(latitude, longitude);
            document.add(new Field(_cartesianTierPlotter.getTierFieldName(), NumericUtils.doubleToPrefixCoded(boxId), Store.YES,
                    Index.NOT_ANALYZED_NO_NORMS));
        }
    }

    private double km2mile(double km) {
        return km / _RATE_MILE_TO_KM;
    }

    private int mile2Meter(double miles) {
        return (int) (miles * _RATE_MILE_TO_KM * 1000);
    }

    public List<String> search(String keyword, double latitude, double longitude, double range) throws IOException {
        List<String> results = new ArrayList<String>();

        double miles = km2mile(range);
        DistanceQueryBuilder distanceQueryBuilder = new DistanceQueryBuilder(latitude, longitude, miles, "lat", "lng",
                CartesianTierPlotter.DEFALT_FIELD_PREFIX, true, _startTier, _endTier);

        DistanceFieldComparatorSource distanceFieldComparatorSource = new DistanceFieldComparatorSource(
                distanceQueryBuilder.getDistanceFilter());
        Sort sort = new Sort(new SortField("geo_distance", distanceFieldComparatorSource));

        Query query = buildQuery();
        TopDocs hits = _indexSearcher.search(query, distanceQueryBuilder.getFilter(), Integer.MAX_VALUE, sort);

        Map<Integer, Double> distance = distanceQueryBuilder.getDistanceFilter().getDistances();

        for (int i = 0; i < hits.totalHits; i++) {
            int docId = hits.scoreDocs[i].doc;

            Document doc = _indexSearcher.doc(docId);
            System.out.println(doc.get("id") + ":" + mile2Meter(distance.get(docId)));
        }

        return results;
    }

    private Query buildQuery() {
        return new MatchAllDocsQuery();
    }

    public static void main(String[] args) throws CorruptIndexException, LockObtainFailedException, IOException {
        LuceneSpatial luceneSpatial = new LuceneSpatial();
        luceneSpatial.search("", 39.959661, 116.31614, 20);
    }
}
