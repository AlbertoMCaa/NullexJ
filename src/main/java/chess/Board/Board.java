package chess.Board;

import java.util.*;

import chess.Board.Move.Move;
import chess.Pieces.Piece;

import static chess.Board.BoardUtils.*;
import static chess.Board.Move.Move.NO_CAPTURE;
import static chess.Pieces.Piece.*;

public class Board {
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

    // Castling flags (4-bit KQkq system)
    private static final byte WHITE_KINGSIDE  = 0b1000;  // K
    private static final byte WHITE_QUEENSIDE = 0b0100;  // Q
    private static final byte BLACK_KINGSIDE  = 0b0010;  // k
    private static final byte BLACK_QUEENSIDE = 0b0001;  // q
    private static final byte CASTLE_ALL = 0b1111;       // KQkq

    public long[] bitboards = new long[12];
    private int enPassantSquare = -1; // -1 for no en passant
    private byte castlingRights = CASTLE_ALL; // 4-bit flags: KQkq

    private final ArrayDeque<Move> moveHistory = new ArrayDeque<>(); // Tried arrays but this is faster. Should do more testing in a real chessboard play

    public boolean isWhiteToMove = true;
    private boolean isBotWhite;

    private long occupied = 0L;

    /*
     * Constructor of a normal chess board.
     */
    public static Board initiateChess()
    {
        Board board = new Board();
        String chessBoard = "rnbqkbnrpppppppp                                PPPPPPPPRNBQKBNR";
        
        board.arrayToBitboards(chessBoard);
        board.updateOccupied();
        printBitboardArray(board);
        return board;
    }
    public static Board getChessBoard(){
        return new Board();
    }
    public Board(){}
    public Board(Board other)
    {
        this.bitboards = Arrays.copyOf(other.bitboards, other.bitboards.length);
        this.enPassantSquare = other.enPassantSquare;
        this.isWhiteToMove = other.isWhiteToMove;
        this.isBotWhite = other.isBotWhite;
    }

/*
 * The first character of the string is the a1 tile and the last tile is the h8 tile.
 * Which means that the bitboard representation of the black rooks is : 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 10000001
 * 
 * This method takes a Bitboards object, which is an array of 64-bit longs that represents the positions of the pieces on the board.  Each piece type has its own bitboard (long).
 * This method also takes a String representation of the chess board in one dimensional array.
 * 
 * This representation of the board works as follows:
 *  Each piece is represented by a character in the string.
 *  If the character is a capital letter, the piece is white. Otherwise, the piece is black.
 * 
 *  R = rook
 *  N = knight
 *  Q = queen
 *  K = king
 *  B = bishop
 *  P = pawn
 * 
 * First, we loop through each character in the string. 
 * "bit" is a single-bit mask that shifts to the left  to represent the position in the bitboard. "chessTiles -1 -i" adjusts the index to match the bitboard convention (from a8 to h1)
 * In the first loop iteration, the bit is shifted 63 to the left (64 - 0 - 1) representation of a8. Then an "or" operation is performed on the bitboard and the bit is set to 1.
 * 
 * If the character is a non-recognized piece, the default case does nothing.
 */ 
    public void arrayToBitboards(String chessBoard)
    {
        for (int i = 0; i < chessTiles; i++)
        {
            long bit = 1L << (chessTiles - 1 - i);
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
    }
    public boolean isEmpty(int square)
    {
        return ((occupied & (1L << square)) == 0);
    }
    public void updateOccupied()
    {
        occupied = 0L;
        for (long bb : bitboards) occupied |= bb;
    }
    public int getPieceCode(int position)
    {
        long bit = 1L << position;
        for(int i = 0; i < bitboards.length; i++)
        {
            if ((bitboards[i] & bit) != 0)
            {
                return switch (i)
                {
                    case 0 ->  // wP
                            0b10001; // White Pawn: 0b10000 (white)
                    case 1 ->  // wN
                            0b10010; // White Knight: 0b10000 | 0b010
                    case 2 ->  // wB
                            0b10100; // White Bishop: 0b10000 | 0b100
                    case 3 ->  // wR
                            0b10101; // White Rook:  0b10000 | 0b101
                    case 4 ->  // wQ
                            0b10110; // White Queen: 0b10000 | 0b110
                    case 5 ->  // wK
                            0b10111; // White King:  0b10000 | 0b111
                    case 6 ->  // bP
                            0b01001; // Black Pawn: 0b01000 (black)
                    case 7 ->  // bN
                            0b01010; // Black Knight: 0b01000 | 0b010
                    case 8 ->  // bB
                            0b01100; // Black Bishop: 0b01000 | 0b100
                    case 9 ->  // bR
                            0b01101; // Black Rook:  0b01000 | 0b101
                    case 10 -> // bQ
                            0b01110; // Black Queen: 0b01000 | 0b110
                    case 11 -> // bK
                            0b01111; // Black King:  0b01000 | 0b111
                    default -> -1;
                };
            }
        }
        return -1; // there is no piece at this position
    }
    public int getPieceBitBoardCode(int position)
    {
        long bit = 1L << position;
        for(int i = 0; i < bitboards.length; i++)
        {
            if ((bitboards[i] & bit) != 0)
            {
                return i;
            }
        }
        return -1; // there is no piece at this position
    }
    public int getEnPassantSquare()
    {
        return enPassantSquare;
    }
    public void setEnPassantSquare(int enPassantSquare){
        this.enPassantSquare = enPassantSquare;
    }

    public void makeMove2(Move move)
    {
        int movingPieceBitboardCode = getPieceBitBoardCode(move.getMovingPiece());
        int fromSquare = move.getFrom();
        int toSquare =  move.getTo();
        int promotionPiece = move.getPromo();
        int capturedPiece = move.getCapturedPiece() != NO_CAPTURE ? getPieceBitBoardCode(toSquare) : NO_CAPTURE; // this is correct
        boolean enPassant = move.isEnPassant();
        boolean castling = move.isCastling();

        // Always remove the movingPiece from its current square
        bitboards[movingPieceBitboardCode] ^= 1L << fromSquare;

        // The, add it to its destination square if it's not a promotion move
        if (promotionPiece != 0) {
            int btCode = switch (promotionPiece) {
                case PAWN -> isWhiteToMove ? wP : bP;
                case KNIGHT -> isWhiteToMove ? wN : bN;
                case BISHOP -> isWhiteToMove ? wB : bB;
                case QUEEN -> isWhiteToMove ? wQ : bQ;
                case ROOK -> isWhiteToMove ? wR : bR;
                default -> throw new IllegalStateException("Unexpected value: " + promotionPiece);
            };

            bitboards[btCode] |= 1L << toSquare;

        } else { // place piece at destination
            bitboards[movingPieceBitboardCode] |= 1L << toSquare;
        }

        // handle capturing pieces
        if (capturedPiece != NO_CAPTURE && !enPassant) {
            bitboards[capturedPiece] ^= 1L << toSquare;
        }

        if (enPassant) {
            int capturedPawnSquare = isWhiteToMove ? toSquare - 8 : toSquare + 8;
            int capturedPawnCode = isWhiteToMove ? bP : wP;
            bitboards[capturedPawnCode] ^= 1L << capturedPawnSquare;
        }

        if (castling) {
            // Determine if it's kingside or queenside castling
            final boolean isKingSide = (toSquare - fromSquare) == 2;
            final int rookCode = isWhiteToMove ? wR : bR;

            if (isKingSide) {
                final int rookFrom = isWhiteToMove ? 7 : 63; // h1 or h8
                final int rookTo   = isWhiteToMove ? 5 : 61; // f1 or f8

                bitboards[rookCode] ^= 1L << rookFrom;
                bitboards[rookCode] |= 1L << rookTo;
            } else {
                final int rookFrom = isWhiteToMove ? 0 : 56; // a1 or a8
                final int rookTo   = isWhiteToMove ? 3 : 59; // d1 or d8

                bitboards[rookCode] ^= 1L << rookFrom;
                bitboards[rookCode] |= 1L << rookTo;
            }

            // Update castling rights
            final byte mask = (byte) (isWhiteToMove ? 0b1100 : 0b0011);
            this.castlingRights &= ~mask; // set to 0 the corresponding bits.
        }

        // handle king moves (not castling)
        if (move.getMovingPiece() == KING && !castling) {
            byte mask = (byte) (isWhiteToMove ? 0b1100 : 0b0011);
            this.castlingRights &= ~mask; // Clear the bits (remove rights)
        }

        // handle rook mves
        if (move.getMovingPiece() == ROOK) {
            if (isWhiteToMove) {
                if (fromSquare == 0) { // a1 rook
                    this.castlingRights &= ~0b0100; // Remove white queenside
                } else if (fromSquare == 7) { // h1 rook
                    this.castlingRights &= ~0b1000; // Remove white kingside
                }
            }else {
                if (fromSquare == 56) { // a8 rook
                    this.castlingRights &= ~0b0001; // Remove black queenside
                } else if (fromSquare == 63) { // h8 rook
                    this.castlingRights &= ~0b0010; // Remove black kingside
                }
            }
        }

        // Handle rook captures - remove castling rights if corner rook is captured
        if (capturedPiece != NO_CAPTURE && move.getCapturedPiece() == ROOK) {
            if (toSquare == 0) { // a1 rook captured
                this.castlingRights &= ~0b0100; // Remove white queenside
            } else if (toSquare == 7) { // h1 rook captured
                this.castlingRights &= ~0b1000; // Remove white kingside
            } else if (toSquare == 56) { // a8 rook captured
                this.castlingRights &= ~0b0001; // Remove black queenside
            } else if (toSquare == 63) { // h8 rook captured
                this.castlingRights &= ~0b0010; // Remove black kingside
            }
        }

        if (move.getMovingPiece() == PAWN && Math.abs(toSquare - fromSquare) == 16) {
            // This is a double pawn push - check if enemy pawns can capture en passant
            int enPassantTargetSquare = isWhiteToMove ? fromSquare + 8 : fromSquare - 8;

            // check for adjacent enemy pawns that can capture
            int targetFile = enPassantTargetSquare % 8;
            int targetRank = enPassantTargetSquare / 8;

            boolean canBeCaptured = false;
            int enemyPawnCode = isWhiteToMove ? bP : wP;

            // check left adjacent file
            if (targetFile > 0) {
                int leftSquare = targetRank * 8 + (targetFile - 1);
                if ((bitboards[enemyPawnCode] & (1L << leftSquare)) != 0) {
                    canBeCaptured = true;
                }
            }

            // check right adjacent file
            if (targetFile < 7) {
                int rightSquare = targetRank * 8 + (targetFile + 1);
                if ((bitboards[enemyPawnCode] & (1L << rightSquare)) != 0){
                    canBeCaptured = true;
                }
            }

            if (canBeCaptured) {
                this.enPassantSquare = enPassantTargetSquare;
            } else {
                // Clear en passant target square for any other move
                this.enPassantSquare = -1;
            }
        } else {
            this.enPassantSquare = -1;
        }
        Move move1 = Move.create(move.getFrom(),move.getTo(),move.getPromo(),move.getMovingPiece(),move.getCapturedPiece(),castling,this.enPassantSquare != -1,this.castlingRights);
        moveHistory.addLast(move1);
        isWhiteToMove = !isWhiteToMove;
        updateOccupied(); // optimized this to a partial
    }
    public void unmakeMove2(){
        if (moveHistory.isEmpty()) return;
        Move move = moveHistory.pollLast();

        int movingPieceBitboardCode = getPieceBitBoardCode(move.getMovingPiece()); // this is correct
        int fromSquare = move.getFrom();
        int toSquare =  move.getTo();
        int promotionPiece = move.getPromo(); // returns promoted piece like in the switch.
        int capturedPiece = move.getCapturedPiece() != NO_CAPTURE ? getPieceBitBoardCode(move.getCapturedPiece()) : NO_CAPTURE; //this is correct
        boolean enPassant = move.isEnPassant(); // this returns a boolean, if the move was en passant
        boolean castling = move.isCastling(); // thi is a boolean, also correct.

        isWhiteToMove = !isWhiteToMove;

        // add the moving piece to its original position
        bitboards[movingPieceBitboardCode] |= 1L << fromSquare;
        bitboards[movingPieceBitboardCode] &= ~(1L << toSquare); // Remove from destination

        // Restore promotion move
        if (promotionPiece != 0) {
            // If it was a promotion, remove the promoted piece from destination
            int promotedPieceBtCode = switch (promotionPiece) {
                case PAWN -> isWhiteToMove ? wP : bP;
                case KNIGHT -> isWhiteToMove ? wN : bN;
                case BISHOP -> isWhiteToMove ? wB : bB;
                case QUEEN -> isWhiteToMove ? wQ : bQ;
                case ROOK -> isWhiteToMove ? wR : bR;
                default -> throw new IllegalStateException("Unexpected value: " + promotionPiece);
            };
            bitboards[promotedPieceBtCode] ^= 1L << toSquare;
        }

        // 2. Restore captured pieces
        if (capturedPiece != NO_CAPTURE && !enPassant) {
            bitboards[capturedPiece] |= 1L << toSquare;
        }

        // 3. Handle en passant capture reversal
        if (enPassant) {
            // Restore the captured pawn
            int capturedPawnSquare = isWhiteToMove ? toSquare - 8 : toSquare + 8;
            int capturedPawnCode = isWhiteToMove ? bP : wP;
            bitboards[capturedPawnCode] |= 1L << capturedPawnSquare;
            this.enPassantSquare = toSquare; // restore enPassantSquare
        } else {
            this.enPassantSquare = -1;
        }

        // 4. Handle castling reversal
        if (castling) {
            // Determine if it was kingside or queenside castling
            final boolean isKingSide = (toSquare - fromSquare) == 2;
            final int rookCode = isWhiteToMove ? wR : bR;

            if (isKingSide) {
                final int rookFrom = isWhiteToMove ? 7 : 63; // h1 or h8 (original position)
                final int rookTo = isWhiteToMove ? 5 : 61;   // f1 or f8 (where it moved to)

                // Move rook back from f1/f8 to h1/h8
                bitboards[rookCode] ^= 1L << rookTo;
                bitboards[rookCode] |= 1L << rookFrom;

                this.castlingRights |= (byte) (isWhiteToMove ?
                                        (WHITE_KINGSIDE | WHITE_QUEENSIDE) :
                                        (BLACK_KINGSIDE | BLACK_QUEENSIDE));
            } else {
                final int rookFrom = isWhiteToMove ? 0 : 56; // a1 or a8 (original position)
                final int rookTo = isWhiteToMove ? 3 : 59;   // d1 or d8 (where it moved to)

                // Move rook back from d1/d8 to a1/a8
                bitboards[rookCode] ^= 1L << rookTo;
                bitboards[rookCode] |= 1L << rookFrom;

                this.castlingRights |= (byte) (isWhiteToMove ?
                                        (WHITE_KINGSIDE | WHITE_QUEENSIDE) :
                                        (BLACK_KINGSIDE | BLACK_QUEENSIDE));
            }
        }

        if (move.getMovingPiece() == KING && !castling) {
            int originalSquare = isWhiteToMove ? 4 : 60;
            boolean can = toSquare == originalSquare;

            byte mask = (byte) (isWhiteToMove ? 0b1100 : 0b0011);
            this.castlingRights &= ~mask;
        }

        // 6. Update occupied bitboards
        updateOccupied();
    }

    public boolean isWhiteToMove() {
        return isWhiteToMove;
    }
    public long getFriendlyPieces() {
        return isWhiteToMove ?
                bitboards[wP] | bitboards[wN] | bitboards[wB] |
                        bitboards[wR] | bitboards[wQ] | bitboards[wK]
                :
                bitboards[bP] | bitboards[bN] | bitboards[bB] |
                        bitboards[bR] | bitboards[bQ] | bitboards[bK];
    }
    public long getEnemyPieces() {
        return isWhiteToMove ?
                bitboards[bP] | bitboards[bN] | bitboards[bB] |
                        bitboards[bR] | bitboards[bQ] | bitboards[bK]
                :
                bitboards[wP] | bitboards[wN] | bitboards[wB] |
                        bitboards[wR] | bitboards[wQ] | bitboards[wK];
    }
    public long getOccupied() {
        return occupied;
    }
    public long[] getBitboards() {
        return bitboards;
    }
    public long getBitboard(int bitboard){
        assert bitboard >= 0 && bitboard <= 11 : "Invalid bitboard index: " + bitboard; // DEBUG
        return bitboards[bitboard];
    }

    /**
     * @return Current castling rights as byte (4 bits: KQkq)
     */
    public byte getCastlingRights() {
        return castlingRights;
    }

    /**
     * Disables specific castling right
     * @param right One of WHITE_KINGSIDE, WHITE_QUEENSIDE, BLACK_KINGSIDE, BLACK_QUEENSIDE
     */
    public void disableCastlingRight(byte right) {
        castlingRights &= (byte) ~right;
    }

    /**
     * Creates a Board from a FEN (Forsyth-Edwards Notation) string
     * FEN format: "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
     * Components: piece_placement active_color castling_availability en_passant halfmove_clock fullmove_number
     *
     * @param fen The FEN string representing the board position
     * @return A Board object configured according to the FEN string
     * @throws IllegalArgumentException if the FEN string is invalid
     */
    public static Board createBoardFromFEN(String fen) {
        if (fen == null || fen.trim().isEmpty()) {
            throw new IllegalArgumentException("FEN string cannot be null or empty");
        }

        String[] fenParts = fen.trim().split("\\s+");
        if (fenParts.length != 6) {
            throw new IllegalArgumentException("Invalid FEN string: must have exactly 6 components");
        }

        Board board = new Board();

        // 1. Parse piece placement (first part)
        String piecePlacement = fenParts[0];
        parsePiecePlacement(board, piecePlacement);

        // 2. Parse active color (second part)
        String activeColor = fenParts[1];
        if ("w".equals(activeColor)) {
            board.isWhiteToMove = true;
        } else if ("b".equals(activeColor)) {
            board.isWhiteToMove = false;
        } else {
            throw new IllegalArgumentException("Invalid active color in FEN: " + activeColor);
        }

        // 3. Parse castling availability (third part)
        String castlingAvailability = fenParts[2];
        parseCastlingRights(board, castlingAvailability);

        // 4. Parse en passant target square (fourth part)
        String enPassantTarget = fenParts[3];
        parseEnPassantSquare(board, enPassantTarget);

        // 5. Parse halfmove clock (fifth part)
        String halfmoveClockStr = fenParts[4];
        parseHalfmoveClock(board, halfmoveClockStr);

        // 6. Parse fullmove counter (sixth part)
        String fullmoveCounterStr = fenParts[5];
        parseFullmoveCounter(board, fullmoveCounterStr);

        // Update the occupied bitboard
        board.updateOccupied();
        //BoardUtils.printBitboards(board);
        return board;
    }

    /**
     * Parses the piece placement portion of the FEN string
     * Updated for standard board representation (a1=0, h8=63)
     */
    private static void parsePiecePlacement(Board board, String piecePlacement) {
        String[] ranks = piecePlacement.split("/");
        if (ranks.length != 8) {
            throw new IllegalArgumentException("Invalid piece placement: must have 8 ranks");
        }

        // Clear all bitboards first
        Arrays.fill(board.bitboards, 0L);

        // Process each rank (8th rank first, 1st rank last)
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

                    // Calculate square index for a1=0 representation
                    // FEN rank 0 = chess rank 8, FEN rank 7 = chess rank 1
                    int chessRank = 7 - rank;  // Convert FEN rank to chess rank
                    int square = chessRank * 8 + file;  // a1=0, b1=1, ..., h8=63
                    long bit = 1L << square;

                    // Place the piece on the appropriate bitboard
                    switch (c) {
                        case 'r': board.bitboards[Board.bR] |= bit; break;
                        case 'n': board.bitboards[Board.bN] |= bit; break;
                        case 'b': board.bitboards[Board.bB] |= bit; break;
                        case 'q': board.bitboards[Board.bQ] |= bit; break;
                        case 'k': board.bitboards[Board.bK] |= bit; break;
                        case 'p': board.bitboards[Board.bP] |= bit; break;
                        case 'R': board.bitboards[Board.wR] |= bit; break;
                        case 'N': board.bitboards[Board.wN] |= bit; break;
                        case 'B': board.bitboards[Board.wB] |= bit; break;
                        case 'Q': board.bitboards[Board.wQ] |= bit; break;
                        case 'K': board.bitboards[Board.wK] |= bit; break;
                        case 'P': board.bitboards[Board.wP] |= bit; break;
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
    }

    /**
     * Parses the castling rights portion of the FEN string
     * Supports both standard FEN (KQkq) and Shredder-FEN (file letters A-H, a-h)
     */
    private static void parseCastlingRights(Board board, String castlingAvailability) {
        board.castlingRights = 0; // Clear all castling rights first

        if ("-".equals(castlingAvailability)) {
            return; // No castling rights
        }

        for (int i = 0; i < castlingAvailability.length(); i++) {
            char c = castlingAvailability.charAt(i);

            if (c >= 'A' && c <= 'H') {
                // Shredder-FEN: White castling (uppercase file letters)
                // For simplicity, we'll map common cases
                if (c == 'H' || c == 'K') {
                    board.castlingRights |= 0b1000; // White kingside
                } else if (c == 'A' || c == 'Q') {
                    board.castlingRights |= 0b0100; // White queenside
                } else {
                    // Other files - treat as valid but map to appropriate side
                    // You may need to adjust this based on your specific needs
                    if (c >= 'E') {
                        board.castlingRights |= 0b1000; // Kingside for files E-H
                    } else {
                        board.castlingRights |= 0b0100; // Queenside for files A-D
                    }
                }
            } else if (c >= 'a' && c <= 'h') {
                // Shredder-FEN: Black castling (lowercase file letters)
                if (c == 'h' || c == 'k') {
                    board.castlingRights |= 0b0010; // Black kingside
                } else if (c == 'a' || c == 'q') {
                    board.castlingRights |= 0b0001; // Black queenside
                } else {
                    // Other files - treat as valid but map to appropriate side
                    if (c >= 'e') {
                        board.castlingRights |= 0b0010; // Kingside for files e-h
                    } else {
                        board.castlingRights |= 0b0001; // Queenside for files a-d
                    }
                }
            } else {
                // Standard FEN characters
                switch (c) {
                    case 'K': // White kingside
                        board.castlingRights |= 0b1000;
                        break;
                    case 'Q': // White queenside
                        board.castlingRights |= 0b0100;
                        break;
                    case 'k': // Black kingside
                        board.castlingRights |= 0b0010;
                        break;
                    case 'q': // Black queenside
                        board.castlingRights |= 0b0001;
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid castling character: " + c);
                }
            }
        }
    }

    /**
     * Parses the en passant target square portion of the FEN string
     * Updated for standard board representation (a1=0, h8=63)
     */
    private static void parseEnPassantSquare(Board board, String enPassantTarget) {
        if ("-".equals(enPassantTarget)) {
            board.enPassantSquare = -1;
            return;
        }

        if (enPassantTarget.length() != 2) {
            throw new IllegalArgumentException("Invalid en passant target: " + enPassantTarget);
        }

        char fileChar = enPassantTarget.charAt(0);
        char rankChar = enPassantTarget.charAt(1);

        if (fileChar < 'a' || fileChar > 'h') {
            throw new IllegalArgumentException("Invalid en passant file: " + fileChar);
        }

        if (rankChar != '3' && rankChar != '6') {
            throw new IllegalArgumentException("Invalid en passant rank: " + rankChar + " (must be 3 or 6)");
        }

        int file = fileChar - 'a';
        int rank = rankChar - '1';

        // Convert to board indexing (a1=0, b1=1, ..., h8=63) - standard representation
        board.enPassantSquare = rank * 8 + file;
    }

    /**
     * Parses the halfmove clock portion of the FEN string
     */
    private static void parseHalfmoveClock(Board board, String halfmoveClockStr) {
        try {
            int halfmoveClock = Integer.parseInt(halfmoveClockStr);
            if (halfmoveClock < 0) {
                throw new IllegalArgumentException("Halfmove clock cannot be negative: " + halfmoveClock);
            }
            //board.halfmoveClock = halfmoveClock;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid halfmove clock: " + halfmoveClockStr);
        }
    }

    /**
     * Parses the fullmove counter portion of the FEN string
     */
    private static void parseFullmoveCounter(Board board, String fullmoveCounterStr) {
        try {
            int fullmoveCounter = Integer.parseInt(fullmoveCounterStr);
            if (fullmoveCounter < 1) {
                throw new IllegalArgumentException("Fullmove counter must be at least 1: " + fullmoveCounter);
            }
            //board.fullmoveCounter = fullmoveCounter;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid fullmove counter: " + fullmoveCounterStr);
        }
    }
}
