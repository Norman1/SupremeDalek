package basicAlgorithms;

import java.util.ArrayList;
import java.util.List;

import main.Region;
import model.HeuristicMapModel;
//import model.MapModel;
import bot.BotState;

/**
 * This class is responsible for calculating the distance to the closest opponent border.
 */
public class DistanceToOpponentBorderCalculator {
	
	public static List<Region> getClosestRegionsToOpponentBorder(BotState state){
		int closestDistance = 1000;
		for(Region region: state.getVisibleMap().getOwnedRegions(state)){
			if(region.getDistanceToOpponentBorder() < closestDistance){
				closestDistance = region.getDistanceToOpponentBorder();
			}
		}
		List<Region> out = new ArrayList<>();
		for(Region region: state.getVisibleMap().getOwnedRegions(state)){
			if(region.getDistanceToOpponentBorder() == closestDistance){
				out.add(region);
			}
		}
		return out;
	}

	public static void calculateDistanceToOpponentBorder(BotState state) {
		List<Region> guessedOpponentSpots = HeuristicMapModel.getGuessedOpponentRegions();
		// Give all guessed opponent spots a distance of 0
		for (Region opponentSpot : guessedOpponentSpots) {
			opponentSpot.setDistanceToOpponentBorder(0);
		}
		// Give each other region a distance of 1000 as initialization
		for (Region region : state.getVisibleMap().getRegions()) {
			if (!guessedOpponentSpots.contains(region)) {
				region.setDistanceToOpponentBorder(1000);
			}
		}
		// Now do the real stuff
		boolean hasSomethingChanged = true;
		while (hasSomethingChanged) {
			hasSomethingChanged = false;
			for (Region region : state.getVisibleMap().getRegions()) {
				Region closestNeighbor = getClosestNeighborToOpponentBorder(state, region);
				if (closestNeighbor.getDistanceToOpponentBorder() < region.getDistanceToOpponentBorder()
						&& region.getDistanceToOpponentBorder() != 1 + closestNeighbor.getDistanceToOpponentBorder()) {
					region.setDistanceToOpponentBorder(closestNeighbor.getDistanceToOpponentBorder() + 1);
					hasSomethingChanged = true;
				}
			}
		}

	}

	private static Region getClosestNeighborToOpponentBorder(BotState state, Region region) {
		List<Region> neighbors = region.getNeighbors();
		Region closestNeighbor = region;
		for (Region neighbor : neighbors) {
			if (neighbor.getDistanceToOpponentBorder() < closestNeighbor.getDistanceToOpponentBorder()) {
				closestNeighbor = neighbor;
			}
		}
		return closestNeighbor;
	}

}
