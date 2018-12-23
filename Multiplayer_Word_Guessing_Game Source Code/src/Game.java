import java.util.ArrayList;

public class Game {
	
	public static long serialVersionUID = 1;
	
	private String gameName = "";
	private int originalNumPlayers = 0;
	private int numPlayers = 0;
	private int currNumPlayers = 1;
	
	private ArrayList<String> listOfPlayerNames = new ArrayList<String>();
	
	public Game(String gameName, int numPlayers) {
		this.gameName = gameName;
		this.originalNumPlayers = numPlayers;
		this.numPlayers = numPlayers;
	}
	
	public String getName() {
		return gameName;
	}
	
	public int getOriginalNumPlayers() {
		return originalNumPlayers;
	}

	public int getNumPlayers() {
		return numPlayers;
	}
	
	public void setNumPlayers(int numPlayers) {
		this.numPlayers = numPlayers;
	}
	
	public int getCurrNumPlayers() {
		return currNumPlayers;
	}
	
	public void setCurrNumPlayers(int currNumPlayers) {
		this.currNumPlayers = currNumPlayers;
	}
	
	public void incrementCurrNumPlayers() {
		currNumPlayers++;
	}
	
	public void decrementNumPlayers() {
		numPlayers--;
	}
	
	public void addNameToListofPlayerNames(String name) {
		listOfPlayerNames.add(name);
	}
	
	public ArrayList<String> getListOfPlayerNames(){
		return listOfPlayerNames;
	}
	
	public void setListOfPlayerNames(ArrayList<String> listOfPlayerNames) {
		this.listOfPlayerNames = listOfPlayerNames;
	}
	
}
