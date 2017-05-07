import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.UnknownHostException;

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
         String address = socket.getInetAddress().getHostAddress();
         
         InetAddress srcIP = InetAddress.getLocalHost();
         System.out.println("Src IP: " + srcIP);
         InetAddress cbxyzIP = InetAddress.getByName(new URL("https://codebank.xyz/").getHost());
         System.out.println("Codebank.xyz IP: " + cbxyzIP);
         
         int srcIPInt = getIPInt(srcIP);
         int cbxyzIPInt = getIPInt(cbxyzIP);
         
         System.out.println("Connected to the server " + address);
         
         MagicNumberLookup magicNumCheck = new MagicNumberLookup();
         boolean canContinue = true;
         int dataLength = 2;
         // Send a total of 12 packets, starting at 2B. Double in size each time
         while (canContinue && dataLength <= 4096)
         {
            // TODO: Packet Generator
            IPv6PacketGenerator packetGen = new IPv6PacketGenerator(dataLength);
            // Identify each packet being sent by the "data length: n"
            System.out.printf("data length: %d\n", dataLength);
            byte[] packet = packetGen.getPacket(srcIPInt, cbxyzIPInt);
            
            // Send packet to server
            for (int i = 0; i < packet.length; ++i)
            {
               os.write(packet[i]);
            }
            
            // finally, print out the server's 4B magic number
            int response = 0;
            for (int i = 0; i < 4; ++i)
            {
               response = response << 8;
               response += is.read();
            }
            
            System.out.println("Response: " + String.format("%08X", response));
            
            canContinue = magicNumCheck.translate(response);
            dataLength *= 2;
            
            /*
            if (response == 0xCAFEBABE)
            {
               dataLength *= 2;
               if (dataLength > 4096)
               {
                  canContinue = false;
               }
            }
            else
            {
               canContinue = false;
            }*/
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
   
   public static int getIPInt(InetAddress ip)
   {
      int result = 0;
      byte[] bIP = ip.getAddress();
      for (int i = 0; i < bIP.length; ++i)
      {
         System.out.print(String.format("%02X", bIP[i]));
         result = result << 8;
         result += (bIP[i] & 0xFF);
      }
      System.out.println(" Result: " + result + " = " + String.format("%08X", result));
      return result;
   }
}
