package UnitTEst;
import org.junit.Test;
import static org.junit.Assert.*;
import chess.*;
import chess.Board.Bitboards;

public class TestBoard {
    @Test
    public void testStandardChessboard() {
        String chessBoard = "rnbqkbnrpppppppp                                PPPPPPPPRNBQKBNR";
        long expectedWP = 65280L;
        long expectedWN = 66L;
        long expectedWB = 36L;
        long expectedWR = 129L;
        long expectedWQ = 16L;
        long expectedWK = 8L;
        long expectedBP = 71776119061217280L;
        long expectedBN = 4755801206503243776L;
        long expectedBB = 2594073385365405696L;
        long expectedBR = -9151314442816847872L;
        long expectedBQ = 1152921504606846976L;
        long expectedBK = 576460752303423488L;
        
        Bitboards bitboards = new Bitboards();

        Board.arrayToBitboards(chessBoard, bitboards);
    
        assertEquals(expectedWP, bitboards.wP);
        assertEquals(expectedWN, bitboards.wN);
        assertEquals(expectedWB, bitboards.wB);
        assertEquals(expectedWR, bitboards.wR);
        assertEquals(expectedWQ, bitboards.wQ);
        assertEquals(expectedWK, bitboards.wK);
         
        assertEquals(expectedBB, bitboards.bB);
        assertEquals(expectedBK, bitboards.bK);
        assertEquals(expectedBN, bitboards.bN);
        assertEquals(expectedBP, bitboards.bP);
        assertEquals(expectedBQ, bitboards.bQ);   
        assertEquals(expectedBR, bitboards.bR); 
    }
}
