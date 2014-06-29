package strategy;

import helpers.MovesPerformer;

import java.util.ArrayList;
import java.util.List;

import main.Region;
import main.SuperRegion;
import model.HeuristicMapModel;
import move.AttackTransferMove;
import move.PlaceArmiesMove;
import strategy.MovesChooser.Moves;
import strategy.NeededArmiesCalculator.NeededAttackArmies;
import strategy.NeededArmiesCalculator.NeededDefenseArmies;
import basicAlgorithms.ShortestPathCalculator;
import bot.BotState;
import evaluation.GameStateCalculator;
import evaluation.OpponentDeploymentGuesser;
import evaluation.RegionValueCalculator;

public class FightingMovesChooser {

	private static int stillAvailableArmies;

	public static Moves getFightingMoves(BotState state, int armiesForFighting) {
		stillAvailableArmies = armiesForFighting;
		Moves out = new Moves();
		out = performStep1(state);
		// out = MovesChooser.mergeMoves(out, performStep1(state));
		out = MovesChooser.mergeMoves(out, performStep2(state));
		out = MovesChooser.mergeMoves(out, performStep3(state));
		out = MovesChooser.mergeMoves(out, performStep4(state));
		out = MovesChooser.mergeMoves(out, performStep6(state));
		out = MovesChooser.mergeMoves(out, performStep7(state));
		out = MovesChooser.mergeMoves(out, performStep9(state));
		out = MovesChooser.mergeMoves(out, performStep8(state));
		// out = MovesChooser.mergeMoves(out, performStep10(state));
		out = MovesChooser.mergeMoves(out, performStep11(state));
		out = MovesChooser.mergeMoves(out, performStep12(state));
		out = MovesChooser.mergeMoves(out, performStep13(state));
		out = MovesChooser.mergeMoves(out, performStep14(state));
		out = MovesChooser.mergeMoves(out, performStep15(state));
		out = MovesChooser.mergeMoves(out, performStep16(state));
		performStep17(state);
		out = MovesChooser.mergeMoves(out, performStep18(state));
		return out;
	}

	public static Moves getLateFightingMoves(BotState state) {
		Moves out = new Moves();
		out = MovesChooser.mergeMoves(out, performStep19(state));
		out = MovesChooser.mergeMoves(out, performStep20(state));
		out = MovesChooser.mergeMoves(out, performStep0(state));
		return out;
	}

	public static Moves getCleanupMoves(BotState state, Moves movesSoFar) {
		Moves out = MovesChooser.mergeMoves(movesSoFar, performCleanupStep0(state));
		performCleanupStep1(state, out);
		out = MovesChooser.mergeMoves(out, performCleanupStep2(state, out));
		return out;
	}

	private static Moves performStep0(BotState state) {
		Moves out = new Moves();
		List<Region> distanceTwoRegions = new ArrayList<>();
		for (Region region : state.getVisibleMap().getOwnedRegions(state)) {
			if (region.getDistanceToOpponentBorder() == 2 && region.getIdleArmies() > 0) {
				distanceTwoRegions.add(region);
			}
		}
		for (Region distanceTwoRegion : distanceTwoRegions) {
			Region bestRegionToTransferTo = getBestRegionToTransferTo(state, distanceTwoRegion);
			if (bestRegionToTransferTo != distanceTwoRegion) {
				AttackTransferMove attackTransferMove = new AttackTransferMove(state.getMyPlayerName(),
						distanceTwoRegion, bestRegionToTransferTo, distanceTwoRegion.getIdleArmies());
				MovesPerformer.performAttackTransferMove(state, attackTransferMove);
				out.attackTransferMoves.add(attackTransferMove);
			}
		}
		return out;
	}

	private static Moves performStep1(BotState state) {
		Moves out = new Moves();
		List<Region> highImportanceDefenceRegions = RegionValueCalculator
				.getOrderedListOfHighImportanceDefenceRegions(state);
		for (Region region : highImportanceDefenceRegions) {
			NeededDefenseArmies nda = NeededArmiesCalculator.getNeededDefenseArmies(region);
			int maxArmies = nda.defendingArmiesFullDeployment;
			int minArmies = nda.defendingArmiesNoDeployment;
			Moves newMoves = getDefensePlan(state, region, minArmies, maxArmies);
			out = MovesChooser.mergeMoves(out, newMoves);
		}
		return out;
	}

	private static Moves performStep2(BotState state) {
		Moves out = new Moves();
		List<Region> highImportanceAttackRegions = RegionValueCalculator
				.getOrderedListOfHighImportanceAttackRegions(state);
		for (Region opponentRegion : highImportanceAttackRegions) {
			NeededAttackArmies nta = NeededArmiesCalculator.getNeededAttackArmies(opponentRegion);
			int maxArmies = nta.overkillArmiesFullDeployment;
			int minArmies = nta.crushingArmiesFullDeployment;
			Moves newMoves = getBestAttackPlan(state, opponentRegion, minArmies, maxArmies);
			out = MovesChooser.mergeMoves(out, newMoves);
		}
		return out;
	}

