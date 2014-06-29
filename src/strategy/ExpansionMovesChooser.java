package strategy;

import helpers.MovesPerformer;

import java.util.ArrayList;
import java.util.List;

import debug.IDNameMapper;

import evaluation.GameStateCalculator;
import evaluation.RegionValueCalculator;

import main.Region;
import main.SuperRegion;
import model.HeuristicMapModel;
import move.AttackTransferMove;
import move.PlaceArmiesMove;
import strategy.MovesChooser.Moves;
import basicAlgorithms.DistanceToOpponentBorderCalculator;
import basicAlgorithms.IdealExpansionCalculator;
import basicAlgorithms.IdealExpansionCalculator.ExpansionDecisions;
import bot.BotState;

/**
 * This class is responsible for expansion moves. Expanding only happens in
 * maximum one SuperRegion at a time. Killing neutrals in other SuperRegions is
 * also possible but not for expansion purpose.
 * 
 */
public class ExpansionMovesChooser {

	public static Moves getExpansionMoves(BotState state, int armiesForExpanding, boolean useAllArmies) {
		Moves out = new Moves();
		boolean isExpansionPossible = true;
		List<SuperRegion> expandableSuperRegions = MovesChooser.getOrderedListOfExpandableSuperRegions(state);
		if (expandableSuperRegions.size() == 0) {
			isExpansionPossible = false;
		}
		if (isExpansionPossible) {
			SuperRegion mostDesirableSuperRegion = expandableSuperRegions.get(0);
			Moves neededMovesToTakeInOneTurn = getNeededMovesToTakeSuperRegionInOneTurn(state,
					mostDesirableSuperRegion, armiesForExpanding);
			if (neededMovesToTakeInOneTurn != null) {
				// if we can take the SuperRegion in one turn then do it no
				// matter what
				out = neededMovesToTakeInOneTurn;
			} else {
				// if we can't take the SuperRegion in one turn then maybe we
				// don't want to perform an expansion step.
				boolean isExpandingInSuperRegionSmart = isExpandingInSuperRegionSmart(state, mostDesirableSuperRegion);

				if (isExpandingInSuperRegionSmart) {
					Moves neededMovesToKeepExpansionGoing = getNeededMovesToKeepExpansionGoing(state,
							mostDesirableSuperRegion);
					int neededArmiesToKeepExpansionGoing = neededMovesToKeepExpansionGoing.totalDeployment;
					if (armiesForExpanding >= neededArmiesToKeepExpansionGoing
							&& isPerformingThisSpecificMoveSmart(state, neededMovesToKeepExpansionGoing)) {
						out = neededMovesToKeepExpansionGoing;
					}

				}

			}
		}
		if (useAllArmies && HeuristicMapModel.getGuessedOpponentRegions().size() > 0) {
			MovesPerformer.performMoves(state, out);
			out = MovesChooser.mergeMoves(out, performMoveTowardsOpponent(state, out));
		} else {
			if (useAllArmies && out.attackTransferMoves.size() > 0) {
				out = performCleanupMoves(state, out);
			}
			MovesPerformer.performMoves(state, out);
		}
		return out;
	}

	public static Moves getRemainingExpansionMoves(BotState state) {
		Moves out = new Moves();
		List<Region> borderRegions = state.getVisibleMap().getBorderRegions(state);
		List<Region> valuableExpansionRegions = new ArrayList<>();
		for (Region borderRegion : borderRegions) {
			if (isExpandingValuable(state, borderRegion)) {
				valuableExpansionRegions.add(borderRegion);
			}
		}
		// Now that we found out which regions are good to make an expansion
		// move calculate the expansion moves.
		for (Region valuableExpansionRegion : valuableExpansionRegions) {
			List<Region> possibleToRegions = getNeighborsWithinMostDesirableSuperregion(valuableExpansionRegion, state);

			List<Region> orderedPossibleToRegions = RegionValueCalculator.sortRegionsByExpansionRegionValue(state,
					possibleToRegions);
			Region toRegion = orderedPossibleToRegions.get(0);
			if (valuableExpansionRegion.getIdleArmies() > 2
					|| (valuableExpansionRegion.getIdleArmies() == 2 && toRegion.getArmies() == 1)) {
				int valuableExpansionRegionIdleArmies = valuableExpansionRegion.getIdleArmies();
				AttackTransferMove move = new AttackTransferMove(state.getMyPlayerName(), valuableExpansionRegion,
						toRegion, valuableExpansionRegionIdleArmies);
				out.attackTransferMoves.add(move);
			}
		}
		MovesPerformer.performMoves(state, out);
		return out;
	}

