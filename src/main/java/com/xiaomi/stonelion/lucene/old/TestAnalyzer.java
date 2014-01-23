
package com.xiaomi.stonelion.lucene.old;

import com.chenlb.mmseg4j.analysis.ComplexAnalyzer;
import com.chenlb.mmseg4j.analysis.MMSegAnalyzer;
import com.chenlb.mmseg4j.analysis.MaxWordAnalyzer;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

public class TestAnalyzer {
    public static void main(String[] args) throws IOException, ParseException {
        testKindsOfAnalyzer();
        testSimpleChinese();
    }

    /**
     * 看看用什么analyzer
     */
    private static void testSimpleChinese() throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
//        Analyzer analyzer = new MaxWordAnalyzer(new File("/home/shixin/workspace/stonelion/index/dict"));
        Analyzer analyzer = new MaxWordAnalyzer();
        IndexWriter indexWriter = new IndexWriter(new RAMDirectory(), analyzer, true, MaxFieldLength.UNLIMITED);
        Document document = new Document();
        document.add(new Field("id", "1", Store.YES, Index.NOT_ANALYZED_NO_NORMS));
        document.add(new Field("name", "哈北大", Store.YES, Index.ANALYZED));

        indexWriter.addDocument(document);
        indexWriter.optimize();
        IndexSearcher indexSearcher = new IndexSearcher(indexWriter.getReader());

        // 尝试使用各种query
        // 1 parser出来的QUERY
         Query query = new QueryParser(Version.LUCENE_30, "name", analyzer).parse("北大");
        System.out.println(query.getClass());
        // 2 termQuery
//        Query query = new TermQuery(new Term("name", "北大"));

        TopDocs topDocs = indexSearcher.search(query, 10);
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            System.out.println(indexSearcher.doc(scoreDoc.doc).get("id") + " : " + scoreDoc.score);
        }
    }

    /**
     * 查看各种analyzer是如何分词的
     * 
     * @throws java.io.IOException
     */
    private static void testKindsOfAnalyzer() throws IOException {
        String text = "北大";

        Analyzer analyzer = new WhitespaceAnalyzer();
        printToken(analyzer, text);

        analyzer = new SimpleAnalyzer();
        printToken(analyzer, text);

        analyzer = new StopAnalyzer(Version.LUCENE_30);
        printToken(analyzer, text);

        analyzer = new StandardAnalyzer(Version.LUCENE_30);
        printToken(analyzer, text);

        analyzer = new KeywordAnalyzer();
        printToken(analyzer, text);

        analyzer = new PerFieldAnalyzerWrapper(new SimpleAnalyzer());
        printToken(analyzer, text);

        // analyzer = new MaxWordAnalyzer(new File("/home/shixin/workspace/stonelion/index/dict"));
        analyzer = new MaxWordAnalyzer();
        printToken(analyzer, text);

        analyzer = new ComplexAnalyzer(new File("/home/shixin/workspace/stonelion/index/dict"));
        printToken(analyzer, text);

        analyzer = new MMSegAnalyzer();
        printToken(analyzer, text);
    }

    private static void printToken(Analyzer analyzer, String text) throws IOException {
        System.out.println("analyzer : " + analyzer.getClass() + " | text : " + text);

        TokenStream tokenSream = analyzer.tokenStream("", new StringReader(text));
        while (tokenSream.incrementToken()) {
            TermAttribute termAttribute = tokenSream.getAttribute(TermAttribute.class);
            System.out.println(termAttribute.term());
        }
        tokenSream.close();
    }

}
