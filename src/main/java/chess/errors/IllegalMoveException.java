package chess.errors;

public final class IllegalMoveException extends ChessEngineException {
    public IllegalMoveException(String message) {
        super(message);
    }
}