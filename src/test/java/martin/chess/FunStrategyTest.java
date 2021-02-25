package martin.chess;

import java.util.function.Supplier;

import org.junit.Test;

import martin.chess.engine.Color;
import martin.chess.engine.GameResultData;
import martin.chess.strategy.IPlayerStrategy;
import martin.chess.strategy.PieceValueStrategy;
import martin.chess.strategy.PieceValueStrategy.ValuationMode;
import martin.chess.strategy.RandomStrategy;

public class FunStrategyTest {

	
	@Test
	public void randomVsRandom() {
		testStrategies(10000, () -> new RandomStrategy(), () -> new RandomStrategy());
	}
	
	@Test
	public void randomVsPieceValueEndStateOnlyDepth3() {
		testStrategies(10000, () -> new RandomStrategy(), () -> new PieceValueStrategy(ValuationMode.END_STATE_ONLY, 3, 2));
	}
	
	@Test
	public void PieceValueVsPieceValueDepth4Vs3() {
		testStrategies(10000, () -> new PieceValueStrategy(ValuationMode.INTERMEDIATE_STATES_DECAY_90, 3, 3), () -> new PieceValueStrategy(ValuationMode.INTERMEDIATE_STATES_DECAY_90, 4, 3));
	}
	
	@Test
	public void randomVsPieceValueIntermediateDepth3() {
		testStrategies(10000, () -> new RandomStrategy(), () -> new PieceValueStrategy(ValuationMode.INTERMEDIATE_STATES_DECAY_90, 4, 6));
	}

	private void testStrategies(int numGames, Supplier<IPlayerStrategy> whitePlayerSupplier, Supplier<IPlayerStrategy> blackPlayerSupplier) {
		
		int whiteWon = 0;
		int blackWon = 0;
		int stalemates = 0;
		int insufficientMaterial = 0;
		int threefoldRepetitions = 0;
		int fiftyMoveRule = 0;
		
		for (int i = 0; i < numGames; i++) {
			IPlayerStrategy whitePlayer = whitePlayerSupplier.get();
			IPlayerStrategy blackPlayer = blackPlayerSupplier.get();
			
			GameManager mgr = new GameManager(whitePlayer, blackPlayer);
			mgr.setLogging(false);
			GameResultData result = mgr.startGame();
			
			switch (result.getOutcome()) {
			case CHECKMATE:
				if (result.getWinner() == Color.WHITE) {
					whiteWon++;
				} else if (result.getWinner() == Color.BLACK) {
					blackWon++;
				} 
				break;
			case DRAW_INSUFFICIENT_MATERIAL:
				insufficientMaterial++;
				break;
			case STALEMATE:
				stalemates++;
				break;
			case DRAW_THREEFOLD_REPETITION:
				threefoldRepetitions++;
				break;
			case DRAW_FIFTY_MOVE_RULE:
				fiftyMoveRule++;
				break;
			default:
				break;
			}
			System.out.println("Game done after " + result.getNumberOfMoves() + " moves");
		}
		
		System.out.println(" ============================== ");
		System.out.println(String.format("White wins: %d, black wins: %d, insufficient material: %d, stale mate: %d, 3-fold repetition: %d, fifty-move rule: %d", 
			whiteWon, blackWon, insufficientMaterial, stalemates, threefoldRepetitions, fiftyMoveRule));
		System.out.println(" ============================== ");
		
	}
}
