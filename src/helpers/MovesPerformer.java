package helpers;

import java.util.List;

import move.AttackTransferMove;
import move.PlaceArmiesMove;
import strategy.MovesChooser.Moves;
import bot.BotState;

/**
 * This class is responsible for keeping track of the moves performed during the
 * turn.
 */
public class MovesPerformer {

	public static void performMoves(BotState state, Moves moves) {
		performDeployments(state, moves.armyPlacementMoves);
		performAttackTransferMoves(state, moves.attackTransferMoves);
	}

	public static void performDeployments(BotState state, List<PlaceArmiesMove> placeArmiesMoves) {
		for (PlaceArmiesMove placeArmiesMove : placeArmiesMoves) {
			performDeployment(state, placeArmiesMove);
		}
	}

	public static void performDeployment(BotState state, PlaceArmiesMove placeArmiesMove) {
		placeArmiesMove.getRegion().addArmyPlacement(placeArmiesMove);
	}

	public static void performAttackTransferMoves(BotState state, List<AttackTransferMove> attackTransferMoves) {
		for (AttackTransferMove attackTransferMove : attackTransferMoves) {
			performAttackTransferMove(state, attackTransferMove);
		}
	}

	public static void performAttackTransferMove(BotState state, AttackTransferMove attackTransferMove) {
		attackTransferMove.getFromRegion().addOutgoingMove(attackTransferMove);
		attackTransferMove.getToRegion().addIncomingMove(attackTransferMove);
	}

}
