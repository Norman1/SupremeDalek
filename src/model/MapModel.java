package model;

import helpers.EnemyDeploymentReader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import main.Region;
import main.SuperRegion;
import bot.BotState;

/**
 * This class is responsible for giving information about what is going on
 * behind the fog. It's not possible that this class gives wrong information but
 * this comes at the cost that an opponent SuperRegion is only identified if
 * it's 100% clear that he has it.
 * 
 */
public class MapModel {
	/**
	 * All the regions that the opponent ever had according to our intel. Since
	 * each round the visible map contains other region objects we work with the
	 * region IDs.
	 */
	private static List<Integer> regionsOpponentHold = new ArrayList<>();

	private static List<Integer> superRegionsOpponentHas = new ArrayList<>();

	private static List<Integer> possibleOpponentSuperRegionsLastTurn = new ArrayList<>();
	// private static List<Integer> possibleOpponentSuperRegionsLastTurnBegin =
	// new ArrayList();

	/**
	 * Contains the ordered list of 6 picks that we went for.
	 */
	private static List<Integer> picks = new ArrayList<>();

	// private static BotState botState = null;

	/**
	 * Regions that we hold in the turn before we are calculating since the
	 * enemy can kick us out of one area in one turn.
	 */
	private static List<Integer> regionsThatWeHoldLastTurn = new ArrayList<>();

	public static List<SuperRegion> getKnownOpponentSuperRegions(BotState state) {
		List<SuperRegion> out = new ArrayList<>();
		for (SuperRegion superRegion : state.getVisibleMap().getSuperRegions()) {
			if (superRegionsOpponentHas.contains(superRegion.getId())) {
				out.add(superRegion);
			}
		}
		return out;
	}

	/**
	 * Retrieves the known opponent spots. These are the spots that the opponent
	 * ever hold minus the spots in our possession.
	 * 
	 * @param state
	 */
	public static List<Region> getKnownOpponentSpots(BotState state) {
		List<Region> out = new ArrayList<>();
		List<Region> ourSpots = state.getVisibleMap().getOwnedRegions(state);
		List<Integer> ourSpotsIDs = new ArrayList<>();
		for (Region region : ourSpots) {
			ourSpotsIDs.add(region.getId());
		}
		for (Integer opponentSpotID : regionsOpponentHold) {
			if (!ourSpotsIDs.contains(opponentSpotID)) {
				out.add(state.getVisibleMap().getRegion(opponentSpotID));
			}
		}
		return out;
	}

	private static List<Integer> getKnownOpponentSpotIDs(BotState state) {
		List<Integer> out = new ArrayList<>();
		List<Region> knownOpponentSpots = getKnownOpponentSpots(state);
		for (Region region : knownOpponentSpots) {
			out.add(region.getId());
		}
		return out;
	}

	/**
	 * Update the map model after the picks to calculate some of the enemy spots
	 * according to the picks that we lost.
	 * 
	 * @param state
	 */
	private static void updateMapModelAfterPicks(BotState state) {
		List<Region> ourSpots = state.getVisibleMap().getOwnedRegions(state);
		List<Integer> ourSpotsIDs = new ArrayList<>();
		for (Region region : ourSpots) {
			ourSpotsIDs.add(region.getId());
		}
		int pickPrefference = 1;
		for (int pick : picks) {
			if (pickPrefference < 4) {
				if (ourSpotsIDs.contains(pick)) {
					pickPrefference++;
				} else {
					if (!regionsOpponentHold.contains(pick)) {
						regionsOpponentHold.add(pick);
					}
				}
			}

		}
	}

	public static void storePicks(List<Integer> picks) {
		MapModel.picks = picks;
	}

