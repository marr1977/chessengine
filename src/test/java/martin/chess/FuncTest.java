package martin.chess;

import org.junit.Assert;
import org.junit.Test;

import martin.chess.engine.Board;
import martin.chess.engine.FENNotation;
import martin.chess.engine.Move;

/**
 * https://lichess.org/editor
 */
public class FuncTest {

		
	@Test
	public void pinnedPieceRook() {
		// Rook on e3, can only move up or down the e-file
		Board board = new Board("8/4q3/8/8/8/4R3/8/4K3 w - - 0 1");
		
		Assert.assertEquals("[e1f2, e1d2, e1e2, e1f1, e1d1, e3e4, e3e5, e3e6, e3e7, e3e2]", board.getAvailableMoves().toString());
	}
	
	@Test
	public void pinnedPieceKnight() {
		Board board = new Board("8/4q3/3N4/2K5/8/8/8/8 w - - 0 1");
		
		Assert.assertEquals("[c5b6, c5d4, c5b4, c5c6, c5c4, c5d5, c5b5]", board.getAvailableMoves().toString());
	}
	
	@Test
	public void kingInCheck1() {
		Board board = new Board("8/1b2q3/3KR3/8/8/8/8/8 w - - 0 1");
		
		Assert.assertEquals("[d6e7, d6e5, e6e7]", board.getAvailableMoves().toString());
	}
	
	@Test
	public void kingInCheck2() {
		Board board = new Board("8/8/4k3/8/2Q5/6N1/3R4/8 b - - 0 1");
		
		Assert.assertEquals("[e6e7, e6e5, e6f6]", board.getAvailableMoves().toString());
		
		board.move(new Move("e6f6"));
		
		board.move(new Move("d2d6"));
		
		Assert.assertEquals("[f6g7, f6e7, f6g5, f6e5]", board.getAvailableMoves().toString());
	}
	
	@Test
	public void knightCanBlockCheck() {
		
		Board board = new Board("8/4q3/8/2K5/4N3/8/8/8 w - - 0 1");
		
		Assert.assertEquals("[e4d6, c5b6, c5d4, c5c6, c5c4, c5d5, c5b5]", board.getAvailableMoves().toString());
	}

	@Test
	public void pinBecomesUnpinned() {
		
		Board board = new Board("8/8/8/kp4Q1/4P3/2p5/8/8 b - - 0 1");
		
		// Black pawn on b5 can't move because it would expose king to check
		Assert.assertEquals("[c3c2, a5b6, a5b4, a5a6, a5a4]", board.getAvailableMoves().toString());
		
		// Black moves his other pawn
		board.move(new Move("c3c2"));
		
		// White moves his pawn and blocks the queen
		Assert.assertEquals("[e4e5, g5h6, g5f6, g5e7, g5d8, g5h4, g5f4, g5e3, g5d2, g5c1, g5g6, g5g7, g5g8, g5g4, g5g3, g5g2, g5g1, g5h5, g5f5, g5e5, g5d5, g5c5, g5b5]", 
				board.getAvailableMoves().toString());
			
		board.move(new Move("e4e5"));
		
		// Now the black pawn can move from b5 to to b4. Also, note the queening moves
		Assert.assertEquals("[c2c1b, c2c1q, c2c1n, c2c1r, a5b6, a5b4, a5a6, a5a4, b5b4]", board.getAvailableMoves().toString());
	}
	
	@Test
	public void castlingPossibilityElimatedByCapture() {
		Board board = new Board("8/8/8/8/8/2P4r/8/4K2R w K - 0 1");
		
		// White can castle (e1g1) 
		Assert.assertEquals("[e1f2, e1d2, e1e2, e1f1, e1d1, e1g1, h1h2, h1h3, h1g1, h1f1, c3c4]", board.getAvailableMoves().toString());
		
		// But chooses not to
		board.move(new Move("c3c4"));

		// Black captures the rook
		Assert.assertEquals("[h3h4, h3h5, h3h6, h3h7, h3h8, h3h2, h3h1, h3g3, h3f3, h3e3, h3d3, h3c3, h3b3, h3a3]", board.getAvailableMoves().toString());
		
		board.move(new Move("h3h1"));
		
		// Castling is now not possible
		Assert.assertEquals("[e1f2, e1d2, e1e2]", board.getAvailableMoves().toString());
	}
	
	@Test
	public void castlingMoves() {
		Board board = new Board("8/r7/8/8/8/8/8/R3K2R w KQ - 0 1");
		
		// Should be able to castle queen side (e1c1) and king side (e1g1)
		Assert.assertEquals("[a1a2, a1a3, a1a4, a1a5, a1a6, a1a7, a1b1, a1c1, a1d1, e1f2, e1d2, e1e2, e1f1, e1d1, e1g1, e1c1, h1h2, h1h3, h1h4, h1h5, h1h6, h1h7, h1h8, h1g1, h1f1]", board.getAvailableMoves().toString());
	}
	
	@Test
	public void enpassantCapture() {
		Board board = new Board("3kq3/p7/8/1P6/8/8/8/7K b - - 0 1");
		
		Assert.assertEquals("[a7a6, a7a5, d8e7, d8c7, d8d7, d8c8, e8f7, e8g6, e8h5, e8d7, e8c6, e8b5, e8e7, e8e6, e8e5, e8e4, e8e3, e8e2, e8e1, e8f8, e8g8, e8h8]", 
			board.getAvailableMoves().toString());
		
		// Blacks pawn on a7 moves to a5
		board.move(new Move("a7a5"));
		
		// White captures en passant
		board.move(new Move("b5a6"));
		
		Assert.assertEquals("[d8e7, d8c7, d8d7, d8c8, e8f7, e8g6, e8h5, e8d7, e8c6, e8b5, e8a4, e8e7, e8e6, e8e5, e8e4, e8e3, e8e2, e8e1, e8f8, e8g8, e8h8]", board.getAvailableMoves().toString());
		
		Assert.assertEquals("3kq3/8/P7/8/8/8/8/7K b - - 0 2", FENNotation.toString(board));
	}

}
