package martin.chess.strategy.traits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import martin.chess.engine.Board;
import martin.chess.engine.Color;
import martin.chess.engine.Move;

/**
 * Checks if there is a move
 */
public class AvoidCheckMateTrait extends Trait {

	private Map<Move, Double> moveValues;
	
	@Override
	public void initialize(Board board) {
		Color ourColor = board.getColorToMove();
		
		Set<Move> movesInWhichCheckMateArePossible = new HashSet<>();
		
		if (executorService == null) {
			movesInWhichCheckMateArePossible = new Runner(board, ourColor, 0, 1).call();
		} else {
			
			final int numThreads = 6;
			
			List<Future<Set<Move>>> futures = new ArrayList<>();
			
			for (int i = 0; i < numThreads; ++i) {
				Runner runner = new Runner(board, ourColor, i, numThreads);
				futures.add(executorService.submit(runner));
			}
			
			for (var future : futures) {
				try {
					movesInWhichCheckMateArePossible.addAll(future.get());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}

		if (movesInWhichCheckMateArePossible.isEmpty()) {
			moveValues = null;
		} else {
			moveValues = new HashMap<>();
			for (var move : board.getAvailableMoves()) {
				moveValues.put(move, movesInWhichCheckMateArePossible.contains(move) ? -500d : 500d);
			}
		}
		
	}
	private static class Runner implements Callable<Set<Move>> {

		private Board board;
		private Color ourColor;
		private int myIdx;
		private int totalNumRunners;
		
		public Runner(Board board, Color ourColor, int myIdx, int totalNumRunners) {
			this.board = board;
			this.ourColor = ourColor;
			this.myIdx = myIdx;
			this.totalNumRunners = totalNumRunners;
			
		}
		
		@Override
		public Set<Move> call() {
			Set<Move> movesInWhichCheckMateArePossible = new HashSet<>();
			Board b = new Board(board);
			b.validateMoves(false);
			b.setLogging(false);
			
			List<Move> moves = b.getAvailableMoves();
			
			for (int i = 0; i < moves.size(); ++i) {
				if (i % totalNumRunners != myIdx) {
					continue;
				}
				
				var myMove = moves.get(i);
				b.move(myMove);
				
				if (b.getResult() == null) {
					for (var theirMove : b.getAvailableMoves()) {
						b.move(theirMove);
	
						if (b.getResult() != null && 
							b.getResult().getWinner() != null && 
							b.getResult().getWinner() != ourColor) {
							
							movesInWhichCheckMateArePossible.add(myMove);
						}
						
						b.undoLastMove();
					}
				}
				
				b.undoLastMove();
			}
			
			
			return movesInWhichCheckMateArePossible;
		}
		
	}
	
	@Override
	public double vote(Color ourColor, Board boardBefore, Board boardAfter, Move m) {
		return moveValues == null ? 0 : moveValues.get(m);
	}
}
