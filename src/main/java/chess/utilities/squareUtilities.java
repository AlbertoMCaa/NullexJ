package chess.utilities;

import chess.data.Move;

public class squareUtilities {
    public static String squareToAlgebraic(int square) {
        int file = square % 8;
        int rank = square / 8;
        return "" + (char)('a' + file) + (char)('1' + rank);
    }

    public static void validateSquare(int square, String name) {
        if (square < 0 || square > 63) {
            throw new IllegalArgumentException(name + " square must be 0-63, got: " + square);
        }
    }

    public static String toAlgebraic(Move move) {
        return squareUtilities.squareToAlgebraic(move.from()) +
                squareUtilities.squareToAlgebraic(move.to());
    }
}
