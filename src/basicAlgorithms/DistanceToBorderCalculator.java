package basicAlgorithms;

import java.util.ArrayList;
import java.util.List;

import main.Map;
import main.Region;
import bot.BotState;

/**
 * This class is responsible for calculating the distance to the closest border.
 */
public class DistanceToBorderCalculator {

	public static void calculateDistanceToBorder(BotState state) {
		Map visibleMap = state.getVisibleMap();
		for (Region region : visibleMap.getRegions()) {
			if (region.getPlayerName().equals(state.getMyPlayerName())) {
				// 1000 as initialization
				region.setDistanceToBorder(1000);
			} else {
				region.setDistanceToBorder(0);
			}
		}
		// Now do the real stuff
		boolean hasSomethingChanged = true;
		while (hasSomethingChanged) {
			hasSomethingChanged = false;
			for (Region region : state.getVisibleMap().getRegions()) {
				Region closestNeighbor = getClosestNeighborToBorder(state, region);
				if (closestNeighbor.getDistanceToBorder() < region.getDistanceToBorder()
						&& region.getDistanceToBorder() != closestNeighbor.getDistanceToBorder() + 1) {
					region.setDistanceToBorder(closestNeighbor.getDistanceToBorder() + 1);
					hasSomethingChanged = true;
				}
			}
		}
	}

	/**
	 * Calculates the closest neighbors to the border. The size of the list is 0
	 * if no neighbor is closer to the border and > 1 if more than one neighbor
	 * is the closest to the border. In this case the list is sorted according
	 * to the adjusted region values of the neighbors. If the distance of the
	 * closest neighbor to the border equals the distance of the region itself
	 * to the border then the closest neighbor is added to the list.
	 * 
	 * @param state
	 * @param inRegion
	 * @return
	 */
	public static List<Region> getClosestOwnedNeighborsToBorder(BotState state, Region inRegion) {
		int minDistance = inRegion.getDistanceToBorder();
		for (Region neighbor : inRegion.getOwnedNeighbors(state)) {
			if (neighbor.getDistanceToBorder() < minDistance) {
				minDistance = neighbor.getDistanceToBorder();
			}
		}
		List<Region> neighborsWithMinDistance = new ArrayList<>();
		for (Region neighbor : inRegion.getNeighbors()) {
			if(neighbor.getDistanceToBorder() == minDistance){
				neighborsWithMinDistance.add(neighbor);
			}
		}
		List<Region> out = new ArrayList<>();
		while(!neighborsWithMinDistance.isEmpty()){
			Region highestPriorityNeighbor = neighborsWithMinDistance.get(0);
			for(Region region : neighborsWithMinDistance){
				if(region.getAdjustedRegionValue(state) > highestPriorityNeighbor.getAdjustedRegionValue(state)){
					highestPriorityNeighbor = region;
				}
			}
			neighborsWithMinDistance.remove(highestPriorityNeighbor);
			out.add(highestPriorityNeighbor);
		}

		return out;
	}

	private static Region getClosestNeighborToBorder(BotState state, Region inRegion) {
		List<Region> neighbors = inRegion.getNeighbors();
		// In case two neighbors have the same distance pick the higher ranked
		// one
		Region closestNeighbor = inRegion;
		int minDistance = inRegion.getDistanceToBorder();
		for (Region neighbor : neighbors) {
			if (neighbor.getDistanceToBorder() < minDistance) {
				minDistance = neighbor.getDistanceToBorder();
			}
		}
		int maxValue = 0;
		for (Region neighbor : neighbors) {
			if (neighbor.getDistanceToBorder() == minDistance && neighbor.getAdjustedRegionValue(state) >= maxValue) {
				maxValue = neighbor.getAdjustedRegionValue(state);
				closestNeighbor = neighbor;
			}
		}
		return closestNeighbor;
	}

}
