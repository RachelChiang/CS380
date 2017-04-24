import java.net.InetAddress;

public class PacketGenerator implements PacketConstants
{
   private int[] bitPacket;
   private int dataLength;
   
   public PacketGenerator(int dataLength)
   {
      // dataLength is in bytes, so conver to bits.
      // There are 5 rows of 32 bits, ignoring the last row
      // The packet will be an array of 1's and 0's
      // that will eventually be translated into Bytes
      this.dataLength = dataLength;
      bitPacket = new int[(this.dataLength * 8) + (32 * 5)];
      for (int i = 0; i < bitPacket.length; ++i)
      {
         bitPacket[i] = 0;
      }
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
      fillVersion();
      fillIHL();
      fillLength();
      fillFlags();
      fillTTL();
      fillProtocol();
      fillAddresses();
      fillData();
      //fillLeftovers();
      fillChecksum();
   }
   
   private void fillVersion()
   {
      // Version - (4b), indicates format of internet header
      // Version is 4 (0100)
      /*for (int i = 3; i >= 0; --i)
      {
         bitPacket[VERSION_START + i] = 0x1 & (4 >>> i);
      }*/
      /*bitPacket[3] = 0; // 010X
      bitPacket[2] = 0; // 01X0
      bitPacket[1] = 1; // 0X00
      bitPacket[0] = 0; // X100*/
      for (int i = 0; i < 4; ++i)
      {
         bitPacket[VERSION_END - i] = 0x1 & (4 >>> i);
      }
   }
   
   private void fillIHL()
   {
      // HLen (IHL) - (4b), length of the internet header in 32b words;
      // points to beginning of data. (Note min value = 5)
      // TODO: Double-check this is right
      /*for (int i = 3; i >= 0; --i)
      {
         bitPacket[IHL_START + i] = 0x1 & (5 >>> i);
      }*/
      
      for (int i = 0; i < 4; ++i)
      {
         bitPacket[IHL_END - i] = 0x1 & (5 >>> i);
      }
   }
   
   private void fillLength()
   {
      // Length - (16b), length of datagram, measured in octets, including
      // internet header and data
      // TODO: Double-check this is right
      /*for (int i = 15; i >= 0; --i)
      {
         bitPacket[LENGTH_START + i] = 0x1 & (dataLength >>> i);
      }*/
      // 160 bits in the IHL divided into octets (by 8), plus the dataLength
      // which is already measured in octets
      for (int i = 0; i < 16; ++i)
      {
         bitPacket[LENGTH_END - i] = 0x1 & ((dataLength + (160 / 8)) >>> i);
         //bitPacket[LENGTH_END - i] = 0x1 & ((dataLength) >>> i);
      }
   }
   
   private void fillFlags()
   {
      // Flags (assuming no fragmentation) - (3b) b0b1b2
      // b0 must be 0; b1 = 1 (don't fragment), b1 = 0 (may fragment);
      //  b2 = 0 (last fragment), b2 = 1 (more fragments)
      // TODO: Double-check this is right
      bitPacket[FLAGS_START] = 0;
      bitPacket[FLAGS_START + 1] = 1;
      bitPacket[FLAGS_START + 2] = 0;
   }
   
   private void fillTTL()
   {
      // TTL (assuming every packet has TTL=50) - (8b) Maximum time the datagram
      // is allowed to remain in the internet system. If this field = 0, datagram
      // must be destroyed. Modified in internet header processing. Time is
      // measured in units of seconds. TTL is an upper bound on the time a
      // datagram may exist
      /*for (int i = 7; i >= 0; --i)
      {
         bitPacket[TTL_START + i] = 0x1 & (50 >>> i);
      }*/
      
      for (int i = 0; i < 8; ++i)
      {
         bitPacket[TTL_END - i] = 0x1 & (50 >>> i);
      }
   }
   
   private void fillProtocol()
   {
      // Protocol (assuming TCP) - (8b) This field indicates the next level
      // protocol used in the data portion of the internet diagram. TCP=6
      /*for (int i = 7; i >= 0; --i)
      {
         bitPacket[PROTOCOL_START + i] = 0x1 & (6 >>> i);
      }*/
      for (int i = 0; i < 8; ++i)
      {
         bitPacket[PROTOCOL_END - i] = 0x1 & (6 >>> i);
      }
   }
   
   private void fillChecksum()
   {
      // Checksum - (16b) Checksum for the header only.
      // TODO: Fill it in properly.
      /*for (int i = 15; i >= 0; --i)
      {
         bitPacket[CHECKSUM_START + i] = 2;
      }*/
      
      /*
      // if checksum is on prior header
      byte[] header = new byte[10];
      int i = 0;
      int j = 0;
      while (i < CHECKSUM_START)
      {
         header[j++] = accumulateBits(i);
         i += 8;
      }
      */
      ///*
      // if checksum is on the whole header
      byte[] header = new byte[20];
      int i = 0;
      int j = 0;
      while (i < 160)
      {
         header[j++] = accumulateBits(i);
         i += 8;
      }
      //*/
      
      short checksum = checksum(header);
      
      for (int idx = 0; idx < 15; ++idx)
      {
         bitPacket[CHECKSUM_END - idx] = 0x1 & (checksum >>> idx);
      }
   }
   
   private void fillAddresses()
   {
      // TODO: Fill it in properly.
      // SourceAddr (with IP address of your choice) - (32b)
      // DestinationAddr (with IP address of the server) - (32b)
      for (int i = 0; i < 32; ++i)
      {
         bitPacket[SRCADDR_END - i] = 0x1 & (0xC0A801E9 >>> i); // but not this one
         //bitPacket[SRCADDR_END - i] = 0x1 & (0xAAAAAAAA >>> i); // why does this work aaaa
         //bitPacket[DESTADDR_END - i] = 0;
         bitPacket[DESTADDR_END - i] = 0x1 & (0x3425589A >>> i);
      }
   }
   
   private void fillData()
   {
      // Data (using zeros or rand data)
      // TODO: Maybe fill it in with random data if you want
      for (int i = DATA_START; i < bitPacket.length; ++i)
      {
         bitPacket[i] = 0;
      }
   }
   
   private void fillLeftovers()
   {
      // Do Not Implement: TOS (8b), Identification (16b), Offset (13b)
      // Fill with 0's
      for (int i = TOS_START; i < TOS_START + 8; ++i)
      {
         bitPacket[i] = 0;
      }
      
      for (int i = IDEN_START; i < IDEN_START + 16; ++i)
      {
         bitPacket[i] = 0;
      }
      
      for (int i = OFFSET_START; i < OFFSET_START + 13; ++i)
      {
         bitPacket[i] = 0;
      }
   }
   
   public void printBitPacket()
   {
      for (int i = 0; i < bitPacket.length; ++i)
      {
         if ((i%32) == 0)
         {
            System.out.println();
         }
         System.out.print(bitPacket[i] + " ");
      }
      System.out.println();
   }
   
   // TODO: Fix the comments
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
      
      short result = (short) (~(sum & 0xFFFF));
      System.out.println("checksum: " + result + " = " + String.format("%02X", result));
      
      // return the checksum
      return (short) (~(sum & 0xFFFF));
   }
   // TODO: fix comments here
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
