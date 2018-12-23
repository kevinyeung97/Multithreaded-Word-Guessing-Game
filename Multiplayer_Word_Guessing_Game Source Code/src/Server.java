import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Server {
	
	static Connection conn = null;
	
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	public int numMessages = 0;
	public int numMessages2 = 0;
	public int numMessages3 = 0;
	
	public Vector<ServerThread> serverThreads;
	public static Map<String, Game> runningGames = new HashMap<String, Game>();
	public static Set<String> runningGamesNames = new HashSet<String>();
	
	static ArrayList<String> secretWords = new ArrayList<String>();
	
	static ArrayList<String> masterUsernameAL = new ArrayList<String>();	//stores list of names of everyone in the game, used to print out records
	
	public Boolean TwoPlayerGuessWrongWordVariable = true;
	
	public static void main(String [] args) {
		Scanner scan = new Scanner(System.in);
		String inputFilename = new String();
		BufferedReader br = null;
		Boolean inputErrorExists = false;
				
		while(inputErrorExists == false) {
			try {
				System.out.print("What is the name of the input file? (Server-side) ");
				inputFilename = scan.nextLine();
				br = new BufferedReader(new FileReader(inputFilename));				
				inputErrorExists = true;
			} catch(FileNotFoundException fnfe) {
				System.out.println("Configuration file " + inputFilename + " could not be found.");
				System.out.println("");
			}

		}
		
		String ServerHostname = "";
		String ServerPort = "";
		String DBConnection = "";
		String DBUsername = "";
		String DBPassword = "";
		String SecretWordFile = "";
		
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream(inputFilename));
			
			for(int i = 0; i < 6; i++) {
				if(i == 0) {
					ServerHostname = prop.getProperty("ServerHostname");
					if(ServerHostname.equals("") == true) {
						System.out.println("");
						System.out.println("ServerHostname is a required parameter in the configuration file");
						System.exit(0);
					}
				}
				if(i == 1) {
					ServerPort = prop.getProperty("ServerPort");
					if(ServerPort.equals("") == true) {
						System.out.println("");
						System.out.println("ServerPort is a required parameter in the configuration file");
						System.exit(0);
					}
				}
				else if(i == 2) {
					DBConnection = prop.getProperty("DBConnection");
					if(DBConnection.equals("") == true) {
						System.out.println("");
						System.out.println("DBConnection is a required parameter in the configuration file");
						System.exit(0);
					}
				}
				else if(i == 3) {
					DBUsername = prop.getProperty("DBUsername");
					if(DBUsername.equals("") == true) {
						System.out.println("");
						System.out.println("DBUsername is a required parameter in the configuration file");
						System.exit(0);
					}
				}
				else if(i == 4) {
					DBPassword = prop.getProperty("DBPassword");
					if(DBPassword.equals("") == true) {
						System.out.println("");
						System.out.println("DBPassword is a required parameter in the configuration file");
						System.exit(0);
					}
				}
				else if(i == 5) {
					SecretWordFile = prop.getProperty("SecretWordFile");
					if(SecretWordFile.equals("") == true) {
						System.out.println("");
						System.out.println("SecretWordFile is a required parameter in the configuration file");
						System.exit(0);
					}
				}
			}
		} catch(IOException ioe) {
			System.out.println("ioe near server 124: " + ioe.getMessage());
		}
		
		
		System.out.println("");
		System.out.println("Reading config file...");
		System.out.println("Server Hostname - " + ServerHostname);
		System.out.println("ServerPort - " + ServerPort);
		System.out.println("Database Connection String - " + DBConnection);
		System.out.println("Database Username - " + DBUsername);
		System.out.println("Database Password - " + DBPassword);
		System.out.println("Secret Word File - " + SecretWordFile);
		System.out.println("");
				
	
		
		//Connecting to MySQL Database
		//conn is declared up top, conn is a shared global variable
		try {
			Class.forName("com.mysql.jdbc.Driver");	
			conn = DriverManager.getConnection(DBConnection + "?user=" + DBUsername + "&password=" + DBPassword + "&useSSL=false");
			
			System.out.println("Trying to connect to database...Connected! (Server-side)");
			System.out.println("");
																	
																															
		}catch(SQLException sqle) {
			System.out.println("Trying to connect to database...Unable to connect to database " + DBConnection + " with username " + DBUsername + " and password " + DBPassword + ".");
		}catch(ClassNotFoundException cnfe) {
			System.out.println("Trying to connect to database...Unable to connect to database " + DBConnection + " with username " + DBUsername + " and password " + DBPassword + ".");
		}
		
		
		try{
		    BufferedReader br2 = new BufferedReader(new FileReader(SecretWordFile));
		    String line = br2.readLine();
		    while(line != null) {
		        String[] wordsLine = line.split(" ");
		        for(String word : wordsLine) {
		            secretWords.add(word);
		        }
		        line = br2.readLine();
		    }

		} catch (Exception e) {
		    System.out.println("Couldn't generate random word from file");
		}
		
		
		
		
		
		
		Server server = new Server(Integer.parseInt(ServerPort));
		
		
		
		

		
		
	} //end of main
	
	
	
	
	public Server(int port) {
		ServerSocket ss = null;
		
		try {
			ss = new ServerSocket(port); 
			serverThreads = new Vector<ServerThread>();
			while(true) {								
				Socket s = ss.accept();
				ServerThread st = new ServerThread(s, this); 
				serverThreads.add(st);	
			}
			
		} catch(IOException ioe) {
			System.out.println("ioe near server 203: " + ioe.getMessage());
		} finally {
			try {
				if(ss != null) {
					ss.close();
				}
				
			} catch(IOException ioe) {
				System.out.println("ioe near server 211: " + ioe.getMessage());
			}
		}
		
	}	//end of Server
	
	
	
	
	
	
	
	
	
	public void login(String msg, ServerThread currentST) {
	    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
	    Date date = new Date();
	    
		String username = msg.substring(0, msg.indexOf(','));
		String password = msg.substring(msg.indexOf(',')+1);
		System.out.println(dateFormat.format(date) + " " + username + " - " + "trying to log in with password " + password + ".");
		
		
		Statement st = null;
		ResultSet rs = null;
		try {
			st = conn.createStatement();
			String sql = "SELECT * FROM Users WHERE username = '" + username + "';" ;
			rs = st.executeQuery(sql);
			
			if(rs.next() == false) {	//account does not exist yet, create new account
				System.out.println(dateFormat.format(date) + " " + username + " - " + "does not have an account so not successfully logged in.");
				Message newMessage = new Message("login", username + "," + password);
				this.broadcast(newMessage, currentST);
			}
			
			rs.beforeFirst();
			
			if(rs.next() == true) {	//account already exists
				String retrievedPassword = rs.getString("password");
				if(retrievedPassword.equals(password)) {
					dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
				    date = new Date();
					System.out.println(dateFormat.format(date) + " " + username + " - " + "successfully logged in");
					//tell client to continue game
					Message newMessage = new Message("loginSuccessfully", username + "," + password);
					this.broadcast(newMessage, currentST);
				}
				else {			//user typed in the wrong password
					dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
				    date = new Date();
					System.out.println(dateFormat.format(date) + " " + username + " - " + "has an account but not successfully logged in.");
					//Tell client to insert login credentials again
					Message newMessage = new Message("loginAgain,WrongPassword", "");
					this.broadcast(newMessage, currentST);
				}
			
			}
			
		} catch (SQLException sqle) {
			System.out.println("sqle Server 274: " + sqle.getMessage());
		}
	}
	
	public void createNewAccount(String msg, ServerThread currentST) {
		Statement st = null;
		try {
			st = conn.createStatement();
			String username = msg.substring(0, msg.indexOf(','));
			String password = msg.substring(msg.indexOf(',')+1);
			String sql = "INSERT INTO Users (username, password, wins, losses) VALUES ('" + username + "', '" + password + "', '" + 0 + "', '" + 0 + "');";
			st.executeUpdate(sql);
			DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
		    Date date = new Date();
			System.out.println(dateFormat.format(date) + " " + username + " - " + "created an account with password " + password + ".");
			Message newMessage = new Message("loginSuccessfully", username + "," + password);
			this.broadcast(newMessage, currentST);
			
		} catch (SQLException sqle) {
			System.out.println("sqle in server 288: " + sqle.getMessage());
		}
	}
	
	
	public void getRecord1(String msg, ServerThread currentST) {
		Statement st = null;
		ResultSet rs = null;
		try {
			st = conn.createStatement();
			String username = msg.substring(0, msg.indexOf(','));
			String password = msg.substring(msg.indexOf(',')+1);
			String sql = "SELECT * FROM Users WHERE username = '" + username + "';" ;			
			rs = st.executeQuery(sql);
			
			String numWins = "";
			String numLosses = "";
			
			while(rs.next()) {
				numWins = rs.getString("wins");
				numLosses = rs.getString("losses");
			}
			
			DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
		    Date date = new Date();
			System.out.println(dateFormat.format(date) + " " + username + " - " + "has record " + numWins + " wins and " + numLosses + " losses.");
			
			Message newMessage = new Message("printRecord1", username + "," + numWins + "," + numLosses);
			this.broadcast(newMessage, currentST);
			
			
		} catch (SQLException sqle) {
			System.out.println("sqle in server 323: " + sqle.getMessage());
		}
	}
	
	public void doNothing1(String msg, ServerThread currentST) {
		String username = msg;
		Message newMessage = new Message("startOrJoinGame", username);
		this.broadcast(newMessage, currentST);
	}
	
	
	
	
	public void checkIfGameExists(String msg, ServerThread currentST) {
		String [] msgArr = msg.split(",");
		
		String username = msgArr[0];
		String gameName = msgArr[1];
		
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
	    Date date = new Date();
		System.out.println(dateFormat.format(date) + " " + username + " - " + "wants to start a game called " + gameName + ".");
		
		Boolean gameNameExists = runningGamesNames.contains(gameName);
		
		if(gameNameExists == true) {	//game already exists
			dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
			date = new Date();
			System.out.println(dateFormat.format(date) + " " + username + " - " + gameName + " already exists, so unable to start " + gameName + ".");
			Message newMessage = new Message("startOrJoinGame2", username + "," + gameName);
			this.broadcast(newMessage, currentST);
		}
		else if (gameNameExists == false) {	//game doesn't exist yet, proceed to ask for # of users
			Message newMessage = new Message("askForNumPlayers", username + "," + gameName);
			this.broadcast(newMessage, currentST);
		}
		
	}
	
	
	
	public void createGame(String msg, ServerThread currentST) {	
		String [] msgArr = msg.split(",");
		
		String username = msgArr[0];
		String gameName = msgArr[1];
		String numPlayers = msgArr[2];							//runningGamesNames <String> set
		
		int numPlayersInt = Integer.parseInt(numPlayers);		//runningGames <String, Game> hashMap
		if(runningGamesNames.contains(gameName) == false) {		//game doesnt exist yet, create new game
			runningGamesNames.add(gameName);
			Game newGame = new Game(gameName, numPlayersInt);			//CHANGE TO INSERT INTO A HASHMAP, NEED a name as key, and game object as value. Need to do this so that when new user joins a game, they can increment the currNumPlayers of that particular game
			newGame.addNameToListofPlayerNames(username);
			runningGames.put(gameName,newGame);
			
			DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
		    Date date = new Date();
			System.out.println(dateFormat.format(date) + " " + username + " - " + "successfully started a game " + gameName + ".");
			
			if(newGame.getCurrNumPlayers() == numPlayersInt) {		//single player, immediately start game	
				Random rand = new Random(System.currentTimeMillis());
			    String randomSecretWord = secretWords.get(rand.nextInt(secretWords.size())).toUpperCase();
			    
				String emptyWord = "";
				for(int i = 0; i < randomSecretWord.length(); i++) {
					emptyWord += "_ ";
				}
										
				Message newMessage = new Message("startGame1", emptyWord + "," + username + "," + randomSecretWord + "," + "NA" + "," + gameName);		//adding empty stuff to satisfy startGame parameters
				this.broadcast(newMessage, currentST); 
				
				dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
			    date = new Date();
				System.out.println(dateFormat.format(date) + " " + username + " - " + gameName + " has " + numPlayers + " players so starting game.");
				System.out.println("Secret word is " + randomSecretWord);
				
			}
			
			else if (newGame.getCurrNumPlayers() != numPlayersInt) {	//multiplayer, need to wait for other users
				Message newMessage = new Message("waitForAllPlayers", username + "," + gameName + "," + numPlayers);	//////////////////////////////
				this.broadcast(newMessage, currentST);
				
				dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
			    date = new Date();
				System.out.println(dateFormat.format(date) + " " + username + " - " + gameName + " needs " + numPlayers + " players to start game.");
			}
			
			
		}
		
		
	} //end of createGame()
	
	

	
	
	public void joinGameGetRecords(String msg, ServerThread currentST) {
		String [] msgArr = msg.split(",");
		
		String gameName = msgArr[0];
		String currUsername = msgArr[1];

		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
	    Date date = new Date();
		System.out.println(dateFormat.format(date) + " " + currUsername + " - " + "wants to join a game called " + gameName + ".");
		
		if(runningGames.containsKey(gameName) == false) {	//user wants to join a game that doesn't exist
			dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
		    date = new Date();
			System.out.println(dateFormat.format(date) + " " + currUsername + " - " + "wants to join a game called " + gameName + ", but that game doesn't exist.");
			Message newMessage = new Message("startOrJoinGame4", currUsername);
			this.broadcast(newMessage, currentST);
			return;
		}
		
		//join game, add currUsername to list of players for that game, increment numplayers
		Game gameObject = runningGames.get(gameName);
			
		if(gameObject.getNumPlayers() == gameObject.getCurrNumPlayers()) {	//game already has max number of players, prompt client to either start/join game
			dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
			date = new Date();
			System.out.println(dateFormat.format(date) + " " + currUsername + " - " + gameName + " exists, but " + currUsername + " unable to join because maximum # of players have already joined " + gameName + ".");
			Message newMessage = new Message("startOrJoinGame3", currUsername);
			this.broadcast(newMessage, currentST);
			return;
		}
		
		
		gameObject.addNameToListofPlayerNames(currUsername);
		
		gameObject.incrementCurrNumPlayers();
		
		runningGames.replace(gameName, gameObject);
		
		gameObject = runningGames.get(gameName); //this should be the updated version of game object with new user who just joined the game
		
		ArrayList<String> listOfPlayerNames = gameObject.getListOfPlayerNames();
		
		ArrayList<User> listOfTempUsers = new ArrayList<User>();
		
		String passThisMsg = "";
		
		for(int i = 0; i < listOfPlayerNames.size(); i++) {
			if(currUsername != listOfPlayerNames.get(i)) {	//gather all OTHER players info, dont need currUser's info
				Statement st = null;
				ResultSet rs = null;
				try {
					st = conn.createStatement();
					String username = listOfPlayerNames.get(i);
					String sql = "SELECT * FROM Users WHERE username = '" + username + "';" ;			
					rs = st.executeQuery(sql);
					
					String numWins = "";
					String numLosses = "";
					
					while(rs.next()) {
						numWins = rs.getString("wins");
						numLosses = rs.getString("losses");
					}
					
					passThisMsg += username + "," + numWins + "," + numLosses + ",";		//currUsername is the name of the client
					
				} catch(SQLException sqle) {
					System.out.println("sql in Server 474: " + sqle);
				}
			}
			
		}	//end of for loop
																
		
		passThisMsg = passThisMsg.substring(0, passThisMsg.length() - 1);	//remove extra comma at the end
		
		dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
	    date = new Date();
		System.out.println(dateFormat.format(date) + " " + currUsername + " - " + "successfully joined game " + gameName + ".");

		
		Message newMessage = new Message("printCurrPlayersRecords", passThisMsg);	//prints out record of everyone who is currently in the game
		this.broadcast(newMessage, currentST);

		
		Message newMessage1 = new Message("announceNewlyJoinedUser", currUsername + "," + gameName);	
		this.broadcast(newMessage1, currentST);
		
		
		
		
		//start of getRecordForNewlyJoinedUser
		Statement st = null;
		ResultSet rs = null;
		try {
			st = conn.createStatement();
			String sql = "SELECT * FROM Users WHERE username = '" + currUsername + "';" ;			
			rs = st.executeQuery(sql);
			
			String numWins = "";
			String numLosses = "";
			
			while(rs.next()) {
				numWins = rs.getString("wins");
				numLosses = rs.getString("losses");
			}

			Game tempGameObject = runningGames.get(gameName);
			int expectedNumPlayers = tempGameObject.getOriginalNumPlayers();
			int currNumPlayers = tempGameObject.getCurrNumPlayers();
			int remainingNumPlayers = expectedNumPlayers - currNumPlayers;
			
			Message newMessage3 = new Message("printRecordForNewlyJoinedUser", currUsername + "," + numWins + "," + numLosses + "," + gameName + "," + Integer.toString(remainingNumPlayers));
			this.broadcast(newMessage3, currentST);
			
			
		} catch (SQLException sqle) {
			System.out.println("sqle in server 541: " + sqle.getMessage());
		}
		
		
		
		
		
		//start of checkForStartCondition
		Game gameObject2 = runningGames.get(gameName);

		if(gameObject2.getCurrNumPlayers() == gameObject2.getNumPlayers()) {		//all users have joined that game
			Random rand = new Random(System.currentTimeMillis());
			String randomSecretWord = secretWords.get(rand.nextInt(secretWords.size())).toUpperCase();
			
			String emptyWord = "";
			for(int i = 0; i < randomSecretWord.length(); i++) {
				emptyWord += "_ ";
			}
			
			dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
			date = new Date();
			System.out.println(dateFormat.format(date) + " " + currUsername + " - " + gameName + " has " + gameObject.getNumPlayers() + " players so starting game.");
			System.out.println("Secret word is " + randomSecretWord);
			
			masterUsernameAL = gameObject2.getListOfPlayerNames();	//use this later on for printing records of EVERYONE.
					
			ArrayList<String> namesAL = gameObject2.getListOfPlayerNames();
			String usernameOfGuesser = namesAL.get(0);
			
			Message newMessage2 = new Message("startGame2", emptyWord + "," + currUsername + "," + randomSecretWord + "," + usernameOfGuesser + "," + gameName);
			this.broadcast(newMessage2, currentST); 
			
		}
		

	}	//end of join game get records
	
	
	
	
	public void askForGuessInput(String msg, ServerThread currentST) {
		String [] msgArr = msg.split(",");
		

		String emptyWord = msgArr[1];
		String secretWord = msgArr[2];
		String numGuesses = msgArr[3];
		String indexOfGuesserUsername = msgArr[4];
		String gameName = msgArr[5];
											
		
		int indexOfGuesserUsernameInt = Integer.parseInt(indexOfGuesserUsername);
		Game gameObject = runningGames.get(gameName);
		ArrayList<String> namesAL = gameObject.getListOfPlayerNames();
		String usernameOfGuesser = namesAL.get(indexOfGuesserUsernameInt);
		
		
		Message newMessage = new Message("printGameLogicUI", emptyWord + "," + secretWord + "," + numGuesses + "," + indexOfGuesserUsername + "," + usernameOfGuesser + "," + gameName);
		this.broadcast(newMessage, currentST);
	}
	
	
	public void checkGuessedLetter(String msg, ServerThread currentST) {
		String [] msgArr = msg.split(",");
		
		String emptyWord = msgArr[0];
		String secretWord = msgArr[1];
		String numGuesses = msgArr[2];
		String indexOfGuesserUsername = msgArr[3];
		String usernameOfGuesser = msgArr[4];
		String gameName = msgArr[5];
		String guessedLetter = msgArr[6];
					
		
		char[] emptyWordArr = emptyWord.toCharArray();
		
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
		Date date = new Date();
		System.out.println(dateFormat.format(date) + " " + gameName + " " + usernameOfGuesser + " - guessed letter '" + guessedLetter + "'.");
		
		if(secretWord.toUpperCase().contains(guessedLetter.toUpperCase()) && emptyWord.contains(guessedLetter.toUpperCase()) == false) {	//user guessed letter correctly, and letter has not been guessed before
			
			for(int i = 0; i < secretWord.length(); i++) {		//replacing emptyWord __ with actual letter
				if(Character.toString(secretWord.charAt(i)).toUpperCase().equals(guessedLetter.toUpperCase()) == true) {	//userInput is a letter in the randomWord
					emptyWordArr[2*i] = Character.toUpperCase(guessedLetter.charAt(0));
					emptyWord = String.valueOf(emptyWordArr);
				}
			}
			
			ArrayList<Integer> charPositionsAL = new ArrayList<Integer>();	//finding positions of letter (positions only used for printing out to console)
			for(int i = 0; i < secretWord.length(); i++){
			    if(Character.toString(secretWord.charAt(i)).toUpperCase().equals(guessedLetter.toUpperCase())){
			       charPositionsAL.add(i);
			    }
			}
			
			
			dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
			date = new Date();
			System.out.print(dateFormat.format(date) + " " + gameName + " " + usernameOfGuesser + " - '" + guessedLetter + "' is in " + secretWord + " in position(s) ");
			for(int i = 0; i < charPositionsAL.size(); i++) {
				System.out.print(charPositionsAL.get(i));
				System.out.print(" ");
			}
			System.out.println(". Secret word now shows " + emptyWord);	
			
			
			Message newMessage = new Message("printGuessedLetterToOtherClients", emptyWord + "," + secretWord + "," + numGuesses + "," + indexOfGuesserUsername + "," + usernameOfGuesser + "," + gameName + "," + guessedLetter);
			this.broadcast(newMessage, currentST);
			
			
			int indexOfGuesserUsernameInt = Integer.parseInt(indexOfGuesserUsername);

			Game gameObject = runningGames.get(gameName);
			ArrayList<String> namesAL = gameObject.getListOfPlayerNames();
			
			if(gameObject.getOriginalNumPlayers() > 1) {	//multiplayer version changes indexOfGuesser
				indexOfGuesserUsernameInt++;
				if(indexOfGuesserUsernameInt >= gameObject.getNumPlayers()) {	//loops back to first guesser after everyone already made a guess
					indexOfGuesserUsernameInt = 0;
				}
				indexOfGuesserUsername = Integer.toString(indexOfGuesserUsernameInt);
				usernameOfGuesser = namesAL.get(indexOfGuesserUsernameInt);
			}
			
			
//			else if(gameObject.getOriginalNumPlayers() == 2) {
//				if(TwoPlayerGuessWrongWordVariable == true) {
//					int indexOfRemainingGuesser = namesAL.indexOf(usernameOfGuesser);
//					if (indexOfRemainingGuesser == 0) {
//						indexOfRemainingGuesser = 1;
//					}
//					else if (indexOfRemainingGuesser == 1) {
//						indexOfRemainingGuesser = 0;
//					}
//					indexOfGuesserUsername = Integer.toString(indexOfRemainingGuesser);
//					usernameOfGuesser = namesAL.get(indexOfRemainingGuesser);
//					TwoPlayerGuessWrongWordVariable = false;
//				}
//				
//			}
			
			
			Message newMessage2 = new Message("printResultsOfGuessedLetter", emptyWord + "," + secretWord + "," + numGuesses + "," + indexOfGuesserUsername + "," + usernameOfGuesser + "," + gameName + "," + guessedLetter + "," + "1");
			this.broadcast(newMessage2, currentST);	
			
			
		}
		
		
		else if (emptyWord.contains(guessedLetter.toUpperCase())) {			//guessedLetter has already been guessed before
			Message newMessage = new Message("letterAlreadyGuessedBefore", emptyWord + "," + secretWord + "," + numGuesses + "," + indexOfGuesserUsername + "," + usernameOfGuesser + "," + gameName + "," + guessedLetter);
			this.broadcast(newMessage, currentST);
		}
		
		
		else {		//guessedLetter is incorrect			
			dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
			date = new Date();
			System.out.println(dateFormat.format(date) + " " + gameName + " " + usernameOfGuesser + " - '" + guessedLetter + "' is not in " + secretWord);
			
			Message newMessage = new Message("printGuessedLetterToOtherClients", emptyWord + "," + secretWord + "," + numGuesses + "," + indexOfGuesserUsername + "," + usernameOfGuesser + "," + gameName + "," + guessedLetter);
			this.broadcast(newMessage, currentST);
			
			
			int indexOfGuesserUsernameInt = Integer.parseInt(indexOfGuesserUsername);
			Game gameObject = runningGames.get(gameName);
			ArrayList<String> namesAL = gameObject.getListOfPlayerNames();
			
			
			if(gameObject.getOriginalNumPlayers() != 1) {	//multiplayer version changes indexOfGuesser
				indexOfGuesserUsernameInt++;
				if(indexOfGuesserUsernameInt >= gameObject.getNumPlayers()) {	//loops back to first guesser after everyone already made a guess
					indexOfGuesserUsernameInt = 0;
				}
				indexOfGuesserUsername = Integer.toString(indexOfGuesserUsernameInt);
				usernameOfGuesser = namesAL.get(indexOfGuesserUsernameInt);
			}
			
			int numGuessesInt = Integer.parseInt(numGuesses);	//decrement # of remaining guesses after guessing incorrectly
			numGuessesInt--;
			if(numGuessesInt == 0) {  //all players lose after running out of guesses
				Game checkIfSingleOrMultiPlayer = runningGames.get(gameName);
				if(checkIfSingleOrMultiPlayer.getOriginalNumPlayers() == 1) {	//single player mode run out of guesses
					Statement st = null;
					ResultSet rs = null;
					
					String msg1 = "";
					String numWins = "";
					String numLosses = "";
					
					try {
						st = conn.createStatement();
						String sql = "UPDATE Users SET losses = losses + 1 WHERE username = '" + usernameOfGuesser + "';" ;
						st.executeUpdate(sql);
						
						sql = "SELECT * FROM Users WHERE username = '" + usernameOfGuesser + "';" ;	
						rs = st.executeQuery(sql);
						
						while(rs.next()) {					
							numWins = rs.getString("wins");
							numLosses = rs.getString("losses");
							msg1 += usernameOfGuesser + "," + numWins + "," + numLosses;
						}
					} catch(SQLException sqle) {
						System.out.println("sqle in Server 748: " + sqle.getMessage());
					}
					
					dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
					date = new Date();
					System.out.println(dateFormat.format(date) + " " + gameName + " " + usernameOfGuesser + " - " + "ran out of guesses and has lost the game");
					
					Message newMessage2 = new Message("1playerNoGuessesLeft", usernameOfGuesser + "," + numWins + "," + numLosses);
					this.broadcast(newMessage2, currentST);	
					return;
				}
				
				else {	//multi player mode, run out of guesses 		
					Statement st = null;
					ResultSet rs = null;
					
					String msg1 = "";
					String numWins = "";
					String numLosses = "";
					
					try {
						st = conn.createStatement();
						String sql = "";
						for(int i = 0; i < namesAL.size(); i ++) {
							sql = "UPDATE Users SET losses = losses + 1 WHERE username = '" + namesAL.get(i) + "';" ;
							st.executeUpdate(sql);
						}
						
						for(int i = 0; i < masterUsernameAL.size(); i++) {	//namesAL.size()
							sql = "SELECT * FROM Users WHERE username = '" + masterUsernameAL.get(i) + "';" ;	//namesAL.get(i)
							rs = st.executeQuery(sql);
							
							while(rs.next()) {					
								numWins = rs.getString("wins");
								numLosses = rs.getString("losses");
								msg1 += namesAL.get(i) + "," + numWins + "," + numLosses + ",";
							}
						}
						
						msg1 = msg1.substring(0, msg1.length()-1);
						
					} catch(SQLException sqle) {
						System.out.println("sqle in Server 765: " + sqle.getMessage());
					}
					
					dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
					date = new Date();
					System.out.println(dateFormat.format(date) + " " + gameName + " - " + "ran out of guesses and everyone loses the game");
					
					Message newMessage2 = new Message("runOutOfGuessesMultiplayer", msg1 + "," + usernameOfGuesser + "," + guessedLetter);
					this.broadcast(newMessage2, currentST);	
					return;
				}
			}
			numGuesses = Integer.toString(numGuessesInt);
			
			Message newMessage2 = new Message("printResultsOfGuessedLetter", emptyWord + "," + secretWord + "," + numGuesses + "," + indexOfGuesserUsername + "," + usernameOfGuesser + "," + gameName + "," +  guessedLetter + "," + "0");
			this.broadcast(newMessage2, currentST);	
			
		}
		
		
		
		
	} //end of checkGuessedLetter

	
	
	
	public void nextGuess(String msg, ServerThread currentST) {
		String [] msgArr = msg.split(",");
		
		String emptyWord = msgArr[0];
		String secretWord = msgArr[1];
		String numGuesses = msgArr[2];
		String indexOfGuesserUsername = msgArr[3];
		String usernameOfGuesser = msgArr[4];
		String gameName = msgArr[5];
		String guessedLetter = msgArr[6];
		
		Message newMessage = new Message("nextGuess", emptyWord + "," + secretWord + "," + numGuesses + "," + indexOfGuesserUsername + "," + usernameOfGuesser + "," + gameName + "," + guessedLetter);
		this.broadcast(newMessage, currentST);
		
	}
	
	
	
	
	
	
	
	
	
	public synchronized void checkGuessedWord(String msg, ServerThread currentST) {
		String [] msgArr = msg.split(",");
		
		String emptyWord = msgArr[0];
		String secretWord = msgArr[1];
		String numGuesses = msgArr[2];
		String indexOfGuesserUsername = msgArr[3];
		String usernameOfGuesser = msgArr[4];
		String gameName = msgArr[5];
		String guessedWord = msgArr[6];
																	
		
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
		Date date = new Date();
		System.out.println(dateFormat.format(date) + " " + gameName + " " + usernameOfGuesser + " - guessed word '" + guessedWord + "'.");
		
		if(guessedWord.toUpperCase().equals(secretWord)) {	//guessed word correctly, win, update DB, removeGame from Server
			dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
			date = new Date();
			System.out.println(dateFormat.format(date) + " " + gameName + " " + usernameOfGuesser + " - " + guessedWord + " is correct.");
			System.out.print(usernameOfGuesser + " wins game. ");
			Game gameObject = runningGames.get(gameName);
			ArrayList<String> tempNamesAL = gameObject.getListOfPlayerNames();
			
			for(int i = 0; i < masterUsernameAL.size(); i++) {	//tempNamesAL.size()
				String tempName = masterUsernameAL.get(i);		//tempNamesAL.get(i)
				if(!tempName.equals(usernameOfGuesser)) {
					System.out.print(tempName + " ");
				}
			}
		
			if(tempNamesAL.size() == 1) {
				System.out.print("no other users ");
			}
			System.out.println("have lost the game.");
			
			Game tempGameObject = runningGames.get(gameName);
			
			if(tempGameObject.getOriginalNumPlayers() == 1) {		//single player win
				Statement st = null;
				ResultSet rs = null;
				
				try {
					st = conn.createStatement();
					String sql = "UPDATE Users SET wins = wins + 1 WHERE username = '" + usernameOfGuesser + "';" ;
					st.executeUpdate(sql);
					
					sql = "SELECT * FROM Users WHERE username = '" + usernameOfGuesser + "';";	
					rs = st.executeQuery(sql);
					
					String msg1 = "";
					String username = "";
					String numWins = "";
					String numLosses = "";
					
					while(rs.next()) {					
						username = rs.getString("username");
						numWins = rs.getString("wins");
						numLosses = rs.getString("losses");
						msg1 += username + "," + numWins + "," + numLosses + ",";
					}
					msg1 = msg1.substring(0, msg1.length() - 1); //removes last comma at the end
					
					Message newMessage = new Message("printWinner", msg1);	
					this.broadcast(newMessage, currentST);
					
					runningGames.remove(gameName);
					runningGamesNames.remove(gameName);
					
				} catch(SQLException sqle) {
					System.out.println("sql in Server 825: " + sqle);
				}
			}
			
			else {	//multiplayer, one user wins, all other players lose.	///////////////////////
				Statement st = null;
				ResultSet rs = null;
				ResultSet rs2 = null;
				ResultSet rs3 = null;
				
				try {
					st = conn.createStatement();
					String sql = "UPDATE Users SET wins = wins + 1 WHERE username = '" + usernameOfGuesser + "';" ;
					st.executeUpdate(sql);
					
					ArrayList<String> tempAL = gameObject.getListOfPlayerNames();
					
					int indexOfCurrentST = 0;
					int z = 0;
					for(ServerThread thread : serverThreads) {
						if(thread == currentST) {
							indexOfCurrentST = z;
						}
						z++;
					}
					
					String nameOfCurrentST = masterUsernameAL.get(indexOfCurrentST);		//tempAL.get(indexOfCurrentST)
					
					int indexOfGuesser = 0;	//the guesser is the user who won
					for(int i = 0; i < masterUsernameAL.size(); i++) {		//tempAL.size()
						if(masterUsernameAL.get(i) == usernameOfGuesser) {			//tempAL.get(i)
							indexOfGuesser = i;
						}
					}
					
					
					for(int i = 0; i < masterUsernameAL.size(); i++) {		//tempAL.size()
						if(i != indexOfGuesser) {
							sql = "UPDATE Users SET losses = losses + 1 WHERE username = '" + masterUsernameAL.get(i) + "';" ;		//tempAL.get(i)
							st.executeUpdate(sql);
						}
						
					}
					


					String msg1 = "";
					String username = "";
					String numWins = "";
					String numLosses = "";
					
//					sql = "SELECT * FROM Users WHERE username = '" + nameOfCurrentST + "';";		//used this to try to get records to print with name of client first
//					rs2 = st.executeQuery(sql);
//					
//					while(rs2.next()) {					
//						username = rs2.getString("username");
//						numWins = rs2.getString("wins");
//						numLosses = rs2.getString("losses");
//						msg1 += username + "," + numWins + "," + numLosses + ",";
//					}
					
//					sql = "SELECT * FROM Users WHERE username = '" + usernameOfGuesser + "';";		//(un)comment this portion and the if statement right below on line 985 for record print order
//					rs = st.executeQuery(sql);														//run it where first client wins multiplayer mode
//																									//multiplayer printing wrong winner of game
//					while(rs.next()) {					
//						username = rs.getString("username");
//						numWins = rs.getString("wins");
//						numLosses = rs.getString("losses");
//						msg1 += username + "," + numWins + "," + numLosses + ",";
//					}
					
					//tempAL.remove(indexOfGuesser);
					
					for(int i = 0; i < masterUsernameAL.size(); i++) {		//tempAL.size()
						//if(i != indexOfGuesser) {
							sql = "SELECT * FROM Users WHERE username = '" + masterUsernameAL.get(i) + "';";	//tempAL.get(i)
							rs3 = st.executeQuery(sql);
							
							while(rs3.next()) {					
								username = rs3.getString("username");
								numWins = rs3.getString("wins");
								numLosses = rs3.getString("losses");
								msg1 += username + "," + numWins + "," + numLosses + ",";
							}
						//}
						
					}

					
					
					
					msg1 = msg1.substring(0, msg1.length() - 1); //removes last comma at the end
					msg1 += "," + guessedWord;
					
					Message newMessage = new Message("printWinnerMultiplayer", msg1);	
					this.broadcast(newMessage, currentST);
					
					runningGames.remove(gameName);
					runningGamesNames.remove(gameName);
					
				} catch(SQLException sqle) {
					System.out.println("sql in Server 984: " + sqle);
				}
			}
				
		}	//end of if(guessed word correctly)
		

		else {	//guessed word incorrectly, lose, update DB, remove Game from server(single player)
			dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
			date = new Date();
			System.out.println(dateFormat.format(date) + " " + gameName + " " + usernameOfGuesser + " - " + guessedWord + " is incorrect.");
			
			dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
			date = new Date();
			System.out.println(dateFormat.format(date) + " " + gameName + " " + usernameOfGuesser + " has lost and will spectate for the remainder of the game.");
			
			Game tempGameObject = runningGames.get(gameName);
			ArrayList<String> tempNamesAL = tempGameObject.getListOfPlayerNames();
			
			if(tempGameObject.getOriginalNumPlayers() == 1) {		//single player
				Statement st = null;
				ResultSet rs = null;
				try {
					st = conn.createStatement();
					String sql = "UPDATE Users SET losses = losses + 1 WHERE username = '" + usernameOfGuesser + "';" ;
					st.executeUpdate(sql);
					
					sql = "SELECT * FROM Users WHERE username = '" + usernameOfGuesser + "';";		
					rs = st.executeQuery(sql);
					
					String msg1 = "";
					String username = "";
					String numWins = "";
					String numLosses = "";
					
					while(rs.next()) {					
						username = rs.getString("username");
						numWins = rs.getString("wins");
						numLosses = rs.getString("losses");
						msg1 += username + "," + numWins + "," + numLosses + ",";
					}
					
					msg1 = msg1.substring(0, msg1.length() - 1); //removes last comma at the end
					Message newMessage = new Message("printLoser", msg1);	////////////
					this.broadcast(newMessage, currentST);
					
					runningGames.remove(gameName);
					runningGamesNames.remove(gameName);
					
				} catch(SQLException sqle) {
					System.out.println("sql in Server 1030: " + sqle);
				}
			}
			
			else if (tempGameObject.getOriginalNumPlayers() == 2) {
				Game tempGameObject2 = runningGames.get(gameName);
				ArrayList<String> modifiedTempNamesAL = tempGameObject2.getListOfPlayerNames();
				
				String usernameOfNextGuesser = "";
				String indexOfNextGuesser = "";
				
				for(int i = 0; i < modifiedTempNamesAL.size(); i++) {
					if(usernameOfGuesser != modifiedTempNamesAL.get(i)) {
						usernameOfNextGuesser = modifiedTempNamesAL.get(i);
					}
				}
				
				indexOfNextGuesser = Integer.toString(modifiedTempNamesAL.indexOf(usernameOfNextGuesser));
				
				
				currentST.printGameLogicUIToThisClient = false;
				
				Message newMessage2 = new Message("printGameLogicUIAfterGuessWrongWord2Player", emptyWord + "," + secretWord + "," + numGuesses + "," + indexOfNextGuesser + "," + usernameOfNextGuesser + "," + gameName);
				this.broadcast(newMessage2, currentST);
				
			}
			
			else {			//multi player, only the guesser loses, only spectates remove his name from player AL, since code above iterates through names AL for next user.
								//remember to change game numPlayers
								//and update the game ArrayList of names (?)
				
				Game tempGameObject2 = runningGames.get(gameName);
				ArrayList<String> modifiedTempNamesAL = tempGameObject2.getListOfPlayerNames();
				
				String usernameOfNextGuesser = "";
				String indexOfNextGuesser = "";
				
				int j = 0;
				for(int i = 0; i < modifiedTempNamesAL.size(); i++) {			//this updates the parameters to ensure the next guesser is the right client, does the same 
					if(modifiedTempNamesAL.get(i).equals(usernameOfGuesser)) {
						if(i == 0) {
							usernameOfNextGuesser = modifiedTempNamesAL.get(1);
							indexOfNextGuesser = "0";
							break;
						}
						
						else if (i == modifiedTempNamesAL.size()-1) {
							usernameOfNextGuesser = modifiedTempNamesAL.get(0);
							indexOfNextGuesser = "0";
							break;
						}
						
						else {
							usernameOfNextGuesser = modifiedTempNamesAL.get(i+1);
							indexOfNextGuesser = Integer.toString(i);
							break;
						}
					}
				}
				
				for(int i = 0; i < modifiedTempNamesAL.size(); i++) {
					if(modifiedTempNamesAL.get(i).equals(usernameOfGuesser)) {
						modifiedTempNamesAL.remove(i);
					}
				}
				
				tempGameObject2.setListOfPlayerNames(modifiedTempNamesAL);
				tempGameObject2.decrementNumPlayers();
				runningGames.replace(gameName, tempGameObject2);
				
				currentST.printGameLogicUIToThisClient = false;	//stops future printGameLogicUI to be printed to this user since the client should be in spectator mode.
				
				Message newMessage = new Message("guessedWrongWordMultiplayer", usernameOfGuesser);	////////////
				this.broadcast(newMessage, currentST);
				

				Message newMessage2 = new Message("printGameLogicUIAfterGuessWrongWord", emptyWord + "," + secretWord + "," + numGuesses + "," + indexOfNextGuesser + "," + usernameOfNextGuesser + "," + gameName);
				this.broadcast(newMessage2, currentST);
			}
			
		
		}
		
	}	//end of checkGuessedWord
	
	
	
	
	
	
	
	
	
	
	
	
	
	//going to need to change one of the broadcast else ifs to broadcast printGameLogicUI2 instead. (printGameLogicUIAfterGuessWrongWord)
	
