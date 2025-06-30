package chess.functions.moves;

import chess.data.*;

public class MoveApplication {

    private MoveApplication() {} // Utility class

    public static Position applyMove(Position position, Move move) {
        return switch (move.specialFlag()) {
            case Move.NORMAL -> applyNormalMove(position, move);
            case Move.PROMOTION -> applyPromotionMove(position, move);
            case Move.EN_PASSANT -> applyEnPassantMove(position, move);
            case Move.CASTLING -> applyCastlingMove(position, move);
            default -> throw new IllegalArgumentException("Invalid move flag: " + move.specialFlag());
        };
    }

    public static Position applyMoves(Position position, Move... moves) {
        Position current = position;
        for (Move move : moves) {
            current = applyMove(current, move);
        }
        return current;
    }

    private static Position applyNormalMove(Position position, Move move) {
        Square from = Square.of(move.from());
        Square to = Square.of(move.to());

        int movingPiece = position.pieceAt(from.value());
        if (movingPiece == -1) {
            throw new IllegalArgumentException("No piece at square " + from.value());
        }


        long[] newBitboards = position.bitboards().clone();

        // Remove piece from origin square
        newBitboards[movingPiece] &= ~from.toBitboard();

        // Handle capture
        int capturedPiece = position.pieceAt(to.value());
        if (capturedPiece != -1) {
            newBitboards[capturedPiece] &= ~to.toBitboard();
        }

        // Place piece on destination square
        newBitboards[movingPiece] |= to.toBitboard();

        // Update game state
        byte newCastlingRights = updateCastlingRights(position, move, movingPiece, capturedPiece);
        int newEnPassantSquare = updateEnPassantSquare(position, move, movingPiece);
        int newHalfmoveClock = updateHalfmoveClock(position, movingPiece, capturedPiece);
        int newFullmoveNumber = position.whiteToMove() ? position.fullmoveNumber() : position.fullmoveNumber() + 1;

        return Position.create(
                newBitboards,
                !position.whiteToMove(),
                newCastlingRights,
                newEnPassantSquare,
                newHalfmoveClock,
                newFullmoveNumber
        );
    }
    private static Position applyPromotionMove(Position position, Move move) {
        Square from = Square.of(move.from());
        Square to = Square.of(move.to());

        Color movingColor = position.whiteToMove() ? Color.WHITE : Color.BLACK;
        int pawnIndex = PieceType.PAWN.toBitboardIndex(movingColor);
        int promotionPieceIndex = getPromotionPieceIndex(move.promotionType(), movingColor);

        long[] newBitboards = position.bitboards().clone();

        // Remove pawn from origin square
        newBitboards[pawnIndex] &= ~from.toBitboard();

        // Handle capture
        int capturedPiece = position.pieceAt(to.value());
        if (capturedPiece != -1) {
            newBitboards[capturedPiece] &= ~to.toBitboard();
        }

        // Place promoted piece on destination square
        newBitboards[promotionPieceIndex] |= to.toBitboard();

        // Update game state
        byte newCastlingRights = updateCastlingRights(position, move, pawnIndex, capturedPiece);
        int newHalfmoveClock = 0; // Pawn move resets halfmove clock
        int newFullmoveNumber = position.whiteToMove() ? position.fullmoveNumber() : position.fullmoveNumber() + 1;

        return Position.create(
                newBitboards,
                !position.whiteToMove(),
                newCastlingRights,
                -1, // No en passant after promotion
                newHalfmoveClock,
                newFullmoveNumber
        );
    }
    private static Position applyEnPassantMove(Position position, Move move) {
        Square from = Square.of(move.from());
        Square to = Square.of(move.to());

        Color movingColor = position.whiteToMove() ? Color.WHITE : Color.BLACK;
        Color capturedColor = movingColor.opposite();

        int movingPawnIndex = PieceType.PAWN.toBitboardIndex(movingColor);
        int capturedPawnIndex = PieceType.PAWN.toBitboardIndex(capturedColor);

        // Calculate captured pawn position
        int capturedPawnSquare = movingColor == Color.WHITE ? to.value() - 8 : to.value() + 8;
        Square capturedPawnPos = Square.of(capturedPawnSquare);

        long[] newBitboards = position.bitboards().clone();

        // Remove moving pawn from origin
        newBitboards[movingPawnIndex] &= ~from.toBitboard();

        // Remove captured pawn
        newBitboards[capturedPawnIndex] &= ~capturedPawnPos.toBitboard();

        // Place moving pawn on destination
        newBitboards[movingPawnIndex] |= to.toBitboard();

        // Update game state
        int newHalfmoveClock = 0; // Pawn move resets halfmove clock
        int newFullmoveNumber = position.whiteToMove() ? position.fullmoveNumber() : position.fullmoveNumber() + 1;

        return Position.create(
                newBitboards,
                !position.whiteToMove(),
                position.castlingRights(), // En passant doesn't affect castling
                -1, // No en passant after en passant capture
                newHalfmoveClock,
                newFullmoveNumber
        );
    }
    private static Position applyCastlingMove(Position position, Move move) {
        Square kingFrom = Square.of(move.from());
        Square kingTo = Square.of(move.to());

        Color color = position.whiteToMove() ? Color.WHITE : Color.BLACK;
        boolean isKingside = kingTo.value() > kingFrom.value();

        // Determine rook positions
        CastlingPositions positions = getCastlingPositions(color, isKingside);

        int kingIndex = PieceType.KING.toBitboardIndex(color);
        int rookIndex = PieceType.ROOK.toBitboardIndex(color);

        long[] newBitboards = position.bitboards().clone();

        // Move king
        newBitboards[kingIndex] &= ~kingFrom.toBitboard();
        newBitboards[kingIndex] |= kingTo.toBitboard();

        // Move rook
        newBitboards[rookIndex] &= ~positions.rookFrom().toBitboard();
        newBitboards[rookIndex] |= positions.rookTo().toBitboard();

        // Update castling rights (remove for the color that just castled)
        byte newCastlingRights = removeCastlingRights(position.castlingRights(), color);

        // Update game state
        int newHalfmoveClock = position.halfmoveClock() + 1; // King move doesn't reset clock
        int newFullmoveNumber = position.whiteToMove() ? position.fullmoveNumber() : position.fullmoveNumber() + 1;

        return Position.create(
                newBitboards,
                !position.whiteToMove(),
                newCastlingRights,
                -1, // No en passant after castling
                newHalfmoveClock,
                newFullmoveNumber
        );
    }

