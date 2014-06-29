package strategy;

import java.util.ArrayList;
import java.util.List;

import evaluation.OpponentDeploymentGuesser;
import evaluation.RegionValueCalculator;

import strategy.NeededArmiesCalculator.NeededAttackArmies;
import strategy.NeededArmiesCalculator.NeededDefenseArmies;

import main.Region;
import model.HeuristicMapModel;
import move.AttackTransferMove;
import bot.BotState;

/**
 * This class is responsible for scheduling the AttackTransferMoves in a
 * meaningful way.
 * 
 */
public class MovesScheduler {

	/**
	 * Schedules the AttackTransferMoves. The Scheduling happens in two steps.
	 * At high level first all moves are classified and the moves are scheduled
	 * according to their classification. At low level the moves in a certain
	 * classification are also scheduled.
	 * 
	 * The classifications at high level are in following order: Needed support
	 * moves, Safe attack moves, Support Moves, Big expansion moves, Transfer
	 * Moves, ExpansionMoves , Risky attack moves, AttacksFromDefensiveSpot
	 * 
	 * According to some heuristics not all attacks from a defensive spot are
	 * inserted into AttacksFromDefensiveSpot.
	 * 
	 * @param in
	 * @param state
	 * @return
	 */
	public static List<AttackTransferMove> scheduleMoves(List<AttackTransferMove> in, BotState state) {
		List<AttackTransferMove> out = new ArrayList<>();
		List<AttackTransferMove> neededSupportMoves = new ArrayList<>();
		List<AttackTransferMove> safeAttackMoves = new ArrayList<>();
		List<AttackTransferMove> supportMoves = new ArrayList<>();
		List<AttackTransferMove> bigExpansionMoves = new ArrayList<>();
		List<AttackTransferMove> transferMoves = new ArrayList<>();
		List<AttackTransferMove> expansionMoves = new ArrayList<>();
		List<AttackTransferMove> riskyAttackMoves = new ArrayList<>();
		List<AttackTransferMove> attacksFromDefensiveSpot = new ArrayList<>();
		for (AttackTransferMove atm : in) {
			// attack to neutral
			if (atm.getToRegion().getPlayerName().equals("neutral")) {
				if (atm.getArmies() >= 7) {
					bigExpansionMoves.add(atm);
				} else {
					expansionMoves.add(atm);
				}
			}
			// transfer to own region
			if (atm.getToRegion().getPlayerName().equals(state.getMyPlayerName())) {
				if (atm.getToRegion().getEnemyNeighbors(state).size() == 0) {
					transferMoves.add(atm);
				} else {
					int missingArmies = getMissingRegionArmies(state, atm.getToRegion());
					if (missingArmies > 0) {
						neededSupportMoves.add(atm);
					} else {
						supportMoves.add(atm);
					}
				}
			}
			// attack opponent region
			if (atm.getToRegion().getPlayerName().equals(state.getOpponentPlayerName())) {
				if (isSafeAttack(state, atm) && !isCounterAttackPossible(state, atm)) {
					safeAttackMoves.add(atm);
				} else {
					riskyAttackMoves.add(atm);
				}
			}
		}
		// Move some attacks from defensive spots to attacksFromDefensiveSpot
		for (AttackTransferMove atm : in) {
			if (atm.getToRegion().equals(state.getOpponentPlayerName())) {
				int fromDefenceRegionValue = atm.getFromRegion().getDefenceRegionValue();
				boolean isHighPriorityRegion = fromDefenceRegionValue >= RegionValueCalculator.LOWEST_HIGH_PRIORITY_VALUE ? true
						: false;
				if (isHighPriorityRegion
						&& getMissingRegionArmies(state, atm.getFromRegion()) >= -state.getStartingArmies()) {
					safeAttackMoves.remove(atm);
					riskyAttackMoves.remove(atm);
					attacksFromDefensiveSpot.add(atm);
				}
			}
		}

		out.addAll(scheduleTransferMoves(state, neededSupportMoves));
		out.addAll(scheduleTransferMoves(state, safeAttackMoves));
		out.addAll(scheduleTransferMoves(state, supportMoves));
		out.addAll(scheduleAttackMoves(state, bigExpansionMoves));
		out.addAll(scheduleTransferMoves(state, transferMoves));
		out.addAll(scheduleAttackMoves(state, expansionMoves));
		out.addAll(scheduleAttackMoves(state, riskyAttackMoves));
		out.addAll(scheduleAttackMoves(state, attacksFromDefensiveSpot));

		return out;
	}

