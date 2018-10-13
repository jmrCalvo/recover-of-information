/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analizador1;

import java.io.IOException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
/**
 *
 * @author jhg
 */
public class Analizador {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
       // Analyzer an = new WhitespaceAnalyzer();
        Analyzer an = new SimpleAnalyzer();
        String cadena = "Ejemplo    de analizador + WhiteSpace, lucene-7.1.0";
        TokenStream stream  = an.tokenStream(null,  cadena);
      
        ShingleFilter sf = new ShingleFilter(stream,2,2);
         
//        stream.reset();
//        while (stream.incrementToken()) {
//        //cad = stream.getAttribute(CharTermAttribute.class).toString();
//       System.out.println(stream.getAttribute(CharTermAttribute.class));
//      }
//      stream.end();
//      stream.close();
      
         sf.reset();
        while (sf.incrementToken()) {
        //cad = stream.getAttribute(CharTermAttribute.class).toString();
       System.out.println(sf.getAttribute(CharTermAttribute.class));
      }
      sf.end();
      sf.close();
    }
    
}
 
