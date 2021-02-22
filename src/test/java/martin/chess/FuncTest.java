package martin.chess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import martin.chess.engine.Algebraic;
import martin.chess.engine.Board;
import martin.chess.engine.FENNotation;
import martin.chess.engine.Move;

/**
 * https://lichess.org/editor
 */
public class FuncTest {

	@Test
	public void cantBlockBehindKing() {
		Board board = new Board("6k1/8/8/q7/8/8/2PK4/2Q5 w - - 0 1");
		
		verifyMoves("[c2c3, d2e3, d2d3, d2d1, d2e2]", board.getAvailableMoves());
	}
	
	@Test
	public void kingCantCaptureIfItWouldBeInCheck2() {
		Board board = new Board("8/8/8/K7/6R1/6k1/6P1/8 b - - 0 1");
		verifyMoves("[g3h2, g3f2, g3g4]", board.getAvailableMoves());
	}
	
	@Test
	public void cantCaptureEnPassantIfKingWouldBeInCheck1() {
		Board board = new Board("8/8/8/K7/1R3p1k/8/4P3/8 w - - 0 1");
		
		board.move(new Move("e2e4"));
		
		// f4f3 should not be possible since it would remove pawn on e4, thereby exposing king to check from rook on b4
		verifyMoves("[f4f3, h4g5, h4g3, h4h5, h4h3, h4g4]", board.getAvailableMoves());
	}
	
	@Test
	public void irrelevantEnPassantDoesNotStopPawnAdvance1() {
		Board board = new Board("4k3/4p3/8/8/8/8/1P2P3/2K1R3 w - - 0 1");
		board.move(new Move("b2b4"));

		// Bug was: missing e7e5
		verifyMoves("[e7e6, e7e5, e8f7, e8d7, e8f8, e8d8]", board.getAvailableMoves());
	}
	
	@Test
	public void irrelevantEnPassantDoesNotStopPawnAdvance2() {
		Board board = new Board("4k3/4p3/8/8/8/8/3PP3/2K1R3 w - - 0 1");
		board.move(new Move("d2d4"));

		printMovesByOrigin(board);
		
		// Bug was: missing e7e5
		verifyMoves("[e7e6, e7e5, e8f7, e8d7, e8f8, e8d8]", board.getAvailableMoves());
	}
	
	@Test
	public void enPassantCapturePossibleIfOnePieceStillBlocksKing() {
		Board board = new Board("8/8/8/8/R2pp2k/8/2P1P3/2K5 w - - 0 1");
		board.move(new Move("c2c4"));

		printMovesByOrigin(board);
		
		verifyMoves("[d4c3, d4d3, e4e3, h4g5, h4g3, h4h5, h4h3, h4g4]", board.getAvailableMoves());
	}
	
	@Test
	public void cantCaptureEnPassantIfKingWouldBeInCheck3() {
		Board board = new Board("8/8/8/K7/1R3p1k/8/4P1P1/8 w - - 0 1");
		
		board.move(new Move("g2g4"));
		printMovesByOrigin(board);
		
		// f4g3 should not be possible since it would remove pawn on g4, thereby exposing king to check from rook on b4
		verifyMoves("[f4f3, h4g5, h4g3, h4h3, h4g4]", board.getAvailableMoves());
	}
	
	@Test
	public void cantCaptureEnPassantIfKingWouldBeInCheck2() {
		Board board = new Board("8/8/5k2/K7/5p2/8/4PR2/8 w - - 0 1");
		board.move(new Move("e2e4"));
		
		// f4f3 is not valid
		verifyMoves("[f4f3, f6g7, f6e7, f6g5, f6e5, f6f7, f6g6, f6e6]", board.getAvailableMoves());
	}
	
	
	@Test
	public void cantCaptureEnPassantIfKingIsInCheck() {
		Board board = new Board("8/2p5/3p4/KP5r/5p2/8/2R1P2k/8 w - - 0 1");
		board.move(new Move("e2e4"));
		
		// Black king is now in check
		
		verifyMoves("[h2g3, h2g1, h2h3, h2h1]", board.getAvailableMoves());
	}
	
	@Test
	public void pawnCantCaptureIfKingWouldBeInCheck() {
		// ver ok in new solution
		
		Board board = new Board("k7/8/8/2q5/3p4/2P5/2K5/8 w - - 0 1");
		
		verifyMoves("[c2d3, c2b3, c2d1, c2b1, c2c1, c2d2, c2b2, c3c4]", board.getAvailableMoves());
	}
	
