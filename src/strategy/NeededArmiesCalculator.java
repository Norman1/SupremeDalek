package strategy;

import java.util.ArrayList;
import java.util.List;

import main.Region;
import bot.BotState;

/**
 * This class is responsible for calculating the needed armies for either
 * defending an own spot or for attacking an opponent spot.
 * 
 */
public class NeededArmiesCalculator {

	public static List<NeededDefenseArmies> neededDefenseArmiesList = new ArrayList<>();
	public static List<NeededAttackArmies> neededAttackArmiesList = new ArrayList<>();

	/**
	 * Returns null if the region isn't under attack else the calculated
	 * neededDefenseArmies for this region.
	 * 
	 * @param region
	 * @return
	 */
	public static NeededDefenseArmies getNeededDefenseArmies(Region region) {
		NeededDefenseArmies out = null;
		for (NeededDefenseArmies nDA : neededDefenseArmiesList) {
			if (nDA.region.equals(region)) {
				out = nDA;
			}
		}
		return out;
	}

	/**
	 * Returns null if the region isn't belonging to the opponent. Else the
	 * neededAttackArmies for this region.
	 * 
	 * @param region
	 * @return
	 */
	public static NeededAttackArmies getNeededAttackArmies(Region region) {
		NeededAttackArmies out = null;
		for (NeededAttackArmies nAA : neededAttackArmiesList) {
			if (nAA.region.equals(region)) {
				out = nAA;
			}
		}
		return out;
	}

	public static void calculateNeededArmies(int opponentIncome, BotState state) {
		neededDefenseArmiesList.clear();
		neededAttackArmiesList.clear();
		calculateNeededDefenseArmies(opponentIncome, state);
		calculateNeededAttackArmies(opponentIncome, state);
	}

	public static void calculateNeededDefenseArmies(int opponentIncome, BotState state) {
		for (Region opponentBorderingRegion : state.getVisibleMap().getOpponentBorderingRegions(state)) {
			NeededDefenseArmies nA = new NeededDefenseArmies();
			nA.region = opponentBorderingRegion;
			int currentAttackingArmies = calculateCurrentOpponentNeighborPossibleAttackArmies(state,
					opponentBorderingRegion);
			int fairDefenceArmiesNoDeployment = (int) Math.ceil(currentAttackingArmies * 0.6 / 0.7);
			int fairDefenceArmiesDeployment = (int) Math.ceil((currentAttackingArmies + opponentIncome) * 0.6 / 0.7);
			int defendingArmiesNoDeployment = (int) Math.ceil((currentAttackingArmies) * 0.6);
			int defendingArmiesFullDeployment = (int) Math.ceil((currentAttackingArmies + opponentIncome) * 0.6);
			nA.fairDefenceArmiesNoDeployment = fairDefenceArmiesNoDeployment;
			nA.fairDefenceArmiesDeployment = fairDefenceArmiesDeployment;
			nA.defendingArmiesNoDeployment = defendingArmiesNoDeployment;
			nA.defendingArmiesFullDeployment = defendingArmiesFullDeployment;
			neededDefenseArmiesList.add(nA);
		}
	}

	public static void calculateNeededAttackArmies(int opponentIncome, BotState state) {
		for (Region opponentRegion : state.getVisibleMap().getEnemyRegions(state)) {
			NeededAttackArmies nA = new NeededAttackArmies();
			nA.region = opponentRegion;
			int currentArmies = opponentRegion.getArmies();
			int fairFightArmiesNoDeployment = (int) Math.ceil(currentArmies * 0.7 / 0.6);
			int fairFightArmiesFullDeployment = (int) Math.ceil((currentArmies + opponentIncome) * 0.7 / 0.6);
			int crushingArmiesNoDeployment = (int) Math.ceil(currentArmies / 0.6);
			int crushingArmiesFullDeployment = (int) Math.ceil((currentArmies + opponentIncome) / 0.6);
			int overkillArmiesFullDeployment = (int) Math.ceil((currentArmies + opponentIncome + 7) / 0.6);
			nA.fairFightArmiesNoDeployment = fairFightArmiesNoDeployment;
			nA.fairFightArmiesFullDeployment = fairFightArmiesFullDeployment;
			nA.crushingArmiesNoDeployment = crushingArmiesNoDeployment;
			nA.crushingArmiesFullDeployment = crushingArmiesFullDeployment;
			nA.overkillArmiesFullDeployment = overkillArmiesFullDeployment;
			neededAttackArmiesList.add(nA);
		}
	}

	private static int calculateCurrentOpponentNeighborPossibleAttackArmies(BotState state, Region ourRegion) {
		int out = 0;
		for (Region opponentNeighbor : ourRegion.getEnemyNeighbors(state)) {
			out = out + opponentNeighbor.getArmies() - 1;
		}
		return out;
	}

	public static class NeededDefenseArmies {
		public Region region;
		public int fairDefenceArmiesNoDeployment;
		public int fairDefenceArmiesDeployment;
		public int defendingArmiesNoDeployment;
		public int defendingArmiesFullDeployment;
	}

	public static class NeededAttackArmies {
		public Region region;
		public int fairFightArmiesNoDeployment;
		public int fairFightArmiesFullDeployment;
		public int crushingArmiesNoDeployment;
		public int crushingArmiesFullDeployment;
		public int overkillArmiesFullDeployment;
	}
}