    private record CastlingPositions(Square rookFrom, Square rookTo) {}
    private static CastlingPositions getCastlingPositions(Color color, boolean isKingside) {
        if (color == Color.WHITE) {
            return isKingside ?
                    new CastlingPositions(Square.of(7), Square.of(5)) :  // Kingside: h1->f1
                    new CastlingPositions(Square.of(0), Square.of(3));   // Queenside: a1->d1
        } else {
            return isKingside ?
                    new CastlingPositions(Square.of(63), Square.of(61)) : // Kingside: h8->f8
                    new CastlingPositions(Square.of(56), Square.of(59));  // Queenside: a8->d8
        }
    }
    private static byte updateCastlingRights(Position position, Move move,
                                             int movingPiece, int capturedPiece) {
        byte rights = position.castlingRights();

        // Remove castling rights if king moves
        if (isKing(movingPiece)) {
            Color color = getColorFromPieceIndex(movingPiece);
            rights = removeCastlingRights(rights, color);
        }

        // Remove castling rights if rook moves
        if (isRook(movingPiece)) {
            rights = updateCastlingRightsForRookMove(rights, move.from());
        }

        // Remove castling rights if rook is captured
        if (capturedPiece != -1 && isRook(capturedPiece)) {
            rights = updateCastlingRightsForRookMove(rights, move.to());
        }

        return rights;
    }

    private static int getPromotionPieceIndex(int promotionType, Color color) {
        PieceType pieceType = switch (promotionType) {
            case Move.PROMO_KNIGHT -> PieceType.KNIGHT;
            case Move.PROMO_BISHOP -> PieceType.BISHOP;
            case Move.PROMO_ROOK -> PieceType.ROOK;
            case Move.PROMO_QUEEN -> PieceType.QUEEN;
            default -> throw new IllegalArgumentException("Invalid promotion type: " + promotionType);
        };
        return pieceType.toBitboardIndex(color);
    }
    private static byte removeCastlingRights(byte rights, Color color) {
        if (color == Color.WHITE) {
            return (byte) (rights & ~0x03); // Remove white castling rights
        } else {
            return (byte) (rights & ~0x0C); // Remove black castling rights
        }
    }

    private static byte updateCastlingRightsForRookMove(byte rights, int square) {
        return switch (square) {
            case 0 -> (byte) (rights & ~0x02);  // White queenside rook
            case 7 -> (byte) (rights & ~0x01);  // White kingside rook
            case 56 -> (byte) (rights & ~0x08); // Black queenside rook
            case 63 -> (byte) (rights & ~0x04); // Black kingside rook
            default -> rights;
        };
    }
    private static int updateEnPassantSquare(Position position, Move move, int movingPiece) {
        // Only pawn double moves create en passant opportunities
        if (!isPawn(movingPiece)) {
            return -1;
        }

        int fromSquare = move.from();
        int toSquare = move.to();
        int distance = Math.abs(toSquare - fromSquare);

        // Check for pawn double move
        if (distance == 16) {
            return (fromSquare + toSquare) / 2; // En passant square is between from and to
        }

        return -1;
    }
    private static int updateHalfmoveClock(Position position, int movingPiece, int capturedPiece) {
        // Reset clock on pawn move or capture
        if (isPawn(movingPiece) || capturedPiece != -1) {
            return 0;
        }
        return position.halfmoveClock() + 1;
    }

    // ===== UTILITY FUNCTIONS =====
    private static boolean isPawn(int pieceIndex) {
        return pieceIndex == 0 || pieceIndex == 6; // White pawn or black pawn
    }
    private static boolean isKing(int pieceIndex) {
        return pieceIndex == 5 || pieceIndex == 11; // White king or black king
    }
    private static boolean isRook(int pieceIndex) {
        return pieceIndex == 3 || pieceIndex == 9; // White rook or black rook
    }

    private static Color getColorFromPieceIndex(int pieceIndex) {
        return pieceIndex < 6 ? Color.WHITE : Color.BLACK;
    }
}