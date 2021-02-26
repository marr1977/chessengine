package martin.chess.strategy.traits;

import martin.chess.engine.Board;
import martin.chess.engine.Color;
import martin.chess.engine.Move;

/**
 * Prefers a move that will avoid check mate 
 */
public class AvoidCheckMateTrait implements Trait {

	@Override
	public double vote(Color ourColor, Board boardBefore, Board boardAfter, Move m) {
		if (boardAfter.getResult() != null && boardAfter.getResult().getWinner() != null) {
			if (boardAfter.getResult().getWinner() != ourColor) {
				return -10000;
			}
		}
		return 0;
	}
}
