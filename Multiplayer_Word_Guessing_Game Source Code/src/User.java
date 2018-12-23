
public class User {
	
	public static long serialVersionUID = 1;
	//public: accessible by everyone
	//static only 1 instance of it

	private String username;
	private String password;
	private int wins = 0;
	private int losses = 0;
	
	public User(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public int getWins() {
		return wins;
	}
	
	public void setWins(int numWins) {
		wins = numWins;
	}
	
	public int getLosses() {
		return losses;
	}
	
	public void setLosses(int numLosses) {
		losses = numLosses;
	}
	
	public void incrementWins() {
		wins++;
	}
	
	public void incrementLosses() {
		losses++;
	}
	

}
