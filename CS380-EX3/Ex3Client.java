/*
 * Rachel Chiang
 * CS 380.01 Computer Networks
 * Exercise 3: Generating the Checksum
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Ex3Client
{
   public static void main(String[] args)
   {
      // Socket to codebank.xyz on port 38103
      try
      {
         Socket socket = new Socket("codebank.xyz", 38103);
         // Setting up streams
         OutputStream os = socket.getOutputStream();
         InputStream is = socket.getInputStream();

         System.out.println("Connected to server.");
         
         // Receive Byte corresponding to the number of Bytes to receive for the message
         int numToRec = is.read();
         System.out.printf("Reading %d Bytes.\n", numToRec);
         byte[] received = new byte[numToRec];
         
         // Read in the Bytes and store them in an array
         for (int i = 0; i < numToRec; ++i)
         {
            received[i] = (byte) is.read();
         }
         
         // TEST-------------------------------------
         //byte[] received = {(byte) 240, (byte) 143, (byte) 120, (byte) 7, (byte) 35, (byte) 108};
         // TEST 2-----------------------------------
        /* byte[] received = {(byte) 90, (byte) 220, (byte) 2, (byte) 180, (byte) 211, (byte) 73, (byte) 41, (byte) 105, (byte) 9, (byte) 118,
               (byte) 183, (byte) 51, (byte) 146, (byte) 77, (byte) 134, (byte) 25, (byte) 30, (byte) 175, (byte) 79, (byte) 184,
               (byte) 239, (byte) 68, (byte) 130, (byte) 39, (byte) 66, (byte) 119, (byte) 227, (byte) 211, (byte) 79, (byte) 245,
               (byte) 156, (byte) 168, (byte) 121, (byte) 93, (byte) 135, (byte) 166, (byte) 66, (byte) 40, (byte) 77};*/
         
         // Print out received Bytes
         System.out.printf("Data received: ");
         for (int i = 0; i < received.length; ++i)
         {
            if ((i)%5 == 0)
            {
               System.out.print("\n  ");
            }
            System.out.print(String.format("%02X", received[i]));
         }
         System.out.println();
         
         // Find checksum of these Bytes
         short checksum = checksum(received);
         System.out.printf("Checksum calculated: 0x%s\n", String.format("%04X", checksum));
         // Send the server the checksum represented as two Bytes
         System.out.print(String.format("%04X", checksum >>> 8) + " ");
         System.out.print(String.format("%04X", checksum) + "\n");
         os.write((byte) (checksum >>> 8));
         os.write((byte) checksum);
         
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
   
   public static short checksum(byte[] b)
   {
      // This method will implement the Internet checksum algorithm that is also used
      // in project 3 for IPv4 packets
      /*
       * The algorithm maintains a 32 bit number as the sum and goes through the byte
       * array two bytes at a time, forms a 16-bit number out of each pair of bytes
       * and adds to the sum. After each time it adds, it checks for overflow.
       * If overflow occurs, it is cleared and added back in to the sum
       * (acting like a wrap-around).
       * Finally, when the sum is calculated we perform ones’ complement and return
       * the rightmost 16 bits of the sum.
       */
      /*
       * Data received: F0 8F 78 07 23 6C Checksum calculated: 0x73FC
       */
      long sum = 0;
      // 28D
      for (int i = 0; i < b.length;)
      {
         System.out.println(i + ":");
         //sum += b[i];
         long left = (byteToUnsignedLong(b[i++])) << 8;
         long right = 0;
         if (i < b.length)
         {
            right = byteToUnsignedLong(b[i++]);
         }
         //long nextValue = (byteToUnsignedLong(b[i++]) << 8) + (byteToUnsignedLong(b[i++]));
         long nextValue = left + right;
         sum += nextValue;
         //sum += byteToUnsignedLong(b[i]);
         System.out.println("  Intermediate sum: " + sum);
         if ((sum & 0xFFFF0000) != 0)
         {
            // Carry occurred, must wrap around
            sum &= 0xFFFF;
            ++sum;
            System.out.println("  Carry occurred.");
         }
         System.out.println("  Sum: " + sum);
      }
      System.out.println("Final sum: " + sum + " = " + String.format("%04X", sum));
      System.out.println("D&C: " + String.format("%04X", ~(sum & 0xFFFF)));
      /*Byte left = (byte) (sum >>> 8);
      Byte right = (byte) sum;
      long result = (byteToUnsignedLong(left) << 8) + (byteToUnsignedLong(right));
      System.out.println("Unsigned long: " + result + " = " + String.format("%04X", result));
      System.out.println("Left: " + String.format("%02X", left));
      System.out.println("Right: " + String.format("%02X", right));*/
      return (short) (~(sum & 0xFFFF));
   }
   
   private static long byteToUnsignedLong(byte b)
   {
      return (b & 0xFF);
   }
}
