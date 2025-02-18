package chess.Board.Move;

public class Move
{
    final int from;
    final int to;
    final int piece;
    final int capturedPiece;
    final Integer enPassantBefore;

    public Move(int from, int to, int piece, int capturedPiece, Integer enPassantBefore)
    {
        this.from = from;
        this.to = to;
        this.piece = piece;
        this.capturedPiece = capturedPiece;
        this.enPassantBefore = enPassantBefore;
    }

    public int getCapturedPiece() {
        return capturedPiece;
    }

    public int getFrom() {
        return from;
    }

    public int getPiece() {
        return piece;
    }

    public int getTo() {
        return to;
    }

    public Integer getEnPassantBefore() {
        return enPassantBefore;
    }
}
