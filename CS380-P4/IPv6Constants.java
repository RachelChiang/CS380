
public interface IPv6Constants
{
   // The ending index of the relevant fields in the header
   public static final int VERSION_END = 3;
   public static final int PLENGTH_END = 47;
   public static final int NEXTHDR_END = 55;
   public static final int HOPLIMIT_END = 63;
   public static final int SRCADDR_END = 191;
   public static final int DESTADDR_END = 319;
   
   // The size in bits of the relevant fields
   public static final int VERSION_SIZE = 4;
   public static final int PLENGTH_SIZE = 16;
   public static final int NEXTHDR_SIZE = 8;
   public static final int HOPLIMIT_SIZE = 8;
   public static final int ADDR_SIZE = 128;
   public static final int HEADER_SIZE = 320;
}

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
 * 
 * IPv6 Header Format
 *  0                   1                   2                   3
 *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1        
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |Version| Traffic Class |           Flow Label                  |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |         Payload Length        |  Next Header  |   Hop Limit   |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                     Source Address [128b]                     |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                   Destination Address [128b]                  |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 */