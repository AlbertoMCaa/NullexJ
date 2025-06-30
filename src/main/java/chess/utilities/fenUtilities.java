package chess.utilities;

public class fenUtilities {
    private static final int chessTiles = 64;

    public static final int wP = 0;
    public static final int wN = 1;
    public static final int wB = 2;
    public static final int wR = 3;
    public static final int wQ = 4;
    public static final int wK = 5;
    public static final int bP = 6;
    public static final int bN = 7;
    public static final int bB = 8;
    public static final int bR = 9;
    public static final int bQ = 10;
    public static final int bK = 11;

    private static final String chessBoard = "RNBQKBNRPPPPPPPP                                pppppppprnbqkbnr";

    private fenUtilities() {} // Utility class

    private static long[] arrayToBitboards(String chessBoard)
    {
        long[] bitboards = new long[12];

        for (int i = 0; i < chessTiles; i++)
        {
            long bit = 1L << i;
            switch (chessBoard.charAt(i))
            {
                case 'r':
                    bitboards[bR] |= bit;
                    break;
                case 'n':
                    bitboards[bN] |= bit;
                    break;
                case 'b':
                    bitboards[bB] |= bit;
                    break;
                case 'q':
                    bitboards[bQ] |= bit;
                    break;
                case 'k':
                    bitboards[bK] |= bit;
                    break;
                case 'p':
                    bitboards[bP] |= bit;
                    break;
                case 'R':
                    bitboards[wR] |= bit;
                    break;
                case 'N':
                    bitboards[wN] |= bit;
                    break;
                case 'B':
                    bitboards[wB] |= bit;
                    break;
                case 'Q':
                    bitboards[wQ] |= bit;
                    break;
                case 'K':
                    bitboards[wK] |= bit;
                    break;
                case 'P':
                    bitboards[wP] |= bit;
                    break;
                default:
                    break;
            }
        }
        return bitboards;
    }

    public static long[] standChess() {
        return arrayToBitboards(chessBoard);
    }
}
