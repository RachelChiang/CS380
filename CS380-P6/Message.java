import java.io.Serializable;

public abstract class Message implements Serializable {

    private static final long serialVersionUID = 0L;

    private final MessageType type;

    public Message(MessageType type) {
        this.type = type;
    }

    public MessageType getType() {
        return type;
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
