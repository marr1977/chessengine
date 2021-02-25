package martin.chess.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import martin.chess.engine.Board;
import martin.chess.engine.GameResultData;
import martin.chess.engine.Move;
import martin.chess.engine.Piece;
import martin.chess.engine.PieceType;
import martin.chess.strategy.IPlayerStrategy;
import martin.chess.strategy.RandomStrategy;
import martin.chess.ui.DragAndDropDetector.DragAndDropHandler;

public class BoardDrawer implements DragAndDropHandler {

	private Board board;
	private Canvas boardCanvas;
	private double size;
	private double squareSize;
	private Image piecesImage;
	private double pieceImageWidth;
	private double pieceOffset;
	private double dotSize;
	private double dotOffset;
	
	private static final Color DARK_SQUARE_COLOR = new Color(0.46, 0.588, 0.337, 1);
	private static final Color DARK_DOT = new Color(0.416, 0.529, 0.30, 1);
	
	private static final Color LIGHT_SQUARE_COLOR = new Color(0.93, 0.93, 0.82, 1);
	private static final Color LIGHT_DOT = new Color(0.839, 0.839, 0.74, 1);

	private static final Color DARK_SQUARE_COLOR_LASTPLAYED = new Color(0.73, 0.796, 0.169, 1);
	private static final Color LIGHT_SQUARE_COLOR_LASTPLAYED = new Color(0.97, 0.97, 0.412, 1);
	
	private DragAndDropDetector dragDetector;
	
	private Piece draggedPiece;
	private double dragOffsetX;
	private double dragOffsetY;
	private int dragFromIdx;
	
	private Map<Integer, Set<Integer>> availableMoves = new HashMap<>();
	
	private static final double CAPTURE_CIRCLE_WIDTH = 6;
	
	private IPlayerStrategy playerStrategyWhite;
	private IPlayerStrategy playerStrategyBlack;
	
	private int lastPlayedFromIdx = -1;
	private int lastPlayedToIdx = -1;
	
	private boolean gameInProgress = false;
	
	private GameListener gameListener;
	
	public BoardDrawer(Canvas boardCanvas, GameListener gameListener) {
		this.boardCanvas = boardCanvas;
		this.gameListener = gameListener;

		this.board = new Board();
		
		dragDetector = new DragAndDropDetector(boardCanvas, this);
		
		piecesImage = new Image(getClass().getResourceAsStream("/500px-Chess_Pieces_Sprite.svg.png"));
		pieceImageWidth = piecesImage.getWidth() / 6;
		
		size = Math.min(boardCanvas.getWidth(), boardCanvas.getHeight());
		squareSize = size / 8;
		dotSize = squareSize / 3;
		dotOffset = (squareSize - dotSize) / 2;
		pieceOffset = (squareSize - pieceImageWidth) / 2;
		
		drawBoard();
	}

	public void startGame(PlayerType whitePlayerType, PlayerType blackPlayerType) {
		resetGame();
		this.gameInProgress = true;
		this.playerStrategyWhite = getStrategy(whitePlayerType);
		this.playerStrategyBlack = getStrategy(blackPlayerType);
		requestMove();
	}
	
	private IPlayerStrategy getStrategy(PlayerType playerType) {
		switch (playerType) {
 			case Human:			return null;
 			case RandomRobby:	return new RandomStrategy();
		
		}
		throw new IllegalArgumentException("Unknown player type: " + playerType);
	}

	private void drawBoard() {
		GraphicsContext gc = boardCanvas.getGraphicsContext2D();
		
		for (int rank = 0; rank < 8; ++rank) {
			for (int file = 0; file < 8; ++file) {
				boolean isDarkSquare = board.getSquareColor(rank, file) == martin.chess.engine.Color.BLACK;
				
				gc.setFill(isDarkSquare ? 
						isLastPlayed(rank, file) ? DARK_SQUARE_COLOR_LASTPLAYED : DARK_SQUARE_COLOR : 
						isLastPlayed(rank, file) ? LIGHT_SQUARE_COLOR_LASTPLAYED : LIGHT_SQUARE_COLOR);
				
				double topLeftX = file * squareSize;
				double topLeftY = (7 - rank) * squareSize;
				
				gc.fillRect(topLeftX, topLeftY, squareSize, squareSize);
				Piece piece = board.pieceAt(rank, file);

				if (piece != null && piece != draggedPiece) {
					if (isSquareAvailable(rank, file)) {
						gc.setLineWidth(CAPTURE_CIRCLE_WIDTH);
						gc.setStroke(isDarkSquare ? DARK_DOT : LIGHT_DOT);
						gc.strokeOval(topLeftX + CAPTURE_CIRCLE_WIDTH/2, topLeftY + CAPTURE_CIRCLE_WIDTH/2, squareSize - CAPTURE_CIRCLE_WIDTH, squareSize - CAPTURE_CIRCLE_WIDTH);
					}
					
					double topOffset = piece.getColor() == martin.chess.engine.Color.BLACK ? piecesImage.getHeight() / 2 : 0;
					double leftOffset = pieceImageWidth * getPieceOffset(piece.getType());
					
					gc.drawImage(piecesImage, leftOffset, topOffset, pieceImageWidth, pieceImageWidth, topLeftX + pieceOffset, topLeftY + pieceOffset, pieceImageWidth, pieceImageWidth);
				} else if (isSquareAvailable(rank, file)) {
					gc.setFill(isDarkSquare ? DARK_DOT : LIGHT_DOT);
					gc.fillOval(topLeftX + dotOffset, topLeftY + dotOffset, dotSize, dotSize);
				}
			}
		}
		
		if (draggedPiece != null) {
			double topOffset = draggedPiece.getColor() == martin.chess.engine.Color.BLACK ? piecesImage.getHeight() / 2 : 0;
			double leftOffset = pieceImageWidth * getPieceOffset(draggedPiece.getType());
			
			gc.drawImage(piecesImage, leftOffset, topOffset, pieceImageWidth, pieceImageWidth, dragDetector.getX() + dragOffsetX, dragDetector.getY() + dragOffsetY, pieceImageWidth, pieceImageWidth);
			
		}
	}


