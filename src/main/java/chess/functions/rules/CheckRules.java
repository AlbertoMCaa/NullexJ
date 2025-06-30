package chess.functions.rules;

import chess.data.*;
import chess.functions.attacks.AttackMasks;
import chess.functions.moves.MoveApplication;

public final class CheckRules {

    private CheckRules() {} // Utility class

    public static boolean isSquareAttacked(Position position, Square square, Color byColor) {
        long occupied = position.occupied();

        // Check for pawn attacks
        if ((AttackMasks.pawnAttacks(square, byColor.opposite()) &
                position.bitboards()[PieceType.PAWN.toBitboardIndex(byColor)]) != 0) {
            return true;
        }

        // Check for knight attacks
        if ((AttackMasks.knightAttacks(square) &
                position.bitboards()[PieceType.KNIGHT.toBitboardIndex(byColor)]) != 0) {
            return true;
        }

        // Check for bishop/queen diagonal attacks
        long bishopQueenAttacks = AttackMasks.bishopAttacks(square, occupied);
        if ((bishopQueenAttacks & (position.bitboards()[PieceType.BISHOP.toBitboardIndex(byColor)] |
                position.bitboards()[PieceType.QUEEN.toBitboardIndex(byColor)])) != 0) {
            return true;
        }

        // Check for rook/queen orthogonal attacks
        long rookQueenAttacks = AttackMasks.rookAttacks(square, occupied);
        if ((rookQueenAttacks & (position.bitboards()[PieceType.ROOK.toBitboardIndex(byColor)] |
                position.bitboards()[PieceType.QUEEN.toBitboardIndex(byColor)])) != 0) {
            return true;
        }

        // Check for king attacks
        if ((AttackMasks.kingAttacks(square) &
                position.bitboards()[PieceType.KING.toBitboardIndex(byColor)]) != 0) {
            return true;
        }

        return false;
    }

    public static boolean leavesKingInCheck(Position position, Move move) {
        Position newPosition = MoveApplication.applyMove(position, move);
        Color movedColor = position.whiteToMove() ? Color.WHITE : Color.BLACK;
        Square kingSquare = findKingSquare(newPosition, movedColor);

        return isSquareAttacked(newPosition, kingSquare, movedColor.opposite());
    }

    private static Square findKingSquare(Position position, Color color) {
        long king = position.bitboards()[PieceType.KING.toBitboardIndex(color)];
        if (king == 0) {
            throw new IllegalStateException("King not found for color: " + color);
        }
        return Square.of(Long.numberOfTrailingZeros(king));
    }

    public static boolean isInCheck(Position position, Color activeColor) {
        return isSquareAttacked(position, findKingSquare(position,activeColor),activeColor);
    }
}