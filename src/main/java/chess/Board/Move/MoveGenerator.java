package chess.Board.Move;

import chess.Board.Board;
import chess.Pieces.Piece;

import java.util.ArrayList;
import java.util.List;

import static chess.Board.Board.*;
import static chess.Board.Move.Move.*;

public class MoveGenerator {
    public static final long[] KNIGHT_ATTACKS = new long[64];
    public static final long[] KING_ATTACKS = new long[64];
    public static final long[][] PAWN_ATTACKS = new long[2][64]; // [color][square]

    // Precomputed magic numbers and attack tables
    private static long[] ROOK_MAGICS = new long[64];
    private static final long[] ROOK_MASKS = new long[64];
    private static final int[] ROOK_BITS = new int[64];
    private static final long[][] ROOK_ATTACKS = new long[64][];

    private static long[] BISHOP_MAGICS = new long[64];
    private static final long[] BISHOP_MASKS = new long[64];
    private static final int[] BISHOP_BITS = new int[64];
    private static final long[][] BISHOP_ATTACKS = new long[64][];

    // Pawn move constants for a1=0 representation
    private static final long RANK_2 = 0x000000000000FF00L; // White pawn starting rank (a2-h2)
    private static final long RANK_7 = 0x00FF000000000000L; // Black pawn starting rank (a7-h7)
    private static final long RANK_1 = 0x00000000000000FFL; // White promotion rank (a1-h1)
    private static final long RANK_8 = 0xFF00000000000000L; // Black promotion rank (a8-h8)

    // Castling masks for a1=0 board
    private static final long WHITE_KINGSIDE_EMPTY = 0x0000000000000060L;  // f1, g1 (bits 5,6)
    private static final long WHITE_QUEENSIDE_EMPTY = 0x000000000000000EL; // b1, c1, d1 (bits 1,2,3)
    private static final long BLACK_KINGSIDE_EMPTY = 0x6000000000000000L;  // f8, g8 (bits 61,62)
    private static final long BLACK_QUEENSIDE_EMPTY = 0x0E00000000000000L; // b8, c8, d8 (bits 57,58,59)

    static {
        initializeAttackTables();
        initializeMagicBitboards();
    }

    public static List<Move> generateLegalMoves(Board board) {
        List<Move> legalMoves = new ArrayList<>(64);
        boolean isWhite = board.isWhiteToMove();

        // Generate all pseudo-legal moves
        generatePawnMoves(board, legalMoves, isWhite);
        generateKnightMoves(board, legalMoves, isWhite);
        generateBishopMoves(board, legalMoves, isWhite);
        generateRookMoves(board, legalMoves, isWhite);
        generateQueenMoves(board, legalMoves, isWhite);
        generateKingMoves(board, legalMoves, isWhite);

        // Filter out moves that leave king in check
        legalMoves.removeIf(move -> leavesKingInCheck(move.getFrom(), move.getTo(), move.getCapturedPiece(), board));

        return legalMoves;
    }

    // Magic Bitboard methods
    private static long getRookAttacks(int square, long occupied) {
        occupied &= ROOK_MASKS[square];
        occupied *= ROOK_MAGICS[square];
        occupied >>>= (64 - ROOK_BITS[square]);
        return ROOK_ATTACKS[square][(int)occupied];
    }

    private static long getBishopAttacks(int square, long occupied) {
        occupied &= BISHOP_MASKS[square];
        occupied *= BISHOP_MAGICS[square];
        occupied >>>= (64 - BISHOP_BITS[square]);
        return BISHOP_ATTACKS[square][(int)occupied];
    }

    private static long getQueenAttacks(int square, long occupied) {
        return getRookAttacks(square, occupied) | getBishopAttacks(square, occupied);
    }

    // Piece-specific generators

