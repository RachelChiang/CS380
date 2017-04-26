public class PacketGenerator implements PacketConstants
{
   private int[] bitPacket;
   private int dataLength;
   
   public PacketGenerator(int dataLength)
   {
      // dataLength is in bytes, so convert to bits.
      // There are 5 rows of 32 bits, ignoring the last row
      // The packet will be an array of 1's and 0's
      // that will eventually be translated into Bytes
      this.dataLength = dataLength;
      bitPacket = new int[(32 * 5)];
      populateBitPacket();
   }
   
   public byte[] getPacket()
   {
      // Each row of the internet header is 4 Bytes long (5 * 4)
      // And then you need to add the dataLength
      byte[] packet = new byte[(5 * 4) + dataLength];
      
      int i = 0;
      int j = 0;
      while (i < bitPacket.length)
      {
         packet[j++] = accumulateBits(i);
         i += 8;
      }
      
      return packet;
   }
   
   private byte accumulateBits(int index)
   {
      int b = 0;
      for (int bitShift = 7; bitShift >= 0; --bitShift)
      {
         b += bitPacket[index++] << bitShift;
      }
      return (byte) (b & 0xFF);
   }
   
   private void populateBitPacket()
   {
      // Version [4b] - indicates format of internet header; IPv4 --> value = 4
      fill(VERSION_END, VERSION_SIZE, 4);
      // IHL [4b]: Header length in 32b words --> value = 5
      fill(IHL_END, IHL_SIZE, 5);
      // Length [16b]: datagram in octets --> value = dataLength + IH[bits]/octet = dataLength + 160/8
      fill(LENGTH_END, LENGTH_SIZE, (dataLength + (160 / 8)));
      // Flags [3b]: no fragmentation --> value = 010[bin] = 2
      // b0 must be 0; b1 = 1 (don't fragment), b1 = 0 (may fragment);
      // b2 = 0 (last fragment), b2 = 1 (more fragments)
      fill(FLAGS_END, FLAGS_SIZE, 2);
      // TTL [8b]: max time datagram can exist in internet system --> value = 50
      fill(TTL_END, TTL_SIZE, 50);
      // Protocol [8b]: next level protocol used in data portion, assume TCP --> value = TCP = 6
      fill(PROTOCOL_END, PROTOCOL_SIZE, 6);
      // Addresses [32b]: Src = choice addr, Dest = server IP = 0x3425589A
      fill(SRCADDR_END, ADDR_SIZE, 0xC0A801E9);
      fill(DESTADDR_END, ADDR_SIZE, 0x3425589A);
      
      // Checksum
      fillChecksum();
   }
   
   private void fill(int endIndex, int fieldSize, int value)
   {
      for (int i = 0; i < fieldSize; ++i)
      {
         bitPacket[endIndex - i] = 0x1 & (value >>> i);
      }
   }
   
   private void fillChecksum()
   {
      // Checksum - (16b) Checksum for the header only.
      
      // if checksum is on the whole header
      byte[] header = new byte[20];
      int i = 0;
      int j = 0;
      while (i < 160)
      {
         header[j++] = accumulateBits(i);
         i += 8;
      }
      
      short checksum = checksum(header);
      for (int idx = 0; idx < 16; ++idx)
      {
         bitPacket[CHECKSUM_END - idx] = 0x1 & (checksum >>> idx);
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
   private short checksum(byte[] b)
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