//	public void checkGuessedLetter2(String msg, ServerThread currentST) {		/
//		String [] msgArr = msg.split(",");
//		
//		String emptyWord = msgArr[0];
//		String secretWord = msgArr[1];
//		String numGuesses = msgArr[2];
//		String indexOfGuesserUsername = msgArr[3];
//		String usernameOfGuesser = msgArr[4];
//		String gameName = msgArr[5];
//		String guessedLetter = msgArr[6];
//					
//		
//		char[] emptyWordArr = emptyWord.toCharArray();
//		
//		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
//		Date date = new Date();
//		System.out.println(dateFormat.format(date) + " " + gameName + " " + usernameOfGuesser + " - guessed letter '" + guessedLetter + "'.");
//		
//		if(secretWord.toUpperCase().contains(guessedLetter.toUpperCase()) && emptyWord.contains(guessedLetter.toUpperCase()) == false) {	//user guessed letter correctly, and letter has not been guessed before
//			
//			for(int i = 0; i < secretWord.length(); i++) {		//replacing emptyWord __ with actual letter
//				if(Character.toString(secretWord.charAt(i)).toUpperCase().equals(guessedLetter.toUpperCase()) == true) {	//userInput is a letter in the randomWord
//					emptyWordArr[2*i] = Character.toUpperCase(guessedLetter.charAt(0));
//					emptyWord = String.valueOf(emptyWordArr);
//				}
//			}
//			
//			ArrayList<Integer> charPositionsAL = new ArrayList<Integer>();	//finding positions of letter (positions only used for printing out to console)
//			for(int i = 0; i < secretWord.length(); i++){
//			    if(Character.toString(secretWord.charAt(i)).toUpperCase().equals(guessedLetter.toUpperCase())){
//			       charPositionsAL.add(i);
//			    }
//			}
//			
//			
//			dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
//			date = new Date();
//			System.out.print(dateFormat.format(date) + " " + gameName + " " + usernameOfGuesser + " - '" + guessedLetter + "' is in " + secretWord + " in position(s) ");
//			for(int i = 0; i < charPositionsAL.size(); i++) {
//				System.out.print(charPositionsAL.get(i));
//				System.out.print(" ");
//			}
//			System.out.println(". Secret word now shows " + emptyWord);	
//			
//			
//			Message newMessage = new Message("printGuessedLetterToOtherClients", emptyWord + "," + secretWord + "," + numGuesses + "," + indexOfGuesserUsername + "," + usernameOfGuesser + "," + gameName + "," + guessedLetter);
//			this.broadcast(newMessage, currentST);
//			
//			
//			int indexOfGuesserUsernameInt = Integer.parseInt(indexOfGuesserUsername);
//
//			Game gameObject = runningGames.get(gameName);
//			ArrayList<String> namesAL = gameObject.getListOfPlayerNames();
//			if(gameObject.getOriginalNumPlayers() != 1) {	//multiplayer version changes indexOfGuesser
//				indexOfGuesserUsernameInt++;
//				if(indexOfGuesserUsernameInt >= gameObject.getNumPlayers()) {	//loops back to first guesser after everyone already made a guess
//					indexOfGuesserUsernameInt = 0;
//				}
//				indexOfGuesserUsername = Integer.toString(indexOfGuesserUsernameInt);
//				usernameOfGuesser = namesAL.get(indexOfGuesserUsernameInt);
//			}
//			
//			
//			Message newMessage2 = new Message("printResultsOfGuessedLetter2", emptyWord + "," + secretWord + "," + numGuesses + "," + indexOfGuesserUsername + "," + usernameOfGuesser + "," + gameName + "," + guessedLetter + "," + "1");
//			this.broadcast(newMessage2, currentST);	
//			
//			
//		}
//		
//		
//		else if (emptyWord.contains(guessedLetter.toUpperCase())) {			//guessedLetter has already been guessed before
//			Message newMessage = new Message("letterAlreadyGuessedBefore", emptyWord + "," + secretWord + "," + numGuesses + "," + indexOfGuesserUsername + "," + usernameOfGuesser + "," + gameName + "," + guessedLetter);
//			this.broadcast(newMessage, currentST);
//		}
//		
//		
//		else {		//guessedLetter is incorrect			
//			dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
//			date = new Date();
//			System.out.println(dateFormat.format(date) + " " + gameName + " " + usernameOfGuesser + " - '" + guessedLetter + "' is not in " + secretWord);
//			
//			Message newMessage = new Message("printGuessedLetterToOtherClients", emptyWord + "," + secretWord + "," + numGuesses + "," + indexOfGuesserUsername + "," + usernameOfGuesser + "," + gameName + "," + guessedLetter);
//			this.broadcast(newMessage, currentST);
//			
//			
//			int indexOfGuesserUsernameInt = Integer.parseInt(indexOfGuesserUsername);
//			Game gameObject = runningGames.get(gameName);
//			ArrayList<String> namesAL = gameObject.getListOfPlayerNames();
//			
//			
//			if(gameObject.getOriginalNumPlayers() != 1) {	//multiplayer version changes indexOfGuesser
//				indexOfGuesserUsernameInt++;
//				if(indexOfGuesserUsernameInt >= gameObject.getNumPlayers()) {	//loops back to first guesser after everyone already made a guess
//					indexOfGuesserUsernameInt = 0;
//				}
//				indexOfGuesserUsername = Integer.toString(indexOfGuesserUsernameInt);
//				usernameOfGuesser = namesAL.get(indexOfGuesserUsernameInt);
//			}
//			
//			int numGuessesInt = Integer.parseInt(numGuesses);	//decrement # of remaining guesses after guessing incorrectly
//			numGuessesInt--;
//			if(numGuessesInt == 0) {  //all players lose after running out of guesses
//				Game checkIfSingleOrMultiPlayer = runningGames.get(gameName);
//				if(checkIfSingleOrMultiPlayer.getOriginalNumPlayers() == 1) {	//single player mode run out of guesses
//					Statement st = null;
//					ResultSet rs = null;
//					
//					String msg1 = "";
//					String numWins = "";
//					String numLosses = "";
//					
//					try {
//						st = conn.createStatement();
//						String sql = "UPDATE Users SET losses = losses + 1 WHERE username = '" + usernameOfGuesser + "';" ;
//						st.executeUpdate(sql);
//						
//						sql = "SELECT * FROM Users WHERE username = '" + usernameOfGuesser + "';" ;	
//						rs = st.executeQuery(sql);
//						
//						while(rs.next()) {					
//							numWins = rs.getString("wins");
//							numLosses = rs.getString("losses");
//							msg1 += usernameOfGuesser + "," + numWins + "," + numLosses;
//						}
//					} catch(SQLException sqle) {
//						System.out.println("sqle in Server 748: " + sqle.getMessage());
//					}
//					
//					dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
//					date = new Date();
//					System.out.println(dateFormat.format(date) + " " + gameName + " " + usernameOfGuesser + " - " + "ran out of guesses and has lost the game");
//					
//					Message newMessage2 = new Message("1playerNoGuessesLeft", usernameOfGuesser + "," + numWins + "," + numLosses);
//					this.broadcast(newMessage2, currentST);	
//					return;
//				}
//				
//				else {	//multi player mode, run out of guesses 		
//					Statement st = null;
//					ResultSet rs = null;
//					
//					String msg1 = "";
//					String numWins = "";
//					String numLosses = "";
//					
//					try {
//						st = conn.createStatement();
//						String sql = "";
//						for(int i = 0; i < namesAL.size(); i ++) {
//							sql = "UPDATE Users SET losses = losses + 1 WHERE username = '" + namesAL.get(i) + "';" ;
//							st.executeUpdate(sql);
//						}
//						
//						for(int i = 0; i < masterUsernameAL.size(); i++) {	//namesAL.size()
//							sql = "SELECT * FROM Users WHERE username = '" + masterUsernameAL.get(i) + "';" ;	//namesAL.get(i)
//							rs = st.executeQuery(sql);
//							
//							while(rs.next()) {					
//								numWins = rs.getString("wins");
//								numLosses = rs.getString("losses");
//								msg1 += namesAL.get(i) + "," + numWins + "," + numLosses + ",";
//							}
//						}
//						
//						msg1 = msg1.substring(0, msg1.length()-1);
//						
//					} catch(SQLException sqle) {
//						System.out.println("sqle in Server 765: " + sqle.getMessage());
//					}
//					
//					dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
//					date = new Date();
//					System.out.println(dateFormat.format(date) + " " + gameName + " - " + "ran out of guesses and everyone loses the game");
//					
//					Message newMessage2 = new Message("runOutOfGuessesMultiplayer", msg1 + "," + usernameOfGuesser + "," + guessedLetter);
//					this.broadcast(newMessage2, currentST);	
//					return;
//				}
//			}
//			numGuesses = Integer.toString(numGuessesInt);
//			
//			Message newMessage2 = new Message("printResultsOfGuessedLetter", emptyWord + "," + secretWord + "," + numGuesses + "," + indexOfGuesserUsername + "," + usernameOfGuesser + "," + gameName + "," +  guessedLetter + "," + "0");
//			this.broadcast(newMessage2, currentST);	
//			
//		}
//		
//		
//		
//		
//	} //end of checkGuessedLetter
//	
//	
//	
//	public void nextGuess2(String msg, ServerThread currentST) {
//		String [] msgArr = msg.split(",");
//		
//		String emptyWord = msgArr[0];
//		String secretWord = msgArr[1];
//		String numGuesses = msgArr[2];
//		String indexOfGuesserUsername = msgArr[3];
//		String usernameOfGuesser = msgArr[4];
//		String gameName = msgArr[5];
//		String guessedLetter = msgArr[6];
//		
//		Message newMessage = new Message("nextGuess2", emptyWord + "," + secretWord + "," + numGuesses + "," + indexOfGuesserUsername + "," + usernameOfGuesser + "," + gameName + "," + guessedLetter);
//		this.broadcast(newMessage, currentST);
//		
//	}
	
	
	
	
	public synchronized void broadcast(Message message, ServerThread currentST) {		//for multiplayer
		
		if(message.getTag().equals("announceNewlyJoinedUser")) {
			for(ServerThread threads : serverThreads) {
				if(threads != currentST) {			//prints to every OTHER client that a new user has joined
					message.setTag("announceNewlyJoinedUser");
					message.setMessage(message.getMessage());
					threads.sendMessage(message);
				}
			}
		}
		
		
		
		
		
		else if (message.getTag().equals("printRecordForNewlyJoinedUser")) {	////////////
			for(ServerThread threads : serverThreads) {
				if(threads != currentST) {
					threads.sendMessage(message);
				}
			}
		}
		
		
		
		
		
		else if (message.getTag().equals("startGame1")) {	//start a single player game, only broadcast to that specific client's serverthread
			for(ServerThread threads : serverThreads) {
				if(threads == currentST) {
					message.setTag("startGame");
					threads.sendMessage(message);
				}
			}
		}
			
		else if (message.getTag().equals("startGame2")) {		//start a multiplayer game, broadcast to ALL client's serverthreads
			for(ServerThread threads : serverThreads) {
				message.setTag("startGame");
				threads.sendMessage(message);
			}
		}
		
		else if (message.getTag().equals("printGameLogicUI")) {
			if(numMessages != 0) {
				numMessages++;
			} 
			else {
				numMessages++;				
				for(ServerThread threads : serverThreads) {
					if(threads == currentST) {
						message.setTag("printGameLogicUI");/////////////////
						threads.sendMessage(message);
					}
					else if (threads != currentST) {	//users who are not guessing are prompted to wait for their turn.
						message.setTag("waitingForOtherUserToGuess");
						threads.sendMessage(message);
					}
				}
			}
			if(numMessages == serverThreads.size()) {
				numMessages = 0;
			}
			
		}
		

		
		
			//multiple clients printing stuff out multiple times problem starts with printGameLogicUI. Comment out everything below and try to fix before moving on
		
		
		
		else if (message.getTag().equals("printGuessedLetterToOtherClients")) {
			for(ServerThread threads : serverThreads) {
				if(threads != currentST) {
					threads.sendMessage(message);
				}
			}
		}
		
		
		else if (message.getTag().equals("printResultsOfGuessedLetter")) {
			String msg = message.getMessage();
			String [] msgArr = msg.split(",");
			
			String indexOfGuesserUsername = msgArr[3];
			int indexOfGuesserUsernameInt = Integer.parseInt(indexOfGuesserUsername);	//the index has already been incremented above

										//System.out.println("server broadcast indexOfGuesserUsernameInt: " + indexOfGuesserUsernameInt);
			
			for(ServerThread threads : serverThreads) {
				threads.sendMessage(message);		
			}
			
			
		}
		
		
		
		
		
		else if (message.getTag().equals("nextGuess")) {
			String msg = message.getMessage();
			
			String [] msgArr = msg.split(",");
			
			String indexOfGuesserUsername = msgArr[3];
			int indexOfGuesserUsernameInt = Integer.parseInt(indexOfGuesserUsername);	//the index has already been incremented above
						
			if(numMessages2 != 0) {
				numMessages2++;
			}
			else {
				numMessages2++;		
				
				int i = 0;
				for(ServerThread thread : serverThreads) {	//gives guessing component to next user
					if(i == indexOfGuesserUsernameInt) {	//next guesser is prompted to guess
						message.setTag("printGameLogicUI");
						thread.sendMessage(message);
					}
					else if (i != indexOfGuesserUsernameInt) {	// all other clients are told to wait
						message.setTag("waitingForOtherUserToGuess");
						thread.sendMessage(message);
					}
					
					i++;
					
				}
								
			}
			
			if(numMessages2 == serverThreads.size()) {
				numMessages2 = 0;
			}
			
			
			
		}
		
		
		
		else if (message.getTag().equals("letterAlreadyGuessedBefore")) {
			for(ServerThread threads : serverThreads) {
				if(threads == currentST) {
					threads.sendMessage(message);
				}

			}
		}
		
		
		
		
		else if (message.getTag().equals("printWinner")) {
			for(ServerThread threads : serverThreads) {
				threads.sendMessage(message);
			}
		}
		
		else if (message.getTag().equals("printWinnerMultiplayer")) {
			for(ServerThread threads : serverThreads) {
				if(threads == currentST) {
					message.setTag("printWinnerMultiplayer");/////////////
					threads.sendMessage(message);
				}
				else if (threads != currentST) {
					message.setTag("printLoserMultiplayer");
					threads.sendMessage(message);
				}
			}
		}
		
		
		else if (message.getTag().equals("printLoser")) {
			for(ServerThread threads : serverThreads) {
				threads.sendMessage(message);
			}
		}
		
		else if (message.getTag().equals("1playerNoGuessesLeft")) {
			for(ServerThread threads : serverThreads) {
				if(threads == currentST) {
					threads.sendMessage(message);
				}
			}
		}
		
		else if (message.getTag().equals("runOutOfGuessesMultiplayer")) {
			for(ServerThread threads : serverThreads) {
				threads.sendMessage(message);
			}
		}

		
		
		
		else if (message.getTag().equals("guessedWrongWordMultiplayer")) {
			for(ServerThread threads : serverThreads) {
				if(threads == currentST) {
					message.setTag("guessedWrongWordMultiplayer");
					threads.sendMessage(message);
				}
				else if (threads != currentST) {
					message.setTag("otherUserGuessedWrongWordMultiplayer");
					threads.sendMessage(message);
				}
			}
		}
		
		
		else if (message.getTag().equals("printGameLogicUIAfterGuessWrongWord")) {
			int i = 0;
			for(ServerThread threads : serverThreads) {
				if(threads == currentST) {
					ServerThread printGameLogicUIToThisThread = serverThreads.get(i+1);
					message.setTag("printGameLogicUI");
					printGameLogicUIToThisThread.sendMessage(message);
																System.out.println(" broadcasting pGameLogicUIAfterGuessWrongWord to NEXT client: " + i+1);
					for(ServerThread threads2 : serverThreads) {
						if(threads2 != printGameLogicUIToThisThread) {
							message.setTag("waitingForOtherUserToGuess");
							threads2.sendMessage(message);
																System.out.println("broadcasting waitingForOtherUserToGuess to every client ~NEXT client");
						}
					}
				}
				i++;
			}
		}

		
		else if (message.getTag().equals("printGameLogicUIAfterGuessWrongWord2Player")) {
			if(numMessages3 != 0) {
				numMessages3++;
			}
			else {
				numMessages3++;		

				for(ServerThread threads : serverThreads) {
					if(threads == currentST) {
						message.setTag("guessedWrongWordMultiplayer");
						threads.sendMessage(message);
						message.setTag("waitingForOtherUserToGuess");
						threads.sendMessage(message);
					}
					else if (threads != currentST) {
						message.setTag("printGameLogicUI2Player");
						threads.sendMessage(message);
					}
				}
								
			}
			
			if(numMessages3 == serverThreads.size()) {
				numMessages3 = 0;
			}
		}
		

		

		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
//		else if (message.getTag().equals("nextGuess2")) {
//			String msg = message.getMessage();
//			
//			String [] msgArr = msg.split(",");
//			
//			String emptyWord = msgArr[0];
//			String secretWord = msgArr[1];
//			String numGuesses = msgArr[2];
//			//String indexOfGuesserUsername = msgArr[3];
//			String usernameOfGuesser = msgArr[4];
//			String gameName = msgArr[5];
//			String guessedLetter = msgArr[6];
//			
//			String indexOfGuesserUsername = msgArr[3];
//			int indexOfGuesserUsernameInt = Integer.parseInt(indexOfGuesserUsername);	//the index has already been incremented above
//			
//			//indexOfGuesserUsernameInt--;
//			
////			indexOfGuesserUsernameInt++;
////			Game tempGameObject = runningGames.get(gameName);
////			int gameObjectNumPlayers = tempGameObject.getNumPlayers();
////			
////																System.out.println("gameObjectNumPlayers: " + gameObjectNumPlayers);
////			if(indexOfGuesserUsernameInt == gameObjectNumPlayers) {
////				indexOfGuesserUsernameInt = 0;
////			}
//															System.out.println("indexOfGuesserUsernameInt: " + indexOfGuesserUsernameInt);
//						
//			if(numMessages2 != 0) {
//				numMessages2++;
//			}
//			else {
//				numMessages2++;		
//				
//				Boolean foundNextThread = false;
//				int i = 0;
//				for(ServerThread thread : serverThreads) {	//gives guessing component to next user
//					if(i == indexOfGuesserUsernameInt) {	//next guesser is prompted to guess
//																System.out.println("nextGuess in Server broadcast printing game logic for client: " + i);
//						if(thread.printGameLogicUIToThisClient == false) {
//							while(foundNextThread == false) {
//								ServerThread nextThread = serverThreads.get(indexOfGuesserUsernameInt++);
//								if(indexOfGuesserUsernameInt == serverThreads.size()) {
//                                                                            System.out.println("resetting indexOfCurrentST to 0, looping back to beginning");
//                                    indexOfGuesserUsernameInt = 0;
//								}
//								if(nextThread.printGameLogicUIToThisClient == false) {
//                                                                            System.out.println("hit a serverThread with false at index: " + indexOfGuesserUsername);
//									continue;
//								}
//								foundNextThread = true;
//                                message.setTag("printGameLogicUI2");
//                                                                            System.out.println("should send printGameLogicUI2 to nextThread: " + message.getTag());
//								nextThread.sendMessage(message);
//								for(ServerThread threads2 : serverThreads) {
//									if(threads2 != nextThread) {
//                                        message.setTag("waitingForOtherUserToGuess");
//                                                                            System.out.println("should send waitingForOtherUserToGuess to ~nextThread: " + message.getTag());
//										threads2.sendMessage(message);
//									}
//								}
//							}
//						}
//						message.setTag("printGameLogicUI2");
//						thread.sendMessage(message);
//					}
//					else if (i != indexOfGuesserUsernameInt) {	// all other clients are told to wait
//						message.setTag("waitingForOtherUserToGuess");
//						thread.sendMessage(message);
//					}
//					
//					i++;
//					
//				}
//								
//			}
//			
//			if(numMessages2 == serverThreads.size()) {
//				numMessages2 = 0;
//			}
//			
//			
//			
//		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		else if (!message.getTag().equals(null)) {	//this else if block handles the initial stuff before the hangman game starts
			//iterate through serverThreads in vector (aka send message object to one particular serverthread)
			for(ServerThread threads : serverThreads) {
				if(threads == currentST) {
					threads.sendMessage(message);
				}
			}
		}
		
		

	}	//end of broadcast
	
	

}
