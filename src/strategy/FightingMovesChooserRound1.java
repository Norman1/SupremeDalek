package strategy;

import helpers.MovesPerformer;

import java.util.ArrayList;
import java.util.List;

import main.Region;
import main.SuperRegion;
import move.PlaceArmiesMove;
import strategy.MovesChooser.Moves;
import bot.BotState;

public class FightingMovesChooserRound1 {

	public static Moves getFightingMovesRound1(BotState state, int armiesForFighting) {
		Moves out = new Moves();
		List<Region> southAmericaBorderRegions = new ArrayList<>();
		List<Region> australiaBorderRegions = new ArrayList<>();
		SuperRegion australia = state.getVisibleMap().getSuperRegion(6);
		SuperRegion southAmerica = state.getVisibleMap().getSuperRegion(2);
		for (Region region : state.getVisibleMap().getOpponentBorderingRegions(state)) {
			if (region.getSuperRegion().equals(australia)) {
				australiaBorderRegions.add(region);
			}
			if (region.getSuperRegion().equals(southAmerica)) {
				southAmericaBorderRegions.add(region);
			}
		}
		if (australiaBorderRegions.size() > 0) {
			PlaceArmiesMove placeArmiesMove = new PlaceArmiesMove(state.getMyPlayerName(),
					australiaBorderRegions.get(0), armiesForFighting);
			MovesPerformer.performDeployment(state, placeArmiesMove);
			out.armyPlacementMoves.add(placeArmiesMove);
			out.totalDeployment += armiesForFighting;
		} else if (southAmericaBorderRegions.size() > 0) {
			PlaceArmiesMove placeArmiesMove = new PlaceArmiesMove(state.getMyPlayerName(),
					southAmericaBorderRegions.get(0), armiesForFighting);
			MovesPerformer.performDeployment(state, placeArmiesMove);
			out.armyPlacementMoves.add(placeArmiesMove);
			out.totalDeployment += armiesForFighting;
		} else {
			out = FightingMovesChooser.getFightingMoves(state, armiesForFighting);
		}

		return out;
	}

}
