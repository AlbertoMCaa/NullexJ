package chess.Board.Move;

public final class Move {
    // Bit shift positions
    private static final int FROM_SHIFT   = 0;    // bits 0-5
    private static final int TO_SHIFT     = 6;    // bits 6-11
    private static final int PROMO_SHIFT  = 12;   // bits 12-14
    private static final int MOVING_SHIFT = 15;   // bits 15-18
    private static final int CAPTURED_SHIFT = 19;  // bits 19-22
    private static final int CASTLE_SHIFT = 23;    // bit 23
    private static final int EP_SHIFT     = 24;    // bit 24
    private static final int CASTLING_RIGHTS_SHIFT = 25; // bits 25-28 (4 bits)

    // Bit masks

    private static final int MASK_6  = 0x3F;    // 6 bits mask
    private static final int MASK_3  = 0x7;     // 3 bits mask
    private static final int MASK_4  = 0xF;     // 4 bits mask
    private static final int MASK_1  = 0x1;     // 1 bit mask

    private static final int CASTLING_RIGHTS_MASK = 0b1111; // 4 bits (KQkq)
    public static final int WHITE_CASTLE_MASK = 0b1100; // white K+Q
    public static final int BLACK_CASTLE_MASK = 0b0011; // black k+q

    public static final int WHITE_KINGSIDE_BIT     = 0b1000; // bit 3
    public static final int WHITE_QUEENSIDE_BIT    = 0b0100; // bit 2
    public static final int BLACK_KINGSIDE_BIT     = 0b0010; // bit 1
    public static final int BLACK_QUEENSIDE_BIT    = 0b0001; // bit 0

    public static final int NO_CAPTURE = 15; // No captured piece

    private final int move; // 32-bit int (only 30 bits used)

    private Move(int move) {
        this.move = move;
    }

    public static Move create(
            int from, int to, int promo, int movingPiece,
            int capturedPiece, boolean castling, boolean enPassant,
            byte castlingRights) {

        int move = (from & MASK_6) << FROM_SHIFT
                | (to & MASK_6) << TO_SHIFT
                | (promo & MASK_3) << PROMO_SHIFT
                | (movingPiece & MASK_4) << MOVING_SHIFT
                | (capturedPiece & MASK_4) << CAPTURED_SHIFT
                | (castling ? MASK_1 << CASTLE_SHIFT : 0)
                | (enPassant ? MASK_1 << EP_SHIFT : 0)
                | ((castlingRights & CASTLING_RIGHTS_MASK) << CASTLING_RIGHTS_SHIFT);
        return new Move(move);
    }

    // Getters for each field
    public int getFrom() {
        return (move >> FROM_SHIFT) & MASK_6;
    }
    public int getTo() {
        return (move >> TO_SHIFT) & MASK_6;
    }
    public int getPromo() {
        return (move >> PROMO_SHIFT) & MASK_3;
    }
    public int getMovingPiece() {
        return (move >> MOVING_SHIFT) & MASK_4;
    }
    public int getCapturedPiece() {
        return (move >> CAPTURED_SHIFT) & MASK_4;
    }
    public boolean isCastling() {
        return ((move >> CASTLE_SHIFT) & MASK_1) != 0;
    }
    public boolean isEnPassant() {
        return ((move >> EP_SHIFT) & MASK_1) != 0;
    }
    public byte getCastlingRights() {
        return (byte)((move >> CASTLING_RIGHTS_SHIFT) & CASTLING_RIGHTS_MASK);
    }
    public int getRaw() {
        return move;
    }
}
