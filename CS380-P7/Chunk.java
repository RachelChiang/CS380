
/**
 * One piece of the file being transferred.  Each chunk has a unique
 * sequence number and a CRC32 value for data integrity.  The data itself is
 * encrypted with the transfer's session key.  The CRC32 value was calculated on
 * the plaintext of the data, not the ciphertext.
 * 
 * @author nmpantic
 */
public final class Chunk extends Message {

    private static final long serialVersionUID = 0L;
    
    private final int seq;
    private final byte[] data;
    private final int crc;
    
    /**
     * 
     * @param seq The sequence number for the chunk.
     * @param data The encrypted data for the chunk.
     * @param crc The CRC32 value for the plaintext of the chunk's data.
     */
    public Chunk(int seq, byte[] data, int crc) {
        super(MessageType.CHUNK);
        this.seq = seq;
        this.data = data;
        this.crc = crc;
    }

    /**
     * 
     * @return The sequence number for the chunk.
     */
    public int getSeq() {
        return seq;
    }

    /**
     * 
     * @return The encrypted data for the chunk.
     */
    public byte[] getData() {
        return data;
    }

    /**
     * 
     * @return The CRC32 value for the plaintext of the chunk's data.
     */
    public int getCrc() {
        return crc;
    }
}
