
package com.xiaomi.stonelion.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class LuceneUtil {
    /**
     * 通过反射运行所有被RunQuery修饰的方法
     * @param clazz
     */
    public static void run(Class<?> clazz) {
        Method[] methods = clazz.getMethods();
        if (null != methods) {
            for (Method method : methods) {
                if (method.isAnnotationPresent(RunQuery.class)) {
                    try {
                        method.invoke(clazz.newInstance(), new Object[0]);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static String FIELD_ID = "id";
    public static String FIELD_INDEX = "index";
    public static String FIELD_BIRTHDAY = "birthday";
    public static String FIELD_CATEGORY = "category";
    public static String FIELD_CITY = "city";
    public static String FIELD_NAME = "name";
    public static String FIELD_SIGNATURE = "signature";
    public static String FIELD_INTRODUCTION = "introduction";

    /**
     * 添加文档
     * 
     * @param analyzer
     * @return
     * @throws CorruptIndexException
     * @throws LockObtainFailedException
     * @throws IOException
     */
    public static Directory createRAMDirectory(Analyzer analyzer) throws CorruptIndexException, LockObtainFailedException, IOException {
        Directory directory = new RAMDirectory();

        IndexWriter indexWriter = new IndexWriter(directory, analyzer, true, MaxFieldLength.UNLIMITED);
        Document doc1 = new Document();
        Document doc2 = new Document();
        Document doc3 = new Document();

        // --1
        doc1.add(new Field(FIELD_ID, "1", Store.YES, Index.NOT_ANALYZED_NO_NORMS));
        doc1.add(new Field(FIELD_INDEX, "a", Store.YES, Index.NOT_ANALYZED_NO_NORMS));
        doc1.add(new NumericField(FIELD_BIRTHDAY, Store.YES, true).setLongValue(1986));
        doc1.add(new Field(FIELD_CATEGORY, "/usa/black", Store.NO, Index.NOT_ANALYZED_NO_NORMS));
        doc1.add(new Field(FIELD_CITY, "beijing", Store.YES, Index.NOT_ANALYZED_NO_NORMS));
        doc1.add(new Field(FIELD_NAME, "Lebron James", Store.YES, Index.ANALYZED_NO_NORMS));
        doc1.add(new Field(FIELD_SIGNATURE, "The quick brown fox jumped over the lazy dog!", Store.YES, Index.ANALYZED));
        doc1.add(new Field(FIELD_INTRODUCTION, "中欣", Store.YES, Index.ANALYZED));

        // --2
        doc2.add(new Field(FIELD_ID, "2", Store.YES, Index.NOT_ANALYZED_NO_NORMS));
        doc2.add(new Field(FIELD_INDEX, "b", Store.YES, Index.NOT_ANALYZED_NO_NORMS));
        doc2.add(new NumericField(FIELD_BIRTHDAY, Store.YES, true).setLongValue(1988));
        doc2.add(new Field(FIELD_CATEGORY, "/usa/white", Store.NO, Index.NOT_ANALYZED_NO_NORMS));
        doc2.add(new Field(FIELD_CITY, "beijing", Store.YES, Index.NOT_ANALYZED_NO_NORMS));
        doc2.add(new Field(FIELD_NAME, "&KOBE**BYANT\n", Store.YES, Index.ANALYZED_NO_NORMS));
        doc2.add(new Field(FIELD_SIGNATURE, "I am the hero!", Store.YES, Index.ANALYZED));
        doc2.add(new Field(FIELD_INTRODUCTION, "中的欣", Store.YES, Index.ANALYZED));

        // --3
        doc3.add(new Field(FIELD_ID, "3", Store.YES, Index.NOT_ANALYZED_NO_NORMS));
        doc3.add(new Field(FIELD_INDEX, "c", Store.YES, Index.NOT_ANALYZED_NO_NORMS));
        doc3.add(new NumericField(FIELD_BIRTHDAY, Store.YES, true).setLongValue(1990));
        doc3.add(new Field(FIELD_CATEGORY, "/china", Store.NO, Index.NOT_ANALYZED_NO_NORMS));
        doc3.add(new Field(FIELD_CITY, "jilin", Store.YES, Index.NOT_ANALYZED_NO_NORMS));
        doc3.add(new Field(FIELD_NAME, "石欣愛北的的京城", Store.YES, Index.ANALYZED_NO_NORMS));
        doc3.add(new Field(FIELD_SIGNATURE, "关山口职业技术学院", Store.YES, Index.ANALYZED));
        doc3.add(new Field(FIELD_INTRODUCTION, "我爱中的天欣气", Store.YES, Index.ANALYZED));

        indexWriter.addDocument(doc1);
        indexWriter.addDocument(doc2);
        indexWriter.addDocument(doc3);
        indexWriter.close();

        return directory;
    }

    /**
     * 查看分析器生成的语汇单元细节
     * 
     * @param analyzer
     * @param text
     * @throws IOException
     */
    public static void displayTokens(Analyzer analyzer, String text) throws IOException {
        // 语汇单元流
        TokenStream tokenStream = analyzer.tokenStream("default", new StringReader(text));

        // 获取语汇单元的属性
        TermAttribute termAttribute = tokenStream.addAttribute(TermAttribute.class);
        // 位置增量，在短语查询的时候，同义词查询的时候有作用
        PositionIncrementAttribute positionIncrementAttribute = tokenStream.addAttribute(PositionIncrementAttribute.class);
        // 偏移量，高亮查询匹配结果有用
        OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
        // 语汇单元类型，普通是word,还有email等
        TypeAttribute typeAttribute = tokenStream.addAttribute(TypeAttribute.class);

        int position = 0;
        while (tokenStream.incrementToken()) {
            // 计算位置信息
            int increment = positionIncrementAttribute.getPositionIncrement();
            if (increment > 0) {
                position = position + increment;
            }
            // 打印所有语汇单元的细节信息
            System.out.println("position : " + position + " [" + termAttribute.term() + ":" + offsetAttribute.startOffset() + "->"
                    + offsetAttribute.endOffset() + ":" + typeAttribute.type() + "]");
        }
    }

    /**
     * 查看分词后的语汇单元细节，只打印分词结果
     * 
     * @param analyzer
     * @param text
     * @throws IOException
     */
    public static void displaySimpleTokens(Analyzer analyzer, String text) throws IOException {
        TokenStream tokenStream = analyzer.tokenStream("default", new StringReader(text));
        TermAttribute termAttribute = tokenStream.addAttribute(TermAttribute.class);
        while (tokenStream.incrementToken()) {
            System.out.print(termAttribute.term() + ",");
        }
        System.out.println();
    }

}
