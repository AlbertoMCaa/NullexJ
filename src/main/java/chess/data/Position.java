package chess.data;

public record Position(
        long[] bitboards,           // [wP, wN, wB, wR, wQ, wK, bP, bN, bB, bR, bQ, bK]
        boolean whiteToMove,
        byte castlingRights,
        int enPassantSquare,       // -1 if none
        int halfmoveClock,
        int fullmoveNumber,
        long occupied,             // Cached for performance
        long whitePieces,          // Cached for performance
        long blackPieces           // Cached for performance
) {
    // Factory method for creating positions
    public static Position create(long[] bitboards, boolean whiteToMove,
                                  byte castlingRights, int enPassantSquare,
                                  int halfMoveClock, int fullMoveNumber) {
        long white = bitboards[0] | bitboards[1] | bitboards[2] |
                bitboards[3] | bitboards[4] | bitboards[5];
        long black = bitboards[6] | bitboards[7] | bitboards[8] |
                bitboards[9] | bitboards[10] | bitboards[11];
        long occupied = white | black;

        return new Position(bitboards.clone(), whiteToMove, castlingRights,
                enPassantSquare, halfMoveClock, fullMoveNumber,
                occupied, white, black);
    }

    public long friendlyPieces() {
        return whiteToMove ? whitePieces : blackPieces;
    }

    public long enemyPieces() {
        return whiteToMove ? blackPieces : whitePieces;
    }

    public int pieceAt(int square) {
        long mask = 1L << square;
        for (int piece = 0; piece < 12; piece++) {
            if ((bitboards[piece] & mask) != 0) {
                return piece;
            }
        }
        return -1;
    }
}