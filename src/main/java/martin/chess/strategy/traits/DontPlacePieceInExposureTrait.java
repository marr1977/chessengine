package martin.chess.strategy.traits;

import martin.chess.engine.Board;
import martin.chess.engine.Color;
import martin.chess.engine.Move;
import martin.chess.engine.Piece;

/**
 * Avoids moves that would expose a piece to an enemy attacker
 */
public class DontPlacePieceInExposureTrait extends Trait {

	@Override
	public double vote(Color ourColor, Board boardBefore, Board boardAfter, Move m) {
		
		// See if any available moves are directed towards the square my piece moved to
		Piece piece = boardBefore.pieceAt(m.getIdxFrom());
		
		long piecesAttackingMyPiece = 0;
		for (var move : boardAfter.getAvailableMoves()) {
			if (move.getIdxTo() == m.getIdxTo()) {
				//System.out.println(move + " would attack my " + piece.toString() + " move " + m);
				piecesAttackingMyPiece++;
			}
		}
		
		if (piecesAttackingMyPiece == 0) {
			return 0;
		}
		
		return -piece.getType().getValue();
	}

}
