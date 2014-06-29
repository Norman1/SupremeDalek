package strategy;

import java.util.List;

import main.SuperRegion;
import bot.BotState;
import evaluation.GameStateCalculator;

public class ExpansionFightPrioriser {

	public static int getRecommendedExpansionDeployment(BotState state) {
		int out = 0;
		String gameState = GameStateCalculator.calculateGameState(state);
		if (gameState.equals("open")) {
			out = getRecommendedExpansionDeploymentGameOpen(state);
		} else if (gameState.equals("stalemate")) {
			out = getRecommendedExpansionDeploymentGameStalemate(state);
		} else if (gameState.equals("won")) {
			out = getRecommendedExpansionDeploymentGameWon(state);
		} else if (gameState.equals("lost")) {
			out = getRecommendedExpansionDeploymentGameLost(state);
		} else if (gameState.equals("timeWorkingForUs")) {
			out = getRecommendedExpansionDeploymentTimeWorkingForUs(state);
		} else if (gameState.equals("timeWorkingAgainstUs")) {
			out = getRecommendedExpansionDeploymentTimeWorkingAgainstUs(state);
		} else if (gameState.equals("armiesAdvantage")) {
			// Debug
			out = getRecommendedExpansionDeploymentGameOpen(state);
		} else if (gameState.equals("armiesDisadvantage")) {
			// Debug
			out = getRecommendedExpansionDeploymentGameOpen(state);
		}
		return out;
	}

	/**
	 * In case the game is won we usually don't want to expand but destroy the
	 * opponent instead. (Hard condition for not expanding)
	 * 
	 * @param state
	 * @return
	 */
	private static int getRecommendedExpansionDeploymentGameWon(BotState state) {
		List<SuperRegion> bestExpandableSuperRegions = MovesChooser.getOrderedListOfExpandableSuperRegions(state);
		// Case that we can't expand
		if (bestExpandableSuperRegions.size() == 0) {
			return 0;
		}
		// Only expand if we have >= 50% of the SuperRegion
		SuperRegion mostDesirableSuperRegion = bestExpandableSuperRegions.get(0);
		if (mostDesirableSuperRegion.getOwnedFraction() >= 0.5) {
			return 3;
		} else {

		}
		return 0;
	}

	/**
	 * In case there is a stalemate we want to expand
	 * 
	 * @param state
	 * @return
	 */
	private static int getRecommendedExpansionDeploymentGameStalemate(BotState state) {
		return 4;
	}

	/**
	 * In case the game is open we want to expand if we can afford it. (Medium
	 * condition for expanding)
	 * 
	 * @param state
	 * @return
	 */
	// changed
	private static int getRecommendedExpansionDeploymentGameOpen(BotState state) {
		List<SuperRegion> bestExpandableSuperRegions = MovesChooser.getOrderedListOfExpandableSuperRegions(state);
		// Case that we can't expand
		if (bestExpandableSuperRegions.size() == 0) {
			return 0;
		}
		// Don't expand into completely new SuperRegions
		if (bestExpandableSuperRegions.get(0).getOwnedFraction() == 0) {
			return 0;
		}
		// Don't expand if there are more than 3 territories to take
		int missingTerritories = bestExpandableSuperRegions.get(0).getMissingRegions(state).size();
		if (missingTerritories > 3) {
			return 0;
		}
		return 3;
	}

	/**
	 * In case we have more income but a smaller stack we only want to expand in
	 * very rare circumstances. (Strongest condition for not expanding)
	 * 
	 * @param state
	 * @return
	 */
	private static int getRecommendedExpansionDeploymentTimeWorkingForUs(BotState state) {
		List<SuperRegion> bestExpandableSuperRegions = MovesChooser.getOrderedListOfExpandableSuperRegions(state);
		// Case that we can't expand
		if (bestExpandableSuperRegions.size() == 0) {
			return 0;
		}
		// Don't waste armies for expanding if more than two territories to take
		SuperRegion mostDesirableSuperRegion = bestExpandableSuperRegions.get(0);
		if (mostDesirableSuperRegion.getNeutralSubRegions(state).size() > 2) {
			return 0;
		}
		// Expand with 3 armies else
		return 3;
	}

	private static int getRecommendedExpansionDeploymentTimeWorkingAgainstUs(BotState state) {
		return getRecommendedExpansionDeploymentGameOpen(state);
	}

	private static int getRecommendedExpansionDeploymentGameLost(BotState state) {
		return getRecommendedExpansionDeploymentGameOpen(state);
	}

}
