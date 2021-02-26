package martin.chess.strategy.traits;

import martin.chess.engine.Board;
import martin.chess.engine.Color;
import martin.chess.engine.Move;

/**
 * If a piece is exposed, try to remove it 
 */
public class AvoidPieceExposureTrait implements Trait {
	
	@Override
	public double vote(Color ourColor, Board boardBefore, Board boardAfter, Move m) {
		return 0;
	}
}