	private static Moves performStep3(BotState state) {
		Moves out = new Moves();
		List<Region> highImportanceDefenceRegions = RegionValueCalculator
				.getOrderedListOfHighImportanceDefenceRegions(state);
		for (Region region : highImportanceDefenceRegions) {
			NeededDefenseArmies nda = NeededArmiesCalculator.getNeededDefenseArmies(region);
			int maxArmies = nda.fairDefenceArmiesDeployment;
			int minArmies = nda.fairDefenceArmiesDeployment;
			Moves newMoves = getDefensePlan(state, region, minArmies, maxArmies);
			out = MovesChooser.mergeMoves(out, newMoves);

		}
		return out;
	}

	private static Moves performStep4(BotState state) {
		Moves out = new Moves();
		List<Region> highImportanceAttackRegions = RegionValueCalculator
				.getOrderedListOfHighImportanceAttackRegions(state);
		for (Region opponentRegion : highImportanceAttackRegions) {
			NeededAttackArmies nta = NeededArmiesCalculator.getNeededAttackArmies(opponentRegion);
			int maxArmies = nta.fairFightArmiesFullDeployment;
			int minArmies = nta.fairFightArmiesFullDeployment;
			Moves newMoves = getBestAttackPlan(state, opponentRegion, minArmies, maxArmies);
			out = MovesChooser.mergeMoves(out, newMoves);
		}
		return out;
	}

	private static Moves performStep6(BotState state) {
		Moves out = new Moves();
		List<Region> highImportanceAttackRegions = RegionValueCalculator
				.getOrderedListOfHighImportanceAttackRegions(state);
		for (Region opponentRegion : highImportanceAttackRegions) {
			if (opponentRegion.getIncomingArmies() > 0 && stillAvailableArmies > 0) {
				AttackTransferMove bestAttack = opponentRegion.getIncomingMoves().get(0);
				PlaceArmiesMove pam = new PlaceArmiesMove(state.getMyPlayerName(), bestAttack.getFromRegion(),
						stillAvailableArmies);
				out.totalDeployment += pam.getArmies();
				MovesPerformer.performDeployment(state, pam);
				out.armyPlacementMoves.add(pam);
				bestAttack.setArmies(bestAttack.getArmies() + stillAvailableArmies);
				stillAvailableArmies = 0;
			}
		}
		return out;
	}

	private static Moves performStep7(BotState state) {
		Moves out = new Moves();
		List<Region> mediumImportanceDefenceRegions = RegionValueCalculator
				.getOrderedListOfMediumImportanceDefenceRegions(state);
		for (Region region : mediumImportanceDefenceRegions) {
			NeededDefenseArmies nda = NeededArmiesCalculator.getNeededDefenseArmies(region);
			boolean opponentDeploysToNeighbor = false;
			for (Region oppoentNeighbor : region.getEnemyNeighbors(state)) {
				if (OpponentDeploymentGuesser.getGuessedOpponentDeployment(state, oppoentNeighbor) > 0) {
					opponentDeploysToNeighbor = true;
				}
			}
			int minArmies = 0;
			if (opponentDeploysToNeighbor) {
				minArmies = nda.defendingArmiesFullDeployment;
			} else {
				minArmies = nda.defendingArmiesNoDeployment;
			}
			Moves newMoves = getDefensePlan(state, region, minArmies, minArmies);
			out = MovesChooser.mergeMoves(out, newMoves);
		}
		return out;
	}

	private static Moves performStep8(BotState state) {
		Moves out = new Moves();
		List<Region> mediumImportanceAttackRegions = RegionValueCalculator
				.getOrderedListOfMediumImportanceAttackRegions(state);
		for (Region opponentRegion : mediumImportanceAttackRegions) {
			int maxArmies = 0;
			int minArmies = 0;
			NeededAttackArmies nta = NeededArmiesCalculator.getNeededAttackArmies(opponentRegion);
			// if (state.getVisibleMap().getEnemyRegions(state).size() == 1) {
			// maxArmies = getIdleArmiesSurroundingOpponentRegion(state,
			// opponentRegion) + stillAvailableArmies;
			// minArmies = nta.fairFightArmiesNoDeployment;
			// } else {
			// maxArmies = getIdleArmiesSurroundingOpponentRegion(state,
			// opponentRegion) + stillAvailableArmies;
			// minArmies = nta.fairFightArmiesNoDeployment;
			// }
			if (OpponentDeploymentGuesser.getGuessedOpponentDeployment(state, opponentRegion) > 0) {
				// maxArmies = nta.crushingArmiesFullDeployment;
				maxArmies = getIdleArmiesSurroundingOpponentRegion(state, opponentRegion) + stillAvailableArmies;
				minArmies = nta.fairFightArmiesFullDeployment;
			} else {
				// maxArmies = nta.crushingArmiesNoDeployment;
				maxArmies = getIdleArmiesSurroundingOpponentRegion(state, opponentRegion) + stillAvailableArmies;
				minArmies = nta.fairFightArmiesNoDeployment;
			}
			if (maxArmies > 0) {
				System.err.println("maxArmies: " + maxArmies);
			}
			Moves newMoves = getBestAttackPlan(state, opponentRegion, minArmies, maxArmies);
			out = MovesChooser.mergeMoves(out, newMoves);
		}

		return out;
	}

