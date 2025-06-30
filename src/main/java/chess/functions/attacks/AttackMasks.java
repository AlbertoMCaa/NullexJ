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
                0x84800482D261C000L, 0x464000C1600B9000L, 0x6E000BD57E3FFE00L, 0x1180041088820000L, // need to find magic's without collisions
                0x82FFF7FF9E0BAF00L, 0xAF00310028063C00L, 0x09000F00298C1600L, 0x2B000AC604842100L,
                0xE00C8020400C8000L, 0x940C802000400580L, 0xA1190011410B2000L, 0x9876800800811000L,
                0x43A1000431000800L, 0x2962000810743200L, 0x0383008402003B00L, 0x0406000C92070844L,
                0x08632A8002804000L, 0x0093010021894000L, 0x6488C20022001280L, 0x89600A0010224200L,
                0xB83080801C004800L, 0xEC74808044000E00L, 0x21C424002210C108L, 0x3309220001108644L,
                0x082289A680044000L, 0x1450500AC0032000L, 0x50B4C5010010E000L, 0xE11FB80080801000L,
                0x4874018180141800L, 0xA0CA00D200500418L, 0x8322028400183011L, 0x880012820000CD04L,
                0xB828C003818004B0L, 0x8403022381004000L, 0xC190008314802000L, 0x0306409822001200L,
                0x5241001571002800L, 0x4504420080802400L, 0x900068660C000130L, 0x900144441A001289L,
                0x00C08CA840018000L, 0x4114820621020040L, 0x101D320144820020L, 0x5A460040E02A0050L,
                0xC020CA00AAE20010L, 0x7B0FFF9D873F400CL, 0x5083491806240010L, 0xD600A08108420014L,
                0x1208290200418600L, 0x04208141270A0200L, 0x21020142E0805200L, 0xAC42042010184200L,
                0x7469801800140180L, 0x01A20084A810A200L, 0x1200380210094400L, 0x85008904004D8200L,
                0x2321020060814032L, 0x08C0010272258041L, 0x980201408111202AL, 0x5600B43000602901L,
                0x6205313500180025L, 0x4C89002400580E21L, 0x1050220510008824L, 0x8820048400210042L
        };

        BISHOP_MAGICS = new long[] {
                0x4008200418807300L, 0x2010830205820000L, 0x1108062428A00000L, 0x2539240100000000L,
                0x289510C000000000L, 0x25120A3024000000L, 0x08A24C0C04400000L, 0x6302908088209000L,
                0x0042401C2404AA00L, 0x0020281828A08200L, 0x0501111C0400C000L, 0x04C24C1502000000L,
                0x0007CD1040000000L, 0x64200602C2200000L, 0x20012206A4244000L, 0x020C20608E109000L,
                0x4488401012506400L, 0x018800A218053400L, 0x04BC00080814A168L, 0x050A077402360000L,
                0x4F2A048401A20000L, 0x062B00C201099A00L, 0x4628508404020800L, 0x500220C2110D1800L,
                0x0028061040704E00L, 0x0818881120311100L, 0x7902501025030200L, 0x222E2800240040A8L,
                0xE5498400F7806000L, 0x0140E30182010104L, 0x202E140500450800L, 0x910202002289C104L,
                0x4410020883119000L, 0x0308182A01043400L, 0x21CC014900380A00L, 0x41566A0080180080L,
                0x6FC0004010150100L, 0x50C20806022B4140L, 0xC20A080200A44A00L, 0x219881020050D200L,
                0x00123802A8814000L, 0x0001088620961000L, 0x3960209150104800L, 0x6BA1062011001800L,
                0x3698440428200400L, 0x1040580A81A09100L, 0x0310192214000490L, 0x10213C0982001080L,
                0xA2040CC424200000L, 0x006D208C04200000L, 0xC800413108180000L, 0x31881022C2020000L,
                0x1100302A50240000L, 0x0C0028100D0E0000L, 0x22400ABA82020000L, 0x0910211243820000L,
                0x2AC3402805086040L, 0x4483404146082000L, 0x0800881161080880L, 0x9006600402608800L,
                0x060008800D504400L, 0x02809140D8018100L, 0x20005A480D080200L, 0x91C4411002060040L
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