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
			List<List<Integer>> pinData = currentState.pinnedPieces.get(move.idxFrom); //54
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
			logInfo("Performing move " + move + " in state: " + FENNotation.toString(this));
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
	private void getMoves(List<Move> moves, List<Move> attackingMoves, int rank, int file, Piece piece, int[][] directions, boolean oneStepOnly) {
		int fromIdx = getArrayIdx(rank, file);

		ColorData opponentData = currentState.getColorData(piece.color.getOpposite());
		
		for (int[] vector : directions) {
			
			int i = 1;
			
			boolean blocked = false;
			boolean hasMetKing = false;
			List<Integer> encounteredOwnPieces = new ArrayList<>();
			List<Integer> encounteredOpponentPieces= new ArrayList<>();

			List<Integer> pinnedSquares = new ArrayList<>();
			Set<Integer> pathsToKing = new HashSet<>();
			
			pinnedSquares.add(fromIdx);
			
			while (true) {
				int newRank = vector[0] * i + rank;
				int newFile = vector[1] * i + file;
				
				int newIdx = getArrayIdx(newRank, newFile);
				if (newIdx == -1) {
					break;
				}
				
				if (!hasMetKing) {
					pathsToKing.add(newIdx);
				}
				
				Piece pieceAtDest = board[newIdx];
				if (!blocked) {
					if (pieceAtDest == null) {
						moves.add(new Move(fromIdx, newIdx));
						attackingMoves.add(new Move(fromIdx, newIdx));
					} else {
						blocked = true;
						
						if (pieceAtDest.color != piece.color) {
							// Captures
							moves.add(new Move(fromIdx, newIdx));
							
							if (pieceAtDest.type == PieceType.KING) {
								opponentData.attackingPathsToMyKing.add(pathsToKing);
								
								hasMetKing = true;
							} else {
								encounteredOpponentPieces.add(newIdx);
							}
						} else {
							attackingMoves.add(new Move(fromIdx, newIdx));
							encounteredOwnPieces.add(newIdx);
							
							if (currentState.enPassantTargetIdx == -1 || pieceAtDest.type != PieceType.PAWN) {
								break;
							}
						}
					} 
					
					pinnedSquares.add(newIdx);
				} else {
					if (pieceAtDest != null) {
						if (hasMetKing) {
							break;
						}
						if (pieceAtDest.color == piece.color) {
							encounteredOwnPieces.add(newIdx);
							if (currentState.enPassantTargetIdx == -1 || pieceAtDest.type != PieceType.PAWN) {
								break;
							}
						} 
						else {
							if (pieceAtDest.type == PieceType.KING) {
								hasMetKing = true; 
							} else {
								encounteredOpponentPieces.add(newIdx);
							}
						}
					} else if (!hasMetKing) {
						pinnedSquares.add(newIdx);
					}
				}
				
				if (hasMetKing && encounteredOpponentPieces.size() == 0) {
					attackingMoves.add(new Move(fromIdx, newIdx));
				}
				
				if (oneStepOnly) {
					break;
				}
				
				++i;
			}
			
			if (hasMetKing && encounteredOwnPieces.isEmpty() && encounteredOpponentPieces.size() == 1) {
				Integer pinnedIdx = encounteredOpponentPieces.get(0);
				//System.out.println(board[pinnedIdx] + " is pinned on squares " + pinnedSquares.stream().map(idx -> Algebraic.toAlgebraic(idx)).collect(Collectors.toList()));
				currentState.pinnedPieces.computeIfAbsent(pinnedIdx, k -> new ArrayList<>()).add(pinnedSquares);
			}
			
			if (hasMetKing && vector[0] == 0 && currentState.enPassantTargetIdx != -1 && encounteredOwnPieces.size() == 1 && encounteredOpponentPieces.size() == 1) {
				int ownIdx = encounteredOwnPieces.get(0);
				int oppIdx = encounteredOpponentPieces.get(0);
				if (board[ownIdx].type == PieceType.PAWN && board[oppIdx].type == PieceType.PAWN) {
					// If our pawn is the one causing en-passant, pin the opposing pawn from capturing
					int enPassantPawnRank = currentState.enPassantTargetIdx / 8;
					int enPassantPawnFile = currentState.enPassantTargetIdx % 8;
					
					int opponentFile = oppIdx % 8;
					
					// enPassantTargetIdx represents the capturing square, but the pawn is one rank more advanced
					int expectedOurPawnRank = board[ownIdx].color == Color.WHITE ? 2 : 5;
					if (expectedOurPawnRank == enPassantPawnRank && ((enPassantPawnFile - 1) == opponentFile || (enPassantPawnFile + 1) == opponentFile)) {
						// "Pin" the opposing pawn to forward or capture the other way
						int opposingRankDelta = board[oppIdx].color == Color.WHITE ? 1 : -1;
						int opposingRank = oppIdx / 8;
						int opposingFile = oppIdx % 8;
						
						int fwdIdx = getArrayIdx(opposingRank + opposingRankDelta, opposingFile);
						int leftCaptureIdx = getArrayIdx(opposingRank + opposingRankDelta, opposingFile + 1);
						int rightCaptureIdx = getArrayIdx(opposingRank + opposingRankDelta, opposingFile - 1);
						
						List<Integer> pinnedPawnSquares = new ArrayList<>();
						pinnedPawnSquares.add(fwdIdx);
						if (leftCaptureIdx != currentState.enPassantTargetIdx) {
							pinnedPawnSquares.add(leftCaptureIdx);
						}
						if (rightCaptureIdx != currentState.enPassantTargetIdx) {
							pinnedPawnSquares.add(rightCaptureIdx);
						}
						currentState.pinnedPieces.computeIfAbsent(oppIdx, k -> new ArrayList<>()).add(pinnedPawnSquares);
					}
				}
				
			}
		}
		
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
