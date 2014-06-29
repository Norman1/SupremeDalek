package evaluation;

import java.util.ArrayList;
import java.util.List;

import main.Region;
import main.SuperRegion;
import model.HeuristicMapModel;
import bot.BotState;

/**
 * This class is responsible for annotating regions allowing us to make smart
 * decisions accordingly.
 * 
 * 
 */
public class RegionAndSuperRegionAnnotator {

	public static void annotate(BotState state) {
		annotateSuperRegions(state);
		annotateRegions(state);
	}

	private static void annotateRegions(BotState state) {
		for (Region region : state.getVisibleMap().getRegions()) {
			List<RegionAnnotations> annotations = new ArrayList<>();
			boolean isSpotInOpponentSuperRegion = isSpotInOpponentSuperRegion(state, region);
			boolean isSpotInOwnSuperRegion = isSpotInOwnSuperRegion(state, region);
			boolean isEntranceToOpponentSuperRegion = isEntranceToOpponentSuperRegion(state, region);
			boolean isEntranceToOwnSuperRegion = isEntranceToOwnSuperRegion(state, region);
			boolean canPushOpponentOut = canPushOpponentOut(state, region);
			boolean canGetPushedOut = canGetPushedOut(state, region);
			boolean canTakeSuperRegion = canTakeSuperRegion(state, region);
			boolean canSuperRegionBeTakenByOpponent = canSuperRegionBeTakenByOpponent(state, region);
			if (isSpotInOpponentSuperRegion) {
				annotations.add(RegionAnnotations.SPOT_IN_OPPONENT_SUPERREGION);
			}
			if (isSpotInOwnSuperRegion) {
				annotations.add(RegionAnnotations.SPOT_IN_OWN_SUPERREGION);
			}
			if (isEntranceToOpponentSuperRegion) {
				annotations.add(RegionAnnotations.ENTRANCE_TO_OPPONENT_SUPERREGION);
			}
			if (isEntranceToOwnSuperRegion) {
				annotations.add(RegionAnnotations.ENTRANCE_TO_OWN_SUPERREGION);
			}
			if (canPushOpponentOut) {
				annotations.add(RegionAnnotations.CAN_PUSH_OPPONENT_OUT);
			}
			if (canGetPushedOut) {
				annotations.add(RegionAnnotations.CAN_GET_PUSHED_OUT);
			}
			if (canTakeSuperRegion) {
				annotations.add(RegionAnnotations.CAN_TAKE_SUPERREGION);
			}
			if (canSuperRegionBeTakenByOpponent) {
				annotations.add(RegionAnnotations.SUPERREGION_CAN_BE_TAKEN);
			}
			if (isUnimportantSpot(state, region, annotations)) {
				annotations.add(RegionAnnotations.UNIMPORTANT_SPOT);
			}
			region.setAnnotations(annotations);
		}
	}

	private static void annotateSuperRegions(BotState state) {
		for (SuperRegion superRegion : state.getVisibleMap().getSuperRegions()) {
			boolean canSuperRegionBeTaken = canSuperRegionBeTaken(state, superRegion);
			boolean canSuperRegionBeTakenByOpponent = canSuperRegionBeTakenByOpponent(state, superRegion);
			boolean canGetPushedOutOfSuperRegion = canGetPushedOutOfSuperRegion(state, superRegion);
			boolean canPushOpponentOutOfSuperRegion = canPushOpponentOutOfSuperRegion(state, superRegion);
			List<SuperRegionAnnotations> annotations = new ArrayList<>();
			if (canSuperRegionBeTaken) {
				annotations.add(SuperRegionAnnotations.CAN_BE_TAKEN);
			}
			if (canSuperRegionBeTakenByOpponent) {
				annotations.add(SuperRegionAnnotations.CAN_BE_TAKEN_BY_OPPONENT);
			}
			if (canPushOpponentOutOfSuperRegion) {
				annotations.add(SuperRegionAnnotations.CAN_PUSH_OPPONENT_OUT);
			}
			if (canGetPushedOutOfSuperRegion) {
				annotations.add(SuperRegionAnnotations.CAN_GET_PUSHED_OUT);
			}
			superRegion.setAnnotations(annotations);
		}

	}

