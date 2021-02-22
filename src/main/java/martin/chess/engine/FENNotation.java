package martin.chess.engine;

import martin.chess.engine.Board.BoardState;
import martin.chess.engine.Board.CastlingAbility;

/**
 * https://en.wikipedia.org/wiki/Forsyth%E2%80%93Edwards_Notation
 *
 */
public class FENNotation {

	private static final int BOARD_SIZE = 8;
	
	static void initialize(Board theBoard, String fenString) {
		Piece[] board = theBoard.getBoard();
		BoardState currentState = theBoard.getCurrentState();
		
		for (Piece piece : board) {
			if (piece != null) {
				throw new IllegalArgumentException("Board has already been initialized");
			}
		}
		
		int rank = 7;
		int file = 0;
		
		int i = 0;
		char c;
		for (; i < fenString.length(); ++i) {
			
			c = fenString.charAt(i);
			if (c == ' ') {
				break;
			}
			
			if (c == '/') {
				rank--;
				file = 0;
				continue;
			}
			
			if (Character.isDigit(c)) {
				file += Character.digit(c, 10);
				continue;
			}
			
			if (Character.isAlphabetic(c)) {
				Color color = Character.isUpperCase(c) ? Color.WHITE : Color.BLACK;
				PieceType type= null;
				
				switch (Character.toUpperCase(c)) {
					case 'P': type = PieceType.PAWN; break;
					case 'R': type = PieceType.ROOK; break;
					case 'N': type = PieceType.KNIGHT; break;
					case 'B': type = PieceType.BISHOP; break;
					case 'Q': type = PieceType.QUEEN; break;
					case 'K': type = PieceType.KING; break;
					default: throw new IllegalArgumentException(("Invalid piece: " + c));
				}
				
				board[getArrayIdx(rank, file)] = new Piece(type, color);
				file++;
			}
		}
		
		i++;
		
		// To move
		currentState.colorToMove = fenString.charAt(i) == 'w' ? Color.WHITE : Color.BLACK;
		i+=2;
		
		// Castling availability
		if (fenString.charAt(i) != '-') {
			while (fenString.charAt(i) != ' ') {
				CastlingAbility ca = Character.isUpperCase(fenString.charAt(i)) ? currentState.whiteData.castling : currentState.blackData.castling;
				c = Character.toUpperCase(fenString.charAt(i));
				if (c == 'K') {
					ca.canCastleKingSide = true;
				} else if (c == 'Q') {
					ca.canCastleQueenSide = true;
				} else {
					throw new IllegalArgumentException("Bad castling character: " + c);
				}
				i++;
			}
		} else {
			i++;
		}
		
		// En passant square
		i++;
		if (fenString.charAt(i) != '-') {
			currentState.enPassantTargetIdx = Algebraic.fromAlgebraic(fenString, i);
			i += 1;
		}
		
		i+=2;

		// Half-move clock
		int nextSpace = fenString.indexOf(' ', i);
		currentState.halfMoveClock = Integer.parseInt(fenString.substring(i,  nextSpace));
		
		i = nextSpace + 1;
		
		//Full move number
		currentState.moveNumber = Integer.parseInt(fenString.substring(i));

	}
	
	public static String toString(Board board) {
		Piece[] pieces = board.getBoard();
		BoardState state = board.getCurrentState();
		
		StringBuilder sb = new StringBuilder();
		
		for (int rank = BOARD_SIZE - 1; rank >= 0; --rank) {
			
			int file = 0;
			while (true) {
			
				int empty = 0;
				while (file < BOARD_SIZE && pieces[getArrayIdx(rank, file)] == null) {
					file++;
					empty++;
				}
				if (empty > 0) {
					sb.append(empty);
				}
				
				if (file >= BOARD_SIZE) {
					break;
				}
				Piece piece = pieces[getArrayIdx(rank, file)];
				sb.append(piece.toString());
				file++;
			}
			
			if (rank > 0) {
				sb.append("/");
			}
		}
		
		sb.append(" ").append(state.colorToMove == Color.WHITE ? "w" : "b").append(" ");
		
		CastlingAbility whiteCA = state.getColorData(Color.WHITE).castling;
		CastlingAbility blackCA = state.getColorData(Color.BLACK).castling;
		
		StringBuilder castlingString = new StringBuilder();
		if (whiteCA.canCastleKingSide) {
			castlingString.append(PieceType.KING.getShortName(Color.WHITE));
		}
		if (whiteCA.canCastleQueenSide) {
			castlingString.append(PieceType.QUEEN.getShortName(Color.WHITE));
		}
		if (blackCA.canCastleKingSide) {
			castlingString.append(PieceType.KING.getShortName(Color.BLACK));
		}
		if (blackCA.canCastleQueenSide) {
			castlingString.append(PieceType.QUEEN.getShortName(Color.BLACK));
		}
		sb.append(castlingString.length() > 0 ? castlingString.toString() : "-").append(" ");
		
		
		if (state.enPassantTargetIdx != -1) {
			sb.append(Algebraic.toAlgebraic(state.enPassantTargetIdx));	
		} else {
			sb.append("-");
		}
		sb.append(" ").append(state.halfMoveClock).append(" ").append(state.moveNumber);
		
		return sb.toString();
	}
	
	private static int getArrayIdx(int rank, int file) {
		return rank * 8 + file;
	}
}