	@Test
	public void pieceBehindKingNotPinned() {
		// ver ok in new solution
		
		Board board = new Board("8/3q4/8/8/3R4/3K4/3N4/8 w - - 0 1");
		
		verifyMoves("[d2b3, d2c4, d2f3, d2e4, d2b1, d2f1, d3e4, d3c4, d3e2, d3c2, d3e3, d3c3, d4d5, d4d6, d4d7]", board.getAvailableMoves());
	}

	@Test
	public void opponentQueenPinnedWhenPawnQueens() {
		// ver ok in new solution
		
		Board board = new Board("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8");
		board.move(new Move("d7c8q"));
		
		verifyMoves(
			"[f2d3, f2e4, f2h3, f2g4, f2d1, f2h1, c6c5, a7a6, a7a5, b7b6, b7b5, e7f6, e7g5, e7h4, e7d6, e7c5, e7b4, e7a3, f7f6, f7f5, g7g6, g7g5, h7h6, h7h5, b8a6, b8d7, d8e8, d8c8, f8g8, f8e8, h8g8]", 
			board.getAvailableMoves());
	}
	
	@Test
	public void pinnedPieceRook() {
		// ver ok in new solution
		
		// Rook on e3, can only move up or down the e-file
		Board board = new Board("8/4q3/8/8/8/4R3/8/4K3 w - - 0 1");
		printMovesByOrigin(board);
		
		verifyMoves("[e1f2, e1d2, e1e2, e1f1, e1d1, e3e4, e3e5, e3e6, e3e7, e3e2]", board.getAvailableMoves());
	}
	
	private void verifyMoves(String expectedMovesStr, List<Move> availableMoves) {
		Set<String> expectedMoves = new HashSet<>();
		var split = expectedMovesStr.split(",");
		for (var token : split) {
			var trimmed = token.trim();
			if (trimmed.startsWith("[")) {
				trimmed = trimmed.substring(1);
			} else if (trimmed.endsWith("]")) {
				trimmed = trimmed.substring(0, trimmed.length() - 1);
			}
			expectedMoves.add(trimmed);
		}
		
		Set<String> actualMoves = availableMoves.stream().map(m -> m.toString()).collect(Collectors.toSet());
		
		Set<String> expectedButNotFound = new HashSet<>();
		
		for (String expectedMove : expectedMoves) {
			if (!actualMoves.remove(expectedMove)) {
				expectedButNotFound.add(expectedMove);
			}
		}
		
		if (actualMoves.size() == 0 && expectedButNotFound.size() == 0) {
			return;
		}
		System.out.println("Found   : " + availableMoves);
		System.out.println("Expected: " + expectedMovesStr);
		
		System.out.println("Expected moves not found: " + expectedButNotFound);
		System.out.println("Found moves not expected: " + actualMoves);
		
		Assert.fail("\nExpected moves not found: " + expectedButNotFound + "\nFound moves not expected: " + actualMoves);
	}

	@Test
	public void pinnedPieceKnight() {
		// ver ok in new solution
		Board board = new Board("8/4q3/3N4/2K5/8/8/8/8 w - - 0 1");
		
		verifyMoves("[c5b6, c5d4, c5b4, c5c6, c5c4, c5d5, c5b5]", board.getAvailableMoves());
	}
	
	@Test
	public void kingInCheck1() {
		// ver ok in new solution
		Board board = new Board("8/1b2q3/3KR3/8/8/8/8/8 w - - 0 1");
		
		verifyMoves("[d6e7, d6e5, e6e7]", board.getAvailableMoves());
	}
	
	@Test
	public void kingCantCaptureIfItWouldBeInCheck() {
		// ver ok in new solution
		Board board = new Board("8/4q3/8/3Kb3/8/8/8/8 w - - 0 1");
		
		verifyMoves("[d5c6, d5e4, d5c4]", board.getAvailableMoves());
	}
	
	@Test
	public void twoPathsToKingCantBlock() {
		// ver ok in new solution
		
		Board board = new Board("8/8/4b3/8/8/1K3q2/8/3R4 w - - 0 1");
		
		// Both black's queen and bishop have the king in the check, rook can only block one of them. Must move king
		verifyMoves("[b3a4, b3c2, b3b4, b3b2]", board.getAvailableMoves());
	}
	
