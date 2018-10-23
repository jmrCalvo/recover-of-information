import java.io.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.tokenattributes.*;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.analysis.charfilter.MappingCharFilter;
import org.apache.lucene.analysis.charfilter.NormalizeCharMap;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.util.Version;


import org.apache.lucene.analysis.*;

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

import java.io.IOException;
import java.io.Reader;



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
  filename=filename+".csv";
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
  for(Map.Entry<Integer,List<String>> entry : Ordermap.entrySet()) {

    Integer key = entry.getKey();
    List<String> value = entry.getValue();

    for(String values: value){
      String resultado=values+" "+Integer.toString(key)+" "+Math.log10(i)+" "+Math.log10(key);
      InsertLine(archivo,resultado);
      System.out.println(i+"  "+resultado);
      i=i+1;
    }
  }


}


  public static void tokenizeString( Analyzer analyzer , String string){
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

   for(Map.Entry<String,Integer> entry : wordmaps.entrySet()) {
      System.out.println(entry);
   }

    stream.end();
      //fin
    }catch ( IOException e ) {
      throw new RuntimeException(e);
    }
  }

public static void FirstAnalyzer(String fichero)throws Exception{
  Analyzer an = new SimpleAnalyzer();
  tokenizeString(an,fichero);
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
			                 FirstAnalyzer(ch.toString());
		                   }
              }
          }
  }

  public static void WalkFileCode(String dir)throws Exception{
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
			                     Analyzer code= AnalyzerCode();
   					     tokenizeString(code,ch.toString());
		                   }
              }
          }
  }


public static Analyzer AnalyzerCode(){



	return new Analyzer(){


		@Override
		 protected Reader initReader(String fieldName, Reader reader) {
		    NormalizeCharMap.Builder builder = new NormalizeCharMap.Builder();
		    builder.add(".", " ");
		    builder.add("_", " ");
		    NormalizeCharMap normMap = builder.build();
		    return new MappingCharFilter(normMap, reader);
		 }



		@Override
		protected Analyzer.TokenStreamComponents createComponents(String string) {

		try{

		//final Tokenizer source = new StandardTokenizer();
		final StandardTokenizer source = new StandardTokenizer();

		TokenStream result = new StandardFilter(source);
		//result = new LowerCaseFilter(result);

		List<String> stopwords = new ArrayList<String>();

		String cadena;
		FileReader f = new FileReader("AnalyzerWords/code.in");
		BufferedReader b = new BufferedReader(f);

		while((cadena = b.readLine())!=null){

			stopwords.add(cadena);

		}
		b.close();


		result = new NumerosFilter(result);

		result = new StopFilter(result, new CharArraySet(stopwords,true));
		//result = new SnowballFilter(result, language);

		return new TokenStreamComponents(source, result){

			@Override
			protected void setReader(final Reader reader) { super.setReader(reader); };
		};
		}
		catch (IOException e){
			throw new RuntimeException(e);
		}
		}

    	};
}

static class NumerosFilter extends FilteringTokenFilter {

	private final CharTermAttribute termAtt = addAttribute ( CharTermAttribute.class );

	public NumerosFilter (TokenStream in) {
		super (in);
	}

	@Override
	protected boolean accept() throws IOException {

		String token = new String (termAtt.buffer(), 0, termAtt.length());

		if(token.matches("[0-9,.]+")){
			return false;
		}
		return true;
	}
}


  public static void main(String[] args) throws Exception{

     File fichero = new File("Codes");
     if (fichero.exists()){ //Si el fichero que queremos crear ya está creado lo eliminamos
         File[] files = fichero.listFiles();
         for (File file : files) {
           file.delete();
        }
     }
     WalkFileCode(args[0]); //Llamamos a la función para recorrer el path

  }


}
