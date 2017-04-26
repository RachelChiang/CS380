public interface PacketConstants
{
   public static final int DATA_START = 160;
   
   public static final int VERSION_END = 3;
   public static final int IHL_END = 7;
   public static final int TOS_END = 15;
   public static final int LENGTH_END = 31;
   public static final int IDEN_END = 47;
   public static final int FLAGS_END = 50;
   public static final int OFFSET_END = 63;
   public static final int TTL_END = 71;
   public static final int PROTOCOL_END = 79;
   public static final int CHECKSUM_END = 95;
   public static final int SRCADDR_END = 127;
   public static final int DESTADDR_END = 159;
   
   public static final int VERSION_SIZE = 4;
   public static final int IHL_SIZE = 4;
   public static final int LENGTH_SIZE = 16;
   public static final int FLAGS_SIZE = 3;
   public static final int TTL_SIZE = 8;
   public static final int PROTOCOL_SIZE = 8;
   public static final int CHECKSUM_SIZE = 16;
   public static final int ADDR_SIZE = 32;
}
/*
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
 * Implement:
 *    Version - (4b), indicates format of internet header (version 4)
 *    HLen (IHL) - (4b), length of the internet header in 32b words; points to
 *       beginning of data. (Note min value = 5)
 *    Length - (16b), length of datagram, measured in octets, including
 *       internet header and data
 *    Flags (assuming no fragmentation) - (3b) b0b1b2
 *       b0 must be 0;
 *       b1 = 1 (don't fragment), b1 = 0 (may fragment);
 *       b2 = 0 (last fragment), b2 = 1 (more fragments)
 *    TTL (assuming every packet has TTL=50) - (8b) Maximum time the datagram
 *       is allowed to remain in the internet system. If this field = 0,
 *       datagram must be destroyed. Modified in internet header processing.
 *       Time is measured in units of seconds. TTL is an upper bound on the
 *       time a datagram may exist
 *    Protocol (assuming TCP) - (8b) This field indicates the next level
 *       protocol used in the data portion of the internet diagram. TCP=6
 *    Checksum - (16b) Checksum for the header only. Algorithm: "The checksum
 *       field is the 16 bit one's complement of the one's complement sum of
 *       all 16 bit words in the header. For purposes of computing the
 *       checksum, the value of the checksum field is zero."
 *    SourceAddr (with IP address of your choice) - (32b)
 *    DestinationAddr (with IP address of the server) - (32b)
 *    Data (using zeros or rand data)
 * Do Not Implement: TOS, Identification, Offset
 * Do Not Include At All: Options, Padding
 */