	private static boolean canGetPushedOutOfSuperRegion(BotState state, SuperRegion superRegion) {
		boolean out = true;
		List<Region> subRegions = superRegion.getSubRegions();
		for (Region subRegion : subRegions) {
			if (subRegion.getPlayerName().equals(state.getMyPlayerName())
					&& subRegion.getEnemyNeighbors(state).size() == 0) {
				out = false;
			}
		}
		if (superRegion.getOwnedFraction() == 0) {
			out = false;
			;
		}
		if (canSuperRegionBeTakenByOpponent(state, superRegion)) {
			out = false;
		}

		return out;
	}

	private static boolean canPushOpponentOutOfSuperRegion(BotState state, SuperRegion superRegion) {
		boolean out = true;
		List<Region> subRegions = superRegion.getSubRegions();
		for (Region subRegion : subRegions) {
			if (subRegion.getPlayerName().equals("unknown")
					&& HeuristicMapModel.getGuessedOpponentRegions().contains(subRegion)) {
				out = false;
			}
		}
		if (superRegion.isOwnedByMyself(state)) {
			out = false;
		}
		if (canSuperRegionBeTaken(state, superRegion)) {
			out = false;
		}

		return out;
	}

	private static boolean canSuperRegionBeTakenByOpponent(BotState state, SuperRegion superRegion) {
		boolean out = true;
		List<Region> subRegions = superRegion.getSubRegions();
		for (Region subRegion : subRegions) {
			if (subRegion.getPlayerName().equals("neutral")) {
				out = false;
			}
			if (subRegion.getPlayerName().equals(state.getMyPlayerName())
					&& subRegion.getEnemyNeighbors(state).size() == 0) {
				out = false;
			}
			if (subRegion.getPlayerName().equals("unknown")
					&& !HeuristicMapModel.getGuessedOpponentRegions().contains(subRegion)) {
				out = false;
			}
		}
		if (HeuristicMapModel.getGuessedOpponentSuperRegions().contains(superRegion)) {
			out = false;
		}
		return out;
	}

	private static boolean canSuperRegionBeTaken(BotState state, SuperRegion superRegion) {
		boolean canBeTaken = true;
		List<Region> subRegions = superRegion.getSubRegions();
		for (Region subRegion : subRegions) {
			if (subRegion.getPlayerName().equals("neutral") || subRegion.getPlayerName().equals("unknown")) {
				canBeTaken = false;
			}
		}
		if (superRegion.isOwnedByMyself(state)) {
			canBeTaken = false;
		}
		return canBeTaken;
	}

	private static boolean isSpotInOpponentSuperRegion(BotState state, Region region) {
		boolean out = false;
		if (HeuristicMapModel.getGuessedOpponentSuperRegions().contains(region.getSuperRegion())) {
			out = true;
		}
		return out;
	}

	private static boolean isSpotInOwnSuperRegion(BotState state, Region region) {
		boolean out = false;
		if (region.getSuperRegion().isOwnedByMyself(state)) {
			out = true;
		}
		return out;
	}

	private static boolean isEntranceToOwnSuperRegion(BotState state, Region region) {
		boolean out = false;
		List<Region> neighbors = region.getNeighbors();
		for (Region neighbor : neighbors) {
			if (!neighbor.getSuperRegion().equals(region.getSuperRegion())
					&& neighbor.getSuperRegion().isOwnedByMyself(state)) {
				out = true;
			}
		}
		return out;
	}

	private static boolean isEntranceToOpponentSuperRegion(BotState state, Region region) {
		boolean out = false;
		List<Region> neighbors = region.getNeighbors();
		for (Region neighbor : neighbors) {
			if (!neighbor.getSuperRegion().equals(region.getSuperRegion())
					&& HeuristicMapModel.getGuessedOpponentSuperRegions().contains(neighbor.getSuperRegion())) {
				out = true;
			}
		}
		return out;
	}

