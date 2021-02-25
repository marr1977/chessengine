package martin.chess;

import martin.chess.engine.Board;
import martin.chess.engine.Color;
import martin.chess.engine.GameResultData;
import martin.chess.engine.Move;
import martin.chess.strategy.IPlayerStrategy;

public class GameManager {

	private Board board;
	private IPlayerStrategy whitePlayer;
	private IPlayerStrategy blackPlayer;
	private boolean logging;
	
	public GameManager(IPlayerStrategy whitePlayer, IPlayerStrategy blackPlayer) {
		this.whitePlayer = whitePlayer;
		this.blackPlayer = blackPlayer;
	}
	
	public GameResultData startGame() {
		board = new Board();
		board.setLogging(logging);
		
		while (board.getResult() == null) {
			IPlayerStrategy toMove = board.getColorToMove() == Color.WHITE ? whitePlayer : blackPlayer;
			
			Move move = null;
			try {
				move = toMove.getMove(board);
			} catch (InterruptedException e) {
				System.out.println("Move was interrupted, aborting game");
				return null;
			}
			
			board.move(move);
		}
		
		if (board.getResult().getWinner() != null) {
			System.out.println(board.getResult().getWinner() + " won");
		} else {
			System.out.println("Draw: " + board.getResult());
		}
		
		return board.getResult();
	}

	public void setLogging(boolean logging) {
		this.logging = logging;
	}
	
	
}
