package martin.chess.strategy;

import martin.chess.engine.Board;
import martin.chess.engine.Move;

public class AsyncStrategy implements IPlayerStrategy {

	Move move;
	
	@Override
	public synchronized Move getMove(Board board) throws InterruptedException {
		while (move == null) {
			wait();
		}
		return move;
	}
	
	public synchronized void setMove(Move move) {
		this.move = move;
		notifyAll();
	}
}
