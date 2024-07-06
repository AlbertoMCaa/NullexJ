package chess.Board;
import chess.Pieces.*;

public class move {
    final Board board;
    final Piece movedPiece;
    final int destinationSquare;

    public move(Board board, Piece movedPiece, int destinationSquare) {
        this.board = board;
        this.movedPiece = movedPiece;
        this.destinationSquare = destinationSquare;
    }
    public Board getBoard() {
        return board;
    }
    public Piece getMovedPiece() {
        return movedPiece;
    }
    public int getDestinationSquare() {
        return destinationSquare;
    }
    /*
     * This inner class is used to represent an AttackMove.
     */
    public static final class AttackMove extends move {
        final Piece attackedPiece;
        public AttackMove(Board board, Piece movedPiece, int destinationSquare, Piece attackedPiece) {
            super(board, movedPiece, destinationSquare);
            this.attackedPiece = attackedPiece;
        }
    }
    /*
     * This inner class is used to represent a Major Piece move (Rook and Queen).
     */
    public static final class MayorMove extends move{
        public MayorMove(Board board, Piece movedPiece, int destinationSquare) {
            super(board, movedPiece, destinationSquare);
        }
    }

    //----------------------------------------------------------------//

    /*
     * This method calls the isValidMove method of the piece that is being moved to determine if the move is valid.
     */
    public boolean isValidMove() {
        return movedPiece.isValidMove(destinationSquare, board);
    }
}
