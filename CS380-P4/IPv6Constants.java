/*
 * Rachel Chiang
 * CS 380.01 Computer Networks
 * Project 4: IPv6
 */

/**
 * This is simply an interface containing constants pertaining to the IPv6 
 * header.
 */
public interface IPv6Constants
{
   // The ending index of the relevant fields in the header
   public static final int VERSION_END = 0;
   public static final int PLENGTH_END = 11;
   public static final int NEXTHDR_END = 13;
   public static final int HOPLIMIT_END = 15;
   public static final int SRCADDR_END = 47;
   public static final int DESTADDR_END = 79;
   
   // The size in nibbles of the relevant fields
   public static final int VERSION_SIZE = 1;
   public static final int PLENGTH_SIZE = 4;
   public static final int NEXTHDR_SIZE = 2;
   public static final int HOPLIMIT_SIZE = 2;
   public static final int ADDR_SIZE = 32;
   public static final int HEADER_SIZE = 80;
}

/*
 * Implement:
 *    Version [4b] = 6
 *    Payload Length [16b] = length of the whole packet in octets
 *    Next Header [8b] = UDP = 17, identifies next type of header
 *    Hop Limit [8b] = 20
 *    Source Address [128b] = an IPv4 address that has been extended to IPv6
 *       for a device that does not use IPv6
 *    Destination Address [128b] = an IPv4 address that has been extended to
 *       IPv6 for a device that does not use IPv6 using the IP address of the
 *       server you are connected to
 * Don't Implement: Traffic Class [8b], Flow Label [20b]
 * 
 * IPv6 Header Format [bits]
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
 * 
 * IPv6 Header Format [nibbles]
 *  0 1 2 3 4 5 6 7
 * +-+-+-+-+-+-+-+-+
 * |V|TC |FlowLabel|
 * +-+-+-+-+-+-+-+-+
 * |PLength|NHd|HL |
 * +-+-+-+-+-+-+-+-+
 * |Src Addr [32n] |
 * +-+-+-+-+-+-+-+-+
 * |DestAddr [32n] |
 * +-+-+-+-+-+-+-+-+
 */