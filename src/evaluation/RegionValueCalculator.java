package evaluation;

import java.util.ArrayList;
import java.util.List;

import main.Region;
import main.SuperRegion;
import model.HeuristicMapModel;
import bot.BotState;

/**
 * This class is responsible for calculating region values for each region. With
 * this information valid decisions can be made where to expand, which regions
 * to defend and which opponent regions to attack.
 */
public class RegionValueCalculator {

	public static final int LOWEST_MEDIUM_PRIORITY_VALUE = 30;
	public static final int LOWEST_HIGH_PRIORITY_VALUE = 1000;

	public static void calculateRegionValues(BotState state) {
		calculateExpansionRegionValues(state);
		calculateDefenceRegionValues(state);
		calculateAttackRegionValues(state);
	}

	public static List<Region> sortRegionsByExpansionRegionValue(BotState state, List<Region> in) {
		List<Region> out = new ArrayList<>();
		List<Region> copy = new ArrayList<>();
		copy.addAll(in);
		while (!copy.isEmpty()) {
			Region highestExpansionRegion = copy.get(0);
			for (Region region : copy) {
				if (region.getExpansionRegionValue() > highestExpansionRegion.getExpansionRegionValue()) {
					highestExpansionRegion = region;
				}
			}
			copy.remove(highestExpansionRegion);
			out.add(highestExpansionRegion);
		}
		return out;
	}

	public static List<Region> sortRegionsByDefenceRegionValue(BotState state, List<Region> in) {
		List<Region> out = new ArrayList<>();
		List<Region> copy = new ArrayList<>();
		copy.addAll(in);
		while (!copy.isEmpty()) {
			Region highestDefenceRegion = copy.get(0);
			for (Region region : copy) {
				if (region.getDefenceRegionValue() > highestDefenceRegion.getDefenceRegionValue()) {
					highestDefenceRegion = region;
				}
			}
			copy.remove(highestDefenceRegion);
			out.add(highestDefenceRegion);
		}
		return out;
	}

	public static List<Region> sortRegionsByAttackRegionValue(BotState state, List<Region> in) {
		List<Region> out = new ArrayList<>();
		List<Region> copy = new ArrayList<>();
		copy.addAll(in);
		while (!copy.isEmpty()) {
			Region highestAttackRegion = copy.get(0);
			for (Region region : copy) {
				if (region.getAttackRegionValue() > highestAttackRegion.getAttackRegionValue()) {
					highestAttackRegion = region;
				}
			}
			copy.remove(highestAttackRegion);
			out.add(highestAttackRegion);
		}
		return out;
	}

	public static List<Region> getOrderedListOfDefenceRegions(BotState state) {
		List<Region> out = new ArrayList<>();
		List<Region> opponentBorderingRegions = state.getVisibleMap().getOpponentBorderingRegions(state);
		List<Region> copy = new ArrayList<>();
		copy.addAll(opponentBorderingRegions);
		while (!copy.isEmpty()) {
			Region highestDefenceRegion = copy.get(0);
			for (Region region : copy) {
				if (region.getDefenceRegionValue() > highestDefenceRegion.getDefenceRegionValue()) {
					highestDefenceRegion = region;
				}
			}
			copy.remove(highestDefenceRegion);
			out.add(highestDefenceRegion);
		}

		return out;
	}

	public static List<Region> getOrderedListOfAttackRegions(BotState state) {
		List<Region> out = new ArrayList<>();
		List<Region> opponentRegions = state.getVisibleMap().getEnemyRegions(state);
		List<Region> copy = new ArrayList<>();
		copy.addAll(opponentRegions);
		while (!copy.isEmpty()) {
			Region highestAttackRegion = copy.get(0);
			for (Region region : copy) {
				if (region.getAttackRegionValue() > highestAttackRegion.getAttackRegionValue()) {
					highestAttackRegion = region;
				}
			}
			copy.remove(highestAttackRegion);
			out.add(highestAttackRegion);
		}

		return out;
	}

	private static List<Region> getOrderedListOfAttackRegionsBetween(BotState state, int lowValue, int highValue) {
		List<Region> orderedAttackRegions = getOrderedListOfAttackRegions(state);
		List<Region> out = new ArrayList<>();
		for (Region region : orderedAttackRegions) {
			if (region.getAttackRegionValue() >= lowValue && region.getAttackRegionValue() <= highValue) {
				out.add(region);
			}
		}
		return out;
	}

	private static List<Region> getOrderedListOfDefenceRegionsBetween(BotState state, int lowValue, int highValue) {
		List<Region> orderedDefenceRegions = getOrderedListOfDefenceRegions(state);
		List<Region> out = new ArrayList<>();
		for (Region region : orderedDefenceRegions) {
			if (region.getDefenceRegionValue() >= lowValue && region.getDefenceRegionValue() <= highValue) {
				out.add(region);
			}
		}
		return out;
	}

