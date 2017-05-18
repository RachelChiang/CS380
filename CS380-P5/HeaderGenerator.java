
public abstract class HeaderGenerator
{
   /**
    * This method implements the Internet checksum algorithm. It is almost
    * identical to the one used in Exercise 3, but I removed the parameter.
    * Before beginning the algorithm, it generates the header in Bytes using
    * {@link #convertHeaderToBytes()}, which is necessary because the {@link
    * #bitHeader} is filled with bits, despite its data type. The algorithm
    * traverses the newly-created header two Bytes at a time, combining these
    * two Bytes into a 16-bit value, which is added to the sum. Every time the
    * new sum is calculated, it must pass an overflow check. If an overflow
    * occurred, then we logical-and the sum with 0xFFFF (thereby dropping the
    * overflow but keeping the lower-order 16 bits) and then add 1, functioning
    * as a "wrap-around". Finally, when the sum has been calculated, the
    * complement of the sum is calculated and only the rightmost 16 bits are
    * returned.
    * @return - the checksum corresponding to the Byte array header
    */
   public short checksum(byte[] header)
   {
      // Here we begin the actual algorithm.
      long sum = 0;
      int i = 0;
      while (i < header.length)
      {
         // Find the left-side Byte (upper 8 bits)
         long left = (byteToUnsignedLong(header[i++])) << 8;
         // Find the right-side Byte (lower 8 bits), but only if it exists
         long right = 0;
         if (i < header.length)
         {
            // since the server can pass in an odd number of Bytes, we must
            // make sure to only calculate a right-side from the array if
            // it exists.
            right = byteToUnsignedLong(header[i++]);
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
      // System.out.println(String.format(" Checksum: %04X", (short) (~(sum & 0xFFFF))));
      return (short) (~(sum & 0xFFFF));
   }
   
   /**
    * This method simply finds the unsigned long value corresponding to the
    * passed in Byte.
    * @param b - a Byte
    * @return - the equivalent unsigned long to b
    */
   private long byteToUnsignedLong(byte b)
   {
      return (b & 0xFF);
   }
   
   public int byteAddrToInt(byte[] address)
   {
      int result = 0;
      for (int i = 0; i < address.length; ++i)
      {
         result = result << 8;
         result += (int) (0xFF & address[i]);
      }
      //System.out.println(String.format("%08X", result));
      return result;
   }
}
