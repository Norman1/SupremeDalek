package debug;

public class IDNameMapper {
	
	public static String getSuperRegionName(int superRegionID){
		String out = "";
		switch(superRegionID){
		case 1:
			out = "North America";
			break;
		case 2:
			out = "South America";
			break;
		case 3:
			out = "Europe";
			break;
		case 4:
			out = "Africa";
			break;
		case 5:
			out = "Asia";
			break;
		case 6:
			out = "Australia";
			break;
		}
		return out;
	}
	
	public static String getRegionName(int regionID){
		String out = "";
		switch(regionID){
		case 1 :
			out = "Alaska";
		break;
		case 2:
			out = "Northwest Territory";
			break;
		case 3:
			out = "Greenland";
			break;
		case 4:
			out = "Alberta";
			break;
		case 5:
			out = "Ontario";
			break;
		case 6:
			out = "Quebec";
			break;
		case 7:
			out = "Western United States";
			break;
		case 8:
			out = "Eastern United States";
			break;
		case 9:
			out = "Central America";
			break;
		case 10:
			out = "Venezuela";
			break;
		case 11:
			out = "Peru";
			break;
		case 12:
			out = "Brazil";
			break;
		case 13:
			out = "Argentinia";
			break;
		case 14:
			out = "Iceland";
			break;
		case 15:
			out = "Great Britain";
			break;
		case 16:
			out = "Scandinavia";
			break;
		case 17:
			out = "Ukraine";
			break;
		case 18:
			out = "Western Europe";
			break;
		case 19:
			out = "Northern Europe";
			break;
		case 20:
			out = "Southern Europe";
			break;
		case 21:
			out = "North Africa";
			break;
		case 22:
			out = "Egypt";
			break;
		case 23:
			out = "East Africa";
			break;
		case 24:
			out = "Congo";
			break;
		case 25:
			out = "South Africa";
			break;
		case 26:
			out = "Madagascar";
			break;
		case 27:
			out = "Ural";
			break;
		case 28:
			out = "Siberia";
			break;
		case 29:
			out = "Yakutsk";
			break;
		case 30:
			out = "Kamchatka";
			break;
		case 31:
			out = "Irkutsk";
			break;
		case 32:
			out = "Kazakhstan";
			break;
		case 33:
			out = "China";
			break;
		case 34:
			out = "Mongolia";
			break;
		case 35:
			out = "Japan";
			break;
		case 36:
			out = "Middle East";
			break;
		case 37:
			out = "India";
			break;
		case 38:
			out = "Siam";
			break;
		case 39:
			out = "Indonesia";
			break;
		case 40:
			out = "New Guinea";
			break;
		case 41:
			out = "Western Australia";
			break;
		case 42:
			out = "Eastern Australia";
			break;			
		}
		return out;
	}


}
