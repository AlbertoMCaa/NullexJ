package chess.Board;

import chess.Board.Move.Move;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static chess.Board.Board.*;

public abstract class BoardUtils
{
    static final int chessTiles = 64;
    static final int rows = 8;

    // Precalculated list of pseudo legal moves for all the pieces
    public static final long[] KNIGHT_MOVES = new long[chessTiles];
    public static final long[] BISHOP_MOVES = new long[chessTiles];
    public static final long[] ROOK_MOVES = new long[chessTiles];
    public static final long[] QUEEN_MOVES = new long[chessTiles];

    public static final long FIRST_COLUMN = 0x0101010101010101L;
    public static final long SECOND_COLUMN = 0x0202020202020202L;
    public static final long SEVENTH_COLUMN = 0x4040404040404040L;
    public static final long EIGHTH_COLUMN = 0x8080808080808080L;

    //public static final boolean[] FIRST_COLUMN = initFiles(0);
    //public static final boolean[] SECOND_COLUMN = initFiles(1);
    public static final boolean[] THIRD_COLUMN = initFiles(2);
    public static final boolean[] FOURTH_COLUMN = initFiles(3);
    public static final boolean[] FIFTH_COLUMN = initFiles(4);
    public static final boolean[] SIXTH_COLUMN = initFiles(5);
    //public static final boolean[] SEVENTH_COLUMN = initFiles(6);
    //public static final boolean[] EIGHTH_COLUMN = initFiles(7);

    public static final boolean[] EIGHTH_RANK = initRanks(0);
    public static final boolean[] SEVENTH_RANK = initRanks(1);
    public static final boolean[] SIXTH_RANK = initRanks(2);
    public static final boolean[] FIFTH_RANK = initRanks(3);
    public static final boolean[] FOURTH_RANK = initRanks(4);
    public static final boolean[] THIRD_RANK = initRanks(5);
    public static final boolean[] SECOND_RANK = initRanks(6);
    public static final boolean[] FIRST_RANK = initRanks(7);

    private BoardUtils()
    {
        throw new IllegalStateException("Utility class");
    }
    /*
     * Method used to check if a given coordinate is within the bounds of the chess boards.
     * 
     * Returns true if coordinate is equal to or greater than 0 and less than 64.
     */
    public static boolean isValidCoordinate(final int coordinates)
    {
        return coordinates >= 0 && coordinates < 64;
    }

    /*
     * Initialize a column array. This is used to speed up the process of checking if a tile is in a given column. This is only performed once.
     * 
    */
    private static boolean[] initFiles(int columnNumber)
    {
        boolean[] column = new boolean[64];
        int startOffset = columnNumber; // Start from the given column index at the top (a8, b8, ..., h8)
    
        while (startOffset < 64) {
            column[startOffset] = true;
            startOffset += 8; // Move to the next rank within the same column
        }
        return column;
    }

    /*
     * Initialize a rank array. This is used to speed up the process of checking if a tile is in a given rank. This is only performed once.
    */
    public static boolean[] initRanks(int rankNumber)
    {
        boolean[] rank = new boolean[64];
        int startOffset = rankNumber * 8; // Calculate the starting index for the rank (0-7 for a8-h8, 8-15 for a7-h7, ..., 56-63 for a1-h1)
        int endOffset = startOffset + 8;  // Calculate the ending index for the rank

        for (int i = startOffset; i < endOffset; i++)
        {
            rank[i] = true;
        }
        return rank;
    }
    public static boolean isSameFile(int destinationSquare, int sourcePosition)
    {
        return destinationSquare % 8 == sourcePosition % 8;
    }
    public static boolean isSameRank(int destinationSquare, int sourcePosition)
    {
        return destinationSquare / 8 == sourcePosition / 8;
    }
    public static boolean isSameColor(int pieceCode, int pieceCode2)
    {
        return ((pieceCode & 0b11000) == (pieceCode2 & 0b11000));
    }

    //Columns

    public static boolean isFirstColumn(int position)
    {
        return position % 8 == 0;
    }
    public static boolean isSecondColumn(int position)
    {
        return position % 8 == 1;
    }
    public static boolean isSeventhColumn(int position) 
    {
        return position % 8 == 6;
    }
    public static boolean isEighthColumn(int position) 
    {
        return position % 8 == 7;
    }

