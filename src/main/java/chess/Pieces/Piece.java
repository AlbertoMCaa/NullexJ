package chess.Pieces;

import chess.Board.Board;
import chess.Board.BoardUtils;

/*
 * This class represents a piece in the chess game.
 * 0b00000
 * The first 3 bit represent the type of piece.
 * The 4 and 5 bit represent the color of the piece.
 *
 * Example: 10001 represents a White Pawn.
 *          01001 represents a Black Pawn.
 *
 * 0b001 represents Pawn.
 * 0b010 represents Knight.
 * 0b100 represents Bishop.
 * 0b101 represents Rook.
 * 0b110 represents Queen.
 * 0b111 represents King.
 *
 * 0b10000 represents White.
 * 0b01000 represents Black.
 */
public class Piece {
    public static final int WHITE_KNIGHT = 0b10010;
    public static final int BLACK_KNIGHT = 0b01010;
    public static final int WHITE_BISHOP = 0b10100;
    public static final int BLACK_BISHOP = 0b01100;
    public static final int WHITE_QUEEN = 0b10110;
    public static final int WHITE_KING = 0b10111;
    public static final int BLACK_QUEEN = 0b01110;
    public static final int BLACK_KING = 0b01111;
    public static final int WHITE_ROOK = 0b10101;
    public static final int BLACK_ROOK = 0b01101;
    public static final int WHITE_PAWN = 0b10001;
    public static final int BLACK_PAWN = 0b01001;

    public static final int PAWN = 0b001;
    public static final int KNIGHT = 0b010;
    public static final int BISHOP = 0b100;
    public static final int QUEEN = 0b110;
    public static final int ROOK = 0b101;
    public static final int KING = 0b111;

    private static final int colorMask = 0b11000;
    private static final int pieceTypeMask = 0b111;

    private final int pieceCode; //The comnbined color and type of the piece. 01101 represents a Black Rook.
    private int piecePosition; // From 0 to 63 inclusive

    public Piece(int piecePosition, int pieceCode)
    {
        this.piecePosition = piecePosition;
        this.pieceCode = pieceCode;
    }

    public int getPiecePosition()
    {
        return piecePosition;
    }
    public int getPieceCode()
    {
        return pieceCode;
    }
    public int getPieceColor()
    {
        return (pieceCode & colorMask);
    }
    public int getPieceType()
    {
        // Extract the piece type from the pieceCode using bitwise AND with pieceTypeMask
        int pieceType = pieceCode & pieceTypeMask;

        // Return the extracted piece type
        switch (pieceType)
        {
            case 0b001: return 0b001;  // Pawn
            case 0b010: return 0b010;  // Knight
            case 0b100: return 0b100;  // Bishop
            case 0b101: return 0b101;  // Rook
            case 0b110: return 0b110;  // Queen
            case 0b111: return 0b111;  // King
            default: return -1;    // Invalid piece type
        }
    }

