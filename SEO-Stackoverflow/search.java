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

import org.jsoup.Jsoup;

import org.apache.lucene.search.*;
import org.apache.lucene.queryparser.classic.*;

import org.apache.lucene.facet.*;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.FacetLabel;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts;

public class search{
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

  public static void codequery(String query,Set<Document> solutions_All)throws Exception{
    String indexPath = "./index";
    Directory dir=FSDirectory.open(Paths.get(indexPath));
    IndexReader reader=DirectoryReader.open(dir);
    IndexSearcher searcher=new IndexSearcher(reader);
    Similarity similarity = new ClassicSimilarity();
    searcher.setSimilarity(similarity);
    QueryParser parser =new QueryParser("All",AnalyzerCode());
    String q_request=String.format("Code: %s ", query);
    Query	q_resumen= parser.parse(q_request);
    TopDocs docs=searcher.search(q_resumen,20);
    for (ScoreDoc sd : docs.scoreDocs){
      Document d=searcher.doc(sd.doc);
      solutions_All.add(d);
    }

  }

  public static void body_titlequery(String query,Set<Document> solutions_All)throws Exception{
    String indexPath = "./index";
    Directory dir=FSDirectory.open(Paths.get(indexPath));
    IndexReader reader=DirectoryReader.open(dir);
    IndexSearcher searcher=new IndexSearcher(reader);
    Similarity similarity = new ClassicSimilarity();
    searcher.setSimilarity(similarity);
    QueryParser parser =new QueryParser("All",new EnglishAnalyzer());
    String q_request=String.format("body : %s  title: %s ", query,query);
    Query	q_resumen= parser.parse(q_request);
    TopDocs docs=searcher.search(q_resumen,20);
    for (ScoreDoc sd : docs.scoreDocs){
      Document d=searcher.doc(sd.doc);
      solutions_All.add(d);
    }
  }
  public static void unitariquery(String query,Set<Document> solutions_All,Set<String> solutions_Tags)throws Exception{

    String indexPath = "./index";
    Directory dir=FSDirectory.open(Paths.get(indexPath));
    IndexReader reader=DirectoryReader.open(dir);
    IndexSearcher searcher=new IndexSearcher(reader);
    Similarity similarity = new ClassicSimilarity();
    searcher.setSimilarity(similarity);

    QueryParser parser =new QueryParser("All",new EnglishAnalyzer());
    String q_request=String.format("Id_Q : %s Id_A : %s Id_T : %s  User: %s tag: %s", query,query,query,query,query);
    Query	q_ID= parser.parse(q_request);
    TopDocs docs=searcher.search(q_ID,20);
    for (ScoreDoc sd : docs.scoreDocs){
      Document d=searcher.doc(sd.doc);
        String type=d.get("Type");
        if(!type.equals("Tag")){
          // System.out.println(d.get("All"));
          solutions_All.add(d);
        }
        else{
          //System.out.println(d.get("tag"));
          solutions_Tags.add(d.get("tag"));
        }
    }
    //we will se if also it form part of code or all;
     body_titlequery(query,solutions_All);
     codequery(query,solutions_All);
  }

  public static void searchin_process(String consultation, String Facets)throws Exception{
    Set<Document> solutions_All = new HashSet<Document>();
    Set<String> solutions_Tags = new HashSet<String>();
    //test that consultation is not a file
    File af = new File(consultation);
    String document="";
    String line="";
    if (af.isFile()){
      BufferedReader br = new BufferedReader(new FileReader(consultation));
        while ((line = br.readLine()) != null) {
          document=document+" "+line;
        }
        body_titlequery(document,solutions_All);
        codequery(document,solutions_All);
    }
	else if(Integer.parseInt(Facets) == 1){
		facetsQuery(consultation,solutions_All);
	}
    else{
      //test if the query is an ID of user, an ID of question/answer or a tag
      if(consultation.indexOf(" ")==-1){
          unitariquery(consultation,solutions_All,solutions_Tags);
      }
      else{
        body_titlequery(consultation,solutions_All);
        codequery(consultation,solutions_All);
      }
    }
    int calification=1;
    Map  order_document  = new TreeMap(Collections.reverseOrder());

    for (Document item : solutions_All){
      //System.out.println(item.get("All"));
      //System.out.println("\n\n");
      double mark=(Math.log10(Math.pow(2,Integer.parseInt(item.get("Score"))))+1)/calification;
      //System.out.println(mark);
      order_document.put(mark,item);
     calification=calification+1;
   }

   Iterator<Map.Entry> it = order_document.entrySet().iterator();
   while (it.hasNext()) {
     Map.Entry pair = it.next();
     Document d=(Document)pair.getValue();
     String all=d.get("All");
     System.out.println(pair.getKey() + "   " +all );
     System.out.println("\n\n");
  }

  }

  public static void facetsQuery(String query,Set<Document> solutions_All)throws Exception{
    String indexPath = "./index";
    String facetsPath = "./indexFacets";

    Directory dir=FSDirectory.open(Paths.get(indexPath));
    Directory dirFacets=FSDirectory.open(Paths.get(facetsPath));

    IndexReader reader=DirectoryReader.open(dir);
    IndexSearcher searcher=new IndexSearcher(reader);
    TaxonomyReader taxoReader = new DirectoryTaxonomyReader(dirFacets);
    FacetsConfig fconfig = new FacetsConfig();

    Similarity similarity = new ClassicSimilarity();
    searcher.setSimilarity(similarity);
    QueryParser parser =new QueryParser("All",new EnglishAnalyzer());
    String q_request=String.format("body : %s  title: %s ", query,query);
    Query q_resumen= parser.parse(q_request);
    TopDocs docs=searcher.search(q_resumen,20);

    FacetsCollector fc = new FacetsCollector();
    TopDocs tdc = FacetsCollector.search(searcher, q_resumen, 10, fc);
    /*for (ScoreDoc sd : docs.scoreDocs){
      Document d=searcher.doc(sd.doc);
      solutions_All.add(d);
    }*/
    for (ScoreDoc sd : tdc.scoreDocs){
      Document d=searcher.doc(sd.doc);
      solutions_All.add(d);
    }

    Facets facetas = new FastTaxonomyFacetCounts(taxoReader, fconfig, fc);

    List<FacetResult> TodasDims = facetas.getAllDims(20);

    System.out.println("Categorias totales " + TodasDims.size());
    for( FacetResult fr: TodasDims) {
            System.out.println("Categoría " + fr.dim);
            for(LabelAndValue lv : fr.labelValues) {
                    System.out.println("   Etiq : " + lv.label + " valor (#n)->" + lv.value);
            }
    }

    FacetResult fresult = facetas.getTopChildren(10, "Author");
  }

public static void main(String[] args) throws Exception{
    
    String argument1 = "";
    String argument2 = "";
    Scanner S=new Scanner(System.in);
    argument1=S.nextLine();
    argument2=S.nextLine();
    searchin_process(argument1, argument2);
}

}
