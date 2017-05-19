/*
 * Rachel Chiang
 * CS 380.01 Computer Networks
 * Project 5: UDP Client with IPv4
 */
/**
 * A super simple interface full of constants for the UDP. Indices and sizes are
 * counted in bytes.
 */
public interface UDPConstants
{
   // UDP constants
   // End indices in bytes
   public static final int SRCPORT_END = 1;
   public static final int DESTPORT_END = 3;
   public static final int LENGTH_END = 5;
   public static final int CHECKSUM_END = 7;
   // Sizes in bytes
   public static final int FIELD_SIZE = 2;
   public static final int HEADER_SIZE = 8;
   
   // Pseudoheader constants
   // End indices in bytes
   public static final int PROTOCOL_END = 9;
   public static final int UDPLENGTH_END = 11;
   // Sizes in bytes
   public static final int ADDR_SIZE = 4;
   public static final int UDPLENGTH_SIZE = 2;
   public static final int PSEUDOHEADER_PREFIX_SIZE = 12;
}
/*
 * UDP Header: 4 fields, each 2B [16b]
 *  0                   1                   2                   3
 *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |          Source Port          |        Destination Port       |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |             Length            |            Checksum           |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * 
 * Source Port [16b] - sender's port
 * Destination Port [16b] - receiver's port
 * Length [16b] - Length in Bytes of the UDP Header and UDP Data (min length =
 *    8B = Header Size)
 * Checksum [16b] - UDP Header, data, and pseudoheader
 * 
 * Pseudoheader:
 *  0                   1                   2                   3
 *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                     Source IPv4 Address                       |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                  Destination IPv4 Address                     |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |     Zeroes    |    Protocol   |           UDP Length          |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |          Source Port          |        Destination Port       |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |             Length            |            Checksum           |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * 
 * Source and Destination Addresses [32b each] - from IPv4
 * Protocol [8b] - UDP = 17 = 0x11
 * Length [16b] - Length in Bytes of the UDP Header and Data
 */