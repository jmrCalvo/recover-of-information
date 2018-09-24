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
      return idioma.getLanguage();
  }

  public static void AllMetadata(String f)throws Exception{
        Tika tika=new Tika();
        File file=new File(f);
        InputStream is=new FileInputStream(file);
        Metadata metadata=new Metadata();
        BodyContentHandler ch=new BodyContentHandler(-1);
        ParseContext parseContext=new ParseContext();
        AutoDetectParser parser=new AutoDetectParser();

        parser.parse(is,ch,metadata,parseContext);
        //System.out.println("ch  "+ch.toString());
        String languages=identifyLanguage(ch.toString());

        // for(String name : metadata.names()){
        //   String valor=metadata.get(name);
        //   if(valor!=null){
        //     System.out.println("metadata: "+name+" "+valor);
        //   }
        // }
        System.out.println("la  "+metadata.get(Metadata.CONTENT_ENCODING));
        String ruta = "solutions/file.csv";
        File archivo = new File(ruta);
        String information=metadata.get("title")+"*"+metadata.get(Metadata.CONTENT_TYPE)+"*"+metadata.get(Metadata.CONTENT_ENCODING)+"*"+languages;
        InsertLine(archivo,information);
  }

  public static void WalkFile(String dir)throws Exception{
      boolean result;
      File folderFile = new File(dir);
          if ((result = folderFile.exists())) {
              File[] files = folderFile.listFiles();
              for (File file : files) {
                  boolean isFolder = file.isDirectory();
                  //System.out.println((isFolder ? "FOLDER: " : "  FILE: ") + file.getName());
                  if (isFolder){WalkFile(file.toString());}
                  else{AllMetadata(file.toString());}
              }
          }
  }


  public static void main(String[] args) throws Exception{
      File fichero = new File("solutions");
      if (fichero.exists()){
          File[] files = fichero.listFiles();
          for (File file : files) {
            file.delete();
          }
      }
      WalkFile(args[0]);
  }

}
