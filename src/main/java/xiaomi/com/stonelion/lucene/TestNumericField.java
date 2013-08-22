
package xiaomi.com.stonelion.lucene;

import com.chenlb.mmseg4j.analysis.SimpleAnalyzer;
import com.xiaomi.stonelion.lucene.IndexSearcherManager;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;

public class TestNumericField {
    public static void main(String[] args) throws CorruptIndexException, LockObtainFailedException, IOException, InterruptedException {
        Directory directory = new RAMDirectory();
        IndexWriter indexWriter = new IndexWriter(directory, new SimpleAnalyzer(), true, MaxFieldLength.UNLIMITED);
        IndexSearcherManager manager = new IndexSearcherManager(indexWriter);

        Document doc1 = new Document();
        doc1.add(new Field("id", "1", Store.YES, Index.NOT_ANALYZED_NO_NORMS));
        doc1.add(new NumericField("id1", Store.YES, true).setLongValue(1l));
        doc1.add(new NumericField("id2", Store.YES, true).setLongValue(2l));
        indexWriter.addDocument(doc1);
        indexWriter.commit();
        manager.tryToReopen();

        IndexSearcher indexSearcher = manager.getIndexSearcher();
        TopDocs topDocs = indexSearcher.search(new TermQuery(new Term("id", "1")), 1);
        Document oDoc = indexSearcher.doc(topDocs.scoreDocs[0].doc);

        Field f1 = oDoc.getField("id1");
        System.out.println(f1.stringValue());
        Field f2 = oDoc.getField("id2");
        System.out.println(f2.stringValue());
        
        
        /////
        oDoc.removeField("id2");
        oDoc.add(new NumericField("id2", Store.YES, true).setLongValue(3l));
        
        Fieldable fieldable = oDoc.getFieldable("id2");
        System.out.println(((NumericField)fieldable).stringValue());
        indexWriter.updateDocument(new Term("id", "1"), oDoc);
        indexWriter.commit();
        manager.tryToReopen();
        
        ///
        indexSearcher = manager.getIndexSearcher();
        topDocs = indexSearcher.search(NumericRangeQuery.newLongRange("id2", 2l, 3l, true, true), 1);
        oDoc = indexSearcher.doc(topDocs.scoreDocs[0].doc);

        f1 = oDoc.getField("id1");
        System.out.println(f1.stringValue());
        f2 = oDoc.getField("id2");
        System.out.println(f2.stringValue());
    }
}
