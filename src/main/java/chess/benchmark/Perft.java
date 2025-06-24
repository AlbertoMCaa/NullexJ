package chess.benchmark;

import chess.Board.Board;
import chess.Board.Move.Move;
import chess.Board.Move.MoveGenerator;

import java.util.List;

public class Perft {

    public static long perft(Board board, int depth) {
        if (board.bitboards[Board.wK] == 0 || board.bitboards[Board.bK] == 0) {
            return 0; // Terminal node - king captured
        }

        if (depth == 0) {
            return 1;
        }

        List<Move> moves = MoveGenerator.generateLegalMoves(board);
        if (moves.isEmpty()) {
            return 0; // Terminal node - checkmate or stalemate
        }

        long nodes = 0;
        for (Move move : moves) {
            board.makeMove2(move);
            nodes += perft(board, depth - 1);
            board.unmakeMove2();
            board.validateBoardConsistency(move);
        }
        return nodes;
    }

    public static void runPerft(Board board, int depth) {
        // Warm up
        perft(board, Math.min(depth, 2));

        // Actual run
        long startTime = System.nanoTime();
        long nodes = perft(board, depth);
        long time = (System.nanoTime() - startTime) / 1_000_000;

        System.out.println("Perft(" + depth + ") = " + nodes +
                " nodes (" + time + " ms)");
        System.out.println("Nodes/second: " + (nodes * 1000L / Math.max(1, time)));
    }
}