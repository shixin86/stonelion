
package xiaomi.com.stonelion.lucene;

import com.chenlb.mmseg4j.analysis.MaxWordAnalyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.function.CustomScoreProvider;
import org.apache.lucene.search.function.CustomScoreQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import xiaomi.com.stonelion.lucene.Util.AllInOneUserDocKey;
import xiaomi.com.stonelion.lucene.Util.UserBoost;

import java.io.File;
import java.io.IOException;

/**
 * 测试线上的user index
 * 
 * @author shixin
 */
public class TestUserIndex {
    private static String INDEX_PATH = "/home/shixin/document/allInOne/temp";
    private static String DICT_PATH = "/home/shixin/document/chinese_dict";

    public static void main(String[] args) throws IOException, ParseException {
        testSearchMiliaoUser("雷军");
    }

    private static void testSearchMiliaoUser(String queryStr) throws IOException, ParseException {
        Directory directory = FSDirectory.open(new File(INDEX_PATH));
        Analyzer analyzer = new MaxWordAnalyzer(new File(DICT_PATH));
        IndexWriter indexWriter = new IndexWriter(directory, analyzer, false, MaxFieldLength.UNLIMITED);
        IndexSearcher indexSearcher = new IndexSearcher(indexWriter.getReader());

        Query query = getQuery(queryStr, analyzer);
        ActivenessBoostingQuery activenessBoostingQuery = new ActivenessBoostingQuery(query, 2.0);

        TopDocs topDocs = indexSearcher.search(activenessBoostingQuery, 10);
        System.out.println("count : " + topDocs.totalHits);
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document document = indexSearcher.doc(scoreDoc.doc);
            System.out.println(document.get("userId") + ":" + document.get("nickname") + ":" + document.get("school") + ":"
                    + document.get("corporation") + ":" + document.get("activeness"));
            Explanation e = indexSearcher.explain(query, scoreDoc.doc);
            System.out.println(e.toString());
            System.out.println("----------------------------------------------------");
        }
    }

    private static Query getQuery(String query, Analyzer analyzer) throws ParseException {
        BooleanQuery booleanQuery = new BooleanQuery();
        String[] subQueries = query.trim().split("\\s+");
        if (null != subQueries && subQueries.length > 1) {
            for (String subQuery : subQueries) {
                booleanQuery.add(getBasicQuery(subQuery, analyzer), Occur.MUST);
            }
            return booleanQuery;
        }
        return getBasicQuery(query, analyzer);
    }

    private static Query getBasicQuery(String query, Analyzer analyzer) throws ParseException {
        BooleanQuery booleanQuery = new BooleanQuery();
        String escapedquery = QueryParser.escape(query);

        Query userIdQuery = new TermQuery(new Term(AllInOneUserDocKey.USERID, query));
        userIdQuery.setBoost(UserBoost.FIELD_USERID_BOOST);
        booleanQuery.add(userIdQuery, BooleanClause.Occur.SHOULD);

        Query nicknameQuery = getParsedQuery(AllInOneUserDocKey.NICKNAME, analyzer, escapedquery);
        nicknameQuery.setBoost(UserBoost.FIELD_NICKNAME_BOOST);
        booleanQuery.add(nicknameQuery, Occur.SHOULD);

        Query schoolsQuery = getParsedQuery(AllInOneUserDocKey.SCHOOL, analyzer, escapedquery);
        schoolsQuery.setBoost(UserBoost.FIELD_SCHOOLS_BOOST);
        booleanQuery.add(schoolsQuery, Occur.SHOULD);

        Query corporationsQuery = getParsedQuery(AllInOneUserDocKey.CORPRATION, analyzer, escapedquery);
        corporationsQuery.setBoost(UserBoost.FIELD_CORPORATIONS_BOOST);
        booleanQuery.add(corporationsQuery, Occur.SHOULD);

        Query cityQuery = getParsedQuery(AllInOneUserDocKey.CITY, analyzer, escapedquery);
        cityQuery.setBoost(UserBoost.FIELD_CITY_BOOST);
        booleanQuery.add(cityQuery, Occur.SHOULD);

        return booleanQuery;
    }

    private static Query getParsedQuery(String fieldName, Analyzer analyzer, String fieldValue) throws ParseException {
        return new QueryParser(Version.LUCENE_30, fieldName, analyzer).parse(fieldValue);
    }

    private static class ActivenessBoostingQuery extends CustomScoreQuery {
        private static final long serialVersionUID = 1L;
        private double multiplier;

        public ActivenessBoostingQuery(Query subQuery, double multiplier) {
            super(subQuery);
            this.multiplier = multiplier;
        }

        @Override
        protected CustomScoreProvider getCustomScoreProvider(IndexReader reader) throws IOException {
            return new ActivinessCoreProvider(reader);
        }

        private class ActivinessCoreProvider extends CustomScoreProvider {
            private double[] activiness;

            public ActivinessCoreProvider(IndexReader reader) throws IOException {
                super(reader);
                activiness = FieldCache.DEFAULT.getDoubles(reader, "activeness");
            }

            @Override
            public float customScore(int doc, float subQueryScore, float valSrcScore) throws IOException {
                return subQueryScore + (float) (multiplier * activiness[doc]);
            }
        }
    }

    private static void testMutilpleBooleanQuery() throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
        Analyzer analyzer = new SimpleAnalyzer();
        IndexWriter indexWriter = new IndexWriter(new RAMDirectory(), analyzer, true, MaxFieldLength.UNLIMITED);

        Document document = new Document();
        document.add(new Field("id", "雷军1", Store.YES, Index.NOT_ANALYZED_NO_NORMS));
        Field nameField = new Field("name", "小米", Store.YES, Index.ANALYZED);
        nameField.setBoost(1.3f);
        document.add(nameField);
        Field schoolField = new Field("school", "雷军", Store.YES, Index.ANALYZED);
        schoolField.setBoost(1.0f);
        document.add(schoolField);
        Field corpField = new Field("corp", "雷军", Store.YES, Index.ANALYZED);
        corpField.setBoost(1.0f);
        document.add(corpField);
        Field cityField = new Field("city", "雷军", Store.YES, Index.ANALYZED);
        cityField.setBoost(1.0f);
        document.add(cityField);
        indexWriter.addDocument(document);

        document = new Document();
        document.add(new Field("id", "2", Store.YES, Index.NOT_ANALYZED_NO_NORMS));
        nameField = new Field("name", "石欣", Store.YES, Index.ANALYZED);
        nameField.setBoost(1.3f);
        document.add(nameField);
        schoolField = new Field("school", "理工", Store.YES, Index.ANALYZED);
        schoolField.setBoost(1.0f);
        document.add(schoolField);
        corpField = new Field("corp", "小米", Store.YES, Index.ANALYZED);
        corpField.setBoost(1.0f);
        document.add(corpField);
        cityField = new Field("city", "北京", Store.YES, Index.ANALYZED);
        cityField.setBoost(1.0f);
        document.add(cityField);
        indexWriter.addDocument(document);

        indexWriter.optimize();

        IndexSearcher indexSearcher = new IndexSearcher(indexWriter.getReader());

        BooleanQuery query = new BooleanQuery();
        BooleanQuery query1 = new BooleanQuery();
        BooleanQuery query2 = new BooleanQuery();
        query.add(query1, Occur.MUST);
        query.add(query2, Occur.MUST);

        query1.add(new QueryParser(Version.LUCENE_30, "id", analyzer).parse("雷军"), Occur.SHOULD);
        query1.add(new QueryParser(Version.LUCENE_30, "name", analyzer).parse("雷军"), Occur.SHOULD);
        query1.add(new QueryParser(Version.LUCENE_30, "school", analyzer).parse("雷军"), Occur.SHOULD);
        query1.add(new QueryParser(Version.LUCENE_30, "corp", analyzer).parse("雷军"), Occur.SHOULD);
        query1.add(new QueryParser(Version.LUCENE_30, "city", analyzer).parse("雷军"), Occur.SHOULD);

        query2.add(new QueryParser(Version.LUCENE_30, "id", analyzer).parse("小米"), Occur.SHOULD);
        query2.add(new QueryParser(Version.LUCENE_30, "name", analyzer).parse("小米"), Occur.SHOULD);
        query2.add(new QueryParser(Version.LUCENE_30, "school", analyzer).parse("小米"), Occur.SHOULD);
        query2.add(new QueryParser(Version.LUCENE_30, "corp", analyzer).parse("小米"), Occur.SHOULD);
        query2.add(new QueryParser(Version.LUCENE_30, "city", analyzer).parse("小米"), Occur.SHOULD);

        TopDocs topDocs = indexSearcher.search(query, 10);
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            System.out.println(indexSearcher.doc(scoreDoc.doc).get("id") + " : " + scoreDoc.score);
        }
    }
}
