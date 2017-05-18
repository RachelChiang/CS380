// Goal: Create an IPv4 Packet with UDP Packet as data for IPv4 Packet
public class PacketGenerator
{
   private int dataLength;
   private byte[] srcAddr;
   private byte[] destAddr;
   private int destPort;
   
   public PacketGenerator(int dataLength, byte[] srcAddr, byte[] destAddr, int destPort)
   {
      this.dataLength = dataLength;
      this.srcAddr = srcAddr;
      this.destAddr = destAddr;
      this.destPort = destPort;
   }
   
   public byte[] getPacket()
   {
      UDPGenerator udpGen = new UDPGenerator(dataLength, srcAddr, destAddr, destPort);
      byte[] udp = udpGen.getPacket();
      IPv4Generator ipv4Gen = new IPv4Generator(udp.length, srcAddr, destAddr);
      return ipv4Gen.getPacket(udp);
   }
   
   private void printMe(byte[] b)
   {
      System.out.print(" From PG: ");
      int c = 0;
      for (int i = 0; i < b.length; ++i)
      {
         System.out.print(String.format("%02X", b[i]));
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
