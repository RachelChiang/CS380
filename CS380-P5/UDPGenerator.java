import java.util.Random;

public class UDPGenerator extends HeaderGenerator implements UDPConstants
{
   private int dataLength;
   private byte[] byteHeader;
   private byte[] packet;
   
   public UDPGenerator(int dataLength, byte[] srcAddr, byte[] destAddr, int destPort)
   {
      this.dataLength = dataLength;
      byteHeader = new byte[HEADER_SIZE];
      packet = new byte[HEADER_SIZE + dataLength];
      makePacket(srcAddr, destAddr, destPort);
   }

   public byte[] getPacket()
   {
      return packet;
   }
   
   private void makePacket(byte[] srcAddr, byte[] destAddr, int destPort)
   {
      // Source Port
      // TODO: randomize it maybe?
      fill(SRCPORT_END, 0xABCD);
      // Destination Port
      fill(DESTPORT_END, destPort);
      // Length
      fill(LENGTH_END, HEADER_SIZE + dataLength);
      // Data
      addData();
      
      // Checksum conducted on pseudoheader (which includes data)
      fill(CHECKSUM_END, checksum(getPseudoheader(srcAddr, destAddr)));
      for (int i = CHECKSUM_END - FIELD_SIZE + 1; i < byteHeader.length; ++i)
      {
         packet[i] = byteHeader[i];
      }
   }
   
   private void addData()
   {
      byte[] data = new byte[dataLength];
      Random randomizer = new Random();
      randomizer.nextBytes(data);
      
      for (int i = 0; i < byteHeader.length; ++i)
      {
         packet[i] = byteHeader[i];
      }
      int j = 0;
      for (int i = byteHeader.length; i < packet.length; ++i)
      {
         packet[i] = data[j++];
      }
   }
   
   private void fill(int endIndex, int value)
   {
      for (int i = 0; i < FIELD_SIZE; ++i)
      {
         byteHeader[endIndex - i] = (byte) (0xFF & (value >>> (8 * i)));
      }
   }
   
   private byte[] getPseudoheader(byte[] srcAddr, byte[] destAddr)
   {
      byte[] pseudoheader = new byte[packet.length + PSEUDOHEADER_PREFIX_SIZE];
      
      // Source Address
      int i = 0;
      int j = 0;
      while (i < ADDR_SIZE)
      {
         pseudoheader[i] = srcAddr[j++];
         ++i;
      }
      
      // Destination Address
      j = 0;
      while (i < 2 * ADDR_SIZE)
      {
         pseudoheader[i] = destAddr[j++];
         ++i;
      }
      
      // Protocol
      i = PROTOCOL_END;
      pseudoheader[i] = (byte) (0xFF & 0x11); // UDP = 0x11 = 17
      
      // UDP Length
      j = 0;
      while (j < UDPLENGTH_SIZE)
      {
         pseudoheader[UDPLENGTH_END - j] = (byte) (0xFF & ((HEADER_SIZE + dataLength) >>> (8 * j)));
         ++j;
      }
      
      // The UDP Part
      j = 0;
      for (int k = PSEUDOHEADER_PREFIX_SIZE; k < pseudoheader.length; ++k)
      {
         pseudoheader[k] = packet[j++];
      }
      
      /*
      System.out.print(" Pseudoheader: ");
      int c = 0;
      for (int k = 0; k < pseudoheader.length; ++k)
      {
         System.out.print(String.format("%02X", pseudoheader[k]));
         ++c;
         if (c == 4)
         {
            System.out.print(" ");
            c = 0;
         }
      }
      System.out.println();
      */
      
      return pseudoheader;
   }
   
   private void printHeader()
   {
      System.out.print(" UDP Header: ");
      int c = 0;
      for (int i = 0; i < byteHeader.length; ++i)
      {
         System.out.print(String.format("%02X", byteHeader[i]));
         ++c;
         if (c == 4)
         {
            System.out.print(" ");
            c = 0;
         }
      }
      System.out.println();
   }
}