	private static Moves performStep9(BotState state) {
		Moves out = new Moves();
		List<Region> mediumImportantDefenceRegions = RegionValueCalculator
				.getOrderedListOfMediumImportanceDefenceRegions(state);
		for (Region region : mediumImportantDefenceRegions) {
			NeededDefenseArmies nda = NeededArmiesCalculator.getNeededDefenseArmies(region);
			int maxArmies = nda.fairDefenceArmiesDeployment;
			int minArmies = nda.defendingArmiesNoDeployment;
			Moves newMoves = getDefensePlan(state, region, minArmies, maxArmies);
			out = MovesChooser.mergeMoves(out, newMoves);
		}
		return out;
	}

	private static Moves performStep10(BotState state) {
		Moves out = new Moves();
		List<Region> mediumImportanceAttackRegions = RegionValueCalculator
				.getOrderedListOfMediumImportanceAttackRegions(state);
		for (Region opponentRegion : mediumImportanceAttackRegions) {
			if (opponentRegion.getIncomingArmies() > 0 && stillAvailableArmies > 0) {
				AttackTransferMove bestAttack = opponentRegion.getIncomingMoves().get(0);
				PlaceArmiesMove pam = new PlaceArmiesMove(state.getMyPlayerName(), bestAttack.getFromRegion(),
						stillAvailableArmies);
				out.totalDeployment += pam.getArmies();
				MovesPerformer.performDeployment(state, pam);
				out.armyPlacementMoves.add(pam);
				bestAttack.setArmies(bestAttack.getArmies() + stillAvailableArmies);
				stillAvailableArmies = 0;
			}
		}
		return out;
	}

	private static Moves performStep11(BotState state) {
		Moves out = new Moves();
		List<Region> worthwileRegions = new ArrayList<>();
		worthwileRegions.addAll(RegionValueCalculator.getOrderedListOfHighImportanceDefenceRegions(state));
		worthwileRegions.addAll(RegionValueCalculator.getOrderedListOfMediumImportanceDefenceRegions(state));
		for (Region worthwileRegion : worthwileRegions) {
			NeededDefenseArmies nda = NeededArmiesCalculator.getNeededDefenseArmies(worthwileRegion);
			int neededArmies = nda.fairDefenceArmiesDeployment
					- worthwileRegion.getArmiesAfterDeploymentAndIncomingMoves(state);
			int minimumArmies = nda.defendingArmiesNoDeployment
					- worthwileRegion.getArmiesAfterDeploymentAndIncomingMoves(state);
			if (neededArmies > 0 && worthwileRegion.getEnemyNeighbors(state).size() > 0) {
				List<AttackTransferMove> supportMoves = getNecessarySupportMoves(state, worthwileRegion, neededArmies,
						minimumArmies);
				out.attackTransferMoves.addAll(supportMoves);
				MovesPerformer.performAttackTransferMoves(state, supportMoves);
			}
		}
		return out;
	}

	// private static Moves performStep11_2(BotState state) {
	// Moves out = new Moves();
	// List<Region> worthwileRegions = new ArrayList<>();
	// worthwileRegions.addAll(RegionValueCalculator.getOrderedListOfHighImportanceDefenceRegions(state));
	// worthwileRegions.addAll(RegionValueCalculator.getOrderedListOfMediumImportanceDefenceRegions(state));
	// for (Region worthwileRegion : worthwileRegions) {
	// NeededDefenseArmies nda =
	// NeededArmiesCalculator.getNeededDefenseArmies(worthwileRegion);
	// int neededArmies = nda.defendingArmiesFullDeployment
	// - worthwileRegion.getArmiesAfterDeploymentAndIncomingMoves(state);
	// if (neededArmies > 0 && worthwileRegion.getEnemyNeighbors(state).size() >
	// 0) {
	// List<AttackTransferMove> supportMoves = getNecessarySupportMoves(state,
	// worthwileRegion, neededArmies);
	// out.attackTransferMoves.addAll(supportMoves);
	// MovesPerformer.performAttackTransferMoves(state, supportMoves);
	// }
	// }
	// return out;
	// }

	private static List<AttackTransferMove> getNecessarySupportMoves(BotState state, Region neededHelpRegion,
			int missingArmies, int minimumUsefulArmies) {
		List<AttackTransferMove> out = new ArrayList<>();
		List<Region> ownedNeighbors = new ArrayList<>();
		ownedNeighbors.addAll(neededHelpRegion.getOwnedNeighbors(state));
		ownedNeighbors = RegionValueCalculator.sortRegionsByDefenceRegionValue(state, ownedNeighbors);
		int stillMissingArmies = missingArmies;
		for (int i = ownedNeighbors.size() - 1; i >= 0; i--) {
			Region ownedNeighbor = ownedNeighbors.get(i);
			int neededDefenceArmies;
			if (ownedNeighbor.getDefenceRegionValue() < RegionValueCalculator.LOWEST_MEDIUM_PRIORITY_VALUE
					|| (neededHelpRegion.getDefenceRegionValue() >= RegionValueCalculator.LOWEST_HIGH_PRIORITY_VALUE && ownedNeighbor
							.getDefenceRegionValue() < RegionValueCalculator.LOWEST_HIGH_PRIORITY_VALUE)) {
				neededDefenceArmies = 0;
			} else {
				neededDefenceArmies = NeededArmiesCalculator.getNeededDefenseArmies(ownedNeighbor).defendingArmiesFullDeployment;
			}
			int possibleSpareArmies = Math.min(ownedNeighbor.getIdleArmies(), ownedNeighbor.getArmiesAfterDeployment()
					- neededDefenceArmies);
			int transferingArmies = Math.min(stillMissingArmies, possibleSpareArmies);
			if (transferingArmies > 0) {
				AttackTransferMove atm = new AttackTransferMove(state.getMyPlayerName(), ownedNeighbor,
						neededHelpRegion, transferingArmies);
				out.add(atm);
			}
			stillMissingArmies -= transferingArmies;
			minimumUsefulArmies -= transferingArmies;
		}
		// if (stillMissingArmies == 0) {
		if (minimumUsefulArmies <= 0) {
			return out;
		} else {
			return new ArrayList<AttackTransferMove>();
		}
	}

