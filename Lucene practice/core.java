import java.io.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.tokenattributes.*;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import java.util.*;
import javax.swing.*;
import java.lang.Math;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.tika.Tika;
import org.apache.tika.parser.*;
import org.apache.tika.sax.BodyContentHandler;
import java.net.*;
import org.apache.tika.metadata.Metadata;

import org.apache.tika.language.LanguageIdentifier;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.langdetect.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageResult;
import org.apache.tika.parser.txt.TXTParser;
import org.apache.lucene.analysis.*;



public class core{


  public static void InsertLine(File archivo, String line)throws IOException{
    if(!archivo.exists()){
      archivo.createNewFile();
    }
    FileWriter fw=new FileWriter(archivo,true);
    BufferedWriter bw;
    bw = new BufferedWriter(fw);
    bw.write(line+"\n");
    bw.close();

  }

public static void ToCSV(Map<String, Integer> wordmaps, String filename)throws Exception{
  //if( filename == ""){filename= Math.random().toString();}
  String ruta="solutions/"+filename+"_ocurrencias.csv";


  TreeMap<Integer, List<String>> Ordermap = new TreeMap<>(Comparator.reverseOrder());
  Iterator<Map.Entry<String, Integer>> itr = wordmaps.entrySet().iterator();

  while(itr.hasNext()){
       Map.Entry<String, Integer> entry = itr.next();
       //System.out.println("Key = " + entry.getKey() +  ", Value = " + entry.getValue());
       if(Ordermap.containsKey(entry.getValue())){
         //Ordermap.put(entry.getValue(),entry.getKey());
         Ordermap.get(entry.getValue()).add(entry.getKey());
       }
       else{
         List lista=new ArrayList();
         lista.add(entry.getKey());
         Ordermap.put(entry.getValue(),lista);
       }
  }

  File archivo = new File(ruta);

  Integer i=1;
  InsertLine(archivo,wordmaps.size()+"\n");
  for(Map.Entry<Integer,List<String>> entry : Ordermap.entrySet()) {

    Integer key = entry.getKey();
    List<String> value = entry.getValue();


    for(String values: value){
      String resultado=values+" "+Integer.toString(key);
      InsertLine(archivo,resultado);
      //System.out.println(i+"  "+resultado);
      i=i+1;
    }
  }


}


  public static void tokenizeString( Analyzer analyzer , String string,String name)throws Exception{
    try{
    TokenStream stream=analyzer.tokenStream(null, new StringReader(string));
    CharTermAttribute cAtt=stream.addAttribute(CharTermAttribute.class);
    stream.reset();

    //adding the words to the map
    Map<String, Integer> wordmaps = new HashMap<String, Integer>();


    while (stream.incrementToken()) {
       //System.out.println(stream.getAttribute(CharTermAttribute.class));
       String word=stream.getAttribute(CharTermAttribute.class).toString();
       //wordmaps.put(word,wordmaps.get(word)+1);
       if(wordmaps.containsKey(word)){
           wordmaps.put(word,wordmaps.get(word)+1);
       }
       else{
         wordmaps.put(word,1);
       }
   }
   ToCSV(wordmaps,name);


    stream.end();
      //fin
    }catch ( IOException e ) {
      throw new RuntimeException(e);
    }
  }

public static void FirstAnalyzer(String fichero,String name)throws Exception{
  // Analyzer an = new SimpleAnalyzer();
  // tokenizeString(an,fichero,"SA_"+name);

  RussianAnalyzer anRu = new RussianAnalyzer();
  tokenizeString(anRu,fichero,"RA_"+name);

  Analyzer Wa=new WhitespaceAnalyzer();
  tokenizeString(Wa,fichero,"WA_"+name);
  //
  //
  SpanishAnalyzer SpA=new SpanishAnalyzer();
  tokenizeString(SpA,fichero,"SpA_"+name);


  EnglishAnalyzer EA=new EnglishAnalyzer();
  tokenizeString(EA,fichero,"EA"+name);
}



  public static void WalkFile(String dir)throws Exception{
      boolean result;
      File folderFile = new File(dir); // Creamos un directorio con el path dado.
          if ((result = folderFile.exists())) { //Si todo marcha bien...
              File[] files = folderFile.listFiles(); //Los archivos del directorio
              for (File file : files) { // Recorremos los archivos
                  boolean isFolder = file.isDirectory();

                  if (isFolder){WalkFile(file.toString());} //Si el archivo es de un directorio lo recorremos llamando recursivamente a esta función
                  else{//SI no es un directorio, extraemos sus metadatos y contenido.
                      InputStream is=new FileInputStream(file); //Guardamos en is el archivo
                      BodyContentHandler ch=new BodyContentHandler(-1); //Contenido del archivo
                      Metadata metadata=new Metadata();
		                  ParseContext parseContext=new ParseContext();
                      AutoDetectParser parser=new AutoDetectParser();
                      ///**************************************************AQUI  Como leer un documento
                      parser.parse(is,ch,metadata,parseContext);
			                 FirstAnalyzer(ch.toString(),metadata.get("title"));// se manda el texto en un String
		                   }
              }
          }
  }


  public static void main(String[] args) throws Exception{
    File fichero = new File("solutions");
    if (fichero.exists()){ //Si el fichero que queremos crear ya está creado lo eliminamos
        File[] files = fichero.listFiles();
        for (File file : files) {
          file.delete();
        }
    }
     WalkFile(args[0]); //Llamamos a la función para recorrer el path
    //Analyzer an= AnalizadorCodigo(); //Este no esta hecho
    //Analyzer an=new WhitespaceAnalyzer();
    //tokenizeString(an,"La revolucion empieza por la revolucion de las masas");

  }


}
