package chess.errors;

public abstract class ChessEngineException extends RuntimeException {
    public ChessEngineException(String message) {
        super(message);
    }
}
