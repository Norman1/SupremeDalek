package main;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import evaluation.RegionAndSuperRegionAnnotator;
import evaluation.RegionValueCalculator;
import evaluation.RegionAndSuperRegionAnnotator.RegionAnnotations;

import move.AttackTransferMove;
import move.PlaceArmiesMove;
import bot.BotState;

public class Region {

	private int id;
	private LinkedList<Region> neighbors;
	private SuperRegion superRegion;
	private int armies;
	private String playerName;
	private int distanceToBorder;
	private int distanceToOpponentBorder;
	private int distanceToOpponentSuperRegionBorder;
	private int expansionRegionValue = 0;
	private int attackRegionValue = 0;
	private int defenceRegionValue = 0;
	private int stillMissingArmies;
	private List<PlaceArmiesMove> armyPlacement = new ArrayList<>();
	private List<AttackTransferMove> outgoingMoves = new ArrayList<>();
	private List<AttackTransferMove> incomingMoves = new ArrayList<>();
	private List<RegionAndSuperRegionAnnotator.RegionAnnotations> annotations;

	public Region(int id, SuperRegion superRegion) {
		this.id = id;
		this.superRegion = superRegion;
		this.neighbors = new LinkedList<Region>();
		this.playerName = "unknown";
		this.armies = 0;
		// this.receivingAttackingArmies = 0;
		superRegion.addSubRegion(this);
		this.stillMissingArmies = 0;
	}

	public List<RegionAnnotations> getAnnotations() {
		return this.annotations;
	}

	public void setAnnotations(List<RegionAnnotations> annotations) {
		this.annotations = annotations;
	}

	public int getIncomingArmies() {
		int out = 0;
		for (AttackTransferMove atm : incomingMoves) {
			out += atm.getArmies();
		}
		return out;
	}

	public int getArmiesAfterDeployment() {
		int out = this.armies;
		for (PlaceArmiesMove pam : armyPlacement) {
			out += pam.getArmies();
		}
		return out;
	}

	public int getArmiesAfterDeploymentAndIncomingMoves(BotState state) {
		int out = getArmiesAfterDeployment();
		out += getIncomingArmies();
		return out;
	}

	public int getIdleArmies() {
		int idleArmies = armies - 1;
		for (PlaceArmiesMove pam : armyPlacement) {
			idleArmies += pam.getArmies();
		}
		for (AttackTransferMove atm : outgoingMoves) {
			idleArmies -= atm.getArmies();
		}
		return idleArmies;
	}

	public List<PlaceArmiesMove> getArmyPlacement() {
		return this.armyPlacement;
	}

	public List<AttackTransferMove> getOutgoingMoves() {
		return this.outgoingMoves;
	}

	public List<AttackTransferMove> getIncomingMoves() {
		return this.incomingMoves;
	}

	public void addArmyPlacement(PlaceArmiesMove placeArmiesMove) {
		this.armyPlacement.add(placeArmiesMove);
	}

	public void addOutgoingMove(AttackTransferMove attackTransferMove) {
		this.outgoingMoves.add(attackTransferMove);
	}

	public void addIncomingMove(AttackTransferMove attackTransferMove) {
		this.incomingMoves.add(attackTransferMove);
	}

	/**
	 * Calculates with how many armies we can attack this region without
	 * deploying. Not uses the available idleArmies but amount of armies -1.
	 * 
	 * @return
	 */
	public int getOwnSurroundingPossibleAttackArmies(BotState state) {
		int attackArmies = 0;
		for (Region ownedNeighbor : this.getOwnedNeighbors(state)) {
			attackArmies = attackArmies + ownedNeighbor.getArmies() - 1;
		}
		return attackArmies;
	}

	public int getDistanceToOpponentSuperRegionBorder() {
		return distanceToOpponentSuperRegionBorder;
	}

	public void setDistanceToOpponentSuperRegionBorder(int distanceToOpponentSuperRegionBorder) {
		this.distanceToOpponentSuperRegionBorder = distanceToOpponentSuperRegionBorder;
	}

