package martin.chess.engine;

public class Algebraic {

	public static String toAlgebraic(int idx) {
		return String.format("%c%c", 'a' + (idx % 8), '1' + (idx / 8));
	}
	
	public static int fromAlgebraic(String str) {
		return fromAlgebraic(str, 0);
	}
	
	public static int fromAlgebraic(String str, int offset) {
		char algFile = str.charAt(offset);
		char algRank = str.charAt(offset + 1);
		
		int rank = algRank - '1';
		int file = algFile - 'a';
	
		return rank * 8 + file;
	}
}
