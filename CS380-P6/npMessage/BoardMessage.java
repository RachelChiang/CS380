package npMessage;
public final class BoardMessage extends Message {

    private static final long serialVersionUID = 0L;

    private final byte[][] board;
    private final Status status;
    private final byte turn;

    public BoardMessage(byte[][] board, Status status, byte turn) {
        super(MessageType.BOARD);
        this.board = board;
        this.status = status;
        this.turn = turn;
    }

    public byte[][] getBoard() {
        return board;
    }

    public Status getStatus() {
        return status;
    }

    public byte getTurn() {
        return turn;
    }

    public static enum Status {
        PLAYER1_SURRENDER, PLAYER2_SURRENDER, PLAYER1_VICTORY, PLAYER2_VICTORY,
        STALEMATE, IN_PROGRESS, ERROR;

        private static final long serialVersionUID = 0L;
    }
}
