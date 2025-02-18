package chess;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import chess.Board.Board;
import chess.Pieces.Piece;

public class Main {
    final static int[][] KNIGHT_MOVES;
    private static final int[][] OFFSETS = {
            {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
            {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
    };
    static {
        KNIGHT_MOVES = new int[64][];
        for (int square = 0; square < 64; square++) {
            int x = square % 8;
            int y = square / 8;
            int[] moves = new int[8]; // Máximo 8 movimientos
            int count = 0;

            for (int[] offset : OFFSETS) {
                int nx = x + offset[0];
                int ny = y + offset[1];
                if (nx >= 0 && nx < 8 && ny >= 0 && ny < 8) {
                    moves[count++] = ny * 8 + nx;
                }
            }
            KNIGHT_MOVES[square] = Arrays.copyOf(moves, count);
        }
    }

    public static void main(String[] args) {
        Board board = new Board();

        int[][] legalKnightMoves = new int[64][64];

        long startTime = System.nanoTime();
        for (int start = 0; start < 64; start++) {
            Piece piece = new Piece(start, 0b10010); // Crear una pieza (caballo blanco)
    
            // Verificar los movimientos legales del caballo desde la posición inicial
            for (int dest = 0; dest < 64; dest++) 
            {
                if (piece.isValidMove(dest, board)) 
                {
                    legalKnightMoves[start][dest] = 1;
                } else {
                    legalKnightMoves[start][dest] = 0;
                }
            }
        }
        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        System.out.println("Tiempo transcurrido:");
        System.out.println("   Nanosegundos: " + duration + " ns");
        System.out.println("   Milisegundos: " + (duration / 1000000.0) + " ms");
        System.out.println("   Segundos: " + (duration / 1000000000.0) + " s");

        System.out.println("------------------------------------------------");
        checkMoveValidity(legalKnightMoves, board);


        System.out.println("------------------------------------------------");
        System.out.println("  Method 2");
        final int[][] legalKnightMove2s = new int[64][];

        startTime = System.nanoTime();

        int countIndex = 0;
        for (int start = 0; start < 64; start++) {
            Piece piece = new Piece(start, 0b10010); // Crear una pieza (caballo blanco)
            List<Integer> movesList = new ArrayList<>(); // Lista para almacenar movimientos legales
            // Verificar los movimientos legales del caballo desde la posición inicial
            for (int dest = 0; dest < 64; dest++) {
                if (piece.isValidMove(dest, board)) {
                    movesList.add(dest);
                    countIndex++;
                }
            }
            legalKnightMove2s[start] = movesList.stream().mapToInt(i -> i).toArray();
        }
        endTime = System.nanoTime();
        duration = endTime - startTime;
        
        System.out.println("Movimientos legales: " + countIndex);
        System.out.println("Tiempo transcurrido:");
        System.out.println("   Nanosegundos: " + duration + " ns");
        System.out.println("   Milisegundos: " + (duration / 1000000.0) + " ms");
        System.out.println("   Segundos: " + (duration / 1000000000.0) + " s");

        check2(legalKnightMove2s, board);

        check2(KNIGHT_MOVES, board);


        final int[][] KNIGHT_MOVES = new int[64][];

    }
    public static void checkMoveValidity(int[][] legalMoves, Board board) {
        // Obtener el tiempo inicial en nanosegundos
        long startTime = System.nanoTime();

        // Contador para movimientos válidos
        int validMovesCount = 0;

        // Recorrer todos los movimientos
        for (int from = 0; from < 64; from++) {
            Piece piece = new Piece(from, 0b10010);
            for (int to = 0; to < 8; to++) {
                if (legalMoves[from][to] == 1) {
                    if (piece.isValidMove(to, board)) {
                        validMovesCount++;
                    }
                }
            }
        }

        // Obtener el tiempo final en nanosegundos
        long endTime = System.nanoTime();

        // Calcular la duración en nanosegundos
        long duration = endTime - startTime;

        // Mostrar resultados
        System.out.println("Movimientos válidos encontrados: " + validMovesCount);
        System.out.println("Tiempo transcurrido en verificar movimientos (nanosegundos): " + duration + " ns");
        System.out.println("Tiempo transcurrido en verificar movimientos (milisegundos): " + (duration / 1000000.0) + " ms");
        System.out.println("Tiempo transcurrido en verificar movimientos (segundos): " + (duration / 1000000000.0) + " s");
    }

    public static void check2(int[][] legalMoves, Board board) {
        long startTime = System.nanoTime();
        int validMovesCount = 0;
        
        for(int i = 0; i < legalMoves.length; i++)
        {
            Piece piece = new Piece(i, 0b10010);
            for(int move : legalMoves[i]){
                if (piece.isValidMove(move, board)) {
                    validMovesCount++;
                }
            }
        }


        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        System.out.println("Numero de movimientos: " + validMovesCount);
        System.out.println("Tiempo transcurrido en verificar movimientos (nanosegundos): " + duration + " ns");
        System.out.println("Tiempo transcurrido en verificar movimientos (milisegundos): " + (duration / 1000000.0) + " ms");
        System.out.println("Tiempo transcurrido en verificar movimientos (segundos): " + (duration / 1000000000.0) + " s");
    }
}