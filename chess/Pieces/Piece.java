package chess.Pieces;

import chess.Board.Board;
import chess.Board.BoardUtils;
/*
 * I will use this class to represent a piece in the chess game.
 * The first 3 bit represent the type of piece.
 * The 4 and 5 bit represent the color of the piece.
 * 
 * Example: 10001 represents a White Pawn.
 * 01001 represents a Black Pawn.
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
    private int piecePosition;

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
    public int getPieceType() {
        // Map the piece type to the corresponding index in the Board class
        switch (pieceCode & pieceTypeMask) {
            case 0b001: // Pawn
                return getPieceColor() == 0b10000 ? 0 : 6; // White Pawn or Black Pawn
            case 0b010: // Knight
                return getPieceColor() == 0b10000 ? 1 : 7; // White Knight or Black Knight
            case 0b100: // Bishop
                return getPieceColor() == 0b10000 ? 2 : 8; // White Bishop or Black Bishop
            case 0b101: // Rook
                return getPieceColor() == 0b10000 ? 3 : 9; // White Rook or Black Rook
            case 0b110: // Queen
                return getPieceColor() == 0b10000 ? 4 : 10; // White Queen or Black Queen
            case 0b111: // King
                return getPieceColor() == 0b10000 ? 5 : 11; // White King or Black King
            default:
                return -1; // Invalid piece type
        }
    }

    public boolean isValidMove(int destinationSquare, Board board)
    {
        if(!BoardUtils.isValidCoordinate(destinationSquare)){return false;}

        int pieceType = getPieceType();
        int color = getPieceColor();

        if (pieceType == 0b001) //Pawn
        {
            int direction = color == 2 ? 1 : -1; // 1 for white, -1 for black
            int startRow = color == 2 ? 1 : 6; // Initial row for white and black pawns

            int currentRow = piecePosition / 8;
            int currentCol = piecePosition % 8;
            int destRow = destinationSquare / 8;
            int destCol = destinationSquare % 8;

            //forward move BoardUtils.isSameRank(destinationSquare,this.piecePosition + direction);
            if(BoardUtils.isSameFile(destinationSquare, this.piecePosition) && BoardUtils.isSameRank(destinationSquare,this.piecePosition + direction)
             && board.isEmpty(destinationSquare))
            {
                return true;
            };
            
            //double move
            if(destCol == currentCol && destRow == startRow + direction * 2 
            && board.isEmpty(destinationSquare) && board.isEmpty(piecePosition + direction))
            {
                return true;
            }

            // capture move
            if (Math.abs(destCol - currentCol) == 1 && destRow == currentRow + direction
            && !board.isEmpty(destinationSquare) && !BoardUtils.isSameColor(pieceCode, board.getPieceCode(destinationSquare))) 
            {
                return true;
            }

            Integer enPassantSquare = board.getEnPassantSquare();
            if (enPassantSquare != null && Math.abs(destCol - currentCol) == 1 && destRow == currentRow + direction
                && destinationSquare == enPassantSquare) 
                {
                return true;
            }
        }
        return false;
    };
}