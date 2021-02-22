package martin.chess.engine;

public class Piece {

	PieceType type;
	Color color;
	
	public Piece(PieceType type, Color color) {
		this.type = type;
		this.color = color;
	}
	
	@Override
	public String toString() {
		return String.valueOf(type.getShortName(color));
	}
	
	public Color getColor() {
		return color;
	}
	
	public PieceType getType() {
		return type;
	}

}
