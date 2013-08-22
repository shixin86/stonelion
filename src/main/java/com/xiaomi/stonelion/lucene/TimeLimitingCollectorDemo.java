
package com.xiaomi.stonelion.lucene;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TimeLimitingCollector;
import org.apache.lucene.search.TimeLimitingCollector.TimeExceededException;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Counter;

import java.io.IOException;

/**
 * 停止较慢的搜索
 * 
 * @author shixin
 */
public class TimeLimitingCollectorDemo {
    public static void main(String[] args) throws CorruptIndexException, IOException {
        Directory directory = new RAMDirectory();
        IndexSearcher indexSearcher = new IndexSearcher(directory, true);

        // 默认lucene使用这个collector收集分数比较高的doc, 这里查询100个
        TopScoreDocCollector topScoreDocCollector = TopScoreDocCollector.create(100, false);
        // 包装一个
        TimeLimitingCollector timeLimitingCollector = new TimeLimitingCollector(topScoreDocCollector, new Counter() {

            @Override
            public long get() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public long addAndGet(long arg0) {
                // TODO Auto-generated method stub
                return 0;
            }
        }, 300);

        try {
            indexSearcher.search(new TermQuery(new Term("", "")), timeLimitingCollector);
        } catch (TimeExceededException e) {
            // 如果超时了怎么办
        }

        indexSearcher.close();
        directory.close();
    }
}
