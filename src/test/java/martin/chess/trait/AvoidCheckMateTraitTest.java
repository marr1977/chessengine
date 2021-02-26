package martin.chess.trait;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import martin.chess.engine.Board;
import martin.chess.engine.Move;
import martin.chess.strategy.traits.AvoidCheckMateTrait;

public class AvoidCheckMateTraitTest {

	AvoidCheckMateTrait trait = new AvoidCheckMateTrait();
	
	@Test
	public void testWhenCheckmateIsPossible() {
		Board boardBefore = new Board("2nrkn2/p2pp3/1p6/8/8/PQ4PP/5R2/2K5 b - - 0 1");
		
		Board boardAfter = new Board(boardBefore);
		boardAfter.validateMoves(false);
		
		Set<Move> movesToAvoidCheckMate = Set.of(
			new Move("c8d6"), // Knight can capture queen
			new Move("f8e6"), // Knight can block queen
			new Move("e7e6"), // Pawn can block queen
			new Move("d7d6"), // By moving pawn, king can escape
			new Move("d7d5")  // Pawn can block queen 
		);
		
		trait.initialize(boardBefore);
	
		for (Move move : boardBefore.getAvailableMoves()) {
			double expectedValue = movesToAvoidCheckMate.contains(move) ? 5000 : -5000;

			boardAfter.move(move);
			
			Assert.assertEquals(move.toString(), expectedValue, trait.vote(boardBefore.getColorToMove(), boardBefore, boardAfter, move), 0.00001);
			boardAfter.undoLastMove();
		}
	}
	
	@Test
	public void testWhenCheckmateIsNotPossible() {
		Board boardBefore = new Board();
		
		Board boardAfter = new Board(boardBefore);
		boardAfter.validateMoves(false);

		trait.initialize(boardBefore);
	
		for (Move move : boardBefore.getAvailableMoves()) {
			boardAfter.move(move);
			
			Assert.assertEquals(move.toString(), 0, trait.vote(boardBefore.getColorToMove(), boardBefore, boardAfter, move), 0.00001);
			boardAfter.undoLastMove();
		}
	}

}