	public static List<Region> getOrderedListOfHighImportanceDefenceRegions(BotState state) {
		return getOrderedListOfDefenceRegionsBetween(state, LOWEST_HIGH_PRIORITY_VALUE, 1000000);
	}

	public static List<Region> getOrderedListOfMediumImportanceDefenceRegions(BotState state) {
		return getOrderedListOfDefenceRegionsBetween(state, LOWEST_MEDIUM_PRIORITY_VALUE,
				LOWEST_HIGH_PRIORITY_VALUE - 1);
	}

	public static List<Region> getOrderedListOfLowImportanceDefenceRegions(BotState state) {
		return getOrderedListOfDefenceRegionsBetween(state, 0, LOWEST_MEDIUM_PRIORITY_VALUE - 1);
	}

	public static List<Region> getOrderedListOfHighImportanceAttackRegions(BotState state) {
		return getOrderedListOfAttackRegionsBetween(state, LOWEST_HIGH_PRIORITY_VALUE, 1000000);
	}

	public static List<Region> getOrderedListOfMediumImportanceAttackRegions(BotState state) {
		return getOrderedListOfAttackRegionsBetween(state, LOWEST_MEDIUM_PRIORITY_VALUE, LOWEST_HIGH_PRIORITY_VALUE - 1);
	}

	public static List<Region> getOrderedListOfLowImportanceAttackRegions(BotState state) {
		return getOrderedListOfAttackRegionsBetween(state, 0, LOWEST_MEDIUM_PRIORITY_VALUE - 1);
	}

	private static void calculateExpansionRegionValues(BotState state) {
		for (Region region : state.getVisibleMap().getRegions()) {
			if (region.isNeutralRegion(state)) {
				// give each region +10 value for unknown neighbors within the
				// same
				// SuperRegion
				List<Region> unknownNeighborsWithinSameSuperRegion = region.getUnknownNeighborsWithinSameSuperRegion();
				region.setExpansionRegionValue(unknownNeighborsWithinSameSuperRegion.size() * 10);

				// add 1000 to the region value for each possible enemy
				// bordering
				// SuperRegion
				int enemySuperRegionNeighborAmount = getAmountOfPossibleEnemyOwnedSuperRegionNeighbors(region, state);
				region.setExpansionRegionValue(region.getExpansionRegionValue() + 1000 * enemySuperRegionNeighborAmount);

				// give each region +1 value for each non owned neighbor within
				// the same SuperRegion
				List<Region> neighbors = region.getNeighborsWithinSameSuperRegion();
				for (Region neighbor : neighbors) {
					if (neighbor.getPlayerName().equals("neutral")) {
						region.setExpansionRegionValue(region.getExpansionRegionValue() + 1);
					}
				}

			}
		}
	}

	private static void calculateAttackRegionValues(BotState state) {
		for (Region region : state.getVisibleMap().getRegions()) {
			if (region.getPlayerName().equals(state.getOpponentPlayerName())) {

				if (region.getSuperRegion().getId() == 6) {
					region.setAttackRegionValue(region.getAttackRegionValue() + 4);
				}
				if (region.getSuperRegion().getId() == 2) {
					region.setAttackRegionValue(region.getAttackRegionValue() + 3);
				}

				// add 100 to the region value for each possible enemy bordering
				// SuperRegion
				int enemySuperRegionNeighborAmount = getAmountOfGuessedOpponentOwnedSuperRegionNeighbors(region, state);
				region.setAttackRegionValue(region.getAttackRegionValue() + 100 * enemySuperRegionNeighborAmount);

				// add 100 to the region value for each own SuperRegion
				// bordering
				region.setAttackRegionValue(region.getAttackRegionValue() + 100
						* getAmountOfOwnedSuperRegionNeighbors(region, state));

				// add 10000 to the region value if it's a spot in an opponent
				// SuperRegion
				if (HeuristicMapModel.getGuessedOpponentSuperRegions().contains(region.getSuperRegion())) {
					region.setAttackRegionValue(region.getAttackRegionValue() + 10000);
				}
				// add 1 to the region value for each known non neutral spot in
				// the SuperRegion
				int nonNeutralSpots = getAmountOfKnownNonNeutralTerritories(region.getSuperRegion(), state);
				region.setAttackRegionValue(region.getAttackRegionValue() + nonNeutralSpots);

				// add +1 to the region value if the amount of non neutral
				// regions >= the amount of neutral regions in the SuperRegion
				int totalSpots = region.getSuperRegion().getSubRegions().size();
				if (nonNeutralSpots * 2 >= totalSpots) {
					region.setAttackRegionValue(region.getAttackRegionValue() + 1);
				}

				// add 1000 to the region value if it's the opponent last spot
				// in our own SuperRegion
				boolean weHaveRest = true;
				for (Region subregion : region.getSuperRegion().getSubRegions()) {
					if (!subregion.equals(region) && !subregion.getPlayerName().equals(state.getMyPlayerName())) {
						weHaveRest = false;
					}
				}
				if (weHaveRest) {
					region.setAttackRegionValue(region.getAttackRegionValue() + 1000);
				}

			}
		}
	}

