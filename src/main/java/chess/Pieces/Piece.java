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
    
    public int getpieceCode()
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
            int direction = color == 16 ? 1 : -1; // 1 for white, -1 for black
            int startRow = color == 16 ? 1 : 6; // Initial row for white and black pawns

            //forward move BoardUtils.isSameRank(destinationSquare,this.piecePosition + direction);
            if(BoardUtils.isSameFile(destinationSquare, this.piecePosition) && BoardUtils.isSameRank(destinationSquare,this.piecePosition + direction * 8)
             && board.isEmpty(destinationSquare))
            {
                isPseudoLegal = true;
            };
            
            //double move BoardUtils.isSameRank(destinationSquare,this.piecePosition + direction * 2)
            if(BoardUtils.isSameFile(destinationSquare, this.piecePosition) && destRow == startRow + direction * 2 
            && board.isEmpty(destinationSquare) && board.isEmpty(piecePosition + direction))
            {
                isPseudoLegal = true;
            }

            // capture move
            if (Math.abs(destCol - currentCol) == 1 && destRow == currentRow + direction
            && !board.isEmpty(destinationSquare) && !BoardUtils.isSameColor(pieceCode, board.getPieceCode(destinationSquare))) 
            {
                isPseudoLegal = true;
            }

            Integer enPassantSquare = board.getEnPassantSquare();
            if (enPassantSquare != null && Math.abs(destCol - currentCol) == 1 && destRow == currentRow + direction
                && destinationSquare == enPassantSquare) 
            {
                isPseudoLegal = true;
            }
        }
        else if (pieceType == 0b010) //Knight
        {
            /*
             * 
             *  2D representation of the chess board. This is the easiest way to verify a Knight move because this way there is no column or row exeption
             *  We take advantage of the L shape movement of a Knight to verify a move."
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
            if ((rowdiff == 1 && coldiff == 2 || rowdiff == 2 && coldiff == 1) && !BoardUtils.isSameColor(pieceCode,board.getPieceCode(destinationSquare))) {
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
        }
        else if (pieceType == 0b100) 
        {
            int rowdiff = Math.abs(destRow - currentRow);
            int coldiff = Math.abs(destCol - currentCol);
            
            if (rowdiff != coldiff) 
            {
                return false;
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
                    return false;
                }

                currentCheckRow += rowDirection;
                currentCheckCol += colDirection;
            }

            if (!board.isEmpty(destinationSquare) && BoardUtils.isSameColor(pieceCode, board.getPieceCode(destinationSquare)))
            {
                return false;
            }

            isPseudoLegal = true;
        }

        if (!isPseudoLegal)
        {
            return false;
        }

        int originalPosition = this.piecePosition;
        board.makeMove(originalPosition,destinationSquare);

        boolean isCurrentPlayerWhite = this.getPieceColor() == 0b10000;
        boolean inCheck = board.isKingInCheck(isCurrentPlayerWhite);

        board.unMakeMove();

        return !inCheck;
    };
}