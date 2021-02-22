package martin.chess.strategy;

import java.util.List;
import java.util.Random;

import martin.chess.engine.Board;
import martin.chess.engine.Move;

public class RandomStrategy implements IPlayerStrategy {

	private Random random;

	public RandomStrategy() {
		this(System.currentTimeMillis());
	}
	
	public RandomStrategy(long seed) {
		random = new Random(seed);
	}

	@Override
	public Move getMove(Board board) {
		List<Move> moves = board.getAvailableMoves();
		return moves.get(random.nextInt(moves.size()));
	}
}
