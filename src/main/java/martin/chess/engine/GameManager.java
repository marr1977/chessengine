package martin.chess.engine;

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
	
	public ResultData startGame() {
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
		
		if (board.getWinner() != null) {
			System.out.println(board.getWinner() + " won");
		} else {
			System.out.println("Draw: " + board.getResult());
		}
		
		return new ResultData(board.getWinner(), board.getResult(), board.getNumberOfMoves());
	}

	public void setLogging(boolean logging) {
		this.logging = logging;
	}
	
	public static class ResultData {
		private Color winner;
		private GameResult result;
		private int numberOfMoves;
		
		private ResultData(Color winner, GameResult result, int numberOfMoves) {
			this.winner = winner;
			this.result = result;
			this.numberOfMoves = numberOfMoves;
		}
		
		public Color getWinner() {
			return winner;
		}
		public GameResult getResult() {
			return result;
		}
		public int getNumberOfMoves() {
			return numberOfMoves;
		}
	}
	
}
