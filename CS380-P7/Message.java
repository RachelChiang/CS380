
import java.io.Serializable;

/**
 * The base class for the messages passed between server and client.  Its only
 * purpose is to identify the type of message being passed.
 * 
 * @author nmpantic
 */
public abstract class Message implements Serializable {

    private static final long serialVersionUID = 0L;

    private final MessageType type;

    /**
     * 
     * @param type The type of the message.
     */
    public Message(MessageType type) {
        this.type = type;
    }

    /**
     * 
     * @return The type of the message.
     */
    public MessageType getType() {
        return type;
    }

    /**
     * 
     * @return The type of the message is returned as a {@code String}.
     */
    @Override
    public String toString() {
        return type.toString();
    }
}
