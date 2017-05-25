public final class ConnectMessage extends Message {

    private static final long serialVersionUID = 0L;

    private final String user;

    public ConnectMessage(String user) {
        super(MessageType.CONNECT);
        this.user = user;
    }

    public String getUser() {
        return user;
    }
}
