package evaluation;

import java.util.List;

import main.Region;
import main.SuperRegion;
import model.HeuristicMapModel;
import strategy.NeededArmiesCalculator;
import strategy.NeededArmiesCalculator.NeededAttackArmies;
import strategy.NeededArmiesCalculator.NeededDefenseArmies;
import bot.BotState;
import evaluation.RegionAndSuperRegionAnnotator.RegionAnnotations;
import evaluation.RegionAndSuperRegionAnnotator.SuperRegionAnnotations;

/**
 * This class is responsible for calculating the state of the game.
 */
public class GameStateCalculator {

	public static String calculateGameState2(BotState state) {
		String out = "";
		int ownIncome = state.getStartingArmies();
		int opponentIncome = HeuristicMapModel.getGuessedOpponentIncome();
		if (ownIncome > opponentIncome) {
			out = "won";
		}
		if (ownIncome < opponentIncome) {
			out = "lost";
		}
		if (ownIncome == opponentIncome) {

		}
		return out;
	}

	public static boolean isStalemateSituation2(BotState state) {
		boolean isStalemateSituation = true;

		// No stalemate situation with income difference
		if (HeuristicMapModel.getGuessedOpponentIncome() != state.getStartingArmies()) {
			isStalemateSituation = false;
		}

		// No stalemate situation without seeing the opponent
		if (state.getVisibleMap().getEnemyRegions(state).size() == 0) {
			isStalemateSituation = false;
		}
		// No stalemate when we can attack an important opponent spot without
		// him deploying.
		List<NeededAttackArmies> neededAttackArmiesList = NeededArmiesCalculator.neededAttackArmiesList;
		for (NeededAttackArmies nAA : neededAttackArmiesList) {
			Region attackRegion = nAA.region;
			int maximumAttackArmies = attackRegion.getOwnSurroundingPossibleAttackArmies(state);
			if (maximumAttackArmies > nAA.fairFightArmiesNoDeployment
					&& !attackRegion.getAnnotations().contains(RegionAnnotations.UNIMPORTANT_SPOT)) {
				isStalemateSituation = false;
			}
		}

		// No stalemate when the opponent can attack an important spot without
		// us deploying.
		List<NeededDefenseArmies> neededDefenseArmiesList = NeededArmiesCalculator.neededDefenseArmiesList;
		for (NeededDefenseArmies nDA : neededDefenseArmiesList) {
			Region defenceRegion = nDA.region;
			if (defenceRegion.getArmies() < nDA.fairDefenceArmiesDeployment
					&& !defenceRegion.getAnnotations().contains(RegionAnnotations.UNIMPORTANT_SPOT)) {
				isStalemateSituation = false;
			}
		}
		// No stalemate situaion when we have the opportunity to clear an
		// opponent from a SuperRegion with <= 4 neutrals
		for (SuperRegion superRegion : state.getVisibleMap().getSuperRegions()) {
			int neutrals = calculateNeutralsInSuperRegion(state, superRegion);
			if ((superRegion.getAnnotations().contains(SuperRegionAnnotations.CAN_PUSH_OPPONENT_OUT) || superRegion
					.getAnnotations().contains(SuperRegionAnnotations.CAN_BE_TAKEN)) && neutrals <= 4) {
				boolean clearingEverythingPossible = true;
				for (Region subRegion : superRegion.getSubRegions()) {
					NeededAttackArmies nAA = NeededArmiesCalculator.getNeededAttackArmies(subRegion);

					int maximumAttackArmies = subRegion.getOwnSurroundingPossibleAttackArmies(state);
					if (maximumAttackArmies > nAA.fairFightArmiesNoDeployment) {
						clearingEverythingPossible = false;
					}
				}
				if (clearingEverythingPossible) {
					isStalemateSituation = false;
				}
			}
		}
		// No stalemate situation when the opponent has to opportunity to clear
		// us from a SuperRegion with <= 4 neutrals
		for (SuperRegion superRegion : state.getVisibleMap().getSuperRegions()) {
			int neutrals = calculateNeutralsInSuperRegion(state, superRegion);
			if ((superRegion.getAnnotations().contains(SuperRegionAnnotations.CAN_GET_PUSHED_OUT) || superRegion
					.getAnnotations().contains(SuperRegionAnnotations.CAN_BE_TAKEN_BY_OPPONENT)) && neutrals <= 4) {
				boolean clearingEverythingPossible = true;
				for (Region subRegion : superRegion.getSubRegions()) {
					NeededDefenseArmies nda = NeededArmiesCalculator.getNeededDefenseArmies(subRegion);

					if (subRegion.getArmies() < nda.fairDefenceArmiesDeployment) {
						clearingEverythingPossible = false;
					}

				}
				if (clearingEverythingPossible) {
					isStalemateSituation = false;
				}
			}
		}
		return isStalemateSituation;
	}

