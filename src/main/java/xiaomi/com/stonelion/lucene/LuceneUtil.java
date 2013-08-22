
package xiaomi.com.stonelion.lucene;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.FieldCacheRangeFilter;
import org.apache.lucene.search.FieldCacheTermsFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeFilter;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixFilter;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeFilter;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;
import java.util.List;

public class LuceneUtil {

    public static Directory genRAMDirectory(Analyzer analyzer, List<User> users) throws CorruptIndexException, LockObtainFailedException,
                                                                                IOException {
        Directory directory = new RAMDirectory();
        IndexWriter indexWriter = null;
        try {
            indexWriter = new IndexWriter(directory, analyzer, true, MaxFieldLength.UNLIMITED);

            // 添加文档
            for (User user : users) {
                addDocument(indexWriter, user);
            }

            return directory;
        } finally {
            if (null != indexWriter) {
                indexWriter.close();
            }
        }
    }

    private static void addDocument(IndexWriter indexWriter, User user) throws CorruptIndexException, IOException {
        Document document = new Document();

        Field idField = new Field(User.KEY_ID, String.valueOf(user.id), Store.YES, Index.NOT_ANALYZED);
        // 跳过出现位置和出现频率的索引
        idField.setOmitTermFreqAndPositions(true);
        // 在评分时候不加权
        idField.setOmitNorms(true);
        // 使用indexwriter的分析器生成field
        document.add(idField);

        Field nameField = new Field(User.KEY_NAME, user.name, Store.YES, Index.ANALYZED);
        document.add(nameField);

        // 索引数字
        NumericField birthdayField = new NumericField(User.KEY_BIRTHDAY, Store.YES, true).setLongValue(user.birthday);
        birthdayField.setOmitNorms(true);
        birthdayField.setOmitTermFreqAndPositions(true);
        document.add(birthdayField);

        indexWriter.addDocument(document);
    }

    public static void displayResult(Directory directory, Query query, Sort sort) throws CorruptIndexException, IOException {
        // 以只读方式打开
        IndexReader indexReader = IndexReader.open(directory, true);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        // 指定对搜索到的结果进行评分
        indexSearcher.setDefaultFieldSortScoring(true, false);

        // 查询
        TopDocs topDocs = null;
        if (null == sort) {
            topDocs = indexSearcher.search(query, 20);
        } else {
            topDocs = indexSearcher.search(query, null, 20, sort);
        }

        System.out.println("\nResults for : " + query.toString() + " sorted by " + sort);
        System.out.println(StringUtils.rightPad("id", 10) + StringUtils.rightPad("name", 20) + StringUtils.rightPad("birthday", 20)
                + StringUtils.center("score", 10));

        for (ScoreDoc doc : topDocs.scoreDocs) {
            int docId = doc.doc;
            float score = doc.score;
            Document document = indexSearcher.doc(docId);
            System.out.println(StringUtils.rightPad(document.get(User.KEY_ID), 10) + StringUtils.rightPad(document.get(User.KEY_NAME), 20)
                    + StringUtils.rightPad(document.get(User.KEY_BIRTHDAY), 20) + StringUtils.center(String.valueOf(score), 10));
        }
    }

    /**
     * 根据特定项目进行搜索
     * 
     * @param filedName
     * @param fieldValue
     * @return
     */
    public static Query getTermQuery(String filedName, String fieldValue) {
        Term term = new Term(filedName, fieldValue);
        return new TermQuery(term);
    }

    /**
     * 数字范围搜索，包含上下界限
     * 
     * @param fieldName
     * @param min
     * @param max
     * @return
     */
    public static Query getLongNumericRangeQuery(String fieldName, long min, long max) {
        boolean minInclusive = true;
        boolean maxInclusive = true;
        return NumericRangeQuery.newLongRange(fieldName, min, max, minInclusive, maxInclusive);
    }

    /**
     * 通过前缀查询
     * 
     * @param filedName
     * @param fieldValue
     * @return
     */
    public static Query getPrefixQuery(String filedName, String fieldValue) {
        Term term = new Term(filedName, fieldValue);
        return new PrefixQuery(term);
    }

    /**
     * 通过短语查询
     * 
     * @param fieldName
     * @param fieldValues
     * @param slot
     * @return
     */
    public static Query getPhraseQuery(String fieldName, String[] fieldValues, int slot) {
        PhraseQuery query = new PhraseQuery();
        query.setSlop(slot);
        for (String fieldValue : fieldValues) {
            query.add(new Term(fieldName, fieldValue));
        }
        return query;
    }

    /**
     * 通配符查询
     * 
     * @param filedName
     * @param fieldValue
     * @return
     */
    public static Query getWildcardQuery(String filedName, String fieldValue) {
        Term term = new Term(filedName, fieldValue);
        return new WildcardQuery(term);
    }

    /**
     * 搜索类似项
     * 
     * @param filedName
     * @param fieldValue
     * @return
     */
    public static Query getFuzzyQuery(String filedName, String fieldValue) {
        Term term = new Term(filedName, fieldValue);
        return new FuzzyQuery(term);
    }

    /**
     * 匹配所有文档
     * 
     * @return
     */
    public static Query getMatchAllDocsQuery() {
        return new MatchAllDocsQuery();
    }

    /**
     * 域范围过滤器
     * @param fieldName
     * @param lowerTerm
     * @param upperTerm
     * @param includeLower
     * @param includeUpper
     * @return
     */
    public static Filter getTermRangeFilter(String fieldName, String lowerTerm, String upperTerm, boolean includeLower,
        boolean includeUpper) {
        if(null == upperTerm && null == lowerTerm){
            return null;
        }
        
        if(null == lowerTerm)
            return TermRangeFilter.Less(fieldName, upperTerm);
        
        if(null == upperTerm)
            return TermRangeFilter.More(fieldName, lowerTerm);
        
            
        return new TermRangeFilter(fieldName, lowerTerm, upperTerm, includeLower, includeUpper);
    }
    
    /**
     * 
     * @param field
     * @param min
     * @param max
     * @param minInclusive
     * @param maxInclusive
     * @return
     */
    public static Filter getLongNumericRangeFilter(String field, long min, long max, boolean minInclusive, boolean maxInclusive){
        return NumericRangeFilter.newLongRange(field, min, max, minInclusive, maxInclusive);
    }
    
    /**
     * 使用域缓存实现TermRangeFilter和NumericRangeFilter相同的效果
     * 但是FieldCacheRangeFilter会使用已经放入内存的域缓存数据，会提升性能
     * @param fieldName
     * @param lowerTerm
     * @param upperTerm
     * @param includeLower
     * @param includeUpper
     * @return
     */
    public static Filter getFieldCacheRangeFilter(String fieldName, String lowerTerm, String upperTerm, boolean includeLower,
        boolean includeUpper){
        return FieldCacheRangeFilter.newStringRange(fieldName, lowerTerm, upperTerm, includeLower, includeUpper);
    }
    
    public static Filter getFieldCacheTermsFilter(String field, String fileds){
        return new FieldCacheTermsFilter(field, fileds);
    }
    
    public static Filter getQueryWrapperFilter(Query query){
        return new QueryWrapperFilter(query);
    }
    
    public static Filter getPrefixFilter(String field, String value){
        return new PrefixFilter(new Term(field, value));
    }
}
