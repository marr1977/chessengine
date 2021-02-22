package martin.chess.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Board {
	
	private static final PieceType[] QUEENING_PIECES = new PieceType[] {
		PieceType.BISHOP,
		PieceType.QUEEN,
		PieceType.KNIGHT,
		PieceType.ROOK,
	};
	
	private static final int[][] KNIGHT_DIRECTIONS = new int[][] {
		new int[] {1, -2},
		new int[] {2, -1},
		new int[] {1, 2},
		new int[] {2, 1},
		new int[] {-1, -2},
		new int[] {-2, -1},
		new int[] {-1, 2},
		new int[] {-2, 1},
	};

	private static final int[][] BISHOP_DIRECTIONS = new int[][] {
		new int[] {1, 1},
		new int[] {1, -1},
		new int[] {-1, 1},
		new int[] {-1, -1}
	};
			
	private static final int[][] ROOK_DIRECTIONS = new int[][] {
		new int[] {1, 0},
		new int[] {-1, 0},
		new int[] {0, 1},
		new int[] {0, -1}
	};
	
	private static final int[][] ALL_DIRECTIONS = new int[][] {
		new int[] {1, 1},
		new int[] {1, -1},
		new int[] {-1, 1},
		new int[] {-1, -1},
		new int[] {1, 0},
		new int[] {-1, 0},
		new int[] {0, 1},
		new int[] {0, -1}
	};
	

	static class CastlingAbility {
		public CastlingAbility() {
		}
		
		public CastlingAbility(CastlingAbility from) {
			canCastleKingSide = from.canCastleKingSide;
			canCastleQueenSide = from.canCastleQueenSide;
		}
		
		boolean canCastleKingSide;
		boolean canCastleQueenSide;
	}

	static class BoardState {
		public BoardState() {
			 blackData = new ColorData();
			 whiteData = new ColorData();
		}
		
		public BoardState(BoardState from) {
			this.colorToMove = from.colorToMove;
			this.halfMoveClock = from.halfMoveClock;
			this.moveNumber = from.moveNumber;
			this.enPassantTargetIdx = from.enPassantTargetIdx;
			
			this.blackData = new ColorData(from.blackData);
			this.whiteData = new ColorData(from.whiteData);
		}
		
		Color colorToMove;
		int enPassantTargetIdx = -1;
		ColorData blackData;
		ColorData whiteData;
		Map<Integer, List<List<Integer>>> pinnedPieces = new HashMap<>();
		int halfMoveClock;
		int moveNumber;
		
		public ColorData getColorData(Color color) {
			return color == Color.WHITE ? whiteData : blackData;
		}
	}
	
	static class ColorData {
		boolean inCheck;
		List<Set<Integer>> attackingPathsToMyKing = new ArrayList<>();
		Map<Integer, Set<Integer>> squaresAttackedByMe = new HashMap<>();
		CastlingAbility castling;
		
		ColorData() {
			castling = new CastlingAbility();
		}
		
		ColorData(ColorData from) {
			inCheck = from.inCheck;
			attackingPathsToMyKing = new ArrayList<>(from.attackingPathsToMyKing);
			squaresAttackedByMe = new HashMap<>(from.squaresAttackedByMe);
			castling = new CastlingAbility(from.castling);
		}
		
		void clear() {
			attackingPathsToMyKing.clear();
			squaresAttackedByMe.clear();
			inCheck = false;
		}

		public String getAttackedSquares() {
			return squaresAttackedByMe.entrySet().stream().map(i -> Algebraic.toAlgebraic(i.getKey())).collect(Collectors.joining(","));
		}
	}
	
	static class BoardHistoryEntry {
		BoardState state;
		Move move;
		Piece takenPiece;
		int takenPieceIdx;
		Piece originalPieceMoved;
	}
	
	private List<BoardHistoryEntry> history = new ArrayList<>();
	private Piece[] board = new Piece[64];
	private BoardState currentState = new BoardState();

	private Color winner;
	private GameResult result;
	private List<Move> availableMoves;

	private boolean logging = true;

	private boolean validateMoves;
	
	public Board() {
		this("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
	}
	
	public Board(String fenString) {
		logInfo("Initializing from " + fenString);
		FENNotation.initialize(this, fenString);
		updateAvailableMoves();
	}
	
	public List<Move> getAvailableMoves() {
		return availableMoves;
	}

	public GameResult getResult() {
		return result;
	}
	
	public Color getWinner() {
		return winner;
	}
	
	public int getNumberOfMoves() {
		return currentState.moveNumber;
	}
	
	private void updateAvailableMoves() {
		availableMoves = getAvailableMovesInt();
		
		if (availableMoves.size() == 0) {
			if (isInCheck(currentState.colorToMove)) {
				result = GameResult.CHECKMATE;
				winner = currentState.colorToMove == Color.WHITE ? Color.BLACK : Color.WHITE;
			} else {
				result = GameResult.STALEMATE;
			}
		}
		
		if (isInsufficientMaterial()) {
			result = GameResult.DRAW_INSUFFICIENT_MATERIAL;
		}
	}
	
	// Combinations with insufficient material to checkmate include:
	// king versus king
	// king and bishop versus king
	// king and knight versus king
	// king and bishop versus king and bishop with the bishops on the same color.
	private boolean isInsufficientMaterial() {
		List<Piece> nonKingPieces = Arrays.stream(board).filter(p -> p != null && p.type != PieceType.KING).collect(Collectors.toList());
		if (nonKingPieces.size() == 0) {
			return true;
		}
		
		if (nonKingPieces.size() == 1) { 
			return 
				nonKingPieces.get(0).type == PieceType.BISHOP || 
				nonKingPieces.get(0).type == PieceType.KNIGHT;
		}
		
		if (nonKingPieces.size() == 2) {
			Piece piece1 = nonKingPieces.get(0);
			Piece piece2 = nonKingPieces.get(1);
			if (piece1.type != PieceType.BISHOP || piece2.type != PieceType.BISHOP || piece1.color == piece2.color) {
				return false;
			}
			// Now we have to find their colors...
			return getSquareColor(piece1) == getSquareColor(piece2);
		}
		return false;
	}

	private Color getSquareColor(int rank, int file) {
		if (rank % 2 == 0) {
			return file % 2 == 0 ? Color.BLACK : Color.WHITE;
		}
		return file % 2 == 1 ? Color.BLACK : Color.WHITE;
	}
	
	private Color getSquareColor(Piece piece) {
		for (int i = 0; i < board.length; ++i) {
			if (board[i] == piece) {
				int rank = i / 8;
				int file = i % 8;
				return getSquareColor(rank, file);
			}
		}
		return null;
	}

	private void logInfo(String string) {
		if (!logging) {
			return;
		}
		System.out.println(string);
	}

	private int getArrayIdx(int rank, int file) {
		if (rank < 0 || rank > 7) {
			return -1;
		}
		if (file < 0 || file > 7) {
			return -1;
		}
		return rank * 8 + file;
	}

	/**
	 * Gets a list of all playable moves in the current state 
	 */
	private List<Move> getAvailableMovesInt() {
		currentState.pinnedPieces.clear();
		currentState.blackData.clear();
		currentState.whiteData.clear();
		
		// Update squares attacked by the other party
		getAvailableMoves(currentState.colorToMove == Color.WHITE ? Color.BLACK : Color.WHITE, false);
		
		return getAvailableMoves(currentState.colorToMove, true);
	}
	
	
	/**
	 * Gets the list of playable moves for the given color. 
	 * 
	 * If considerCheck is true, a move is not considered playable if it would cause 
	 * a check.
	 * 
	 * It is false in the cases where we want to see if a color is in check
	 */
	private List<Move> getAvailableMoves(Color color, boolean considerCheck) {
		
		List<Move> moves = new ArrayList<>();
		List<Move> attackingMoves = new ArrayList<>();
		
		for (int rank = 0; rank < 8; ++rank) {
			for (int file = 0; file < 8; ++file) {
				Piece piece = board[getArrayIdx(rank, file)];
				if (piece != null && piece.color == color) {
					List<Move> pieceMoves = getAvailableMoves(rank, file, piece, attackingMoves);
					moves.addAll(pieceMoves);
				}
			}
		}
		
		int opponentKingIdx = -1;
		for (int idx = 0; idx < board.length; ++idx) {
			Piece piece = board[idx];
			if (piece != null && piece.color != color && piece.type == PieceType.KING) {
				opponentKingIdx = idx;
				break;
			}
		}
		
		ColorData myData = currentState.getColorData(color);
		ColorData opponentData = currentState.getColorData(color.getOpposite());
				
		Map<Integer, Set<Integer>> attackedSquares = new HashMap<>();
		for (var move : attackingMoves) {
			attackedSquares.computeIfAbsent(move.getIdxTo(), k -> new HashSet<>()).add(move.getIdxFrom());
			if (attackedSquares.containsKey(opponentKingIdx)) {
				opponentData.inCheck = true;
			}
		}
		
		myData.squaresAttackedByMe = attackedSquares;
		
		if (considerCheck) {
			Iterator<Move> moveIterator = moves.iterator();
			while (moveIterator.hasNext()) {
				Move move = moveIterator.next();
				if (wouldBeInCheck(color, move)) {
					moveIterator.remove();
				}
			}
		}
		
		
		
		return moves;
	}
	
	/**
	 * Gets a list of playable moves for the given piece 
	 */
	private List<Move> getAvailableMoves(int rank, int file, Piece piece, List<Move> attackingMoves) {
		int fromIdx = getArrayIdx(rank, file);
		
		List<Move> moves = new ArrayList<>();
		
		switch (piece.type) {
		case BISHOP:
			getMoves(moves, attackingMoves, rank, file, piece, BISHOP_DIRECTIONS, false);
			break;
		case KNIGHT:
			getMoves(moves, attackingMoves, rank, file, piece, KNIGHT_DIRECTIONS, true);
			break;
		case QUEEN:
			getMoves(moves, attackingMoves, rank, file, piece, ALL_DIRECTIONS, false);
			break;
		case ROOK:
			getMoves(moves, attackingMoves, rank, file, piece, ROOK_DIRECTIONS, false);
			break;
		case KING:
			getMoves(moves, attackingMoves, rank, file, piece, ALL_DIRECTIONS, true);
			
			CastlingAbility ca = currentState.getColorData(piece.color).castling;
			
			int kingRankToCastle = piece.color == Color.WHITE ? 0 : 7;
			int kingFileToCastle = 4;
			
			if (kingRankToCastle == rank && kingFileToCastle == file) {
				getCastlingMove(moves, piece, rank, file, ca, true);
				getCastlingMove(moves, piece, rank, file, ca, false);
			}
			
			break;
		case PAWN:
			
			int forwardRank = piece.color == Color.WHITE ? 1 : -1;
			int takeLeftIdx = getArrayIdx(rank + forwardRank, file - 1);
			int takeRightIdx = getArrayIdx(rank + forwardRank, file + 1);
					
			// Capture left?
			if (takeLeftIdx != -1) {
				attackingMoves.add(new Move(fromIdx, takeLeftIdx));
				getPawnCaptureMove(piece, fromIdx, moves, takeLeftIdx);
			}

			// Capture right?
			if (takeRightIdx != -1) {
				attackingMoves.add(new Move(fromIdx, takeRightIdx));
				getPawnCaptureMove(piece, fromIdx, moves, takeRightIdx);
			}
			
			// Go forward one step?
			int fwdIndex1 = getArrayIdx(rank + forwardRank, file);
			if (fwdIndex1 != -1 && null == board[fwdIndex1]) {
				addPawnMoves(moves, fromIdx, fwdIndex1);
			}
			
			boolean hasMoved = 
					(piece.color == Color.BLACK && rank != 6) ||
					(piece.color == Color.WHITE && rank != 1);
			
			if (!hasMoved) {
				// Go forward two steps?
				int fwdIndex2 = getArrayIdx(rank + 2 * forwardRank, file);
				if (fwdIndex2 != -1 && null == board[fwdIndex1] && null == board[fwdIndex2]) {
					addPawnMoves(moves, fromIdx, fwdIndex2);
				}
			}
			
			break;
		default:
			break;
		
		}
		
		return moves;
	}

	private void getPawnCaptureMove(Piece piece, int fromIdx, List<Move> moves, int takeIdx) {
		Piece takeLeftPiece = board[takeIdx];
		if (takeLeftPiece != null && takeLeftPiece.color != piece.color) {
			addPawnMoves(moves, fromIdx, takeIdx);
		}

		if (currentState.enPassantTargetIdx == takeIdx) {
			addPawnMoves(moves, fromIdx, currentState.enPassantTargetIdx);
		}
	}

	/**
	 * Adds a list of pawn moves from fromIdx to toIdx.
	 *  
	 * Considers the case when the pawn is queening in which case we add four possible moves
	 *  - Queen to queen
	 *  - Queen to knight
	 *  - Queen to bishop
	 *  - Queen to rook
	 *  
	 */
	private void addPawnMoves(List<Move> moves, int fromIdx, int toIdx) {
		int toRank = toIdx / 8;
		
		if (toRank == 7 || toRank == 0) {
			for (var type : QUEENING_PIECES) {
				moves.add(new Move(fromIdx, toIdx, type));
			}
		} else {
			moves.add(new Move(fromIdx, toIdx));
		}
	}

	private void getCastlingMove(List<Move> moves, Piece piece, int rank, int file, CastlingAbility ca, boolean castleKingSide) {
		if (castleKingSide && !ca.canCastleKingSide) {
			return;
		}
		
		if (!castleKingSide && !ca.canCastleQueenSide) {
			return;
		}
		
		if (isInCheck(piece.color))	{
			return;
		}
		
		int fileDelta = castleKingSide ? 1 : -1;
		int rookFile = castleKingSide ? 7 : 0;
		
		Piece rook = board[getArrayIdx(rank, rookFile)];
		
		if (rook == null || rook.color != piece.color && rook.type != PieceType.ROOK) {
			// Perhaps the rook was captured
			return;
		}
		
		int fromIdx = getArrayIdx(rank, file);
		
		// All squares towards the rook must be unoccupied and the king can't be in check on any intermediate square
		int nextKingFile = file + fileDelta;
		int stepsTaken = 1;
		while (nextKingFile != rookFile) {
			int newIdx = getArrayIdx(rank, nextKingFile);
			if (null != board[newIdx]) {
				// Occupied
				return;
			}
			
			// The king moves two squares, it can't be in check in any of them
			if (stepsTaken <= 2 && wouldBeInCheck(piece.color, new Move(fromIdx, newIdx))) {
				// Would be in check
				return;
			}
			
			nextKingFile = nextKingFile + fileDelta;
			stepsTaken++;
		}
		
		// Ok, can castle
		Move move = new Move(fromIdx, getArrayIdx(rank, file + fileDelta * 2));
		move.additionalIdxFrom = getArrayIdx(rank, rookFile);
		move.additionalIdxTo = getArrayIdx(rank, file + fileDelta);
		moves.add(move);
	}

	private boolean wouldBeInCheck(Color color, Move move) {
		
		Piece piece = board[move.idxFrom];

		ColorData myData = currentState.getColorData(piece.color);
		ColorData opponentData = currentState.getColorData(piece.color.getOpposite());
		
		var attackedSquares = opponentData.squaresAttackedByMe;
		var pathsToKing = myData.attackingPathsToMyKing;
		
		if (piece.type == PieceType.KING) {
			// Can't move to an attacked square
			return attackedSquares.containsKey(move.idxTo);
		} else {
			// Can't move this piece so that the king becomes in check
			
			// Check pinning
			List<List<Integer>> pinData = currentState.pinnedPieces.get(move.idxFrom); 
			if (pinData != null) {
				boolean matchesAllPins = true;
				for (var list : pinData) {
					if (!list.contains(move.idxTo)) {
						matchesAllPins = false;
						break;
					}
				}
				
				if (!matchesAllPins) {
					return true;
				}
			}
			
			// Check if we can block check by moving in the way of the attacker and the king
			boolean canBlockAllPaths = pathsToKing.size() > 0;
			for (var path : pathsToKing) {
				if (!path.contains(move.idxTo)) {
					canBlockAllPaths = false;
					break;
				}
			}

			// Check the current state of the king
			int kingIdx = getKingIdx(piece.color);
			var setOfAttackers = attackedSquares.get(kingIdx);
			boolean hasAttackers = setOfAttackers != null;
			
			if (hasAttackers) {
			
				// We are in check with multiple attackers
				if (setOfAttackers.size() > 1) {
					//System.err.println("Multiple attackers, in check: TRUE");
					return true;
				}
				
				// If this move captures the lone attacker, we are not in check
				if (setOfAttackers.contains(move.idxTo)) {
					//System.err.println("Single captured attacked, in check: FALSE");
					return false;
				}

				// See if this would in fact capture the attacker en-passant
				if (currentState.enPassantTargetIdx != -1 && move.idxTo == currentState.enPassantTargetIdx && piece.type == PieceType.PAWN) {
					int attackerIdx = setOfAttackers.iterator().next();
					
					int attackerRank = attackerIdx / 8; 
					int attackerFile = attackerIdx % 8;
							
					Piece attackingPiece = board[attackerIdx];
					if (attackingPiece.type == PieceType.PAWN) {
						int enPassantRank = currentState.enPassantTargetIdx / 8;
						int enPassantFile = currentState.enPassantTargetIdx % 8;
						
						// If the en passant capture rank is 2, the pawn must be on rank 3 (white), otherwise on rank 4 (black) 
						int expectedAttackerRank = enPassantRank == 2 ? 3 : 4;
								
						if (attackerFile == enPassantFile &&
							attackerRank == expectedAttackerRank) {
							// It would capture
							return false;
						}
					}
				}
			}
			
			
			if (canBlockAllPaths) {
				//System.err.println("Can block all paths with move to " + Algebraic.toAlgebraic(move.idxTo) + ", in check: FALSE");
				return false;
			}
			
			// Check en-passant
			if (!hasAttackers) {
				//if (piece.type == PieceType.PAWN && move.idxTo == currentState.enPassantTargetIdx)
			}
			
			//System.err.println("Returning default (hasAttackers), in check: " + hasAttackers);
			return hasAttackers;
		}
	}

	private int getKingIdx(Color color) {
		for (int idx = 0; idx < board.length; ++idx) {
			Piece kingPiece = board[idx];
			if (kingPiece != null && kingPiece.color == color && kingPiece.type == PieceType.KING) {
				return idx;
			}
		}
		throw new RuntimeException("No king!");
	}

	public void undoLastMove() {
		BoardHistoryEntry historyEntry = history.remove(history.size() - 1);
		
		this.currentState = historyEntry.state;
		
		Move move = historyEntry.move;
		board[move.idxTo] = null;
		board[move.idxFrom] = historyEntry.originalPieceMoved;
		
		if (move.additionalIdxFrom != -1) {
			Piece additionalPiece = board[move.additionalIdxTo];
			board[move.additionalIdxTo] = null;
			board[move.additionalIdxFrom] = additionalPiece;
		}
		
		if (historyEntry.takenPiece != null) {
			board[historyEntry.takenPieceIdx] = historyEntry.takenPiece;
		}
	}

	public void move(Move move) {
		if (validateMoves && !availableMoves.contains(move)) {
			throw new IllegalArgumentException("Move is illegal");
		}
		
		doMove(move, true);
		
		updateAvailableMoves();
	}
	
	private void doMove(Move move, boolean realMove) {
		if (realMove) {
			logInfo("Performing move " + move + " in state: " + getState());
		}
		
		int oldRank = move.idxFrom / 8;
		int oldFile = move.idxFrom % 8;
	
		int newRank = move.idxTo / 8;
		int newFile = move.idxTo % 8;
		
		Piece piece = board[move.idxFrom];
		board[move.idxFrom] = null;
		
		Piece takenPiece = board[move.idxTo];
		int takenPieceIdx = -1;
		
		if (takenPiece != null) {
			if (takenPiece.type == PieceType.KING) {
				String moves = this.history.stream().map(e -> e.move.toString()).collect(Collectors.joining(","));
				throw new RuntimeException("Can't capture king. Move " + move + " in state " + getState() + ", move history: " + moves);
			}
			
			takenPieceIdx = move.idxTo;
		} else if (piece.type == PieceType.PAWN && move.idxTo == currentState.enPassantTargetIdx) {
			// Find the pawn
			int takenRank = piece.color == Color.WHITE ? newRank - 1 : newRank + 1;
			takenPieceIdx = getArrayIdx(takenRank, newFile);
			takenPiece = board[takenPieceIdx];
			if (takenPiece == null || takenPiece.type != PieceType.PAWN) {
				throw new RuntimeException("enpassant capture of non-pawn");
			}
		}
		
		if (takenPieceIdx != -1) {
			board[takenPieceIdx] = null;
		}

		// Check queening and replace piece with new piece
		Piece originalPieceMoved = piece;
		if (move.queeningPiece != null) {
			piece = new Piece(move.queeningPiece, piece.color);
		}
		
		board[move.idxTo] = piece;
		
		// The rook in a castling move
		if (move.additionalIdxFrom != -1) {
			Piece additionalPiece = board[move.additionalIdxFrom];
			board[move.additionalIdxFrom] = null;
			board[move.additionalIdxTo] = additionalPiece;
		}
		
		BoardHistoryEntry historyEntry = new BoardHistoryEntry();
		historyEntry.move = move;
		historyEntry.originalPieceMoved = originalPieceMoved;
		historyEntry.state = new BoardState(currentState);
		historyEntry.takenPiece = takenPiece;
		historyEntry.takenPieceIdx = takenPieceIdx;
		history.add(historyEntry);
		
		// Adjust half move clock
		if (takenPiece != null || piece.type == PieceType.PAWN) {
			currentState.halfMoveClock = 0;
		} else {
			++currentState.halfMoveClock;
		}
		
		//
		// Check if this is a pawn moving two steps forward, update en-passant square
		//
		if (piece.type == PieceType.PAWN) {
			if (oldRank - newRank == 2) {
				// Black moving
				currentState.enPassantTargetIdx = getArrayIdx(newRank + 1, oldFile);
			} else if (newRank - oldRank == 2) {
				// White moving
				currentState.enPassantTargetIdx = getArrayIdx(oldRank + 1, oldFile);
			} else {
				currentState.enPassantTargetIdx = -1;
			}
		} else {
			currentState.enPassantTargetIdx = -1;
		}
		
		//
		// Update castling ability
		//
		if (realMove) {
			CastlingAbility ca = currentState.getColorData(currentState.colorToMove).castling;
			if (piece.type == PieceType.KING) {
				ca.canCastleKingSide = false;
				ca.canCastleQueenSide = false;
			} else if (piece.type == PieceType.ROOK) {
				int fromFile = move.idxFrom % 8;
				if (fromFile == 0) {
					ca.canCastleQueenSide = false;
				} else if (fromFile == 7) {
					ca.canCastleKingSide = false;
				}
			}
		}
		
		currentState.colorToMove = currentState.colorToMove == Color.WHITE ? Color.BLACK : Color.WHITE;
		
		if (currentState.colorToMove == Color.WHITE) {
			currentState.moveNumber++;
		}
		
	}

	public String getState() {
		return FENNotation.toString(this);
	}

	/**
	 * Returns true if the color is currently in check
	 */
	private boolean isInCheck(Color color) {
		return currentState.getColorData(color).inCheck; 
	}

	/**
	 * Given a piece on a square and a list of direction vectors, this method adds all possible moves in those directions
	 * 
	 * oneStepOnly = true for knight and king who can only move one multiple of the supplied direction vectors
	 *               false for queen/bishop/rook who can move one or more multiples of the direction vectors
	 */
	class PathData {
		List<Integer> path = new ArrayList<>();
		
		// Indices into "path"
		int opponentKingIdx = -1; 
		List<Integer> ownPiecesIdx = new ArrayList<>();
		List<Integer> oppPiecesIdx = new ArrayList<>();
		
		int firstPieceIdx = -1;
		boolean firstPieceIsOurs;

		private Piece ourPiece;

		private int[] vector;

		public PathData(Piece ourPiece, int[] vector) {
			this.ourPiece = ourPiece;
			this.vector = vector;
		}

		public void addSquare(int idx, Piece pieceOnSquare) {
			path.add(idx);
			
			if (pieceOnSquare != null) {
				int pieceIdx = path.size() - 1;
				
				if (ourPiece.color == pieceOnSquare.color) {
					ownPiecesIdx.add(pieceIdx);
				} else {
					if (pieceOnSquare.type == PieceType.KING) {
						opponentKingIdx = pieceIdx;
					} else {
						oppPiecesIdx.add(pieceIdx);
					}
				}
				if (firstPieceIdx == -1) {
					firstPieceIdx = pieceIdx;
					firstPieceIsOurs = ourPiece.color == pieceOnSquare.color; 
				}
			}
		}

		public boolean isBeforeFirstPiece(int idx) {
			return firstPieceIdx == -1 || idx < firstPieceIdx;
		}
		
		public boolean isPastFirstPiece(int idx) {
			return firstPieceIdx != -1 && idx > firstPieceIdx;
		}
		
		public boolean isAtFirstPiece(int idx) {
			return firstPieceIdx != -1 && idx == firstPieceIdx;
		}
		
		public int getOwnPiecesBefore(int idx) {
			return getPiecesBefore(ownPiecesIdx, idx);
		}

		public int getOpponentPiecesBefore(int idx) {
			return getPiecesBefore(oppPiecesIdx, idx);
		}

		private int getPiecesBefore(List<Integer> pieceIdxs, int idx) {
			int count = 0;
			for (int pieceIdx : pieceIdxs) {
				if (pieceIdx < idx) {
					++count;
				} else {
					break;
				}
			}
			return count;
		}
		
		@Override
		public String toString() {
			String direction = null;
			if (vector[0] == 0) {
				// Side ways
				if (vector[1] == -1) {
					direction = "LEFT";
				} else {
					direction = "RIGHT";
				}
			} else if (vector[0] == 1) {
				// Upwards
				if (vector[1] == 0) {
					direction = "UP";
				} else if (vector[1] == -1) {
					direction = "UP-LEFT";
				} else {
					direction = "UP-RIGHT";
				}
			} else {
				// Downwards
				if (vector[1] == 0) {
					direction = "DOWN";
				} else if (vector[1] == -1) {
					direction = "DOWN-LEFT";
				} else {
					direction = "DOWN-RIGHT";
				}
			}
			
			StringBuilder sb = new StringBuilder();
			
			sb.append(ourPiece.color).append(" ").append(ourPiece.type).append(" ").append(direction).append("\n");
			sb.append("Path: ").append(path.stream().map(i -> Algebraic.toAlgebraic(i)).collect(Collectors.joining(","))).append("\n");
			sb.append("King: ").append(opponentKingIdx == -1 ? "No" : Algebraic.toAlgebraic(path.get(opponentKingIdx))).append("\n");
			sb.append("Frst piece: ").append(firstPieceIdx == -1 ? "None" : Algebraic.toAlgebraic(path.get(firstPieceIdx)) + (firstPieceIsOurs ? " (ours)" : " (theirs)")).append("\n");
			sb.append("Own pieces: ").append(ownPiecesIdx.stream().map(i -> Algebraic.toAlgebraic(path.get(i))).collect(Collectors.joining(","))).append("\n");
			sb.append("Opp pieces: ").append(oppPiecesIdx.stream().map(i -> Algebraic.toAlgebraic(path.get(i))).collect(Collectors.joining(","))).append("\n");

			if (opponentKingIdx != -1) {
				sb.append("Own pieces before king: ").append(ownPiecesIdx.stream().filter(i -> i < opponentKingIdx).map(i -> Algebraic.toAlgebraic(path.get(i))).collect(Collectors.joining(","))).append("\n");
				sb.append("Opp pieces before king: ").append(oppPiecesIdx.stream().filter(i -> i < opponentKingIdx).map(i -> Algebraic.toAlgebraic(path.get(i))).collect(Collectors.joining(","))).append("\n");
			}
			
			return sb.toString();
		}
		
	}
	
	private void getMoves(List<Move> moves, List<Move> inCheckMoves, int rank, int file, Piece piece, int[][] directions, boolean oneStepOnly) {
		int fromIdx = getArrayIdx(rank, file);
		ColorData opponentData = currentState.getColorData(piece.color.getOpposite());
		
		for (int[] vector : directions) {
			
			PathData pathData = getPathData(vector, rank, file, piece, oneStepOnly);
			
			if (pathData.path.isEmpty()) {
				continue;
			}
			
			//
			// Add possible moves and attacking moves.
			//
			for (int i = 0; i < pathData.path.size(); ++i) {
				int toIdx = pathData.path.get(i);
				
				if (pathData.isBeforeFirstPiece(i)) {
					moves.add(new Move(fromIdx, toIdx));
					inCheckMoves.add(new Move(fromIdx, toIdx));
				}
				else if (pathData.isAtFirstPiece(i)) {
					if (pathData.firstPieceIsOurs) {
						// If king captures this piece, it would be in check
						inCheckMoves.add(new Move(fromIdx, toIdx));	
					} else {
						// We can capture this piece
						moves.add(new Move(fromIdx, toIdx));
					}
					
					break;
				}
				else if (pathData.isPastFirstPiece(i)) {
					break;
				}
			}
			
			if (pathData.opponentKingIdx != -1) {
				
				//
				// Add pinned pieces
				//
				// If there is only one piece between us and the king, that piece is pinned to the squares between us and the king
				//
				int ownPiecesBeforeKing = pathData.getOwnPiecesBefore(pathData.opponentKingIdx);
				int oppPiecesBeforeKing = pathData.getOpponentPiecesBefore(pathData.opponentKingIdx);
				
				if (ownPiecesBeforeKing == 0 && oppPiecesBeforeKing == 1) {

					Integer pinnedPieceIdx = pathData.path.get(pathData.oppPiecesIdx.get(0));
					List<Integer> pinnedSquares = new ArrayList<>(pathData.path.subList(0, pathData.opponentKingIdx)); // Clone since subList is a view
					// Add our own position to the pinned squares to let the pinned piece capture us
					pinnedSquares.add(fromIdx);
					//System.out.println(board[pinnedPieceIdx] + " is pinned on squares " + pinnedSquares.stream().map(idx -> Algebraic.toAlgebraic(idx)).collect(Collectors.toList()));
					
					currentState.pinnedPieces.computeIfAbsent(pinnedPieceIdx, k -> new ArrayList<>()).add(pinnedSquares);
				}
				
				//
				// If there is nothing between us and the king, add attacking squares behind the king.
				// This is to prevent the king from moving away from us but still in the line of sight 
				//
				if (ownPiecesBeforeKing == 0 && oppPiecesBeforeKing == 0) {
					for (int idx = pathData.opponentKingIdx; idx < pathData.path.size(); idx++) {
						inCheckMoves.add(new Move(fromIdx, pathData.path.get(idx)));
					}
					
					// Also add a path to king (to enable pieces to place themselves in this path, thereby preventing check)
					Set<Integer> pathToKing = new HashSet<>(pathData.path.subList(0, pathData.opponentKingIdx));
					opponentData.attackingPathsToMyKing.add(pathToKing);
				}
				
				//
				// This is a very special case for en-passant captures
				//
				// If we are attacking the king horizontally, and there is only two pawns of different colors between us and the king,
				// and if the opposing colored pawn can capture our pawn en passant, the en passant capture is illegal since it
				// would leave NO pieces between us and the king
				// 
				//
				if (ownPiecesBeforeKing == 1 && oppPiecesBeforeKing == 1 && 
					currentState.enPassantTargetIdx != -1 && vector[0] == 0) {
					
					int oppPieceIdx = pathData.path.get(pathData.oppPiecesIdx.get(0));
					int ownPieceIdx = pathData.path.get(pathData.ownPiecesIdx.get(0));
					
					if (board[ownPieceIdx].type == PieceType.PAWN && board[oppPieceIdx].type == PieceType.PAWN) {
						// Ok there are two different colored pawns between us.
						
						// Now if
						//
						// 1) Our pawn is the one who can be captured en-passant AND
						// 2) The other pawn can capture en passant
						//
						// Then we pin the other pawn to squares other than the en-passant capture square
						
						int enPassantPawnRank = currentState.enPassantTargetIdx / 8;
						int enPassantPawnFile = currentState.enPassantTargetIdx % 8;
						
						int oppPawnFile = oppPieceIdx % 8;
						
						int ownPawnRank = ownPieceIdx / 8;
						int ownPawnFile = ownPieceIdx % 8;

						int enpassantPawnRankForOurColor = board[ownPieceIdx].color == Color.WHITE ? 3 : 4;
						int enpassantCaptureRankForOurColor = board[ownPieceIdx].color == Color.WHITE ? 2 : 5;
						
						if (enpassantPawnRankForOurColor == ownPawnRank && 
							enpassantCaptureRankForOurColor == enPassantPawnRank &&
							enPassantPawnFile == ownPawnFile) {
							
							// Yes, our pawn caused the en passant
							
							// Can the other pawn capture our pawn?
							if ((oppPawnFile + 1) == ownPawnFile || (oppPawnFile - 1) == ownPawnFile) {
								// Yes. Pin the opposing pawn
								int opposingColorRankDelta = board[oppPieceIdx].color == Color.WHITE ? 1 : -1;

								int fwdIdx = getArrayIdx(ownPawnRank + opposingColorRankDelta, oppPawnFile);
								int leftCaptureIdx = getArrayIdx(ownPawnRank + opposingColorRankDelta, oppPawnFile + 1);
								int rightCaptureIdx = getArrayIdx(ownPawnRank + opposingColorRankDelta, oppPawnFile - 1);
								
								List<Integer> pinnedPawnSquares = new ArrayList<>();
								pinnedPawnSquares.add(fwdIdx);
								if (leftCaptureIdx != currentState.enPassantTargetIdx) {
									pinnedPawnSquares.add(leftCaptureIdx);
								}
								if (rightCaptureIdx != currentState.enPassantTargetIdx) {
									pinnedPawnSquares.add(rightCaptureIdx);
								}
								currentState.pinnedPieces.computeIfAbsent(oppPieceIdx, k -> new ArrayList<>()).add(pinnedPawnSquares);
							}
						}
						
					}
				}
					
			}
			
		}
		
	}	
	
	private PathData getPathData(int[] vector, int rank, int file, Piece piece, boolean oneStepOnly) {
		PathData pathData = new PathData(piece, vector);
		
		for (int i = 1; ; ++i) {
			int newRank = vector[0] * i + rank;
			int newFile = vector[1] * i + file;
			
			int newIdx = getArrayIdx(newRank, newFile);
			if (newIdx == -1) {
				break;
			}
			
			pathData.addSquare(newIdx, board[newIdx]);
			
			if (oneStepOnly) {
				break;
			}
		}
		
		return pathData;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(currentState.colorToMove + " to move\n");
		for (int rank = 7; rank >= 0; --rank) {
			
			sb.append("  ");
			for (int file = 0; file < 8; ++file) {
				sb.append("--");
			}
			sb.append("\n");
			
			sb.append((char)('1' + rank)).append(" ");
			
			for (int file = 0; file < 8; ++file) {
				sb.append("|");
				
				Piece piece = board[getArrayIdx(rank, file)];
				if (piece == null) {
					sb.append(" ");
				} else {
					sb.append(piece.toString());
				}
			}
			sb.append("|");
			
			sb.append("\n");
		}
		sb.append("  ");
		for (int file = 0; file < 8; ++file) {
			sb.append("--");
		}
		
		sb.append("\n");
		sb.append("  ");
		for (int file = 0; file < 8; ++file) {
			sb.append(" ").append((char) ('a' + file));
		}
		sb.append("\n");
		return sb.toString();
	}
	
	BoardState getCurrentState() {
		return currentState;
	}
	
	Piece[] getBoard() {
		return board;
	}

	public void setLogging(boolean logging) {
		this.logging = logging;
	}

	public void validateMoves(boolean validateMoves) {
		this.validateMoves = validateMoves;
	}

	public String getAttackedSquares() {
		return "By black: " + currentState.getColorData(Color.BLACK).getAttackedSquares() + "\nBy white: " + currentState.getColorData(Color.WHITE).getAttackedSquares();
	}
}
