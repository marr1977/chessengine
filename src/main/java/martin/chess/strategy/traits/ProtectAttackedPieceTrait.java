package martin.chess.strategy.traits;

import java.util.HashSet;
import java.util.Set;

import martin.chess.engine.Board;
import martin.chess.engine.Color;
import martin.chess.engine.Move;

/**
 * Prefers a move that would protect our own undefended piece
 */
public class ProtectAttackedPieceTrait extends Trait {

	private Set<Integer> attackedIndexes;
	
	@Override
	public void initialize(Board boardBefore) {
		Board b = new Board(boardBefore);
		b.setColorToMove(b.getColorToMove().getOpposite());
		
		attackedIndexes = new HashSet<>();
		for (var move : b.getAvailableMoves()) {
			attackedIndexes.add(move.getIdxTo());
		}
	}

	@Override
	public double vote(Color ourColor, Board boardBefore, Board boardAfter, Move m) {
		return getProtectedValue(ourColor, boardAfter) - getProtectedValue(ourColor, boardBefore);
	}

	/**
	 * The piece value sum of all our pieces that are protected 
	 */
	private double getProtectedValue(Color ourColor, Board board) {
		
		//double protectedValue = 0;
		
		//List<Move> opponentMoves = board.getAvailableMoves(ourColor.getOpposite());
		
		return 0;
	}

}
