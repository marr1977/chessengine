package martin.chess.engine;

public class GameResultData {
	private Color winner;
	private GameOutcome outcome;
	private int numberOfMoves;
	
	GameResultData(Color winner, GameOutcome outcome, int numberOfMoves) {
		this.winner = winner;
		this.outcome = outcome;
		this.numberOfMoves = numberOfMoves;
	}
	
	public Color getWinner() {
		return winner;
	}
	public GameOutcome getOutcome() {
		return outcome;
	}
	public int getNumberOfMoves() {
		return numberOfMoves;
	}
}
