package martin.chess.trait;

import org.junit.Assert;
import org.junit.Test;

import martin.chess.engine.Board;
import martin.chess.engine.Move;
import martin.chess.strategy.traits.CapturePieceTrait;

public class CapturePieceTraitTest {

	CapturePieceTrait trait = new CapturePieceTrait();
	
	@Test
	public void test() {
		Board boardBefore = new Board("3rkn2/p2pp3/8/8/2p1n3/PQ4PP/5R2/2K5 b - - 0 1");
		
		Board boardAfter = new Board(boardBefore);
		boardAfter.validateMoves(false);
		
		for (Move move : boardBefore.getAvailableMoves()) {
			double expectedValue = 0;
			
			switch (move.toString()) {
			case "c4b3": expectedValue = 9; break;
			case "e4g3": expectedValue = 1; break;
			case "e4f2": expectedValue = 5; break;
			}

			boardAfter.move(move);
			
			Assert.assertEquals(move.toString(), expectedValue, trait.vote(boardBefore.getColorToMove(), boardBefore, boardAfter, move), 0.00001);
			boardAfter.undoLastMove();
		}
	}
}