	private static void calculateDefenceRegionValues(BotState state) {
		for (Region region : state.getVisibleMap().getRegions()) {
			if (region.getPlayerName().equals(state.getMyPlayerName()) && region.getEnemyNeighbors(state).size() > 0) {

				if (region.getSuperRegion().getId() == 6) {
					region.setDefenceRegionValue(region.getDefenceRegionValue() + 4);
				}

				if (region.getSuperRegion().getId() == 2) {
					region.setDefenceRegionValue(region.getDefenceRegionValue() + 3);
				}

				// add 1000 to the amount of the region if it's probably the
				// last spot in an enemy SuperRegion
				boolean opponentHasAll = true;
				List<Region> guessedOpponentSpots = HeuristicMapModel.getGuessedOpponentRegions();
				List<Region> subregions = region.getSuperRegion().getSubRegions();
				for (Region subregion : subregions) {
					if (!region.equals(subregion) && !guessedOpponentSpots.contains(subregion)) {
						opponentHasAll = false;
					}
				}
				if (opponentHasAll) {
					region.setDefenceRegionValue(region.getDefenceRegionValue() + 1000);
				}

				// add the rewarded armies * 1000 to the amount of the region if
				// the
				// region is owned by
				// myself and the SuperRegion is under attack
				if (region.getSuperRegion().isUnderAttack(state)) {
					region.setDefenceRegionValue(region.getDefenceRegionValue() + 1000
							* region.getSuperRegion().getArmiesReward());
				}
				// add 30 to the amount of the region for each own bordering
				// SuperRegion.
				region.setDefenceRegionValue(region.getDefenceRegionValue() + 30
						* getAmountOfOwnedSuperRegionNeighbors(region, state));
				// add 1 to the region value for each known non neutral spot in
				// the SuperRegion
				int nonNeutralSpots = getAmountOfKnownNonNeutralTerritories(region.getSuperRegion(), state);
				region.setDefenceRegionValue(region.getDefenceRegionValue() + nonNeutralSpots);

				// add +1 to the region value if the amount of non neutral
				// regions >= the amount of neutral regions in the SuperRegion
				int totalSpots = region.getSuperRegion().getSubRegions().size();
				if (nonNeutralSpots * 2 >= totalSpots) {
					region.setDefenceRegionValue(region.getDefenceRegionValue() + 1);
				}

			}
		}
	}

	private static int getAmountOfKnownNonNeutralTerritories(SuperRegion superRegion, BotState state) {
		List<Region> guessedOpponentSpots = HeuristicMapModel.getGuessedOpponentRegions();
		int nonNeutralSpots = 0;
		for (Region region : superRegion.getSubRegions()) {
			if (region.getPlayerName().equals(state.getMyPlayerName())) {
				nonNeutralSpots++;
			}
			if (guessedOpponentSpots.contains(region)) {
				nonNeutralSpots++;
			}
		}
		return nonNeutralSpots;
	}

	private static int getAmountOfOwnedSuperRegionNeighbors(Region region, BotState state) {
		List<SuperRegion> ownedSuperRegionNeighbors = new ArrayList<>();
		for (Region neighbor : region.getNeighbors()) {
			SuperRegion neighborSuperRegion = neighbor.getSuperRegion();
			if (neighborSuperRegion.isOwnedByMyself(state) && !ownedSuperRegionNeighbors.contains(neighborSuperRegion)
					&& !(region.getSuperRegion().equals(neighborSuperRegion))) {
				ownedSuperRegionNeighbors.add(neighborSuperRegion);
			}
		}
		return ownedSuperRegionNeighbors.size();
	}

	private static int getAmountOfGuessedOpponentOwnedSuperRegionNeighbors(Region region, BotState state) {
		List<SuperRegion> enemySuperRegionNeighbors = new ArrayList<>();
		for (Region neighbor : region.getNeighbors()) {
			SuperRegion neighborSuperRegion = neighbor.getSuperRegion();
			if (HeuristicMapModel.getGuessedOpponentSuperRegions().contains(neighborSuperRegion)
					&& !enemySuperRegionNeighbors.contains(neighborSuperRegion)) {
				enemySuperRegionNeighbors.add(neighborSuperRegion);

			}
		}
		return enemySuperRegionNeighbors.size();
	}

	private static int getAmountOfPossibleEnemyOwnedSuperRegionNeighbors(Region region, BotState state) {
		List<SuperRegion> enemySuperRegionNeighbors = new ArrayList<>();
		for (Region neighbor : region.getNeighbors()) {
			SuperRegion neighborSuperRegion = neighbor.getSuperRegion();
			if (neighborSuperRegion.isPossibleOwnedByEnemy(state)
					&& !enemySuperRegionNeighbors.contains(neighborSuperRegion)) {
				enemySuperRegionNeighbors.add(neighborSuperRegion);

			}
		}
		return enemySuperRegionNeighbors.size();
	}

}
