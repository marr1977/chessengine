package martin.chess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import martin.chess.engine.Color;
import martin.chess.engine.GameResultData;
import martin.chess.strategy.Pair;
import martin.chess.strategy.TraitStrategy;
import martin.chess.strategy.traits.AvoidCheckMateTrait;
import martin.chess.strategy.traits.AvoidPieceExposureTrait;
import martin.chess.strategy.traits.CapturePieceTrait;
import martin.chess.strategy.traits.DontPlacePieceInExposureTrait;
import martin.chess.strategy.traits.PerformCheckMateTrait;

/**
 * Try to develop the best TraitStrategy by letting TraitStrategies with different c  
 *
 */
public class Genetics {
	
	private static final double ADJUSTMENT_RANGE = 0.1; // 10%

	private Random random = new Random();
	
	@Test
	public void test() {
		var strategies = getStrategies(50, 3, 1, 1, 1, 1, 1);
		
		final int NUM_GAMES = 10;
		
		runTournament(strategies, NUM_GAMES);
	}
	
	
	
	private void runTournament(List<Pair<TraitStrategy, Double>> strategies, int numGames) {
		long start = System.currentTimeMillis();
		int totalNumGames = 0;
		
		for (int i = 0; i < strategies.size(); ++i) {
			for (int j = i + 1; j < strategies.size(); ++j) {
				System.out.println("Starting match between #" + i + " and #" + j);
				
				
				for (int game = 0; game < numGames; ++game) {
					var white = game % 2 == 0 ? strategies.get(i) : strategies.get(j);   
					var black = game % 2 == 0 ? strategies.get(j) : strategies.get(i);
					
					if (game % 100 == 0) {
						System.out.println("Starting game " + (game + 1));
					}
					
					GameManager mgr = new GameManager(white.first, black.first);
					mgr.setLogging(false);
					GameResultData result = mgr.startGame();
					if (result.getWinner() != null) {
						if (result.getWinner() == Color.BLACK) {
							black.second += 1;
						} else {
							white.second += 1;
						}
					} else {
						black.second += 0.5;
						white.second += 0.5;
					}
					totalNumGames++;
				} 
			}
		}
		
		long end = System.currentTimeMillis();
		
		System.out.println("Played " + totalNumGames + " in " + (end - start)/1000 + " seconds (" + ((end - start)/(double) totalNumGames) + " ms per game)");
		
		Collections.sort(strategies, new Comparator<Pair<TraitStrategy, Double>>() {

			@Override
			public int compare(Pair<TraitStrategy, Double> o1, Pair<TraitStrategy, Double> o2) {
				return -Double.compare(o1.second, o2.second);
			}
			
		});

		System.out.println("Done. Top 5 strategies: ");
		
		for (int i = 0; i < 5; ++i) {
			System.out.println("Score: " + strategies.get(i).second + ". Strategy: " + strategies.get(i).first);
		}
	}

	private List<Pair<TraitStrategy, Double>> getStrategies(
		int numStrategies,
		double exponent,
		double avoidCheckMateFactor,
		double performCheckMateFactor,
		double dontPlacePieceInExposureFactor,
		double capturePieceFactor,
		double avoidPieceExposureFactor) {
		
		ExecutorService executorService = Executors.newFixedThreadPool(6);
		
		var list = new ArrayList<Pair<TraitStrategy, Double>>();
		
		for (int i = 0; i < numStrategies; ++i) {
			TraitStrategy strat = new TraitStrategy(adjust(exponent), executorService);
			strat.addTrait(new AvoidCheckMateTrait(), adjust(avoidCheckMateFactor));
			strat.addTrait(new PerformCheckMateTrait(), adjust(performCheckMateFactor));
			strat.addTrait(new DontPlacePieceInExposureTrait(), adjust(dontPlacePieceInExposureFactor));
			strat.addTrait(new CapturePieceTrait(), adjust(capturePieceFactor));
			strat.addTrait(new AvoidPieceExposureTrait(), adjust(avoidPieceExposureFactor));
			
			list.add(new Pair<TraitStrategy, Double>(strat, 0d));
		}
		
		return list;
	}
	
	double adjust(double val) {
		double adjustmentFactor = 1 + (-ADJUSTMENT_RANGE + 2*ADJUSTMENT_RANGE*random.nextDouble());
		return val * adjustmentFactor;
	}
	
}
