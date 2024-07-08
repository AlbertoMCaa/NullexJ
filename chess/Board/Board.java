package chess.Board;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import chess.Pieces.Piece;

public class Board {

    static final int wP = 0;
    static final int wN = 1;
    static final int wB = 2;
    static final int wR = 3;
    static final int wQ = 4;
    static final int wK = 5;
    static final int bP = 6;
    static final int bN = 7;
    static final int bB = 8;
    static final int bR = 9;
    static final int bQ = 10;
    static final int bK = 11;

    private long[] bitboards = new long[12];
    private Integer enPassantSquare = null;

    static final int chessTiles = 64;
    static final int rows = 8;

    /*
     * Constructor of a normal chess board.
     */
    public static void initiateChess()
    {
        Board board = new Board();
        String chessBoard = "rnbqkbnrpppppppp                                PPPPPPPPRNBQKBNR";
        
        board.arrayToBitboards(chessBoard);
        board.printBitboardArray();
    }
    /*
     * Bitboards are used to store the position of the pieces on the board.
     */

/*
 * The first character of the string is the a8 tile and the last tile is the h1 tile. Left to right is the file and top to bottom is the rank.
 * Which means that the bitboard representation of the black rooks is : 10000001 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000
 * 
 * This method takes a Bitboards object, which is an array of 64-bit longs that represents the positions of the pieces on the board. Each piece has it's own bitboard (long).
 * This method also takes a String representation of the chess board in one dimensional array.
 * 
 * This representation of the board works as follows:
 *  Each piece is represented by a character in the string.
 *  If the character is a capital letter, the piece is white. Otherwise the piece is black.
 * 
 *  R = rook
 *  N = knight
 *  Q = queen
 *  K = king
 *  B = bishop
 *  P = pawn
 * 
 * First, we loop through each character in the string. 
 * "bit" is a single-bit mask that shifts to the left  to represent the position in the bitboard. "chessTiles -1 -i" adjusts the index to match the bitboard convention (from a8 to h1)
 * In the first loop iteration, the bit is shifted 63 to the left (64 - 0 - 1) representating a8. Then an "or" operation is performed on the bitboard and the bit is set to 1. 
 * 
 * If the character is a non recognized piece, the default case does nothing.
 */
    public void arrayToBitboards(String chessBoard)
    {
        for (int i = 0; i < chessTiles; i++)
        {
            long bit = 1L << (chessTiles - 1 - i);
            switch (chessBoard.charAt(i))
            {
                case 'r':
                    bitboards[bR] |= bit;
                    break;
                case 'n':
                    bitboards[bN] |= bit;
                    break;
                case 'b':
                    bitboards[bB] |= bit;
                    break;
                case 'q':
                    bitboards[bQ] |= bit;
                    break;
                case 'k':
                    bitboards[bK] |= bit;
                    break;
                case 'p':
                    bitboards[bP] |= bit;
                    break;
                case 'R':
                    bitboards[wR] |= bit;
                    break;
                case 'N':
                    bitboards[wN] |= bit;
                    break;
                case 'B':
                    bitboards[wB] |= bit;
                    break;
                case 'Q':
                    bitboards[wQ] |= bit;
                    break;
                case 'K':
                    bitboards[wK] |= bit;
                    break;
                case 'P':
                    bitboards[wP] |= bit;
                    break;
                default:
                    break;
            }
        }
    }

    public boolean isEmpty(int square)
    {
        long bit = 1L << square;
        for(long bitboard : bitboards)
        {
            if ((bitboard & bit)!= 0) 
            {
                return false;
            }
        }
        return true;
    }
    public int getPieceCode(int position)
    {
        long bit = 1L << position;
        for(int i = 0; i < bitboards.length; i++)
        {
            if ((bitboards[i] & bit) != 0) 
            {
                return i;
            }
        }
        return -1; // there is no piece at this position
    }

    public int getEnPassantSquare()
    {
        return enPassantSquare;
    }
    public void setEnPassantSquare(int enPassantSquare){
        this.enPassantSquare = enPassantSquare;
    }
    /*
     * Some code made just for debugging purposes.
     */
    @SuppressWarnings("unused")
    private void printBitboards()
    {
        System.out.println("White Pawns: " + Long.toBinaryString(bitboards[wP]));
        System.out.println("White Knights: " + Long.toBinaryString(bitboards[wN]));
        System.out.println("White Bishops: " + Long.toBinaryString(bitboards[wB]));
        System.out.println("White Rooks: " + Long.toBinaryString(bitboards[wR]));
        System.out.println("White Queens: " + Long.toBinaryString(bitboards[wQ]));
        System.out.println("White Kings: " + Long.toBinaryString(bitboards[wK]));
        System.out.println("Black Pawns: " + Long.toBinaryString(bitboards[bP]));
        System.out.println("Black Knights: " + Long.toBinaryString(bitboards[bN]));
        System.out.println("Black Bishops: " + Long.toBinaryString(bitboards[bB]));
        System.out.println("Black Rooks: " + Long.toBinaryString(bitboards[bR]));
        System.out.println("Black Queens: " + Long.toBinaryString(bitboards[bQ]));
        System.out.println("Black Kings: " + Long.toBinaryString(bitboards[bK]));
    }
    @SuppressWarnings("unused")
    private void printBitboardArray()
    {
        String chessBoard[][] = new String[8][8];
        for (int i = 0; i < chessTiles; i++)
        {
            chessBoard[i / 8][i % 8] = " ";
        }
        for (int i = 0; i < chessTiles; i++)
        {
            if (((bitboards[wK] >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "K"; }
            if (((bitboards[bK] >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "k"; }
            if (((bitboards[wQ] >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "Q"; }
            if (((bitboards[bQ] >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "q"; }
            if (((bitboards[wR] >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "R"; }
            if (((bitboards[bR] >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "r"; }
            if (((bitboards[wB] >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "B"; }
            if (((bitboards[bB] >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "b"; }
            if (((bitboards[wN] >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "N"; }
            if (((bitboards[bN] >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "n"; }
            if (((bitboards[wP] >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "P"; }
            if (((bitboards[bP] >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "p"; }
        }
        for (int i = 0; i < 8; i++)
        {
            System.out.println(Arrays.toString(chessBoard[i]));
        }
    }
}
