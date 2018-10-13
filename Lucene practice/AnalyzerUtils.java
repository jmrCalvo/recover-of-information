/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package luceneej1;


import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.UAX29URLEmailAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;


import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;


import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardTokenizer;
/**
 *
 * @author jhg
 */



public class AnalyzerUtils {

    public static final Analyzer [] analizadores = {
            new WhitespaceAnalyzer(Version.LUCENE_43),
            new SimpleAnalyzer(Version.LUCENE_43),
            new StopAnalyzer(Version.LUCENE_43),
            new StandardAnalyzer(Version.LUCENE_43 ),
            new SpanishAnalyzer(Version.LUCENE_43),
            new UAX29URLEmailAnalyzer(Version.LUCENE_43),
            new AnalyzerNuevo()
            };

   public static List<String> tokenizeString(Analyzer analyzer, String string) {
    List<String> result = new ArrayList<String>();
    
  //  StandardTokenizer aux;
    
   
    String cad;
    try {
      TokenStream stream  = analyzer.tokenStream(null, new StringReader(string));
      OffsetAttribute offsetAtt = stream.addAttribute(OffsetAttribute.class);
      CharTermAttribute cAtt= stream.addAttribute(CharTermAttribute.class);
      stream.reset();
      while (stream.incrementToken()) {
     
        //cad = stream.getAttribute(CharTermAttribute.class).toString();
        result.add( cAtt.toString()+" : ("+ offsetAtt.startOffset()+"," + offsetAtt.endOffset()+")");
      }
      stream.end();
    } catch (IOException e) {
      // not thrown b/c we're using a string reader...
      throw new RuntimeException(e);
    }
    return result;
  }

public static void testLuceneStandardTokenizer() throws Exception {

  StandardTokenizer tokenizer=new StandardTokenizer( Version.LUCENE_43, 
          new StringReader("Ella dijo: 'No me puedo creer  presentación presentaremos que el Madrid ganará la copa de S.M. el jhg.kjh.kjh 123.234.334 Rey en 2015-16'."));
  List<String> result=new ArrayList<String>();
  tokenizer.reset();
  while (tokenizer.incrementToken()) {
      
    result.add(((CharTermAttribute)tokenizer.getAttribute(CharTermAttribute.class)).toString());
  }
  System.out.println(result.toString());
 
 
}

  public static void displayTokens(  String text) throws IOException {

    List<String> tokens;
    
    
    
    
    
    
    
    
    for (Analyzer an : analizadores){
        System.out.println("Analizador "+an.getClass());
        tokens = tokenizeString(an,text);
        for (String tk : tokens) {
            System.out.println("[" + tk + "] ");
         }
     }
    }

   public static void main(String[] args) throws IOException, Exception {
           displayTokens("Esto es un mensaje, with some text to write's 123.45 12 AB&C and jhg@decsai.ugr.es http://decsai.ugr.es");
           testLuceneStandardTokenizer();
           displayTokens("Ella dijo: dijeron perro perra perritos perrita  'No me puedo creer que el Madrid presentación presentaremos  ganará la copa de S.M. el Rey en 2015-16'.");
    }

    }


 
