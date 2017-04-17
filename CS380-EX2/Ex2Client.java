/*
 * Rachel Chiang
 * CS 380.01 Computer Networks
 * Exercise 2: CRC Generation on a Sequence of 100 Bytes
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
//--------//--------//--------//--------//--------//--------//--------//--------
/**
 * This class simply connects to codebank.xyz through port number 38102. The
 * server sends 200 Bytes, which are supposed to each by "half bytes" and thus
 * two bytes are to be combined into one byte. After aggregating the 100 bytes,
 * it uses Java's CRC32 and checksum to generate an error-detecting code, which
 * is sent back to the server in a sequence of four bytes. If the server
 * constructed the same bytes as the program, the server will respond with a 1.
 * Otherwise, it responds with a 0.
 */
public class Ex2Client
{
   public static void main(String[] args)
   {
      try
      {
         // Create Socket connection to codebank.xyz port 38102
         Socket socket = new Socket("codebank.xyz", 38102);
         // Setting up streams
         OutputStream os = socket.getOutputStream();
         InputStream is = socket.getInputStream();
         
         System.out.println("Connected to the server.");
         
         // This array will represent the 100 Bytes received from the server.
         byte[] received = new byte[100];
         for (int i = 0; i < received.length; ++i)
         {
            // Read the two bytes from the server.
            int read = is.read();
            // Shift 4 over so the next bit can be simply added to the end.
            read = read << 4;
            int nextRead = is.read();
            
            // Combine the two received pieces (ex. 0x5A)
            read = read + nextRead;
            
            // Finally, store the Byte into the received array
            received[i] = (byte) read;
         }
         
         // Prints out the received bytes.
         System.out.print("Received Bytes:"); 
         for (int i = 0; i < received.length; ++i)
         {
            if ((i)%5 == 0)
            {
               System.out.print("\n  ");
            }
            System.out.print(String.format("%02X", received[i]));
         }
         
         // After constructing the 100B message, use java.util.zip.CRC32
         // to generate a CRC32 error code for the 100B
         Checksum crc32 = new CRC32();
         crc32.update(received, 0, received.length);
         long errorCode = crc32.getValue();
         System.out.println("\nGenerated CRC32: " + String.format("%08X", errorCode));
         
         // Send CRC code as a sequence of 4B back to the server
         for (int i = 3; i >= 0; --i)
         {
            // Can simply send the whole code just shifted, since the write(byte) function
            // actually ignores the 24 high-order bits, thankfully
            os.write((byte) (errorCode >>> 8 * i));
         }
         
         
         // Server responds
         if (is.read() == 1)
         {
            // If server responds with 1, the CRC32 code was correct
            System.out.println("Response good.");
         }
         else
         {
            // If server responds with 0, the CRC32 code was incorrect
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
