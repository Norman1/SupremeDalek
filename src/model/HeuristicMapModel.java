package model;

import helpers.EnemyDeploymentReader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import debug.IDNameMapper;

import main.Region;
import main.SuperRegion;
import bot.BotState;

/**
 * This class is responsible for giving heuristic information about the board.
 * It's possible that the algorithms in this class falsely assume the opponent
 * has a certain SuperRegion and on the other hand it's also possible that the
 * algorithms aren't able to identify a certain SuperRegion.
 */
public class HeuristicMapModel {

	private static List<Integer> possibleSuperRegionsLastTurnIDs = new ArrayList<>();
	private static List<SuperRegion> guessedSuperRegions = new ArrayList<>();
	private static Set<Region> guessedRegions = new HashSet<>();

	public static List<SuperRegion> getGuessedOpponentSuperRegions() {
		return guessedSuperRegions;
	}

	public static int getGuessedOpponentIncome() {
		int out = 5;
		for (SuperRegion guessedSuperRegion : guessedSuperRegions) {
			out += guessedSuperRegion.getArmiesReward();
		}
		return out;
	}

	public static List<Region> getGuessedOpponentRegions() {
		List<Region> out = new ArrayList<>();
		out.addAll(guessedRegions);
		return out;
	}

	/**
	 * Guesses the SuperRegions the opponent has according to following
	 * parameters:
	 * 
	 * Known opponent regions.
	 * 
	 * Opponents visible deployment.
	 * 
	 * Known opponent SuperRegions.
	 */
	public static void guessOpponentSuperRegions(BotState state) {
		// Update the references so that the guessed SuperRegions are new
		// Objects
		List<SuperRegion> guessedSuperRegionsCopy = new ArrayList<>();
		for (SuperRegion guessedSuperRegion : guessedSuperRegions) {
			guessedSuperRegionsCopy.add(state.getVisibleMap().getSuperRegion(guessedSuperRegion.getId()));
		}
		guessedSuperRegions.clear();
		guessedSuperRegions.addAll(guessedSuperRegionsCopy);

		List<SuperRegion> possibleSuperRegionsLastTurn = new ArrayList<>();
		for (int superRegionID : possibleSuperRegionsLastTurnIDs) {
			possibleSuperRegionsLastTurn.add(state.getVisibleMap().getSuperRegion(superRegionID));
		}
		// Debug
		// List<SuperRegion> possibleLastTurn =
		// MapModel.getPossibleOpponentSuperRegionsLastTurn(state);
		System.err.print("Possible super regions last turn: ");
		for (int i : possibleSuperRegionsLastTurnIDs) {
			System.err.print(IDNameMapper.getSuperRegionName(i) + ", ");
		}
		System.err.println();
		// end Debug
		List<SuperRegion> removedSuperRegions = removeInpossibleSuperRegionsLastTurn(state,
				possibleSuperRegionsLastTurn);
		addKnownSuperRegions(state);
		int opponentIncomeLastTurn = EnemyDeploymentReader.getKnownEnemyDeploymentLastTurn();
		// Calculate the known opponent income from his known SuperRegions
		int knownOpponentIncome = 5;
		// List<SuperRegion> knownOpponentSuperRegions =
		// MapModel.getKnownOpponentSuperRegions(state);
		// new
		for (SuperRegion knownSuperRegion : guessedSuperRegions) {
			knownOpponentIncome += knownSuperRegion.getArmiesReward();
		}
		for (SuperRegion removedSuperRegion : removedSuperRegions) {
			// hack... let's see
			knownOpponentIncome += removedSuperRegion.getArmiesReward();
		}
		int missingIncome = opponentIncomeLastTurn - knownOpponentIncome;
		boolean hasSomethingChanged = true;
		while (missingIncome > 0 && hasSomethingChanged) {
			hasSomethingChanged = false;
			// Guess that the missingIncome comes from the smallest SuperRegion
			// where we know the the opponent has some regions owned.
			SuperRegion smallestKnownSpotsSuperRegion = getSmallestSuperRegionWithKnownOpponentSpots(state,
					possibleSuperRegionsLastTurn);
			SuperRegion smallestPossibleSuperRegion = getSmallestMissingPossibleSuperRegion(state,
					possibleSuperRegionsLastTurn);
			if (smallestKnownSpotsSuperRegion != null) {
				guessedSuperRegions.add(smallestKnownSpotsSuperRegion);
				missingIncome = missingIncome - smallestKnownSpotsSuperRegion.getArmiesReward();
				hasSomethingChanged = true;
			} else if (smallestPossibleSuperRegion != null) {
				guessedSuperRegions.add(smallestPossibleSuperRegion);
				missingIncome = missingIncome - smallestPossibleSuperRegion.getArmiesReward();
				hasSomethingChanged = true;
			}
		}
		// Remove all guessed SuperRegions that aren't possible anymore with the
		// intel of the current turn.
		List<SuperRegion> inpossibleSuperRegions = new ArrayList<>();
		for (SuperRegion superRegion : guessedSuperRegions) {
			if (!state.getVisibleMap().getPossibleEnemySuperRegions(state).contains(superRegion)) {
				inpossibleSuperRegions.add(superRegion);
			}
		}
		guessedSuperRegions.removeAll(inpossibleSuperRegions);
		guessOpponentRegions(state);
		// Store for the next round
		List<SuperRegion> possibleOpponentSuperRegions = state.getVisibleMap().getPossibleEnemySuperRegions(state);
		possibleSuperRegionsLastTurnIDs.clear();
		for (SuperRegion possibleSuperRegion : possibleOpponentSuperRegions) {
			possibleSuperRegionsLastTurnIDs.add(possibleSuperRegion.getId());
		}
	}

