package martin.chess.strategy.traits;

import java.util.HashSet;
import java.util.Set;

import martin.chess.engine.Board;
import martin.chess.engine.Color;
import martin.chess.engine.Move;
import martin.chess.engine.Piece;

/**
 * If a piece is exposed, try to remove it 
 */
public class AvoidPieceExposureTrait extends Trait {
	
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
		if (attackedIndexes.contains(m.getIdxFrom()) && !attackedIndexes.contains(m.getIdxTo())) {
			Piece piece = boardBefore.pieceAt(m.getIdxFrom());
			return piece.getType().getValue();
		}
		
		return 0;
	}
}
