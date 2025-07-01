package chess.data;

import chess.functions.fen.FenParser;

import java.util.List;

public record Game(
        Position position,
        List<Position> history,
        GameState state,
        List<Move> legalMoves,
        List<Move> moveHistory
) {
    public static Game newStandardGame() {
        Position starPos = FenParser.standPos();
        return new Game(
                starPos,
                List.of(starPos),
                new GameState.Ongoing(),
                List.of(),
                List.of()
        );
    }

    public static Game fromFen(String FEN) {
        Position fenGame = FenParser.parse(FEN);
        return new Game(
                fenGame,
                List.of(fenGame),
                new GameState.Ongoing(),
                List.of(),
                List.of()
        );
    }

    public int getHalfMoveClock() {
        return position.halfmoveClock();
    }

    public int getFullMoveClock() {
        return position.fullmoveNumber();
    }

    public int getTotalPlies() {
        return (position.fullmoveNumber() - 1) * 2 +
                (position.whiteToMove() ? 0 : 1);
    }

    public Color activePlayer() {
        return position.whiteToMove() ? Color.WHITE : Color.BLACK;
    }

    public boolean isGameOver() {
        return !(state instanceof GameState.Ongoing);
    }

    public boolean canMakeMove() {
        return state instanceof GameState.Ongoing && !legalMoves.isEmpty();
    }
}
