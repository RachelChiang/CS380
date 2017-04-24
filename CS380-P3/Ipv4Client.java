import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Ipv4Client
{
   public static void main(String[] args)
   {
      try
      {
         // Connect to codebank.xyz on port 38003
         Socket socket = new Socket("codebank.xyz", 38003);
         // Setting up streams
         OutputStream os = socket.getOutputStream();
         InputStream is = socket.getInputStream();
         
         System.out.println("Connected to the server.");
         
         PacketGenerator packetGen = new PacketGenerator(2);
         System.out.println("data length: 2");
         packetGen.printBitPacket();
         byte[] packet = packetGen.getPacket();
         for (int i = 0; i < packet.length; ++i)
         {
            System.out.print(String.format("%02X ", packet[i]));
            os.write(packet[i]);
         }
         System.out.println();
         
         InputStreamReader isr = new InputStreamReader(is, "UTF-8");
         BufferedReader br = new BufferedReader(isr);
         String msg = "";
         while (msg != null)
         {
            System.out.println(msg);
            msg = br.readLine();
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
      
      // Send a total of 12 packets, starting at 2B and doubling in size each
      // time
      
      // Identify each packet being sent by the "data length: n"
      // and print out the server's response
   }
}
