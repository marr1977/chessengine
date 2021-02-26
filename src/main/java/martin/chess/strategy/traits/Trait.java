package martin.chess.strategy.traits;

import martin.chess.engine.Board;
import martin.chess.engine.Color;
import martin.chess.engine.Move;

public interface Trait {

	default void initialize(Board boardBefore) {}
	
	public double vote(Color ourColor, Board boardBefore, Board boardAfter, Move m);

	default public String getName() {
		String name = getClass().getSimpleName();
		return name.endsWith("Trait") ? name.substring(0, name.length() - 5) : name;
	}
}
