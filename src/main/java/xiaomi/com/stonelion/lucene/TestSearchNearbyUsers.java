
package xiaomi.com.stonelion.lucene;

import com.xiaomi.stonelion.lucene.LocationBasedUtil;
import com.xiaomi.stonelion.lucene.LocationComparatorSource;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * 测试附近的人
 * 
 * @author shixin
 */
public class TestSearchNearbyUsers {

    private static final String DOC_KEY_USERID = "userId";
    private static final String DOC_KEY_REGTIME = "regTime";
    private static final String DOC_KEY_SEX = "sex";
    private static final String DOC_KEY_LATITUDE = "latitude";
    private static final String DOC_KEY_LONGITUDE = "longitude";
    private static final String DOC_KEY_TOWERADDRESS = "towerAddress";

    private static final String LATLON_INDEX_PATH = "/home/shixin/document/latlon";
    private static final String TOWERS_INDEX_PATH = "/home/shixin/document/towers";

    private static final String LATLON_STAGING_INDEX_PATH = "/home/shixin/document/latlon-staging";
    private static final String TOWERS_STAGING_INDEX_PATH = "/home/shixin/document/towers-staging";

    public static void main(String[] args) throws Exception {
        System.out.println(System.currentTimeMillis());
        System.out.println((System.currentTimeMillis() - 1341042363000l) / 864000);
        testSearchByLatlon();
        // testSearchByTowers();
    }

    /**
     * 用基站查询
     * 
     * @throws IOException
     */
    private static void testSearchByTowers() throws IOException {
        IndexWriter towersIndexWriter = new IndexWriter(FSDirectory.open(new File(TOWERS_INDEX_PATH)), new SimpleAnalyzer(), false,
                MaxFieldLength.UNLIMITED);
        IndexSearcher towersIndexSearcher = new IndexSearcher(towersIndexWriter.getReader());

        long userId = 0;
        String towersAddress = "f8:d1:11:fa:b1:58";
        String sex = null;

        Query query = getNearbyUserTowerQuery(towersAddress);
        Filter filter = getNearbyUserFilter(userId, sex);
        Sort sort = new Sort();

        TopFieldDocs topFieldDocs = towersIndexSearcher.search(query, filter, 5, sort);
        for (ScoreDoc scoreDoc : topFieldDocs.scoreDocs) {
            System.out.println("-------------------------------------------------------");
            Document document = towersIndexSearcher.doc(scoreDoc.doc);
            System.out.println(document.get(DOC_KEY_USERID));
            System.out.println(document.get(DOC_KEY_REGTIME));
            System.out.println(document.get(DOC_KEY_SEX));
            System.out.println(Arrays.asList(document.getValues(DOC_KEY_TOWERADDRESS)));
        }
        System.out.println("Total hits : " + topFieldDocs.totalHits);

        towersIndexSearcher.close();
        towersIndexWriter.close();
    }

    private static Query getNearbyUserTowerQuery(String towersAddress) {
        BooleanQuery query = new BooleanQuery();
        query.add(new TermQuery(new Term(DOC_KEY_TOWERADDRESS, towersAddress)), Occur.SHOULD);
        return query;
    }

    /**
     * 用经纬度查询
     * 
     * @throws CorruptIndexException
     * @throws LockObtainFailedException
     * @throws IOException
     */
    private static void testSearchByLatlon() throws CorruptIndexException, LockObtainFailedException, IOException {
        IndexWriter latlonIndexWriter = new IndexWriter(FSDirectory.open(new File(LATLON_STAGING_INDEX_PATH)), new SimpleAnalyzer(), false,
                MaxFieldLength.UNLIMITED);

        IndexSearcher latlonIndexSearcher = new IndexSearcher(latlonIndexWriter.getReader());

        long userId = 0;
        double latitude = 39.98654;
        double longitude = 116.4883;
        String sex = "F";

        Query query = getNearbyUserLatlonQuery(latitude, longitude);
        Filter filter = getNearbyUserFilter(userId, sex);
        Sort sort = new Sort(new SortField("", new LocationComparatorSource(latitude, longitude)));

        TopFieldDocs topFieldDocs = latlonIndexSearcher.search(query, filter, 100, sort);
        for (ScoreDoc scoreDoc : topFieldDocs.scoreDocs) {
            System.out.println("-------------------------------------------------------");
            Document document = latlonIndexSearcher.doc(scoreDoc.doc);
            System.out.println(document.get(DOC_KEY_USERID));
            System.out.println(document.get(DOC_KEY_REGTIME));
            System.out.println(document.get(DOC_KEY_SEX));
            System.out.println(document.get(DOC_KEY_LATITUDE));
            System.out.println(document.get(DOC_KEY_LONGITUDE));
        }
        System.out.println("Total hits : " + topFieldDocs.totalHits);

        latlonIndexSearcher.close();
        latlonIndexWriter.close();
    }

    private static Query getNearbyUserLatlonQuery(double latitude, double longitude) {
        BooleanQuery query = new BooleanQuery();
        double minLat = LocationBasedUtil.getLowerLatitude(latitude);
        double maxLat = LocationBasedUtil.getUpperLatitude(latitude);
        double minLon = LocationBasedUtil.getLowerLongitude(longitude, latitude);
        double maxLon = LocationBasedUtil.getUpperLongitude(longitude, latitude);
        query.add(NumericRangeQuery.newDoubleRange(DOC_KEY_LATITUDE, minLat, maxLat, true, true), Occur.MUST);
        query.add(NumericRangeQuery.newDoubleRange(DOC_KEY_LONGITUDE, minLon, maxLon, true, true), Occur.MUST);
        return query;
    }

    private static Filter getNearbyUserFilter(long userId, String sex) {
        BooleanQuery query = new BooleanQuery();
        query.add(NumericRangeQuery.newLongRange(DOC_KEY_REGTIME, 0l, System.currentTimeMillis(), true, true), Occur.MUST);
        query.add(new TermQuery(new Term(DOC_KEY_USERID, String.valueOf(userId))), Occur.MUST_NOT);
        if (!StringUtils.isEmpty(sex)) {
            query.add(new TermQuery(new Term(DOC_KEY_SEX, sex)), Occur.MUST);
        }
        return new QueryWrapperFilter(query);
    }
}