	private static boolean canPushOpponentOut(BotState state, Region region) {
		SuperRegion superRegion = region.getSuperRegion();
		List<SuperRegionAnnotations> superRegionAnnotations = superRegion.getAnnotations();
		boolean out = false;
		if (superRegionAnnotations.contains(SuperRegionAnnotations.CAN_PUSH_OPPONENT_OUT)) {
			out = true;
		}
		if (!region.getPlayerName().equals(state.getOpponentPlayerName())) {
			out = false;
		}
		return out;
	}

	private static boolean canGetPushedOut(BotState state, Region region) {
		SuperRegion superRegion = region.getSuperRegion();
		List<SuperRegionAnnotations> superRegionAnnotations = superRegion.getAnnotations();
		boolean out = false;
		if (superRegionAnnotations.contains(SuperRegionAnnotations.CAN_GET_PUSHED_OUT)) {
			out = true;
		}
		if (!region.getPlayerName().equals(state.getMyPlayerName())) {
			out = false;
		}
		return out;
	}

	private static boolean canTakeSuperRegion(BotState state, Region region) {
		SuperRegion superRegion = region.getSuperRegion();
		List<SuperRegionAnnotations> superRegionAnnotations = superRegion.getAnnotations();
		boolean out = false;
		if (superRegionAnnotations.contains(SuperRegionAnnotations.CAN_BE_TAKEN)) {
			out = true;
		}
		return out;
	}

	private static boolean canSuperRegionBeTakenByOpponent(BotState state, Region region) {
		SuperRegion superRegion = region.getSuperRegion();
		List<SuperRegionAnnotations> superRegionAnnotations = superRegion.getAnnotations();
		boolean out = false;
		if (superRegionAnnotations.contains(SuperRegionAnnotations.CAN_BE_TAKEN_BY_OPPONENT)) {
			out = true;
		}
		return out;
	}

	/**
	 * Heuristics about when the spot isn't worth defending or worth attacking,
	 * Except for the sake of killing opponent armies or working our way through
	 * to more important spots.
	 * 
	 * @param state
	 * @param region
	 * @return
	 */
	private static boolean isUnimportantSpot(BotState state, Region region,
			List<RegionAnnotations> regionAnnotationsSoFar) {
		if (regionAnnotationsSoFar.contains(RegionAnnotations.ENTRANCE_TO_OWN_SUPERREGION)) {
			return false;
		}
		if (regionAnnotationsSoFar.contains(RegionAnnotations.ENTRANCE_TO_OPPONENT_SUPERREGION)) {
			return false;
		}
		if (regionAnnotationsSoFar.contains(RegionAnnotations.SPOT_IN_OWN_SUPERREGION)) {
			return false;
		}
		if (regionAnnotationsSoFar.contains(RegionAnnotations.SPOT_IN_OPPONENT_SUPERREGION)) {
			return false;
		}
		if (regionAnnotationsSoFar.contains(RegionAnnotations.SUPERREGION_CAN_BE_TAKEN)) {
			return false;
		}
		if (regionAnnotationsSoFar.contains(RegionAnnotations.CAN_TAKE_SUPERREGION)) {
			return false;
		}
		// if the region is part of Australia or South America then it's not
		// unimportant.
		if (region.getSuperRegion().getId() == 2 || region.getSuperRegion().getId() == 6) {
			return false;
		}
		// in all other cases return true
		return true;

	}

	public enum SuperRegionAnnotations {
		CAN_BE_TAKEN, CAN_BE_TAKEN_BY_OPPONENT, CAN_PUSH_OPPONENT_OUT, CAN_GET_PUSHED_OUT
	}

	public enum RegionAnnotations {
		SPOT_IN_OPPONENT_SUPERREGION, SPOT_IN_OWN_SUPERREGION, ENTRANCE_TO_OWN_SUPERREGION,
		ENTRANCE_TO_OPPONENT_SUPERREGION, CAN_GET_PUSHED_OUT, CAN_PUSH_OPPONENT_OUT, CAN_TAKE_SUPERREGION,
		SUPERREGION_CAN_BE_TAKEN, UNIMPORTANT_SPOT;
	}

}
