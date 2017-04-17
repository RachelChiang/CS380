import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class Ex2Client
{
   public static void main(String[] args)
   {
      try
      {
         // Create Socket connection to codebank.xyz port 38102
         Socket socket = new Socket("codebank.xyz", 38102);
         OutputStream os = socket.getOutputStream();
         PrintStream out = new PrintStream(os, true, "UTF-8");
         // Gets the input stream for the socket
         InputStream is = socket.getInputStream();
         
         System.out.println("Connected to the server.");
         
         byte[] received = new byte[100];
         for (int i = 0; i < received.length; ++i)
         {
            // Combine the two received pieces (ex. 0x5A)
            int read = is.read();
            //System.out.print(Integer.toHexString(read) + " shifted 4 = ");
            read = read << 4;
            //System.out.print(Integer.toHexString(read) + "\n");
            
            int nextRead = is.read();
            //System.out.println("Next Half: " + Integer.toHexString(nextRead));
            read = read + nextRead;
            //System.out.println("Together: " + Integer.toHexString(read));
            received[i] = (byte) read;
            //System.out.println("Byte Array Element: " + (Integer.toHexString(received[i] & 0xFF)));
         }
         
         System.out.print("Received Bytes:"); 
         for (int i = 0; i < received.length; ++i)
         {
            if ((i)%5 == 0)
            {
               System.out.print("\n  ");
            }
            System.out.print(String.format("%02X", received[i]));
         }
         
         Checksum checksum = new CRC32();
         checksum.update(received, 0, received.length);
         long errorCode = checksum.getValue();
         
         System.out.println("\nGenerated CRC32: " + String.format("%08X", errorCode));
         /*
          * Writes the specified byte to this output stream. The general contract for
          * write is that one byte is written to the output stream. The byte to be 
          * written is the eight low-order bits of the argument b. The 24 high-order 
          * bits of b are ignored. 
          */
         for (int i = 3; i >= 0; --i)
         {
            os.write((byte) (errorCode >>> 8 * i));
         }
         if (is.read() == 1)
         {
            System.out.println("Response good.");
         }
         else if (is.read() == 0)
         {
            System.out.println("Response bad.");
         }
         else
         {
            System.out.println("No response.");
         }
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
      
      // After constructing the 100B message, use java.util.zip.CRC32
      // to generate a CRC32 error code for the 100B
      // Send CRC code as a sequence of 4B back to the server
      // If server responds with 1, the CRC32 code was correct
      // If server responds with 0, the CRC32 code was incorrect
   }
}
