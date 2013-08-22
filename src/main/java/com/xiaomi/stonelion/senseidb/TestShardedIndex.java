
package com.xiaomi.stonelion.senseidb;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestShardedIndex {
    public static void main(String[] args) throws IOException {
        //getTotalDocNum();
        queryUser();
        // hasDuplicatedUserid();
        
    }

    private static void getTotalDocNum() throws IOException {
        int totalNum = 0;
        String indexDirPrefix = "/tmp/sensei-miliaouser-index/index/shard";
        int indexDirCount = 80;
        for (int i = 0; i < indexDirCount; i++) {
            Directory d = FSDirectory.open(new File(indexDirPrefix + i));
            IndexSearcher indexSearcher = new IndexSearcher(d);
            totalNum = totalNum + indexSearcher.getIndexReader().numDocs();
        }
        System.out.println(totalNum);
    }

    private static void queryUser() throws IOException {
        String indexDirPrefix = "/tmp/sensei-miliaouser-index/index/shard";
        int indexDirCount = 66;
        for (int i = 65; i < indexDirCount; i++) {
            Directory d = FSDirectory.open(new File(indexDirPrefix + i));
            IndexSearcher indexSearcher = new IndexSearcher(d);
            Query query = new TermQuery(new Term("idstr", "40319265"));
            TopDocs result = indexSearcher.search(query, 10);
            if (result.totalHits > 0) {
                System.out.println(40319265 % 80);
                System.out.println(i);
                System.out.println(result.totalHits);
            }
            for(ScoreDoc doc : result.scoreDocs){
                System.out.println(indexSearcher.doc(doc.doc));
            }
        }
    }

    private static void getTotalUserNum() {
        int[] users = new int[] {
            9898284, 9898376, 9898286, 9898251, 9900600, 9898270, 9898239, 9898296, 9898310, 9898262
        };

        int totalNum = 0;

        for (int i : users) {
            totalNum = totalNum + i;
        }
        System.out.println(totalNum);
    }

    private static void hasDuplicatedUserid() throws IOException {
        File f = new File("");
        List<Long> userIds = new ArrayList<Long>();
        BufferedReader reader = new BufferedReader(new FileReader(f));
        String line = null;
        while ((line = reader.readLine()) != null) {
            String[] fields = line.split("\t");
            Long userId = Long.parseLong(fields[0]);
            if (userIds.contains(userId)) {
                System.out.println("------------------------------" + userId);
            } else {
                userIds.add(userId);
            }
        }
        System.out.println(userIds.size());
    }
}
