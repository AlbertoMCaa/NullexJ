package chess.Board;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import chess.Board.Move.Move;
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
    private final Deque<Move> moveHistory = new ArrayDeque<>(); //save the move history

    static final int chessTiles = 64;
    static final int rows = 8;

    private boolean isWhiteToMove = true;
    private boolean isBotWhite;

    /*
     * Constructor of a normal chess board.
     */
    public static Board initiateChess()
    {
        Board board = new Board();
        String chessBoard = "rnbqkbnrpppppppp                                PPPPPPPPRNBQKBNR";
        
        board.arrayToBitboards(chessBoard);
        //board.printBitboardArray();
        return board;
    }
    public static Board getChessBoard(){
        return new Board();
    }

    public Board(){}
    public Board(Board other)
    {
        this.bitboards = Arrays.copyOf(other.bitboards, other.bitboards.length);
        this.enPassantSquare = other.enPassantSquare;
        this.isWhiteToMove = other.isWhiteToMove;
        this.isBotWhite = other.isBotWhite;
    }

/*
 * The first character of the string is the a8 tile and the last tile is the h1 tile. Left to right is the file and top to bottom is the rank.
 * Which means that the bitboard representation of the black rooks is : 10000001 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000
 * 
 * This method takes a Bitboards object, which is an array of 64-bit longs that represents the positions of the pieces on the board. Each piece type has it's own bitboard (long).
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
 * If the character is a non-recognized piece, the default case does nothing.
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
    public void placePiece(Piece piece)
    {
        int position = 63 - piece.getPiecePosition();
        int color = piece.getPieceColor();
        int type = piece.getPieceType();

        int boardIndex = -1;
        switch (type)
        {
            case 0b001: // Pawn
                boardIndex = (color == 0b10000) ? wP : bP;
                break;
            case 0b010: // Knight
                boardIndex = (color == 0b10000) ? wN : bN;
                break;
            case 0b100: // Bishop
                boardIndex = (color == 0b10000) ? wB : bB;
                break;
            case 0b101: // Rook
                boardIndex = (color == 0b10000) ? wR : bR;
                break;
            case 0b110: // Queen
                boardIndex = (color == 0b10000) ? wQ : bQ;
                break;
            case 0b111:  // King
                boardIndex = (color == 0b10000) ? wK : bK;
                break;
        }

        if (boardIndex != -1)
        {
            bitboards[boardIndex] |= (1L << position);
        }
    }

    public boolean isEmpty(int square)
    {
        long bit = 1L << 63 - square;
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
        long bit = 1L << 63 - position;
        for(int i = 0; i < bitboards.length; i++)
        {
            if ((bitboards[i] & bit) != 0) 
            {
                return i;
            }
        }
        return -1; // there is no piece at this position
    }

    public Integer getEnPassantSquare()
    {
        return enPassantSquare;
    }
    public void setEnPassantSquare(int enPassantSquare){
        this.enPassantSquare = enPassantSquare;
    }

    public boolean isKingInCheck(boolean isWhite)
    {
        long kingBitBoard = isWhite ? bitboards[wK] : bitboards[bK];
        //if (Long.bitCount(kingBitBoard) != 1) throw new RuntimeException("There cannot be more than one king per side!");
        int kingSquare = Long.numberOfTrailingZeros(kingBitBoard);

        if (hasDiagonalAttackers(kingSquare,isWhite)) return true;
        if (hasStraightAttackers(kingSquare,isWhite)) return true;

        long knightAttacks = calculateKnightAttacks(kingSquare);
        long enemyKnights = isWhite ? bitboards[bN] : bitboards[wN];
        if ((knightAttacks & enemyKnights) != 0) return true;

        long pawnAttacks = calculatePawnAttacks(kingSquare,isWhite);
        long enemyPawns = isWhite ? bitboards[bP] : bitboards[wP];
        if ((pawnAttacks & enemyPawns) != 0) return true;

        return false;
    }

    public void makeMove(int fromSquare, int toSquare)
    {
        int pieceCode = getPieceCode(fromSquare);
        int capturedPieceCode = getPieceCode(toSquare);

        //if (pieceCode == -1) return;

        Move move = new Move(fromSquare,toSquare,pieceCode,capturedPieceCode,enPassantSquare);
        moveHistory.push(move);

        long fromBit = 1L << (63 - fromSquare);
        long toBit = 1L << (63 - toSquare);

        bitboards[pieceCode] ^= fromBit | toBit; // XOR to toggle

        if (capturedPieceCode != -1)
        {
           bitboards[capturedPieceCode] &= ~toBit;  //bitboards[capturedPieceCode] ^= fromBit | toBit;  bitboards[capturedPieceCode] &= ~toBit;
        }

        isWhiteToMove = !isWhiteToMove;
        enPassantSquare = null;
    }

    public void unMakeMove()
    {
        Move move = moveHistory.pop();

        long fromBit = 1L << (63 - move.getFrom());
        long toBit = 1L << (63 - move.getTo());
        bitboards[move.getPiece()] ^= fromBit | toBit; // Toggle

        if (move.getCapturedPiece() != -1)
        {
            bitboards[move.getCapturedPiece()] |= toBit; // Restore captured piece
        }

        enPassantSquare = move.getEnPassantBefore();
    }

    private boolean hasStraightAttackers(int kingSquare, boolean isWhite)
    {
        int[] dirs = {-8, 8, -1, 1}; // Rectas
        long enemy = isWhite ? (bitboards[bR] | bitboards[bQ]) : (bitboards[wR] | bitboards[wQ]);
        return checkSlidingAttack(kingSquare, dirs, enemy);
    }

    private boolean hasDiagonalAttackers(int kingSquare, boolean isWhite)
    {
        int[] dirs = {-9, -7, 7, 9}; // Diagonales
        long enemy = isWhite ? (bitboards[bB] | bitboards[bQ]) : (bitboards[wB] | bitboards[wQ]);
        return checkSlidingAttack(kingSquare, dirs, enemy);
    }

    private long calculateKnightAttacks(int square)
    {
        long attacks = 0L;
        int[][] moves = {{2,1}, {2,-1}, {-2,1}, {-2,-1}, {1,2}, {1,-2}, {-1,2}, {-1,-2}};
        for (int[] m : moves)
        {
            int r = (square / 8) + m[0], c = (square % 8) + m[1];
            if (r >= 0 && r < 8 && c >= 0 && c < 8)
                attacks |= 1L << (r * 8 + c);
        }
        return attacks;
    }

    private long calculatePawnAttacks(int kingSquare, boolean isWhite)
    { //This can be cached for the same position / turn.    TODO

        long attacks = 0L;
        int row = kingSquare / 8, col = kingSquare % 8;
        if (isWhite)
        {
            if (row < 7)
            {
                if (col > 0) attacks |= 1L << (kingSquare + 7);
                if (col < 7) attacks |= 1L << (kingSquare + 9);
            }
        }
        else
        {
            if (row > 0)
            {
                if (col > 0) attacks |= 1L << (kingSquare - 9);
                if (col < 7) attacks |= 1L << (kingSquare - 7);
            }
        }
        return attacks;
    }

    private boolean checkSlidingAttack(int square, int[] dirs, long enemy)
    {
        for (int dir : dirs)
        {
            int pos = square + dir;
            int prevRow = square / 8, prevCol = square % 8;
            while (pos >= 0 && pos < 64)
            {
                int row = pos / 8, col = pos % 8;
                // Validar dirección (evitar wraps)
                if (Math.abs(row - prevRow) > 1 || Math.abs(col - prevCol) > 1) break;
                if ((enemy & (1L << pos)) != 0) return true;
                if (!isEmpty(pos)) break; // Bloqueo
                prevRow = row; prevCol = col;
                pos += dir;
            }
        }
        return false;
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

    public boolean isWhiteToMove() {
        return isWhiteToMove;
    }

    public void setWhiteToMove(boolean whiteToMove) {
        isWhiteToMove = whiteToMove;
    }
}
