package strategy;

import java.util.ArrayList;
import java.util.List;

import main.Region;
import main.SuperRegion;
import bot.BotState;

public class StartingRegionChooser {

	/**
	 * Calculates the six most preferred starting regions. Number 1 and 3 are in
	 * australia, number 2 and 4 are in south america and number 5 and 6 are in
	 * africa. In a superrregion some regions are preffered above others.
	 * 
	 * @param state
	 *            the bot state
	 * @return ordered list of the 6 most preferred starting regions
	 */
	public static ArrayList<Region> getPreferredStartingRegions(BotState state) {
		ArrayList<Region> out = new ArrayList<>();
		List<Region> pickableRegions = state.getPickableStartingRegions();
		List<Region> australiaRegions = getSubListFromSuperRegion(pickableRegions, state.getFullMap().getSuperRegion(6));
		List<Region> southAmericaRegions = getSubListFromSuperRegion(pickableRegions, state.getFullMap()
				.getSuperRegion(2));
		// add first spot in australia
		if (getRegionValueInSuperRegion(australiaRegions.get(0).getId()) < getRegionValueInSuperRegion(australiaRegions
				.get(1).getId())) {
			out.add(australiaRegions.get(0));
		} else {
			out.add(australiaRegions.get(1));
		}
		// add second spot in australia
		if (out.contains(australiaRegions.get(0))) {
			out.add(australiaRegions.get(1));
		} else {
			out.add(australiaRegions.get(0));
		}
		
		// add first spot in south america
		if (getRegionValueInSuperRegion(southAmericaRegions.get(0).getId()) < getRegionValueInSuperRegion(southAmericaRegions
				.get(1).getId())) {
			out.add(southAmericaRegions.get(0));
		} else {
			out.add(southAmericaRegions.get(1));
		}
		// add second spot in south america
		if (out.contains(southAmericaRegions.get(0))) {
			out.add(southAmericaRegions.get(1));
		} else {
			out.add(southAmericaRegions.get(0));
		}
		List<Region> africaRegions = getSubListFromSuperRegion(pickableRegions, state.getFullMap().getSuperRegion(4));
		// add the two spots in africa
		if (getRegionValueInSuperRegion(africaRegions.get(0).getId()) < getRegionValueInSuperRegion(africaRegions
				.get(1).getId())) {
			out.add(africaRegions.get(0));
			out.add(africaRegions.get(1));
		} else {
			out.add(africaRegions.get(1));
			out.add(africaRegions.get(0));
		}

		return out;
	}

	private static List<Region> getSubListFromSuperRegion(List<Region> regions, SuperRegion superRegion) {
		List<Region> out = new ArrayList<>();
		for (Region region : regions) {
			if (superRegion.getSubRegions().contains(region)) {
				out.add(region);
			}
		}
		return out;
	}

	/**
	 * Gets the value of a region in a superRegion, identified by regionID, for
	 * the picking stage. Only the south america, australia and africa are
	 * covered.
	 * 
	 * @param regionID
	 *            the ID of the region
	 * @return value of the region where 1 represents the highest value
	 */
	private static int getRegionValueInSuperRegion(int regionID) {
		int regionValue = -1;
		switch (regionID) {
		// south america
		case 10:
			regionValue = 3;
			break;
		case 11:
			regionValue = 2;
			break;
		case 12:
			regionValue = 1;
			break;
		case 13:
			regionValue = 4;
			break;
		// Africa
		case 21:
			regionValue = 1;
			break;
		case 22:
			regionValue = 3;
			break;
		case 23:
			regionValue = 2;
			break;
		case 24:
			regionValue = 4;
			break;
		case 25:
			regionValue = 5;
			break;
		case 26:
			regionValue = 6;
			break;
		// Australia
		case 39:
			regionValue = 3;
			break;
		case 40:
			regionValue = 2;
			break;
		case 41:
			regionValue = 1;
			break;
		case 42:
			regionValue = 4;
			break;
		}
		return regionValue;
	}
}
