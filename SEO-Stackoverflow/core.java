core.java
Tipo
Java
Tamaño
13 KB (13.599 bytes)
Almacenamiento usado
0 bytesPropiedad de undefined
Ubicación
parte A
Propietario
Exmorphis L
Modificado
12:30 por Exmorphis L
Abierto
12:31 por mí
Creación
12:30
Añadir descripción
Los lectores pueden descargar

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
import org.apache.lucene.analysis.snowball.*;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;
import java.nio.file.Paths;
import java.util.*;
import javax.swing.*;
import java.lang.Math;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;




import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import org.joda.time.format.DateTimeFormat;

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
import org.apache.lucene.document.*;
import org.joda.time.format.DateTimeFormatter.*;
import org.joda.time.DateTime;

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



public class core{



	public static Analyzer AnalyzerCode(){



		return new Analyzer(){


			@Override
			 protected Reader initReader(String fieldName, Reader reader) {
				NormalizeCharMap.Builder builder = new NormalizeCharMap.Builder();
				builder.add(".", " ");
				builder.add(",", " ");
				builder.add("{", " ");
				builder.add("}", " ");
				builder.add("(", " ");
				builder.add(")", " ");
				builder.add("<", " ");
				builder.add(">", " ");
				builder.add(";", " ");
				builder.add("==", " ");
				builder.add("=", " ");
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

  public static String[] GetCode(String line)throws Exception{

    String startString="<pre><code>";
    String endString="</code></pre>";
    int startPoint=line.indexOf(startString);
    //System.out.print(startPoint+"   ");
    if(startPoint<0){
      String[] S_array={"",""};//the first position is the rest of the string and the second is the part wanted
      return S_array;
    }
    else{
      //System.out.print(line+"   ");

      startPoint+=startString.length();
      int endPoint=line.indexOf(endString);

      if(endPoint==-1){//if there is some error and do not find the </code></pre> final
        String[] S_array={"",""};//the first position is the rest of the string and the second is the part wanted
        return S_array;
      }//this is the last error and we wont consider more problems

      if(endPoint<startPoint){startString="<code>";startPoint=line.indexOf(startString);} //in some cases pre is consider as a class and has specifications

      //System.out.print(endPoint+"\n\n");

      int end=line.length();
      String[] S_array={line.substring(startPoint,endPoint),line.substring(endPoint+endString.length(),end)};
      return S_array;
    }

  }

  public static String separateCode(String line)throws Exception{
    String[] results=GetCode(line);
    String solution="";
    while(results[0]!=""){
      solution=solution+" "+results[0];
      results=GetCode(results[1]);
    }
    //System.out.print(solution+"\n\n");
    return solution;
  }

  public static String readQ(String path)throws Exception{

        String indexPath = "./index";

        Similarity similarity = new ClassicSimilarity();

        //the Analyzer
        Map <String,Analyzer> analyzerPerField=new HashMap <String,Analyzer>();
        analyzerPerField.put("Tittle",new EnglishAnalyzer());
        Analyzer directoryAnalyzer = new EnglishAnalyzer();
        analyzerPerField.put("body",directoryAnalyzer );
        Analyzer codeAnalyzer= AnalyzerCode();
        analyzerPerField.put("Code",codeAnalyzer );
        PerFieldAnalyzerWrapper analyzer=new PerFieldAnalyzerWrapper(new EnglishAnalyzer(),analyzerPerField);

        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setSimilarity(similarity);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        FSDirectory dir = FSDirectory.open(Paths.get(indexPath));
        IndexWriter writer = new IndexWriter(dir, iwc);
        File fd=new File(path);

        if(fd.exists()){
          if(fd.isFile()){
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;
            String document = "";
        		String tittle = "";
        		boolean primeraLinea = true; //La primera línea de los documentos es "metadatos" y no los necesitamos
        		br.readLine();//read the first line which is the explication
            while ((line = br.readLine()) != null) {
        		      if(line.length()>0){
        		        if(line.charAt(0) == '\"'){
        		          if(line.length()==1){
        		            Document doc = new Document();

                        String[] separate=document.split(",");
                        doc.add(new TextField("All", document, Field.Store.YES));
                        doc.add(new TextField("Tittle", separate[4], Field.Store.YES));
                        doc.add(new StringField("Id",separate[0], Field.Store.YES));
                        doc.add(new StringField("User",separate[1], Field.Store.YES));

                        String body="";
                        for(int i=5;i<separate.length;i++){
                          body=body+" "+separate[i];
                        }
                        doc.add(new TextField("body",body, Field.Store.YES));

                        int scoreQuestion=Integer.parseInt(separate[3]);
                        doc.add(new StoredField("Score",scoreQuestion));

                        String codePart=separateCode(document);
                        doc.add(new TextField("Code", codePart, Field.Store.YES));

                        Date date = new SimpleDateFormat("yyyy-MM-dd",  Locale.ENGLISH).parse(separate[2]);
                        doc.add(new StoredField("Date",date.getTime()));

                        writer.addDocument(doc);
                        document = "";
        		          }
        		          else{
        		            document =document+line+"\n";
        		          }
        		        }
        		        else{
        		          document =document+line+"\n";
        		        }
                	}
            }
          }
          else{
            System.out.print("the path for questions is not a file, Sorry");
          }
          writer.commit();
          writer.close();
        }
        else{
          System.out.print("the file of questions does not exist, sorry");
        }
        return("OK");
  }
  public static String readA(String path)throws Exception{

        String indexPath = "./index";

        Similarity similarity = new ClassicSimilarity();

		Map <String,Analyzer> analyzerPerField=new HashMap <String,Analyzer>();
        analyzerPerField.put("Tittle",new EnglishAnalyzer());
        Analyzer directoryAnalyzer = new EnglishAnalyzer();
        analyzerPerField.put("body",directoryAnalyzer );
        Analyzer codeAnalyzer= AnalyzerCode();
        analyzerPerField.put("Code",codeAnalyzer );
        PerFieldAnalyzerWrapper analyzer=new PerFieldAnalyzerWrapper(new EnglishAnalyzer(),analyzerPerField);

        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setSimilarity(similarity);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        FSDirectory dir = FSDirectory.open(Paths.get(indexPath));
        IndexWriter writer = new IndexWriter(dir, iwc);
        File fd=new File(path);

        if(fd.exists()){
          if(fd.isFile()){
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;
            String document = "";
            String tittle = "";
            boolean primeraLinea = true; //La primera línea de los documentos es "metadatos" y no los necesitamos
            br.readLine();//read the first line which is the explication
            while ((line = br.readLine()) != null) {
                  if(line.length()>0){
                    if(line.charAt(0) == '\"'){
                      if(line.length()==1){
                        Document doc = new Document();
                        String[] separate=document.split(",");
                        doc.add(new TextField("All", document, Field.Store.YES));
                        doc.add(new StringField("Id",separate[0], Field.Store.YES));
                        doc.add(new StringField("User",separate[1], Field.Store.YES));
                        int scoreAnswer=Integer.parseInt(separate[4]);
                        doc.add(new StoredField("Score",scoreAnswer));
                        doc.add(new StringField("IdParent",separate[3], Field.Store.YES));
                        doc.add(new StringField("Aproved",separate[5], Field.Store.YES));

                        String codePart=separateCode(document);
                        //System.out.print(codePart+"\n\n");
                        doc.add(new TextField("Code", codePart, Field.Store.YES));
                        Date date = new SimpleDateFormat("yyyy-MM-dd",  Locale.ENGLISH).parse(separate[2]);
                        doc.add(new StoredField("Date",date.getTime()));

                        writer.addDocument(doc);
                        document = "";
                      }
                      else{
                        document =document+line+"\n";
                      }
                    }
                    else{
                      document =document+line+"\n";
                    }
                  }
            }
          }
          else{
            System.out.print("the path for questions is not a file, Sorry");
          }
          writer.commit();
          writer.close();
        }
        else{
          System.out.print("the file of questions does not exist, sorry");
        }
        return("OK");
  }

  public static String readAll(String path)throws Exception{
    switch(path){
      case "R_information/Questions.csv":
          readQ(path);
      break;
      case "R_information/Answers.csv":
          //readA(path);
      break;

    }
    return ("ok");
  }

  public static void main(String[] args) throws Exception{
    readAll("R_information/Answers.csv");
    readAll("R_information/Questions.csv");
    //readAll("R_information/Tags.csv");

  }
}
