package chess.Pieces;

import chess.Board.Board;
import chess.Board.BoardUtils;

/*
 * This class represents a piece in the chess game.
 * 0b00000
 * The first 3 bit represent the type of piece.
 * The 4 and 5 bit represent the color of the piece.
 *
 * Example: 10001 represents a White Pawn.
 *          01001 represents a Black Pawn.
 *
 * 0b001 represents Pawn.
 * 0b010 represents Knight.
 * 0b100 represents Bishop.
 * 0b101 represents Rook.
 * 0b110 represents Queen.
 * 0b111 represents King.
 *
 * 0b10000 represents White.
 * 0b01000 represents Black.
 */
public class Piece {
    public static final int WHITE_KNIGHT = 0b10010;
    public static final int BLACK_KNIGHT = 0b01010;
    public static final int WHITE_BISHOP = 0b10100;
    public static final int BLACK_BISHOP = 0b01100;
    public static final int WHITE_QUEEN = 0b10110;
    public static final int WHITE_KING = 0b10111;
    public static final int BLACK_QUEEN = 0b01110;
    public static final int BLACK_KING = 0b01111;
    public static final int WHITE_ROOK = 0b10101;
    public static final int BLACK_ROOK = 0b01101;
    public static final int WHITE_PAWN = 0b10001;
    public static final int BLACK_PAWN = 0b01001;

    public static final int PAWN = 0b001;
    public static final int KNIGHT = 0b010;
    public static final int BISHOP = 0b100;
    public static final int QUEEN = 0b110;
    public static final int ROOK = 0b101;
    public static final int KING = 0b111;

    private static final int colorMask = 0b11000;
    private static final int pieceTypeMask = 0b111;
}