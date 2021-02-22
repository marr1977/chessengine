package martin.chess;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Test;

import martin.chess.engine.Board;
import martin.chess.engine.FENNotation;
import martin.chess.engine.Move;

/**
 * https://www.chessprogramming.org/Perft_Results
 */
public class PerftTest {
	
	private static boolean DISABLE_THREADS = false;

	private static final String POS_1 = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
	private static final String POS_2 = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1";
	private static final String POS_3 = "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1";
	private static final String POS_4 = "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1";
	private static final String POS_4_MIRROR = "r2q1rk1/pP1p2pp/Q4n2/bbp1p3/Np6/1B3NBn/pPPP1PPP/R3K2R b KQ - 0 1 ";
	private static final String POS_5 = "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8";
	private static final String POS_6 = "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10";
	
	@Test
	public void bugFinding() {
		
		// You have 318 moves for a5b4 but reference has 319
		// checkBoardStates(POS_3, 3, "b4a4", "h4g3");

		// You have 5 moves for c7c5 but reference has 6
		//checkBoardStates(POS_3, 2, "b4a4", "h4g3", "a5b4");
		
		// b5b6 missing
		//checkBoardStates(POS_3, 1, "b4a4", "h4g3", "a5b4", "c7c5");
	}
	
	@Test
	public void position1_Depth1() {
		verifyNumBoardStates(POS_1, 1, 20);
	}
	
	@Test
	public void position1_Depth2() {
		verifyNumBoardStates(POS_1, 2, 400);
	}

	@Test
	public void position1_Depth3() {
		verifyNumBoardStates(POS_1, 3, 8902);
	}

	@Test
	public void position1_Depth4() {
		verifyNumBoardStates(POS_1, 4, 197_281);
	}

	@Test
	public void position1_Depth5() {
		verifyNumBoardStates(POS_1, 5, 4_865_609);
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
		
		// 2295 ms after re-write
		
		// 1823 ms after tweaks
	}
	
	@Test
	public void position2_Depth4() {
		verifyNumBoardStates(POS_2, 4, 4085603);
		// 327458 ms
		
		// 72756 ms after re-write
		
		// 58557 ms after tweaks
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
		verifyNumBoardStates(POS_3, 4, 43238);
	}
	
	@Test
	public void position3_Depth5() {
		verifyNumBoardStates(POS_3, 5, 674624);
		// 10652 ms
		
		// 4149 ms after tweaks and re-writes
	}
	
	@Test
	public void position3_Depth6() {
		verifyNumBoardStates(POS_3, 6, 11_030_083);
		// 166123 ms
		
		// 92073 ms efter omskrivning
		
		// 70240 ms efter lite smågrejor
		
		// 60442 ms
		
		// 57907 ms
	}
	
	@Test
	public void position4_Depth1() {
		verifyNumBoardStates(POS_4, 1, 6);
	}
	
	@Test
	public void position4_Depth2() {
		verifyNumBoardStates(POS_4, 2, 264);
	}
	
	@Test
	public void position4_Depth3() {
		verifyNumBoardStates(POS_4, 3, 9467);
	}

	@Test
	public void position4_Depth4() {
		verifyNumBoardStates(POS_4, 4, 422_333);
	}

	@Test
	public void position4_Depth5() {
		verifyNumBoardStates(POS_4, 5, 15_833_292);
		
		// 87 seconds with threads enabled
	}
	
	@Test
	public void position4_Mirror_Depth1() {
		verifyNumBoardStates(POS_4_MIRROR, 1, 6);
	}
	
	@Test
	public void position4_Mirror_Depth2() {
		verifyNumBoardStates(POS_4_MIRROR, 2, 264);
	}
	
	@Test
	public void position4_Mirror_Depth3() {
		verifyNumBoardStates(POS_4_MIRROR, 3, 9467);
	}

	@Test
	public void position4_Mirror_Depth4() {
		verifyNumBoardStates(POS_4_MIRROR, 4, 422_333);
	}

	@Test
	public void position4_Mirror_Depth5() {
		verifyNumBoardStates(POS_4_MIRROR, 5, 15_833_292);
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
		
		// 26309 ms
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
		
		//71453 ms
		
		//57751 ms
		
		//55096 ms
	}

	private void verifyNumBoardStates(String fen, int depth, int numPos) {
		long start = System.currentTimeMillis();
		try {
			Assert.assertEquals(numPos, getNumberOfPositions(fen, depth));
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		long end = System.currentTimeMillis();
		System.out.println("Depth " + depth + " took " + (end - start) + " ms");
	}
	
	private static int getNumberOfPositions(String fen, int depth) throws Exception {
    	if (!DISABLE_THREADS && depth > 2) {
    		int numThreads = Runtime.getRuntime().availableProcessors() - 1;
    		ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
    		
    		List<Future<Integer>> futures = new ArrayList<>();
    		
    		for (int i = 0; i < numThreads; i++) {
    			BoardChecker checker = new BoardChecker(fen, depth, i, numThreads);
    			futures.add(executorService.submit(checker));
    		}
    		
    		int numPositions = 0;
    		
    		for (var future : futures) {
    			numPositions += future.get();
    		}
    		
    		return numPositions;
    	} else {
    		BoardChecker checker = new BoardChecker(fen, depth, 0, 1);
    		return checker.call();
    	}
    	
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
		try {
			getNumberOfPositions(FENNotation.toString(board), depth);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return board;
	}
	
	static class BoardChecker implements Callable<Integer> {

		private String fen;
		private int myThreadIdx;
		private int numThreads;
		private Board board;
		private int depth;

		public BoardChecker(String fen, int depth, int myThreadIdx, int numThreads) {
			this.fen = fen;
			this.depth = depth;
			this.myThreadIdx = myThreadIdx;
			this.numThreads = numThreads;
		}

		@Override
		public Integer call() throws Exception {
			board = new Board(fen);

			board.setLogging(false);
			board.validateMoves(false);
			
			return getNumberOfPositions(depth, true, true);
		}
		
		private int getNumberOfPositions(int depth, boolean log, boolean filter) throws Exception {
			if (depth == 0) {
				return 1;
			}
			
			int numPositions = 0;
			List<Move> moves = board.getAvailableMoves();
			
			for (int i = 0; i < moves.size(); ++i) {
	    		if (filter) {
	    			if (i % numThreads != myThreadIdx) {
	    				continue;
	    			}
	    		}
	    		Move move = moves.get(i);
	    		board.move(move);
	    		int positions = getNumberOfPositions(depth - 1, false, false);
	    		if (log) {
	    			System.out.println(move + ": " + positions);
	    		}
	    		numPositions += positions;
	    		board.undoLastMove();
	    	}
	    	
	    	return numPositions;
		}
		
	}
	


}
