package martin.chess;

import org.junit.Test;

import martin.chess.engine.Board;
import martin.chess.engine.Color;
import martin.chess.engine.Move;
import martin.chess.strategy.PieceValueStrategy;
import martin.chess.strategy.PieceValueStrategy.ValuationMode;

public class PieceValueStrategyTest {

	
	@Test
	public void test1() throws InterruptedException {
		Board b = new Board("7k/4pp2/8/8/1p6/8/2P1PP2/2K5 w - - 0 1");
		
		PieceValueStrategy strat = new PieceValueStrategy(ValuationMode.END_STATE_ONLY, 3, 1);
		
		Move move = strat.getMove(b);
		
		System.out.println(move);
	}
	
	@Test
	public void test2() throws InterruptedException {
		Board b = new Board("7k/8/8/3B4/8/1P4Q1/P1PKP3/8 w - - 0 1");
		
		PieceValueStrategy strat = new PieceValueStrategy(ValuationMode.END_STATE_ONLY, 3, 1);
		
		Move move = strat.getMove(b);
		
		System.out.println(move);
	}
	
	
}
