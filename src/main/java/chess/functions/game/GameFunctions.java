package chess.functions.game;

import chess.data.*;
import chess.errors.IllegalMoveException;
import chess.functions.moves.MoveApplication;
import chess.functions.moves.MoveGeneration;
import chess.functions.rules.CheckRules;

import java.util.ArrayList;
import java.util.List;

public class GameFunctions {

    private GameFunctions() {} // Utility class

    public static Game initializeGame(Game game) {
        List<Move> legalMoves = MoveGeneration.generateLegalMoves(game.position());
        GameState state = determinegameState(game.position(),game.history(), legalMoves);
        return new Game(
          game.position(),
          game.history(),
          state,
          legalMoves,
          game.moveHistory()
        );
    }

    public static Game applyMove(Game game, Move move) {
        if (game.isGameOver()) {
            throw new IllegalStateException("Cannot make move: game is over");
        }

        // Validate move is legal
        if (!game.legalMoves().contains(move)) {
            throw new IllegalMoveException("Move is not legal: " + move);
        }

        Position newPosition = MoveApplication.applyMove(game.position(), move);
        List<Position> newHistory = appendToHistory(game.history(), newPosition);
        List<Move> newMoveHistory = appendToHistory(game.moveHistory(),move);

        List<Move> newLegalMoves = MoveGeneration.generateLegalMoves(newPosition);

        GameState newState = determinegameState(newPosition, newHistory, newLegalMoves);
        return new Game(
                newPosition,
                newHistory,
                newState,
                newLegalMoves,
                newMoveHistory
        );
    }

    public static Game makeMoves(Game game, Move... moves) {
        Game current = game;

        for (Move move : moves) {
            current = applyMove(current, move);
            if (current.isGameOver()) {
                break;
            }
        }
        return current;
    }

    private static GameState determinegameState(Position position, List<Position> history, List<Move> legalMoves) {
        Color activeColor = position.whiteToMove() ? Color.WHITE : Color.BLACK;
        if (legalMoves.isEmpty()) {
            if (CheckRules.isInCheck(position, activeColor)) {
                // Checkmate - opponent wins
                return position.whiteToMove() ?
                        new GameState.BlackWins(GameState.WinType.CHECKMATE) :
                        new GameState.WhiteWins(GameState.WinType.CHECKMATE);
            } else {
                // Stalemate
                return new GameState.Stalemate();
            }
        }

        if (position.halfmoveClock() >= 100) {
            return new GameState.Draw(GameState.DrawType.FIFTY_MOVE);
        }

        if (isThreeFoldRepetion(position, history)) {
            return new GameState.Draw(GameState.DrawType.REPETITION);
        }

        if (hasInsufficientMaterial(position)) {
            return new GameState.Draw(GameState.DrawType.INSUFFICIENT_MATERIAL);
        }
        // Game continues
        return new GameState.Ongoing();
    }

    private static boolean hasInsufficientMaterial(Position position) {
        throw new Error("Not supported yet");
    }

    private static boolean isThreeFoldRepetion(Position position, List<Position> history) {
        throw new Error("Not supported yet");
    }

    public static Game offerDraw(Game game) {
        if (game.isGameOver()) {
            throw new IllegalStateException("Cannot offer draw: game is over");
        }
        // Needs to be handled by UI
        // for the moment, just throw

        throw new Error("Not supported yet");
    }

    public static Game acceptDraw(Game game) {
        return new Game(
                game.position(),
                game.history(),
                game.state(),
                game.legalMoves(),
                game.moveHistory()
        );
    }

    public static Game resign(Game game, Color resigningPlayer) {
        if (game.isGameOver()) {
            throw new IllegalStateException("Cannot resign: game is over");
        }

        GameState newState = resigningPlayer == Color.WHITE ?
                new GameState.BlackWins(GameState.WinType.RESIGNATION) :
                new GameState.WhiteWins(GameState.WinType.RESIGNATION);

        return new Game(
                game.position(),
                game.history(),
                newState,
                game.legalMoves(),
                game.moveHistory()
        );
    }

    private static List<Position> appendToHistory(List<Position> history, Position newPosition) {
        List<Position> newHistory = new ArrayList<>(history);
        newHistory.add(newPosition);
        return List.copyOf(newHistory); // Return immutable list
    }
    private static List<Move> appendToHistory(List<Move> history, Move move) {
        List<Move> newHistory = new ArrayList<>(history);
        newHistory.add(move);
        return List.copyOf(newHistory);
    }
}