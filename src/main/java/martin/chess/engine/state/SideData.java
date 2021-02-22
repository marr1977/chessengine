package martin.chess.engine.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import martin.chess.engine.Algebraic;

/**
 * Represents data specific for each side
 */
public class SideData {
	public boolean inCheck;
	public int kingIdx;
	public List<Set<Integer>> attackingPathsToMyKing;
	public Map<Integer, Set<Integer>> squaresAttackedByMe;
	public CastlingAbility castling;
	public int pieceValue;
	
	SideData() {
		clear();
		castling = new CastlingAbility();
	}
	
	SideData(SideData from) {
		inCheck = from.inCheck;
		attackingPathsToMyKing = new ArrayList<>(from.attackingPathsToMyKing);
		squaresAttackedByMe = new HashMap<>(from.squaresAttackedByMe);
		castling = new CastlingAbility(from.castling);
		kingIdx = from.kingIdx;
		pieceValue = from.pieceValue;
	}
	
	public void clear() {
		attackingPathsToMyKing = new ArrayList<>(24);
		squaresAttackedByMe = new HashMap<>(128);
		inCheck = false;
	}

	public String getAttackedSquares() {
		return squaresAttackedByMe.entrySet().stream().map(i -> Algebraic.toAlgebraic(i.getKey())).collect(Collectors.joining(","));
	}
}
