package basicAlgorithms;

import java.util.ArrayList;
import java.util.List;

import main.Region;
import bot.BotState;

public class ShortestPathCalculator {

	public static List<Region> getShortestPathToRegions(BotState state, Region fromRegion, List<Region> toRegions) {
		List<Region> out = new ArrayList<>();
		List<AnnotatedRegion> annotatedRegions = annotateRegions(state, toRegions);
		List<Region> inRegionAsList = new ArrayList<>();
		inRegionAsList.add(fromRegion);
		AnnotatedRegion annotatedFromRegion = getAnnotatedRegions(state, inRegionAsList, annotatedRegions).get(0);
		out.add(fromRegion);
		while (annotatedFromRegion.distance != 0) {
			AnnotatedRegion closestNeighbor = getClosestNeighborToRegions(state, annotatedFromRegion, annotatedRegions);
			out.add(closestNeighbor.region);
			annotatedFromRegion = closestNeighbor;
		}
		return out;
	}

	public static List<AnnotatedRegion> annotateRegions(BotState state, List<Region> toRegions) {
		List<AnnotatedRegion> annotatedRegions = new ArrayList<>();

		// initialize
		for (Region region : state.getVisibleMap().getRegions()) {
			if (toRegions.contains(region)) {
				annotatedRegions.add(new AnnotatedRegion(region, 0));
			} else {
				annotatedRegions.add(new AnnotatedRegion(region, 100));
			}
		}

		// do the calculations
		// Now do the real stuff
		boolean hasSomethingChanged = true;
		while (hasSomethingChanged) {
			hasSomethingChanged = false;
			for (AnnotatedRegion annotatedRegion : annotatedRegions) {
				AnnotatedRegion closestNeighbor = getClosestNeighborToRegions(state, annotatedRegion, annotatedRegions);
				if (closestNeighbor.distance < annotatedRegion.distance
						&& annotatedRegion.distance != closestNeighbor.distance + 1) {
					annotatedRegion.distance = closestNeighbor.distance + 1;
					hasSomethingChanged = true;

				}
			}
		}

		return annotatedRegions;
	}

	private static AnnotatedRegion getClosestNeighborToRegions(BotState state, AnnotatedRegion inRegion,
			List<AnnotatedRegion> allAnnotatedRegions) {
		AnnotatedRegion out = inRegion;
		List<Region> neighbors = inRegion.region.getNeighbors();
		List<AnnotatedRegion> annotatedNeighbors = getAnnotatedRegions(state, neighbors, allAnnotatedRegions);
		for (AnnotatedRegion annotatedNeighbor : annotatedNeighbors) {
			// prefer neutral regions
			if (annotatedNeighbor.distance < out.distance
					|| (annotatedNeighbor.distance == out.distance && annotatedNeighbor.region.getPlayerName().equals(
							"neutral"))) {
				out = annotatedNeighbor;
			}
		}
		return out;
	}

	private static List<AnnotatedRegion> getAnnotatedRegions(BotState state, List<Region> in,
			List<AnnotatedRegion> allAnnotatedRegions) {
		List<AnnotatedRegion> out = new ArrayList<>();
		for (AnnotatedRegion annotatedRegion : allAnnotatedRegions) {
			if (in.contains(annotatedRegion.region)) {
				out.add(annotatedRegion);
			}
		}
		return out;
	}

	static class AnnotatedRegion {
		Region region;
		int distance;

		public AnnotatedRegion(Region region, int distance) {
			this.region = region;
			this.distance = distance;
		}

	}

}