    public boolean isValidMove(int destinationSquare, Board board)
    {
        if(!BoardUtils.isValidCoordinate(destinationSquare)){return false;}
        //if (board.getPieceCode(this.piecePosition) == -1){return false;}

        int pieceType = getPieceType();
        int color = getPieceColor();

        int currentRow = piecePosition / 8;
        int currentCol = piecePosition % 8;
        int destRow = destinationSquare / 8;
        int destCol = destinationSquare % 8;

        boolean isPseudoLegal = false;

        if (pieceType == 0b001) //Pawn
        {
            isPseudoLegal = isValidPawnMove(destinationSquare, board, color, isPseudoLegal, destRow, destCol, currentCol, currentRow);
        }
        else if (pieceType == 0b010) //Knight
        {
            isPseudoLegal = isValidKnightMove(destinationSquare, board, destRow, currentRow, destCol, currentCol, isPseudoLegal);
        }
        else if (pieceType == 0b100) // Bishop
        {
            if (isValidBishopMove(destinationSquare, board, destRow, currentRow, destCol, currentCol)) return false;

            isPseudoLegal = true;
        }
        else if (pieceType == 0b101) // Rook
        {
            if (isValidRookMove(destinationSquare, board, currentRow, destRow, currentCol, destCol)) return false;

            isPseudoLegal = true;
        }
        else if (pieceType == 0b110) //Queen
        {
            isPseudoLegal = isValidBishopMove(destinationSquare, board, destRow, currentRow, destCol, currentCol) || isValidRookMove(destinationSquare, board, destRow, currentRow, destCol, currentCol);
        }
        else if (pieceType == 0b111) //King
        {
            isPseudoLegal = isValidKingMove(destinationSquare, board, destRow, currentRow, destCol, currentCol, isPseudoLegal);
        }

        if (!isPseudoLegal)
        {
            return false;
        }

        int originalPosition = this.piecePosition;
        board.makeMove(originalPosition,destinationSquare, 0);

        boolean isCurrentPlayerWhite = this.getPieceColor() == 0b10000;
        //boolean inCheck = board.isKingInCheck(isCurrentPlayerWhite);

        board.unMakeMove();

        return !true;
    }

    private boolean isValidKingMove(int destinationSquare, Board board, int destRow, int currentRow, int destCol, int currentCol, boolean isPseudoLegal) {
        int rowdiff = Math.abs(destRow - currentRow);
        int coldiff = Math.abs(destCol - currentCol);

        boolean isAdjacent = (rowdiff <= 1 && coldiff <= 1);

        if (isAdjacent && !BoardUtils.isSameColor(pieceCode, board.getPieceCode(destinationSquare)))
        {
            isPseudoLegal = true;
        }
        return isPseudoLegal;
    }

    private boolean isValidRookMove(int destinationSquare, Board board, int currentRow, int destRow, int currentCol, int destCol) {
        // Check if the move is a straight line
        if (currentRow != destRow && currentCol != destCol)
        {
            return true;
        }

        // Check if there's a friendly piece at the destination
        int destPiece = board.getPieceCode(destinationSquare);
        if (destPiece != -1 && BoardUtils.isSameColor(pieceCode, destPiece))
        {
            return true;
        }

        // Determine directions and steps
        int rowStep = Integer.compare(destRow, currentRow);
        int colStep = Integer.compare(destCol, currentCol);

        // Verify an empty path
        int steps = Math.max(Math.abs(destRow - currentRow), Math.abs(destCol - currentCol));
        for (int i = 1; i < steps; i++)
        {
            int checkRow = currentRow + rowStep * i;
            int checkCol = currentCol + colStep * i;
            if (!board.isEmpty(checkRow * 8 + checkCol))
            {
                return true;
            }
        }
        return false;
    }

    private boolean isValidBishopMove(int destinationSquare, Board board, int destRow, int currentRow, int destCol, int currentCol) {
        int rowdiff = Math.abs(destRow - currentRow);
        int coldiff = Math.abs(destCol - currentCol);

        if (rowdiff != coldiff)
        {
            return true;
        }

        int rowDirection = (destRow > currentRow) ? 1 : -1;
        int colDirection = (destCol > currentCol) ? 1 : -1;

        int currentCheckRow = currentRow + rowDirection;
        int currentCheckCol = currentCol + colDirection;

        while (currentCheckRow != destRow && currentCheckCol != destCol)
        {
            int currentCheckSquare = currentCheckRow * 8 + currentCheckCol;

            if (!board.isEmpty(currentCheckSquare))
            {
                return true;
            }

            currentCheckRow += rowDirection;
            currentCheckCol += colDirection;
        }

        if (!board.isEmpty(destinationSquare) && BoardUtils.isSameColor(pieceCode, board.getPieceCode(destinationSquare)))
        {
            return true;
        }
        return false;
    }

