/*
 * Rachel Chiang
 * CS 380.01 Computer Networks
 * Exercise 8: Simple Web Server
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This program hosts a server on port 8080 and implements two HTTP responses.
 * The server will only respond to GET requests. If the file specified exists,
 * it will respond with the file's contents in a 200 OK message. Otherwise, it
 * will give a 404 Not Found response.
 * @author rchiang
 */
public class WebServer
{
   public static void main(String[] args)
   {
      try {
         ServerSocket serverSocket = new ServerSocket(8080);

         while (true)
         {
            try {
               // Client connected
               Socket socket = serverSocket.accept();

               // Gets the input stream for the socket
               InputStream is = socket.getInputStream();
               InputStreamReader isr = new InputStreamReader(is, "UTF-8");
               // Used to read from the char input stream and buffers the chars
               BufferedReader br = new BufferedReader(isr);

               // receive a GET request
               String msg = br.readLine();
               String request = "";
               while (!msg.equals(""))
               {
                  request += msg + "\n";
                  msg = br.readLine();
               }
               
               // process request
               RequestProcessor rqProcessor = new RequestProcessor();
               
               // respond
               OutputStream os = socket.getOutputStream();
               PrintStream out = new PrintStream(os, true, "UTF-8");
               out.printf("%s", rqProcessor.generateResponseMessage(request));
               
            } catch (IOException ioe) {
               ioe.printStackTrace(System.err);
            }
         }
      } catch (IOException ioe) {
         ioe.printStackTrace(System.err);
      }
   }

}
