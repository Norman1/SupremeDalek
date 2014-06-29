package bot;

import helpers.EnemyDeploymentReader;

import java.util.ArrayList;
import java.util.List;

import main.Region;
import model.HeuristicMapModel;
import model.MapModel;
import move.AttackTransferMove;
import move.PlaceArmiesMove;
import strategy.MovesChooser;
import strategy.NeededArmiesCalculator;
import strategy.StartingRegionChooser;
import basicAlgorithms.DistanceToBorderCalculator;
import basicAlgorithms.DistanceToOpponentBorderCalculator;
import debug.PrintDebugOutput;
import evaluation.RegionAndSuperRegionAnnotator;
import evaluation.RegionValueCalculator;
import evaluation.SimpleHeuristics;

public class BotStarter implements Bot {

	@Override
	public ArrayList<Region> getPreferredStartingRegions(BotState state, Long timeOut) {
		ArrayList<Region> preferredStartingRegions = StartingRegionChooser.getPreferredStartingRegions(state);
		// Store the IDs of the picks so that we can calculate afterwards which
		// of them we lost.
		List<Integer> pickIDs = new ArrayList<>();
		for (Region region : preferredStartingRegions) {
			pickIDs.add(region.getId());
		}
		MapModel.storePicks(pickIDs);
		return preferredStartingRegions;
	}

	@Override
	public List<PlaceArmiesMove> getPlaceArmiesMoves(BotState state, Long timeOut) {
		PrintDebugOutput.createDebugOutputBeginTurn(state);
		updateOwnMapHeuristics(state);
		PrintDebugOutput.createDebugOutput(state);
		MovesChooser.calculateMoves(state);
		return MovesChooser.getPlaceArmiesMoves();
	}

	@Override
	public List<AttackTransferMove> getAttackTransferMoves(BotState state, Long timeOut) {
		return MovesChooser.getAttackTransferMoves();
	}

	private static void updateOwnMapHeuristics(BotState state) {
		DistanceToBorderCalculator.calculateDistanceToBorder(state);
		SimpleHeuristics.calculateOwnedFractions(state);
		SimpleHeuristics.calculateMostDesirableSuperregion(state);
		EnemyDeploymentReader.readEnemyDeployment(state);
		MapModel.updateMapModel(state);
		HeuristicMapModel.guessOpponentSuperRegions(state);
		NeededArmiesCalculator.calculateNeededArmies(HeuristicMapModel.getGuessedOpponentIncome(), state);
		DistanceToOpponentBorderCalculator.calculateDistanceToOpponentBorder(state);
		RegionValueCalculator.calculateRegionValues(state);
		RegionAndSuperRegionAnnotator.annotate(state);
	}

	public static void main(String[] args) {
		BotParser parser = new BotParser(new BotStarter());
		parser.run();
	}

}
