package chess.data;

public enum Color {
    WHITE(0), BLACK(1);

    public final int index;

    Color(int index) {
        this.index = index;
    }

    public Color opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
    public static Color getPieceColor(int pieceIndex) {
        return pieceIndex < 6 ? Color.WHITE : Color.BLACK;
    }
}