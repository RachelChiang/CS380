
/**
 * This message type is used to acknowledge chunks in the transfer.  If
 * the transfer must be stopped or cannot be started, the server responds with
 * an {@code AckMessage} of sequence number {@code -1}.
 * 
 * <p>
 * 
 * The sequence number in the ACK is for the next expected chunk.  For example,
 * if the server accepts chunk 17, it will send {@code ACK 18}.  When the server has
 * accepted all chunks, it sends {@code ACK N} where {@code N} is the number of
 * chunks in the transfer.
 * 
 * <p>
 * 
 * When a server receives a chunk but cannot accept it, it sends a repeated
 * {@code ACK} (the last {@code ACK} is sent again).  For example, if the next
 * chunk is 17 but the CRC is bad, the server will send {@code ACK 17} again
 * instead of {@code ACK 18} and wait for a retransmission of the previous chunk.
 * 
 * @author nmpantic
 */
public final class AckMessage extends Message {

    private static final long serialVersionUID = 0L;

    private final int seq;

    /**
     * 
     * @param seq The sequence number being acknowledged.
     */
    public AckMessage(int seq) {
        super(MessageType.ACK);
        this.seq = seq;
    }

    /**
     * 
     * @return The sequence number being acknowledged.
     */
    public int getSeq() {
        return seq;
    }
}
