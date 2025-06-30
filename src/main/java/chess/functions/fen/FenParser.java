package chess.functions.fen;

import chess.data.Color;
import chess.data.Position;
import chess.utilities.fenUtilities;

import java.util.Arrays;

import static chess.utilities.fenUtilities.*;


public class FenParser {
    public static Position parse(String FEN) {
        //throw new UnsupportedOperationException("Not supported yet.");

        if (FEN == null || FEN.trim().isEmpty()) {
            throw new IllegalArgumentException("FEN string cannot be null or empty"); // TODO: Create custom exceptions
        }

        String[] tokens = FEN.split("\\s+");
        if (tokens.length != 6) {
            throw new IllegalArgumentException("FEN string must have 6 components");
        }

        String piecePlacement = tokens[0];
        Long[] bitboard = parsePiecePlacement(piecePlacement);

        Color activePlayer = Color.BLACK;

        String activeColor = tokens[1];
        if ("w".equals(activeColor)) {
            activePlayer = Color.WHITE;
        } else if ("b".equals(activeColor)) {
            activePlayer = Color.BLACK;
        } else {
            throw new IllegalArgumentException("Invalid color: " + activeColor);
        }

        String castlingAvailability = tokens[2];


        //Position.create()
        //return Position; //TODO: Must continue with the fen parser implementation
        return standPos(); // Temp
    }

    private static Long[] parsePiecePlacement(String piecePlacement) {
        String[] ranks = piecePlacement.split("/");
        if (ranks.length != 8) {
            throw new IllegalArgumentException("FEN string must have 8 components");
        }

        Long[] bitboard = new Long[12];
        Arrays.fill(bitboard, 0L);

        for (int rank = 0; rank < 8; rank++) {
            String rankStr = ranks[rank];
            int file = 0;

            for (int i = 0; i < rankStr.length(); i++) {
                char c = rankStr.charAt(i);

                if (Character.isDigit(c)) {
                    // Empty squares
                    int emptySquares = Character.getNumericValue(c);
                    if (emptySquares < 1 || emptySquares > 8) {
                        throw new IllegalArgumentException("Invalid empty square count: " + emptySquares);
                    }
                    file += emptySquares;
                } else {
                    // Piece character
                    if (file >= 8) {
                        throw new IllegalArgumentException("Too many pieces/empty squares in rank: " + rankStr);
                    }

                    int chessRank = 7 - rank;  // Convert FEN rank to chess rank
                    int square = chessRank * 8 + file;  // a1=0, b1=1, ..., h8=63
                    long bit = 1L << square;

                    // Place the piece on the appropriate bitboard
                    switch (c) {
                        case 'r': bitboard[bR] |= bit; break;
                        case 'n': bitboard[bN] |= bit; break;
                        case 'b': bitboard[bB] |= bit; break;
                        case 'q': bitboard[bQ] |= bit; break;
                        case 'k': bitboard[bK] |= bit; break;
                        case 'p': bitboard[bP] |= bit; break;
                        case 'R': bitboard[wR] |= bit; break;
                        case 'N': bitboard[wN] |= bit; break;
                        case 'B': bitboard[wB] |= bit; break;
                        case 'Q': bitboard[wQ] |= bit; break;
                        case 'K': bitboard[wK] |= bit; break;
                        case 'P': bitboard[wP] |= bit; break;
                        default:
                            throw new IllegalArgumentException("Invalid piece character: " + c);
                    }
                    file++;
                }
            }

            if (file != 8) {
                throw new IllegalArgumentException("Invalid rank length: " + rankStr);
            }
        }

        return bitboard;
    }

    public static Position standPos() {
        final long[] bitboard = fenUtilities.standChess();
        final boolean whiteToMove = true;
        final byte castLingRights = 0b1111;
        final int enPassantSquare = -1;
        final int halfMoveCounter = 0;
        final int fullMoveCounter = 0;

        return Position.create(
                bitboard,
                whiteToMove,
                castLingRights,
                enPassantSquare,
                halfMoveCounter,
                fullMoveCounter
        );
    }
}
