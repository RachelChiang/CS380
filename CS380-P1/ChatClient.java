/*
 * Rachel Chiang
 * CS 380.01 Computer Networks
 * Project 1: Simple Chat Client
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ChatClient
{
   /**
    * This thread reads messages from the server and displays them.
    */
   private static class ListenThread implements Runnable
   {
      private Socket socket;
      public ListenThread(Socket socket)
      {
         this.socket = socket;
      }

      @Override
      public void run()
      {
         try
         {
            // Gets the input stream for the socket
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            // Used to read from the char input stream and buffers the chars
            BufferedReader br = new BufferedReader(isr);

               String msg = "";
               while (msg != null)
               {
                  msg = br.readLine();
                  // Prints messages from server
                  System.out.printf("%s%n", msg);
               }
            } catch (IOException ioe) {
               System.out.println("[LT] Unexpected IO error occurred.");
               ioe.printStackTrace();
            }
         }
      }

   /**
    * This thread allows the user to input messages to send to the server
    * that should be broadcasted to all connected clients.
    */
   private static class ClientInputThread implements Runnable
   {
      private Socket socket;

      public ClientInputThread(Socket socket)
      {
         this.socket = socket;
      }

      @Override
      public void run()
      {
         try {
            // Gets the output stream
            OutputStream os = socket.getOutputStream();
            PrintStream out = new PrintStream(os, true, "UTF-8");
            Scanner sc = new Scanner(System.in);

            String msg = "";
            while (msg != null)
            {
               msg = sc.nextLine();
               // Sends the message to the server
               out.printf("%s%n", msg);
            }
         } catch (IOException ioe) {
            System.out.println("[CIT] Unexpected IO error occurred.");
            ioe.printStackTrace();
         }
      }
   }

   public static void main(String[] args)
   {
      try
      {
         Socket socket = new Socket("codebank.xyz", 38001);

         OutputStream os = socket.getOutputStream();
         PrintStream out = new PrintStream(os, true, "UTF-8");
         Scanner sc = new Scanner(System.in);
         // User must send a line that will be used as the username
         // If it is in use, the server will respond "Name in use" and
         // close the connection
         System.out.print("Please enter a username: ");
         String username = sc.nextLine();
         out.printf("%s%n", username);
         // Establishing a new listener thread
         new Thread(new ListenThread(socket)).start();
         // Establishing a new user input thread
         new Thread(new ClientInputThread(socket)).start();
      } catch (UnknownHostException uhe) {
         System.out.print("[M] Could not connect to server.");
         uhe.printStackTrace();
      } catch (IOException ioe) {
         System.out.println("[M] Unexpected IO error occurred.");
         ioe.printStackTrace();
      }
   }
}
