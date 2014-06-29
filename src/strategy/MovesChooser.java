package strategy;

import java.util.ArrayList;
import java.util.List;

import main.Region;
import main.SuperRegion;
import move.AttackTransferMove;
import move.PlaceArmiesMove;
import bot.BotState;

public class MovesChooser {

	private static Moves calculatedMoves = new Moves();

	public static void calculateMoves(BotState state) {
		Moves out = new Moves();
		int remainingArmies = state.getStartingArmies();
		int armiesForExpanding = calculateArmiesForExpanding(state);
		boolean useAllArmiiesForExpanding = state.getVisibleMap().getEnemyRegions(state).size() == 0 ? true : false;
		Moves expansionMoves = ExpansionMovesChooser.getExpansionMoves(state, armiesForExpanding,
				useAllArmiiesForExpanding);
		remainingArmies = remainingArmies - expansionMoves.totalDeployment;
		Moves fightMoves = new Moves();
		if (state.getRoundNumber() == 1) {
			fightMoves = FightingMovesChooserRound1.getFightingMovesRound1(state, remainingArmies);
		} else {
			fightMoves = FightingMovesChooser.getFightingMoves(state, remainingArmies);
		}
		remainingArmies -= fightMoves.totalDeployment;
		if (remainingArmies > 0) {
			List<PlaceArmiesMove> remainingDeployments = chooseExpansionDeploymentX(state, remainingArmies);
			expansionMoves.armyPlacementMoves.addAll(remainingDeployments);
		}
		useIdleExpansionArmies(state, expansionMoves);
		out = mergeMoves(out, expansionMoves);
		out = mergeMoves(out, fightMoves);
		out = mergeMoves(out, ExpansionMovesChooser.getRemainingExpansionMoves(state));
		out = mergeMoves(out, FightingMovesChooser.getLateFightingMoves(state));
		out = mergeMoves(out, TransferMovesChooser.getTransferMoves2(state));
		out = FightingMovesChooser.getCleanupMoves(state, out);

		out.attackTransferMoves = SameMovesJoiner.joinSameMoves(out.attackTransferMoves);
		out.attackTransferMoves = MovesScheduler.scheduleMoves(out.attackTransferMoves, state);
		out.armyPlacementMoves = DeploymentEmbelisher.embelishDeployment(out.armyPlacementMoves);
		calculatedMoves = out;
	}

	public static List<PlaceArmiesMove> getPlaceArmiesMoves() {
		return calculatedMoves.armyPlacementMoves;
	}

	public static List<AttackTransferMove> getAttackTransferMoves() {
		return calculatedMoves.attackTransferMoves;
	}

	private static void useIdleExpansionArmies(BotState state, Moves expansionMoves) {
		for (AttackTransferMove attackTransferMove : expansionMoves.attackTransferMoves) {
			int idleArmies = attackTransferMove.getFromRegion().getIdleArmies();
			int opponentNeighbors = attackTransferMove.getFromRegion().getEnemyNeighbors(state).size();
			if (idleArmies > 0 && opponentNeighbors == 0) {
				attackTransferMove.setArmies(attackTransferMove.getArmies() + idleArmies);
			}
		}
	}

	public static List<SuperRegion> getOrderedListOfExpandableSuperRegions(BotState state) {
		// First calculate the SuperRegions that allow expanding
		List<SuperRegion> expandableSuperRegions = state.getVisibleMap().getSuperRegions();
		List<SuperRegion> expandableSuperRegionsCopy = new ArrayList<>();
		expandableSuperRegionsCopy.addAll(expandableSuperRegions);
		for (SuperRegion superRegion : state.getVisibleMap().getSuperRegions()) {
			// Check whether we have the ability to access the SuperRegion
			boolean isAccessible = false;
			for (Region region : superRegion.getNeighborAndSubregions()) {
				if (region.getPlayerName().equals(state.getMyPlayerName())) {
					isAccessible = true;
				}
			}
			if (superRegion.isOwnedByMyself(state) || superRegion.hasEnemyPresence(state) || !isAccessible) {
				expandableSuperRegionsCopy.remove(superRegion);
			}
		}
		// Order the expandable SuperRegions reverse according to their size (so
		// that small SuperRegions appear first afterwards if same fraction)
		List<SuperRegion> firstSorted = new ArrayList<>();
		while (!expandableSuperRegionsCopy.isEmpty()) {
			int maxTerritories = expandableSuperRegionsCopy.get(0).getSubRegions().size();
			SuperRegion maxTerritoriesSuperRegion = expandableSuperRegionsCopy.get(0);
			for (SuperRegion expandableSuperRegion : expandableSuperRegionsCopy) {
				int territories = expandableSuperRegion.getSubRegions().size();
				if (territories > maxTerritories) {
					maxTerritories = territories;
					maxTerritoriesSuperRegion = expandableSuperRegion;
				}
			}
			firstSorted.add(maxTerritoriesSuperRegion);
			expandableSuperRegionsCopy.remove(maxTerritoriesSuperRegion);
		}

		// Order the expandable SuperRegions according to the owned fraction.
		List<SuperRegion> out = new ArrayList<>();
		while (!firstSorted.isEmpty()) {
			double biggestFraction = 0;
			SuperRegion biggestFractionSuperRegion = null;
			for (SuperRegion expandableSuperRegion : firstSorted) {
				if (expandableSuperRegion.getOwnedFraction() >= biggestFraction) {
					biggestFraction = expandableSuperRegion.getOwnedFraction();
					biggestFractionSuperRegion = expandableSuperRegion;
				}
			}
			firstSorted.remove(biggestFractionSuperRegion);
			out.add(biggestFractionSuperRegion);
		}

		return out;
	}

