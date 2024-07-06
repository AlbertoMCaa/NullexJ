package chess.Pieces;

import chess.Board.Board;
/*
 * Here we define the Piece class.  This class is the base class for all the Piece classes that are used in the chess game.
 * Each piece has a position and an alliance.  The alliance is used to determine the ownership of a piece and it's color.
 * Each piece also has a position wich  is used to define where the piece is located on the board.
 */
public abstract class Piece {
    protected final int piecePosition;
    protected final Alliance pieceAlliance;

    protected Piece(int piecePosition, Alliance pieceAlliance) {
        this.piecePosition = piecePosition;
        this.pieceAlliance = pieceAlliance;
    }

    public int getPiecePosition() {
        return piecePosition;
    }
    public Alliance getPieceAlliance() {
        return pieceAlliance;
    }
    
    /*
     * Each child class must override this method.  This method is used to determine if a move is valid.
     */
    public abstract boolean isValidMove(int destinationSquare, Board board);
}