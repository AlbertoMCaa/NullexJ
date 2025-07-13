package chess.functions.hash;

import chess.data.Position;

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

    public static long computeHash(Position position) {
        long hash = 0L;

        // Hash all pieces on the board
        hash ^= hashPieces(position.bitboards());

        // Hash side to move
        if (position.whiteToMove()) {
            hash ^= WHITE_TO_MOVE_KEY;
        }

        // Hash castling rights
        hash ^= CASTLING_KEYS[position.castlingRights() & 0xF];

        // Hash en passant square
        if (position.enPassantSquare() != -1) {
            int file = position.enPassantSquare() % 8;
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
}