import java.io.File;
import org.apache.tika.Tika;
import org.apache.tika.parser.*;
import java.io.*;
import org.apache.tika.sax.BodyContentHandler;
import java.net.*;
import org.apache.tika.metadata.Metadata;

public class core{
  public static void main(String[] args) throws Exception{
      Tika tika=new Tika();
      for (String f : args){
          File file=new File(f);
          InputStream is=new FileInputStream(file);
          Metadata metadata=new Metadata();
          BodyContentHandler ch=new BodyContentHandler(-1);
          ParseContext parseContext=new ParseContext();
          AutoDetectParser parser=new AutoDetectParser();

          parser.parse(is,ch,metadata,parseContext);

          System.out.println("ch  "+ch.toString());


          for(String name : metadata.names()){
            String valor=metadata.get(name);
            if(valor!=null){
              System.out.println("metadata:"+name+" "+valor);
            }
          }
          System.out.println("la  "+metadata.get(Metadata.CONTENT_TYPE));
    }
 }
}
