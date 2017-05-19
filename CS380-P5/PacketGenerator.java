/*
 * Rachel Chiang
 * CS 380.01 Computer Networks
 * Project 5: UDP Client with IPv4
 */
/**
 * This class simply puts together the IPv4 and UDP segments.
 */
public class PacketGenerator
{
   /**
    * The length of the data for the UDP, in bytes.
    */
   private int dataLength;
   
   /**
    * The source address in a byte array.
    */
   private byte[] srcAddr;
   
   /**
    * The destination address in a byte array.
    */
   private byte[] destAddr;
   
   /**
    * The destination port represented by an integer.
    */
   private int destPort;
   
   /**
    * The constructor.
    * @param dataLength
    * @param srcAddr
    * @param destAddr
    * @param destPort
    */
   public PacketGenerator(int dataLength, byte[] srcAddr, byte[] destAddr,
         int destPort)
   {
      this.dataLength = dataLength;
      this.srcAddr = srcAddr;
      this.destAddr = destAddr;
      this.destPort = destPort;
   }
   
   /**
    * To retrieve the packet with UDP in IPv4, call this function.
    * @return
    */
   public byte[] getPacket()
   {
      // Creates the UDP packet
      UDPGenerator udpGen =
            new UDPGenerator(dataLength, srcAddr, destAddr, destPort);
      byte[] udp = udpGen.getPacket();
      // Creates the IPv4 header
      IPv4Generator ipv4Gen = new IPv4Generator(udp.length, srcAddr, destAddr);
      // And places the UDP in the data portion of the IPv4 packet
      return ipv4Gen.getPacket(udp);
   }
}
