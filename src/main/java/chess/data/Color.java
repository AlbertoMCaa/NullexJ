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
}