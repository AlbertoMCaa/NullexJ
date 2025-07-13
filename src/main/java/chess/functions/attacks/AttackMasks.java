package chess.functions.attacks;

import chess.data.Color;
import chess.data.Square;

public class AttackMasks {
    // Pre-computed attack tables
    private static final long[] KNIGHT_ATTACKS = new long[64];
    private static final long[] KING_ATTACKS = new long[64];
    private static final long[][] PAWN_ATTACKS = new long[2][64]; // [color][square]

    // Magic bitboard tables
    private static long[] ROOK_MAGICS = new long[64];
    private static final long[] ROOK_MASKS = new long[64];
    private static final int[] ROOK_BITS = new int[64];
    private static final long[][] ROOK_ATTACKS = new long[64][];

    private static long[] BISHOP_MAGICS = new long[64];
    private static final long[] BISHOP_MASKS = new long[64];
    private static final int[] BISHOP_BITS = new int[64];
    private static final long[][] BISHOP_ATTACKS = new long[64][];

    static {
        initializeAllTables();
    }

    private AttackMasks() {} // Utility class

    // Public API for attack calculations
    public static long knightAttacks(Square square) {
        return KNIGHT_ATTACKS[square.value()];
    }
    public static long kingAttacks(Square square) {
        return KING_ATTACKS[square.value()];
    }
    public static long pawnAttacks(Square square, Color color) {
        return PAWN_ATTACKS[color.index][square.value()];
    }

    public static long rookAttacks(Square square, long occupied) {
        int sq = square.value();
        occupied &= ROOK_MASKS[sq];
        occupied *= ROOK_MAGICS[sq];
        occupied >>>= (64 - ROOK_BITS[sq]);
        return ROOK_ATTACKS[sq][(int)occupied];
    }
    public static long bishopAttacks(Square square, long occupied) {
        int sq = square.value();
        occupied &= BISHOP_MASKS[sq];
        occupied *= BISHOP_MAGICS[sq];
        occupied >>>= (64 - BISHOP_BITS[sq]);
        return BISHOP_ATTACKS[sq][(int)occupied];
    }
    public static long queenAttacks(Square square, long occupied) {
        return rookAttacks(square, occupied) | bishopAttacks(square, occupied);
    }

    private static void initializeAllTables() {
        initializeKnightAttacks();
        initializeKingAttacks();
        initializePawnAttacks();
        initializeMagicBitboards();
    }

