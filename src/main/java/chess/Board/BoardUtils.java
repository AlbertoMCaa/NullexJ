package chess.Board;

public abstract class BoardUtils{

    public static final long FIRST_COLUMN = 0x0101010101010101L;
    public static final long SECOND_COLUMN = 0x0202020202020202L;
    public static final long SEVENTH_COLUMN = 0x4040404040404040L;
    public static final long EIGHTH_COLUMN = 0x8080808080808080L;

    //public static final boolean[] FIRST_COLUMN = initFiles(0);
    //public static final boolean[] SECOND_COLUMN = initFiles(1);
    public static final boolean[] THIRD_COLUMN = initFiles(2);
    public static final boolean[] FOURTH_COLUMN = initFiles(3);
    public static final boolean[] FIFTH_COLUMN = initFiles(4);
    public static final boolean[] SIXTH_COLUMN = initFiles(5);
    //public static final boolean[] SEVENTH_COLUMN = initFiles(6);
    //public static final boolean[] EIGHTH_COLUMN = initFiles(7);

    public static final boolean[] EIGHTH_RANK = initRanks(0);
    public static final boolean[] SEVENTH_RANK = initRanks(1);
    public static final boolean[] SIXTH_RANK = initRanks(2);
    public static final boolean[] FIFTH_RANK = initRanks(3);
    public static final boolean[] FOURTH_RANK = initRanks(4);
    public static final boolean[] THIRD_RANK = initRanks(5);
    public static final boolean[] SECOND_RANK = initRanks(6);
    public static final boolean[] FIRST_RANK = initRanks(7);

    private BoardUtils()
    {
        throw new IllegalStateException("Utility class");
    }
    /*
     * Method used to check if a given coordinate is within the bounds of the chess boards.
     * 
     * Returns true if coordinate is equal to or greater than 0 and less than 64.
     */
    public static final boolean isValidCoordinate(final int coordinates)
    {
        return coordinates >= 0 && coordinates < 64;
    }

    /*
     * Initialize a column array. This is used to speed up the process of checking if a tile is in a given column. This is only performed once.
     * 
    */
    public static boolean[] initFiles(int columnNumber)
    {
        boolean[] column = new boolean[64];
        int startOffset = columnNumber; // Start from the given column index at the top (a8, b8, ..., h8)
    
        while (startOffset < 64) {
            column[startOffset] = true;
            startOffset += 8; // Move to the next rank within the same column
        }
        return column;
    }

    /*
     * Initialize a rank array. This is used to speed up the process of checking if a tile is in a given rank. This is only performed once.
    */
    public static boolean[] initRanks(int rankNumber)
    {
        boolean[] rank = new boolean[64];
        int startOffset = rankNumber * 8; // Calculate the starting index for the rank (0-7 for a8-h8, 8-15 for a7-h7, ..., 56-63 for a1-h1)
        int endOffset = startOffset + 8;  // Calculate the ending index for the rank

        for (int i = startOffset; i < endOffset; i++) {
            rank[i] = true;
        }
        return rank;
    }
    public static boolean isSameFile(int destinationSquare, int sourcePosition)
    {
        return destinationSquare % 8 == sourcePosition % 8;
    }
    public static boolean isSameRank(int destinationSquare, int sourcePosition)
    {
        return destinationSquare / 8 == sourcePosition / 8;
    }
    public static boolean isSameColor(int pieceCode, int pieceCode2){
        return ((pieceCode & 0b11000) == (pieceCode2 & 0b11000));
    };

    //Columns

    public static boolean isFirstColumn(int position)
    {
        return position % 8 == 0;
    }
    public static boolean isSecondColumn(int position)
    {
        return position % 8 == 1;
    }
    public static boolean isSeventhColumn(int position) 
    {
        return position % 8 == 6;
    }
    public static boolean isEighthColumn(int position) 
    {
        return position % 8 == 7;
    }

    //Rows 

    public static boolean isFirstRow(int position) 
    {
        return position / 8 == 0;
    }

    public static boolean isSecondRow(int position) 
    {
        return position / 8 == 1;
    }

    public static boolean isSeventhRow(int position) 
    {
        return position / 8 == 6;
    }

    public static boolean isEighthRow(int position) 
    {
        return position / 8 == 7;
    }

}
