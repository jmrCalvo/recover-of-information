package indexador;

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
import org.apache.lucene.util.NumericUtils;
import org.apache.lucene.document.LongPoint.*;
import org.apache.lucene.document.IntPoint.*;
        
import org.jsoup.Jsoup;
import org.apache.lucene.search.*;

import org.apache.lucene.facet.*;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.FacetLabel;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;

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
					final StandardTokenizer source = new StandardTokenizer();
					TokenStream result = new StandardFilter(source);
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
    if(startPoint<0){
      String[] S_array={"",""};//the first position is the rest of the string and the second is the part wanted
      return S_array;
    }
    else{
      startPoint+=startString.length();
      int endPoint=line.indexOf(endString);
      if(endPoint==-1){//if there is some error and do not find the </code></pre> final
        String[] S_array={"",""};//the first position is the rest of the string and the second is the part wanted
        return S_array;
      }//this is the last error and we wont consider more problems
      if(endPoint<startPoint){startString="<code>";startPoint=line.indexOf(startString);} //in some cases pre is consider as a class and has specifications
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
    return solution;
  }

  public static boolean readQ(String path)throws Exception{
        String indexPath = "./index";
        //String FacetsIndexPath = "./indexFacetsQ";
        String FacetsIndexPath = "./indexFacets";
        Similarity similarity = new ClassicSimilarity();

        //the Analyzer
        Map <String,Analyzer> analyzerPerField=new HashMap <String,Analyzer>();
        analyzerPerField.put("Tittle",new EnglishAnalyzer());
        Analyzer directoryAnalyzer = new EnglishAnalyzer();
        analyzerPerField.put("body",directoryAnalyzer );
        Analyzer codeAnalyzer= AnalyzerCode();
        analyzerPerField.put("Code",codeAnalyzer );
        PerFieldAnalyzerWrapper analyzer=new PerFieldAnalyzerWrapper(new EnglishAnalyzer(),analyzerPerField);

		//the index
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setSimilarity(similarity);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        FSDirectory dir = FSDirectory.open(Paths.get(indexPath));
        IndexWriter writer = new IndexWriter(dir, iwc);
        File fd=new File(path);

        //facets index
        FSDirectory taxoDir = FSDirectory.open(Paths.get(FacetsIndexPath));
        FacetsConfig fconfig = new FacetsConfig();

        DirectoryTaxonomyWriter taxoWriter = new DirectoryTaxonomyWriter(taxoDir);
        fconfig.setHierarchical("Publish_Date", true);
        fconfig.setMultiValued("Author", true);
        
        if(fd.exists()){
          if(fd.isFile()){
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;
            String document = "";
        		String tittle = "";
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

                        org.jsoup.nodes.Document docj = Jsoup.parse(body);
                        doc.add(new TextField("body",docj.body().text(), Field.Store.YES));

                        int scoreQuestion=Integer.parseInt(separate[3]);
                        doc.add(new StoredField("Score",scoreQuestion));

                        String codePart=separateCode(document);
                        doc.add(new TextField("Code", codePart, Field.Store.YES));

                        Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(separate[2]);
                        doc.add(new StoredField("Date",date.getTime()));

                        doc.add(new LongPoint("DateLong",date.getTime()));
                        doc.add(new IntPoint("ScoreInt",scoreQuestion));

                        doc.add(new FacetField("Author", separate[1]));
                        doc.add(new FacetField("Publish_Date", String.valueOf(date.getYear())));

                        writer.addDocument(fconfig.build(taxoWriter, doc));
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
          taxoWriter.close();
          writer.close();
        }
        else{
          System.out.print("the file of questions does not exist, sorry");
        }
				return true;
  }

  public static boolean readA(String path)throws Exception{

        String indexPath = "./index";
        //String FacetsIndexPath = "./indexFacetsA";
        String FacetsIndexPath = "./indexFacets";
        
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
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        FSDirectory dir = FSDirectory.open(Paths.get(indexPath));
        IndexWriter writer = new IndexWriter(dir, iwc);
        File fd=new File(path);

        //facets index
        FSDirectory taxoDir = FSDirectory.open(Paths.get(FacetsIndexPath));
        FacetsConfig fconfig = new FacetsConfig();

        DirectoryTaxonomyWriter taxoWriter = new DirectoryTaxonomyWriter(taxoDir);
        fconfig.setHierarchical("Publish_Date", true);
        fconfig.setMultiValued("Author", true);

        if(fd.exists()){
          if(fd.isFile()){
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;
            String document = "";
            String tittle = "";
            br.readLine();//read the first line which is the explication
            while ((line = br.readLine()) != null) {
                  if(line.length()>0){
                    if(line.charAt(0) == '\"'){
                      if(line.length()==1){
                        Document doc = new Document();
                        String[] separate=document.split(",");

                        String body="";
                        for(int i=6;i<separate.length;i++){
                          body=body+" "+separate[i];
                        }
                        org.jsoup.nodes.Document docj = Jsoup.parse(body);
                        doc.add(new TextField("body",docj.body().text(), Field.Store.YES));

                        doc.add(new TextField("All", document, Field.Store.YES));
                        doc.add(new StringField("Id",separate[0], Field.Store.YES));
                        doc.add(new StringField("User",separate[1], Field.Store.YES));

                        int scoreAnswer=Integer.parseInt(separate[4]);
                        doc.add(new StoredField("Score",scoreAnswer));

                        doc.add(new StringField("IdParent",separate[3], Field.Store.YES));
                        doc.add(new StringField("Aproved",separate[5], Field.Store.YES));

			String codePart=separateCode(document);
                        doc.add(new TextField("Code", codePart, Field.Store.YES));

			Date date = new SimpleDateFormat("yyyy-MM-dd",  Locale.ENGLISH).parse(separate[2]);
                        doc.add(new StoredField("Date",date.getTime()));

                        doc.add(new IntPoint("ScoreInt",scoreAnswer));     
                        doc.add(new LongPoint("DateLong",date.getTime()));



                        doc.add(new FacetField("Author", separate[1]));
                        doc.add(new FacetField("Publish_Date", String.valueOf(date.getYear())));

                        writer.addDocument(fconfig.build(taxoWriter, doc));

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
            System.out.print("the path for Answers is not a file, Sorry");
          }
          writer.commit();
          taxoWriter.close();
          writer.close();
        }
        else{
          System.out.print("the file of Answers does not exist, sorry");
        }
        return true;
  }

  public static boolean readT(String path)throws Exception{

        String indexPath = "./index";

        Similarity similarity = new ClassicSimilarity();

   		Analyzer analyzer = new WhitespaceAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setSimilarity(similarity);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        FSDirectory dir = FSDirectory.open(Paths.get(indexPath));
        IndexWriter writer = new IndexWriter(dir, iwc);
        File fd=new File(path);

        if(fd.exists()){
          if(fd.isFile()){
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;
            String document = "";
            String tittle = "";
            br.readLine();//read the first line which is the explication
            while ((line = br.readLine()) != null) {
                        Document doc = new Document();
                        document = line;
                        String[] separate=document.split(",");

                        doc.add(new StringField("Id",separate[0], Field.Store.YES));
                        doc.add(new StringField("tag",separate[1], Field.Store.YES));
                        writer.addDocument(doc);
                        document = "";
            }
          }
          else{
            System.out.print("the path for tags is not a file, Sorry");
          }
          writer.commit();
          writer.close();
        }
        else{
          System.out.print("the file of tags does not exist, sorry");
        }
				return true;
  }

  public static boolean readAll(String path)throws Exception{
    switch(path){
      case "R_information/Questions.csv":
          readQ(path);
		  System.out.print("OK Questions");
      break;
      case "R_information/Answers.csv":
          readA(path);
		  System.out.print("OK Answers");
      break;
      case "R_information/Tags.csv":
          readT(path);
		  System.out.print("OK Tags");
      break;

    }
		return true;
  }
//****************************************************************************//

	public static void unitariquery(String query)throws Exception{
		String indexPath = "./index";
		Directory dir=FSDirectory.open(Paths.get(indexPath));
		IndexReader reader=DirectoryReader.open(dir);
		IndexSearcher searcher=new IndexSearcher(reader);

		Query	q_ID= new TermQuery(new Term("Id",query));
		TopDocs docs=searcher.search(q_ID,100);
		for (ScoreDoc sd : docs.scoreDocs){
			Document d=searcher.doc(sd.doc);
			System.out.println(d.getField("User"));
		}

	}

	public static void searchin_process(String consultation)throws Exception{

		//test that consultation is not a file
		File af = new File(consultation);
		if (af.isFile()){
		}
		else{
			//test if the query is an ID of user, an ID of question/answer or a tag
			if(consultation.indexOf(" ")==-1){
					unitariquery(consultation);
			}
			else{

			}
		}
	}

//*****************************************************************************//
public static void main(String[] args) throws Exception{
	File af = new File("index");

	boolean steps=true;

	if(!af.exists()){
		steps=steps && readAll("R_information/Answers.csv");
  	steps=steps && readAll("R_information/Questions.csv");
  	steps=steps && readAll("R_information/Tags.csv");
	}

		if(steps){
			searchin_process("77434");
		}
		else{
		System.out.print("the file of tags does not exist, sorry");
		}
	}
}
