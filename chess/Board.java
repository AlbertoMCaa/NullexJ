package chess;

public class Board {
    static final int chessTiles = 64;
    static final int rows = 8;

    public static void initiateChess() {
        Bitboards bitboards = new Bitboards();
        String chessBoard = "rnbqkbnrpppppppp                                PPPPPPPPRNBQKBNR";
        arrayToBitboards(chessBoard, bitboards);
        //printBitboards(bitboards);
    }
    public static class Bitboards {
        public long wP, wN, wB, wR, wQ, wK;
        public long bP, bN, bB, bR, bQ, bK;
    }

    public static void arrayToBitboards(String chessBoard, Bitboards bitboards) {
        for (int i = 0; i < chessTiles; i++) {
            long bit = 1L << (chessTiles - 1 - i); // Ajusta la posición del bit correcto (de a8 a h1)
            switch (chessBoard.charAt(i)) {
                case 'r':
                    bitboards.bR |= bit;
                    break;
                case 'n':
                    bitboards.bN |= bit;
                    break;
                case 'b':
                    bitboards.bB |= bit;
                    break;
                case 'q':
                    bitboards.bQ |= bit;
                    break;
                case 'k':
                    bitboards.bK |= bit;
                    break;
                case 'p':
                    bitboards.bP |= bit;
                    break;
                case 'R':
                    bitboards.wR |= bit;
                    break;
                case 'N':
                    bitboards.wN |= bit;
                    break;
                case 'B':
                    bitboards.wB |= bit;
                    break;
                case 'Q':
                    bitboards.wQ |= bit;
                    break;
                case 'K':
                    bitboards.wK |= bit;
                    break;
                case 'P':
                    bitboards.wP |= bit;
                    break;
                default:
                    break;
            }
        }
    }

    private static void printBitboards(Bitboards bitboards) {
        System.out.println("White Pawns: " + Long.toBinaryString(bitboards.wP));
        System.out.println("White Knights: " + Long.toBinaryString(bitboards.wN));
        System.out.println("White Bishops: " + Long.toBinaryString(bitboards.wB));
        System.out.println("White Rooks: " + Long.toBinaryString(bitboards.wR));
        System.out.println("White Queens: " + Long.toBinaryString(bitboards.wQ));
        System.out.println("White Kings: " + Long.toBinaryString(bitboards.wK));
        System.out.println("Black Pawns: " + Long.toBinaryString(bitboards.bP));
        System.out.println("Black Knights: " + Long.toBinaryString(bitboards.bN));
        System.out.println("Black Bishops: " + Long.toBinaryString(bitboards.bB));
        System.out.println("Black Rooks: " + Long.toBinaryString(bitboards.bR));
        System.out.println("Black Queens: " + Long.toBinaryString(bitboards.bQ));
        System.out.println("Black Kings: " + Long.toBinaryString(bitboards.bK));
    }
}