	@Test
	public void noPinIfTwoPiecesAreProtectingKing() {
		// ver ok in new solution
		
		Board board = new Board("6k1/8/8/q7/8/R7/N7/K7 w - - 0 1");
		
		// Both rook on a3 and knight on a2 are free to move since they are both between the king and the black queen
		Assert.assertEquals("[a1b2, a1b1, a2c3, a2b4, a2c1, a3a4, a3a5, a3b3, a3c3, a3d3, a3e3, a3f3, a3g3, a3h3]", board.getAvailableMoves().toString());
		
		// Rook moves away, pinning the knight
		board.move(new Move("a3e3"));
		
		// Queen moves up one rank
		board.move(new Move("a5a6"));
		
		// Knight on a2 can't move
		verifyMoves("[a1b2, a1b1, e3e4, e3e5, e3e6, e3e7, e3e8, e3e2, e3e1, e3f3, e3g3, e3h3, e3d3, e3c3, e3b3, e3a3]", board.getAvailableMoves());
	}
	
	@Test
	public void kingCanCaptureIfPieceBetween() {
		// ver ok in new solution
		
		Board board = new Board("8/4q3/8/4n3/3Kb3/8/8/8 w - - 0 1");
		
		verifyMoves("[d4e3, d4c3, d4e4]", board.getAvailableMoves());
	}
	
	
	@Test
	public void extendedPinningTest() {
		// ver ok in new solution
		
		Board board = new Board("k7/4q3/4R3/4b3/4K3/8/8/8 w - - 0 1");
		
		Assert.assertEquals("[e4f5, e4d5, e4f3, e4d3, e4e5, e4e3, e6e7, e6e5, e6f6, e6g6, e6h6, e6d6, e6c6, e6b6, e6a6]", board.getAvailableMoves().toString());
		
		// White moves king down one rank
		board.move(new Move("e4e3"));
		
		// White moves bishop, rook is pinned
		board.move(new Move("e5c7"));
		
		verifyMoves("[e3d4, e3f2, e3d2, e3e4, e3e2, e3f3, e3d3, e6e7, e6e5, e6e4]", board.getAvailableMoves());
	}

	@Test
	public void kingCantCaptureProtectedPiece() {
		// ver ok in new solution
		
		Board board = new Board("8/2b5/8/8/5q2/4K3/8/8 w - - 0 1");
		verifyMoves("[e3e2, e3d3]", board.getAvailableMoves());
	}
	
	private void printMovesByOrigin(Board board) {
		Map<String, List<String>> byOrigin = new HashMap<>();
		
		for (var move : board.getAvailableMoves()) {
			String from = Algebraic.toAlgebraic(move.getIdxFrom());
			String to = Algebraic.toAlgebraic(move.getIdxTo());
			
			byOrigin.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
		}
		
		for (var e : byOrigin.entrySet()) {
			System.out.println(e.getKey() + ": " + e.getValue());
		}
		
		System.out.println(board.getAvailableMoves());
	}

	@Test
	public void kingInCheck2() {
		// ver ok in new solution
		
		Board board = new Board("8/8/4k3/8/2Q5/6N1/3R4/K7 b - - 0 1");
		
		verifyMoves("[e6e7, e6e5, e6f6]", board.getAvailableMoves());
		
		board.move(new Move("e6f6"));
		
		board.move(new Move("d2d6"));
		
		verifyMoves("[f6g7, f6e7, f6g5, f6e5]", board.getAvailableMoves());
	}
	
	@Test
	public void kingVsPawn() {
		// ver ok in new solution
		
		Board board = new Board("8/8/2kp4/8/2PK4/8/8/8 w - - 0 1");
		
		// White can't move to c5 or e5, they are attacked by pawn
		verifyMoves("[c4c5, d4e3, d4c3, d4d3, d4e4]", board.getAvailableMoves());
		
		board.move(new Move("d4e4"));
		
		// Black can't move to d5 or b5, they are attacked by pawn
		verifyMoves("[c6d7, c6b7, c6c7, c6c5, c6b6, d6d5]", board.getAvailableMoves());
		
		board.move(new Move("c6c5"));
		
		verifyMoves("[e4f5, e4f3, e4d3, e4e3, e4f4]", board.getAvailableMoves());
	}
	
	@Test
	public void knightCanBlockCheck() {
		// ver ok in new solution
		
		Board board = new Board("8/4q3/8/2K5/4N3/8/8/8 w - - 0 1");
		
		verifyMoves("[e4d6, c5b6, c5d4, c5c6, c5c4, c5d5, c5b5]", board.getAvailableMoves());
	}

	@Test
	public void knightCanBlockOneOfTwoChecks() {
		// ver ok in new solution
		
		Board board = new Board("k7/1r6/4q3/8/8/1K2N3/8/8 w - - 0 1");
		
		verifyMoves("[b3a4, b3c2, b3c3, b3a3]", board.getAvailableMoves());
	}

