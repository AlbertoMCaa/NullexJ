package chess.data;

public enum PieceType {
    PAWN(0), KNIGHT(1), BISHOP(2), ROOK(3), QUEEN(4), KING(5);

    public final int index;

    PieceType(int index) {
        this.index = index;
    }

    // Precomputed values array
    private static final PieceType[] VALUES = values();

    // Precomputed 12-element lookup: [white pieces 0-5] + [black pieces 6-11]
    public static final PieceType[] PIECE_INDEX_TO_TYPE = new PieceType[12];

    static {
        for (int i = 0; i < 12; i++) {
            PIECE_INDEX_TO_TYPE[i] = VALUES[i % 6];
        }
    }

    public int toBitboardIndex(Color color) {
        return color.index * 6 + index;
    }

    public static PieceType fromCombinedIndex(int index) {
        return PIECE_INDEX_TO_TYPE[index];
    }

    public static boolean isPawn(int pieceIndex) {
        return pieceIndex == 0 || pieceIndex == 6;
    }

    public static boolean isKing(int pieceIndex) {
        return pieceIndex == 5 || pieceIndex == 11;
    }

    public static boolean isRook(int pieceIndex) {
        return pieceIndex == 3 || pieceIndex == 9; // White rook or black rook
    }

    public static PieceType getPieceType(int pieceIndex) {
        return PIECE_INDEX_TO_TYPE[pieceIndex];
    }

    public static PieceType getPromotionPieceType(Move move) {
        return switch (move.promotionType()) {
            case Move.PROMO_KNIGHT -> PieceType.KNIGHT;
            case Move.PROMO_BISHOP -> PieceType.BISHOP;
            case Move.PROMO_ROOK -> PieceType.ROOK;
            case Move.PROMO_QUEEN -> PieceType.QUEEN;
            default -> throw new IllegalArgumentException("Invalid promotion type");
        };
    }
}