	public int getStillMissingArmies() {
		return this.stillMissingArmies;
	}

	public void setStillMissingArmies(int stillMissingArmies) {
		this.stillMissingArmies = stillMissingArmies;
	}

	public int getExpansionRegionValue() {
		return this.expansionRegionValue;
	}

	public void setExpansionRegionValue(int expansionRegionValue) {
		this.expansionRegionValue = expansionRegionValue;
	}

	public int getAttackRegionValue() {
		return this.attackRegionValue;
	}

	public void setAttackRegionValue(int attackRegionValue) {
		this.attackRegionValue = attackRegionValue;
	}

	public int getDefenceRegionValue() {
		return this.defenceRegionValue;
	}

	public void setDefenceRegionValue(int defenceRegionValue) {
		this.defenceRegionValue = defenceRegionValue;
	}

	/**
	 * May return null if this region is already closest or no intel on
	 * opponent. If two owned neighbors have the same distance then the neighbor
	 * with the higher defenceRegionValue is returned.
	 * 
	 * @return
	 */
	public Region getClosestOwnedNeighborToOpponentBorder(BotState state) {
		List<Region> ownedNeighbors = this.getOwnedNeighbors(state);
		Region closestNeighbor = null;
		int minDistance = this.getDistanceToOpponentBorder();
		for (Region ownedNeighbor : ownedNeighbors) {
			if (ownedNeighbor.getDistanceToOpponentBorder() < minDistance) {
				closestNeighbor = ownedNeighbor;
				minDistance = ownedNeighbor.getDistanceToOpponentBorder();
			}
		}
		if (closestNeighbor == null) {
			return null;
		}
		List<Region> neighborsWithMinDistance = new ArrayList<>();
		for (Region ownedNeighbor : ownedNeighbors) {
			if (ownedNeighbor.getDistanceToOpponentBorder() == closestNeighbor.getDistanceToOpponentBorder()) {
				neighborsWithMinDistance.add(ownedNeighbor);
			}
		}
		List<Region> sorted = RegionValueCalculator.sortRegionsByDefenceRegionValue(state, neighborsWithMinDistance);
		return sorted.get(0);
	}

	/**
	 * May return null if no intel on opponent.
	 * 
	 * @param state
	 * @return
	 */
	public Region getClosestNeighborRegionToOpponentBorder(BotState state) {
		List<Region> neighbors = this.getNeighbors();
		Region closestNeighbor = null;
		int minDistance = this.getDistanceToOpponentBorder();
		for (Region ownedNeighbor : neighbors) {
			if (ownedNeighbor.getDistanceToOpponentBorder() < minDistance) {
				closestNeighbor = ownedNeighbor;
				minDistance = ownedNeighbor.getDistanceToOpponentBorder();
			}
		}
		return closestNeighbor;
	}

	public int getAdjustedRegionValue(BotState state) {
		if (this.getPlayerName().equals(state.getOpponentPlayerName())) {
			return attackRegionValue;
		}
		List<Region> opponentNeighbos = this.getEnemyNeighbors(state);
		int highestValue = this.getDefenceRegionValue();
		for (Region opponentNeighbor : opponentNeighbos) {
			if (opponentNeighbor.getAttackRegionValue() > highestValue) {
				highestValue = opponentNeighbor.getAttackRegionValue();
			}
		}
		return highestValue;
	}

	public int getSurroundingArmies(BotState state) {
		int surroundingArmies = 0;
		for (Region neighbor : this.getOwnedNeighbors(state)) {
			surroundingArmies = surroundingArmies + neighbor.getArmies();
		}
		return surroundingArmies;
	}

	public List<Region> getUnknownNeighborsWithinSameSuperRegion() {
		List<Region> out = new ArrayList<>();
		List<Region> neighborsWithingSameSuperRegion = getNeighborsWithinSameSuperRegion();
		for (Region neighbor : neighborsWithingSameSuperRegion) {
			if (neighbor.getPlayerName().equals("unknown")) {
				out.add(neighbor);
			}
		}
		return out;
	}

