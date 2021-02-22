package martin.chess.strategy;

import martin.chess.engine.Board;
import martin.chess.engine.Move;

public interface IPlayerStrategy {
	Move getMove(Board board) throws InterruptedException;
}
