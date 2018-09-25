import java.io.File;
import org.apache.tika.Tika;
import org.apache.tika.parser.*;
import java.io.*;
import org.apache.tika.sax.BodyContentHandler;
import java.net.*;
import org.apache.tika.metadata.Metadata;

import org.apache.tika.language.LanguageIdentifier;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.langdetect.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageResult;
import org.apache.tika.parser.txt.TXTParser;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.*;


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


  public static String identifyLanguage(String text) throws IOException{
      LanguageDetector identifier=new OptimaizeLangDetector().loadModels();
      LanguageResult idioma=identifier.detect(text);
      //System.out.print(idioma.getLanguage()+"\n");
      return idioma.getLanguage(); // Devuelve idioma
  }

  public static void separation(String f, String name_file)throws Exception{
    
    if(name_file == null || name_file == "")
    {
	name_file = "Archivo sin Nombre";
    }


    String[] parts=f.split(" ");

    Map<String, Integer> wordmaps = new HashMap<String, Integer>();

    for (String part : parts){
        if(wordmaps.containsKey(part)){
            //esta
            wordmaps.put(part,wordmaps.get(part)+1);
        }
        else{
          wordmaps.put(part,1);
        }
    }

        TreeMap<Integer, String> Ordermap = new TreeMap<>(Comparator.reverseOrder());

        Iterator<Map.Entry<String, Integer>> itr = wordmaps.entrySet().iterator();
        while(itr.hasNext()){
             Map.Entry<String, Integer> entry = itr.next();
             //System.out.println("Key = " + entry.getKey() +  ", Value = " + entry.getValue());
             Ordermap.put(entry.getValue(),entry.getKey());
        }

        for(Map.Entry<Integer,String> entry : Ordermap.entrySet()) {
          Integer key = entry.getKey();
          String value = entry.getValue();

          String resultado=value+" "+Integer.toString(key);
          String ruta="solutions/"+name_file.toString()+"_ocurrencias.txt";
          File archivo = new File(ruta);
          InsertLine(archivo,resultado);
          System.out.println(resultado);
        }
  }

  public static void AllMetadata(String f)throws Exception{
        Tika tika=new Tika(); // Instancia de TIka
        File file=new File(f); 
        InputStream is=new FileInputStream(file); //Guardamos en is el archivo
        Metadata metadata=new Metadata();
        BodyContentHandler ch=new BodyContentHandler(-1); //Contenido del archivo
        ParseContext parseContext=new ParseContext();
        AutoDetectParser parser=new AutoDetectParser();

        parser.parse(is,ch,metadata,parseContext);
        String languages=identifyLanguage(ch.toString());
        System.out.println("Debug");
	separation(ch.toString(),metadata.get("title"));
	System.out.print("\n"+ parseContext +"\n");

        // for(String name : metadata.names()){
        //   String valor=metadata.get(name);
        //   if(valor!=null){
        //     System.out.println("metadata: "+name+" "+valor);
        //   }
        // }	
        System.out.println("la  "+metadata.get(Metadata.CONTENT_ENCODING)); 
        String ruta = "solutions/file.txt"; //Ruta donde escribir
        File archivo = new File(ruta); //Abrimos la ruta
        String information=metadata.get("title")+"*"+metadata.get(Metadata.CONTENT_TYPE)+"*"+metadata.get(Metadata.CONTENT_ENCODING)+"*"+languages; //Title, separado por * de Content_type, content_encoding y languages.

        InsertLine(archivo,information); //Función para escribir la información en el archivo
  }

  public static List<String> extractUrls(String text){
		
	      //System.out.println("Depura1");
	    List<String> containedUrls = new ArrayList<String>();
	    String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
	    Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
	    Matcher urlMatcher = pattern.matcher(text);
	    int i = 0;
	    while (urlMatcher.find())
	    {
		containedUrls.add(text.substring(urlMatcher.start(0),
		        urlMatcher.end(0)));
		i++;
	    }

	    return containedUrls;
  }

  public static void LinksAndOcurrences(String f)throws Exception{

        Tika tika=new Tika(); // Instancia de Tika
        File file=new File(f); 
        InputStream is=new FileInputStream(file); //Guardamos en is el archivo
        Metadata metadata=new Metadata();
        BodyContentHandler ch=new BodyContentHandler(-1); //Contenido del archivo
        ParseContext parseContext=new ParseContext();
        AutoDetectParser parser=new AutoDetectParser();

      	FileInputStream inputstream = new FileInputStream(new File(f));
      	ParseContext pcontext=new ParseContext();
	      
	parser.parse(is,ch,metadata,parseContext); // Para el metadato del titulo
        //Text document parser
      	TXTParser TexTParser = new TXTParser();
      	TexTParser.parse(is, ch, metadata,parseContext);
      	String contenidoDocumentos = ch.toString(); //En esta variable tenemos todo el texto del archivo
	
	String ruta = "solutions/enlaces.txt"; //Ruta donde escribir
        File archivo = new File(ruta); //Abrimos la ruta
	
	List<String> URLs = extractUrls(contenidoDocumentos);
	
	if(metadata.get("title")!=null){

	InsertLine(archivo, "/n" + " DOCUMENTO: " + metadata.get("title") + "/n" + "/n"); // Para separarlos por archivo en el txt
	}
	else{
	InsertLine(archivo, "/n" + " DOCUMENTO: " + f + "/n" + "/n"); // Por si alguno viene sin el título que sepamos cual es
	}

	for (String url : URLs) {
		InsertLine(archivo,url);
	}

  }
  public static void WalkFile(String dir)throws Exception{
      boolean result;
      File folderFile = new File(dir); // Creamos un directorio con el path dado.
          if ((result = folderFile.exists())) { //Si todo marcha bien...
              File[] files = folderFile.listFiles(); //Los archivos del directorio
              for (File file : files) { // Recorremos los archivos
                  boolean isFolder = file.isDirectory();
                  //System.out.println((isFolder ? "FOLDER: " : "  FILE: ") + file.getName());
                  if (isFolder){WalkFile(file.toString());} //Si el archivo es de un directorio lo recorremos llamando recursivamente a esta función
                  else{//SI no es un directorio, extraemos sus metadatos y contenido.
			AllMetadata(file.toString());
			LinksAndOcurrences(file.toString());
		  } 
              }
          }
  }

  public void plot2d() { //int[] Xaxis, int[] Yaxis


  };

  public static void main(String[] args) throws Exception{


      File fichero = new File("solutions");
      if (fichero.exists()){ //Si el fichero que queremos crear ya está creado lo eliminamos
          File[] files = fichero.listFiles();
          for (File file : files) {
            file.delete();
          }
      }

      WalkFile(args[0]); //Llamamos a la función para recorrer el path
  }

}