	public List<Region> getNeighborsWithinSameSuperRegion() {
		List<Region> out = new ArrayList<>();
		SuperRegion superRegion = this.getSuperRegion();
		for (Region neighbor : this.getNeighbors()) {
			if (neighbor.getSuperRegion().equals(superRegion)) {
				out.add(neighbor);
			}
		}
		return out;
	}

	public boolean isNeutralRegion(BotState state) {
		boolean isNeutralRegion = true;
		if (getPlayerName().equals(state.getMyPlayerName()) || getPlayerName().equals(state.getOpponentPlayerName())
				|| getPlayerName().equals("unknown")) {
			isNeutralRegion = false;
		}
		return isNeutralRegion;
	}

	public List<Region> getEnemyNeighbors(BotState state) {
		List<Region> neighbors = getNeighbors();
		List<Region> out = new ArrayList<>();
		for (Region neighbor : neighbors) {
			if (neighbor.getPlayerName().equals(state.getOpponentPlayerName())) {
				out.add(neighbor);
			}
		}
		return out;
	}

	public List<Region> getOwnedNeighbors(BotState state) {
		List<Region> neighbors = getNeighbors();
		List<Region> out = new ArrayList<>();
		for (Region neighbor : neighbors) {
			if (neighbor.getPlayerName().equals(state.getMyPlayerName())) {
				out.add(neighbor);
			}
		}
		return out;
	}

	// public void setReceivingAttackingArmies(int armies) {
	// receivingAttackingArmies = armies;
	// }
	//
	// public int getReceivingAttackinArmies() {
	// return receivingAttackingArmies;
	// }

	public int getDistanceToBorder() {
		return distanceToBorder;
	}

	public void setDistanceToBorder(int distanceToBorder) {
		this.distanceToBorder = distanceToBorder;
	}

	public int getDistanceToOpponentBorder() {
		return distanceToOpponentBorder;
	}

	public void setDistanceToOpponentBorder(int distanceToOpponentBorder) {
		this.distanceToOpponentBorder = distanceToOpponentBorder;
	}

	public Region(int id, SuperRegion superRegion, String playerName, int armies) {
		this.id = id;
		this.superRegion = superRegion;
		this.neighbors = new LinkedList<Region>();
		this.playerName = playerName;
		this.armies = armies;

		superRegion.addSubRegion(this);
	}

	public void addNeighbor(Region neighbor) {
		if (!neighbors.contains(neighbor)) {
			neighbors.add(neighbor);
			neighbor.addNeighbor(this);
		}
	}

	/**
	 * @param region
	 *            a Region object
	 * @return True if this Region is a neighbor of given Region, false
	 *         otherwise
	 */
	public boolean isNeighbor(Region region) {
		if (neighbors.contains(region))
			return true;
		return false;
	}

	/**
	 * @param playerName
	 *            A string with a player's name
	 * @return True if this region is owned by given playerName, false otherwise
	 */
	public boolean ownedByPlayer(String playerName) {
		if (playerName.equals(this.playerName))
			return true;
		return false;
	}

	/**
	 * @param armies
	 *            Sets the number of armies that are on this Region
	 */
	public void setArmies(int armies) {
		this.armies = armies;
	}

	// public void setIdleArmies(int idleArmies) {
	// this.idleArmies = idleArmies;
	// }

	/**
	 * @param playerName
	 *            Sets the Name of the player that this Region belongs to
	 */
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	/**
	 * @return The id of this Region
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return A list of this Region's neighboring Regions
	 */
	public List<Region> getNeighbors() {
		return neighbors;
	}

	/**
	 * @return The SuperRegion this Region is part of
	 */
	public SuperRegion getSuperRegion() {
		return superRegion;
	}

	/**
	 * @return The number of armies on this region
	 */
	public int getArmies() {
		return armies;
	}

	/**
	 * @return A string with the name of the player that owns this region
	 */
	public String getPlayerName() {
		return playerName;
	}

}
