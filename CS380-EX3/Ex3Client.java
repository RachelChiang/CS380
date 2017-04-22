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

/**
 * This program connects to a server codebank.xyz on port 38103. The first Byte
 * that it reads corresponds to the number of Bytes that is to be saved into an
 * array. After reading the whole array, we calculate the checksum of the array
 * of Bytes, using the method {@link #checksum(byte[])}. Once it has been
 * calculated, the client will send the checksum as a sequence of two Bytes back
 * to the server. If the server responds 1, then the checksum was calculated
 * correctly; otherwise, it will respond with a 0.
 */
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
         
         // Receive Byte corresponding to the number of Bytes to receive for
         // the message
         int numToRec = is.read();
         System.out.printf("Reading %d Bytes.\n", numToRec);
         
         // Read in the Bytes and store them in an array
         byte[] received = new byte[numToRec];
         for (int i = 0; i < numToRec; ++i)
         {
            received[i] = (byte) is.read();
         }
         
         // Print out the Bytes received
         System.out.printf("Data received: ");
         for (int i = 0; i < received.length; ++i)
         {
            if ((i)%10 == 0)
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
         for (int i = 1; i >= 0; --i)
         {
            os.write((byte) (checksum >>> (8 * i)));
         }
         
         // Server responds
         if (is.read() == 1)
         {
            // If server responds with 1, the checksum was correct
            System.out.println("Response good.");
         }
         else
         {
            // If server responds with 0, the checksum was incorrect
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
   
   /**
    * This method implements the Internet checksum algorithm given in the EX3
    * project specifications. The algorithm traverses the array b passed in as
    * the argument two Bytes at a time, combining these two Bytes into a 16-bit
    * value, which is added to the sum. Every time the new sum is calculated,
    * it must pass an overflow check. If an overflow occurred, then we logic-and
    * the sum with 0xFFFF (thereby dropping the overflow but keeping the lower-
    * order 16 bits) and then add 1, functioning as a "wrap-around". Finally,
    * when the sum has been calculated, the complement of the sum is calculated
    * and only the rightmost 16 bits are returned.
    * @param b - the Byte array that was received from the server
    * @return - the checksum corresponding to the Byte array b
    */
   public static short checksum(byte[] b)
   {
      long sum = 0;
      int i = 0;
      while (i < b.length)
      {
         // Find the left-side Byte (upper 8 bits)
         long left = (byteToUnsignedLong(b[i++])) << 8;
         // Find the right-side Byte (lower 8 bits), but only if it exists
         long right = 0;
         if (i < b.length)
         {
            // since the server can pass in an odd number of Bytes, we must
            // make sure to only calculate a right-side from the array if
            // it exists.
            right = byteToUnsignedLong(b[i++]);
         }
         // Combine the two.
         long nextValue = left + right;

         // Add this new value to the sum
         sum += nextValue;
         
         // Check for overflow
         if ((sum & 0xFFFF0000) != 0)
         {
            // Carry occurred, must wrap around
            sum &= 0xFFFF;
            ++sum;
         }
      }
      // return the checksum
      return (short) (~(sum & 0xFFFF));
   }
   
   /**
    * This method simply finds the unsigned long value corresponding to the
    * passed in Byte.
    * @param b - a Byte
    * @return - the equivalent unsigned long to b
    */
   private static long byteToUnsignedLong(byte b)
   {
      return (b & 0xFF);
   }
}