    private static void generatePawnMoves(Board board, List<Move> moves, boolean isWhite) {
        long pawns = board.bitboards[isWhite ? wP : bP];
        long friendlyPieces = board.getFriendlyPieces();
        long enemyPieces = board.getOccupied() & ~friendlyPieces;
        long emptySquares = ~board.getOccupied();

        int direction = isWhite ? 8 : -8;
        int startRank = isWhite ? 1 : 6;
        long promotionRank = isWhite ? RANK_8 : RANK_1;

        while (pawns != 0) {
            int fromSquare = Long.numberOfTrailingZeros(pawns);
            int rank = fromSquare / 8;
            int file = fromSquare % 8;

            // Single pawn push
            int singlePushSquare = fromSquare + direction;
            if (singlePushSquare >= 0 && singlePushSquare < 64 &&
                    (emptySquares & (1L << singlePushSquare)) != 0) {

                if (((1L << singlePushSquare) & promotionRank) != 0) {
                    // Promotion moves
                    addPromotionMoves(moves, fromSquare, singlePushSquare, false, isWhite, board);
                } else {
                    moves.add(Move.create(
                            fromSquare, singlePushSquare, 0,
                            isWhite ? Piece.WHITE_PAWN : Piece.BLACK_PAWN,
                            NO_CAPTURE, false, false, board.getCastlingRights()
                    ));
                }

                // Double pawn push
                if (rank == startRank) {
                    int doublePushSquare = fromSquare + (direction * 2);
                    if ((emptySquares & (1L << doublePushSquare)) != 0) {
                        moves.add(Move.create(
                                fromSquare, doublePushSquare, 0,
                                isWhite ? Piece.WHITE_PAWN : Piece.BLACK_PAWN,
                                NO_CAPTURE, false, false, board.getCastlingRights()
                        ));
                    }
                }
            }

            // Pawn captures
            generatePawnCapturesFromSquare(board, moves, fromSquare, isWhite, enemyPieces, promotionRank);

            pawns &= pawns - 1;
        }

        // En passant
        generateEnPassantMoves(board, moves, isWhite);
    }

    // Other pieces
    private static void generateKnightMoves(Board board, List<Move> moves, boolean isWhite) {
        long knights = board.bitboards[isWhite ? wN : bN];
        final long friendlyPieces = board.getFriendlyPieces();
        final long enemyPieces = board.getOccupied() & ~friendlyPieces;

        while (knights != 0) {
            final int fromSquare = Long.numberOfTrailingZeros(knights);
            long attacks = KNIGHT_ATTACKS[fromSquare] & ~friendlyPieces;

            while (attacks != 0) {
                final int toSquare = Long.numberOfTrailingZeros(attacks);
                final boolean isCapture = (enemyPieces & (1L << toSquare)) != 0;

                moves.add(Move.create(
                        fromSquare, toSquare,
                        0, isWhite ? Piece.WHITE_KNIGHT : Piece.BLACK_KNIGHT,
                        isCapture ? board.getPieceCode(toSquare) : NO_CAPTURE,
                        false, false, board.getCastlingRights()
                ));
                attacks &= attacks - 1;
            }
            knights &= knights - 1;
        }
    }
    private static void generateKingMoves(Board board, List<Move> moves, boolean isWhite) {
        long king = board.bitboards[isWhite ? wK : bK];
        final long friendlyPieces = board.getFriendlyPieces();
        final long enemyPieces = board.getOccupied() & ~friendlyPieces;

        while (king != 0) {
            final int fromSquare = Long.numberOfTrailingZeros(king);
            long attacks = KING_ATTACKS[fromSquare] & ~friendlyPieces;

            while (attacks != 0) {
                final int toSquare = Long.numberOfTrailingZeros(attacks);
                final boolean isCapture = (enemyPieces & (1L << toSquare)) != 0;

                moves.add(Move.create(
                        fromSquare, toSquare, 0,
                        isWhite ? Piece.WHITE_KING : Piece.BLACK_KING,
                        isCapture ? board.getPieceCode(toSquare) : NO_CAPTURE,
                        false, false, board.getCastlingRights()
                ));
                attacks &= attacks - 1;
            }
            king &= king - 1;
        }
        generateCastlingMoves(board, moves, isWhite);
    }

