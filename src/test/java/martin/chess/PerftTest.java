package martin.chess;

import org.junit.Assert;
import org.junit.Test;

import martin.chess.engine.Board;
import martin.chess.engine.Move;

/**
 * https://www.chessprogramming.org/Perft_Results
 */
public class PerftTest {

	private static final String POS_5 = "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8";
	
	@Test
	public void position5_Depth1() {
		verifyNumBoardStates(POS_5, 1, 44);
	}
	
	@Test
	public void position5_Depth2() {
		verifyNumBoardStates(POS_5, 2, 1_486);
	}
	
	@Test
	public void position5_Depth3() {
		verifyNumBoardStates(POS_5, 3, 62_379);
	}
	
	@Test
	public void position5_Depth4() {
		verifyNumBoardStates(POS_5, 4, 2_103_487 );
	}

	private void verifyNumBoardStates(String fen, int depth, int numPos) {
		Board board = new Board(fen);
		board.setLogging(false);
		long start = System.currentTimeMillis();
		Assert.assertEquals(numPos, getNumberOfPositions(board, depth, true));
		long end = System.currentTimeMillis();
		System.out.println("Depth " + depth + " took " + (end - start) + " ms");
	}
	
	private static int getNumberOfPositions(Board b, int depth, boolean log) {
    	if (depth == 0) {
    		return 1;
    	}
    	
    	int numPositions = 0;
    	
    	for (Move move : b.getAvailableMoves()) {
    		b.move(move);
    		int positions = getNumberOfPositions(b, depth - 1, false);
    		if (log) {
    			System.out.println(move + ": " + positions);
    		}
    		numPositions += positions;
    		b.undoLastMove();
    	}
    	
    	return numPositions;
	}
}
