
package com.xiaomi.stonelion.lucene;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.OpenBitSet;

import java.io.IOException;

/**
 * 如何创建自定义的过滤器<br>
 * 如果用于过滤的数据已经在索引中，则不需要自定义过滤器。但是如果过滤数据在索引之外，则可以使用自定义的过滤器。
 * 
 * TODO：
 * CachingWrapperFilter<br>
 * FilterQuery<br>
 * 
 * @author shixin
 */
public class CustomFilter extends Filter {

    /**
     * DocIdSet是什么呢? 各Bit位置和docId是对应的，值为1的bit位表示文档可以被搜索到，0则相反。
     */
    @Override
    public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
        // TODO Auto-generated method stub
        
        // DocIdSet的一个子类
        OpenBitSet bits = new OpenBitSet(reader.maxDoc());

        // 这有效的id集合可以从外部程序获取
        String[] validIds = new String[0];

        // 如果索引中的文档号在validIds集合中则可以被搜索到，反之搜索不到
        int[] docs = new int[1];
        int[] freqs = new int[1];
        for (String validId : validIds) {
            TermDocs termDocs = reader.termDocs(new Term("id", validId));
            int count = termDocs.read(docs, freqs);
            if (1 == count) {
                bits.set(docs[0]);
            }
        }

        return bits;
    }
    
    public static void main(String[] args) {
        // FilteredQuery可以把filter和query结合成一个query
        Filter filter = new CustomFilter();
        Query query = new FilteredQuery(new MatchAllDocsQuery(), filter);
    }
}
