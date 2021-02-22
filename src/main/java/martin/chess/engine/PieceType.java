package martin.chess.engine;

public enum PieceType {
	PAWN('p', 1),
	ROOK('r', 5),
	KNIGHT('n', 3),
	BISHOP('b', 3),
	KING('k', 100),
	QUEEN('q', 9);
	
	private char shortName;
	private int value;

	private PieceType(char shortName, int value) {
		this.shortName = shortName;
		this.value = value;
	}
	
	public char getShortName(Color color) {
		return color == Color.WHITE ? Character.toUpperCase(shortName) : shortName;
	}

	public static PieceType fromShortName(char shortName) {
		for (var val : values()) {
			if (val.shortName == shortName) {
				return val;
			}
		}
			
		throw new IllegalArgumentException("No such piece: " + shortName);
	}

	public int getValue() {
		return value;
	}
}
