package chess.data;

public record Square(int value) {
    public Square {
        if (value < 0 || value > 63) {
            throw new IllegalArgumentException("Square value must be 0-63, got: " + value);
        }
    }

    public static Square of(int value) {
        return new Square(value);
    }

    public static Square of(int rank, int file) {
        return new Square(rank * 8 + file);
    }

    public int rank() {
        return value / 8;
    }

    public int file() {
        return value % 8;
    }

    public long toBitboard() {
        return 1L << value;
    }
}