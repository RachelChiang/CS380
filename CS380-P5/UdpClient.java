import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/*
 * Rachel Chiang
 * CS 380.01 Computer Networks
 * Project 5: UDP Client with IPv4
 */
public class UdpClient
{

   public static void main(String[] args)
   {
      try
      {
         // Connect to codebank.xyz, port 38005
         Socket socket = new Socket("codebank.xyz", 38005);
         // Setting up streams
         OutputStream os = socket.getOutputStream();
         InputStream is = socket.getInputStream();
         System.out.println("Connected to the server.\n");
         
         // Grabbing IP addresses
         InetAddress srcIP = InetAddress.getLocalHost();
         InetAddress cbxyzIP = InetAddress.getByName(
               new URL("https://codebank.xyz/").getHost());
         byte[] srcIPB = srcIP.getAddress();
         byte[] cbxyzIPB = cbxyzIP.getAddress();
         
         MagicNumberLookup magicNumCheck = new MagicNumberLookup();
         
         // Part 1. Handshake step
         IPv4Generator ipv4 = new IPv4Generator(4, srcIPB, cbxyzIPB);
         byte[] deadbeef = {(byte) (0xDE), (byte) (0xAD), (byte) (0xBE), (byte) (0xEF)};
         byte[] handshake = ipv4.getPacket(deadbeef);
          // Send handshake packet to server
         for (int i = 0; i < handshake.length; ++i)
         {
            os.write(handshake[i]);
         }
         // Print out the server's 4B magic number
         int response = 0;
         for (int i = 0; i < 4; ++i)
         {
            response = response << 8;
            response += is.read();
         }
         System.out.printf("Handshake Response: %s\n", String.format("%08X", response));
         boolean canContinue = magicNumCheck.translate(response);
         if (canContinue)
         {
            // Server sends 2B of raw data which represents a 16-bit unsigned int corresponding to a port number
            byte[] portInBytes = {(byte) is.read(), (byte) is.read()};
            int portNumber = (0xFF & (portInBytes[0]));
            portNumber = portNumber << 8;
            portNumber += (0xFF & (portInBytes[1]));
            //System.out.printf("Port Number Received: %s = %s\n\n", portNumber, String.format("%04X", portNumber));
            System.out.printf("Port Number Received: %s\n\n", portNumber);
            
            // Send UDP packets inside IPv4 packets with the destPort=portNumber
            // Start with 2B packets at first, doubling each time for 12 packets total
            int dataLength = 2;
            
            while (canContinue && dataLength <= 4096)
            {
               System.out.printf("Data Length: %d\n", dataLength);
               
               PacketGenerator packetGen = new PacketGenerator(dataLength, srcIPB, cbxyzIPB, portNumber);
               byte[] packet = packetGen.getPacket();
               
               // Send packet to server
               for (int i = 0; i < packet.length; ++i)
               {
                  os.write(packet[i]);
               }
               
               // Print out the server's 4B magic number
               response = 0;
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
// 1. Handshaking step
//     i. Send a single IPv4 packet without UDP with 4 B of hard-coded data 0xDEADBEEF
//    ii. Server will respond with a 4B code (want 0xCAFEBABE)
//   iii. If you received 0xCAFEBABE, server sends 2B of raw data which represents a
//        16-bit unsigned int corresponding to a port number
// 2. Send UDP packets inside IPv4 packets with destPort=received 2B port number
//      - Send 2B packet at first, doubling each time for 12 packets total
//      - UDP srcPort=anything
//      - Data must be randomized (can use java.util.Random with nextBytes(byte[] b))
// 3. After each UDP packet is sent, Server will respond with a 4B magic number
//     i. Print out Server response (4B magic number)
//    ii. Print out time elapsed in milliseconds since you sent the packet (estimated
//        RTT) for each packet transmitted
// 4. After all 12 packets are sent, print the mean for the RTT for all 12 packets