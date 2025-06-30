package chess.functions.moves;

import chess.data.*;
import chess.functions.attacks.AttackMasks;
import chess.functions.rules.CheckRules;

import java.util.ArrayList;
import java.util.List;

public final class MoveGeneration {

    private MoveGeneration() {} // Utility class

    // Main entry point for legal move generation
    public static List<Move> generateLegalMoves(Position position) {
        return generatePseudoLegalMoves(position).stream()
                .filter(move -> !CheckRules.leavesKingInCheck(position, move))
                .toList();
    }

    // Generate all pseudo-legal moves
    public static List<Move> generatePseudoLegalMoves(Position position) {
        List<Move> moves = new ArrayList<>(64);
        Color color = position.whiteToMove() ? Color.WHITE : Color.BLACK;

        generatePawnMoves(position, color, moves);
        generateKnightMoves(position, color, moves);
        generateBishopMoves(position, color, moves);
        generateRookMoves(position, color, moves);
        generateQueenMoves(position, color, moves);
        generateKingMoves(position, color, moves);

        return moves;
    }

    private static void generatePawnMoves(Position position, Color color, List<Move> moves) {
        long pawns = position.bitboards()[PieceType.PAWN.toBitboardIndex(color)];

        while (pawns != 0) {
            int square = Long.numberOfTrailingZeros(pawns);
            generatePawnMovesFromSquare(position, Square.of(square), color, moves);
            pawns &= pawns - 1;
        }
    }
    private static void generatePawnMovesFromSquare(Position position, Square from,
                                                    Color color, List<Move> moves) {
        int direction = color == Color.WHITE ? 8 : -8;
        int startRank = color == Color.WHITE ? 1 : 6;
        int promotionRank = color == Color.WHITE ? 7 : 0;

        // Single push
        int singlePushSquare = from.value() + direction;
        if (singlePushSquare >= 0 && singlePushSquare < 64 &&
                position.pieceAt(singlePushSquare) == -1) {

            Square to = Square.of(singlePushSquare);

            if (to.rank() == promotionRank) {
                addPromotionMoves(from, to, moves);
            } else {
                moves.add(Move.normal(from.value(), to.value()));

                // Double push
                if (from.rank() == startRank) {
                    int doublePushSquare = singlePushSquare + direction;
                    if (doublePushSquare >= 0 && doublePushSquare < 64 &&
                            position.pieceAt(doublePushSquare) == -1) {
                        moves.add(Move.normal(from.value(), doublePushSquare));
                    }
                }
            }
        }

        // Captures
        generatePawnCaptures(position, from, color, moves);

        // En passant
        if (position.enPassantSquare() != -1) {
            generateEnPassantCaptures(position, from, color, moves);
        }
    }
    private static void generatePawnCaptures(Position position, Square from,
                                             Color color, List<Move> moves) {
        long attacks = AttackMasks.pawnAttacks(from, color);
        long enemyPieces = position.enemyPieces();
        long captureTargets = attacks & enemyPieces;

        while (captureTargets != 0) {
            int toSquare = Long.numberOfTrailingZeros(captureTargets);
            Square to = Square.of(toSquare);

            int promotionRank = color == Color.WHITE ? 7 : 0;
            if (to.rank() == promotionRank) {
                addPromotionMoves(from, to, moves);
            } else {
                moves.add(Move.normal(from.value(), to.value()));
            }

            captureTargets &= captureTargets - 1;
        }
    }

    private static void generateEnPassantCaptures(Position position, Square from,
                                                  Color color, List<Move> moves) {
        Square enPassantSquare = Square.of(position.enPassantSquare());
        long attacks = AttackMasks.pawnAttacks(from, color);

        if ((attacks & enPassantSquare.toBitboard()) != 0) {
            moves.add(Move.enPassant(from.value(), enPassantSquare.value()));
        }
    }
    private static void addPromotionMoves(Square from, Square to, List<Move> moves) {
        moves.add(Move.promotion(from.value(), to.value(), Move.PROMO_QUEEN));
        moves.add(Move.promotion(from.value(), to.value(), Move.PROMO_ROOK));
        moves.add(Move.promotion(from.value(), to.value(), Move.PROMO_BISHOP));
        moves.add(Move.promotion(from.value(), to.value(), Move.PROMO_KNIGHT));
    }

