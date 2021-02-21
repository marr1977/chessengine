package martin.chess;

import java.util.List;
import java.util.Random;

import martin.chess.engine.Board;
import martin.chess.engine.FENNotation;
import martin.chess.engine.Move;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
//    	Board b = new Board();
//    	System.out.println(b);
//    	b = new Board("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1");
//    	System.out.println(b);
//    	b = new Board("rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq c6 0 2");
//    	System.out.println(b);
//    	b = new Board("rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2");
//    	System.out.println(b);
    	
    	//printMoves("///8/8///N/ w KQkq e3 0 1");
    	
    	//printMoves("///8/8/P//2Q/ w KQkq e3 0 1");
    	//printMoves("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1");
    	
    	
    	//playGame();
    	
    	Board board = new Board("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8");
    	board.setLogging(false);
//    	
//    	board.move(new Move("c1h6"));
//    	System.out.println(getNumberOfPositions(board, 1, true));
//    	System.out.println(FENNotation.toString(board));
//    	
//    	System.out.println();
//    	
    	
    	long start = System.currentTimeMillis();
    	int depth = getNumberOfPositions(board, 4, true);
    	long end = System.currentTimeMillis();
    	
    	System.out.println("#pos: " + depth + " (" + (end - start) + " ms)");
    }
    
    private static int getNumberOfPositions(Board b, int depth, boolean log) {
    	if (depth == 0) {
    		return 1;
    	}
    	
    	int numPositions = 0;
    	
    	for (Move move : b.getAvailableMoves()) {
    		b.move(move);
    		int positions = getNumberOfPositions(b, depth - 1, false);
    		if (log) {
    			System.out.println(move + ": " + positions);
    		}
    		numPositions += positions;
    		b.undoLastMove();
    	}
    	
    	return numPositions;
    	
	}

	private static void playGame() {
    	Random r = new Random(1927);
    	String s = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    	//String s = "n3r3/1Nk5/6pp/PP3pP1/3b1P1P/8/8/2B3qK w - - 0 58";
    	Board b = new Board(s);
    	Move move = null;
    	
    	while (true) {
    		if (b.getResult() != null) {
    			break;
    		}
    		System.out.println(b);
    		System.out.println("Last move: " + move);
    		List<Move> moves = b.getAvailableMoves();
    		System.out.println(moves);
    		if (moves.size() == 0) {
    			break;
    		}
    		move = moves.get(r.nextInt(moves.size()));
    		b.move(move);
    	}
    	
    	System.out.println("End state:" + FENNotation.toString(b));
    	System.out.println("End board:" + b);
    	System.out.println("Result:" + b.getResult());
    	System.out.println("Winner:" + b.getWinner());
    	System.out.println("Number of moves:" + b.getNumberOfMoves());
    }

	private static void printMoves(String string) {
		Board b = new Board(string);
		System.out.println(b);
		System.out.println(b.getAvailableMoves());
	}
}
