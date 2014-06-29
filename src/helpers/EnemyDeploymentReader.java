package helpers;

import java.util.ArrayList;
import java.util.List;

import move.Move;
import move.PlaceArmiesMove;
import bot.BotState;

/**
 * This class is responsible for giving information about the known opponent
 * deployment during the game.
 */
public class EnemyDeploymentReader {
	/**
	 * 
	 */
	public static List<KnownEnemyDeployments> knownEnemyDeploymentsPerTurn = new ArrayList<>();

	/**
	 * 
	 * @param state
	 */
	public static void readEnemyDeployment(BotState state) {
		List<Move> enemyMoves = state.getOpponentMoves();
		List<PlaceArmiesMove> enemyDeploymentMoves = filterDeploymentMoves(enemyMoves);
		knownEnemyDeploymentsPerTurn.add(new KnownEnemyDeployments(enemyDeploymentMoves));
	}

	public static int getKnownEnemyDeploymentLastTurn() {
		int knownDeployment = 0;
		// in case we call this function first turn.
		if (knownEnemyDeploymentsPerTurn.size() > 0) {
			KnownEnemyDeployments k = knownEnemyDeploymentsPerTurn.get(knownEnemyDeploymentsPerTurn.size() - 1);
			knownDeployment = k.getAmountOfEnemyDeployment();
		}
		// KnownEnemyDeployments k =
		// knownEnemyDeploymentsPerTurn.get(knownEnemyDeploymentsPerTurn.size()-1);
		// return k.getAmountOfEnemyDeployment();
		return knownDeployment;
	}

	/**
	 * 
	 * @param enemyMoves
	 * @return
	 */
	private static List<PlaceArmiesMove> filterDeploymentMoves(List<Move> enemyMoves) {
		List<PlaceArmiesMove> out = new ArrayList<>();
		for (Move enemyMove : enemyMoves) {
			if (enemyMove.getClass().equals(PlaceArmiesMove.class)) {
				PlaceArmiesMove enemyArmyPlacement = (PlaceArmiesMove) enemyMove;
				out.add(enemyArmyPlacement);
			}
		}
		return out;
	}

	/**
	 * 
	 *
	 */
	public static class KnownEnemyDeployments {
		private List<PlaceArmiesMove> knownEnemyDeploymentsInTurn;

		/**
		 * 
		 * @param knownEnemyDeploymentsInTurn
		 */
		public KnownEnemyDeployments(List<PlaceArmiesMove> knownEnemyDeploymentsInTurn) {
			this.knownEnemyDeploymentsInTurn = knownEnemyDeploymentsInTurn;
		}

		/**
		 * 
		 * @return
		 */
		public int getAmountOfEnemyDeployment() {
			int amount = 0;
			for (PlaceArmiesMove placeArmiesMove : knownEnemyDeploymentsInTurn) {
				amount = amount + placeArmiesMove.getArmies();
			}
			return amount;
		}
	}
}
