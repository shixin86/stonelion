
package xiaomi.com.stonelion.lucene;

import com.chenlb.mmseg4j.analysis.ComplexAnalyzer;

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
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    
    public static void main(String[] args) throws Exception {
//        testChinese();
        JSONArray j = new JSONArray();
    }

    private static void testChinese() throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
        Analyzer analyzer = new ComplexAnalyzer();
        IndexWriter indexWriter = new IndexWriter(new RAMDirectory(), analyzer, true, MaxFieldLength.UNLIMITED);

        Document document = new Document();
        document.add(new Field("name", "a不是干嘛哈哈", Store.YES, Index.ANALYZED));
        indexWriter.addDocument(document);

        document = new Document();
        document.add(new Field("name", "小米科技有限公司", Store.YES, Index.ANALYZED));
        indexWriter.addDocument(document);

        indexWriter.optimize();
        indexWriter.commit();

        IndexSearcherManager indexSearcherManager = new IndexSearcherManager(indexWriter);
        IndexSearcher indexSearcher = null;
        try {
            indexSearcher = indexSearcherManager.getIndexSearcher();
            QueryParser queryParser = new QueryParser(Version.LUCENE_30, "name", analyzer);
            TopDocs topDocs = indexSearcher.search(queryParser.parse("不是干嘛哈哈"), 10);
            System.out.println(topDocs.totalHits);
        } finally {
            indexSearcherManager.releaseIndexSearcher(indexSearcher);
        }
    }

    private static void testFieldCache() throws CorruptIndexException, LockObtainFailedException, IOException, InterruptedException {
        // 添加索引
        IndexWriter indexWriter = new IndexWriter(new RAMDirectory(), new SimpleAnalyzer(), true, MaxFieldLength.UNLIMITED);
        for (Document document : getDocuments()) {
            indexWriter.addDocument(document);
        }
        indexWriter.optimize();
        indexWriter.commit();
        IndexSearcherManager indexSearcherManager = new IndexSearcherManager(indexWriter);

        // 查看域缓存
        String[] names = FieldCache.DEFAULT.getStrings(indexSearcherManager.getIndexSearcher().getIndexReader(), "city");
        System.out.println(names.length);
        System.out.println(names[0]);
        System.out.println(names[1]);

        // 修改document
        indexWriter.deleteDocuments(new Term("name", "max"));
        Document document1 = new Document();
        document1.add(new Field("name", "max", Store.YES, Index.NOT_ANALYZED));
        document1.add(new Field("city", "bjjj", Store.YES, Index.NOT_ANALYZED));
        indexWriter.updateDocument(new Term("name", "max"), document1);
        indexWriter.commit();
        indexWriter.optimize();

        indexSearcherManager.tryToReopen();

        names = FieldCache.DEFAULT.getStrings(indexSearcherManager.getIndexSearcher().getIndexReader(), "city");
        System.out.println(names.length);
        System.out.println(names[0]);
        System.out.println(names[1]);
        System.out.println(names[2]);
    }

    private static void testIndexSearcherManager() throws CorruptIndexException, LockObtainFailedException, IOException {
        Directory directory = new RAMDirectory();
        final IndexWriter indexWriter = new IndexWriter(directory, new SimpleAnalyzer(), true, MaxFieldLength.UNLIMITED);
        for (Document document : getDocuments()) {
            indexWriter.addDocument(document);
        }

        final IndexSearcherManager indexSearcherManager = new IndexSearcherManager(indexWriter);

        ExecutorService threadPool = Executors.newCachedThreadPool();
        for (int i = 0; i < 5; i++) {
            threadPool.execute(new Runnable() {
                public void run() {
                    IndexSearcher indexSearch = indexSearcherManager.getIndexSearcher();
                    try {

                        Query query = new TermQuery(new Term("name", "max"));
                        TopDocs result = indexSearch.search(query, 1);
                        System.out.println("find " + result.totalHits + " results");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            indexSearcherManager.releaseIndexSearcher(indexSearch);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        threadPool.shutdown();
    }

    private static class IndexSearcherManager {
        private IndexSearcher currentIndexSearcher;
        private IndexWriter indexWriter;
        private boolean reopening;

        public IndexSearcherManager(IndexWriter indexWriter) throws IOException {
            this.indexWriter = indexWriter;
            this.currentIndexSearcher = new IndexSearcher(indexWriter.getReader());
        }

        public synchronized IndexSearcher getIndexSearcher() {
            currentIndexSearcher.getIndexReader().incRef();
            return currentIndexSearcher;
        }

        public synchronized void releaseIndexSearcher(IndexSearcher indexSearcher) throws IOException {
            indexSearcher.getIndexReader().decRef();
        }

        private synchronized void swapIndexeSearcher(IndexSearcher newIndexSearcher) throws IOException {
            releaseIndexSearcher(currentIndexSearcher);
            currentIndexSearcher = newIndexSearcher;
        }

        public void close() throws IOException {
            swapIndexeSearcher(null);
        }

        private synchronized void startReopen() throws InterruptedException {
            while (reopening) {
                wait();
            }
            reopening = true;
        }

        private synchronized void doneReopen() {
            reopening = false;
            notifyAll();
        }

        public void tryToReopen() throws InterruptedException, CorruptIndexException, IOException {
            startReopen();
            try {
                IndexSearcher indexSearcher = getIndexSearcher();
                try {
                    IndexReader newIndexReader = currentIndexSearcher.getIndexReader().reopen();
                    if (newIndexReader != currentIndexSearcher.getIndexReader()) {
                        IndexSearcher newIndexSearcher = new IndexSearcher(newIndexReader);
                        swapIndexeSearcher(newIndexSearcher);
                    }
                } finally {
                    releaseIndexSearcher(indexSearcher);
                }
            } finally {
                doneReopen();
            }
        }
    }

    private static void testUseMultiIndexReader() throws CorruptIndexException, LockObtainFailedException, IOException,
                                                 InterruptedException {
        Directory directory = new RAMDirectory();
        final IndexWriter indexWriter = new IndexWriter(directory, new SimpleAnalyzer(), true, MaxFieldLength.UNLIMITED);

        for (Document document : getDocuments()) {
            indexWriter.addDocument(document);
        }

        ExecutorService threadPool = Executors.newCachedThreadPool();
        for (int i = 0; i < 5; i++) {
            threadPool.execute(new Runnable() {
                public void run() {
                    try {
                        IndexReader indexReader = indexWriter.getReader();
                        IndexSearcher indexSearch = new IndexSearcher(indexReader);

                        Query query = new TermQuery(new Term("name", "max"));
                        TopDocs result = indexSearch.search(query, 1);
                        System.out.println("find " + result.totalHits + " results");

                        indexReader.close();
                        indexSearch.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        threadPool.shutdown();
    }

    private static void testSimpleSearch() throws CorruptIndexException, LockObtainFailedException, IOException {
        Directory directory = new RAMDirectory();
        IndexWriter indexWriter = new IndexWriter(directory, new SimpleAnalyzer(), true, MaxFieldLength.UNLIMITED);

        for (Document document : getDocuments()) {
            indexWriter.addDocument(document);
        }
        indexWriter.close();

        IndexReader indexReader = IndexReader.open(directory);
        IndexSearcher indexSearch = new IndexSearcher(indexReader);

        Query query = new TermQuery(new Term("name", "max"));

        TopDocs result = indexSearch.search(query, 10);

        System.out.println("find " + result.totalHits + " results");
        for (ScoreDoc doc : result.scoreDocs) {
            int docId = doc.doc;
            Document document = indexSearch.doc(docId);
            System.out.println("result : " + document.get("name"));
        }

        indexReader.close();
        indexSearch.close();
        directory.close();
    }


    private static List<Document> getDocuments() {
        List<Document> documents = new ArrayList<Document>();

        Document document1 = new Document();
        document1.add(new Field("name", "max", Store.YES, Index.NOT_ANALYZED));
        document1.add(new Field("city", "bj", Store.YES, Index.NOT_ANALYZED));
        documents.add(document1);

        Document document2 = new Document();
        document2.add(new Field("name", "jane", Store.YES, Index.NOT_ANALYZED));
        document2.add(new Field("city", "bj", Store.YES, Index.NOT_ANALYZED));
        documents.add(document2);

        return documents;
    }
}
