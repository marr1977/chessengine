package martin.chess;

import java.util.Objects;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import martin.chess.engine.Board;
import martin.chess.engine.FENNotation;
import martin.chess.engine.Move;

/**
 * https://www.chessprogramming.org/Perft_Results
 */
public class PerftTest {

	private static final String POS_2 = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1";
	private static final String POS_3 = "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1";
	private static final String POS_5 = "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8";
	private static final String POS_6 = "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10";
	
	@Test
	public void bugFinding() {
		
//		You have 2735 moves for e2e4 but reference has 2748
//		You have 3689 moves for g2g4 but reference has 3702
		checkBoardStates(POS_3, 4);
		
		// You have 329 moves for h4g3 but reference has 328
		//checkBoardStates(POS_3, 3, "b4f4");
	
		// You have 4 moves for f4g4 but reference has 3
		// checkBoardStates(POS_3, 2, "b4f4", "h4g3");
		
		//checkBoardStates(POS_3, 2, "b4f4", "h4g3", "f4g4");
	}
	
	@Test
	public void position2_Depth1() {
		verifyNumBoardStates(POS_2, 1, 48);
	}

	@Test
	public void position2_Depth2() {
		verifyNumBoardStates(POS_2, 2, 2039);
	}

	@Test
	public void position2_Depth3() {
		verifyNumBoardStates(POS_2, 3, 97862);
		// 7591 ms
		// 7774 ms
		
		// 2295
	}
	
	@Test
	public void position2_Depth4() {
		verifyNumBoardStates(POS_2, 4, 4085603);
		// 327458 ms
	}
	
	@Test
	public void position3_Depth1() {
		verifyNumBoardStates(POS_3, 1, 14);
	}

	@Test
	public void position3_Depth2() {
		verifyNumBoardStates(POS_3, 2, 191);
	}

	@Test
	public void position3_Depth3() {
		verifyNumBoardStates(POS_3, 3, 2812);
	}

	@Test
	public void position3_Depth4() {
		// TODO: Wrong number of moves
		verifyNumBoardStates(POS_3, 4, 43238);
	}
	
	@Test
	public void position3_Depth5() {
		// TODO: No king
		verifyNumBoardStates(POS_3, 5, 674624);
		// 10652 ms
	}
	
	@Test
	public void position3_Depth6() {
		// TODO: No king
		verifyNumBoardStates(POS_3, 6, 11030083);
		// 166123
	}

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
		// 138629 ms
		// 143891 ms
		
		// 36552
		// 39075 ms
	}
	
	@Test
	public void position6_Depth1() {
		verifyNumBoardStates(POS_6, 1, 46);
	}

	@Test
	public void position6_Depth2() {
		verifyNumBoardStates(POS_6, 2, 2_079);
	}

	@Test
	public void position6_Depth3() {
		verifyNumBoardStates(POS_6, 3, 89_890);
		// 6289 ms
	}
	
	@Test
	public void position6_Depth4() {
		verifyNumBoardStates(POS_6, 4, 3_894_594);
		//267849 ms
	}

	private void verifyNumBoardStates(String fen, int depth, int numPos) {
		Board board = new Board(fen);
		board.setLogging(false);
		board.validateMoves(false);
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
	
	private Board checkBoardStates(String fen, int depth, String... moves) {
		Board board = new Board(fen);
		board.setLogging(false);
		
		for (String move : moves) {
			Move m = new Move(move);
			boolean moved = false;
			for (var availM : board.getAvailableMoves()) {
				if (availM.getIdxFrom() == m.getIdxFrom() && availM.getIdxTo() == m.getIdxTo() &&
					Objects.equals(availM.getQueeningPiece(), m.getQueeningPiece())) {
					board.move(availM);
					moved = true;
					break;
				}
			}
			if (!moved) {
				throw new IllegalArgumentException("Move " + move + " was not playable");
			}
		}
		System.out.println(FENNotation.toString(board));
		getNumberOfPositions(board, depth, true);
		return board;
	}


}
