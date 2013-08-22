
package xiaomi.com.stonelion.lucene;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestSort {
    public static void main(String[] args) throws Exception {
        _test_sort_relevance();
        _test_sort_indexorder();
        _test_sort_field();
        _test_sort_field_reverse();
        _test_sort_mutiple_field();
    }

    
    /**
     * TODO ： 使用非默认的locale方式进行排序
     */
    
    
    /**
     * 按照评分进行排序，但是性能不高，最好使用默认方法
     * @throws CorruptIndexException
     * @throws LockObtainFailedException
     * @throws IOException
     */
    private static void _test_sort_relevance() throws CorruptIndexException, LockObtainFailedException, IOException {
        _test_sort(Sort.RELEVANCE);
    }
    
    
    /**
     * 按索引顺序排序
     * @throws CorruptIndexException
     * @throws LockObtainFailedException
     * @throws IOException
     */
    private static void _test_sort_indexorder() throws CorruptIndexException, LockObtainFailedException, IOException{
        _test_sort(Sort.INDEXORDER);
    }

    
    /**
     * 根据某个列进行排序
     * 应该是 Index.NOT_ANALYZED; Index.NOT_ANALYZED_NO_NORMS; 但是不用好像也可以
     * @throws CorruptIndexException
     * @throws LockObtainFailedException
     * @throws IOException
     */
    private static void _test_sort_field() throws CorruptIndexException, LockObtainFailedException, IOException{
        Sort sort = new Sort(new SortField(User.KEY_NAME, SortField.STRING));
        _test_sort(sort);
    }
    
    
    /**
     * 根据某个列进行排序，倒序排
     * @throws CorruptIndexException
     * @throws LockObtainFailedException
     * @throws IOException
     */
    private static void _test_sort_field_reverse() throws CorruptIndexException, LockObtainFailedException, IOException{
        Sort sort = new Sort(new SortField(User.KEY_BIRTHDAY, SortField.LONG, true));
        _test_sort(sort);
    }
    
    
    /**
     * 通过多个域进行排序
     * 
     * 先按评分排，再按生日排
     * @throws CorruptIndexException
     * @throws LockObtainFailedException
     * @throws IOException
     */
    private static void _test_sort_mutiple_field() throws CorruptIndexException, LockObtainFailedException, IOException{
        SortField[] sortFields = new SortField[]{
                SortField.FIELD_SCORE, new SortField(User.KEY_BIRTHDAY, SortField.LONG, true)
        };
        Sort sort = new Sort(sortFields);
        _test_sort(sort);
    }
    
    private static void _test_sort(Sort sort) throws CorruptIndexException, LockObtainFailedException, IOException{
        Directory directory = LuceneUtil.genRAMDirectory(new WhitespaceAnalyzer(), genUsers());

        // query是命中，sort是对命中结果排序
        BooleanQuery booleanQuery = new BooleanQuery();
        booleanQuery.add(LuceneUtil.getMatchAllDocsQuery(), Occur.SHOULD);
        booleanQuery.add(LuceneUtil.getTermQuery(User.KEY_NAME, "james"), Occur.SHOULD);

        LuceneUtil.displayResult(directory, booleanQuery, sort);
    }
    
    private static List<User> genUsers() {
        List<User> users = new ArrayList<User>();
        users.add(new User(1, "jame", System.currentTimeMillis() - day(1)));
        users.add(new User(2, "james", System.currentTimeMillis() - day(2)));
        users.add(new User(3, "jordan", System.currentTimeMillis() - day(3)));
        users.add(new User(4, "lebron james", System.currentTimeMillis() - day(4)));
        users.add(new User(5, "answer", System.currentTimeMillis() - day(5)));
        return users;
    }

    private static long day(int day) {
        return 24 * 60 * 60 * 1000 * day;
    }
}