	private static Moves performStep12(BotState state) {
		Moves out = new Moves();
		List<Region> lowImportanceAttackRegions = RegionValueCalculator
				.getOrderedListOfLowImportanceAttackRegions(state);
		for (Region opponentRegion : lowImportanceAttackRegions) {
			int maxArmies = 0;
			int minArmies = 0;
			NeededAttackArmies nta = NeededArmiesCalculator.getNeededAttackArmies(opponentRegion);

			if (state.getVisibleMap().getEnemyRegions(state).size() == 1) {
				maxArmies = nta.crushingArmiesFullDeployment;
				minArmies = nta.fairFightArmiesFullDeployment;
			} else {
				maxArmies = nta.crushingArmiesFullDeployment;
				minArmies = nta.fairFightArmiesNoDeployment;
			}

			// if (OpponentDeploymentGuesser.getGuessedOpponentDeployment(state,
			// opponentRegion) > 0) {
			// maxArmies = nta.crushingArmiesFullDeployment;
			// minArmies = nta.crushingArmiesFullDeployment;
			// } else {
			// maxArmies = nta.crushingArmiesNoDeployment;
			// minArmies = nta.crushingArmiesNoDeployment;
			// }
			Moves newMoves = getBestAttackPlan(state, opponentRegion, minArmies, maxArmies);
			out = MovesChooser.mergeMoves(out, newMoves);
		}

		return out;
	}

	private static Moves performStep13(BotState state) {
		Moves out = new Moves();
		List<Region> lowImportanceDefenceRegions = RegionValueCalculator
				.getOrderedListOfLowImportanceDefenceRegions(state);
		for (Region region : lowImportanceDefenceRegions) {
			NeededDefenseArmies nda = NeededArmiesCalculator.getNeededDefenseArmies(region);
			boolean opponentDeploysToNeighbor = false;
			for (Region oppoentNeighbor : region.getEnemyNeighbors(state)) {
				if (OpponentDeploymentGuesser.getGuessedOpponentDeployment(state, oppoentNeighbor) > 0) {
					opponentDeploysToNeighbor = true;
				}
			}
			int minArmies = 0;
			if (opponentDeploysToNeighbor) {
				minArmies = nda.fairDefenceArmiesDeployment;
			} else {
				minArmies = nda.fairDefenceArmiesNoDeployment;
			}
			Moves newMoves = getDefensePlan(state, region, minArmies, minArmies);
			out = MovesChooser.mergeMoves(out, newMoves);
		}
		return out;
	}

	private static Moves performStep14(BotState state) {
		Moves out = new Moves();
		List<Region> lowImportanceAttackRegions = RegionValueCalculator
				.getOrderedListOfLowImportanceAttackRegions(state);
		for (Region opponentRegion : lowImportanceAttackRegions) {
			NeededAttackArmies nta = NeededArmiesCalculator.getNeededAttackArmies(opponentRegion);
			int maxArmies = nta.crushingArmiesFullDeployment;
			int minArmies = nta.fairFightArmiesFullDeployment;
			Moves newMoves = getBestAttackPlan(state, opponentRegion, minArmies, maxArmies);
			out = MovesChooser.mergeMoves(out, newMoves);
		}
		return out;
	}

	private static Moves performStep15(BotState state) {
		Moves out = new Moves();
		List<Region> lowImportanceDefenceRegions = RegionValueCalculator
				.getOrderedListOfLowImportanceDefenceRegions(state);
		for (Region region : lowImportanceDefenceRegions) {
			NeededDefenseArmies nda = NeededArmiesCalculator.getNeededDefenseArmies(region);
			int maxArmies = nda.fairDefenceArmiesDeployment;
			// int minArmies = 0;
			int minArmies = nda.fairDefenceArmiesDeployment;
			Moves newMoves = getDefensePlan(state, region, minArmies, maxArmies);
			out = MovesChooser.mergeMoves(out, newMoves);
		}
		return out;
	}

