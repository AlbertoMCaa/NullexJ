package chess;

import java.util.ArrayList;
import java.util.List;
import chess.Board.Board;
import chess.Pieces.Piece;

public class NewMain {
    // Implementaciones de movimientos
    private static final int[][] KNIGHT_MOVES_2D = new int[64][];
    private static final long[] KNIGHT_BITBOARDS = new long[64];
    private static final int[] KNIGHT_MOVES_FLAT = new int[336];
    private static final int[] KNIGHT_MOVE_OFFSETS = new int[65];

    // Configuración de pruebas
    private static final int WARMUP_ITERATIONS = 100;
    private static final int BENCHMARK_ITERATIONS = 100_000;
    private static final boolean RUN_WARMUP = true;

    static {
        initializeMoveGenerators();
    }

    private static void initializeMoveGenerators()
    {
        final int[][] OFFSETS = {
                {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
                {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
        };

        int flatIndex = 0;
        for (int square = 0; square < 64; square++) {
            int x = square % 8;
            int y = square / 8;

            List<Integer> movesList = new ArrayList<>(8);
            long bitboard = 0L;
            KNIGHT_MOVE_OFFSETS[square] = flatIndex;

            for (int[] offset : OFFSETS) {
                int nx = x + offset[0];
                int ny = y + offset[1];

                if (nx >= 0 && nx < 8 && ny >= 0 && ny < 8) {
                    int dest = ny * 8 + nx;
                    movesList.add(dest);
                    bitboard |= 1L << dest;
                    KNIGHT_MOVES_FLAT[flatIndex++] = dest;
                }
            }

            KNIGHT_MOVES_2D[square] = movesList.stream().mapToInt(i -> i).toArray();
            KNIGHT_BITBOARDS[square] = bitboard;
            KNIGHT_MOVE_OFFSETS[square + 1] = flatIndex;
        }
    }

    public static void main(String[] args)
    {
        //Board board = Board.initiateChess();
        Board board = new Board();

        for (int square = 0; square < 64; square++) {
            board.placePiece(new Piece(square, 0b10010));
        }

        if(RUN_WARMUP) {
            runBenchmarkCycle(board, WARMUP_ITERATIONS, true);
        }

        runBenchmarkCycle(board, BENCHMARK_ITERATIONS, false);
    }

    private static void runBenchmarkCycle(Board board, int iterations, boolean isWarmup) {
        BenchmarkResult result2D = new BenchmarkResult();
        BenchmarkResult resultBitboard = new BenchmarkResult();
        BenchmarkResult resultFlat = new BenchmarkResult();

        for (int i = 0; i < iterations; i++) {
            updateResult(result2D, benchmark2D(board));
            updateResult(resultBitboard, benchmarkBitboard(board));
            updateResult(resultFlat, benchmarkFlatArray(board));
        }

        if(!isWarmup) {
            printResults("Array 2D", result2D, iterations);
            printResults("Bitboard", resultBitboard, iterations);
            printResults("Array Aplanado", resultFlat, iterations);
        }
    }

    private static class BenchmarkResult {
        long totalTime = 0;
        int movesCount = 0;
    }

    private static void updateResult(BenchmarkResult result, long[] benchmarkData) {
        result.totalTime += benchmarkData[0];
        result.movesCount += (int) benchmarkData[1];
    }

    // Implementación 1: Array 2D tradicional
    private static long[] benchmark2D(Board board) {
        long start = System.nanoTime();
        int count = 0;

        for (int square = 0; square < 64; square++) {
            Piece knight = new Piece(square, 0b10010);
            for (int dest : KNIGHT_MOVES_2D[square]) {
                if (knight.isValidMove(dest, board)) count++;
            }
        }

        return new long[]{System.nanoTime() - start, count};
    }

    // Implementación 2: Bitboards
    private static long[] benchmarkBitboard(Board board) {
        long start = System.nanoTime();
        int count = 0;

        for (int square = 0; square < 64; square++) {
            Piece knight = new Piece(square, 0b10010);
            long moves = KNIGHT_BITBOARDS[square];

            while(moves != 0) {
                int dest = Long.numberOfTrailingZeros(moves);
                if(knight.isValidMove(dest, board)) count++;
                moves &= moves - 1;
            }
        }

        return new long[]{System.nanoTime() - start, count};
    }

    // Implementación 3: Array aplanado
    private static long[] benchmarkFlatArray(Board board) {
        long start = System.nanoTime();
        int count = 0;

        for (int square = 0; square < 64; square++) {
            Piece knight = new Piece(square, 0b10010);
            int startIdx = KNIGHT_MOVE_OFFSETS[square];
            int endIdx = KNIGHT_MOVE_OFFSETS[square + 1];

            for (int i = startIdx; i < endIdx; i++) {
                int dest = KNIGHT_MOVES_FLAT[i];
                if(knight.isValidMove(dest, board)) count++;
            }
        }

        return new long[]{System.nanoTime() - start, count};
    }

    private static void printResults(String label, BenchmarkResult result, int iterations) {
        double avgNanos = result.totalTime / (double) iterations;
        System.out.println("=== " + label + " ===");
        System.out.println("Movimientos totales: " + result.movesCount);
        System.out.printf("Tiempo promedio: %9.3f µs | %9.6f ms | %9.9f s\n",
                avgNanos / 1_000,
                avgNanos / 1_000_000,
                avgNanos / 1_000_000_000);
        System.out.printf("Tiempo total:    %,d ns | %,d ms | %,.3f s\n\n",
                result.totalTime,
                result.totalTime / 1_000_000,
                result.totalTime / 1_000_000_000.0);
    }
}