    // Sliding Pieces
    private static void generateBishopMoves(Board board, List<Move> moves, boolean isWhite) {
        long bishops = isWhite ? board.getBitboard(Board.wB) : board.getBitboard(Board.bB);
        long friendlyPieces = board.getFriendlyPieces();
        long occupied = board.getOccupied();

        while (bishops != 0) {
            int fromSquare = Long.numberOfTrailingZeros(bishops);
            long attacks = getBishopAttacks(fromSquare, occupied) & ~friendlyPieces;

            addMovesFromBitboard(fromSquare, attacks, board, moves,
                    isWhite ? Piece.WHITE_BISHOP : Piece.BLACK_BISHOP);
            bishops &= bishops - 1;
        }
    }
    private static void generateRookMoves(Board board, List<Move> moves, boolean isWhite) {
        long rooks = isWhite ? board.getBitboard(Board.wR) : board.getBitboard(Board.bR);
        long friendlyPieces = board.getFriendlyPieces();
        long occupied = board.getOccupied();

        while (rooks != 0) {
            int fromSquare = Long.numberOfTrailingZeros(rooks);
            long attacks = getRookAttacks(fromSquare, occupied) & ~friendlyPieces;

            addMovesFromBitboard(fromSquare, attacks, board, moves,
                    isWhite ? Piece.WHITE_ROOK : Piece.BLACK_ROOK);
            rooks &= rooks - 1;
        }
    }
    private static void generateQueenMoves(Board board, List<Move> moves, boolean isWhite) {
        long queens = isWhite ? board.getBitboard(Board.wQ) : board.getBitboard(Board.bQ);
        long friendlyPieces = board.getFriendlyPieces();
        long occupied = board.getOccupied();

        while (queens != 0) {
            int fromSquare = Long.numberOfTrailingZeros(queens);
            long attacks = getQueenAttacks(fromSquare, occupied) & ~friendlyPieces;

            addMovesFromBitboard(fromSquare, attacks, board, moves,
                    isWhite ? Piece.WHITE_QUEEN : Piece.BLACK_QUEEN);
            queens &= queens - 1;
        }
    }

