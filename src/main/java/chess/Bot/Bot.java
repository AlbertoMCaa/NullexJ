package chess.Bot;

import chess.Board.Board;
import chess.Board.Move.Move;
import chess.Board.Move.MoveGenerator;

import java.util.List;

import static chess.Board.Board.*;


public class Bot
{
    public static long nodes = 0;
    public static long movesS = 0;
    public static Move findBestMove(Board board, int depth)
    {
        List<Move> moves = MoveGenerator.generateLegalMoves(board);
        Move bestMove = null;
        double bestValue = Integer.MIN_VALUE;

        for (Move move : moves)
        {
            board.makeMove2(move);
            double value = minimax(board, depth - 1, false);
            board.unmakeMove2();

            if (value > bestValue)
            {
                bestValue = value;
                bestMove = move;
            }
        }
        System.out.println("CANTIDAD DE NODOS: " + nodes);
        System.out.println("CANTIDAD DE NODOS: " + movesS);
        return bestMove;
    }

    private static double minimax(Board board, int depth, boolean maximizing)
    {
        nodes++;
        if (depth == 0) return evaluateBoard(board);

        List<Move> moves = MoveGenerator.generateLegalMoves(board);
        movesS += moves.size();
        if (maximizing)
        {
            double maxEval = Integer.MIN_VALUE;

            for (Move move : moves)
            {
                board.makeMove2(move);
                double eval = minimax(board, depth - 1, false);
                board.unmakeMove2();
                maxEval = Math.max(maxEval, eval);
            }
            return maxEval;
        }
        else
        {
            double minEval = Integer.MAX_VALUE;

            for (Move move : moves)
            {
                board.makeMove2(move);
                double eval = minimax(board, depth - 1, true);
                board.unmakeMove2();
                minEval = Math.min(minEval, eval);
            }
            return minEval;
        }
    }

    private static double evaluateBoard(Board board)
    {
        double white = Long.bitCount(board.bitboards[wP]) +
                Long.bitCount(board.bitboards[wN]) * 3 +
                Long.bitCount(board.bitboards[wB]) * 3.5 +
                Long.bitCount(board.bitboards[wR]) * 5 +
                Long.bitCount(board.bitboards[wQ]) * 9;

        double black = Long.bitCount(board.bitboards[bP])  +
                Long.bitCount(board.bitboards[bN]) * 3  +
                Long.bitCount(board.bitboards[bB]) * 3.5 +
                Long.bitCount(board.bitboards[bR]) * 5 +
                Long.bitCount(board.bitboards[bQ]) * 9;

        return white - black;
    }
}