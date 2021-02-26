package martin.chess.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;

import martin.chess.engine.Board;
import martin.chess.engine.Color;
import martin.chess.engine.Move;
import martin.chess.fen.FENNotation;
import martin.chess.strategy.traits.Trait;

public class TraitStrategy implements IPlayerStrategy {

	private List<Pair<Trait, Double>> traits = new ArrayList<>();

	private Random random;

	private static final double MIN_MOVE_VALUE = 1;
	private static final boolean DEBUG = false;
	private static final int MOVES_TO_DEBUG = 5;

	private double exp;

	private ExecutorService executorService;
	
	public TraitStrategy(double exp) {
		this.exp = exp;
		random = new Random();
	}
	
	public TraitStrategy(double exp, ExecutorService executorService) {
		this(exp);
		this.executorService = executorService;
	}

	public double getExponent() {
		return exp;
	}
	
	public List<Pair<Trait, Double>> getTraits() {
		return traits;
	}
	
	public void addTrait(Trait trait, double weight) {
		trait.setExecutorService(executorService);
		
		traits.add(new Pair<>(trait, weight));
	}
	
	@Override
	public Move getMove(Board board) throws InterruptedException {
		Color ourColor = board.getColorToMove();
		
		Board clonedBoard = new Board(FENNotation.toString(board));
		clonedBoard.validateMoves(false);
		clonedBoard.setLogging(false);
		
		double minMoveValue = Double.MAX_VALUE;
		double totalMoveValue = 0;
		
		List<MoveAndValue> moveValues = new ArrayList<>();
		
		List<Move> moves = board.getAvailableMoves();
		
		traits.forEach(t -> t.first.initialize(board));
		
		for (var move : moves) {
			clonedBoard.move(move);
			
			MoveAndValue moveAndValue = new MoveAndValue(move);
			moveValues.add(moveAndValue);
			
			moveAndValue.value = MIN_MOVE_VALUE;
			
			for (var trait : traits) {
				double traitVoteBase = trait.first.vote(ourColor, board, clonedBoard, move);
				boolean isNegative = traitVoteBase < 0;
				
				if (isNegative) traitVoteBase = -traitVoteBase;
				
				double traitVote = Math.pow(trait.second * traitVoteBase, exp);
				
				if (isNegative) traitVote = -traitVote;
				
				moveAndValue.votes.put(trait.first.toString(), traitVote);
				moveAndValue.value += traitVote;
			}
			
			clonedBoard.undoLastMove();
					
			if (moveAndValue.value < minMoveValue) {
				minMoveValue = moveAndValue.value;
			}
			
			totalMoveValue += moveAndValue.value;
		}
		
		// Adjust so that we have no moves with value < MIN_MOVE_VALUE
		if (minMoveValue < MIN_MOVE_VALUE) {
			double adjustment = MIN_MOVE_VALUE - minMoveValue;
			totalMoveValue += adjustment;
			moveValues.forEach(mv -> mv.value += adjustment);
		}
		
		if (DEBUG) {
			Collections.sort(moveValues);
			StringBuilder sb = new StringBuilder();
			int numMoves = Math.min(moveValues.size(), MOVES_TO_DEBUG);
			sb.append("Top ").append(numMoves).append(" moves: \n");
			
			for (int i = 0; i < numMoves; ++i) {
				sb.append(" ").append(moveValues.get(i)).append("\n");
			}
			
			sb.append("\nBottom ").append(numMoves).append(" moves: \n");
			
			for (int i = 0; i < numMoves; ++i) {
				sb.append(" ").append(moveValues.get(moveValues.size() - 1 - i)).append("\n");
			}
			
			System.out.println(sb.toString());
		}
		return selectMove(moveValues, totalMoveValue);
	}
	
	private Move selectMove(List<MoveAndValue> moveValues, double totalMoveValue) {
		double randVal = random.nextDouble() * totalMoveValue;
		double cumulative = 0;
		for (var moveValue : moveValues) {
			cumulative += moveValue.value;
			if (cumulative >= randVal) {
				if (DEBUG) {
					System.out.println("Selected " + moveValue);
				}
				return moveValue.move;
			}
		}
		System.err.println("Didn't find a move with random process!");
		return moveValues.get(0).move;
	}

	private static class MoveAndValue implements Comparable<MoveAndValue> {
		Move move;
		double value;
		Map<String, Double> votes = new HashMap<>();
		
		public MoveAndValue(Move move) {
			this.move = move;
		}
		
		@Override
		public String toString() {
			return String.format("%s: %.2f. Votes = %s", move, value, votes);
		}

		@Override
		public int compareTo(MoveAndValue o) {
			return -Double.compare(value, o.value);
		}
	}
	
	@Override
	public String toString() {
		return String.format("Exp = %f, Traits = %s", exp, traits);
	}
}
