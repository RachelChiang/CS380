
/**
 * This is sent by the client if it wants to disconnect for whatever reason.
 * The server should close the connection when it is received.
 * 
 * @author nmpantic
 */
public final class DisconnectMessage extends Message {

    private static final long serialVersionUID = 0L;

    public DisconnectMessage() {
        super(MessageType.DISCONNECT);
    }
}
