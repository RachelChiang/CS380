/*
 * Rachel Chiang
 * CS 380.01 Computer Networks
 * Project 2: 4B/5B NRZI
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * This class handles communication with the server. After connecting with the
 * server, it receives the 64B preamble to establish a baseline. Then, it will
 * accept 320 Bytes from the server, which represent a signal of 32 Bytes that
 * has been encoded with NRZI and 4B/5B. It uses Decoder to decode the encoded
 * 32 Bytes signal. Finally, the client transmits this decoded message back to
 * the server. If the message was decoded properly, the server will respond 1;
 * otherwise, it will respond 0.
 */
public class PhysLayerClient
{
   public static void main(String[] args)
   {
      // Connect to server (port 38002 on codebank.xyz)
      try
      {
         Socket socket = new Socket("codebank.xyz", 38002);
         // Setting up streams
         OutputStream os = socket.getOutputStream();
         InputStream is = socket.getInputStream();

         System.out.println("Connected to server.");
         
         // Establish baseline from the preamble of 64 HI/LO alternating bytes
         double baseline = 0;
         for (int i = 0; i < 64; ++i)
         {
            baseline += is.read();
         }
         baseline = baseline/64.0;
         System.out.println("Baseline established from preamble: " + baseline);

         // Receive 32B of randomly generated data
         // Since it's encoded in 4b/5b,
         //  32B=256b => 320 bits (for the +1b per 4b)
         int[] received = new int[320];
         for (int i = 0; i < 320; ++i)
         {
            received[i] = is.read();
         }
         Decoder d = new Decoder(baseline);
         // Decode from NRZI-encoded signal --> 5b signal
         // Decode from 5b --> 4b
         // Record this decoded 32B to an array of 32B
         byte[] msg = d.decode(received);
         
         // Send the 32B back to the server
         for (int i = 0; i < msg.length; ++i)
         {
            os.write(msg[i]);
         }
         
         // Server responds
         if (is.read() == 1)
         {
            // If it is correct, the server will send a 1
            System.out.println("Response good.");
         }
         else if (is.read() == 0)
         {
            // If it is incorrect, the server will send a 0
            System.out.println("Response bad.");
         }
         socket.close();
      } catch (UnknownHostException uhe) {
         System.out.print("[M] Could not connect to server.");
         uhe.printStackTrace();
      } catch (IOException ioe) {
         System.out.println("[M] Unexpected IO error occurred.");
         ioe.printStackTrace();
      } finally
      {
         System.out.println("Disconnected from server.");
      }
   }
}
