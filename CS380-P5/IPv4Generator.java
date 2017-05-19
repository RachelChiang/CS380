/*
 * Rachel Chiang
 * CS 380.01 Computer Networks
 * Project 5: UDP Client with IPv4
 */

/**
 * This class generates the IPv4 portion of the packet. Its constants can be
 * found in the interface, {@link #IPv4Constants}. The data portion of the
 * packet may vary, hence the {@link #getPacket(byte[])}, which is different
 * from my previous implementation of IPv4. Otherwise, this class has been left
 * relatively untouched, other than organizational changes.
 */
public class IPv4Generator extends HeaderGenerator implements IPv4Constants
{
   /**
    * This is an array that will be filled with 0's or 1's and represents the
    * Internet header. It is filled in {@link #fill(int, int, int)}. This array
    * is initialized in the {@link #PacketGenerator(int)}. Its values are even-
    * tually copied over as Bytes in {@link #getPacket()}.
    */
   private byte[] bitHeader;
   
   /**
    * This simply holds the length of the data portion of the packet in Bytes.
    */
   private int dataLength;
   
   /**
    * The constructor, which calls on {@link #populateIHeader(byte[], byte[])}.
    * @param dataLength - The length of the data portion in bytes
    * @param srcAddr - The byte array representing the source address
    * @param destAddr - The byte array representing the destination address
    */
   public IPv4Generator(int dataLength, byte[] srcAddr, byte[] destAddr)
   {
      this.dataLength = dataLength;
      bitHeader = new byte[HEADER_SIZE];
      populateIHeader(srcAddr, destAddr);
   }
   
   /**
    * This method is to be called outside of the class. It creates the proper
    * packet by pulling together the {@link #bitHeader} and using the helper
    * {@link #convertHeaderToBytes()}.
    * @return It returns the whole packet, which has a size equal to the
    *    Internet header plus the {@link #dataLength}. 
    */
   public byte[] getPacket(byte[] data)
   {
      // Transfer the bitHeader information to the full byte packet
      byte[] packet = new byte[(HEADER_SIZE/8) + dataLength];
      
      byte[] header = convertHeaderToBytes();
      
      for (int i = 0; i < header.length; ++i)
      {
         packet[i] = header[i];
      }
      
      if (data.length != dataLength)
      {
         System.out.println("Length of provided data incompatible.");
         return null;
      }
      
      // Add the data
      int j = 0;
      for (int i = header.length; i < packet.length; ++i)
      {
         packet[i] = data[j++];
      }
      
      return packet;
   }
   
   /**
    * This method deals with overhead. It calls a different method {@link
    * #fill(int, int, int)} to actually fill the IPv4 segments.
    */
   private void populateIHeader(byte[] srcAddr, byte[] destAddr)
   {
      // Version [4b] - indicates format of internet header
      //    IPv4 --> value = 4
      fill(VERSION_END, VERSION_SIZE, 4);
      
      // IHL [4b]: Header length in 32b words
      //    5 rows of 32 bits --> value = 5
      fill(IHL_END, IHL_SIZE, 5);
      
      // Length [16b]: size of datagram in octets
      //    IH[bits] = 5 rows of 32 bits = 160
      //    value = dataLength + headerSize/octet
      fill(LENGTH_END, LENGTH_SIZE, (dataLength + (HEADER_SIZE / 8)));
      
      // Flags [3b]: no fragmentation
      //    value = b0b1b2[bin]
      //    b0 must be 0;
      //    b1 = 1 (don't fragment), b1 = 0 (may fragment);
      //    b2 = 0 (last fragment), b2 = 1 (more fragments)
      //    value = 010[bin] = 2
      fill(FLAGS_END, FLAGS_SIZE, 2);
      
      // TTL [8b]: max time datagram can exist in Internet system
      //    Assume 50s --> value = 50
      fill(TTL_END, TTL_SIZE, 50);
      
      // Protocol [8b]: next level protocol used in data portion
      //    Assume UDP --> value = UDP = 17
      fill(PROTOCOL_END, PROTOCOL_SIZE, 17);
      
      // Addresses [32b]: Src = choice addr, Dest = server
      fill(SRCADDR_END, ADDR_SIZE, byteAddrToInt(srcAddr));
      fill(DESTADDR_END, ADDR_SIZE, byteAddrToInt(destAddr));
      
      // Checksum [16b]: checksum for the header
      //    The value is not constant --> value = checksum()
      fill(CHECKSUM_END, CHECKSUM_SIZE, checksum(convertHeaderToBytes()));
   }

   /**
    * This method fills each field of the {@link #bitHeader} in one cute little
    * for-loop. As stated previously, it is filled with 0's and 1's. It is
    * called only in {@link #populateIHeader()}.
    * @param endIndex - We fill backwards because I like incremental for-loops.
    * @param fieldSize - This denotes how many bits long each field in the
    *    header is.
    * @param value - This is the value to be stored in the specific field.
    */
   private void fill(int endIndex, int fieldSize, int value)
   {
      for (int i = 0; i < fieldSize; ++i)
      {
         bitHeader[endIndex - i] = (byte) (0x1 & (value >>> i));
      }
   }
   
   /**
    * This method simply converts the {@link #bitHeader} into a Byte array. That
    * is, instead of being filled with 1-bit values, it has 8-bit values that
    * represent the original 1-bit values. It uses {@link #accumulateBits(int)}
    * for help.
    * @return - The header, in Bytes.
    */
   private byte[] convertHeaderToBytes()
   {
      byte[] header = new byte[HEADER_SIZE / 8];
      int i = 0;
      int j = 0;
      while (i < bitHeader.length)
      {
         header[j++] = accumulateBits(i);
         i += 8;
      }
      
      return header;
   }

   /**
    * This simply method accumulates bits into Bytes only from the {@link
    * #bitHeader}.
    * @param index - Requires the index to know where to start making Bytes from
    * @return - Returns the Byte.
    */
   private byte accumulateBits(int index)
   {
      int b = 0;
      for (int bitShift = 7; bitShift >= 0; --bitShift)
      {
         b += bitHeader[index++] << bitShift;
      }
      return (byte) (b & 0xFF);
   }
}
