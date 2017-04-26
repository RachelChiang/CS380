/*
 * Rachel Chiang
 * CS 380.01 Computer Networks
 * Project 3: IPv4
 */

/**
 * A super simple Interface full of constants only.
 */
public interface PacketConstants
{
   // These point to the ending index of each field in the header.
   public static final int VERSION_END = 3;
   public static final int IHL_END = 7;
   public static final int LENGTH_END = 31;
   public static final int FLAGS_END = 50;
   public static final int TTL_END = 71;
   public static final int PROTOCOL_END = 79;
   public static final int CHECKSUM_END = 95;
   public static final int SRCADDR_END = 127;
   public static final int DESTADDR_END = 159;
   
   // These denote the sizes in bits of the fields
   public static final int VERSION_SIZE = 4;
   public static final int IHL_SIZE = 4;
   public static final int LENGTH_SIZE = 16;
   public static final int FLAGS_SIZE = 3;
   public static final int TTL_SIZE = 8;
   public static final int PROTOCOL_SIZE = 8;
   public static final int CHECKSUM_SIZE = 16;
   public static final int ADDR_SIZE = 32;
   
   /*
    * Here is a neat little guide, taken from https://tools.ietf.org/html/rfc791
    * Internet Header Format
    *     0                   1                   2                   3
    *     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
    *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    *     |Version|  IHL  |Type of Service|          Total Length         |
    *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    *     |         Identification        |Flags|      Fragment Offset    |
    *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    *     |  Time to Live |    Protocol   |         Header Checksum       |
    *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    *     |                       Source Address                          |
    *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    *     |                    Destination Address                        |
    *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    *     |                    Options                    |    Padding    |
    *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    * 
    * In this project,
    * Implement:
    *    Version [4b] - indicates format of internet header
    *    HLen (IHL) [4b] - length of the internet header in 32b words; points to
    *       beginning of data. (Note min value = 5)
    *    Length [16b] - length of datagram, measured in octets, including
    *       internet header and data
    *    Flags [3b] - assume no fragmentation, b0b1b2
    *       b0 must be 0;
    *       b1 = 1 (don't fragment), b1 = 0 (may fragment);
    *       b2 = 0 (last fragment), b2 = 1 (more fragments)
    *    TTL [8b] - assume every packet has TTL=50; Maximum time the datagram
    *       is allowed to remain in the internet system. If this field = 0,
    *       datagram must be destroyed. Modified in internet header processing.
    *       Time is measured in units of seconds. TTL is an upper bound on the
    *       time a datagram may exist
    *    Protocol [8b] - assume TCP; this field indicates the next level protocol
    *       used in the data portion of the internet diagram.
    *    Checksum [16b] - Checksum for the header
    *    SourceAddr [32b] - Source IP Address
    *    DestinationAddr [32b] - Server IP Address
    * Do Not Implement: Set these to zero...
    *    TOS [8b], Identification [16b], Offset [13b]
    * Omit: Options, Padding
    */
}