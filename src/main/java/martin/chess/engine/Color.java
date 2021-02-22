package martin.chess.engine;

public enum Color {
	WHITE,
	BLACK;
	
	public Color getOpposite() {
		return this == BLACK ? WHITE : BLACK;
	}
}
