package martin.chess.ui;

public enum PlayerType {
	Human("Human"),
	RandomRobby("Random Robby"),
	Trait1("Balanced trait strategy");
	
	private String name;

	private PlayerType(String name) {
		this.name = name;
	}
	@Override
	public String toString() {
		return name;
	}
}
