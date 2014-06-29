package main;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import bot.BotState;

public class Map {

	public LinkedList<Region> regions;
	public LinkedList<SuperRegion> superRegions;

	public Map() {
		this.regions = new LinkedList<Region>();
		this.superRegions = new LinkedList<SuperRegion>();
	}

	/**
	 * Retrieves all owned regions that border an opponent spot.
	 * 
	 * @param state
	 * @return
	 */
	public List<Region> getOpponentBorderingRegions(BotState state) {
		List<Region> out = new ArrayList<>();
		List<Region> ownedRegions = this.getOwnedRegions(state);
		for (Region ownedRegion : ownedRegions) {
			if (ownedRegion.getEnemyNeighbors(state).size() > 0) {
				out.add(ownedRegion);
			}
		}
		return out;
	}
	
	public List<Region> getBorderRegions(BotState state){
		List<Region> out = new ArrayList<>();
		List<Region> ownedRegions = this.getOwnedRegions(state);
		for (Region ownedRegion : ownedRegions) {
			if(ownedRegion.getDistanceToBorder() == 1){
				out.add(ownedRegion);
			}
		}
		return out;
	}

	public List<Region> getNeutralRegions(BotState state) {
		List<Region> out = new ArrayList<>();
		for (Region region : this.getRegions()) {
			if (region.getPlayerName().equals(state.getMyPlayerName())
					|| region.getPlayerName().equals(
							state.getOpponentPlayerName())
					|| region.getPlayerName().equals("unknown")) {
				// nothing
			} else {
				out.add(region);
			}
		}
		return out;
	}

	public List<Region> getUnknownRegions() {
		List<Region> out = new ArrayList<>();
		for (Region region : this.getRegions()) {
			if (region.getPlayerName().equals("unknown")) {
				out.add(region);
			}
		}
		return out;
	}

	public List<Region> getEnemyRegions(BotState state) {
		List<Region> out = new ArrayList<>();
		for (Region region : this.getRegions()) {
			if (region.getPlayerName().equals(state.getOpponentPlayerName())) {
				out.add(region);
			}
		}
		return out;
	}

	public Map(LinkedList<Region> regions, LinkedList<SuperRegion> superRegions) {
		this.regions = regions;
		this.superRegions = superRegions;
	}

	public List<Region> getOwnedRegions(BotState state) {
		List<Region> out = new ArrayList<>();
		for (Region region : regions) {
			if (region.getPlayerName().equals(state.getMyPlayerName())) {
				out.add(region);
			}
		}
		return out;
	}

	public SuperRegion getMostDesirableSuperRegion() {
		SuperRegion mostDesirableSuperRegion = null;
		for (SuperRegion superRegion : superRegions) {
			if (superRegion.getIsMostDesirableSuperregion()) {
				mostDesirableSuperRegion = superRegion;
			}
		}

		return mostDesirableSuperRegion;
	}

	public List<SuperRegion> getPossibleEnemySuperRegions(BotState state) {
		List<SuperRegion> out = new ArrayList<>();
		for (SuperRegion superRegion : this.getSuperRegions()) {
			if (superRegion.isPossibleOwnedByEnemy(state)) {
				out.add(superRegion);
			}
		}
		return out;
	}

	/**
	 * add a Region to the map
	 * 
	 * @param region
	 *            : Region to be added
	 */
	public void add(Region region) {
		for (Region r : regions)
			if (r.getId() == region.getId()) {
				System.err
						.println("Region cannot be added: id already exists.");
				return;
			}
		regions.add(region);
	}

	/**
	 * add a SuperRegion to the map
	 * 
	 * @param superRegion
	 *            : SuperRegion to be added
	 */
	public void add(SuperRegion superRegion) {
		for (SuperRegion s : superRegions)
			if (s.getId() == superRegion.getId()) {
				System.err
						.println("SuperRegion cannot be added: id already exists.");
				return;
			}
		superRegions.add(superRegion);
	}

	/**
	 * @return : a new Map object exactly the same as this one
	 */
	public Map getMapCopy() {
		Map newMap = new Map();
		for (SuperRegion sr : superRegions) // copy superRegions
		{
			SuperRegion newSuperRegion = new SuperRegion(sr.getId(),
					sr.getArmiesReward());
			newMap.add(newSuperRegion);
		}
		for (Region r : regions) // copy regions
		{
			Region newRegion = new Region(r.getId(), newMap.getSuperRegion(r
					.getSuperRegion().getId()), r.getPlayerName(),
					r.getArmies());
			newMap.add(newRegion);
		}
		for (Region r : regions) // add neighbors to copied regions
		{
			Region newRegion = newMap.getRegion(r.getId());
			for (Region neighbor : r.getNeighbors())
				newRegion.addNeighbor(newMap.getRegion(neighbor.getId()));
		}
		return newMap;
	}

	/**
	 * @return : the list of all Regions in this map
	 */
	public LinkedList<Region> getRegions() {
		return regions;
	}

	/**
	 * @return : the list of all SuperRegions in this map
	 */
	public LinkedList<SuperRegion> getSuperRegions() {
		return superRegions;
	}

	/**
	 * @param id
	 *            : a Region id number
	 * @return : the matching Region object
	 */
	public Region getRegion(int id) {
		for (Region region : regions)
			if (region.getId() == id)
				return region;
		return null;
	}

	/**
	 * @param id
	 *            : a SuperRegion id number
	 * @return : the matching SuperRegion object
	 */
	public SuperRegion getSuperRegion(int id) {
		for (SuperRegion superRegion : superRegions)
			if (superRegion.getId() == id)
				return superRegion;
		return null;
	}

	public String getMapString() {
		String mapString = "";
		for (Region region : regions) {
			mapString = mapString.concat(region.getId() + ";"
					+ region.getPlayerName() + ";" + region.getArmies() + " ");
		}
		return mapString;
	}

}
