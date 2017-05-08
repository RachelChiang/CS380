/*
 * Rachel Chiang
 * CS 380.01 Computer Networks
 * Project 4: IPv6
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Similar to project 3, this program generates a packet with an IP header, but
 * in this case, it generates an IPv6 header and then sends it to the server
 * (codebank.xyz through port 38004). It will create the header using the class
 * {@link #IPv6PacketGenerator.java}. The length of the data begins as 2B and
 * doubles every time (for a total of 12 packets with the maximum size as 4096).
 * After writing to the server, the server will respond with a magic number,
 * which will indicates if the packet was correct (0xCAFEBABE), or it will send
 * a code that indicates the first problem in the header that it encountered
 * (see {@link #MagicNumberLookup.java)} for the code translations).
 */
public class Ipv6Client
{
   public static void main(String[] args)
   {
      try
      {
         // Connect to codebank.xyz on port 38004
         Socket socket = new Socket("codebank.xyz", 38004);
         // Setting up streams
         OutputStream os = socket.getOutputStream();
         InputStream is = socket.getInputStream();
         System.out.println("Connected to the server.");
         
         // Grabbing IP addresses
         InetAddress srcIP = InetAddress.getLocalHost();
         InetAddress cbxyzIP = InetAddress.getByName(
               new URL("https://codebank.xyz/").getHost());
         byte[] srcIPB = srcIP.getAddress();
         byte[] cbxyzIPB = cbxyzIP.getAddress();
         
         // Create the class that will help with interpreting the magic number
         MagicNumberLookup magicNumCheck = new MagicNumberLookup();
         
         // Loop and size variables
         boolean canContinue = true;
         int dataLength = 2;
         
         // Send a total of 12 packets, starting at 2B. Double in size each time
         // 2 doubling 12 times is 4096
         while (canContinue && dataLength <= 4096)
         {
            // Identify each packet being sent by the "data length: n"
            System.out.printf("data length: %d\n", dataLength);
            
            // Creation and retrieval of a packet.
            IPv6PacketGenerator packetGen = new IPv6PacketGenerator(dataLength,
                  srcIPB, cbxyzIPB);
            byte[] packet = packetGen.getPacket();
            
            // Send packet to server
            for (int i = 0; i < packet.length; ++i)
            {
               os.write(packet[i]);
            }
            
            // Print out the server's 4B magic number
            int response = 0;
            for (int i = 0; i < 4; ++i)
            {
               response = response << 8;
               response += is.read();
            }
            System.out.printf("Response: %s\n\n", String.format("%08X", response));
            
            // Checks the response and increases the length of the data
            canContinue = magicNumCheck.translate(response);
            dataLength *= 2;
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
