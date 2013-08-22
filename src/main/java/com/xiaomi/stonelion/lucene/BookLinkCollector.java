
package com.xiaomi.stonelion.lucene;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 如何自定义collector<br>
 * Lucene的搜索都使用内置的Collector子类进行结果收集，相关性排序时使用TopScoreDocCollector, 通过域进行排序时使用TopFieldCollector<br>
 * 
 * @author shixin
 */
public class BookLinkCollector extends Collector {
    private Scorer scorer;
    private int docBase;
    private String[] urls;

    // scoreDocs
    private List<ScoreDoc> scoreDocs = new ArrayList<ScoreDoc>();
    // [doc:url]
    private Map<Integer, String> urlMap = new HashMap<Integer, String>();

    /**
     * 因为每个文档的相关性评分不会被带入到collector中，所以提供了一个setScorer方法把Scorer对象传进来，你可以在collect方法中使用Scorer.score()方法对文档进行评分<br>
     */
    @Override
    public void setScorer(Scorer scorer) throws IOException {
        // TODO Auto-generated method stub
        this.scorer = scorer;
    }

    /**
     * 每个匹配的文档都会进入这个方法
     */
    @Override
    public void collect(int doc) throws IOException {
        // TODO Auto-generated method stub
        // 采集url
        this.urlMap.put(new Integer(doc), urls[doc]);
        // 采集scoreDoc，包含绝对的docId和评分
        this.scoreDocs.add(new ScoreDoc(this.docBase + doc, this.scorer.score()));
    }

    /**
     * 因为每次搜索都是针对某个segment的，所以每次换segment都会进入这个方法
     */
    @Override
    public void setNextReader(IndexReader reader, int docBase) throws IOException {
        // TODO Auto-generated method stub
        urls = FieldCache.DEFAULT.getStrings(reader, "url");
        this.docBase = docBase;
    }

    /**
     * 是否接受乱序的docId, 如果接收，则会对某些查询加速
     */
    @Override
    public boolean acceptsDocsOutOfOrder() {
        // TODO Auto-generated method stub
        return true;
    }

    public Map<Integer, String> getUrlMap() {
        return Collections.unmodifiableMap(this.urlMap);
    }

    public List<ScoreDoc> getScoreDocs() {
        return Collections.unmodifiableList(this.scoreDocs);
    }

    public static void main(String[] args) throws CorruptIndexException, LockObtainFailedException, IOException {
        Directory d = new RAMDirectory();
        IndexWriter w = new IndexWriter(d, new WhitespaceAnalyzer(), true, IndexWriter.MaxFieldLength.UNLIMITED);

        Document d1 = new Document();
        d1.add(new Field("url", "10.0.0.1", Store.YES, Index.NOT_ANALYZED_NO_NORMS));
        d1.add(new Field("name", "hello world", Store.YES, Index.ANALYZED));
        w.addDocument(d1);

        Document d2 = new Document();
        d2.add(new Field("url", "10.0.0.2", Store.YES, Index.NOT_ANALYZED_NO_NORMS));
        d2.add(new Field("name", "hello this world", Store.YES, Index.ANALYZED));
        w.addDocument(d2);

        Document d3 = new Document();
        d3.add(new Field("url", "10.0.0.2", Store.YES, Index.NOT_ANALYZED_NO_NORMS));
        d3.add(new Field("name", "hello this world", Store.YES, Index.ANALYZED));
        w.addDocument(d3);
        w.close();
        
        IndexReader r = IndexReader.open(d);
        IndexSearcher s = new IndexSearcher(r);
        
        BookLinkCollector collector = new BookLinkCollector();
        s.search(new TermQuery(new Term("name", "hello")), null, collector);
        s.close();
        
        System.out.println(collector.getUrlMap());
        System.out.println(collector.getScoreDocs());
    }
}
