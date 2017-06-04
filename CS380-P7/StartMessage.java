
import java.io.File;


/**
 * The type of message for starting a new file transfer.
 * 
 * @author nmpantic
 */
public final class StartMessage extends Message {

    private static final long serialVersionUID = 0L;

    private final String file;
    private final long size;
    private final int chunkSize;
    private final byte[] encryptedKey;
    
    /**
     * Uses a default chunk size of 1024 bytes.
     * 
     * @param file The file being transferred.
     * @param encryptedKey The encrypted, serialized session key for the transfer.
     */
    public StartMessage(String file, byte[] encryptedKey) {
        this(file, encryptedKey, 1024);
    }
    
    /**
     * 
     * @param file The file being transferred.
     * @param encryptedKey The encrypted, serialized session key for the transfer.
     * @param chunkSize The chunk size for the transfer.
     */
    public StartMessage(String file, byte[] encryptedKey, int chunkSize) {
        super(MessageType.START);
        this.file = file;
        this.size = new File(file).length();
        this.chunkSize = chunkSize;
        this.encryptedKey = encryptedKey;
    }
    
    /**
     * 
     * @return The file name for the transfer.
     */
    public String getFile() {
        return file;
    }
    
    /**
     * 
     * @return The total number of bytes in the file.
     */
    public long getSize() {
        return size;
    }
    
    /**
     * 
     * @return The chunk size for the transfer.
     */
    public int getChunkSize() {
        return chunkSize;
    }
    
    /**
     * 
     * @return The encrypted, serialized session key.
     */
    public byte[] getEncryptedKey() {
        return encryptedKey;
    }
}
