package xiaomi.com.stonelion.lucene;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;

import java.util.ArrayList;
import java.util.List;

public class TestQuery {
    public static void main(String[] args) throws Exception {
        _test_termQuery();
        _test_PrefixQuery();
        _test_NumericRangeQuery();
        _test_PhraseQuery();
        _test_WildcardQuery();
        _test_FuzzyQuery();
    }
    
    /**
     * 单项查询
     * @throws Exception
     */
    private static void _test_termQuery() throws Exception{
        Directory directory = LuceneUtil.genRAMDirectory(new WhitespaceAnalyzer(), genUsers());
        Query query = LuceneUtil.getTermQuery(User.KEY_NAME, "ja");
        LuceneUtil.displayResult(directory, query, null);
    }
    
    /**
     * 数字的range query
     * @throws Exception
     */
    private static void _test_NumericRangeQuery() throws Exception{
        Directory directory = LuceneUtil.genRAMDirectory(new WhitespaceAnalyzer(), genUsers());
        Query query = LuceneUtil.getLongNumericRangeQuery(User.KEY_BIRTHDAY, 21, 23);
        LuceneUtil.displayResult(directory, query, null);
    }
    
    /**
     * 前缀查询
     * @throws Exception
     */
    private static void _test_PrefixQuery() throws Exception{
        Directory directory = LuceneUtil.genRAMDirectory(new WhitespaceAnalyzer(), genUsers());
        Query query = LuceneUtil.getPrefixQuery(User.KEY_NAME, "ja");
        LuceneUtil.displayResult(directory, query, null);
    }
    
    /**
     * 短语查询
     * @throws Exception
     */
    private static void _test_PhraseQuery() throws Exception{
        Directory directory = LuceneUtil.genRAMDirectory(new WhitespaceAnalyzer(), genUsers());
        Query query = LuceneUtil.getPhraseQuery(User.KEY_NAME, new String[]{"lebron", "james"}, 2);
        LuceneUtil.displayResult(directory, query, null);
    }
    
    /**
     * ? 0或1
     * * 0或多
     * @throws Exception
     */
    private static void _test_WildcardQuery() throws Exception{
        Directory directory = LuceneUtil.genRAMDirectory(new WhitespaceAnalyzer(), genUsers());
        Query query = LuceneUtil.getWildcardQuery(User.KEY_NAME, "?ja*");
        LuceneUtil.displayResult(directory, query, null);
    }
    
    /**
     * 相似的项
     * @throws Exception
     */
    private static void _test_FuzzyQuery() throws Exception{
        Directory directory = LuceneUtil.genRAMDirectory(new WhitespaceAnalyzer(), genUsers());
        Query query = LuceneUtil.getFuzzyQuery(User.KEY_NAME, "jome");
        LuceneUtil.displayResult(directory, query, null);
    }
    
    private static List<User> genUsers() {
        List<User> users = new ArrayList<User>();
        users.add(new User(1, "jame", 21));
        users.add(new User(2, "james", 22));
        users.add(new User(3, "jordan", 23));
        users.add(new User(4, "lebron james", 24));
        users.add(new User(5, "answer", 25));
        users.add(new User(6, "ajames", 26));
        users.add(new User(7, "lebron a b james", 24));
        return users;
    }
    
}