	private static List<Region> getNeighborsWithinMostDesirableSuperregion(Region region, BotState state) {
		List<Region> nonOwnedNeighbors = getNonOwnedNeighbors(region, state);
		ArrayList<Region> out = new ArrayList<>();
		double biggestFraction = 0;
		for (Region neighbor : nonOwnedNeighbors) {
			if (neighbor.getSuperRegion().getOwnedFraction() > biggestFraction) {
				biggestFraction = neighbor.getSuperRegion().getOwnedFraction();
			}
		}
		for (Region neighbor : nonOwnedNeighbors) {
			if (neighbor.getSuperRegion().getOwnedFraction() == biggestFraction) {
				out.add(neighbor);
			}
		}
		return out;
	}

	/**
	 * Calculates whether it's valuable to make an expansion move from region.
	 * 
	 * @param state
	 * @param region
	 * @return
	 */
	private static boolean isExpandingValuable(BotState state, Region region) {
		boolean isValuable = true;
		// If there is an opponent in the SuperRegion where we want to expand
		// then don't expand
		SuperRegion desiredSuperRegion = getMostDesirableSuperRegionFromRegion(state, region);
		if (desiredSuperRegion.hasEnemyPresence(state)) {
			isValuable = false;
		}
		// If the SuperRegion isn't the overall most desirable SuperRegion, our
		// owned fraction is small and
		// the opponent is nearby then don't expand
		if (!desiredSuperRegion.getIsMostDesirableSuperregion() && region.getDistanceToOpponentBorder() < 4
				&& desiredSuperRegion.getOwnedFraction() < 0.5) {
			isValuable = false;
		}

		if (region.getEnemyNeighbors(state).size() != 0) {
			isValuable = false;
		}

		// neu, zu testen
		// if the opponent can get a good hit at the SuperRegion then expanding
		// isn't fine.
		for (Region neighborAndSubRegion : desiredSuperRegion.getNeighborAndSubregions()) {
			if (neighborAndSubRegion.getPlayerName().equals(state.getMyPlayerName())
					&& neighborAndSubRegion.getEnemyNeighbors(state).size() != 0) {
				int fairDefenceArmies = NeededArmiesCalculator.getNeededDefenseArmies(neighborAndSubRegion).fairDefenceArmiesDeployment;
				if (neighborAndSubRegion.getArmies() < fairDefenceArmies) {
					isValuable = false;
				}
			}
		}

		return isValuable;
	}

	/**
	 * Calculates which SuperRegion is the best spot to expand from from Region.
	 * 
	 * @param state
	 * @param region
	 * @return
	 */
	private static SuperRegion getMostDesirableSuperRegionFromRegion(BotState state, Region region) {
		List<Region> nonOwnedNeighbors = getNonOwnedNeighbors(region, state);
		double biggestFraction = 0;
		SuperRegion out = null;
		for (Region neighbor : nonOwnedNeighbors) {
			if (neighbor.getSuperRegion().getOwnedFraction() >= biggestFraction) {
				biggestFraction = neighbor.getSuperRegion().getOwnedFraction();
				out = neighbor.getSuperRegion();
			}
		}
		return out;
	}

	private static ArrayList<Region> getNonOwnedNeighbors(Region region, BotState state) {
		ArrayList<Region> out = new ArrayList<>();
		for (Region neighbor : region.getNeighbors()) {
			if (!neighbor.getPlayerName().equals(state.getMyPlayerName())) {
				out.add(neighbor);
			}
		}
		return out;
	}