	private static void guessOpponentRegions(BotState state) {
		guessedRegions.clear();
		guessedRegions.addAll(MapModel.getKnownOpponentSpots(state));
		for (SuperRegion guessedSuperRegion : guessedSuperRegions) {
			guessedRegions.addAll(guessedSuperRegion.getSubRegions());
		}
	}

	/**
	 * Calculates the smallest possible SuperRegion the opponent may have with
	 * the constraint that we know that he has owned regions in that
	 * SuperRegion.
	 * 
	 * @param state
	 * @return
	 */
	private static SuperRegion getSmallestSuperRegionWithKnownOpponentSpots(BotState state,
			List<SuperRegion> possibleSuperRegionsLastTurn) {
		SuperRegion out = null;
		int smallestarmiesReward = 1000;
		for (SuperRegion superRegion : state.getVisibleMap().getSuperRegions()) {
			if (possibleSuperRegionsLastTurn.contains(superRegion)
					&& superRegion.getArmiesReward() < smallestarmiesReward
					&& !guessedSuperRegions.contains(superRegion)) {
				boolean hasKnownOpponentspots = false;
				for (Region region : superRegion.getSubRegions()) {
					if (MapModel.getKnownOpponentSpots(state).contains(region)) {
						hasKnownOpponentspots = true;
					}
				}
				if (hasKnownOpponentspots) {
					smallestarmiesReward = superRegion.getArmiesReward();
					out = superRegion;
				}
			}
		}
		return out;
	}

	/**
	 * Calculates the SuperRegion with the least armies reward that the opponent
	 * could possible have and that we haven't considered yet.
	 * 
	 * @param state
	 * @return
	 */
	private static SuperRegion getSmallestMissingPossibleSuperRegion(BotState state,
			List<SuperRegion> possibleSuperRegionsLastTurn) {
		int smallestarmiesReward = 1000;
		SuperRegion guessedSuperRegion = null;
		for (SuperRegion superRegion : state.getVisibleMap().getSuperRegions()) {
			if (possibleSuperRegionsLastTurn.contains(superRegion)
					&& superRegion.getArmiesReward() < smallestarmiesReward
					&& !guessedSuperRegions.contains(superRegion)) {
				smallestarmiesReward = superRegion.getArmiesReward();
				guessedSuperRegion = superRegion;
			}
		}
		return guessedSuperRegion;
	}

	private static void addKnownSuperRegions(BotState state) {
		List<SuperRegion> knownOpponentSuperRegions = MapModel.getKnownOpponentSuperRegions(state);
		for (SuperRegion knownSuperRegion : knownOpponentSuperRegions) {
			if (!guessedSuperRegions.contains(knownSuperRegion)) {
				guessedSuperRegions.add(knownSuperRegion);
			}
		}
	}

	private static List<SuperRegion> removeInpossibleSuperRegionsLastTurn(BotState state,
			List<SuperRegion> possibleSuperRegionsLastTurn) {
		List<SuperRegion> superRegionsToRemove = new ArrayList<>();
		for (SuperRegion superRegion : guessedSuperRegions) {
			if (!possibleSuperRegionsLastTurn.contains(superRegion)) {
				System.err.println("Removed SuperRegion: "
						+ IDNameMapper.getSuperRegionName(superRegion.getArmiesReward()));
				superRegionsToRemove.add(superRegion);
			}
		}
		guessedSuperRegions.removeAll(superRegionsToRemove);
		return superRegionsToRemove;
	}

}
