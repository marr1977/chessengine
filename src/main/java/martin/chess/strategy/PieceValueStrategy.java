package martin.chess.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import martin.chess.engine.Board;
import martin.chess.engine.Color;
import martin.chess.engine.GameOutcome;
import martin.chess.engine.Move;
import martin.chess.fen.FENNotation;

/**
 * Looks only at the piece values, does not take into account opponent moves
 */
public class PieceValueStrategy implements IPlayerStrategy {

	private int depth;
	private int numThreads;
	private ExecutorService executorService;
	private ValuationMode valuationMode;
	
	public enum ValuationMode {
		END_STATE_ONLY,
		INTERMEDIATE_STATES_DECAY_90
	}
	
	public PieceValueStrategy(ValuationMode valuationMode, int depth, int numThreads) {
		this.valuationMode = valuationMode;
		this.depth = depth;
		this.numThreads = numThreads;
		executorService = Executors.newFixedThreadPool(numThreads);
	}
	
	@Override
	public Move getMove(Board board) throws InterruptedException {
		String fen = FENNotation.toString(board);
		
		List<Future<MoveAndValue>> futures = new ArrayList<>();
		
		for (int i = 0; i < numThreads; i++) {
			BoardChecker checker = new BoardChecker(board.getColorToMove(), valuationMode, fen, depth, i, numThreads);
			futures.add(executorService.submit(checker));
		}
		
		MoveAndValue bestMove = null;
		
		for (var future : futures) {
			try {
				MoveAndValue moveValue = future.get();
				
				if (moveValue == null) {
					// There were no moves assigned for this thread
					continue;
				}
				
				if (bestMove == null || moveValue.value > bestMove.value) {
					bestMove = moveValue;
					//System.out.println("New best move is " + moveValue + " (old " + bestMove + ")");
				} else {
					//System.out.println("Move " + moveValue + " did not beat current best " + bestMove);
				}
				
			}
			catch (ExecutionException e) {
				System.err.println("Caught exception: " + e);
			}
		}
		
		return bestMove.move;
	}
	
	private static class MoveAndValue {
		Move move;
		double value;

		public MoveAndValue(Move move, double value) {
			this.move = move;
			this.value = value;
		}

		@Override
		public String toString() {
			return String.format("%s (%f)", move, value);
		}
	}
	
	private static class BoardChecker implements Callable<MoveAndValue> {

		private String fen;
		private int myThreadIdx;
		private int numThreads;
		private Board board;
		private int depth;
		private Color myColor;
		private ValuationMode valuationMode;
		private double decayFactor = 1.0;
		
		public BoardChecker(Color myColor, ValuationMode valuationMode, String fen, int depth, int myThreadIdx, int numThreads) {
			this.myColor = myColor;
			this.valuationMode = valuationMode;
			this.fen = fen;
			this.depth = depth;
			this.myThreadIdx = myThreadIdx;
			this.numThreads = numThreads;
			
			if (valuationMode ==  ValuationMode.INTERMEDIATE_STATES_DECAY_90) {
				decayFactor = 0.9;
			}
		}

		@Override
		public MoveAndValue call() throws Exception {
			board = new Board(fen);

			board.setLogging(false);
			board.validateMoves(false);

			MoveAndValue bestMove = null;
			
			List<Move> moves = board.getAvailableMoves();
			
			for (int i = 0; i < moves.size(); ++i) {
    			if (i % numThreads != myThreadIdx) {
    				continue;
    			}
    			
	    		Move move = moves.get(i);
	    		//System.out.println("Evaluating " + move);
	    		board.move(move);

	    		double value = getBoardValue(depth - 1, decayFactor);
	    		
				if (bestMove == null || value > bestMove.value) {
					bestMove = new MoveAndValue(move, value);
					//System.out.println("Best move in thread so far for move " + bestMove);
				}
				
	    		board.undoLastMove();
    		}
	    
			//System.out.println("Best move in thread: " + bestMove);
			return bestMove;
		}
		

		private double getEndstateValueOrCurrentPieceValueDelta() {
			if (board.getResult() != null) {
				if (board.getResult().getOutcome() == GameOutcome.CHECKMATE) {
					if (board.getResult().getWinner() == myColor) {
						//System.out.println("Returning checkmate for me in leaf state " + FENNotation.toString(board));
						return 10_000;
					} else {
						//System.out.println("Returning checkmate for opponent in leaf state " + FENNotation.toString(board));
						return -10_000;
					}
				} else {
					//System.out.println("Returning draw in leaf state " + FENNotation.toString(board));
					
					return 0;
				}
			}
			
			return board.getPieceValue(myColor) - board.getPieceValue(myColor.getOpposite());
		}
		
		private double getBoardValue(int depth, double decayValue) throws Exception {
			
			if (board.getResult() != null || depth == 0) {
				return decayValue * getEndstateValueOrCurrentPieceValueDelta();
			}
			
			List<Move> moves = board.getAvailableMoves();
			
			double maxValue = Double.MIN_VALUE;
			
			if (valuationMode == ValuationMode.INTERMEDIATE_STATES_DECAY_90) {
				maxValue = decayValue * getEndstateValueOrCurrentPieceValueDelta();
			}
			
			for (int i = 0; i < moves.size(); ++i) {
	    		Move move = moves.get(i);
	    		board.move(move);
	    		double value = getBoardValue(depth - 1, decayValue * decayFactor);
	    		if (value > maxValue) {
	    			maxValue = value;
	    			//System.out.println("New best move " + move + "( " + value + ") in state " + FENNotation.toString(board));
	    		}
	    		board.undoLastMove();
	    	}
	    	
	    	return maxValue;
		}
		
	}


}
