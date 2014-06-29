package evaluation;

import main.Region;
import main.SuperRegion;
import bot.BotState;

public class SimpleHeuristics {

	public static void calculateOwnedFractions(BotState state) {
		for (SuperRegion superRegion : state.getVisibleMap().getSuperRegions()) {
			double totalRegions = superRegion.getSubRegions().size();
			double ownedRegions = 0;
			for (Region region : superRegion.getSubRegions()) {
				if (region.getPlayerName().equals(state.getMyPlayerName())) {
					ownedRegions++;
				}
			}
			superRegion.setOwnedFraction(ownedRegions / totalRegions);
		}
	}

	// may return null
	public static void calculateMostDesirableSuperregion(BotState state) {
		double biggestFractionOwned = 0;
		SuperRegion mostDesirableSuperRegion = null;
		for (SuperRegion superRegion : state.getVisibleMap().getSuperRegions()) {
			if (superRegion.getOwnedFraction() > biggestFractionOwned
					&& superRegion.getOwnedFraction() != 1) {
				biggestFractionOwned = superRegion.getOwnedFraction();
			}
		}
		for (SuperRegion superRegion : state.getVisibleMap().getSuperRegions()) {
			if (superRegion.getOwnedFraction() >= biggestFractionOwned
					&& superRegion.getOwnedFraction() != 1
					&& superRegion.getOwnedFraction() != 0) {
				mostDesirableSuperRegion = superRegion;
			}
		}
		for (SuperRegion superRegion : state.getVisibleMap().getSuperRegions()) {
			if (superRegion.equals(mostDesirableSuperRegion)) {
				superRegion.setIsMostDesirableSuperregion(true);
			} else {
				superRegion.setIsMostDesirableSuperregion(false);
			}
		}
	}
}