	/**
	 * Calculates the expansion moves within the most desirable SuperRegion and
	 * with the restriction of the given armiesForExpanding. Returns empty moves
	 * if no expansion move can be performed.
	 * 
	 * @param state
	 * @param armiesForExpanding
	 * @return
	 */
	public static Moves getExpansionMovesX(BotState state, int armiesForExpanding) {
		boolean cleanupMovesRequired = false;
		if (state.getVisibleMap().getEnemyRegions(state).size() == 0) {
			cleanupMovesRequired = true;
		}
		// out is always empty at present implementation
		Moves out = new Moves();
		// If we can't expand then don't perform expansion moves
		List<SuperRegion> expandableSuperRegions = MovesChooser.getOrderedListOfExpandableSuperRegions(state);
		if (expandableSuperRegions.size() == 0) {
			return out;
		}
		SuperRegion mostDesirableSuperRegion = expandableSuperRegions.get(0);
		// If we can take the SuperRegion in one turn then do it.
		Moves neededMovesToTakeInOneTurn = getNeededMovesToTakeSuperRegionInOneTurn(state, mostDesirableSuperRegion,
				armiesForExpanding);
		if (neededMovesToTakeInOneTurn != null) {
			if (cleanupMovesRequired) {
				neededMovesToTakeInOneTurn = performCleanupMoves(state, neededMovesToTakeInOneTurn);
			}
			MovesPerformer.performMoves(state, neededMovesToTakeInOneTurn);
			return neededMovesToTakeInOneTurn;
		}
		// If expanding in the superRegion isn't smart then don't
		boolean isExpandingSmart = isExpandingInSuperRegionSmart(state, mostDesirableSuperRegion);
		System.err.println("isExpandingSmart in SuperRegion "
				+ IDNameMapper.getSuperRegionName(mostDesirableSuperRegion.getId()) + ": " + isExpandingSmart);
		if (!isExpandingSmart) {
			return out;
		}
		// If possible then perform one expansion move with our
		// armiesForExpanding within our most desirable SuperRegion.
		Moves neededMovesToKeepExpansionGoing = getNeededMovesToKeepExpansionGoing(state, mostDesirableSuperRegion);
		int neededArmiesToKeepExpansionGoing = neededMovesToKeepExpansionGoing.totalDeployment;
		boolean isExpandingValuable = false;
		if (mostDesirableSuperRegion.getOwnedFraction() > 0
				|| GameStateCalculator.calculateGameState(state).equals("stalemate")
				|| state.getVisibleMap().getEnemyRegions(state).size() == 0) {
			isExpandingValuable = true;
		}
		if (neededArmiesToKeepExpansionGoing > armiesForExpanding || !isExpandingValuable) {
			return out;
		} else {
			if (cleanupMovesRequired) {
				neededMovesToKeepExpansionGoing = performCleanupMoves(state, neededMovesToKeepExpansionGoing);
			}
			MovesPerformer.performMoves(state, neededMovesToKeepExpansionGoing);
			return neededMovesToKeepExpansionGoing;
		}
	}

	private static Moves performMoveTowardsOpponent(BotState state, Moves movesSoFar) {
		Moves out = new Moves();
		List<Region> knownOpponentSpots = HeuristicMapModel.getGuessedOpponentRegions();
		if (knownOpponentSpots.size() == 0) {
			System.err.println("knownOpponentSpots.size() == 0");
			return out;
		}
		int currentDeployment = movesSoFar.totalDeployment;
		int totalIncome = state.getStartingArmies();
		int missingDeployment = totalIncome - currentDeployment;
		List<Region> closestNeighbors = DistanceToOpponentBorderCalculator.getClosestRegionsToOpponentBorder(state);
		Region closestNeighborMaxIdleArmies = null;
		int maxIdleArmies = 0;
		for (Region closestNeighbor : closestNeighbors) {
			if (closestNeighbor.getIdleArmies() >= maxIdleArmies) {
				maxIdleArmies = closestNeighbor.getIdleArmies();
				closestNeighborMaxIdleArmies = closestNeighbor;
			}
		}
		if (closestNeighborMaxIdleArmies != null) {
			System.err.println("closestNeighborMaxIdleArmies: "
					+ IDNameMapper.getRegionName(closestNeighborMaxIdleArmies.getId()));
			int totalArmies = maxIdleArmies + missingDeployment;
			if (totalArmies >= 3) {
				List<Region> neighbors = closestNeighborMaxIdleArmies.getNeighbors();
				Region regionToAttack = neighbors.get(0);
				for (Region neighbor : neighbors) {
					if (neighbor.getDistanceToOpponentBorder() < regionToAttack.getDistanceToOpponentBorder()) {
						regionToAttack = neighbor;
					}
				}
				System.err.println("regionToAttack: " + IDNameMapper.getRegionName(regionToAttack.getId()));
				PlaceArmiesMove pam = new PlaceArmiesMove(state.getMyPlayerName(), closestNeighborMaxIdleArmies,
						missingDeployment);
				AttackTransferMove atm = new AttackTransferMove(state.getMyPlayerName(), closestNeighborMaxIdleArmies,
						regionToAttack, totalArmies);
				out.armyPlacementMoves.add(pam);
				out.attackTransferMoves.add(atm);
				System.err.println("pam.getArmies(): " + pam.getArmies());
				System.err.println("atm.getArmies(): " + atm.getArmies());
				out.totalDeployment += pam.getArmies();
			}
		}
		 MovesPerformer.performMoves(state, out);
		// TODO

		return out;
	}