    private static void generateKnightMoves(Position position, Color color, List<Move> moves) {
        long knights = position.bitboards()[PieceType.KNIGHT.toBitboardIndex(color)];
        generateJumpingPieceMoves(position, knights, AttackMasks::knightAttacks, moves);
    }
    private static void generateKingMoves(Position position, Color color, List<Move> moves) {
        long king = position.bitboards()[PieceType.KING.toBitboardIndex(color)];
        generateJumpingPieceMoves(position, king, AttackMasks::kingAttacks, moves);

        // Castling moves
        generateCastlingMoves(position, color, moves);
    }
    private static void generateBishopMoves(Position position, Color color, List<Move> moves) {
        long bishops = position.bitboards()[PieceType.BISHOP.toBitboardIndex(color)];
        generateSlidingPieceMoves(position, bishops,
                square -> AttackMasks.bishopAttacks(square, position.occupied()), moves);
    }
    private static void generateRookMoves(Position position, Color color, List<Move> moves) {
        long rooks = position.bitboards()[PieceType.ROOK.toBitboardIndex(color)];
        generateSlidingPieceMoves(position, rooks,
                square -> AttackMasks.rookAttacks(square, position.occupied()), moves);
    }
    private static void generateQueenMoves(Position position, Color color, List<Move> moves) {
        long queens = position.bitboards()[PieceType.QUEEN.toBitboardIndex(color)];
        generateSlidingPieceMoves(position, queens,
                square -> AttackMasks.queenAttacks(square, position.occupied()), moves);
    }

    // Higher-order functions for piece move generation
    private static void generateJumpingPieceMoves(Position position, long pieces,
                                                  java.util.function.Function<Square, Long> attackFunction,
                                                  List<Move> moves) {
        while (pieces != 0) {
            int fromSquare = Long.numberOfTrailingZeros(pieces);
            Square from = Square.of(fromSquare);

            long attacks = attackFunction.apply(from) & ~position.friendlyPieces();
            addMovesFromBitboard(from, attacks, moves);

            pieces &= pieces - 1;
        }
    }

    private static void generateSlidingPieceMoves(Position position, long pieces,
                                                  java.util.function.Function<Square, Long> attackFunction,
                                                  List<Move> moves) {
        while (pieces != 0) {
            int fromSquare = Long.numberOfTrailingZeros(pieces);
            Square from = Square.of(fromSquare);
            //PieceType pt = PieceType.fromCombinedIndex(position.pieceAt(fromSquare)); // temp fix  TODO: Find the last remaining perfect magics for rooks

            long attacks = attackFunction.apply(from) & ~position.friendlyPieces();
            //long realAttacks = computeRealSliderAttacks(from, position, pt) & ~position.friendlyPieces(); // temp fix

//            if (attacks != realAttacks) {
//                attacks = realAttacks;
//            }

            addMovesFromBitboard(from, attacks, moves);

            pieces &= pieces - 1;
        }
    }

