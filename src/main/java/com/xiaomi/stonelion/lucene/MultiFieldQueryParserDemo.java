
package com.xiaomi.stonelion.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

/**
 * 1 针对多个域进行一次查询<br>
 * 2 使用DisjunctionMaxQuery会是匹配多个条件的文档分数是最高的
 * 
 * @author shixin
 */
public class MultiFieldQueryParserDemo {
    public static void main(String[] args) throws Exception {
        testMultiFieldQueryParser();
    }

    private static void testMultiFieldQueryParser() throws Exception {
        Directory directory = new RAMDirectory();
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
        IndexWriter indexWriter = new IndexWriter(directory, analyzer, MaxFieldLength.UNLIMITED);

        Document document1 = new Document();
        document1.add(new Field("name", "The quick brown fox jumped over the lazy dog!", Store.YES, Index.ANALYZED_NO_NORMS));
        document1.add(new Field("type", "AAA", Store.YES, Index.ANALYZED_NO_NORMS));

        Document document2 = new Document();
        document2.add(new Field("name", "Hello world AAA", Store.YES, Index.ANALYZED_NO_NORMS));
        document2.add(new Field("type", "BBB", Store.YES, Index.ANALYZED_NO_NORMS));

        indexWriter.addDocument(document1);
        indexWriter.addDocument(document2);
        indexWriter.close();

        // 1
        // 默认每个Field的occur是should
        // MultiFieldQueryParser multiFieldQueryParser = new MultiFieldQueryParser(Version.LUCENE_30, new String[]{
        // "name", "type"
        // }, analyzer);
        // Query query = multiFieldQueryParser.parse("AAA");

        // 2
        // 可制定每个field的Occur
        Query query = MultiFieldQueryParser.parse(Version.LUCENE_30, "AAA", new String[] {
            "name", "type"
        }, new BooleanClause.Occur[] {
            BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD
        }, analyzer);
        System.out.println(query.toString());
        System.out.println(query.getClass());

        IndexSearcher indexSearcher = new IndexSearcher(directory);
        TopDocs topDocs = indexSearcher.search(query, 1);
        System.out.println(topDocs.totalHits);

        analyzer.close();
        indexSearcher.close();
        directory.close();
    }
}
