import java.net.InetAddress;

public class IPv6PacketGenerator implements IPv6Constants
{
   private byte[] bitHeader;
   
   /**
    * This simply holds the length of the data portion of the packet in Bytes.
    */
   private int dataLength;
   
   public IPv6PacketGenerator(int dataLength)
   {
      this.dataLength = dataLength;
      bitHeader = new byte[HEADER_SIZE];
   }
   
   private void populateIHeader(int srcAddr, int destAddr)
   {
      fill(VERSION_END, VERSION_SIZE, 6);
      fill(PLENGTH_END, PLENGTH_SIZE, dataLength);
      fill(NEXTHDR_END, NEXTHDR_SIZE, 17);
      fill(HOPLIMIT_END, HOPLIMIT_SIZE, 20);
      
      // TODO Source and Destination Addresses
      //fill(SRCADDR_END, ADDR_SIZE, prependAddress(srcAddr));
      /*
       * Implement:
       *    Version [4b] = 6
       *    Payload Length [16b] = length of the whole packet in octets
       *    Next Header [8b] = UDP = 17, identifies next type of header
       *    Hop Limit [8b] = 20
       *    Source Address [128b] = an IPv4 address that has been extended to IPv6 for a device that does not use IPv6
       *         String address = socket.getInetAddress().getHostAddress();
       *    Destination Address [128b] = an IPv4 address that has been extended to IPv6 for a device that does not use IPv6
       *       using the IP address of the server you are connected to
       * Don't Implement: Traffic Class [8b], Flow Label [20b]
       */
   }
   
   private void fill(int endIndex, int fieldSize, int value)
   {
      for (int i = 0; i < fieldSize; ++i)
      {
         bitHeader[endIndex - i] = (byte) (0x1 & (value >>> i));
      }
   }
   
   public byte[] getPacket(int srcAddr, int destAddr)
   {
      populateIHeader(srcAddr, destAddr);
      
      byte[] packet = new byte[(HEADER_SIZE / 8) + dataLength];
      
      byte[] header = convertHeaderToBytes();
      printByteArray(header);
      
      for (int i = 0; i < header.length; ++i)
      {
         packet[i] = header[i];
      }
      
      return packet;
   }
   
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
      int[] address = new int[ADDR_SIZE / 4];
      for (int i = 0; i < 4; ++i)
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
      
      for (int i = 0; i < address.length; ++i)
      {
         System.out.print(String.format("%01X", address[i]));
      }
      System.out.println();
      return address;
   }
}
