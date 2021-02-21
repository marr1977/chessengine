package martin.chess.engine;

public enum PieceType {
	PAWN('p'),
	ROOK('r'),
	KNIGHT('n'),
	BISHOP('b'),
	KING('k'),
	QUEEN('q');
	
	private char shortName;

	private PieceType(char shortName) {
		this.shortName = shortName;
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
}