	private static Moves performStep16(BotState state) {
		// give armies to attack region
		Moves out = new Moves();
		List<Region> sortedAttackRegions = RegionValueCalculator.getOrderedListOfAttackRegions(state);
		for (Region opponentRegion : sortedAttackRegions) {
			if (opponentRegion.getIncomingArmies() > 0 && stillAvailableArmies > 0) {
				AttackTransferMove bestAttack = opponentRegion.getIncomingMoves().get(0);
				PlaceArmiesMove pam = new PlaceArmiesMove(state.getMyPlayerName(), bestAttack.getFromRegion(),
						stillAvailableArmies);
				MovesPerformer.performDeployment(state, pam);
				out.totalDeployment += pam.getArmies();
				out.armyPlacementMoves.add(pam);
				bestAttack.setArmies(bestAttack.getArmies() + stillAvailableArmies);
				stillAvailableArmies = 0;
			}
		}
		// give armies to defence region
		List<Region> sortedDefenceRegions = RegionValueCalculator.getOrderedListOfDefenceRegions(state);
		for (Region region : sortedDefenceRegions) {
			if (stillAvailableArmies > 0) {
				PlaceArmiesMove pam = new PlaceArmiesMove(state.getMyPlayerName(), region, stillAvailableArmies);
				MovesPerformer.performDeployment(state, pam);
				out.totalDeployment += stillAvailableArmies;
				out.armyPlacementMoves.add(pam);
				stillAvailableArmies = 0;
			}
		}
		return out;
	}

	private static void performStep17(BotState state) {
		for (Region opponentBorderingRegion : state.getVisibleMap().getOpponentBorderingRegions(state)) {
			List<Region> opponentNeighbors = opponentBorderingRegion.getEnemyNeighbors(state);
			List<Region> sortedOpponentNeighbors = RegionValueCalculator.sortRegionsByAttackRegionValue(state,
					opponentNeighbors);
			for (Region opponentNeighbor : sortedOpponentNeighbors) {
				// check whether we attack this neighbor.
				AttackTransferMove theMove = null;
				for (AttackTransferMove receivingMove : opponentNeighbor.getIncomingMoves()) {
					if (receivingMove.getFromRegion().equals(opponentBorderingRegion)) {
						theMove = receivingMove;
					}
				}
				if (theMove != null && opponentBorderingRegion.getIdleArmies() > 0) {
					theMove.setArmies(theMove.getArmies() + opponentBorderingRegion.getIdleArmies());
				}
			}
		}
	}

	private static Moves performStep18(BotState state) {
		Moves out = new Moves();
		for (Region opponentBorderingRegion : state.getVisibleMap().getOpponentBorderingRegions(state)) {
			// > 3 so not to small attack
			if (opponentBorderingRegion.getIdleArmies() > 3) {
				List<Region> opponentNeighbors = opponentBorderingRegion.getEnemyNeighbors(state);
				List<Region> sortedOpponentNeighbors = RegionValueCalculator.sortRegionsByAttackRegionValue(state,
						opponentNeighbors);
				Region firstAttackedOpponentNeighbor = null;
				for (Region opponentNeighbor : sortedOpponentNeighbors) {
					if (opponentNeighbor.getIncomingArmies() > 0 && firstAttackedOpponentNeighbor == null) {
						firstAttackedOpponentNeighbor = opponentNeighbor;
					}
				}
				if (firstAttackedOpponentNeighbor != null) {
					AttackTransferMove atm = new AttackTransferMove(state.getMyPlayerName(), opponentBorderingRegion,
							firstAttackedOpponentNeighbor, opponentBorderingRegion.getIdleArmies());
					out.attackTransferMoves.add(atm);
					MovesPerformer.performAttackTransferMove(state, atm);
				}
			}
		}
		return out;
	}

	private static Moves performStep19(BotState state) {
		Moves out = new Moves();
		List<SuperRegion> guessedSuperRegions = HeuristicMapModel.getGuessedOpponentSuperRegions();
		List<Region> superRegionRegions = new ArrayList<>();
		for (SuperRegion guessedSuperRegion : guessedSuperRegions) {
			superRegionRegions.addAll(guessedSuperRegion.getSubRegions());
		}
		List<Region> potentialMoveRegions = new ArrayList<>();
		for (Region borderRegion : state.getVisibleMap().getBorderRegions(state)) {
			if (superRegionRegions.size() > 0
					&& borderRegion.getIdleArmies() >= 20
					&& (borderRegion.getDefenceRegionValue() < RegionValueCalculator.LOWEST_MEDIUM_PRIORITY_VALUE || borderRegion
							.getEnemyNeighbors(state).size() == 0)) {
				potentialMoveRegions.add(borderRegion);
			}
		}
		for (Region potentialMoveRegion : potentialMoveRegions) {
			List<Region> shortestPath = ShortestPathCalculator.getShortestPathToRegions(state, potentialMoveRegion,
					superRegionRegions);
			if (shortestPath.size() <= 4 && shortestPath.size() > 2) {
				Region toRegion = shortestPath.get(1);
				if (toRegion.getPlayerName().equals("neutral")) {
					AttackTransferMove atm = new AttackTransferMove(state.getMyPlayerName(), potentialMoveRegion,
							toRegion, potentialMoveRegion.getIdleArmies());
					out.attackTransferMoves.add(atm);
				}
			}
		}
		MovesPerformer.performMoves(state, out);
		return out;
	}

	private static Moves performStep20(BotState state) {
		Moves out = new Moves();
		if (state.getVisibleMap().getEnemyRegions(state).size() == 0) {
			for (Region borderRegion : state.getVisibleMap().getBorderRegions(state)) {
				if (borderRegion.getIdleArmies() >= 20) {
					Region closestNeighborToOpponent = borderRegion.getClosestNeighborRegionToOpponentBorder(state);
					AttackTransferMove atm = new AttackTransferMove(state.getMyPlayerName(), borderRegion,
							closestNeighborToOpponent, borderRegion.getIdleArmies());
					out.attackTransferMoves.add(atm);
				}
			}
		}
		MovesPerformer.performMoves(state, out);
		return out;
	}

