package chess.functions.hash;

import chess.data.*;
import chess.functions.moves.MoveApplication;

import java.util.random.RandomGenerator;

public final class ZobristHash {

    private static final long[][] PIECE_SQUARE_KEYS = new long[12][64];  // [piece][square]
    private static final long WHITE_TO_MOVE_KEY;
    private static final long[] CASTLING_KEYS = new long[16];            // All castling combinations
    private static final long[] EN_PASSANT_KEYS = new long[8];          // En passant file keys

    static {
        // Initialize with seed
        RandomGenerator rng = RandomGenerator.of("Xoshiro256PlusPlus");

        // Initialize piece-square keys
        for (int piece = 0; piece < 12; piece++) {
            for (int square = 0; square < 64; square++) {
                PIECE_SQUARE_KEYS[piece][square] = rng.nextLong();
            }
        }

        // Initialize side-to-move key
        WHITE_TO_MOVE_KEY = rng.nextLong();

        // Initialize castling rights keys (16 possible combinations)
        for (int rights = 0; rights < 16; rights++) {
            CASTLING_KEYS[rights] = rng.nextLong();
        }

        // Initialize en passant keys (one per file)
        for (int file = 0; file < 8; file++) {
            EN_PASSANT_KEYS[file] = rng.nextLong();
        }
    }

    // this contract should change, because now the position has a zobristHash row.
    public static long computeHash(long[] bitboard, boolean WhiteToMove, byte castlingRights, int enPassantTarget) {
        long hash = 0L;
        // Hash all pieces on the board
        hash ^= hashPieces(bitboard);

        // Hash side to move
        if (WhiteToMove) {
            hash ^= WHITE_TO_MOVE_KEY;
        }

        // Hash castling rights
        hash ^= CASTLING_KEYS[castlingRights & 0xF];

        // Hash en passant square
        if (enPassantTarget != -1) {
            int file = enPassantTarget % 8;
            hash ^= EN_PASSANT_KEYS[file];
        }

        return hash;
    }

    private static long hashPieces(long[] bitboards) {
        long hash = 0L;

        for (int piece = 0; piece < 12; piece++) {
            long pieceBitboard = bitboards[piece];

            // Process each piece of this type
            while (pieceBitboard != 0) {
                int square = Long.numberOfTrailingZeros(pieceBitboard);
                hash ^= PIECE_SQUARE_KEYS[piece][square];

                pieceBitboard &= pieceBitboard - 1; // Clear lowest set bit
            }
        }

        return hash;
    }

    public static long updateHashForNormalMove(long hash, int movingPiece, int capturedPiece, Position oldPos, int enPassantSquare, byte castlingRights, Move move) {
        Square from = Square.of(move.from());
        Square to = Square.of(move.to());

        // Remove moving piece from origin square
        hash ^= PIECE_SQUARE_KEYS[movingPiece][from.value()];

        // Add moving piece to destination square
        hash ^= PIECE_SQUARE_KEYS[movingPiece][to.value()];

        // Remove captured piece if any
        if (capturedPiece != -1) {
            hash ^= PIECE_SQUARE_KEYS[capturedPiece][to.value()];
        }

        // Update side to move
        hash ^= WHITE_TO_MOVE_KEY;

        // Update castling rights
        hash ^= CASTLING_KEYS[oldPos.castlingRights() & 0xF];
        hash ^= CASTLING_KEYS[castlingRights & 0xF];

        // Update en passant square
        hash = updateEnPassantHash(hash, oldPos.enPassantSquare(), enPassantSquare);

        return hash;
    }

    public static long updateHashForPromotionMove(long hash, int pawnIndex, int promotionPieceIndex, int capturedPiece, Position oldPos, byte castlingRights, Move move) {
        Square from = Square.of(move.from());
        Square to = Square.of(move.to());

        // Remove pawn from origin square
        hash ^= PIECE_SQUARE_KEYS[pawnIndex][from.value()];

        // Add promoted piece to destination square
        hash ^= PIECE_SQUARE_KEYS[promotionPieceIndex][to.value()];

        // Remove captured piece if any
        if (capturedPiece != -1) {
            hash ^= PIECE_SQUARE_KEYS[capturedPiece][to.value()];
        }

        // Update side to move
        hash ^= WHITE_TO_MOVE_KEY;

        // Update castling rights
        hash ^= CASTLING_KEYS[oldPos.castlingRights() & 0xF];
        hash ^= CASTLING_KEYS[castlingRights & 0xF];

        // Update en passant square (always -1 after promotion)
        hash = updateEnPassantHash(hash, oldPos.enPassantSquare(), -1);

        return hash;
    }

    public static long updateHashForEnPassantMove(long hash, int movingPawnIndex, int capturedPawnIndex, int capturedPawnSquare, Position oldPos, Move move) {
        Square from = Square.of(move.from());
        Square to = Square.of(move.to());

        // Remove moving pawn from origin square
        hash ^= PIECE_SQUARE_KEYS[movingPawnIndex][from.value()];

        // Add moving pawn to destination square
        hash ^= PIECE_SQUARE_KEYS[movingPawnIndex][to.value()];

        // Remove captured pawn
        hash ^= PIECE_SQUARE_KEYS[capturedPawnIndex][capturedPawnSquare];

        // Update side to move
        hash ^= WHITE_TO_MOVE_KEY;

        // Update en passant square (always -1 after en passant capture)
        hash = updateEnPassantHash(hash, oldPos.enPassantSquare(), -1);

        return hash;
    }

    public static long updateHashForCastlingMove(long hash, int kingIndex, int rookIndex, MoveApplication.CastlingPositions positions, Position oldPos, byte castlingRights , Move move) {
        Square kingFrom = Square.of(move.from());
        Square kingTo = Square.of(move.to());

        // Remove king from origin square
        hash ^= PIECE_SQUARE_KEYS[kingIndex][kingFrom.value()];

        // Add king to destination square
        hash ^= PIECE_SQUARE_KEYS[kingIndex][kingTo.value()];

        // Remove rook from origin square
        hash ^= PIECE_SQUARE_KEYS[rookIndex][positions.rookFrom().value()];

        // Add rook to destination square
        hash ^= PIECE_SQUARE_KEYS[rookIndex][positions.rookTo().value()];

        // Update side to move
        hash ^= WHITE_TO_MOVE_KEY;

        // Update castling rights
        hash ^= CASTLING_KEYS[oldPos.castlingRights() & 0xF];
        hash ^= CASTLING_KEYS[castlingRights & 0xF];

        // Update en passant square (always -1 after castling)
        hash = updateEnPassantHash(hash, oldPos.enPassantSquare(), -1);

        return hash;
    }

    private static long updateEnPassantHash(long hash, int oldEnPassant, int newEnPassant) {
        if (oldEnPassant != -1) {
            hash ^= EN_PASSANT_KEYS[oldEnPassant & 7];
        }
        if (newEnPassant != -1) {
            hash ^= EN_PASSANT_KEYS[newEnPassant & 7];
        }
        return hash;
    }
}