
/**
 * Used to indicate a file transfer should stop.  Identified by the file name
 * for the transfer.
 * 
 * @author nmpantic
 */
public final class StopMessage extends Message {

    private static final long serialVersionUID = 0L;

    private final String file;
    
    /**
     * 
     * @param file The file name for the transfer.
     */
    public StopMessage(String file) {
        super(MessageType.STOP);
        this.file = file;
    }
    
    /**
     * 
     * @return The file name for the transfer.
     */
    public String getFile() {
        return file;
    }
}
