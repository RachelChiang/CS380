/*
 * Rachel Chiang
 * CS 380.01 Computer Networks
 * Project 4: IPv6
 */

/**
 * This class creates packets. Many of the constants can be found in the inter-
 * face, {@link #IPv6Constants}. Note, the data portion of the packet is arbit-
 * rary so it has been left as all zeroes.
 * This class must be initialized with an integer for {@link #dataLength}, and
 * then it will create a {@link #nibbleHeader}. To acquire the entire packet,
 * call {@link #getPacket(byte[], byte[])}.  
 */
public class IPv6PacketGenerator implements IPv6Constants
{
   /**
    * This is a Byte array that actually represents the header in nibbles (4 bit
    * values). Nibbles were selected for this project because the header fields
    * can be evenly split into four bits. This is initialized in the constructor
    * {@link #IPv6PacketGenerator(int)} and then its values are accumulated in
    * {@link #populateIHeader(byte[], byte[])}, which uses the two methods
    * {@link #fill(int, int, int)} and {@link #fillIPv4Addr(int, byte[])} for
    * help. Finally, it is packed into the proper Byte packet and retrieved in
    * {@link #getPacket(byte[], byte[])}.
    */
   private byte[] nibbleHeader;
   
   /**
    * This simply holds the length of the data portion of the packet in Bytes.
    */
   private int dataLength;
   
   /**
    * The constructor. Saves the length of the data portion of the packet (the
    * payload) {@link #dataLength} and initializes the {@link #nibbleHeader}.
    * It then populates the {@link #nibbleHeader} by using the helper
    * {@link #populateIHeader(byte[], byte[])}.
    * @param dataLength - saved into {@link #dataLength}
    * @param srcAddr - The Source IP Address
    * @param destAddr - The Destination IP Address
    */
   public IPv6PacketGenerator(int dataLength, byte[] srcAddr, byte[] destAddr)
   {
      this.dataLength = dataLength;
      nibbleHeader = new byte[HEADER_SIZE];
      populateIHeader(srcAddr, destAddr);
   }
   
   /**
    * This method controls the population of the entire header, with only the
    * instructed fields implemented.
    * @param srcAddr
    * @param destAddr
    */
   private void populateIHeader(byte[] srcAddr, byte[] destAddr)
   {
      // Version [4b] = 6
      fill(VERSION_END, VERSION_SIZE, 6);
      // Payload Length [16b] = dataLength
      fill(PLENGTH_END, PLENGTH_SIZE, dataLength);
      // Next Header [8b] = UDP = 17
      fill(NEXTHDR_END, NEXTHDR_SIZE, 17);
      // Hop Limit [8b] = 20
      fill(HOPLIMIT_END, HOPLIMIT_SIZE, 20);
      // Source Address [128b] = IPv4 address extended to an IPv6 address
      // Note, the assumption is using the local host address.
      fillIPv4Addr(SRCADDR_END, srcAddr);
      // Destination Address [128b] = IPv4 address extended using codebank's
      // IP address
      fillIPv4Addr(DESTADDR_END, destAddr);
   }
   
   /**
    * This method is used to fill some fields of the {@link #nibbleHeader}. It
    * is called only in {@link #populateIHeader(byte[], byte[])}.
    * @param endIndex - Because it fills backwards
    * @param fieldSize - The size of the field in nibbles
    * @param value - The value to be stored in the field
    */
   private void fill(int endIndex, int fieldSize, int value)
   {
      for (int i = 0; i < fieldSize; ++i)
      {
         nibbleHeader[endIndex - i] = (byte) (0xF & (value >>> (i * 4)));
      }
   }
   
   /**
    * This method fills the address fields in {@link #nibbleHeader}. Unlike the
    * {@link #fill(int, int, int)} method, it just goes traverses the array in
    * order and it only accepts two parameters. One functions similarly to the
    * other, but the value is distinct in that it is the byte[] representation
    * of an IP Address. Using this means that there is an assumption that the
    * passed IP address will be an IPv4 address. To convert this to an IPv6
    * address, it must be prepended with an 0xFFFF. Since the argument value is
    * assumed also to be in actual Bytes, it must be converted into nibbles to
    * fit into the {@link #nibbleHeader} array. Perhaps this could be better
    * handled by simply moving the Byte addresses directly into the completed
    * packet and prepending the 0xFFFF, but I think that keeping the overhead
    * for filling the header fields in one place seems neater (in {@link
    * #populateIHeader(byte[], byte[])}).
    * @param endIndex - The last index in the {@link #nibbleHeader} to be filled
    * @param value - The Byte array representing an IP address
    */
   private void fillIPv4Addr(int endIndex, byte[] value)
   {
      byte[] nibbleAddress = new byte[ADDR_SIZE];
      int ipv4AddrStart = nibbleAddress.length - 8;
      int ipv6PrependStart = nibbleAddress.length - 12;
      
      // Handles the prepend of 0xFFFF
      for (int i = ipv6PrependStart; i < ipv4AddrStart; ++i)
      {
         nibbleAddress[i] = (byte) 0xF;
      }
      
      // Handles the translation from Bytes to nibbles
      int j = 0;
      for (int i = ipv4AddrStart; i < nibbleAddress.length; ++i)
      {
         nibbleAddress[i++] = (byte) (0xF & (value[j] >> 4));
         nibbleAddress[i] = (byte) (0xF & value[j++]);
      }
      
      // Fills the header with the new IPv6 nibble address
      for (int i = 0; i < nibbleAddress.length; ++i)
      {
         nibbleHeader[endIndex - ADDR_SIZE + i + 1] =
               (byte) (0xF & nibbleAddress[i]);
      }
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
   
   /**
    * This method creates the proper packet by pushing the {@link #nibbleHeader}
    * into bytes by using {@link #convertHeaderToBytes()}. Then it simply
    * moves it over to the packet that will be returned.
    * @return - The whole packet.
    */
   public byte[] getPacket()
   {
      byte[] packet = new byte[(HEADER_SIZE / 2) + dataLength];
      
      byte[] header = convertHeaderToBytes();
      
      for (int i = 0; i < header.length; ++i)
      {
         packet[i] = header[i];
      }
      
      return packet;
   }
   
   /**
    * This method simply converts the {@link #nibbleHeader} into Bytes.
    * @return the Byte representation of the nibbleHeader
    */
   private byte[] convertHeaderToBytes()
   {
      byte[] header = new byte[HEADER_SIZE / 2];
      int i = 0;
      int j = 0;
      while (i < nibbleHeader.length)
      {
         header[j++] = accumulateNibbles(i);
         i += 2;
      }
      
      return header;
   }
   
   /**
    * This method simply accumulates nibbles into Bytes from the {@link
    * #nibbleHeader}.
    * @param index - Requires the index to know where to start making Bytes from
    * @return - Returns the Byte.
    */
   private byte accumulateNibbles(int index)
   {
      int b = 0;
      b = nibbleHeader[index++] << 4;
      b += nibbleHeader[index];
      return (byte) (b & 0xFF);
   }
}
