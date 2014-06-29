package basicAlgorithms;

import java.util.ArrayList;
import java.util.List;

import main.Region;
import main.SuperRegion;
import move.AttackTransferMove;
import move.PlaceArmiesMove;
import bot.BotState;

/**
 * This class is responsible for calculating the ideal expansion for taking a
 * SuperRegion in one move.
 * 
 */
public class IdealExpansionCalculator {

	/**
	 * Calculates the ideal expansion to take the SuperRegion in one move.
	 * Returns null if this isn't possible or no solution was found. Probably
	 * won't find any solutions but better than nothing.
	 * 
	 * @param state
	 * @param expansionDeployment
	 * @param superRegion
	 */
	public static ExpansionDecisions calculateIdealExpansion(BotState state, SuperRegion superRegion) {
		ExpansionDecisions out = new ExpansionDecisions();
		List<Region> missingRegions = superRegion.getMissingRegions(state);
		/*
		 * If it's not possible for us to take the SuperRegion in one turn due
		 * to not all missing regions visible return null.
		 */
		for (Region missingRegion : missingRegions) {
			if (missingRegion.getOwnedNeighbors(state).size() == 0) {
				return null;
			}
		}
		/*
		 * If there are bordering owned regions with enough armies to take the
		 * region then choose that region which borders the least amount of
		 * missing regions. If there is no such region then add armies to the
		 * bordering region with the highest amount of idle armies and then let
		 * this region take the missing region.
		 */
		for (Region missingRegion : missingRegions) {
			Region bestNeighborRegion = getBestNeighborRegion(missingRegion, out, state, superRegion);
			int missingArmies = getMissingArmies(bestNeighborRegion, missingRegion, out);
			if (missingArmies > 0) {
				out.neededArmyPlacement = out.neededArmyPlacement + missingArmies;
				out.armyPlacement.add(new PlaceArmiesMove(state.getMyPlayerName(), bestNeighborRegion, missingArmies));
			}
			if (missingRegion.getArmies() == 1) {
				out.expansionMoves.add(new AttackTransferMove(state.getMyPlayerName(), bestNeighborRegion,
						missingRegion, 2));
			} else {
				out.expansionMoves.add(new AttackTransferMove(state.getMyPlayerName(), bestNeighborRegion,
						missingRegion, 3));
			}
		}
		return out;
	}

	/**
	 * Calculates the amount of idle armies still missing on the expandingRegion
	 * to take the toBeTakenRegion after some expansion decisions were already
	 * made. We expand with 3v2 and 2v1.
	 * 
	 * @param expandingRegion
	 * @param toBeTakenRegion
	 * @param madeExpansionDecisions
	 * @return
	 */
	private static int getMissingArmies(Region expandingRegion, Region toBeTakenRegion,
			ExpansionDecisions madeExpansionDecisions) {
		int idleArmies = getOverflowIdleArmies(expandingRegion, madeExpansionDecisions);
		int toBeTakenRegionArmies = toBeTakenRegion.getArmies();
		int neededArmies = toBeTakenRegionArmies + 1;
		if (idleArmies >= neededArmies) {
			return 0;
		} else {
			return neededArmies - idleArmies;
		}
	}

	/**
	 * Calculates the best owned neighbor region to take the missingRegion after
	 * the already made expansion decisions.
	 * 
	 * @param missingRegion
	 * @param madeExpansionDecisions
	 * @return
	 */
	private static Region getBestNeighborRegion(Region missingRegion, ExpansionDecisions madeExpansionDecisions,
			BotState state, SuperRegion superRegion) {
		List<Region> ownedNeighbors = missingRegion.getOwnedNeighbors(state);
		int maximumIdleArmies = 0;
		// First calculate the maximum amount of armies of an owned neighbor.
		for (Region ownedNeighbbor : ownedNeighbors) {
			int idleArmies = getOverflowIdleArmies(ownedNeighbbor, madeExpansionDecisions);
			if (idleArmies > maximumIdleArmies) {
				maximumIdleArmies = idleArmies;
			}
		}
		// Second calculate the owned neighbor having the maximum amount of idle
		// armies while having a minimum amount of sill missing neighbors.
		int minimumMissingNeighbors = 1000;
		Region out = null;
		for (Region ownedNeighbor : ownedNeighbors) {
			int missingNeighborRegions = getStillMissingNeighborRegions(ownedNeighbor, state, madeExpansionDecisions,
					superRegion).size();
			if (getOverflowIdleArmies(ownedNeighbor, madeExpansionDecisions) == maximumIdleArmies
					&& missingNeighborRegions < minimumMissingNeighbors) {
				out = ownedNeighbor;
				minimumMissingNeighbors = missingNeighborRegions;
			}
		}
		return out;
	}

	/**
	 * Calculates which neighbor regions from region are still missing after the
	 * already made expansion decisions.
	 * 
	 * @param region
	 * @param state
	 * @param madeExpansionDecisions
	 * @param superRegion
	 * @return
	 */
	private static List<Region> getStillMissingNeighborRegions(Region region, BotState state,
			ExpansionDecisions madeExpansionDecisions, SuperRegion superRegion) {
		List<Region> stillMissingRegions = getStillMissingRegions(state, madeExpansionDecisions, superRegion);
		List<Region> out = new ArrayList<>();
		for (Region neighbor : region.getNeighbors()) {
			if (stillMissingRegions.contains(neighbor)) {
				out.add(neighbor);
			}
		}

		return out;
	}

	/**
	 * Calculates the still missing regions in the SuperRegion after we made a
	 * couple expansion decisions.
	 * 
	 * @param state
	 * @param madeExpansionDecisions
	 * @return
	 */
	private static List<Region> getStillMissingRegions(BotState state, ExpansionDecisions madeExpansionDecisions,
			SuperRegion superRegion) {
		List<Region> out = superRegion.getMissingRegions(state);
		for (AttackTransferMove expansionMove : madeExpansionDecisions.expansionMoves) {
			if (out.contains(expansionMove.getToRegion())) {
				out.remove(expansionMove);
			}
		}
		return out;
	}

	/**
	 * Calculates the amount of still available idle armies on a region after
	 * some other expansion decisions were calculated.
	 * 
	 * @param region
	 * @param exppansionDecisions
	 * @return
	 */
	private static int getOverflowIdleArmies(Region region, ExpansionDecisions expansionDecisions) {
		int out = region.getIdleArmies();
		for (PlaceArmiesMove placeArmiesMove : expansionDecisions.armyPlacement) {
			if (placeArmiesMove.getRegion().equals(region)) {
				out = out + placeArmiesMove.getArmies();
			}
		}
		for (AttackTransferMove expansionMove : expansionDecisions.expansionMoves) {
			if (expansionMove.getFromRegion().equals(region)) {
				out = out - expansionMove.getArmies();
			}
		}
		return out;
	}

	public static class ExpansionDecisions {
		public int neededArmyPlacement = 0;
		public List<PlaceArmiesMove> armyPlacement = new ArrayList<>();
		public List<AttackTransferMove> expansionMoves = new ArrayList<>();
	}
}
