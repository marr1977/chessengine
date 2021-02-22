package martin.chess.engine.state;

/**
 * Represents a player's castling ability
 */
public class CastlingAbility {
	public CastlingAbility() {
	}
	
	public CastlingAbility(CastlingAbility from) {
		canCastleKingSide = from.canCastleKingSide;
		canCastleQueenSide = from.canCastleQueenSide;
	}
	
	public boolean canCastleKingSide;
	public boolean canCastleQueenSide;
}
