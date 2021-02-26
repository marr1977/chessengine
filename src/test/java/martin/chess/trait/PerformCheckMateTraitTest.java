package martin.chess.trait;

import org.junit.Assert;
import org.junit.Test;

import martin.chess.engine.Board;
import martin.chess.engine.Move;
import martin.chess.strategy.traits.PerformCheckMateTrait;

public class PerformCheckMateTraitTest {

	PerformCheckMateTrait trait = new PerformCheckMateTrait();

	@Test
	public void testWhenCheckmateIsPossible() {
		Board boardBefore = new Board("3rkn2/3pp3/8/8/8/PQ4PP/5R2/2K5 w - - 0 1");
		
		Board boardAfter = new Board(boardBefore);
		boardAfter.validateMoves(false);
		
		Move checkMateMove = new Move("b3f7");
		
		for (Move move : boardBefore.getAvailableMoves()) {
			double expectedValue = move.equals(checkMateMove) ? 10000 : 0;

			boardAfter.move(move);
			
			Assert.assertEquals(move.toString(), expectedValue, trait.vote(boardBefore.getColorToMove(), boardBefore, boardAfter, move), 0.00001);
			boardAfter.undoLastMove();
		}
	}

}
