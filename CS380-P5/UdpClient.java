/*
 * Rachel Chiang
 * CS 380.01 Computer Networks
 * Project 5: UDP Client with IPv4
 */
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This program creates packets with an IPv4 header with UDP as its data and
 * random bytes as the UDP's data. The IPv4 header is constructed approximately
 * identically to the one in project 3; notably, the protocol is set to UDP in-
 * stead of TCP. The following outlines the overall procedure:
 * 1. Handshaking
 *      i. The client will send a single IPv4 packet (without UDP), with 4 Bytes
 *         of hard-coded data 0xDEADBEEF.
 *     ii. The server will respond with a 4B code.
 *    iii. If the server sends 0xCAFEBABE, the server will send 2 Bytes of raw
 *         data representing a 16-bit unsigned integer corresponding to a port
 *         number, which will be the destination port of the UDP.
 * 2. The client will send UDP packets inside IPv4 packets, beginning with
 *    the UDP data containing 2B of random bytes, doubling each time for
 *    a total of 12 packets.
 * 3. After each UDP packet is sent, the server will respond with a 4 Byte magic
 *    number, which will be printed out along with the time elapsed in milli-
 *    seconds since the packet was sent for each packet transmitted.
 * 4. After all 12 packets have been sent, the mean for the RTT will be printed.
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
         
         // Lookup table
         MagicNumberLookup magicNumCheck = new MagicNumberLookup();
         
         // Part 1. Handshake step
         IPv4Generator ipv4 = new IPv4Generator(4, srcIPB, cbxyzIPB);
         byte[] deadbeef = {
               (byte) (0xDE), (byte) (0xAD), (byte) (0xBE), (byte) (0xEF)
               };
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
         System.out.printf("Handshake Response: %s\n",
               String.format("%08X", response));
         boolean canContinue = magicNumCheck.translate(response);
         
         // If CAFEBEEF was received
         if (canContinue)
         {
            // Server sends 2B of raw data which represents a port number
            byte[] portInBytes = {(byte) is.read(), (byte) is.read()};
            int portNumber = (0xFF & (portInBytes[0]));
            portNumber = portNumber << 8;
            portNumber += (0xFF & (portInBytes[1]));
            System.out.printf("Port Number Received: %s\n\n", portNumber);
            
            // Send UDP packets inside IPv4 packets
            // Start with 2B packets at first, doubling each time for 12
            //  packets total
            // Keep track of the RTTs
            int dataLength = 2;
            double averageRTT = 0;
            
            while (canContinue && dataLength <= 4096)
            {
               response = 0;
               System.out.printf("Sending packet with %d bytes of data\n",
                     dataLength);
               
               // Create the packet
               PacketGenerator packetGen = new PacketGenerator(
                     dataLength, srcIPB, cbxyzIPB, portNumber);
               byte[] packet = packetGen.getPacket();
               
               // Send the packet to the server
               long startTime = System.nanoTime();
               for (int i = 0; i < packet.length; ++i)
               {
                  os.write(packet[i]);
               }
               
               // Read and print out the server's 4B magic number
               for (int i = 0; i < 4; ++i)
               {
                  response = response << 8;
                  response += is.read();
               }
               // Calculate the time elapsed
               double timeElapsed = (System.nanoTime() - startTime)/1000000.0;
               
               // Print out the response and the time elapsed
               System.out.printf("Response: %s\n",
                     String.format("0x%08X", response));
               System.out.printf("RTT: %.2f ms\n\n", timeElapsed);
               
               // Check the response and increase the length of the data and
               //  add the elapsed time to the averageRTT, which will be divided
               // below
               canContinue = magicNumCheck.translate(response);
               dataLength *= 2;
               averageRTT += timeElapsed;
            }
            
            // The mean RTT
            System.out.printf("Average RTT: %.2f ms\n\n", (averageRTT/12));
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