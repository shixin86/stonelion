
package xiaomi.com.stonelion.lucene;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AllDocCollector extends Collector {
    private Scorer scorer;
    private int docBase;
    private List<ScoreDoc> docs = new ArrayList<ScoreDoc>();

    @Override
    public boolean acceptsDocsOutOfOrder() {
        return true;
    }

    @Override
    public void collect(int doc) throws IOException {
        docs.add(new ScoreDoc(doc + docBase, scorer.score()));
    }

    @Override
    public void setNextReader(IndexReader indexReader, int docBase) throws IOException {
        // 可以加载域缓存

        this.docBase = docBase;
    }

    @Override
    public void setScorer(Scorer scorer) throws IOException {
        this.scorer = scorer;
    }

    public List<ScoreDoc> getHits() {
        return docs;
    }

    // test
    public static void main(String[] args) throws CorruptIndexException, LockObtainFailedException, IOException {
        IndexWriter indexWriter = new IndexWriter(new RAMDirectory(), new SimpleAnalyzer(), true, MaxFieldLength.UNLIMITED);

        addDocument(indexWriter, "hello");
        addDocument(indexWriter, "helloworld");

        IndexSearcher indexSearcher = new IndexSearcher(indexWriter.getReader());

        Query query = new TermQuery(new Term("name", "hello"));
        AllDocCollector collector = new AllDocCollector();
        indexSearcher.search(query, collector);

        for (ScoreDoc scoreDoc : collector.getHits()) {
            System.out.println("docId : " + scoreDoc.doc + " doc : " + scoreDoc.score);
        }
    }

    private static void addDocument(IndexWriter indexWriter, String name) throws CorruptIndexException, IOException {
        Document document = new Document();
        document.add(new Field("name", name, Store.YES, Index.ANALYZED));
        indexWriter.addDocument(document);
    }
}