    private static void initializeKnightAttacks() {
        for (int square = 0; square < 64; square++) {
            long attacks = 0L;
            int rank = square / 8;
            int file = square % 8;

            int[][] offsets = {{2,1}, {1,2}, {-1,2}, {-2,1},
                    {-2,-1}, {-1,-2}, {1,-2}, {2,-1}};

            for (int[] offset : offsets) {
                int r = rank + offset[0];
                int f = file + offset[1];

                if (r >= 0 && r < 8 && f >= 0 && f < 8) {
                    attacks |= 1L << (r * 8 + f);
                }
            }
            KNIGHT_ATTACKS[square] = attacks;
        }
    }
    private static void initializeKingAttacks() {
        for (int square = 0; square < 64; square++) {
            long attacks = 0L;
            int rank = square / 8;
            int file = square % 8;

            for (int r = Math.max(0, rank - 1); r <= Math.min(7, rank + 1); r++) {
                for (int f = Math.max(0, file - 1); f <= Math.min(7, file + 1); f++) {
                    if (r != rank || f != file) {
                        attacks |= 1L << (r * 8 + f);
                    }
                }
            }
            KING_ATTACKS[square] = attacks;
        }
    }
    private static void initializePawnAttacks() {
        for (int square = 0; square < 64; square++) {
            int rank = square / 8;
            int file = square % 8;

            // White pawn attacks
            long whiteAttacks = 0L;
            if (rank < 7) {
                if (file > 0) whiteAttacks |= 1L << ((rank + 1) * 8 + (file - 1));
                if (file < 7) whiteAttacks |= 1L << ((rank + 1) * 8 + (file + 1));
            }
            PAWN_ATTACKS[0][square] = whiteAttacks;

            // Black pawn attacks
            long blackAttacks = 0L;
            if (rank > 0) {
                if (file > 0) blackAttacks |= 1L << ((rank - 1) * 8 + (file - 1));
                if (file < 7) blackAttacks |= 1L << ((rank - 1) * 8 + (file + 1));
            }
            PAWN_ATTACKS[1][square] = blackAttacks;
        }
    }
    private static void initializeMagicBitboards() {
        // Initialize magic numbers
        ROOK_MAGICS = new long[] {
                0x0080011040008020L, 0x0440002008100040L, 0x0080200010001880L, 0x0080048010008800L,
                0x0200020010042008L, 0x0180010200040080L, 0x0080060008800100L, 0x0080022100004080L,
                0x0010800420804000L, 0x0400401000200440L, 0x0003001040200100L, 0x0000800800823000L,
                0x4040808004000800L, 0x0200808044000200L, 0x0005000200010084L, 0x0208800040800100L,
                0x0480008020804000L, 0x0400808040002004L, 0x0003010020004010L, 0x0090004040080400L,
                0x1008008008040080L, 0x3000808004000200L, 0x0042004080400100L, 0x0000020000408124L,
                0x0001400080208002L, 0x0000400080200182L, 0x0000100480200480L, 0x0040090100100020L,
                0x0008110100040800L, 0x0084010040400200L, 0x0401000100020014L, 0x0003000100004082L,
                0x0000864000800020L, 0x0040008040802004L, 0x0000904101002000L, 0x0001080084801000L,
                0x8000040280800800L, 0x0011000803000400L, 0x0001000C21000200L, 0x0200802040800100L,
                0x0080209040008000L, 0x0010002010C04000L, 0x0020200041010010L, 0x000010200A020040L,
                0x0000900801010004L, 0x2000140002008080L, 0x0000A20004010100L, 0x0001000440810002L,
                0x4280004000200040L, 0x0011804000200080L, 0x0000421100200100L, 0x0800880080100080L,
                0x0800040080880080L, 0x1001040080020080L, 0x0001004200044100L, 0x0400800041000280L,
                0x0000800020410011L, 0x0000400080102101L, 0x0000410010200009L, 0x0000100100200805L,
                0x0002001004200802L, 0x0002000410080102L, 0x0001000200208401L, 0x0000002040810402L
        };
        BISHOP_MAGICS = new long[] {
                0x0008200400404100L, 0x000C040404002000L, 0x0008080500201000L, 0x0004050208000400L,
                0x0022021000000180L, 0x8001042004200000L, 0x4001041004040000L, 0x0000804410010800L,
                0x0080100410040040L, 0x0000020202022200L, 0x0000100106006000L, 0x2001024081000000L,
                0x0000020210002402L, 0x0000430420040000L, 0x00000A0801041000L, 0x0000002088041040L,
                0x0040000410042100L, 0x0020000401060200L, 0x1008001000801010L, 0x0008000401202010L,
                0x0804001080A00000L, 0x0041000080414000L, 0x0004040082011000L, 0x0000800042080140L,
                0x00044000A0020400L, 0x0008080020018100L, 0x0000221010008200L, 0x0024004004010002L,
                0x0800840000802002L, 0x0008104002010080L, 0x0001011004008800L, 0x0004208001004100L,
                0x0008041010042000L, 0x0008012400080800L, 0x0040802080040800L, 0x0000401808008200L,
                0x0800408020020200L, 0x00008101000A1000L, 0x0005040080040200L, 0x2008010020004200L,
                0x0002022020000408L, 0x0082020202002000L, 0x0000104028001004L, 0x0000804012001040L,
                0x8000400081200200L, 0x0802200041000080L, 0x0004010401000401L, 0x0010014200200080L,
                0x0001011010040020L, 0x0000208808084000L, 0x0400008400880010L, 0x0402000084040100L,
                0x0006201002020000L, 0x0200082008008010L, 0x00200801010C0000L, 0x0208100082004000L,
                0x0000820801010800L, 0x0080002404040400L, 0x0000000084208820L, 0x0000002005420200L,
                0x0000000008912400L, 0x0000001042101100L, 0x0002200802080040L, 0x0010200080820040L
        };

        // Initialize masks and attack tables
        for (int square = 0; square < 64; square++) {
            ROOK_MASKS[square] = createRookMask(square);
            BISHOP_MASKS[square] = createBishopMask(square);
            ROOK_BITS[square] = Long.bitCount(ROOK_MASKS[square]);
            BISHOP_BITS[square] = Long.bitCount(BISHOP_MASKS[square]);

            // Initialize attack tables
            ROOK_ATTACKS[square] = new long[1 << ROOK_BITS[square]];
            BISHOP_ATTACKS[square] = new long[1 << BISHOP_BITS[square]];

            initSliderAttacks(square, true);  // rook
            initSliderAttacks(square, false); // bishop
        }
    }