	/**
	 * 
	 * @param state
	 */
	public static void updateMapModel(BotState state) {
		// mal schauen...
		// possibleOpponentSuperRegionsLastTurnBegin =
		// possibleOpponentSuperRegionsLastTurn;

		List<Region> opponentRegions = state.getVisibleMap().getEnemyRegions(state);
		for (Region enemyRegion : opponentRegions) {
			if (!regionsOpponentHold.contains(enemyRegion.getId())) {
				regionsOpponentHold.add(enemyRegion.getId());
			}
		}
		// Note that the opponent can also kick us out of one area in one turn.
		List<Region> ourSpots = state.getVisibleMap().getOwnedRegions(state);
		List<Integer> ourSpotsIDs = new ArrayList<>();
		for (Region region : ourSpots) {
			ourSpotsIDs.add(region.getId());
		}
		for (int regionIDThatWeHold : regionsThatWeHoldLastTurn) {
			if (!ourSpotsIDs.contains(regionIDThatWeHold) && !regionsOpponentHold.contains(regionIDThatWeHold)) {
				regionsOpponentHold.add(regionIDThatWeHold);
			}
		}
		// after the picks look at which we lost
		if (state.getRoundNumber() == 1) {
			updateMapModelAfterPicks(state);
		}
		// Store the regions that we hold this turn so we can access them next
		// turn.
		storeRegionsThatWeHold(state);
		calculateEnemySuperRegionsFromRegionInput(state);
		calculateEnemySuperRegionsFromDeploymentInput(state);
		// Store the possible opponent SuperRegions so we can access them next
		// turn to see what was possible this turn.
		List<SuperRegion> possibleOpponentSuperRegions = state.getVisibleMap().getPossibleEnemySuperRegions(state);
		possibleOpponentSuperRegionsLastTurn.clear();
		for (SuperRegion possibleSuperRegion : possibleOpponentSuperRegions) {
			possibleOpponentSuperRegionsLastTurn.add(possibleSuperRegion.getId());
		}
	}

	private static void storeRegionsThatWeHold(BotState state) {
		regionsThatWeHoldLastTurn.clear();
		List<Region> regions = state.getVisibleMap().getOwnedRegions(state);
		for (Region region : regions) {
			regionsThatWeHoldLastTurn.add(region.getId());
		}
	}

