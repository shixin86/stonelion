
package com.xiaomi.stonelion.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

public class QueryParserDemo {
    public static void main(String[] args) {
        LuceneUtil.run(QueryParserDemo.class);
    }

    /**
     * 列举常见的QueryParser的parse结果<br>
     * 练习查询表达式的写法<br>
     * 
     * @throws ParseException
     */
    @RunQuery
    public void printQueryPaserResult() throws ParseException {
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
        QueryParser queryParser = new QueryParser(Version.LUCENE_30, "content", analyzer);

        displayQueryType(queryParser, "java");

        displayQueryType(queryParser, "java lucene");
        displayQueryType(queryParser, "java OR lucene");

        displayQueryType(queryParser, "+java +lucene");
        displayQueryType(queryParser, "java AND lucene");

        displayQueryType(queryParser, "+java -lucene");
        displayQueryType(queryParser, "java AND NOT lucene");

        displayQueryType(queryParser, "+name:java -type:lucene");
        displayQueryType(queryParser, "name:java AND NOT type:lucene");

        displayQueryType(queryParser, "(name:java OR type:lucene) AND school:bit");
        displayQueryType(queryParser, "+(name:java type:lucene) +school:bit");

        displayQueryType(queryParser, "shi xin");
        displayQueryType(queryParser, "\"shi xin\"");
        displayQueryType(queryParser, "\"shi xin\"~5");

        displayQueryType(queryParser, "name:java~");
        displayQueryType(queryParser, "name:java*");
        displayQueryType(queryParser, "name:[1 TO 2]");
        displayQueryType(queryParser, "date:[1/1/09 TO 2/1/09]");

        analyzer.close();
    }

    public void displayQueryType(QueryParser queryParser, String queryLanguage) throws ParseException {
        Query query = queryParser.parse(queryLanguage);
        System.out.println("QueryLanguage is : " + queryLanguage);
        System.out.println("Query.toString : " + query.toString());
        System.out.println("Query class : " + query.getClass());
        System.out.println("-------------------------------------");
    }
}
