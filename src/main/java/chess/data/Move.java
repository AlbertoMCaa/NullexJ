package chess.data;

import static chess.utilities.squareUtilities.squareToAlgebraic;
import static chess.utilities.squareUtilities.validateSquare;

/**
 * Bit Layout:
 * Bits  0–5   : destination square (0–63)
 * Bits  6–11  : origin square      (0–63)
 * Bits 12–13  : promotion piece type (if any): 00=knight, 01=bishop, 10=rook, 11=queen
 * Bits 14–15  : special-move flag: 00=normal, 01=promotion, 10=en passant, 11=castling
 */
public record Move(int encoded) {

    // Bit shift positions
    private static final int DEST_SHIFT = 0;     // bits 0-5
    private static final int ORIGIN_SHIFT = 6;   // bits 6-11
    private static final int PROMO_SHIFT = 12;   // bits 12-13
    private static final int SPECIAL_SHIFT = 14; // bits 14-15

    // Bit masks
    private static final int SQUARE_MASK = 0x3F;  // 6 bits (0-63)
    private static final int PROMO_MASK = 0x3;    // 2 bits (0-3)
    private static final int SPECIAL_MASK = 0x3;  // 2 bits (0-3)

    // Special move flags
    public static final int NORMAL = 0b00;
    public static final int PROMOTION = 0b01;
    public static final int EN_PASSANT = 0b10;
    public static final int CASTLING = 0b11;

    // Promotion piece types
    public static final int PROMO_KNIGHT = 0b00;
    public static final int PROMO_BISHOP = 0b01;
    public static final int PROMO_ROOK = 0b10;
    public static final int PROMO_QUEEN = 0b11;

    // Factory methods for different move types
    public static Move normal(int from, int to) {
        return new Move(encode(from, to, 0, NORMAL));
    }

    public static Move promotion(int from, int to, int promoType) {
        validatePromoType(promoType);
        return new Move(encode(from, to, promoType, PROMOTION));
    }

    public static Move enPassant(int from, int to) {
        return new Move(encode(from, to, 0, EN_PASSANT));
    }

    public static Move castling(int from, int to) {
        return new Move(encode(from, to, 0, CASTLING));
    }

    // Compact constructor with validation
    public Move {
        if ((encoded & 0xFFFF0000) != 0) {
            throw new IllegalArgumentException("Move encoding exceeds 16 bits: " + encoded);
        }
    }

    // Accessors
    public int from() {
        return (encoded >> ORIGIN_SHIFT) & SQUARE_MASK;
    }

    public int to() {
        return (encoded >> DEST_SHIFT) & SQUARE_MASK;
    }

    public int promotionType() {
        return (encoded >> PROMO_SHIFT) & PROMO_MASK;
    }

    public int specialFlag() {
        return (encoded >> SPECIAL_SHIFT) & SPECIAL_MASK;
    }

    // Convenience predicates
    public boolean isNormal() {
        return specialFlag() == NORMAL;
    }

    public boolean isPromotion() {
        return specialFlag() == PROMOTION;
    }

    public boolean isEnPassant() {
        return specialFlag() == EN_PASSANT;
    }

    public boolean isCastling() {
        return specialFlag() == CASTLING;
    }

    // Chess notation methods
    public String toAlgebraic() {
        return squareToAlgebraic(from()) + squareToAlgebraic(to()) +
                (isPromotion() ? promoTypeToChar(promotionType()) : "");
    }

    public String toUci() {
        return toAlgebraic(); // UCI format is same as algebraic for moves
    }

    // Utility methods
    private static int encode(int from, int to, int promoType, int specialFlag) {
        validateSquare(from, "from");
        validateSquare(to, "to");

        return (to & SQUARE_MASK) << DEST_SHIFT
                | (from & SQUARE_MASK) << ORIGIN_SHIFT
                | (promoType & PROMO_MASK) << PROMO_SHIFT
                | (specialFlag & SPECIAL_MASK) << SPECIAL_SHIFT;
    }

    private static void validatePromoType(int promoType) {
        if (promoType < 0 || promoType > 3) {
            throw new IllegalArgumentException("Promotion type must be 0-3, got: " + promoType);
        }
    }

    private static char promoTypeToChar(int promoType) {
        return switch (promoType) {
            case PROMO_KNIGHT -> 'n';
            case PROMO_BISHOP -> 'b';
            case PROMO_ROOK -> 'r';
            case PROMO_QUEEN -> 'q';
            default -> throw new IllegalArgumentException("Invalid promotion type: " + promoType);
        };
    }

    @Override
    public String toString() {
        return switch (specialFlag()) {
            case NORMAL -> String.format("Move[%s→%s]",
                    squareToAlgebraic(from()), squareToAlgebraic(to()));
            case PROMOTION -> String.format("Move[%s→%s=%c]",
                    squareToAlgebraic(from()), squareToAlgebraic(to()), promoTypeToChar(promotionType()));
            case EN_PASSANT -> String.format("Move[%s→%s ep]",
                    squareToAlgebraic(from()), squareToAlgebraic(to()));
            case CASTLING -> String.format("Move[%s→%s O-O]",
                    squareToAlgebraic(from()), squareToAlgebraic(to()));
            default -> String.format("Move[%s→%s ?%d]",
                    squareToAlgebraic(from()), squareToAlgebraic(to()), specialFlag());
        };
    }
}