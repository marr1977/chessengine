package martin.chess.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import martin.chess.engine.Board;
import martin.chess.engine.Move;
import martin.chess.engine.Piece;
import martin.chess.engine.PieceType;
import martin.chess.ui.DragAndDropDetector.DragAndDropHandler;

public class BoardDrawer implements DragAndDropHandler {

	private Board board;
	private Canvas boardCanvas;
	private boolean whiteIsDown;
	private double size;
	private double squareSize;
	private Image piecesImage;
	private double pieceImageWidth;
	private double pieceOffset;
	private double dotSize;
	private double dotOffset;
	
	private static final Color BLACK = new Color(0.46, 0.588, 0.337, 1);
	private static final Color BLACK_DOT = new Color(0.416, 0.529, 0.30, 1);
	
	private static final Color WHITE = new Color(0.93, 0.93, 0.82, 1);
	private static final Color WHITE_DOT = new Color(0.839, 0.839, 0.74, 1);
	
	private DragAndDropDetector dragDetector;
	
	private Piece draggedPiece;
	private double dragOffsetX;
	private double dragOffsetY;
	private int dragFromIdx;
	
	private static final double CAPTURE_CIRCLE_WIDTH = 6;
	
	public BoardDrawer(Board board, Canvas boardCanvas, boolean whiteIsDown) {
		this.board = board;
		this.boardCanvas = boardCanvas;
		
		dragDetector = new DragAndDropDetector(boardCanvas, this);
		
		this.whiteIsDown = whiteIsDown;
		piecesImage = new Image(getClass().getResourceAsStream("/500px-Chess_Pieces_Sprite.svg.png"));
		pieceImageWidth = piecesImage.getWidth() / 6;
		
		size = Math.min(boardCanvas.getWidth(), boardCanvas.getHeight());
		squareSize = size / 8;
		dotSize = squareSize / 3;
		dotOffset = (squareSize - dotSize) / 2;
		pieceOffset = (squareSize - pieceImageWidth) / 2;
		
		drawBoard();
	}


	public void drawBoard() {
		GraphicsContext gc = boardCanvas.getGraphicsContext2D();
		
		for (int rank = 0; rank < 8; ++rank) {
			for (int file = 0; file < 8; ++file) {
				boolean isDarkSquare = board.getSquareColor(rank, file) == martin.chess.engine.Color.BLACK;
				
				gc.setFill(isDarkSquare ? BLACK : WHITE);
				
				double topLeftX = file * squareSize;
				double topLeftY = (7 - rank) * squareSize;
				
				gc.fillRect(topLeftX, topLeftY, squareSize, squareSize);
				Piece piece = board.pieceAt(rank, file);

				if (piece != null && piece != draggedPiece) {
					if (isSquareAvailable(rank, file)) {
						gc.setLineWidth(CAPTURE_CIRCLE_WIDTH);
						gc.setStroke(isDarkSquare ? BLACK_DOT : WHITE_DOT);
						gc.strokeOval(topLeftX + CAPTURE_CIRCLE_WIDTH/2, topLeftY + CAPTURE_CIRCLE_WIDTH/2, squareSize - CAPTURE_CIRCLE_WIDTH, squareSize - CAPTURE_CIRCLE_WIDTH);
					}
					
					double topOffset = piece.getColor() == martin.chess.engine.Color.BLACK ? piecesImage.getHeight() / 2 : 0;
					double leftOffset = pieceImageWidth * getPieceOffset(piece.getType());
					
					gc.drawImage(piecesImage, leftOffset, topOffset, pieceImageWidth, pieceImageWidth, topLeftX + pieceOffset, topLeftY + pieceOffset, pieceImageWidth, pieceImageWidth);
				} else if (isSquareAvailable(rank, file)) {
					gc.setFill(isDarkSquare ? BLACK_DOT : WHITE_DOT);
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
		System.out.println("To move:" + board.getColorToMove());
		System.out.println("Result:" + board.getResult());
		
		int rank = 7 - (int) (dragDetector.getY() / squareSize);
		int file = (int) (dragDetector.getX() / squareSize);
		
		Piece piece = board.pieceAt(rank, file);
		if (piece == null) {
			return;
		}
		
		if (board.getResult() == null) {
			return;
		}
		
		if (piece.getColor() != board.getColorToMove()) {
			return;
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
					
					board.move(move);
					break;
				}
			}
		}
		
		draggedPiece = null;
		availableMoves = null;
		
		drawBoard();
	}

	private Map<Integer, Set<Integer>> availableMoves = new HashMap<>();
}
