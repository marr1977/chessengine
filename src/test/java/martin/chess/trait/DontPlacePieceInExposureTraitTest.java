package martin.chess.trait;

import org.junit.Assert;
import org.junit.Test;

import martin.chess.engine.Board;
import martin.chess.engine.Move;
import martin.chess.strategy.traits.DontPlacePieceInExposureTrait;

public class DontPlacePieceInExposureTraitTest {

	DontPlacePieceInExposureTrait trait = new DontPlacePieceInExposureTrait();
	
	@Test
	public void test() {
		Board boardBefore = new Board("3rkn2/p2pp3/8/8/2p1n3/P5PP/1Q2R3/2K5 w - - 0 1");
		
		Board boardAfter = new Board(boardBefore);
		boardAfter.validateMoves(false);
		
		for (Move move : boardBefore.getAvailableMoves()) {
			double expectedValue = 0;
			
			switch (move.toString()) {
			case "b2b3":
			case "b2c3":
			case "b2f6":
			case "b2d2":
			case "b2b6":
			case "b2b8":
				expectedValue = -9; 
				break;
			case "e2d2":
			case "e2f2": 
				expectedValue = -5; break;
			}

			boardAfter.move(move);
			
			Assert.assertEquals(move.toString(), expectedValue, trait.vote(boardBefore.getColorToMove(), boardBefore, boardAfter, move), 0.00001);
			boardAfter.undoLastMove();
		}
		
	}
	
}

