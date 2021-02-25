package martin.chess.ui;

public enum PlayerType {
	Human("Human"),
	RandomRobby("Random Robby");
	
	private String name;

	private PlayerType(String name) {
		this.name = name;
	}
	@Override
	public String toString() {
		return name;
	}
}
