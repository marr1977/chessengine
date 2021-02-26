package martin.chess.trait;

import org.junit.Assert;
import org.junit.Test;

import martin.chess.engine.Board;
import martin.chess.engine.Move;
import martin.chess.strategy.traits.AvoidPieceExposureTrait;

public class AvoidPieceExposureTraitTest {
	AvoidPieceExposureTrait trait = new AvoidPieceExposureTrait();
	
	
	@Test
	public void test() {
		Board boardBefore = new Board("3rkn2/p2pp3/8/8/1pp1n3/PQP2pPP/1BP1BR2/2K2R2 w - - 0 1");
		
		Board boardAfter = new Board(boardBefore);
		boardAfter.validateMoves(false);
		boardAfter.setLogging(false);
		
		trait.initialize(boardBefore);
		System.out.println(boardBefore.getAvailableMoves());
		for (Move move : boardBefore.getAvailableMoves()) {
			double expectedValue = 0;
			
			switch (move.toString()) {
			case "a3b4": 
			case "a3a4":
			case "c3b4":
			case "g3g4":
				expectedValue = 1; 
				break;
			case "b3b2": 
			case "b3c4":
			case "b3a4":
			case "b3b4":
			case "b3a2":
				expectedValue = 9; 
				break;
			case "f2h2":
			case "f2g2":
			case "f2f3": 
				expectedValue = 5; 
				break;
			case "e2f3":
			case "e2d3": 
			case "e2c4":
			case "e2d1":
				expectedValue = 3; 
				break;
			}

			boardAfter.move(move);
			
			Assert.assertEquals(move.toString(), expectedValue, trait.vote(boardBefore.getColorToMove(), boardBefore, boardAfter, move), 0.00001);
			boardAfter.undoLastMove();
		}
	}
}