	private static void calculateEnemySuperRegionsFromDeploymentInput(BotState state) {
		List<Set<Integer>> allSuperRegionCombinations = new ArrayList<Set<Integer>>();
		for (int a = 1; a <= 6; a++) {
			for (int b = 1; b <= 6; b++) {
				for (int c = 1; c <= 6; c++) {
					for (int d = 1; d <= 6; d++) {
						for (int e = 1; e <= 6; e++) {
							for (int f = 1; f <= 6; f++) {
								Set<Integer> possibleCombination = new HashSet<>();
								possibleCombination.add(a);
								possibleCombination.add(b);
								possibleCombination.add(c);
								possibleCombination.add(d);
								possibleCombination.add(e);
								possibleCombination.add(f);
								allSuperRegionCombinations.add(possibleCombination);
							}
						}
					}
				}
			}
		}
		// remove those combinations that contain a SuperRegion that the enemy
		// can't have.
		// List<Integer> possibleEnemySuperRegionsLastTurn =
		// possibleOpponentSuperRegionsLastTurn;
		Set<Integer> possibleEnemySuperRegionIDs = new HashSet<Integer>();
		for (int superRegionID : possibleOpponentSuperRegionsLastTurn) {
			possibleEnemySuperRegionIDs.add(superRegionID);
		}
		List<Set<Integer>> possibleSuperRegionCombinations = new ArrayList<Set<Integer>>();
		for (Set<Integer> combination : allSuperRegionCombinations) {
			if (possibleEnemySuperRegionIDs.containsAll(combination)) {
				possibleSuperRegionCombinations.add(combination);
			}
		}
		int baseIncome = 5;
		// remove all possible super region combinations that don't give enough
		// armies to justify the opponents deployment.
		int enemyDeployment = EnemyDeploymentReader.getKnownEnemyDeploymentLastTurn();
		int totalIncomeFromSuperRegions = enemyDeployment - baseIncome;
		List<Set<Integer>> realPossibleCombinations = new ArrayList<Set<Integer>>();
		for (Set<Integer> combination : possibleSuperRegionCombinations) {
			int comboValue = 0;
			for (int superRegionID : combination) {
				SuperRegion superRegion = state.getVisibleMap().getSuperRegion(superRegionID);
				comboValue = comboValue + superRegion.getArmiesReward();
			}
			// only when the combo value justifies the enemy deployment add
			// this combo to the realPossibleCombinations
			if (comboValue >= totalIncomeFromSuperRegions) {
				realPossibleCombinations.add(combination);
			}
		}
		// If a certain SuperRegion appears in all real possible
		// combinations
		// then the enemy must have that SuperRegion.
		List<Integer> inpossibleSuperRegions = new ArrayList<>();
		for (SuperRegion superRegion : state.getVisibleMap().getSuperRegions()) {
			int superRegionID = superRegion.getId();
			boolean isEverywherePresent = true;
			for (Set<Integer> realPossibleCombination : realPossibleCombinations) {
				if (!realPossibleCombination.contains(superRegionID)) {
					isEverywherePresent = false;
				}
			}
			if (isEverywherePresent && realPossibleCombinations.size() > 0 && totalIncomeFromSuperRegions > 0) {
				SuperRegion foundOpponentSuperRegion = state.getVisibleMap().getSuperRegion(superRegionID);
				if (state.getVisibleMap().getPossibleEnemySuperRegions(state).contains(foundOpponentSuperRegion)) {
					superRegionsOpponentHas.add(superRegionID);
					System.err.println("Found out that he has SuperRegion: " + superRegionID);
				} else {
					System.err.println("Found out that he had last turn SuperRegion " + superRegionID
							+ " but we broke it");
					inpossibleSuperRegions.add(superRegionID);
				}
			}
		}

		// If the enemy has the SuperRegion then he must also have all
		// including
		// SubRegions. By adding this info to regionsOpponent hold we also
		// ensure that we don't lose the information about the found
		// SuperRegion. Since we might have broken the SuperRegion this turn be
		// careful.
		for (int superRegionID : superRegionsOpponentHas) {
			SuperRegion superRegion = state.getVisibleMap().getSuperRegion(superRegionID);
			List<Region> subregions = superRegion.getSubRegions();
			for (Region subregion : subregions) {
				if (!regionsOpponentHold.contains(subregion.getId())) {
					regionsOpponentHold.add(subregion.getId());
				}
			}
		}
		for (int superRegionID : inpossibleSuperRegions) {
			SuperRegion superRegion = state.getVisibleMap().getSuperRegion(superRegionID);
			List<Region> subregions = superRegion.getSubRegions();
			for (Region subregion : subregions) {
				if (!subregion.getPlayerName().equals(state.getMyPlayerName())
						&& !regionsOpponentHold.contains(subregion.getId())) {
					regionsOpponentHold.add(subregion.getId());
				}
			}
		}

	}

	/**
	 * Calculates the known opponent SuperRegions according to the intel we have
	 * about his regions. If he has all regions in a SuperRegion he must have
	 * that SuperRegion.
	 */
	private static void calculateEnemySuperRegionsFromRegionInput(BotState state) {
		superRegionsOpponentHas.clear();
		List<Integer> knownOpponentSpotsIDs = getKnownOpponentSpotIDs(state);
		for (SuperRegion superRegion : state.getVisibleMap().getSuperRegions()) {
			boolean hasAllSpots = true;
			for (Region region : superRegion.getSubRegions()) {
				int regionID = region.getId();
				if (!knownOpponentSpotsIDs.contains(regionID)) {
					hasAllSpots = false;
				}
			}
			if (hasAllSpots) {
				superRegionsOpponentHas.add(superRegion.getId());
			}
		}
	}

}