	public static Moves performCleanupMoves(BotState state, Moves movesSoFar) {
		Moves out = movesSoFar;
		int currentDeployment = movesSoFar.totalDeployment;
		int totalIncome = state.getStartingArmies();
		int missingDeployment = totalIncome - currentDeployment;
		// add the missingDeployment to the region that performs the biggest
		// expansion move.
		AttackTransferMove biggestMove = movesSoFar.attackTransferMoves.get(0);
		if (missingDeployment > 0) {
			for (AttackTransferMove atm : movesSoFar.attackTransferMoves) {
				if (atm.getArmies() > biggestMove.getArmies()) {
					biggestMove = atm;
				}
			}
			PlaceArmiesMove pam = new PlaceArmiesMove(state.getMyPlayerName(), biggestMove.getFromRegion(),
					missingDeployment);
			out.armyPlacementMoves.add(pam);
			out.totalDeployment += missingDeployment;
		}
		return out;
	}

	/**
	 * Calculates the needed minimal effort moves to keep the expansion going
	 * (performing single expansion step with minimal effort.) in the specified
	 * SuperRegion. The SuperRegion has to allow expansion else undefined
	 * behavior. The deployment is enough to ensure 3v2 and 3v1 expansion. The
	 * attack transfer moves happen full force if no opponent bordering and else
	 * we attack the neutral region with 3 armies. This function isn't supposed
	 * to have side effects.
	 * 
	 * @param state
	 * @return
	 */
	public static Moves getNeededMovesToKeepExpansionGoing(BotState state, SuperRegion superRegion) {
		Moves out = new Moves();
		// Calculate the region with the highest amount of idle armies.
		Region biggestStackRegion = null;
		int biggestIdleStack = 0;
		for (Region neutralRegion : superRegion.getNeutralSubRegions(state)) {
			List<Region> ownedNeighbors = neutralRegion.getOwnedNeighbors(state);
			for (Region ownedNeighbor : ownedNeighbors) {
				if (ownedNeighbor.getIdleArmies() >= biggestIdleStack) {
					biggestIdleStack = ownedNeighbor.getIdleArmies();
					biggestStackRegion = ownedNeighbor;
				}
			}
		}
		// Calculate the needed deployment
		int idleArmies = biggestStackRegion.getIdleArmies();
		int neededExpansionDeployment = 3 - idleArmies;
		if (neededExpansionDeployment > 0) {
			PlaceArmiesMove deployment = new PlaceArmiesMove(state.getMyPlayerName(), biggestStackRegion,
					3 - idleArmies);
			out.armyPlacementMoves.add(deployment);
			out.totalDeployment = out.totalDeployment + (3 - idleArmies);
		} else {
			neededExpansionDeployment = 0;
		}
		// Calculate the best region to which we should perform an expansion
		// move.
		int highestExpansionRegionValue = 0;
		Region bestExpansionRegion = null;
		for (Region neighbor : biggestStackRegion.getNeighbors()) {
			if (superRegion.getSubRegions().contains(neighbor) && neighbor.isNeutralRegion(state)) {
				int expansionValue = neighbor.getExpansionRegionValue();
				if (expansionValue >= highestExpansionRegionValue) {
					highestExpansionRegionValue = expansionValue;
					bestExpansionRegion = neighbor;
				}

			}
		}
		// Calculate the amount of armies that should be used for the expansion
		// move.
		int usedArmies;
		if (biggestStackRegion.getDistanceToOpponentBorder() == 1) {
			usedArmies = 3;
		} else {
			usedArmies = biggestStackRegion.getIdleArmies() + neededExpansionDeployment;
		}
		// Perform the AttackTransferMove
		AttackTransferMove expansionMove = new AttackTransferMove(state.getMyPlayerName(), biggestStackRegion,
				bestExpansionRegion, usedArmies);
		out.attackTransferMoves.add(expansionMove);

		return out;
	}

	/**
	 * Calculates the minimal effort moves to take the superRegion within one
	 * turn. This functions isn't supposed to cause any side effects. May return
	 * null if taking the superRegion within one turn isn't possible with the
	 * given maximumArmiesToDeploy. Note that the expansion moves all happen 3v2
	 * and 3v1 regardless of the available armies or the opponent being present.
	 * 
	 * @param state
	 * @param superRegion
	 * @param maximumArmiesToDeploy
	 * @return
	 */
	private static Moves getNeededMovesToTakeSuperRegionInOneTurn(BotState state, SuperRegion superRegion,
			int maximumArmiesToDeploy) {
		Moves out = null;
		// Check whether it's possible to take the superRegion in one turn with
		// our maximumArmiesToDeploy.
		boolean canBeTakenInOneTurn = false;
		ExpansionDecisions ex = IdealExpansionCalculator.calculateIdealExpansion(state, superRegion);
		if (ex != null && ex.neededArmyPlacement <= maximumArmiesToDeploy) {
			canBeTakenInOneTurn = true;
		}
		// If the SuperRegion can be taken in one turn then store the moves in
		// out.
		if (canBeTakenInOneTurn) {
			out = new Moves();
			out.armyPlacementMoves = ex.armyPlacement;
			out.attackTransferMoves = ex.expansionMoves;
			out.totalDeployment = ex.neededArmyPlacement;
		}

		return out;
	}

