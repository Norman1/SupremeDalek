package strategy;

import helpers.MovesPerformer;

import java.util.ArrayList;
import java.util.List;

import main.Region;
import move.AttackTransferMove;
import strategy.MovesChooser.Moves;
import basicAlgorithms.DistanceToBorderCalculator;
import bot.BotState;

public class TransferMovesChooser {

	/**
	 * Calculates the transfer moves.
	 * 
	 * This method is responsible for transfer moves from one of our regions to
	 * one of our other regions under following conditions. The from region
	 * isn't allowed to border an opponent but bordering a neutral region is
	 * allowed. The to region is allowed to border an opponent but it's not the
	 * task of this method to perform the transfer move then in an extremely
	 * meaningful way. If the to region borders an opponent then the
	 * corresponding transfer move is better calculated in the class
	 * FightingMovesChooser.
	 * 
	 * This method is meant to be called after all other methods responsible for
	 * moving armies around.
	 * 
	 * @param state
	 * @return
	 */
	public static Moves getTransferMoves2(BotState state) {
		Moves out = new Moves();
		List<Region> transferRegions = new ArrayList<>();
		transferRegions.addAll(state.getVisibleMap().getOwnedRegions(state));
		transferRegions.removeAll(state.getVisibleMap().getOpponentBorderingRegions(state));
		List<Region> transferRegionsWithNoIdleArmies = new ArrayList<>();
		for (Region region : transferRegions) {
			if (region.getIdleArmies() == 0) {
				transferRegionsWithNoIdleArmies.add(region);
			}
		}
		transferRegions.removeAll(transferRegionsWithNoIdleArmies);
		for (Region fromRegion : transferRegions) {
			AttackTransferMove transferMove = calculateTransferMoveWithoutDelays(state, fromRegion);
			if (transferMove != null) {
				out.attackTransferMoves.add(transferMove);
			}
		}
		MovesPerformer.performMoves(state, out);
		return out;
	}

	private static AttackTransferMove calculateTransferMoveWithoutDelays(BotState state, Region fromRegion) {
		List<Region> bestRegionsToTransferTo = calculateBestRegionsToTransferTo(state, fromRegion);
		if (bestRegionsToTransferTo.size() == 0) {
			return null;
		}
		Region bestRegion = bestRegionsToTransferTo.get(0);
		AttackTransferMove out = new AttackTransferMove(state.getMyPlayerName(), fromRegion, bestRegion,
				fromRegion.getIdleArmies());
		return out;
	}

	private static List<Region> calculateBestRegionsToTransferTo(BotState state, Region transferRegion) {
		List<Region> closestNeighborsToBorder = DistanceToBorderCalculator.getClosestOwnedNeighborsToBorder(state,
				transferRegion);
		Region closestNeighborToOpponent = transferRegion.getClosestOwnedNeighborToOpponentBorder(state);
		List<Region> out = new ArrayList<>();
		if (closestNeighborsToBorder.size() == 0 && closestNeighborToOpponent == null) {
			return out;
		}
		if (closestNeighborToOpponent == null) {
			return closestNeighborsToBorder;
		}
		if (closestNeighborsToBorder.size() == 0) {
			out.add(closestNeighborToOpponent);
			return out;
		}
		int distanceToBorder = closestNeighborsToBorder.get(0).getDistanceToBorder();
		int distanceToOpponentBorder = closestNeighborToOpponent.getDistanceToOpponentBorder();
		// heuristic when to transfer to opponent border instead of closest
		// border
		if (distanceToOpponentBorder <= distanceToBorder + 3) {
			out.add(closestNeighborToOpponent);
		} else {
			out.addAll(closestNeighborsToBorder);
		}

		return out;
	}

}
