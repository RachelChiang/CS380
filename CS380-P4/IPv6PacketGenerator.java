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
   
   private void populateIHeader(byte[] srcAddr, byte[] destAddr)
   {
      fill(VERSION_END, VERSION_SIZE, 6);
      fill(PLENGTH_END, PLENGTH_SIZE, dataLength);
      fill(NEXTHDR_END, NEXTHDR_SIZE, 17);
      fill(HOPLIMIT_END, HOPLIMIT_SIZE, 20);
      fillIPv4Addr(SRCADDR_END, srcAddr);
      fillIPv4Addr(DESTADDR_END, destAddr);
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
   
   private void fillIPv4Addr(int endIndex, byte[] value)
   {
      byte[] nibbleAddress = new byte[ADDR_SIZE];
      int ipv4AddrStart = nibbleAddress.length - 8;
      int ipv6PrependStart = nibbleAddress.length - 12;
      
      for (int i = ipv6PrependStart; i < ipv4AddrStart; ++i)
      {
         nibbleAddress[i] = (byte) 0xF;
      }
      
      int j = 0;
      for (int i = ipv4AddrStart; i < nibbleAddress.length; ++i)
      {
         nibbleAddress[i++] = (byte) (0xF & (value[j] >> 4));
         nibbleAddress[i] = (byte) (0xF & value[j++]);
      }
      
      /*for (int i = 0; i < nibbleAddress.length; ++i)
      {
         System.out.print(String.format("%01X", nibbleAddress[i]));
      }
      System.out.println();*/
      
      for (int i = 0; i < nibbleAddress.length; ++i)
      {
         nibbleHeader[endIndex - ADDR_SIZE + i + 1] = (byte) (0xF & nibbleAddress[i]);
      }
   }
   
   public byte[] getPacket(byte[] srcAddr, byte[] destAddr)
   {
      populateIHeader(srcAddr, destAddr);
      
      byte[] packet = new byte[(HEADER_SIZE / 2) + dataLength];
      
      byte[] header = convertHeaderToBytes();
      //printByteArray(header);
      
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
}