	public static int calculateArmiesForExpanding(BotState state) {
		int availableArmies = state.getStartingArmies();
		// If we don't border the opponent put all armies into expanding
		if (state.getVisibleMap().getEnemyRegions(state).size() == 0) {
			return availableArmies;
		}
		// In case we can't expand don't use armies for expanding
		List<SuperRegion> bestExpandableSuperRegions = getOrderedListOfExpandableSuperRegions(state);
		if (bestExpandableSuperRegions.size() == 0) {
			return 0;
		}
		// Else spare the recommended expansion deployment for expansion
		return ExpansionFightPrioriser.getRecommendedExpansionDeployment(state);
	}

	public static Moves mergeMoves(Moves a, Moves b) {
		Moves out = new Moves();
		out.totalDeployment = a.totalDeployment + b.totalDeployment;
		out.armyPlacementMoves.addAll(a.armyPlacementMoves);
		out.armyPlacementMoves.addAll(b.armyPlacementMoves);
		out.attackTransferMoves.addAll(a.attackTransferMoves);
		out.attackTransferMoves.addAll(b.attackTransferMoves);
		return out;
	}

	private static List<PlaceArmiesMove> chooseExpansionDeploymentX(BotState state, int armiesToDeploy) {
		List<PlaceArmiesMove> expansionDeployments = new ArrayList<>();
		// mostDesirableSuperRegion can be null if no SuperRegion partially
		// owned
		SuperRegion mostDesirableSuperRegion = state.getVisibleMap().getMostDesirableSuperRegion();
		boolean nonExistantMostDesirableSuperRegion = false;
		if (mostDesirableSuperRegion == null) {
			nonExistantMostDesirableSuperRegion = true;
			for (SuperRegion superRegion : state.getVisibleMap().getSuperRegions()) {
				if (superRegion.getOwnedFraction() > 0) {
					mostDesirableSuperRegion = superRegion;
				}
			}
		}
		Region bestDeploymentSpot = null;
		if (!nonExistantMostDesirableSuperRegion) {
			int maximumIdleArmies = 0;
			List<Region> neutralOrEnemySubRegions = mostDesirableSuperRegion.getNeutralOrEnemySubRegions(state);
			for (Region neutralOrEnemySubRegion : neutralOrEnemySubRegions) {
				for (Region neighbor : neutralOrEnemySubRegion.getOwnedNeighbors(state)) {
					int idleArmies = neighbor.getIdleArmies();
					if (idleArmies >= maximumIdleArmies) {
						maximumIdleArmies = idleArmies;
						bestDeploymentSpot = neighbor;
					}
				}
			}
		} else {
			// Just choose a random region at a border
			for (Region region : state.getVisibleMap().getRegions()) {
				if (region.getPlayerName().equals(state.getMyPlayerName()) && region.getDistanceToBorder() == 1) {
					bestDeploymentSpot = region;
				}
			}
		}
		int opponentBorderingRegions = state.getVisibleMap().getOpponentBorderingRegions(state).size();
		if (bestDeploymentSpot != null
				&& (!bestDeploymentSpot.getSuperRegion().hasEnemyPresence(state) || opponentBorderingRegions == 0)) {
			PlaceArmiesMove pam = new PlaceArmiesMove(state.getMyPlayerName(), bestDeploymentSpot, armiesToDeploy);
			expansionDeployments.add(pam);
			bestDeploymentSpot.addArmyPlacement(pam);
		}
		return expansionDeployments;
	}

	public static class Moves {
		public int totalDeployment = 0;
		public List<PlaceArmiesMove> armyPlacementMoves = new ArrayList<>();
		public List<AttackTransferMove> attackTransferMoves = new ArrayList<>();
	}
}
