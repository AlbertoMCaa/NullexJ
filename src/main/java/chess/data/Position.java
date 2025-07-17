package chess.data;

import java.util.Arrays;

public record Position(
        long[] bitboards,           // [wP, wN, wB, wR, wQ, wK, bP, bN, bB, bR, bQ, bK]
        boolean whiteToMove,
        byte castlingRights,
        int enPassantSquare,       // -1 if none
        int halfmoveClock,
        int fullmoveNumber,
        long occupied,             // Cached for performance
        long whitePieces,          // Cached for performance
        long blackPieces,           // Cached for performance
        long zobristHash
) {
    // Factory method for creating positions
    public static Position create(long[] bitboards, boolean whiteToMove,
                                  byte castlingRights, int enPassantSquare,
                                  int halfMoveClock, int fullMoveNumber, long zobristHash) {
        long white = bitboards[0] | bitboards[1] | bitboards[2] |
                bitboards[3] | bitboards[4] | bitboards[5];
        long black = bitboards[6] | bitboards[7] | bitboards[8] |
                bitboards[9] | bitboards[10] | bitboards[11];
        long occupied = white | black;

        return new Position(bitboards, whiteToMove, castlingRights,
                enPassantSquare, halfMoveClock, fullMoveNumber,
                occupied, white, black, zobristHash);
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Position other)) return false;

        // Zobrist hash comparison
        if (this.zobristHash() != other.zobristHash()) {
            return false;
        }

        // Fallback full equal implementation
        return Arrays.equals(this.bitboards, other.bitboards) &&
                this.whiteToMove == other.whiteToMove &&
                this.castlingRights == other.castlingRights &&
                this.enPassantSquare == other.enPassantSquare;
    }
}