    private static void initSliderAttacks(int square, boolean isRook) {
        long mask = isRook ? ROOK_MASKS[square] : BISHOP_MASKS[square];
        int bits = (isRook ? ROOK_BITS[square] : BISHOP_BITS[square]);

        for (int i = 0; i < (1 << bits); i++) {
            long occupied = createOccupancyFromIndex(i, mask);
            long attacks = isRook ? createRookAttacks(square, occupied) : createBishopAttacks(square, occupied);

            long magic = isRook ? ROOK_MAGICS[square] : BISHOP_MAGICS[square];
            int index = (int)((occupied * magic) >>> (64 - bits)); // guchi

            if (isRook) {
                ROOK_ATTACKS[square][index] = attacks;
            } else {
                BISHOP_ATTACKS[square][index] = attacks;
            }
        }
    }
    private static long createRookMask(int square) {
        long mask = 0L;
        int rank = square / 8;
        int file = square % 8;

        // Horizontal
        for (int f = 1; f < 7; f++) {
            if (f != file) {
                mask |= 1L << (rank * 8 + f);
            }
        }
        // Vertical
        for (int r = 1; r < 7; r++) {
            if (r != rank) {
                mask |= 1L << (r * 8 + file);
            }
        }

        return mask;
    }
    private static long createBishopMask(int square) {
        long mask = 0L;
        int rank = square / 8;
        int file = square % 8;

        // Diagonal directions
        int[][] directions = {{1,1}, {1,-1}, {-1,1}, {-1,-1}};

        for (int[] dir : directions) {
            int r = rank + dir[0];
            int f = file + dir[1];

            while (r >= 1 && r <= 6 && f >= 1 && f <= 6) {
                mask |= 1L << (r * 8 + f);
                r += dir[0];
                f += dir[1];
            }
        }
        return mask;
    }

    private static long createOccupancyFromIndex(int index, long mask) {
        long occupancy = 0L;
        int bitIndex = 0;

        while (mask != 0) {
            int square = Long.numberOfTrailingZeros(mask);
            if ((index & (1 << bitIndex)) != 0) {
                occupancy |= 1L << square;
            }
            mask &= mask - 1;
            bitIndex++;
        }

        return occupancy;
    }
    private static long createRookAttacks(int square, long occupied) {
        long attacks = 0L;
        int rank = square / 8;
        int file = square % 8;

        // Horizontal and vertical directions
        int[][] directions = {{0,1}, {0,-1}, {1,0}, {-1,0}};

        for (int[] dir : directions) {
            int r = rank + dir[0];
            int f = file + dir[1];

            while (r >= 0 && r < 8 && f >= 0 && f < 8) {
                int targetSquare = r * 8 + f;
                attacks |= 1L << targetSquare;

                if ((occupied & (1L << targetSquare)) != 0) {
                    break;
                }

                r += dir[0];
                f += dir[1];
            }
        }

        return attacks;
    }
    private static long createBishopAttacks(int square, long occupied) {
        long attacks = 0L;
        int rank = square / 8;
        int file = square % 8;

        // Diagonal directions
        int[][] directions = {{1,1}, {1,-1}, {-1,1}, {-1,-1}};

        for (int[] dir : directions) {
            int r = rank + dir[0];
            int f = file + dir[1];

            while (r >= 0 && r < 8 && f >= 0 && f < 8) {
                int targetSquare = r * 8 + f;
                attacks |= 1L << targetSquare;

                if ((occupied & (1L << targetSquare)) != 0) {
                    break;
                }

                r += dir[0];
                f += dir[1];
            }
        }

        return attacks;
    }
}