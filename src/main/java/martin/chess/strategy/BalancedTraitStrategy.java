package martin.chess.strategy;

import martin.chess.strategy.traits.AvoidCheckMateTrait;
import martin.chess.strategy.traits.AvoidPieceExposureTrait;
import martin.chess.strategy.traits.CapturePieceTrait;
import martin.chess.strategy.traits.DevelopPiecesTrait;
import martin.chess.strategy.traits.DontPlacePieceInExposureTrait;
import martin.chess.strategy.traits.PerformCheckMateTrait;
import martin.chess.strategy.traits.ProtectAttackedPieceTrait;

public class BalancedTraitStrategy extends TraitStrategy {

	public BalancedTraitStrategy() {
		super(3);
		addTrait(new AvoidCheckMateTrait(), 1);
		addTrait(new PerformCheckMateTrait(), 1);
		addTrait(new DontPlacePieceInExposureTrait(), 0.7);
		addTrait(new CapturePieceTrait(), 1);
		addTrait(new AvoidPieceExposureTrait(), 0.8);
		
		// TODO:
		addTrait(new DevelopPiecesTrait(), 1);
		addTrait(new ProtectAttackedPieceTrait(), 1);
	}
}
