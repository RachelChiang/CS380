/*
 * Rachel Chiang
 * CS 380.01 Computer Networks
 * Project 5: UDP Client with IPv4
 */
import java.util.Random;
/**
 * This class generates the UDP portion of the packet. Many of the constants can
 * be found in the interface, {@link #UDPConstants}. The data portion of the
 * packet is randomly generated using java.util.Random's nextBytes(byte[]). To
 * create an instance of this class, it must be given the {@link #dataLength},
 * source and destination addresses, and a destination port, which it will then
 * use to build the {@link #byteHeader} and subsequently the {@link #packet},
 * which can be accessed via {@link #getPacket()}.
 */
public class UDPGenerator extends HeaderGenerator implements UDPConstants
{
   /**
    * This is the length of the data portion in bytes.
    */
   private int dataLength;
   
   /**
    * This is the UDP header in bytes. In retrospect, I could have foregone this
    * variable and simply used the {@link #packet} from the get-go.
    */
   private byte[] byteHeader;
   
   /**
    * This is the packet, meaning it is the {@link #byteHeader} along with the
    * random data of size {@link #dataLength}.
    */
   private byte[] packet;
   
   /**
    * The constructor, which calls {@link #makePacket(byte[], byte[], int)} to,
    * well, make the packet.
    * @param dataLength - The length of the data portion of the UDP packet, in
    *    bytes, which is stored in {@link #dataLength}.
    * @param srcAddr - The source address
    * @param destAddr - The destination address
    * @param destPort - The destination port
    */
   public UDPGenerator(int dataLength, byte[] srcAddr, byte[] destAddr,
         int destPort)
   {
      this.dataLength = dataLength;
      byteHeader = new byte[HEADER_SIZE];
      packet = new byte[HEADER_SIZE + dataLength];
      makePacket(srcAddr, destAddr, destPort);
   }

   /**
    * This method simply returns {@link #packet}.
    * @return
    */
   public byte[] getPacket()
   {
      return packet;
   }
   
   /**
    * This method handles the overhead of generating the entire packet. First it
    * will generate the {@link #byteHeader} and then calls on {@link #addData()}
    * to both fill the data portion out and fill the {@link #packet} itself.
    * Finally, the checksum can be calculated. UDP conducts a checksum over
    * the pseudoheader, which is retrieved from the method {@link
    * #getPseudoheader(byte[], byte[])}. Finally, the checksum is added to the
    * {@link #packet}.
    * @param srcAddr - The source address in a byte array.
    * @param destAddr - The destination address in a byte array.
    * @param destPort - The destination port, represented by an integer.
    */
   private void makePacket(byte[] srcAddr, byte[] destAddr, int destPort)
   {
      // Source Port
      fill(SRCPORT_END, 0xABCD);
      // Destination Port
      fill(DESTPORT_END, destPort);
      // Length
      fill(LENGTH_END, HEADER_SIZE + dataLength);
      // Data and packet-filling
      addData();
      
      // Checksum conducted on pseudoheader (which includes data)
      fill(CHECKSUM_END, checksum(getPseudoheader(srcAddr, destAddr)));
      for (int i = CHECKSUM_END - FIELD_SIZE + 1; i < byteHeader.length; ++i)
      {
         packet[i] = byteHeader[i];
      }
   }
   
   /**
    * This method fills the field of the UDP packet with the given value. It's
    * just about the same as the past two projects, though this one does not
    * require the size of the section since the UDP sections are all the same
    * size.
    * @param endIndex - The last index of the field.
    * @param value - The value to be saved in the field.
    */
   private void fill(int endIndex, int value)
   {
      for (int i = 0; i < FIELD_SIZE; ++i)
      {
         byteHeader[endIndex - i] = (byte) (0xFF & (value >>> (8 * i)));
      }
   }

   /**
    * This method adds data to the packet, specifically the information from the
    * {@link #byteHeader} as well as the {@link #dataLength} number of random
    * bytes.
    */
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
   
   /**
    * This method builds a pseudoheader, which will be used for the checksum.
    * @param srcAddr - The source address in a byte array
    * @param destAddr - The destination address in a byte array
    * @return - The pseudoheader, which consists of the IPv4 source and
    *       destination addresses, the protocol, the UDP Length, the source and
    *       destination ports, and the length.
    */
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
         pseudoheader[UDPLENGTH_END - j] =
               (byte) (0xFF & ((HEADER_SIZE + dataLength) >>> (8 * j)));
         ++j;
      }
      
      // The UDP Part
      j = 0;
      for (int k = PSEUDOHEADER_PREFIX_SIZE; k < pseudoheader.length; ++k)
      {
         pseudoheader[k] = packet[j++];
      }
      
      return pseudoheader;
   }
}