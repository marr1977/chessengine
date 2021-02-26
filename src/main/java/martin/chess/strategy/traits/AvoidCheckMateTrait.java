package martin.chess.strategy.traits;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import martin.chess.engine.Board;
import martin.chess.engine.Color;
import martin.chess.engine.Move;

/**
 * Checks if there is a move
 */
public class AvoidCheckMateTrait implements Trait {

	private Map<Move, Double> moveValues;
	
	@Override
	public void initialize(Board board) {
		Color ourColor = board.getColorToMove();
		
		Set<Move> movesInWhichCheckMateArePossible = new HashSet<>();
		
		Board b = new Board(board);
		b.validateMoves(false);
		b.setLogging(false);
		
		for (var myMove : b.getAvailableMoves()) {
			
			b.move(myMove);
			
			for (var theirMove : b.getAvailableMoves()) {
				b.move(theirMove);

				if (b.getResult() != null && 
					b.getResult().getWinner() != null && 
					b.getResult().getWinner() != ourColor) {
					
					movesInWhichCheckMateArePossible.add(myMove);
				}
				
				b.undoLastMove();
			}
			
			b.undoLastMove();
		}
		
		if (movesInWhichCheckMateArePossible.isEmpty()) {
			moveValues = null;
		} else {
			moveValues = new HashMap<>();
			for (var move : board.getAvailableMoves()) {
				moveValues.put(move, movesInWhichCheckMateArePossible.contains(move) ? -5000d : 5000d);
			}
		}
		
	}
	
	@Override
	public double vote(Color ourColor, Board boardBefore, Board boardAfter, Move m) {
		return moveValues == null ? 0 : moveValues.get(m);
	}
}
