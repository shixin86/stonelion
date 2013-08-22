
package xiaomi.com.stonelion.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;
import java.io.StringReader;

public class ShixinAnalyzerUtils {
    // 打印语汇单元流
    public static void displayTokens(Analyzer analyzer, String text) throws IOException {
        displayTokens(analyzer.tokenStream("content", new StringReader(text)));
    }

    public static void displayTokens(TokenStream tokenStream) throws IOException {
        TermAttribute term = tokenStream.addAttribute(TermAttribute.class);
        while (tokenStream.incrementToken()) {
            System.out.println("[" + term.term() + "]");
        }
    }

    // 打印相信的语汇单元流
    public static void displayTokensWithFullDetails(Analyzer analyzer, String text) throws IOException {
        TokenStream tokenStream = analyzer.tokenStream("content", new StringReader(text));

        // 获取所有有用属性
        TermAttribute termAttribute = tokenStream.addAttribute(TermAttribute.class);
        PositionIncrementAttribute positionIncrementAttribute = tokenStream.addAttribute(PositionIncrementAttribute.class);
        OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
        TypeAttribute typeAttribute = tokenStream.addAttribute(TypeAttribute.class);

        int position = 0;
        // 递归处理所有语汇单元
        while (tokenStream.incrementToken()) {
            //计算位置信息并打印
            int increment = positionIncrementAttribute.getPositionIncrement();
            if (increment > 0) {
                position = position + increment;
                System.out.println();
                System.out.print(position + ": ");
            }
            
            //打印所有语汇单元的细节信息
            System.out.println("[" + termAttribute.term() + ":" + offsetAttribute.startOffset() + "->" + offsetAttribute.endOffset() + ":"
                    + typeAttribute.type() + "]");
        }
        System.out.println();
    }
    
    public static void main(String[] args) throws IOException {
        ShixinAnalyzerUtils.displayTokensWithFullDetails(new SimpleAnalyzer(), "The quick brown fox....");
    }
} 
