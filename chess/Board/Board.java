package chess.Board;

import java.util.Arrays;

public class Board {
    static final int chessTiles = 64;
    static final int rows = 8;

    /*
     * Constructor of a normal chess board.
     */
    public static void initiateChess() {
        Bitboards bitboards = new Bitboards();
        String chessBoard = "rnbqkbnrpppppppp                                PPPPPPPPRNBQKBNR";
        arrayToBitboards(chessBoard, bitboards);
        //printBitboards(bitboards);
        printBitboardArray(bitboards);
    }
    /*
     * Bitboards are used to store the position of the pieces on the board.
     */
    public static class Bitboards {
        public long wP, wN, wB, wR, wQ, wK;
        public long bP, bN, bB, bR, bQ, bK;
    }
/*
 * The first character of the string is the a8 tile and the last tile is the h1 tile. Left to right is the file and top to bottom is the rank.
 * Which means that the bitboard representation of the black rooks is : 10000001 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000
 * 
 * This method takes a Bitboards object, which is a set of 64-bit longs that represents the positions of the pieces on the board. Each piece has it's own bitboard (long).
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
 * bit is a single-bit mask that shifts to the left  to represent the position in the bitboard. "chessTiles -1 -i" adjusts the index to match the bitboard convention (from a8 to h1)
 * In the first loop iteration, the bit is shifted 63 to the left (64 - 0 - 1) representating a8. Then an "or" operation is performed on the bitboard and the bit is set to 1. 
 * 
 * If the character is a non recognized piece, the default case does nothing.
 */
    public static void arrayToBitboards(String chessBoard, Bitboards bitboards) {
        for (int i = 0; i < chessTiles; i++) {
            long bit = 1L << (chessTiles - 1 - i);
            switch (chessBoard.charAt(i)) {
                case 'r':
                    bitboards.bR |= bit;
                    break;
                case 'n':
                    bitboards.bN |= bit;
                    break;
                case 'b':
                    bitboards.bB |= bit;
                    break;
                case 'q':
                    bitboards.bQ |= bit;
                    break;
                case 'k':
                    bitboards.bK |= bit;
                    break;
                case 'p':
                    bitboards.bP |= bit;
                    break;
                case 'R':
                    bitboards.wR |= bit;
                    break;
                case 'N':
                    bitboards.wN |= bit;
                    break;
                case 'B':
                    bitboards.wB |= bit;
                    break;
                case 'Q':
                    bitboards.wQ |= bit;
                    break;
                case 'K':
                    bitboards.wK |= bit;
                    break;
                case 'P':
                    bitboards.wP |= bit;
                    break;
                default:
                    break;
            }
        }
    }
    /*
     * Some code made just for debugging purposes.
     */
    @SuppressWarnings("unused")
    private static void printBitboards(Bitboards bitboards) {
        System.out.println("White Pawns: " + Long.toBinaryString(bitboards.wP));
        System.out.println("White Knights: " + Long.toBinaryString(bitboards.wN));
        System.out.println("White Bishops: " + Long.toBinaryString(bitboards.wB));
        System.out.println("White Rooks: " + Long.toBinaryString(bitboards.wR));
        System.out.println("White Queens: " + Long.toBinaryString(bitboards.wQ));
        System.out.println("White Kings: " + Long.toBinaryString(bitboards.wK));
        System.out.println("Black Pawns: " + Long.toBinaryString(bitboards.bP));
        System.out.println("Black Knights: " + Long.toBinaryString(bitboards.bN));
        System.out.println("Black Bishops: " + Long.toBinaryString(bitboards.bB));
        System.out.println("Black Rooks: " + Long.toBinaryString(bitboards.bR));
        System.out.println("Black Queens: " + Long.toBinaryString(bitboards.bQ));
        System.out.println("Black Kings: " + Long.toBinaryString(bitboards.bK));
    }
    @SuppressWarnings("unused")
    private static void printBitboardArray(Bitboards bitboard) {
        String chessBoard[][] = new String[8][8];
        for (int i = 0; i < chessTiles; i++) {
            chessBoard[i / 8][i % 8] = " ";
        }
        for (int i = 0; i < chessTiles; i++) {
            if (((bitboard.wK >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "K"; }
            if (((bitboard.bK >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "k"; }
            if (((bitboard.wQ >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "Q"; }
            if (((bitboard.bQ >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "q"; }
            if (((bitboard.wR >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "R"; }
            if (((bitboard.bR >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "r"; }
            if (((bitboard.wB >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "B"; }
            if (((bitboard.bB >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "b"; }
            if (((bitboard.wN >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "N"; }
            if (((bitboard.bN >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "n"; }
            if (((bitboard.wP >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "P"; }
            if (((bitboard.bP >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "p"; }
        }
        for (int i = 0; i < 8; i++) {
            System.out.println(Arrays.toString(chessBoard[i]));
        }
    }
}