	private boolean isLastPlayed(int rank, int file) {
		int squareIdx = board.getArrayIdx(rank, file);

		return lastPlayedFromIdx == squareIdx || lastPlayedToIdx == squareIdx;
	}

	private boolean isSquareAvailable(int rank, int file) {
		if (availableMoves == null) {
			return false;
		}
		var files = availableMoves.get(rank);
		return files != null && files.contains(file);
	}


	private int getPieceOffset(PieceType type) {
		switch(type) {
		case BISHOP:	return 2;
		case KING:		return 0;
		case KNIGHT:	return 3;
		case PAWN:		return 5;
		case QUEEN:		return 1;
		case ROOK:		return 4;
		default:
			break;
		}
		throw new IllegalArgumentException("Unknown type");
	}

	@Override
	public void onDragStarted() {
		if (!gameInProgress) {
			return;
		}
		
		System.out.println("To move:" + board.getColorToMove());
		System.out.println("Result:" + board.getResult());
		
		int rank = 7 - (int) (dragDetector.getY() / squareSize);
		int file = (int) (dragDetector.getX() / squareSize);
		
		Piece piece = board.pieceAt(rank, file);
		if (piece == null) {
			return;
		}
		
		if (piece.getColor() != board.getColorToMove()) {
			return;
		}
		
		IPlayerStrategy type = piece.getColor() == martin.chess.engine.Color.BLACK ? playerStrategyBlack : playerStrategyWhite;
		
		if (type != null) {
			return; // It is computer
		}
		
		// Calculate a drag offset so that that the piece doesn't "jump" as soon as you start dragging
		
		double topLeftSquareX = file * squareSize;
		double topLeftSquareY = (7 - rank) * squareSize;
		
		double offsetIntoSquareX = dragDetector.getX() - topLeftSquareX - pieceOffset;
		double offsetIntoSquareY = dragDetector.getY() - topLeftSquareY - pieceOffset;

		dragOffsetX = -offsetIntoSquareX;
		dragOffsetY = -offsetIntoSquareY;
		
		System.out.println("Started dragging " + piece);
		draggedPiece = piece;
		
		dragFromIdx = board.getArrayIdx(rank, file);
		
		availableMoves = new HashMap<>();
		for (var move : board.getAvailableMoves()) {
			if (move.getIdxFrom() == dragFromIdx) {
				int toRank = move.getIdxTo() / 8;
				int toFile = move.getIdxTo() % 8;
				availableMoves.computeIfAbsent(toRank, k -> new HashSet<>()).add(toFile);
			}
		}
		System.out.println("Moves: " + availableMoves);
		System.out.println("All moves: " + board.getAvailableMoves());
		
		drawBoard();
	}

	public boolean isGameInProgress() {
		return gameInProgress;
	}
	
	@Override
	public void onDrag() {
		drawBoard();
	}


	@Override
	public void onDragEnded() {
		moveDraggedPiece();
	}


	private void moveDraggedPiece() {
		if (draggedPiece == null) {
			return;
		}
		
		// Check if this is an available square. 
		// TODO: We should probably use the X/Y of the center of the image instead of
		// the mouse pointer
		
		int rank = 7 - (int) (dragDetector.getY() / squareSize);
		int file = (int) (dragDetector.getX() / squareSize);

		if (isSquareAvailable(rank, file)) {
			int idxTo = board.getArrayIdx(rank, file);
			// We could do this
			// board.move(new Move(dragFromIdx, ));
			// But this doesn't work for queening moves
			for (var move : board.getAvailableMoves()) {
				if (move.getIdxFrom() == dragFromIdx && 
					move.getIdxTo() == idxTo && 
					(move.getQueeningPiece() == null || move.getQueeningPiece() == PieceType.QUEEN)) {
					
					doMove(move);
					break;
				}
			}
		}
		
		draggedPiece = null;
		availableMoves = null;
		
		drawBoard();
	}

	private void doMove(Move move) {
		lastPlayedFromIdx = move.getIdxFrom();
		lastPlayedToIdx = move.getIdxTo();
		board.move(move);
		drawBoard();
		
		if (board.getResult() != null) {
			gameInProgress = false;
			gameListener.onGameEnded(board.getResult());
		}
		else {
			requestMove();
		}
	}

	private void requestMove() {
		IPlayerStrategy strategyToPlay = board.getColorToMove() == martin.chess.engine.Color.BLACK ? playerStrategyBlack : playerStrategyWhite;
		if (strategyToPlay == null) {
			return;
		}
		
		try {
			doMove(strategyToPlay.getMove(board));
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public interface GameListener {
		void onGameEnded(GameResultData result);
	}

	public void resetGame() {
		gameInProgress = false;
		lastPlayedFromIdx = -1;
		lastPlayedToIdx = -1;
		this.board = new Board();
		drawBoard();
	}
}
