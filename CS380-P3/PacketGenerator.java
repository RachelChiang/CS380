/*
 * Rachel Chiang
 * CS 380.01 Computer Networks
 * Project 3: IPv4
 */

/**
 * This class creates packets. Since the data portion of the packet is arbitrary
 * I chose to leave it as filled with all zeroes. It could have been randomized,
 * but running the program is faster without filling the data. Otherwise, this
 * class works by accepting an integer to save into {@link #dataLength}, and
 * from there it jumps straight into creating the {@link #bitHeader}. Once the
 * constructor has been called, it will {@link #populateIHeader()}, which
 * handles creating only the header and is represented as an array of bytes
 * which can only be 0's or 1's. To collect the entire Byte packet (which will
 * contain both the Internet header and the data portion), call the method
 * {@link #getPacket()}.
 */
public class PacketGenerator implements PacketConstants
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
   
   /**
    * The constructor initializes its fields {@link #dataLength} and {@link
    * #bitHeader}. Then, it calls {@link #populateIHeader()} to fill the
    * {@link #bitHeader}.
    * @param dataLength - The size of the data portion of the packet in bytes.
    */
   public PacketGenerator(int dataLength)
   {
      this.dataLength = dataLength;
      bitHeader = new byte[(32 * 5)];
      populateIHeader();
   }
   
   /**
    * This method is to be called outside of the class. It creates the proper
    * packet by pulling together the {@link #bitHeader} and using the helper
    * {@link #convertHeaderToBytes()}.
    * @return It returns the whole packet, which has a size equal to the
    *    Internet header plus the {@link #dataLength}. The size of the Internet
    *    header must be found in Bytes; each of the 5 rows of the header is 4
    *    Bytes long. Thus we derive the packet.length = (5 * 4) + dataLength. 
    */
   public byte[] getPacket()
   {
      byte[] packet = new byte[(5 * 4) + dataLength];
      
      byte[] header = convertHeaderToBytes();
      
      for (int i = 0; i < header.length; ++i)
      {
         packet[i] = header[i];
      }
      
      return packet;
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
   private void populateIHeader()
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
      //    Assume TCP --> value = TCP = 6
      fill(PROTOCOL_END, PROTOCOL_SIZE, 6);
      
      // Addresses [32b]: Src = choice addr, Dest = server IP = 0x3425589A
      fill(SRCADDR_END, ADDR_SIZE, 0xC0A801E9);
      fill(DESTADDR_END, ADDR_SIZE, 0x3425589A);
      
      // Checksum [16b]: checksum for the header
      //    The value is not constant --> value = checksum()
      fill(CHECKSUM_END, CHECKSUM_SIZE, checksum());
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
   private short checksum()
   {
      // Collect the bits from the bitHeader into a Byte array that we can
      // operate on for the checksum. Perhaps holding the bitHeader as a
      // byteHeader could reduce redundancy, seeing how this is done again
      // in getPacket()...
      byte[] header = convertHeaderToBytes();
      
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

   /**
    * This method simply converts the {@link #bitHeader} into a true Byte array.
    * That is, instead of being filled with 1-bit values, it has 8-bit values
    * that represent the original 1-bit values. It uses {@link
    * #accumulateBits(int)} for help.
    * @return - the header, as actual Bytes.
    */
   private byte[] convertHeaderToBytes()
   {
      byte[] header = new byte[20];
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