	private static Moves performCleanupStep0(BotState state) {
		Moves out = new Moves();
		for (Region region : state.getVisibleMap().getOpponentBorderingRegions(state)) {
			if (region.getDefenceRegionValue() < RegionValueCalculator.LOWEST_MEDIUM_PRIORITY_VALUE
					&& region.getIdleArmies() > 0) {
				Region bestRegionToTransferTo = getBestRegionToTransferTo(state, region);
				if (bestRegionToTransferTo != region) {
					AttackTransferMove attackTransferMove = new AttackTransferMove(state.getMyPlayerName(), region,
							bestRegionToTransferTo, region.getIdleArmies());
					out.attackTransferMoves.add(attackTransferMove);
					MovesPerformer.performAttackTransferMove(state, attackTransferMove);
				}
			}
		}

		return out;
	}

	/**
	 * Calculates the best region to transfer to. fromRegion has to have a
	 * distance to the opponent border of 1 or 2.
	 * 
	 * If there is a medium or high importance neighbor then tansfer to that
	 * region. If all neighbors are of low value then transfer to the neighbor
	 * with the biggest stack.
	 * 
	 * @param state
	 * @param fromRegion
	 * @return
	 */
	private static Region getBestRegionToTransferTo(BotState state, Region fromRegion) {
		List<Region> neighbors = fromRegion.getOwnedNeighbors(state);
		List<Region> opponentBorderingNeighbors = new ArrayList<>();
		for (Region neighbor : neighbors) {
			if (neighbor.getEnemyNeighbors(state).size() > 0) {
				opponentBorderingNeighbors.add(neighbor);
			}
		}
		List<Region> highImportanceDefenceRegions = new ArrayList<>();
		List<Region> mediumImportanceDefenceRegions = new ArrayList<>();
		List<Region> lowImportanceDefenceRegions = new ArrayList<>();
		for (Region neighbor : opponentBorderingNeighbors) {
			if (neighbor.getDefenceRegionValue() > RegionValueCalculator.LOWEST_HIGH_PRIORITY_VALUE) {
				highImportanceDefenceRegions.add(neighbor);
			} else if (neighbor.getDefenceRegionValue() > RegionValueCalculator.LOWEST_MEDIUM_PRIORITY_VALUE) {
				mediumImportanceDefenceRegions.add(neighbor);
			} else {
				lowImportanceDefenceRegions.add(neighbor);
			}
		}
		Region out = fromRegion;
		boolean foundSolution = false;
		if (highImportanceDefenceRegions.size() > 0) {
			out = getRegionWithMostMissingArmies(state, highImportanceDefenceRegions);
			foundSolution = true;
		}
		if (!foundSolution && mediumImportanceDefenceRegions.size() > 0) {
			out = getRegionWithMostMissingArmies(state, mediumImportanceDefenceRegions);
			foundSolution = true;
		}
		if (!foundSolution) {
			for (Region lowImportanceNeighbor : lowImportanceDefenceRegions) {
				if (lowImportanceNeighbor.getArmiesAfterDeploymentAndIncomingMoves(state) > out
						.getArmiesAfterDeploymentAndIncomingMoves(state)) {
					out = lowImportanceNeighbor;
				}
			}
		}
		return out;
	}

	private static Region getRegionWithMostMissingArmies(BotState state, List<Region> in) {
		Region out = in.get(0);
		int maximumMissingArmies = NeededArmiesCalculator.getNeededDefenseArmies(out).fairDefenceArmiesDeployment
				- out.getArmiesAfterDeploymentAndIncomingMoves(state);
		for (Region region : in) {
			int neededDefenceArmies = NeededArmiesCalculator.getNeededDefenseArmies(region).fairDefenceArmiesDeployment;
			int currentArmies = region.getArmiesAfterDeploymentAndIncomingMoves(state);
			int missingArmies = neededDefenceArmies - currentArmies;
			if (missingArmies > maximumMissingArmies) {
				out = region;
				maximumMissingArmies = missingArmies;
			}
		}
		return out;
	}

	private static void performCleanupStep1(BotState state, Moves movesSoFar) {
		List<Region> regionsToConsider = new ArrayList<>();
		for (Region region : state.getVisibleMap().getOpponentBorderingRegions(state)) {
			if (region.getIncomingArmies() > 0 && region.getOutgoingMoves().size() == 0
			// && region.getDefenceRegionValue() <
			// RegionValueCalculator.LOWEST_MEDIUM_PRIORITY_VALUE
			) {
				regionsToConsider.add(region);
			}
		}
		for (Region region : regionsToConsider) {
			int biggestOpponentAttack = getBiggestPossibleIncomingAttack(state, region);
			AttackTransferMove biggestTransfer = getBiggestIncomingTransfer(state, region);
			boolean isTransferingBetter = isTransferingBetter(state, region, biggestOpponentAttack, biggestTransfer);
			if (isTransferingBetter) {
				int armiesToShift = region.getArmiesAfterDeployment() - region.getArmies();
				movesSoFar.armyPlacementMoves.removeAll(region.getArmyPlacement());
				region.getArmyPlacement().clear();
				Region fromRegion = biggestTransfer.getFromRegion();
				PlaceArmiesMove pam = new PlaceArmiesMove(state.getMyPlayerName(), fromRegion, armiesToShift);
				movesSoFar.armyPlacementMoves.add(pam);
				fromRegion.addArmyPlacement(pam);
				biggestTransfer.setArmies(biggestTransfer.getArmies() + armiesToShift);
			}
		}
	}

