package chess.data;

public sealed interface GameState
        permits GameState.Ongoing, GameState.WhiteWins, GameState.BlackWins,
        GameState.Draw, GameState.Stalemate {

    record Ongoing() implements GameState {}
    record WhiteWins(WinType type) implements GameState {}
    record BlackWins(WinType type) implements GameState {}
    record Draw(DrawType type) implements GameState {}
    record Stalemate() implements GameState {}

    enum WinType { CHECKMATE, RESIGNATION, TIME }
    enum DrawType { STALEMATE, REPETITION, FIFTY_MOVE, INSUFFICIENT_MATERIAL, AGREEMENT }
}