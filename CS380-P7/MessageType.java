
/**
 * This {@code enum} is used for the different types of allowed messages.
 * {@code PAUSE} and {@code RESUME} are included although not used in the project.
 * They can be implemented if desired.
 * 
 * @author nmpantic
 */
public enum MessageType {

    START, STOP, CHUNK, PAUSE, RESUME, ACK, DISCONNECT;

    private static final long serialVersionUID = 0L;
}