	private static List<AttackTransferMove> scheduleTransferMoves(BotState state, List<AttackTransferMove> transferMoves) {
		List<AttackTransferMove> out = new ArrayList<>();
		while (!transferMoves.isEmpty()) {
			// search for the highest priority of all transfer moves
			int highestPriority = 0;
			for (AttackTransferMove transferMove : transferMoves) {
				if (transferMove.getToRegion().getAdjustedRegionValue(state) > highestPriority) {
					highestPriority = transferMove.getToRegion().getAdjustedRegionValue(state);
				}
			}
			// search for the transfer move with highest priority having the
			// most moving armies
			// TODO debug 0
			// int mostMovingArmies = 0;
			int mostMovingArmies = 1;
			for (AttackTransferMove transferMove : transferMoves) {
				if (transferMove.getToRegion().getAdjustedRegionValue(state) == highestPriority
						&& transferMove.getArmies() >= mostMovingArmies) {
					mostMovingArmies = transferMove.getArmies();
				}
			}
			// search for the just identified transfer move
			AttackTransferMove mostImportantTransferMove = null;
			for (AttackTransferMove transferMove : transferMoves) {
				if (transferMove.getToRegion().getAdjustedRegionValue(state) == highestPriority
						&& transferMove.getArmies() == mostMovingArmies) {
					mostImportantTransferMove = transferMove;
				}
			}
			out.add(mostImportantTransferMove);
			transferMoves.remove(mostImportantTransferMove);
		}
		return out;
	}

	private static List<AttackTransferMove> scheduleAttackMoves(BotState state, List<AttackTransferMove> attackMoves) {
		List<AttackTransferMove> out = new ArrayList<>();
		while (!attackMoves.isEmpty()) {
			AttackTransferMove safestAttack = attackMoves.get(0);
			int highestDifference = safestAttack.getArmies()
					- (safestAttack.getToRegion().getArmies() + OpponentDeploymentGuesser.getGuessedOpponentDeployment(
							state, safestAttack.getToRegion()));
			for (AttackTransferMove attackMove : attackMoves) {
				int difference = attackMove.getArmies()
						- (attackMove.getToRegion().getArmies() + OpponentDeploymentGuesser
								.getGuessedOpponentDeployment(state, attackMove.getToRegion()));
				if (difference > highestDifference) {
					safestAttack = attackMove;
					highestDifference = safestAttack.getArmies() - safestAttack.getToRegion().getArmies();
				}
			}
			out.add(safestAttack);
			attackMoves.remove(safestAttack);
		}
		return out;
	}

	private static boolean isSafeAttack(BotState state, AttackTransferMove atm) {
		NeededAttackArmies naa = NeededArmiesCalculator.getNeededAttackArmies(atm.getToRegion());
		int neededSafeAttackArmies = naa.fairFightArmiesFullDeployment;
		int attackingArmies = atm.getArmies();
		return attackingArmies >= neededSafeAttackArmies ? true : false;
	}

	private static boolean isCounterAttackPossible(BotState state, AttackTransferMove ourAttack) {
		int armiesBeforeDeployment = ourAttack.getFromRegion().getArmies();
		int possibleOpponentAttackingArmies = ourAttack.getToRegion().getArmies()
				+ HeuristicMapModel.getGuessedOpponentIncome() - 1;
		if (possibleOpponentAttackingArmies * 0.6 >= armiesBeforeDeployment * 0.7) {
			return true;
		} else {
			return false;
		}
	}

	private static int getMissingRegionArmies(BotState state, Region ownedRegion) {
		NeededDefenseArmies nda = NeededArmiesCalculator.getNeededDefenseArmies(ownedRegion);
		int currentArmies = ownedRegion.getArmiesAfterDeployment();
		int neededArmies = nda.fairDefenceArmiesDeployment;
		return neededArmies - currentArmies;
	}
}
