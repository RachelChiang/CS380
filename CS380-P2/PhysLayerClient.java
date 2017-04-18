import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

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
            //if (i+1 == 64)
            //{
            //   int last = is.read();
            //   System.out.println("Last: " + last);
            //   baseline += last;
            //}
            //else
            //{
               baseline += is.read();               
            //}
         }
         baseline = baseline/64.0;
         System.out.println("Baseline established from preamble: " + baseline);
         
         // Receive 32B of randomly generated data
         // Since it's encoded in 4b/5b, 32B=256b => 320 bits (for the +1b per 4b)
         int[] received = new int[320];
         for (int i = 0; i < 320; ++i)
         {
            received[i] = is.read();
            //System.out.print(" " + String.format("%02X", received[i]));
            System.out.print("  " + received[i]);
         }
         System.out.println();
         Decoder d = new Decoder(received, baseline);
         // Decode from NRZI-encoded signal --> 5b signal
         // Decode from 5b --> 4b
         // Record this decoded 32B to an array of 32B
         byte[] msg = d.decode();
         
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
