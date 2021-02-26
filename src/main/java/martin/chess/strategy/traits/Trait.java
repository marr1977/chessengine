package martin.chess.strategy.traits;

import java.util.concurrent.ExecutorService;

import martin.chess.engine.Board;
import martin.chess.engine.Color;
import martin.chess.engine.Move;

public abstract class Trait {
	
	protected ExecutorService executorService;
	
	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	public void initialize(Board boardBefore) {
	}
	
	public abstract double vote(Color ourColor, Board boardBefore, Board boardAfter, Move m);

	@Override
	public String toString() {
		String name = getClass().getSimpleName();
		return name.endsWith("Trait") ? name.substring(0, name.length() - 5) : name;
	}
}
