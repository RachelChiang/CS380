public final class MoveMessage extends Message {

    private static final long serialVersionUID = 0L;

    private final byte row;
    private final byte col;

    public MoveMessage(byte row, byte col) {
        super(MessageType.MOVE);
        this.row = row;
        this.col = col;
    }

    public byte getRow() {
        return row;
    }

    public byte getCol() {
        return col;
    }
}
