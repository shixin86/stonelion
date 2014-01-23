
package com.xiaomi.stonelion.lucene.old;

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
import org.apache.lucene.search.*;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NearRealTimeSearch {
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_CITY = "city";

    public static void main(String[] args) throws CorruptIndexException, LockObtainFailedException, IOException {
//        simpleNearRealTimeSearch();
        simpleNearRealTimeSearch2_updateIndex();

    }
    
    //结果，在更新的时候的评分规则，注意maxDocs，很有意思
/*    3
    0.71231794 = (MATCH) fieldWeight(city:bj in 2), product of:
      1.0 = tf(termFreq(city:bj)=1)
      0.71231794 = idf(docFreq=3, maxDocs=3)
      1.0 = fieldNorm(field=city, doc=2)

    1
    0.4451987 = (MATCH) fieldWeight(city:bj in 0), product of:
      1.0 = tf(termFreq(city:bj)=1)
      0.71231794 = idf(docFreq=3, maxDocs=3)
      0.625 = fieldNorm(field=city, doc=0)

    2
    0.35615897 = (MATCH) fieldWeight(city:bj in 1), product of:
      1.0 = tf(termFreq(city:bj)=1)
      0.71231794 = idf(docFreq=3, maxDocs=3)
      0.5 = fieldNorm(field=city, doc=1)*/
    /**
     * 
     * @throws org.apache.lucene.index.CorruptIndexException
     * @throws org.apache.lucene.store.LockObtainFailedException
     * @throws java.io.IOException
     */
    private static void simpleNearRealTimeSearch2_updateIndex() throws CorruptIndexException, LockObtainFailedException, IOException {
        Analyzer analyzer = new SimpleAnalyzer();
        IndexWriter indexWriter = new IndexWriter(new RAMDirectory(), analyzer, true, MaxFieldLength.UNLIMITED);

        List<User> users = new ArrayList<User>();
        users.add(new User(1, null, null));
        users.add(new User(2, null, null));
        users.add(new User(3, null, null));
        buildIndex(indexWriter, users);

        // get IndexSearcher
        IndexReader indexReader = indexWriter.getReader();
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        // 更新索引
        updateUserIndex(indexWriter, indexSearcher, 1, "tom", "newyork bj");
        updateUserIndex(indexWriter, indexSearcher, 2, "james", "a b c bj");
        updateUserIndex(indexWriter, indexSearcher, 3, "shixin", "bj");

        // 搜索,没有
        Query query = new TermQuery(new Term(KEY_CITY, "bj"));
        TopDocs topDocs = indexSearcher.search(query, 10);
        printResults(indexSearcher, query, topDocs);

        indexWriter.commit();
        indexWriter.optimize();
        IndexReader newIndexReader = indexReader.reopen();
        if (newIndexReader != indexReader) {
            indexReader.close();
            indexSearcher = new IndexSearcher(newIndexReader);
        }
        // 搜索
        query = new TermQuery(new Term(KEY_CITY, "bj"));
        topDocs = indexSearcher.search(query, 10);
        printResults(indexSearcher, query, topDocs);
    }

    private static void updateUserIndex(IndexWriter indexWriter, IndexSearcher indexSearcher, long userId, String nickname, String city)
                                                                                                                                        throws IOException {
        TopDocs topDocs = indexSearcher.search(new TermQuery(new Term(KEY_ID, String.valueOf(userId))), 1);
        Document document = indexSearcher.doc(topDocs.scoreDocs[0].doc);
        if (null != nickname) {
            document.add(new Field(KEY_NAME, nickname, Store.YES, Index.ANALYZED));
        }
        if (null != city) {
            document.add(new Field(KEY_CITY, city, Store.YES, Index.ANALYZED));
        }
        indexWriter.updateDocument(new Term(KEY_ID, String.valueOf(userId)), document);
    }

    /**
     * 增加和删除document，在近实时搜索的时候
     *
     * @throws org.apache.lucene.index.CorruptIndexException
     * @throws org.apache.lucene.store.LockObtainFailedException
     * @throws java.io.IOException
     */
    private static void simpleNearRealTimeSearch() throws CorruptIndexException, LockObtainFailedException, IOException {
        Analyzer analyzer = new SimpleAnalyzer();
        IndexWriter indexWriter = new IndexWriter(new RAMDirectory(), analyzer, true, MaxFieldLength.UNLIMITED);

        List<User> users = new ArrayList<User>();
        users.add(new User(1, "tom", "newyork"));
        users.add(new User(2, "james", "beijing"));
        users.add(new User(3, "shixin", "changchun"));
        buildIndex(indexWriter, users);

        // get IndexSearcher
        IndexReader indexReader = indexWriter.getReader();
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        System.out.println("1----------------------------------");
        Query query = new TermQuery(new Term(KEY_NAME, "tom"));
        TopDocs topDocs = indexSearcher.search(query, 1);
        printResults(indexSearcher, query, topDocs);

        // delete
        indexWriter.deleteDocuments(new Term(KEY_ID, "1"));
        indexWriter.commit();

        // add
        users = new ArrayList<User>();
        users.add(new User(4, "xiaoming", "qingdao"));
        buildIndex(indexWriter, users);

        // get new indexSearcher
        IndexReader newIndexReader = indexReader.reopen();
        if (newIndexReader != indexReader) {
            indexReader.close();
            indexSearcher = new IndexSearcher(newIndexReader);
        }

        System.out.println("2----------------------------------");
        query = new TermQuery(new Term(KEY_NAME, "tom"));
        topDocs = indexSearcher.search(query, 10);
        printResults(indexSearcher, query, topDocs);

        System.out.println("3----------------------------------");
        query = new TermQuery(new Term(KEY_NAME, "xiaoming"));
        topDocs = indexSearcher.search(query, 10);
        printResults(indexSearcher, query, topDocs);

        indexSearcher.close();
        indexWriter.close();
    }

    private static void printResults(IndexSearcher indexSearcher, Query query, TopDocs topDocs) throws CorruptIndexException, IOException {
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document document = indexSearcher.doc(scoreDoc.doc);
            System.out.println(document.get(KEY_ID));

            Explanation explanation = indexSearcher.explain(query, scoreDoc.doc);
            System.out.println(explanation.toString());
        }
    }

    private static void buildIndex(IndexWriter indexWriter, List<User> users) throws CorruptIndexException, IOException {
        for (User user : users) {
            Document document = new Document();
            if (0 == user.userId) {
                continue;
            } else {
                document.add(new Field(KEY_ID, String.valueOf(user.userId), Store.YES, Index.NOT_ANALYZED));
            }

            if (null != user.nickname) {
                document.add(new Field(KEY_NAME, user.nickname, Store.YES, Index.ANALYZED));
            }
            if (null != user.city) {
                document.add(new Field(KEY_CITY, user.city, Store.YES, Index.ANALYZED));
            }
            indexWriter.addDocument(document);
        }
    }

    private static class User {
        long userId;
        String nickname;
        String city;

        public User(long userId, String nickname, String city) {
            this.userId = userId;
            this.nickname = nickname;
            this.city = city;
        }
    }

}
