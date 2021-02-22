package martin.chess;

import org.junit.Test;

import martin.chess.engine.Color;
import martin.chess.engine.GameManager;
import martin.chess.engine.GameManager.ResultData;
import martin.chess.strategy.IPlayerStrategy;
import martin.chess.strategy.RandomStrategy;

public class FunStrategyTest {

	
	@Test
	public void randomVsRandom() {
		
		int whiteWon = 0;
		int blackWon = 0;
		int stalemates = 0;
		int insufficientMaterial = 0;

		int numGames = 100;
		
		for (int i = 0; i < numGames; i++) {
			IPlayerStrategy whitePlayer = new RandomStrategy();
			IPlayerStrategy blackPlayer = new RandomStrategy();
			
			GameManager mgr = new GameManager(whitePlayer, blackPlayer);
			mgr.setLogging(false);
			ResultData result = mgr.startGame();
			
			switch (result.getResult()) {
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
			}
			System.out.println("Game done after " + result.getNumberOfMoves() + " moves");
		}
		
		System.out.println(" ============================== ");
		System.out.println(String.format("White wins: %d, black wins: %d, insufficient material: %d, stale mate: %d", whiteWon, blackWon, insufficientMaterial, stalemates));
		System.out.println(" ============================== ");
		
	}
}
