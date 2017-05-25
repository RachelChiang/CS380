public final class CommandMessage extends Message {

    private static final long serialVersionUID = 0L;

    private final Command cmd;

    public CommandMessage(Command cmd) {
        super(MessageType.COMMAND);
        this.cmd = cmd;
    }

    public Command getCommand() {
        return cmd;
    }

    public static enum Command {

        NEW_GAME, LIST_PLAYERS, EXIT, SURRENDER;

        private static final long serialVersionUID = 0L;
    }
}
