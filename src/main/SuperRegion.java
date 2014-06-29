package main;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import evaluation.RegionAndSuperRegionAnnotator;
import evaluation.RegionAndSuperRegionAnnotator.SuperRegionAnnotations;

import model.MapModel;

import bot.BotState;

public class SuperRegion {

	private int id;
	private int armiesReward;
	private LinkedList<Region> subRegions;
	private double ownedFraction;
	private boolean isMostDesirableSuperRegion;
	private List<RegionAndSuperRegionAnnotator.SuperRegionAnnotations> annotations;

	public SuperRegion(int id, int armiesReward) {
		this.id = id;
		this.armiesReward = armiesReward;
		subRegions = new LinkedList<Region>();
	}

	public List<SuperRegionAnnotations> getAnnotations() {
		return this.annotations;
	}

	public void setAnnotations(List<SuperRegionAnnotations> annotations) {
		this.annotations = annotations;
	}

	/**
	 * Retrieves the subregions that we don't own.
	 * 
	 * @param state
	 * @return
	 */
	public List<Region> getMissingRegions(BotState state) {
		List<Region> out = new ArrayList<>();
		for (Region region : this.getSubRegions()) {
			if (!region.getPlayerName().equals(state.getMyPlayerName())) {
				out.add(region);
			}
		}
		return out;
	}

	/**
	 * Calculates whether the SuperRegion has opponent presence, according to
	 * our current intel.
	 * 
	 * @param state
	 * @return
	 */
	public boolean hasEnemyPresence(BotState state) {
		List<Region> knownOpponentSpots = MapModel.getKnownOpponentSpots(state);
		boolean hasEnemyPresence = false;
		for (Region subregion : this.getSubRegions()) {
			if (knownOpponentSpots.contains(subregion)) {
				hasEnemyPresence = true;
			}
		}
		return hasEnemyPresence;
	}

	public List<Region> getNeutralOrEnemySubRegions(BotState state) {
		List<Region> out = new ArrayList<>();
		for (Region region : this.getSubRegions()) {
			if (region.isNeutralRegion(state) || region.getPlayerName().equals(state.getOpponentPlayerName())) {
				out.add(region);
			}
		}
		return out;
	}

	public List<Region> getNeutralSubRegions(BotState state) {
		List<Region> out = new ArrayList<>();
		for (Region region : this.getSubRegions()) {
			if (region.isNeutralRegion(state)) {
				out.add(region);
			}
		}
		return out;
	}

	public boolean isOwnedByMyself(BotState state) {
		if (this.getOwnedFraction() == 1.0) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isPossibleOwnedByEnemy(BotState state) {
		boolean isPossibleOwnedByEnemy = true;
		for (Region subregion : this.getSubRegions()) {
			if (!subregion.getPlayerName().equals(state.getOpponentPlayerName())
					&& !subregion.getPlayerName().equals("unknown")) {
				isPossibleOwnedByEnemy = false;
			}
		}
		return isPossibleOwnedByEnemy;
	}

	public boolean isUnderAttack(BotState state) {
		if (this.getOwnedFraction() < 1) {
			return false;
		}
		boolean isUnderAttack = false;
		List<Region> neighbors = this.getNeighborRegions();
		for (Region neighbor : neighbors) {
			if (neighbor.getPlayerName().equals(state.getOpponentPlayerName())) {
				isUnderAttack = true;
			}
		}

		return isUnderAttack;
	}

	public List<Region> getNeighborRegions() {
		List<Region> out = new ArrayList<>();
		for (Region subRegion : subRegions) {
			for (Region neighborToSubRegion : subRegion.getNeighbors()) {
				if (!this.getSubRegions().contains(neighborToSubRegion) && !out.contains(neighborToSubRegion)) {
					out.add(neighborToSubRegion);
				}
			}
		}
		return out;
	}

	public List<Region> getNeighborAndSubregions() {
		List<Region> out = new ArrayList<>();
		out.addAll(subRegions);
		out.addAll(this.getNeighborRegions());
		return out;
	}

	public void setIsMostDesirableSuperregion(boolean isMostDesirableSuperRegion) {
		this.isMostDesirableSuperRegion = isMostDesirableSuperRegion;
	}

	public boolean getIsMostDesirableSuperregion() {
		return isMostDesirableSuperRegion;
	}

	public double getOwnedFraction() {
		return ownedFraction;
	}

	public void setOwnedFraction(double ownedFraction) {
		this.ownedFraction = ownedFraction;
	}

	public void addSubRegion(Region subRegion) {
		if (!subRegions.contains(subRegion))
			subRegions.add(subRegion);
	}

	/**
	 * @return A string with the name of the player that fully owns this
	 *         SuperRegion
	 */
	public String ownedByPlayer() {
		String playerName = subRegions.getFirst().getPlayerName();
		for (Region region : subRegions) {
			if (!playerName.equals(region.getPlayerName()))
				return null;
		}
		return playerName;
	}

	/**
	 * @return The id of this SuperRegion
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return The number of armies a Player is rewarded when he fully owns this
	 *         SuperRegion
	 */
	public int getArmiesReward() {
		return armiesReward;
	}

	/**
	 * @return A list with the Regions that are part of this SuperRegion
	 */
	public LinkedList<Region> getSubRegions() {
		return subRegions;
	}
}