    private static void addMovesFromBitboard(Square from, long targets, List<Move> moves) {
        while (targets != 0) {
            int toSquare = Long.numberOfTrailingZeros(targets);
            moves.add(Move.normal(from.value(), toSquare));
            targets &= targets - 1;
        }
    }
    private static void generateCastlingMoves(Position position, Color color, List<Move> moves) {
        // Castling constants
        byte kingside = (byte) (color == Color.WHITE ? 0x01 : 0x04);
        byte queenside = (byte) (color == Color.WHITE ? 0x02 : 0x08);

        if ((position.castlingRights() & (kingside | queenside)) == 0) {
            return;
        }

        Square kingSquare = Square.of(color == Color.WHITE ? 4 : 60);

        // Can't castle when in check
        if (CheckRules.isSquareAttacked(position, kingSquare, color.opposite())) {
            return;
        }

        // Kingside castling
        if ((position.castlingRights() & kingside) != 0) {
            generateKingsideCastle(position, color, moves);
        }

        // Queenside castling
        if ((position.castlingRights() & queenside) != 0) {
            generateQueensideCastle(position, color, moves);
        }
    }
    private static void generateKingsideCastle(Position position, Color color, List<Move> moves) {
        int kingSquare = color == Color.WHITE ? 4 : 60;
        int rookSquare = color == Color.WHITE ? 7 : 63;
        int kingDestination = color == Color.WHITE ? 6 : 62;

        // Check if squares are empty
        long emptyMask = color == Color.WHITE ? 0x60L : 0x6000000000000000L;
        if ((position.occupied() & emptyMask) != 0) {
            return;
        }

        // Check if squares are not attacked
        Square f1g1 = Square.of(color == Color.WHITE ? 5 : 61);
        Square g1g8 = Square.of(kingDestination);

        if (!CheckRules.isSquareAttacked(position, f1g1, color.opposite()) &&
                !CheckRules.isSquareAttacked(position, g1g8, color.opposite())) {
            moves.add(Move.castling(kingSquare, kingDestination));
        }
    }
    private static void generateQueensideCastle(Position position, Color color, List<Move> moves) {
        int kingSquare = color == Color.WHITE ? 4 : 60;
        int rookSquare = color == Color.WHITE ? 0 : 56;
        int kingDestination = color == Color.WHITE ? 2 : 58;

        // Check if squares are empty
        long emptyMask = color == Color.WHITE ? 0x0EL : 0x0E00000000000000L;
        if ((position.occupied() & emptyMask) != 0) {
            return;
        }

        // Check if squares are not attacked
        Square d1d8 = Square.of(color == Color.WHITE ? 3 : 59);
        Square c1c8 = Square.of(kingDestination);

        if (!CheckRules.isSquareAttacked(position, d1d8, color.opposite()) &&
                !CheckRules.isSquareAttacked(position, c1c8, color.opposite())) {
            moves.add(Move.castling(kingSquare, kingDestination));
        }
    }

    // Rook attacks (4 directions)
    private static long computeRookAttacksSlow(Square sq, long blockers) {
        long attacks = 0L;
        int file = sq.file();
        int rank = sq.rank();

        // North (increasing rank)
        for (int r = rank + 1; r < 8; r++) {
            long bb = 1L << (file + r * 8);
            attacks |= bb;
            if ((blockers & bb) != 0) break;
        }

        // South (decreasing rank)
        for (int r = rank - 1; r >= 0; r--) {
            long bb = 1L << (file + r * 8);
            attacks |= bb;
            if ((blockers & bb) != 0) break;
        }

        // East (increasing file)
        for (int f = file + 1; f < 8; f++) {
            long bb = 1L << (f + rank * 8);
            attacks |= bb;
            if ((blockers & bb) != 0) break;
        }

        // West (decreasing file)
        for (int f = file - 1; f >= 0; f--) {
            long bb = 1L << (f + rank * 8);
            attacks |= bb;
            if ((blockers & bb) != 0) break;
        }

        return attacks;
    }
    // Bishop attacks (4 diagonals)
    private static long computeBishopAttacksSlow(Square sq, long blockers) {
        long attacks = 0L;
        int file = sq.file();
        int rank = sq.rank();

        // Northeast (file++, rank++)
        for (int f = file + 1, r = rank + 1; f < 8 && r < 8; f++, r++) {
            long bb = 1L << (f + r * 8);
            attacks |= bb;
            if ((blockers & bb) != 0) break;
        }

        // Northwest (file--, rank++)
        for (int f = file - 1, r = rank + 1; f >= 0 && r < 8; f--, r++) {
            long bb = 1L << (f + r * 8);
            attacks |= bb;
            if ((blockers & bb) != 0) break;
        }

        // Southeast (file++, rank--)
        for (int f = file + 1, r = rank - 1; f < 8 && r >= 0; f++, r--) {
            long bb = 1L << (f + r * 8);
            attacks |= bb;
            if ((blockers & bb) != 0) break;
        }

        // Southwest (file--, rank--)
        for (int f = file - 1, r = rank - 1; f >= 0 && r >= 0; f--, r--) {
            long bb = 1L << (f + r * 8);
            attacks |= bb;
            if ((blockers & bb) != 0) break;
        }

        return attacks;
    }

    private static long computeRealSliderAttacks(Square square, Position position, PieceType pt) {
        long blockers = position.occupied();

        switch (pt) {
            case ROOK:   return computeRookAttacksSlow(square, blockers);
            case BISHOP: return computeBishopAttacksSlow(square, blockers);
            case QUEEN:  return computeRookAttacksSlow(square, blockers) |
                    computeBishopAttacksSlow(square, blockers);
            default:     return 0L; // Should never happen for sliding pieces
        }
    }
}