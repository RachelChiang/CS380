public final class ErrorMessage extends Message {

    private static final long serialVersionUID = 0L;

    private final String error;

    public ErrorMessage(String error) {
        super(MessageType.ERROR);
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
