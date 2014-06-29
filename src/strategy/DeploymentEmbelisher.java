package strategy;

import java.util.ArrayList;
import java.util.List;

import move.PlaceArmiesMove;

public class DeploymentEmbelisher {
	public static ArrayList<PlaceArmiesMove> embelishDeployment(
			List<PlaceArmiesMove> in) {
		ArrayList<PlaceArmiesMove> out = new ArrayList<>();
		for (PlaceArmiesMove inDeployMove : in) {
			boolean newRegion = true;
			for (PlaceArmiesMove outDeployMove : out) {
				if (outDeployMove.getRegion().equals(inDeployMove.getRegion())) {
					newRegion = false;
					outDeployMove.setArmies(inDeployMove.getArmies()
							+ outDeployMove.getArmies());
				}
			}
			if (newRegion) {
				out.add(inDeployMove);
			}
		}
		return out;
	}
}