	@Test
	public void pinBecomesUnpinned() {
		// ver ok in new solution
		
		Board board = new Board("8/8/8/kp4Q1/4P3/2p5/8/7K b - - 0 1");
		
		// Black pawn on b5 can't move because it would expose king to check
		verifyMoves("[c3c2, a5b6, a5b4, a5a6, a5a4]", board.getAvailableMoves());
		
		// Black moves his other pawn
		board.move(new Move("c3c2"));
		
		// White moves his pawn and blocks the queen
		verifyMoves("[h1g2, h1h2, h1g1, e4e5, g5h6, g5f6, g5e7, g5d8, g5h4, g5f4, g5e3, g5d2, g5c1, g5g6, g5g7, g5g8, g5g4, g5g3, g5g2, g5g1, g5h5, g5f5, g5e5, g5d5, g5c5, g5b5]", 
				board.getAvailableMoves());
			
		board.move(new Move("e4e5"));
		
		// Now the black pawn can move from b5 to to b4. Also, note the queening moves
		verifyMoves("[c2c1b, c2c1q, c2c1n, c2c1r, a5b6, a5b4, a5a6, a5a4, b5b4]", board.getAvailableMoves());
	}
	
	@Test
	public void castlingPossibilityElimatedByCapture() {
		// ver ok in new solution
		
		Board board = new Board("k7/8/8/8/8/2P4r/8/4K2R w K - 0 1");
		
		// White can castle (e1g1) 
		verifyMoves("[e1f2, e1d2, e1e2, e1f1, e1d1, e1g1, h1h2, h1h3, h1g1, h1f1, c3c4]", board.getAvailableMoves());
		
		// But chooses not to
		board.move(new Move("c3c4"));

		// Black captures the rook
		verifyMoves("[h3h4, h3h5, h3h6, h3h7, h3h8, h3h2, h3h1, h3g3, h3f3, h3e3, h3d3, h3c3, h3b3, h3a3, a8b7, a8a7, a8b8]", board.getAvailableMoves());
		
		board.move(new Move("h3h1"));
		
		// Castling is now not possible
		verifyMoves("[e1f2, e1d2, e1e2]", board.getAvailableMoves());
	}
	
	@Test
	public void unableToCastleDueToCheckOnKing() {
		// ver ok in new solution
		
		Board board = new Board("k7/8/8/8/8/5p2/P7/4K2R w K - 0 1");
		
		// Short castling (e1g1) is available)
		verifyMoves("[e1f2, e1d2, e1f1, e1d1, e1g1, h1h2, h1h3, h1h4, h1h5, h1h6, h1h7, h1h8, h1g1, h1f1, a2a3, a2a4]", board.getAvailableMoves());
		
		// White pushes pawn
		board.move(new Move("a2a3"));
		
		// Black pushes pawn
		board.move(new Move("f3f2"));

		// e1g1 not available
		verifyMoves("[e1f2, e1d2, e1e2, e1f1, e1d1]", board.getAvailableMoves());
	}
	
	@Test
	public void castlingMoves() {
		// ver ok in new solution
		
		Board board = new Board("8/r7/8/8/8/8/8/R3K2R w KQ - 0 1");
		
		// Should be able to castle queen side (e1c1) and king side (e1g1)
		verifyMoves("[a1a2, a1a3, a1a4, a1a5, a1a6, a1a7, a1b1, a1c1, a1d1, e1f2, e1d2, e1e2, e1f1, e1d1, e1g1, e1c1, h1h2, h1h3, h1h4, h1h5, h1h6, h1h7, h1h8, h1g1, h1f1]", board.getAvailableMoves());
	}
	
	@Test
	public void enpassantCapture() {
		// ver ok in new solution
		
		Board board = new Board("3kq3/p7/8/1P6/8/8/8/7K b - - 0 1");
		
		verifyMoves("[a7a6, a7a5, d8e7, d8c7, d8d7, d8c8, e8f7, e8g6, e8h5, e8d7, e8c6, e8b5, e8e7, e8e6, e8e5, e8e4, e8e3, e8e2, e8e1, e8f8, e8g8, e8h8]", 
			board.getAvailableMoves());
		
		// Blacks pawn on a7 moves to a5
		board.move(new Move("a7a5"));
		
		// White captures en passant
		board.move(new Move("b5a6"));
		
		verifyMoves("[d8e7, d8c7, d8d7, d8c8, e8f7, e8g6, e8h5, e8d7, e8c6, e8b5, e8a4, e8e7, e8e6, e8e5, e8e4, e8e3, e8e2, e8e1, e8f8, e8g8, e8h8]", board.getAvailableMoves());
		
		Assert.assertEquals("3kq3/8/P7/8/8/8/8/7K b - - 0 2", FENNotation.toString(board));
	}

}
