public class IPv6PacketGenerator implements IPv6Constants
{
private byte[] nibbleHeader;
   
   /**
    * This simply holds the length of the data portion of the packet in Bytes.
    */
   private int dataLength;
   
   public IPv6PacketGenerator(int dataLength)
   {
      this.dataLength = dataLength;
      nibbleHeader = new byte[HEADER_SIZE];
   }
   
   private void populateIHeader(int srcAddr, int destAddr)
   {
      fill(VERSION_END, VERSION_SIZE, 6);
      fill(PLENGTH_END, PLENGTH_SIZE, dataLength);
      fill(NEXTHDR_END, NEXTHDR_SIZE, 17);
      fill(HOPLIMIT_END, HOPLIMIT_SIZE, 20);
      fillAddr(SRCADDR_END, ADDR_SIZE, prependAddress(srcAddr));
      fillAddr(DESTADDR_END, ADDR_SIZE, prependAddress(destAddr));
      /*
       * Implement:
       *    Version [4b] = 6
       *    Payload Length [16b] = length of the whole packet in octets
       *    Next Header [8b] = UDP = 17, identifies next type of header
       *    Hop Limit [8b] = 20
       *    Source Address [128b] = an IPv4 address that has been extended to IPv6 for a device that does not use IPv6
       *    Destination Address [128b] = an IPv4 address that has been extended to IPv6 for a device that does not use IPv6
       *       using the IP address of the server you are connected to
       * Don't Implement: Traffic Class [8b], Flow Label [20b]
       */
   }
   
   private void fill(int endIndex, int fieldSize, int value)
   {
      for (int i = 0; i < fieldSize; ++i)
      {
         nibbleHeader[endIndex - i] = (byte) (0xF & (value >>> (i * 4)));
      }
   }
   
   private void fillAddr(int endIndex, int fieldSize, int[] value)
   {
      for (int i = 0; i < value.length; ++i)
      {
         nibbleHeader[endIndex - fieldSize + i + 1] = (byte) (0xF & value[i]);
      }
   }
   
   public byte[] getPacket(int srcAddr, int destAddr)
   {
      populateIHeader(srcAddr, destAddr);
      
      byte[] packet = new byte[(HEADER_SIZE / 2) + dataLength];
      
      byte[] header = convertHeaderToBytes();
     // printByteArray(header);
      
      for (int i = 0; i < header.length; ++i)
      {
         packet[i] = header[i];
      }
      
      return packet;
   }
   
   private byte[] convertHeaderToBytes()
   {
      byte[] header = new byte[HEADER_SIZE / 2];
      int i = 0;
      int j = 0;
      while (i < nibbleHeader.length)
      {
         header[j++] = accumulateBits(i);
         i += 2;
      }
      
      return header;
   }
   
   /**
    * This simply method accumulates nibbles into Bytes from the {@link
    * #nibbleHeader}.
    * @param index - Requires the index to know where to start making Bytes from
    * @return - Returns the Byte.
    */
   private byte accumulateBits(int index)
   {
      int b = 0;
      b = nibbleHeader[index++] << 4;
      b += nibbleHeader[index];
      return (byte) (b & 0xFF);
   }
   
   private void printByteArray(byte[] h)
   {
      for (int i = 0; i < h.length; ++i)
      {
         System.out.print(String.format("%02X", h[i]));
         if ((i + 1) % 4 == 0)
         {
            System.out.println();
         }
      }
   }
   
   private int[] prependAddress(int ip)
   {
     // System.out.print("Address: ");
      int[] address = new int[ADDR_SIZE];
      for (int i = address.length - 12; i < address.length - 8; ++i)
      {
         address[i] = 0xF;
      }
      
      int j = 28;
      for (int i = address.length - 8; i < address.length; ++i)
      {
         //int value = 0xF & (ip >>> j);
         address[i] = 0xF & (ip >>> j);
         //System.out.println("Adding " + String.format("%01X", value));
         j -= 4;
      }
      /*
      for (int i = 0; i < address.length; ++i)
      {
         System.out.print(String.format("%01X", address[i]));
      }
      System.out.println();*/
      return address;
   }
}
