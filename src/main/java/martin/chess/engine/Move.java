package martin.chess.engine;

public class Move {
	int idxFrom;
	int idxTo;
	int takenPieceIdx = -1;
	
	// Contains the rook data in a castleing move
	int additionalIdxFrom = -1;
	int additionalIdxTo = -1;
	
	PieceType queeningPiece;

	public Move(int idxFrom, int idxTo) {
		this.idxFrom = idxFrom;
		this.idxTo = idxTo;
	}
	
	public Move(int idxFrom, int idxTo, int takenPieceIdx) {
		this(idxFrom, idxTo);
		this.takenPieceIdx = takenPieceIdx;
	}
	
	public Move(int idxFrom, int idxTo, int takenPieceIdx, PieceType queeningPiece) {
		this(idxFrom, idxTo, takenPieceIdx);
		this.queeningPiece = queeningPiece;
	}
	
	public Move(String string) {
		idxFrom = Algebraic.fromAlgebraic(string);
		idxTo = Algebraic.fromAlgebraic(string, 2);
	}

	@Override
	public String toString() {
//		String val = Algebraic.toAlgebraic(idxFrom) + " => " + Algebraic.toAlgebraic(idxTo);
//		
//		if (takenPieceIdx != -1) {
//			val += " (take on " + Algebraic.toAlgebraic(takenPieceIdx) + ")";
//		}
		//return val;
		return Algebraic.toAlgebraic(idxFrom) + Algebraic.toAlgebraic(idxTo) + (queeningPiece == null ? "" : queeningPiece.getShortName(Color.BLACK));
	}
}
