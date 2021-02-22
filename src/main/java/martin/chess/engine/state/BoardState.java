package martin.chess.engine.state;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import martin.chess.engine.Color;
import martin.chess.engine.GameResult;
import martin.chess.engine.Piece;

public class BoardState {
	public Color colorToMove;
	public int enPassantTargetIdx = -1;
	public SideData blackData;
	public SideData whiteData;
	public Map<Integer, List<List<Integer>>> pinnedPieces = new HashMap<>();
	public int halfMoveClock;
	public int moveNumber;
	public Color winner;
	public GameResult result;

	public BoardState() {
		 blackData = new SideData();
		 whiteData = new SideData();
	}
	
	public BoardState(BoardState from) {
		this.colorToMove = from.colorToMove;
		this.halfMoveClock = from.halfMoveClock;
		this.moveNumber = from.moveNumber;
		this.enPassantTargetIdx = from.enPassantTargetIdx;
		
		this.blackData = new SideData(from.blackData);
		this.whiteData = new SideData(from.whiteData);
		this.winner = from.winner;
		this.result = from.result;
	}
	
	public SideData getSideData(Color color) {
		return color == Color.WHITE ? whiteData : blackData;
	}

	public void updatePieceValues(Piece[] board) {
		int pieceValueBlack = 0;
		int pieceValueWhite = 0;
		
		for (Piece piece : board) {
			if (piece == null) {
				continue;
			}
			if (piece.getColor() == Color.WHITE) {
				pieceValueWhite += piece.getType().getValue();
			} else {
				pieceValueBlack += piece.getType().getValue();
			}
		}
		
		blackData.pieceValue = pieceValueBlack;
		whiteData.pieceValue = pieceValueWhite;
	}


}