	private static Moves performCleanupStep2(BotState state, Moves movesSoFar) {
		Moves out = new Moves();
		boolean meaningfulMovePresent = false;
		for (AttackTransferMove attackTransferMove : movesSoFar.attackTransferMoves) {
			if (!attackTransferMove.getToRegion().getPlayerName().equals(state.getMyPlayerName())) {
				meaningfulMovePresent = true;
			}
		}
		System.err.println("meaningfulMovePresent: " + meaningfulMovePresent);
		String gameState = GameStateCalculator.calculateGameState(state);
		if (!meaningfulMovePresent && gameState.equals("won")) {
			Region biggestArmiesRegion = null;
			int mostArmies = 0;
			for (Region region : state.getVisibleMap().getOpponentBorderingRegions(state)) {
				if (region.getIdleArmies() > mostArmies) {
					mostArmies = region.getIdleArmies();
					biggestArmiesRegion = region;
				}
			}
			if (biggestArmiesRegion != null && biggestArmiesRegion.getIdleArmies() >= 100) {
				System.err.println("biggestArmiesRegion != null && biggestArmiesRegion.getIdleArmies() >= 100");
				Region opponentNeighborRegion = biggestArmiesRegion.getEnemyNeighbors(state).get(0);
				int mostOpponentArmies = opponentNeighborRegion.getArmies()
						+ OpponentDeploymentGuesser.getGuessedOpponentDeployment(state, opponentNeighborRegion);
				double currentFraction = mostArmies / mostOpponentArmies;
				double ownKills = mostArmies * 0.6;
				double opponentKills = mostOpponentArmies * 0.7;
				double newFraction = (mostArmies - opponentKills) / (mostOpponentArmies - ownKills);
				System.err.println("currentFraction: " + currentFraction);
				System.err.println("newFraction: " + newFraction);
				if (newFraction > currentFraction) {
					AttackTransferMove attackTransferMove = new AttackTransferMove(state.getMyPlayerName(),
							biggestArmiesRegion, opponentNeighborRegion, biggestArmiesRegion.getIdleArmies());
					out.attackTransferMoves.add(attackTransferMove);
				}
			}
		}
		MovesPerformer.performMoves(state, out);
		return out;
	}

	private static boolean isTransferingBetter(BotState state, Region ourRegion, int biggestOpponentAttack,
			AttackTransferMove biggestTransfer) {
		// Option that we deploy instead of transfering more (current option)
		// (option A)
		double ourKillsOptionA = 0;
		double opponentKillsOptionA = 0;
		ourKillsOptionA = Math.min(ourRegion.getArmiesAfterDeployment() * 0.7, biggestOpponentAttack);
		opponentKillsOptionA = Math.min(biggestOpponentAttack * 0.6, ourRegion.getArmiesAfterDeployment());
		double remainingOpponentArmiesA = opponentKillsOptionA == ourRegion.getArmiesAfterDeployment() ? biggestOpponentAttack
				- ourKillsOptionA
				: 0;
		if (remainingOpponentArmiesA > 0) {
			ourKillsOptionA += Math.min(biggestTransfer.getArmies() * 0.6, remainingOpponentArmiesA);
			opponentKillsOptionA += Math.min(biggestTransfer.getArmies(), remainingOpponentArmiesA * 0.7);
		}
		double differenceOptionA = ourKillsOptionA - opponentKillsOptionA;
		// Option that we transfer more instead of deploying (new option)
		// (option B)
		double ourKillsOptionB = 0;
		double opponentKillsOptionB = 0;
		ourKillsOptionB = Math.min(ourRegion.getArmies() * 0.7, biggestOpponentAttack);
		opponentKillsOptionB = Math.min(biggestOpponentAttack * 0.6, ourRegion.getArmies());
		double remainingOpponentArmiesB = opponentKillsOptionB == ourRegion.getArmies() ? biggestOpponentAttack
				- ourKillsOptionB : 0;
		if (remainingOpponentArmiesB > 0) {
			int biggerTransfer = biggestTransfer.getArmies()
					+ (ourRegion.getArmiesAfterDeployment() - ourRegion.getArmies());
			ourKillsOptionB += Math.min(biggerTransfer * 0.6, remainingOpponentArmiesB);
			opponentKillsOptionB += Math.min(biggerTransfer, remainingOpponentArmiesB * 0.7);
		}
		double differenceOptionB = ourKillsOptionB - opponentKillsOptionB;
		return differenceOptionB > differenceOptionA ? true : false;
	}

	private static AttackTransferMove getBiggestIncomingTransfer(BotState state, Region region) {
		AttackTransferMove biggestTransfer = region.getIncomingMoves().get(0);
		for (AttackTransferMove atm : region.getIncomingMoves()) {
			if (atm.getArmies() > biggestTransfer.getArmies()) {
				biggestTransfer = atm;
			}
		}
		return biggestTransfer;
	}