    private boolean isValidKnightMove(int destinationSquare, Board board, int destRow, int currentRow, int destCol, int currentCol, boolean isPseudoLegal) {
        /*
         *
         *  2D representation of the chess board. This is the easiest way to verify a Knight move because this way there is no column or row exeption
         *  We take advantage of the L shape movement of a Knight to verify a move.
         *  Each Knight move must have a magnitude of square root of 5, which we use to determine if a move is valid from any given square to another.
         *  First calculate the row difference between the current row at which our piece is placed and the row of move to evaluate, we also do this with the columns.
         *  If the difference between the rows and columns follows a proportion of 2 to 1, the move is valid.
         *
         * (0,0) (0,1) (0,2) (0,3) (0,4) (0,5) (0,6) (0,7)
         * (1,0) (1,1) (1,2) (1,3) (1,4) (1,5) (1,6) (1,7)
         * (2,0) (2,1) (2,2) (2,3) (2,4) (2,5) (2,6) (2,7)
         * (3,0) (3,1) (3,2) (3,3) (3,4) (3,5) (3,6) (3,7)
         * (4,0) (4,1) (4,2) (4,3) (4,4) (4,5) (4,6) (4,7)
         * (5,0) (5,1) (5,2) (5,3) (5,4) (5,5) (5,6) (5,7)
         * (6,0) (6,1) (6,2) (6,3) (6,4) (6,5) (6,6) (6,7)
         * (7,0) (7,1) (7,2) (7,3) (7,4) (7,5) (7,6) (7,7)
         *
         */
        int rowdiff = Math.abs(destRow - currentRow);
        int coldiff = Math.abs(destCol - currentCol);

        //Capture move
        if ((rowdiff == 1 && coldiff == 2 || rowdiff == 2 && coldiff == 1) && !BoardUtils.isSameColor(pieceCode, board.getPieceCode(destinationSquare))) {
            // I think this can be simplified as (rowdiff == 1 && coldiff == 2 || rowdiff == 2 && coldiff == 1) && !BoardUtils.isSameColor(piecePosition,board.getPieceCode(destinationSquare))
            //(rowdiff == 1 && coldiff == 2 || rowdiff == 2 && coldiff == 1)
            //                    && !board.isEmpty(destinationSquare)
            //                    && !BoardUtils.isSameColor(this.pieceCode, board.getPieceCode(destinationSquare)
            isPseudoLegal = true;
        }

        //normal move
        if ((rowdiff == 1 && coldiff == 2 || rowdiff == 2 && coldiff == 1) && board.isEmpty(destinationSquare)) {
            isPseudoLegal = true;
        }
        return isPseudoLegal;
    }

    private boolean isValidPawnMove(int destinationSquare, Board board, int color, boolean isPseudoLegal, int destRow, int destCol, int currentCol, int currentRow)
    {
        int direction = color == 0b10000 ? -1 : +1; // 1 for white, -1 for black  //Possible bug!
        int startRow = color == 0b10000 ? 6 : 1; // Initial row for white and black pawns

        if (BoardUtils.isSameFile(destinationSquare, this.piecePosition)
                && destRow == currentRow + direction
                && board.isEmpty(destinationSquare))
        {
            isPseudoLegal = true;
        }

        if (BoardUtils.isSameFile(destinationSquare, this.piecePosition)
                && currentRow == startRow
                && destRow == currentRow + 2 * direction
                && board.isEmpty(destinationSquare)
                && board.isEmpty(this.piecePosition + direction * 8))
        {
            isPseudoLegal = true;
        }

        // capture move
        if (Math.abs(destCol - currentCol) == 1 &&
                destRow == currentRow + direction &&
                !board.isEmpty(destinationSquare) &&
                !BoardUtils.isSameColor(pieceCode, board.getPieceCode(destinationSquare)))
        {
            isPseudoLegal = true;
        }

        Integer enPassantSquare =  -1; //board.getEnPassantSquare();
        if (enPassantSquare != null &&
                Math.abs(destCol - currentCol) == 1 &&
                destRow == currentRow + direction &&
                destinationSquare == enPassantSquare)
        {
            isPseudoLegal = true;
        }

        return isPseudoLegal;
    }
}