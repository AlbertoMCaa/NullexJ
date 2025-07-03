package chess.data;

public enum PieceType {
    PAWN(0), KNIGHT(1), BISHOP(2), ROOK(3), QUEEN(4), KING(5);

    public final int index;

    PieceType(int index) {
        this.index = index;
    }

    public int toBitboardIndex(Color color) {
        return color.index * 6 + index;
    }

    public static PieceType fromCombinedIndex(int index) {
        int colorOffset = index / 6; // 0=white, 1=black
        int pieceIndex = index % 6;
        return values()[pieceIndex];
    }

    public static boolean isPawn(int pieceIndex) {
        return pieceIndex == 0 || pieceIndex == 6;
    }

    public static boolean isKing(int pieceIndex) {
        return pieceIndex == 5 || pieceIndex == 11;
    }

    public static PieceType getPieceType(int pieceIndex) {
        return PieceType.values()[pieceIndex % 6];
    }
}