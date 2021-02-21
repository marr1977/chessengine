package martin.chess.engine;

public class Move {
	int idxFrom;
	int idxTo;
	
	// Contains the rook data in a castleing move
	int additionalIdxFrom = -1;
	int additionalIdxTo = -1;
	
	PieceType queeningPiece;

	public Move(int idxFrom, int idxTo) {
		this.idxFrom = idxFrom;
		this.idxTo = idxTo;
	}
	
	public Move(int idxFrom, int idxTo, PieceType queeningPiece) {
		this(idxFrom, idxTo);
		this.queeningPiece = queeningPiece;
	}
	
	public Move(String string) {
		idxFrom = Algebraic.fromAlgebraic(string);
		idxTo = Algebraic.fromAlgebraic(string, 2);
		
		if (string.length() > 4) {
			queeningPiece = PieceType.fromShortName(string.charAt(4));
		}
	}

	@Override
	public String toString() {
		return Algebraic.toAlgebraic(idxFrom) + Algebraic.toAlgebraic(idxTo) + (queeningPiece == null ? "" : queeningPiece.getShortName(Color.BLACK));
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Move)) {
			return false;
		}
		
		Move other = (Move) obj;
		return 
				other.idxFrom == idxFrom && other.idxTo == idxTo && 
				other.additionalIdxFrom == additionalIdxFrom && other.additionalIdxTo == additionalIdxTo;
	}
	
	@Override
	public int hashCode() {
		return idxFrom + idxTo;
	}
	
	public int getIdxFrom() {
		return idxFrom;
	}
	
	public int getIdxTo() {
		return idxTo;
	}
}