	private static int calculateNeutralsInSuperRegion(BotState state, SuperRegion superRegion) {
		int out = 0;
		for (Region subRegion : superRegion.getSubRegions()) {
			if (subRegion.getPlayerName().equals("neutral")) {
				out++;
			} else if (subRegion.getPlayerName().equals("unknown")
					&& !HeuristicMapModel.getGuessedOpponentRegions().contains(subRegion)) {
				out++;
			}
		}
		return out;
	}

	public static String calculateGameState(BotState state) {
		// That we don't overestimate our situation at the beginning of the
		// game.
		if (HeuristicMapModel.getGuessedOpponentIncome() == 5 && state.getStartingArmies() == 5
				&& calculateOwnArmies(state) > guessOpponentArmies(state) && !isStalemateSituation(state)) {
			return "open";
		}
		// Do the real calculations
		String gameState = "";
		int ownArmies = calculateOwnArmies(state);
		int opponentArmies = guessOpponentArmies(state);
		int ownIncome = state.getStartingArmies();
		int opponentIncome = HeuristicMapModel.getGuessedOpponentIncome();
		if (ownIncome > opponentIncome) {
			if (ownArmies >= opponentArmies) {
				gameState = "won";
			} else {
				gameState = "timeWorkingForUs";
			}
		} else if (ownIncome == opponentIncome) {
			if (isStalemateSituation(state)) {
				gameState = "stalemate";
			} else if (ownArmies > opponentArmies + 5) {
				gameState = "armiesAdvantage";
			} else if (ownArmies + 5 < opponentArmies) {
				gameState = "armiesDisadvantage";
			} else {
				gameState = "open";
			}
		} else if (ownIncome < opponentIncome) {
			if (opponentArmies >= ownArmies) {
				gameState = "lost";
			} else {
				gameState = "timeWorkingAgainstUs";
			}
		}
		return gameState;
	}

	/**
	 * Checks whether there is a stalemate situation. This is the case if both
	 * players have the same income and none of them can perform an attack move
	 * with an even fight even with the other player not deploying at all to
	 * that spot.
	 * 
	 * @param state
	 * @return
	 */
	private static boolean isStalemateSituation(BotState state) {
		boolean isStalemateSituation = true;
		int ownIncome = state.getStartingArmies();
		int opponentIncome = HeuristicMapModel.getGuessedOpponentIncome();
		// No stalemate situation without seeing the opponent
		if (state.getVisibleMap().getEnemyRegions(state).size() == 0) {
			isStalemateSituation = false;
		}

		// No stalemate with income difference
		if (ownIncome != opponentIncome) {
			isStalemateSituation = false;
		}
		// No stalemate when the opponent can attack us without us deploying.
		List<NeededDefenseArmies> neededDefenseArmiesList = NeededArmiesCalculator.neededDefenseArmiesList;
		for (NeededDefenseArmies nDA : neededDefenseArmiesList) {
			Region defenceRegion = nDA.region;
			if (defenceRegion.getArmies() < nDA.fairDefenceArmiesDeployment) {
				isStalemateSituation = false;
			}
		}
		// No stalemate when we can attack the opponent without him deploying.
		List<NeededAttackArmies> neededAttackArmiesList = NeededArmiesCalculator.neededAttackArmiesList;
		for (NeededAttackArmies nAA : neededAttackArmiesList) {
			Region attackRegion = nAA.region;
			int maximumAttackArmies = attackRegion.getOwnSurroundingPossibleAttackArmies(state);
			if (maximumAttackArmies > nAA.fairFightArmiesNoDeployment) {
				isStalemateSituation = false;
			}
		}

		return isStalemateSituation;
	}

	private static int calculateOwnArmies(BotState state) {
		int armies = 0;
		for (Region ownedRegion : state.getVisibleMap().getOwnedRegions(state)) {
			armies += ownedRegion.getArmies();
		}
		return armies;
	}

	private static int guessOpponentArmies(BotState state) {
		int opponentArmies = 0;
		for (Region opponentRegion : state.getVisibleMap().getEnemyRegions(state)) {
			opponentArmies += opponentRegion.getArmies();
		}
		List<Region> guessedOppoentRegions = HeuristicMapModel.getGuessedOpponentRegions();
		for (Region guessedOpponentRegion : guessedOppoentRegions) {
			if (!state.getVisibleMap().getEnemyRegions(state).contains(guessedOpponentRegion)) {
				opponentArmies += 1;
			}
		}
		return opponentArmies;
	}

}
