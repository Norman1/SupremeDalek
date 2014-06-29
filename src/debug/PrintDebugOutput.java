package debug;

import java.util.ArrayList;
import java.util.List;

import main.Region;
import move.PlaceArmiesMove;
import bot.BotState;
import evaluation.GameStateCalculator;
import evaluation.OpponentDeploymentGuesser;
import evaluation.RegionAndSuperRegionAnnotator.RegionAnnotations;

public class PrintDebugOutput {

	private static String getAnnotations(BotState state) {
		List<Region> relevantRegions = new ArrayList<>();
		for (Region region : state.getVisibleMap().getRegions()) {
			if (region.getPlayerName().equals(state.getMyPlayerName())
					|| region.getPlayerName().equals(state.getOpponentPlayerName())
					|| region.getPlayerName().equals("neutral")) {
				relevantRegions.add(region);
			}
		}
		String out = "";
		for (Region region : relevantRegions) {
			String info = IDNameMapper.getRegionName(region.getId()) + " --> ";
			List<RegionAnnotations> annotations = region.getAnnotations();
			for (RegionAnnotations annotation : annotations) {
				info += annotation + ", ";
			}
			info += "\n";
			out += info;
		}

		return out;
	}

	public static void createDebugOutputBeginTurn(BotState state) {
		System.err.println("=============== Round Nr: " + state.getRoundNumber() + " ===============");
	}

	public static void createDebugOutput(BotState state) {
		System.err.println(getAnnotations(state));
		// System.err.println("Opponent deployed visible: " +
		// EnemyDeploymentReader.getKnownEnemyDeploymentLastTurn());
		// System.err.println("Guessed opponent income: " +
		// HeuristicMapModel.getGuessedOpponentIncome());
		// List<SuperRegion> guessedOppnentSuperRegions =
		// HeuristicMapModel.getGuessedOpponentSuperRegions();
		// System.err.print("Guessed opponent SuperRegions: ");
		// for (SuperRegion superRegion : guessedOppnentSuperRegions) {
		// System.err.print(IDNameMapper.getSuperRegionName(superRegion.getId())
		// + ", ");
		// }
		// System.err.println();
		// List<Region> guessedOpponentRegions =
		// HeuristicMapModel.getGuessedOpponentRegions();
		// System.err.print("Guessed opponent Regions: ");
		// for (Region region : guessedOpponentRegions) {
		// System.err.print(IDNameMapper.getRegionName(region.getId()) + ", ");
		// }
		// System.err.println();
		// System.err.println("Guessed game state: " +
		// GameStateCalculator.calculateGameState(state));
		// PlaceArmiesMove opponentDeployment =
		// OpponentDeploymentGuesser.guessOpponentDeployment(state);
		// System.err.print("Guessed opponent deployment: ");
		// if (opponentDeployment == null) {
		// System.err.println(" - ");
		// } else {
		// System.err.println(opponentDeployment.getArmies() + " armies to "
		// +
		// IDNameMapper.getRegionName(opponentDeployment.getRegion().getId()));
		// }
	}

}
