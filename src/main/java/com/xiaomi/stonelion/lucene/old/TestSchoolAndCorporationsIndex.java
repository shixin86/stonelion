
package com.xiaomi.stonelion.lucene.old;

import com.chenlb.mmseg4j.analysis.MaxWordAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;

public class TestSchoolAndCorporationsIndex {
    public static void main(String[] args) throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
        Directory schoolDirectory = FSDirectory.open(new File("/home/shixin/workspace/stonelion/index/schools_index"));
        Directory corporationDirectory = FSDirectory.open(new File("/home/shixin/workspace/stonelion/index/corporations_index"));
        
        Analyzer analyzer = new MaxWordAnalyzer(new File("/home/shixin/workspace/stonelion/index/dict"));
        IndexWriter schoolIndexWriter = new IndexWriter(schoolDirectory, analyzer, false, MaxFieldLength.UNLIMITED);
        IndexWriter corporationIndexWriter = new IndexWriter(corporationDirectory, analyzer, false, MaxFieldLength.UNLIMITED);

        IndexSearcher schoolIndexSearcher = new IndexSearcher(schoolIndexWriter.getReader());
        
        //查公司
        IndexSearcher corporationIndexSearcher = new IndexSearcher(corporationIndexWriter.getReader());
        QueryParser corporationQueryParser = new QueryParser(Version.LUCENE_30, "co", analyzer);
        Query query = corporationQueryParser.parse("爱立信");
        TopDocs topDocs = corporationIndexSearcher.search(query, 100);
        System.out.println(topDocs.totalHits);
    }
}