    //Rows 

    public static boolean isFirstRow(int position) 
    {
        return position / 8 == 0;
    }
    public static boolean isSecondRow(int position) 
    {
        return position / 8 == 1;
    }
    public static boolean isSeventhRow(int position) 
    {
        return position / 8 == 6;
    }
    public static boolean isEighthRow(int position) 
    {
        return position / 8 == 7;
    }

    /*
     * Some code made just for debugging purposes.
     */
    @SuppressWarnings("unused")
    public static void printBitboards(Board board) {
        final int NAME_WIDTH = 15; // Adjust this to fit longest name + spacing

        for (int i = 0; i < board.bitboards.length; i++) {
            String pieceName = switch(i) {
                case wP -> "White Pawns";
                case wN -> "White Knights";
                case wB -> "White Bishops";
                case wR -> "White Rooks";
                case wQ -> "White Queens";
                case wK -> "White Kings";
                case bP -> "Black Pawns";
                case bN -> "Black Knights";
                case bB -> "Black Bishops";
                case bR -> "Black Rooks";
                case bQ -> "Black Queens";
                case bK -> "Black Kings";
                default -> "Unknown";
            };

            String bits = Long.toBinaryString(board.bitboards[i]);
            String paddedBits = String.format("%64s", bits).replace(' ', '0'); // pad to 64 bits
            System.out.printf("%-" + NAME_WIDTH + "s: %s%n", pieceName, paddedBits);
        }
    }

    @SuppressWarnings("unused")
    public static void printBitboardArray(Board board)
    {
        String chessBoard[][] = new String[8][8];
        for (int i = 0; i < chessTiles; i++)
        {
            chessBoard[i / 8][i % 8] = " ";
        }
        for (int i = 0; i < chessTiles; i++)
        {
            if (((board.bitboards[wK] >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "K"; }
            if (((board.bitboards[bK] >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "k"; }
            if (((board.bitboards[wQ] >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "Q"; }
            if (((board.bitboards[bQ] >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "q"; }
            if (((board.bitboards[wR] >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "R"; }
            if (((board.bitboards[bR] >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "r"; }
            if (((board.bitboards[wB] >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "B"; }
            if (((board.bitboards[bB] >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "b"; }
            if (((board.bitboards[wN] >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "N"; }
            if (((board.bitboards[bN] >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "n"; }
            if (((board.bitboards[wP] >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "P"; }
            if (((board.bitboards[bP] >> i) & 1) == 1) { chessBoard[7 - (i / 8)][i % 8] = "p"; }
        }
        for (int i = 0; i < 8; i++)
        {
            System.out.println(Arrays.toString(chessBoard[i]));
        }
    }

    /*
        Moves
     */

    public static long getLegalMoves(Board board, int square)
    {
        int pieceCode = board.getPieceBitBoardCode(square);
        if (pieceCode == -1) return 0L;

        /*
        return switch (pieceCode) {
            case Board.wP -> PawnMoveHelper.getPawnPushes(board, square, true) |
                    PawnMoveHelper.getPawnAttacks(square, true);
            case Board.bP -> PawnMoveHelper.getPawnPushes(board, square, false) |
                    PawnMoveHelper.getPawnAttacks(square, false);
            case Board.wN, Board.bN -> KNIGHT_MOVES[square];
            case Board.wB, Board.bB -> BISHOP_MOVES[square];
            case Board.wR, Board.bR -> ROOK_MOVES[square];
            case Board.wQ, Board.bQ -> QUEEN_MOVES[square];
            case Board.wK, Board.bK -> MoveGenerator.generateKingMoves(board, square);
            default -> 0L;
        } & ~board.getFriendlyPieces(pieceCode < 6);

         */
        return 1L;
    }
    public static List<Move> generateAllLegalMoves(Board board)
    {
        List<Move> moves = new ArrayList<>();


        return moves;
    }

    // Helper methods to get file and rank (0-based)
    public static int getFile(int square) {
        return square % 8;
    }

    public static int getRank(int square) {
        return square / 8;
    }
}