    // Initializers
    private static void initializeAttackTables() {
        initializeKnightAttacks();
        initializePawnAttacks();
        initializeKingAttacks();
    }
    private static void initializeMagicBitboards() {
        ROOK_MAGICS = new long[] {
                0x84800482D261C000L, 0x464000C1600B9000L, 0x6E000BD57E3FFE00L, 0x1180041088820000L,
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
        }; // 3 and 4 ( 0 indexed) must be improved.

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

    private static void initSliderAttacks(int square, boolean isRook) {
        long mask = isRook ? ROOK_MASKS[square] : BISHOP_MASKS[square];
        int bits = (int) (isRook ? ROOK_BITS[square] : BISHOP_BITS[square]);

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

    private static void initializeKnightAttacks() {
        for (int square = 0; square < 64; square++) {
            long attacks = 0L;
            int rank = square / 8;
            int file = square % 8;

            // All 8 possible knight moves relative to current square
            int[][] offsets = { { 2, 1 }, { 1, 2 }, { -1, 2 }, { -2, 1 }, { -2, -1 }, { -1, -2 }, { 1, -2 },
                    { 2, -1 } };

            for (int[] offset : offsets) {
                int r = rank + offset[0];
                int f = file + offset[1];

                if (r >= 0 && r < 8 && f >= 0 && f < 8) {
                    int targetSquare = r * 8 + f;
                    attacks |= 1L << targetSquare;
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

            // All 8 possible king moves
            for (int r = Math.max(0, rank - 1); r <= Math.min(7, rank + 1); r++) {
                for (int f = Math.max(0, file - 1); f <= Math.min(7, file + 1); f++) {
                    if (r != rank || f != file) { // Exclude current square
                        int targetSquare = r * 8 + f;
                        attacks |= 1L << targetSquare;
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

            // White pawn attacks (capture up and diagonally - rank + 1)
            long whiteAttacks = 0L;
            if (rank < 7) { // Not on last rank (rank 8)
                if (file > 0)
                    whiteAttacks |= 1L << ((rank + 1) * 8 + (file - 1)); // Up-left
                if (file < 7)
                    whiteAttacks |= 1L << ((rank + 1) * 8 + (file + 1)); // Up-right
            }
            PAWN_ATTACKS[0][square] = whiteAttacks; // White = index 0

            // Black pawn attacks (capture down and diagonally - rank - 1)
            long blackAttacks = 0L;
            if (rank > 0) { // Not on first rank (rank 1)
                if (file > 0)
                    blackAttacks |= 1L << ((rank - 1) * 8 + (file - 1)); // Down-left
                if (file < 7)
                    blackAttacks |= 1L << ((rank - 1) * 8 + (file + 1)); // Down-right
            }
            PAWN_ATTACKS[1][square] = blackAttacks; // Black = index 1
        }
    }

    private static int findKingSquare(Board board, boolean isWhite) {
        long kingBitboard = isWhite ? board.getBitboard(Board.wK) : board.getBitboard(Board.bK);
        return Long.numberOfTrailingZeros(kingBitboard);
    }
    private static void addMovesFromBitboard(int fromSquare, long movesBitboard, Board board, List<Move> moves, int pieceType) {
        long enemyPieces = board.getOccupied() & ~board.getFriendlyPieces();

        while (movesBitboard != 0) {
            int toSquare = Long.numberOfTrailingZeros(movesBitboard);
            boolean isCapture = (enemyPieces & (1L << toSquare)) != 0;
            int capturedPiece = isCapture ? board.getPieceCode(toSquare) : NO_CAPTURE;

            moves.add(Move.create(
                    fromSquare, toSquare, 0, pieceType, capturedPiece,
                    false, false, board.getCastlingRights()
            ));
            movesBitboard &= movesBitboard - 1;
        }
    }

    public static boolean isSquareAttacked(Board board, int square) {
        return isSquareAttacked(square, !board.isWhiteToMove(), board);
    }
    public static boolean isSquareAttacked(int square, boolean byWhite, Board board) {
        long occupied = board.getOccupied();

        if (byWhite) {
            long rookAttacks = getRookAttacks(square, occupied);
            if ((rookAttacks & (board.getBitboard(Board.wR) | board.getBitboard(Board.wQ))) != 0) {
                return true;
            }

            long bishopAttacks = getBishopAttacks(square, occupied);
            if ((bishopAttacks & (board.getBitboard(Board.wB) | board.getBitboard(Board.wQ))) != 0) {
                return true;
            }

            if ((KNIGHT_ATTACKS[square] & board.getBitboard(Board.wN)) != 0) {
                return true;
            }

            if ((PAWN_ATTACKS[1][square] & board.getBitboard(Board.wP)) != 0) {
                return true;
            }

            if ((KING_ATTACKS[square] & board.getBitboard(Board.wK)) != 0) {
                return true;
            }
        } else {
            long rookAttacks = getRookAttacks(square, occupied);
            if ((rookAttacks & (board.getBitboard(Board.bR) | board.getBitboard(Board.bQ))) != 0) {
                return true;
            }

            long bishopAttacks = getBishopAttacks(square, occupied);
            if ((bishopAttacks & (board.getBitboard(Board.bB) | board.getBitboard(Board.bQ))) != 0) {
                return true;
            }

            if ((KNIGHT_ATTACKS[square] & board.getBitboard(Board.bN)) != 0) {
                return true;
            }

            if ((PAWN_ATTACKS[0][square] & board.getBitboard(Board.bP)) != 0) {
                return true;
            }

            if ((KING_ATTACKS[square] & board.getBitboard(Board.bK)) != 0) {
                return true;
            }
        }

        return false;
    }

    private static boolean leavesKingInCheck(int fromSquare, int toSquare, int capturedPiece, Board board) {
        boolean isWhite = board.isWhiteToMove();
        int kingSquare = findKingSquare(board, isWhite);

        int movingPiece = board.getPieceBitBoardCode(fromSquare);
        if (movingPiece == (isWhite ? Board.wK : Board.bK)) {
            kingSquare = toSquare;
        }

        long tempOccupied = board.getOccupied();

        tempOccupied &= ~(1L << fromSquare);  // Remove piece from origin
        tempOccupied |= (1L << toSquare);     // Place piece at destination

        return isSquareAttackedWithOccupied(kingSquare, !isWhite, tempOccupied, capturedPiece, toSquare, board);
    }
    private static boolean isSquareAttackedWithOccupied(int square, boolean byWhite, long occupied,int capturedPiece, int capturedSquare, Board board) {
        long captureMask = (capturedPiece != 15) ? ~(1L << capturedSquare) : -1L;

        if (byWhite) {
            long rookAttacks = getRookAttacks(square, occupied);
            if ((rookAttacks & ((board.getBitboard(Board.wR) | board.getBitboard(Board.wQ)) & captureMask)) != 0) {
                return true;
            }

            long bishopAttacks = getBishopAttacks(square, occupied);
            if ((bishopAttacks & ((board.getBitboard(Board.wB) | board.getBitboard(Board.wQ)) & captureMask)) != 0) {
                return true;
            }

            // Non-sliding pieces don't depend on modified occupied
            if ((KNIGHT_ATTACKS[square] & (board.getBitboard(Board.wN) & captureMask)) != 0) {
                return true;
            }

            if ((PAWN_ATTACKS[1][square] & (board.getBitboard(Board.wP) & captureMask)) != 0) {
                return true;
            }

            if ((KING_ATTACKS[square] & (board.getBitboard(Board.wK) & captureMask)) != 0) {
                return true;
            }
        } else {
            long rookAttacks = getRookAttacks(square, occupied);
            if ((rookAttacks & ((board.getBitboard(Board.bR) | board.getBitboard(Board.bQ)) & captureMask)) != 0) {
                return true;
            }

            long bishopAttacks = getBishopAttacks(square, occupied);
            if ((bishopAttacks & ((board.getBitboard(Board.bB) | board.getBitboard(Board.bQ)) & captureMask)) != 0) {
                return true;
            }

            // Non-sliding pieces don't depend on modified occupied
            if ((KNIGHT_ATTACKS[square] & (board.getBitboard(Board.bN) & captureMask)) != 0) {
                return true;
            }

            if ((PAWN_ATTACKS[0][square] & (board.getBitboard(Board.bP) & captureMask)) != 0) {
                return true;
            }

            if ((KING_ATTACKS[square] & (board.getBitboard(Board.bK) & captureMask)) != 0) {
                return true;
            }
        }

        return false;
    }

    private static void generatePawnCapturesFromSquare(Board board, List<Move> moves, int fromSquare,
                                                       boolean isWhite, long enemyPieces, long promotionRank) {
        int rank = fromSquare / 8;
        int file = fromSquare % 8;
        int direction = isWhite ? 8 : -8;

        // Left capture
        if (file > 0) {
            int captureSquare = fromSquare + direction - 1;
            if (captureSquare >= 0 && captureSquare < 64 &&
                    (enemyPieces & (1L << captureSquare)) != 0) {
                if (((1L << captureSquare) & promotionRank) != 0) {
                    addPromotionMoves(moves, fromSquare, captureSquare, true, isWhite, board);
                } else {
                    moves.add(Move.create(
                            fromSquare, captureSquare, 0,
                            isWhite ? Piece.WHITE_PAWN : Piece.BLACK_PAWN,
                            board.getPieceCode(captureSquare), false, false, board.getCastlingRights()
                    ));
                }
            }
        }

        // Right capture
        if (file < 7) {
            int captureSquare = fromSquare + direction + 1;
            if (captureSquare >= 0 && captureSquare < 64 &&
                    (enemyPieces & (1L << captureSquare)) != 0) {
                if (((1L << captureSquare) & promotionRank) != 0) {
                    addPromotionMoves(moves, fromSquare, captureSquare, true, isWhite, board);
                } else {
                    moves.add(Move.create(
                            fromSquare, captureSquare, 0,
                            isWhite ? Piece.WHITE_PAWN : Piece.BLACK_PAWN,
                            board.getPieceCode(captureSquare), false, false, board.getCastlingRights()
                    ));
                }
            }
        }
    }
    private static void addPromotionMoves(List<Move> moves, int fromSquare, int toSquare,
                                          boolean isCapture, boolean isWhite, Board board) {
        int capturedPiece = isCapture ? board.getPieceCode(toSquare) : NO_CAPTURE;
        int[] promotionPieces = {Piece.QUEEN, Piece.BLACK_ROOK, Piece.BISHOP, Piece.KNIGHT};

        for (int promotionPiece : promotionPieces) {
            moves.add(Move.create(
                    fromSquare, toSquare, promotionPiece,
                    isWhite ? Piece.WHITE_PAWN : Piece.BLACK_PAWN,
                    capturedPiece, false, false, board.getCastlingRights()
            ));
        }
    }
    private static void generateEnPassantMoves(Board board, List<Move> moves, boolean isWhite) {
        int enPassantSquare = board.getEnPassantSquare();
        if (enPassantSquare == -1) return;

        long pawns = board.bitboards[isWhite ? wP : bP];
        int direction = isWhite ? -8 : 8;
        int captureFile = enPassantSquare % 8;

        // Check for pawns that can capture en passant
        if (captureFile > 0) {
            int leftPawnSquare = enPassantSquare + direction - 1;
            if (leftPawnSquare >= 0 && leftPawnSquare < 64 &&
                    (pawns & (1L << leftPawnSquare)) != 0) {
                moves.add(Move.create(
                        leftPawnSquare, enPassantSquare, 0,
                        isWhite ? Piece.WHITE_PAWN : Piece.BLACK_PAWN,
                        isWhite ? Piece.BLACK_PAWN : Piece.WHITE_PAWN,
                        false, true, board.getCastlingRights()
                ));
            }
        }

        if (captureFile < 7) {
            int rightPawnSquare = enPassantSquare + direction + 1;
            if (rightPawnSquare >= 0 && rightPawnSquare < 64 &&
                    (pawns & (1L << rightPawnSquare)) != 0) {
                moves.add(Move.create(
                        rightPawnSquare, enPassantSquare, 0,
                        isWhite ? Piece.WHITE_PAWN : Piece.BLACK_PAWN,
                        isWhite ? Piece.BLACK_PAWN : Piece.WHITE_PAWN,
                        false, true, board.getCastlingRights()
                ));
            }
        }
    }
    private static void generateCastlingMoves(Board board, List<Move> moves, boolean isWhite) {
        byte castlingRights = board.getCastlingRights();
        if ((castlingRights & (isWhite ? WHITE_CASTLE_MASK : BLACK_CASTLE_MASK)) == 0) {
            return;
        }

        if (isSquareAttacked(board, findKingSquare(board, isWhite))) return; // Can't castle when in check

        long occupied = board.getOccupied();

        if (isWhite) {
            // White kingside castling (e1-g1) - squares 4 to 6
            if ((castlingRights & WHITE_KINGSIDE_BIT) != 0) {
                if ((occupied & WHITE_KINGSIDE_EMPTY) == 0 && // f1 and g1 empty (squares 5,6)
                        !isSquareAttacked(5, false, board) && // f1 not attacked
                        !isSquareAttacked(6, false, board)) { // g1 not attacked
                    moves.add(Move.create(4, 6, 0, Piece.WHITE_KING, NO_CAPTURE,
                            true, false, castlingRights));
                }
            }
            // White queenside castling (e1-c1) - squares 4 to 2
            if ((castlingRights & WHITE_QUEENSIDE_BIT) != 0) {
                if ((occupied & WHITE_QUEENSIDE_EMPTY) == 0 && // b1, c1, d1 empty (squares 1,2,3)
                        !isSquareAttacked(3, false, board) && // d1 not attacked
                        !isSquareAttacked(2, false, board)) { // c1 not attacked
                    moves.add(Move.create(4, 2, 0, Piece.WHITE_KING, NO_CAPTURE,
                            true, false, castlingRights));
                }
            }
        } else {
            // Black kingside castling (e8-g8) - squares 60 to 62
            if ((castlingRights & BLACK_KINGSIDE_BIT) != 0) {
                if ((occupied & BLACK_KINGSIDE_EMPTY) == 0 && // f8 and g8 empty (squares 61,62)
                        !isSquareAttacked(61, true, board) && // f8 not attacked
                        !isSquareAttacked(62, true, board)) { // g8 not attacked
                    moves.add(Move.create(60, 62, 0, Piece.BLACK_KING, NO_CAPTURE,
                            true, false, castlingRights));
                }
            }
            // Black queenside castling (e8-c8) - squares 60 to 58
            if ((castlingRights & BLACK_QUEENSIDE_BIT) != 0) {
                if ((occupied & BLACK_QUEENSIDE_EMPTY) == 0 && // b8, c8, d8 empty (squares 57,58,59)
                        !isSquareAttacked(59, true, board) && // d8 not attacked
                        !isSquareAttacked(58, true, board)) { // c8 not attacked
                    moves.add(Move.create(60, 58, 0, Piece.BLACK_KING, NO_CAPTURE,
                            true, false, castlingRights));
                }
            }
        }
    }
}