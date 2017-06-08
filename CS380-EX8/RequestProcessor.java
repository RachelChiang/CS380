/*
 * Rachel Chiang
 * CS 380.01 Computer Networks
 * Exercise 8: Simple Web Server
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * This class simply processes the requests given to the server.
 * @author rchiang
 */
public class RequestProcessor
{
   /**
    * This method generates the response message, depending on the validity
    * of the specified file, by calling {@link #create200OK(String)} or
    * {@link #create404NF()}.
    * @param request - the entire request
    * @return - the entire response message, either a 200 OK or 404 Not Found
    */
   public String generateResponseMessage(String request)
   {
      String filePath = parsePath(request);
      File file = new File(filePath);
      if (file.exists())
      {
         // file exists, send content
         return create200OK(filePath);
      }
      
      // file does not exist, send 404
      return create404NF();
   }
   
   /**
    * This method generates the 200 OK response.
    * @param content - the file content
    * @return - the response
    */
   private String create200OK(String filePath)
   {
      String content = extractFileContent(filePath);
      String response = "HTTP/1.1 200 OK\n"
            + "Content-type: text/html\n"
            + "Content-length: " + content.length() + "\n\n";
      response += content;
      
      return response;
   }

   /**
    * This method generates the 404 Not Found response.
    * @return - the response
    */
   private String create404NF()
   {
      String content = extractFileContent("www/notFound.html");
      String response = "HTTP/1.1 404 Not Found\n"
            + "Content-type: text/html\n"
            + "Content-length: " + content.length() + "\n\n";
      return response + content;
   }
   
   /**
    * This method simply pulls text from a file.
    * @param filePath
    * @return
    */
   private String extractFileContent(String filePath)
   {
      try {
         Scanner fin = new Scanner(new FileInputStream(filePath));
         String contents = "";
         while (fin.hasNext())
         {
            contents += fin.nextLine();
         }
         fin.close();
         return contents;
      } catch (FileNotFoundException fnfe) {
         fnfe.printStackTrace(System.err);
      }
      return null;
   }
   
   /**
    * This method simply takes the PATH from the GET message.
    * @param getRequest
    * @return
    */
   private String parsePath(String getRequest)
   {
      if (getRequest.startsWith("GET "))
      {
         if (getRequest.charAt(4) == '/')
         {
            int endIndex = getRequest.indexOf(" HTTP/1.1");
            String path = getRequest.substring(4, endIndex);
            return ("www" + path);
         }
      }
      
      return null;
   }
}
