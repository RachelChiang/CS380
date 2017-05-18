
public class IPv4Generator extends HeaderGenerator implements IPv4Constants
{
   /**
    * This is an array that will be filled with 0's or 1's and represents the
    * Internet header. I chose to fill it in binary because it just seemed more
    * straightforward (to me) to handle the data as individual bits. Perhaps to
    * make generating the packet faster (and more space efficient), it would be
    * better to forgo generating a binary header and simply directly populate
    * the whole packet. However, if it is like this, it makes it convenient,
    * readable, and modifiable to generalize filling the header (see {@link
    * #fill(int, int, int)}. This array is initialized in the {@link
    * #PacketGenerator(int)} with a size of 160 because there are 5 rows of 32
    * bits in the header (and we are ignoring the row for Options and Padding).
    * The default values of an array are initially zero, so I chose to ignore
    * zeroing out certain fields. bitHeader is populated in {@link
    * #populateIHeader()} and its values are eventually copied over as Bytes to
    * the actual full Byte packet in {@link #getPacket()}.
    */
   private byte[] bitHeader;
   
   /**
    * This simply holds the length of the data portion of the packet in Bytes.
    */
   private int dataLength;
   
   public IPv4Generator(int dataLength, byte[] srcAddr, byte[] destAddr)
   {
      this.dataLength = dataLength;
      bitHeader = new byte[HEADER_SIZE];
      populateIHeader(srcAddr, destAddr);
   }
   
   public byte[] getPacket(byte[] data)
   {
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
      
      int j = 0;
      for (int i = header.length; i < packet.length; ++i)
      {
         packet[i] = data[j++];
      }
      
      //printPacket(packet);
      
      return packet;
   }
   
   private void printPacket(byte[] packet)
   {
      System.out.print("IPv4 Packet: ");
      int c = 0;
      for (int i = 0; i < packet.length; ++i)
      {
         
         System.out.print(String.format("%02X", packet[i]));
         ++c;
         if (c == 4)
         {
            System.out.print(" ");
            c = 0;
         }
      }
      System.out.println();
   }
   
   /**
    * This method is basically just the overhead. It calls a different method
    * {@link #fill(int, int, int)}, which handles the bulk of the work. Since
    * there are a lot of constants, I decided to make a separate Interface
    * {@link #PacketConstants.java} to hold onto them. I considered using a
    * nested enum or some sort of data structure to hold the indices, sizes, and
    * values, but I think the Interface approach is the easiest to read, access,
    * and modify. However, it is notably appealing to have an enum or a vector
    * of triples because this method could simply be one for loop instead of 8
    * individual {@link #fill(int, int, int)} calls, but the checksum might
    * have to be individually called anyway since its value is not constant.
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
      //    value = dataLength + IH[bits]/octet = dataLength + 160/8
      fill(LENGTH_END, LENGTH_SIZE, (dataLength + (160 / 8)));
      
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
      
      // Addresses [32b]: Src = choice addr, Dest = server IP = 0x3425589A
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
    * This method simply converts the {@link #bitHeader} into a true Byte array.
    * That is, instead of being filled with 1-bit values, it has 8-bit values
    * that represent the original 1-bit values. It uses {@link
    * #accumulateBits(int)} for help.
    * @return - the header, as actual Bytes.
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
