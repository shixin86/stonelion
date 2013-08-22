
package com.xiaomi.stonelion.lucene;

import com.browseengine.bobo.api.BoboBrowser;
import com.browseengine.bobo.api.BoboIndexReader;
import com.browseengine.bobo.api.Browsable;
import com.browseengine.bobo.api.BrowseException;
import com.browseengine.bobo.api.BrowseFacet;
import com.browseengine.bobo.api.BrowseHit;
import com.browseengine.bobo.api.BrowseRequest;
import com.browseengine.bobo.api.BrowseResult;
import com.browseengine.bobo.api.BrowseSelection;
import com.browseengine.bobo.api.FacetAccessible;
import com.browseengine.bobo.api.FacetSpec;
import com.browseengine.bobo.api.FacetSpec.FacetSortSpec;
import com.browseengine.bobo.facets.FacetHandler;
import com.browseengine.bobo.facets.impl.SimpleFacetHandler;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
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
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.io.StringReader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SuppressWarnings({
    "deprecation", "unused"
})
public class QueryDemo {
    // 常用的分析器
    private static Analyzer[] analyzers = new Analyzer[] {
        new WhitespaceAnalyzer(), new SimpleAnalyzer(), new StopAnalyzer(Version.LUCENE_35), new StandardAnalyzer(Version.LUCENE_35),
        new IKAnalyzer()
    };

    public static void main(String[] args) throws Exception {
        LuceneUtil.run(QueryDemo.class);
    }

    /**
     * 最简单的Query
     * 
     * @throws IOException
     * @throws LockObtainFailedException
     * @throws CorruptIndexException
     */
    @RunQuery
    public void testTermQuery() throws CorruptIndexException, LockObtainFailedException, IOException {
        Query query = new TermQuery(new Term(LuceneUtil.FIELD_NAME, "京城"));
        System.out.println("-----run TermQuery-----");
        System.out.println("Query : " + query.toString());
        runQuery(query);
    }

    /**
     * lucene会使用String.compareTo方法来比较对应的field值<br>
     * 用户也可以自定义Collator用户自定义如何进行比较，但是会比较缓慢，使用开源的CollationKeyAnalyzer可以加速<br>
     * 
     * @throws CorruptIndexException
     * @throws LockObtainFailedException
     * @throws IOException
     */
    @RunQuery
    public void testTermRangeQuery() throws CorruptIndexException, LockObtainFailedException, IOException {
        Query query = new TermRangeQuery(LuceneUtil.FIELD_INDEX, "a", "c", true, true);
        System.out.println("-----run TermRangeQuery-----");
        System.out.println("Query : " + query.toString());
        runQuery(query);
    }

    /**
     * 数字的范围查询要比TermRangeQuery要快，TODO 为什么快?
     * 
     * @throws CorruptIndexException
     * @throws LockObtainFailedException
     * @throws IOException
     */
    @RunQuery
    public void testNumericRangeQuery() throws CorruptIndexException, LockObtainFailedException, IOException {
        Query query = NumericRangeQuery.newLongRange(LuceneUtil.FIELD_BIRTHDAY, 1988l, 2000l, true, true);
        System.out.println("-----run NumericRangeQuery-----");
        System.out.println("Query : " + query.toString());
        runQuery(query);
    }

    /**
     * 通过前缀进行查询
     * 
     * @throws CorruptIndexException
     * @throws LockObtainFailedException
     * @throws IOException
     */
    @RunQuery
    public void testPrefixQuery() throws CorruptIndexException, LockObtainFailedException, IOException {
        Query query = new PrefixQuery(new Term(LuceneUtil.FIELD_CATEGORY, "/usa"));
        System.out.println("-----run PrefixQuery-----");
        System.out.println("Query : " + query.toString());
        runQuery(query);
    }

    /**
     * 通配符查询 <br>
     * 1可能使查询缓慢，所以通配符前缀要尽可能详细，因为lucene会去枚举所有的可能<br>
     * 2*0或多个字符，？0或1个字符<br>
     * 3 通配符查詢對評分沒有影響
     * 
     * @throws CorruptIndexException
     * @throws LockObtainFailedException
     * @throws IOException
     * @throws ParseException
     */
    @RunQuery
    public void testWildcardQuery() throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
        // Query query = new WildcardQuery(new Term("name", "?ame*"));

        // 使用QueryParser生成通配符查询
        QueryParser queryParser = new QueryParser(Version.LUCENE_30, LuceneUtil.FIELD_NAME, new IKAnalyzer());
        // 不变成小写形式
        queryParser.setLowercaseExpandedTerms(false);
        // 默认表达式的开头不能用通配符，可以设定
        queryParser.setAllowLeadingWildcard(true);

        /**
         * 只要在表达式中有*或者?就会被parse成WildcardQuery<br>
         * 但是只有在表达式末尾有通配符的时候，会被优化成PrefixQuery
         */
        Query query = queryParser.parse("?ame*");

        System.out.println("-----run WildcardQuery-----");
        System.out.println("Query : " + query.toString());
        runQuery(query);
    }

    /**
     * 模糊查询，使用Levenshetein距离算法计算相似度
     */
    public void testFuzzyQuery() {

    }

    /**
     * 1 索引的时候如果不指定omitTermFreqAndPosition选项，位置信息就可以被索引，就可以使用短语查询<br>
     * 2 评分规则1 / distance + 1 <br>
     * 3 slop是term需要移动几次才能构成短语
     * 
     * @throws IOException
     * @throws LockObtainFailedException
     * @throws CorruptIndexException
     */
    @RunQuery
    public void testPhraseQuery() throws CorruptIndexException, LockObtainFailedException, IOException {
        PhraseQuery phraseQuery = new PhraseQuery();
        phraseQuery.add(new Term(LuceneUtil.FIELD_SIGNATURE, "quick"));
        phraseQuery.add(new Term(LuceneUtil.FIELD_SIGNATURE, "fox"));
        phraseQuery.setSlop(1);

        System.out.println("-----run PhraseQuery-----");
        System.out.println("Query : " + phraseQuery.toString());
        runQuery(phraseQuery);
    }

    /**
     * 和PhraseQuery的区别就是可以多指定几个term
     * 
     * @throws IOException
     * @throws LockObtainFailedException
     * @throws CorruptIndexException
     */
    @RunQuery
    public void testMultiPhraseQuery() throws CorruptIndexException, LockObtainFailedException, IOException {
        MultiPhraseQuery multiPhraseQuery = new MultiPhraseQuery();
        // 先匹配第一个term
        multiPhraseQuery.add(new Term[] {
            new Term(LuceneUtil.FIELD_SIGNATURE, "quick"), new Term(LuceneUtil.FIELD_SIGNATURE, "fast")
        });
        // 再匹配第二个
        multiPhraseQuery.add(new Term("signature", "fox"));
        multiPhraseQuery.setSlop(1);

        System.out.println("-----run MultiPhraseQuery-----");
        System.out.println("Query : " + multiPhraseQuery.toString());
        runQuery(multiPhraseQuery);
    }

    public void runQuery(Query query) throws CorruptIndexException, LockObtainFailedException, IOException {
        Directory d = LuceneUtil.createRAMDirectory(new IKAnalyzer());
        IndexSearcher indexSearcher = new IndexSearcher(d);
        TopDocs topDocs = indexSearcher.search(query, 10);
        System.out.println("Query total hits : " + topDocs.totalHits);
    }
}