	/**
	 * Created to prevent behavior from
	 * http://theaigames.com/competitions/warlight-ai-challenge
	 * /games/53612d564b5ab21cbdadc272
	 * 
	 * @param state
	 * @param expansionMoves
	 * @return
	 */
	private static boolean isPerformingThisSpecificMoveSmart(BotState state, Moves move) {
		if (GameStateCalculator.calculateGameState(state).equals("stalemate")) {
			return true;
		}
		AttackTransferMove atm = move.attackTransferMoves.get(0);
		int missingRegions = atm.getToRegion().getSuperRegion().getMissingRegions(state).size();
		Region fromRegion = atm.getFromRegion();
		if (fromRegion.getEnemyNeighbors(state).size() == 0 || missingRegions <= 3) {
			return true;
		} else {
			return false;
		}
	}

	private static boolean isExpandingInSuperRegionSmart(BotState state, SuperRegion superRegion) {
		boolean isExpandingSmart = true;
		// If there is maximum one territory to take then expanding is fine.
		if (superRegion.getMissingRegions(state).size() == 1) {
			return true;
		}
		// if we aren't fighting at present and have no intel about opponent
		// then expanding is fine
		if (HeuristicMapModel.getGuessedOpponentRegions().size() == 0) {
			return true;
		}

		// If the opponent can get a good attack with him deploying and us not
		// then expanding isn't fine
		for (Region subRegion : superRegion.getNeighborAndSubregions()) {
			if (subRegion.getPlayerName().equals(state.getMyPlayerName())
					&& subRegion.getEnemyNeighbors(state).size() != 0) {
				int fairDefenceArmies = NeededArmiesCalculator.getNeededDefenseArmies(subRegion).fairDefenceArmiesDeployment;
				if (subRegion.getArmies() < fairDefenceArmies) {
					isExpandingSmart = false;
				}
			}
		}

		if (superRegion.getOwnedFraction() == 0 && !GameStateCalculator.calculateGameState(state).equals("stalemate")
				&& state.getVisibleMap().getEnemyRegions(state).size() != 0) {
			isExpandingSmart = false;
		}

		// If we can crush a neighboring opponent SuperRegion then expanding
		// isn't smart
		List<SuperRegion> guessedOpponentSuperRegions = HeuristicMapModel.getGuessedOpponentSuperRegions();
		for (Region subRegion : superRegion.getSubRegions()) {
			if (subRegion.getPlayerName().equals(state.getMyPlayerName())
					&& subRegion.getEnemyNeighbors(state).size() != 0) {
				List<Region> opponentNeighbors = subRegion.getEnemyNeighbors(state);
				for (Region opponentNeighbor : opponentNeighbors) {
					SuperRegion neighborSuperRegion = opponentNeighbor.getSuperRegion();
					if (guessedOpponentSuperRegions.contains(neighborSuperRegion)) {
						int crushingArmies = NeededArmiesCalculator.getNeededAttackArmies(opponentNeighbor).crushingArmiesFullDeployment;
						int ourArmies = subRegion.getArmies() + state.getStartingArmies();
						if (ourArmies >= crushingArmies) {
							isExpandingSmart = false;
						}
					}
				}

			}
		}
		// If the SuperRegion takes long to take and we have the advantage then
		// expanding isn't smart
		String gameState = GameStateCalculator.calculateGameState(state);
		if (gameState.equals("won") || gameState.equals("timeWoringForUs")) {
			int missingRegions = superRegion.getMissingRegions(state).size();
			if (missingRegions > 3) {
				isExpandingSmart = false;
			}
		}
		
		// neu
		// If the SuperRegion takes super long to take and the game is open or lost then expanding isn't smart
		if(gameState.equals("open")|| gameState.equals("lost")){
			int missingRegions = superRegion.getMissingRegions(state).size();
			if (missingRegions > 6) {
				isExpandingSmart = false;
			}
		}
		
		return isExpandingSmart;
	}

}