	private static int getBiggestPossibleIncomingAttack(BotState state, Region ourRegion) {
		int biggestAttack = 0;
		List<Region> opponentNeighbors = ourRegion.getEnemyNeighbors(state);
		for (Region opponentNeighbor : opponentNeighbors) {
			if (opponentNeighbor.getArmies() + HeuristicMapModel.getGuessedOpponentIncome() - 1 > biggestAttack) {
				biggestAttack = opponentNeighbor.getArmies() + HeuristicMapModel.getGuessedOpponentIncome() - 1;
			}
		}
		return biggestAttack;
	}

	/**
	 * Gives the valuable ownRegion armies. At best maxArmies and and least
	 * minArmies. Already available armies are considered.
	 * 
	 * Returns empty moves if the region can't gain minArmies.
	 * 
	 * @param state
	 * @param ownRegion
	 * @param minArmies
	 * @param maxArmies
	 * @return
	 */
	private static Moves getDefensePlan(BotState state, Region ownRegion, int minArmies, int maxArmies) {
		Moves out = new Moves();
		int currentArmies = ownRegion.getArmiesAfterDeployment();
		int missingArmies = Math.max(0, maxArmies - currentArmies);
		int spentArmies = Math.min(missingArmies, stillAvailableArmies);
		if (spentArmies > 0) {
			out.totalDeployment += spentArmies;
			out.armyPlacementMoves.add(new PlaceArmiesMove(state.getMyPlayerName(), ownRegion, spentArmies));
		}
		int newArmies = currentArmies + spentArmies;
		if (newArmies >= minArmies) {
			MovesPerformer.performMoves(state, out);
			stillAvailableArmies -= out.totalDeployment;
			return out;
		} else {

			return new Moves();
		}
	}

	/**
	 * Calculates the best attack plan to attack the opponentRegion. This
	 * involves a possible usage of deploying armies and a possible usage of
	 * attacking the opponentRegion from different spots.
	 * 
	 * Returns empty moves if minArmies restriction can't be satisfied.
	 * 
	 * @param state
	 * @param opponentRegion
	 * @param minArmies
	 * @param maxArmies
	 * @return
	 */
	private static Moves getBestAttackPlan(BotState state, Region opponentRegion, int minArmies, int maxArmies) {
		Moves out = new Moves();
		List<Region> sortedOwnedNeighbors = getOrderedListOfRegionsByIdleArmies(state,
				opponentRegion.getOwnedNeighbors(state));
		// Try to fulfill at least the minArmies constraint by attacking
		// with all neighboring territories and deploying. First deploy and then
		// pull in more territories.
		int attackedWithSoFar = 0;
		for (int i = 0; i < sortedOwnedNeighbors.size(); i++) {
			if (i == 0) {
				int neededDeployment = Math.max(0, maxArmies - sortedOwnedNeighbors.get(0).getIdleArmies());
				int totalDeployment = Math.min(neededDeployment, stillAvailableArmies);
				if (totalDeployment > 0) {
					out.totalDeployment += totalDeployment;
					out.armyPlacementMoves.add(new PlaceArmiesMove(state.getMyPlayerName(),
							sortedOwnedNeighbors.get(0), totalDeployment));
				}
				int attackingArmies = Math
						.min(maxArmies, sortedOwnedNeighbors.get(0).getIdleArmies() + totalDeployment);
				out.attackTransferMoves.add(new AttackTransferMove(state.getMyPlayerName(),
						sortedOwnedNeighbors.get(0), opponentRegion, attackingArmies));
				attackedWithSoFar += attackingArmies;

			} else {
				// i != 0
				int stillNeededArmies = maxArmies - attackedWithSoFar;
				if (stillNeededArmies > 0 && sortedOwnedNeighbors.get(i).getIdleArmies() > 1) {
					int newAttackingArmies = Math.min(stillNeededArmies, sortedOwnedNeighbors.get(i).getIdleArmies());
					out.attackTransferMoves.add(new AttackTransferMove(state.getMyPlayerName(), sortedOwnedNeighbors
							.get(i), opponentRegion, newAttackingArmies));
					attackedWithSoFar += newAttackingArmies;
				}
			}
		}
		// If the calculated attack plan satisfies the minArtmies constraint
		// then return him. Else return null.
		if (attackedWithSoFar >= minArmies) {
			MovesPerformer.performMoves(state, out);
			stillAvailableArmies -= out.totalDeployment;
			return out;
		} else {
			return new Moves();
		}

	}

	private static List<Region> getOrderedListOfRegionsByIdleArmies(BotState state, List<Region> in) {
		List<Region> out = new ArrayList<>();
		List<Region> copy = new ArrayList<>();
		copy.addAll(in);
		while (!copy.isEmpty()) {
			Region highestIdleArmiesRegion = copy.get(0);
			for (Region region : copy) {
				if (region.getIdleArmies() > highestIdleArmiesRegion.getIdleArmies()) {
					highestIdleArmiesRegion = region;
				}
			}
			copy.remove(highestIdleArmiesRegion);
			out.add(highestIdleArmiesRegion);
		}
		return out;
	}

	private static int getIdleArmiesSurroundingOpponentRegion(BotState state, Region opponentRegion) {
		int idleArmies = 0;
		for (Region neighbor : opponentRegion.getOwnedNeighbors(state)) {
			idleArmies += neighbor.getIdleArmies();
		}
		return idleArmies;
	}

}
