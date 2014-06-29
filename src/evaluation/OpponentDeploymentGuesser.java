package evaluation;

import main.Region;
import model.HeuristicMapModel;
import move.PlaceArmiesMove;
import bot.BotState;

/**
 * This class is responsible for guessing the spots where the opponent puts his income.
 *
 */
public class OpponentDeploymentGuesser {
	
	public static int getGuessedOpponentDeployment(BotState state, Region region){
		int out = 0;
		PlaceArmiesMove opponentDeployment = guessOpponentDeployment(state);
		if(opponentDeployment != null && region.equals(opponentDeployment.getRegion())){
			out = opponentDeployment.getArmies();
		}
		return out;
	}

	/**
	 * Guess that the opponent puts all his income to one spot. May return null.
	 * 
	 * 
	 * @param state
	 * @return
	 */
	public static PlaceArmiesMove guessOpponentDeployment(BotState state) {
		PlaceArmiesMove out = null;
		int guessedOpponentIncome = HeuristicMapModel.getGuessedOpponentIncome();
		Region highestAttackRegionValueRegion = getHighestAttackRegionValueRegion(state);
		Region highestDefenceRegionValueRegion = getHighestDefenceRegionValueRegion(state);
		Region highestValueRegion = null;
		if (highestAttackRegionValueRegion == null && highestDefenceRegionValueRegion != null) {
			highestValueRegion = highestDefenceRegionValueRegion;
		} else if (highestAttackRegionValueRegion != null && highestDefenceRegionValueRegion == null) {
			highestValueRegion = highestAttackRegionValueRegion;
		} else if (highestAttackRegionValueRegion == null && highestDefenceRegionValueRegion == null) {
			highestValueRegion = null;
		} else if (highestAttackRegionValueRegion.getAttackRegionValue() > highestDefenceRegionValueRegion
				.getDefenceRegionValue()) {
			highestValueRegion = highestAttackRegionValueRegion;
		} else {
			highestValueRegion = highestDefenceRegionValueRegion;
		}
		// ...
		if (highestValueRegion == null) {
			// empty
		} else if (highestValueRegion == highestAttackRegionValueRegion) {
			out = new PlaceArmiesMove(state.getOpponentPlayerName(), highestAttackRegionValueRegion,
					guessedOpponentIncome);
		} else if (highestValueRegion == highestDefenceRegionValueRegion) {
			// Calculate the neighboring opponent spot where he has the most
			// armies
			int maximumOpponentArmies = 0;
			Region maximumOpponentNeighbor = null;
			for (Region opponentNeighbor : highestDefenceRegionValueRegion.getEnemyNeighbors(state)) {
				if (opponentNeighbor.getArmies() > maximumOpponentArmies) {
					maximumOpponentNeighbor = opponentNeighbor;
					maximumOpponentArmies = opponentNeighbor.getArmies();
				}
			}
			// If more than one neighbor spot has the maximum amount of armies
			// choose that spot with the highest attack region value.
			for (Region opponentNeighbor : highestDefenceRegionValueRegion.getEnemyNeighbors(state)) {
				if (opponentNeighbor.getArmies() == maximumOpponentArmies
						&& opponentNeighbor.getAttackRegionValue() > maximumOpponentNeighbor.getAttackRegionValue()) {
					maximumOpponentNeighbor = opponentNeighbor;
				}
			}
			out = new PlaceArmiesMove(state.getOpponentPlayerName(), maximumOpponentNeighbor, guessedOpponentIncome);
		}

		return out;
	}

	public static Region getHighestAttackRegionValueRegion(BotState state) {
		Region out = null;
		int currentValue = 0;
		for (Region opponentOwnedRegion : state.getVisibleMap().getEnemyRegions(state)) {
			if (opponentOwnedRegion.getAttackRegionValue() > currentValue) {
				out = opponentOwnedRegion;
				currentValue = opponentOwnedRegion.getAttackRegionValue();
			}
		}
		return out;
	}

	public static Region getHighestDefenceRegionValueRegion(BotState state) {
		Region out = null;
		int currentValue = 0;
		for (Region ownedRegion : state.getVisibleMap().getOpponentBorderingRegions(state)) {
			if (ownedRegion.getDefenceRegionValue() > currentValue) {
				out = ownedRegion;
				currentValue = ownedRegion.getDefenceRegionValue();
			}
		}
		return out;
	}
}
