package martin.chess.strategy.traits;

import martin.chess.engine.Board;
import martin.chess.engine.Color;
import martin.chess.engine.Move;

/**
 * Prefers a move that will capture a piece, the higher value the better
 */
public class CapturePieceTrait extends Trait {

	@Override
	public double vote(Color ourColor, Board boardBefore, Board boardAfter, Move m) {
		return boardBefore.getPieceValue(ourColor.getOpposite()) - boardAfter.getPieceValue(ourColor.getOpposite());
	}

}
