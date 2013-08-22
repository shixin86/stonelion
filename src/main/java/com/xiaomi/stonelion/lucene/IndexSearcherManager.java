
package com.xiaomi.stonelion.lucene;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;

import java.io.IOException;

/**
 * @author shixin
 * @date 2012-7-2
 */
public class IndexSearcherManager {
    private IndexSearcher currentIndexSearcher;
    private boolean reopening;

    public IndexSearcherManager(IndexWriter indexWriter) throws IOException {
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
