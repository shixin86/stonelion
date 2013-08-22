
package com.xiaomi.stonelion.lucene;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.FieldDoc;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;

/**
 * 如何自定义排序规则<br>
 * 自己的排序类要继承FieldComparatorSource, FieldComparator
 * 
 * @author shixin
 */
public class DistanceComparatorSource extends FieldComparatorSource {
    private static final long serialVersionUID = 6242946712359125908L;
    private int x;
    private int y;

    public DistanceComparatorSource(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * fieldName:域的名字<br>
     * numHits:min(用户指定的查询数量, 搜索结果的匹配数量)<br>
     * reversed:是否倒序<br>
     */
    @Override
    public FieldComparator<Comparable<Float>> newComparator(String fieldName, int numHits, int sortPos, boolean reversed)
                                                                                                                         throws IOException {
        return new DistanceScoreDocLookupComparator(numHits);
    }

    /**
     * 通过继承FieldComparator提供自定义的排序功能
     * 
     * @author shixin
     */
    private class DistanceScoreDocLookupComparator extends FieldComparator<Comparable<Float>> {
        private int[] xDoc, yDoc;
        private float[] values;
        private float bottom;

        public DistanceScoreDocLookupComparator(int numHits) {
            this.values = new float[numHits];
        }

        /**
         * 每当搜索一个新的segment时，会通过这个方法通知comparator
         */
        @Override
        public void setNextReader(IndexReader indexReader, int docBase) throws IOException {
            // 可以读所有field的域缓存
            xDoc = FieldCache.DEFAULT.getInts(indexReader, "x");
            yDoc = FieldCache.DEFAULT.getInts(indexReader, "y");
        }

        @Override
        public int compare(int slot1, int slot2) {
            if (values[slot1] < values[slot2])
                return -1;
            if (values[slot1] > values[slot2])
                return 1;
            return 0;
        }

        /**
         * 和记分最低的bottom进行比较
         */
        @Override
        public int compareBottom(int doc) throws IOException {
            float dosDistance = getDistance(doc);
            if (bottom < dosDistance)
                return -1;
            if (bottom > dosDistance)
                return 1;
            return 0;
        }

        /**
         * 设置排序最低的bottom
         */
        @Override
        public void setBottom(int slot) {
            this.bottom = values[slot];
        }

        private float getDistance(int doc) {
            int deltax = xDoc[doc] - x;
            int deltay = yDoc[doc] - y;
            return (float) Math.sqrt(deltax * deltax + deltay * deltay);
        }

        /**
         * 将新命中结果拷贝到队列中，每個被匹配到的文檔都會調用這個方法
         */
        @Override
        public void copy(int slot, int doc) throws IOException {
            values[slot] = getDistance(doc);
        }

        /**
         * 这个方法返回comparator计算的中间结果，在最後返回給搜索結果
         */
        @Override
        public Comparable<Float> value(int slot) {
            return new Float(values[slot]);
        }

    }

    private static IndexSearcher indexSearcher;

    private static void setup() throws CorruptIndexException, LockObtainFailedException, IOException {
        Directory directory = new RAMDirectory();

        IndexWriter indexWriter = new IndexWriter(directory, new WhitespaceAnalyzer(), true, IndexWriter.MaxFieldLength.UNLIMITED);
        addPoint(indexWriter, "a1", "r", 4, 4);
        addPoint(indexWriter, "a2", "r", 2, 2);
        addPoint(indexWriter, "a3", "r", 3, 3);
        addPoint(indexWriter, "a4", "r", 1, 4);
        //优化成一个segment
        indexWriter.optimize();
        indexWriter.close();

        IndexReader indexReader = IndexReader.open(directory);
        indexSearcher = new IndexSearcher(indexReader);
    }

    private static void addPoint(IndexWriter indexWriter, String name, String type, int x, int y) throws CorruptIndexException, IOException {
        Document document = new Document();

        Field nameField = new Field("name", name, Store.YES, Index.NOT_ANALYZED_NO_NORMS);
        // 不保存位置信息和项向量信息
        nameField.setOmitTermFreqAndPositions(true);
        document.add(nameField);

        Field typeField = new Field("type", type, Store.YES, Index.NOT_ANALYZED_NO_NORMS);
        typeField.setOmitTermFreqAndPositions(true);
        document.add(typeField);

        document.add(new NumericField("x", Store.YES, true).setIntValue(x));

        document.add(new NumericField("y", Store.YES, true).setIntValue(y));

        indexWriter.addDocument(document);
    }

    public static void main(String[] args) throws CorruptIndexException, LockObtainFailedException, IOException {
        setup();

        Query query = new TermQuery(new Term("type", "r"));
        Filter filter = null;
        Sort sort = new Sort(new SortField(StringUtils.EMPTY, new DistanceComparatorSource(0, 0)));
        // 只查找前三个
        TopFieldDocs result = indexSearcher.search(query, filter, 3, sort);

        System.out.println(result.totalHits);
        System.out.println(result.scoreDocs.length);
        for (ScoreDoc doc : result.scoreDocs) {
            FieldDoc fieldDoc = (FieldDoc)doc;
            // 可以获取Comparable,我们计算出来的距离
            System.out.println("name : " + indexSearcher.doc(doc.doc).get("name") + " distance : " + fieldDoc.fields[0]);
        }
        indexSearcher.close();
    }
}
