import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;


public class Client extends Thread {
	
	static String ServerHostname = "";
	static String ServerPort = "";
	static String DBConnection = "";
	static String DBUsername = "";
	static String DBPassword = "";
	static String SecretWordFile = "";
	
	private int numTurns = 7;
	
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	
	
	public static void main(String [] args) {
		Scanner scan = new Scanner(System.in);
		String inputFilename = new String();
		BufferedReader br = null;
		Boolean inputErrorExists = false;
				
		while(inputErrorExists == false) {
			try {
				System.out.print("What is the name of the input file? (Client-side) ");
				inputFilename = scan.nextLine();
				br = new BufferedReader(new FileReader(inputFilename));				
				inputErrorExists = true;
			} catch(FileNotFoundException fnfe) {
				System.out.println("Configuration file " + inputFilename + " could not be found.");
				System.out.println("");
			}

		}

		
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
			System.out.println("ioe: " + ioe.getMessage());
		}
		
		
		System.out.println("");
		System.out.println("Server Hostname - " + ServerHostname);
		System.out.println("ServerPort - " + ServerPort);
		System.out.println("Database Connection String - " + DBConnection);
		System.out.println("Database Username - " + DBUsername);
		System.out.println("Database Password - " + DBPassword);
		System.out.println("Secret Word File - " + SecretWordFile);

		
		
		new Client(ServerHostname, Integer.parseInt(ServerPort));
		
		
	} //end of main
	
	
	
	
	
	
	public Client(String hostname, int port) {

		//Establishing connection to server
		Socket s = null;
		
		try {
			s = new Socket(hostname, port);
			System.out.println("");
			System.out.println("Trying to connect to Server...Connected!");
			System.out.println("");
			
			oos = new ObjectOutputStream(s.getOutputStream());
			ois = new ObjectInputStream(s.getInputStream());
								
		} catch(IOException ioe) {
			System.out.println("Trying to connect to server...Unable to connect to server " + hostname + " on port " + port + ".");	//////////////////////////////////////////
			System.exit(0);
		} finally {

			
		}
		
		
		
		
		this.start();		//triggers run() which is a while(true) loop waiting to read from OIS
		
		this.askForLoginInfo();
		
		
		
		
		
	}		//end of Client() constructor

	
	
	
	
	private void askForLoginInfo() {
		Scanner scan = new Scanner(System.in);
		String username;
		String password;
		
		System.out.print("Username: ");
		username = scan.nextLine();
		System.out.print("Password: ");
		password = scan.nextLine();
		System.out.println("");
		
		
		String msg0 = username + "," + password;
		Message message0 = new Message("login", msg0);
		
		try {
			oos.writeObject(message0);
			oos.flush();
		} catch(IOException ioe) {
			System.out.println("ioe near Client 191: " + ioe.getMessage());
			System.out.println(ioe.getCause());
			ioe.printStackTrace();
		}
	}
	







	public void run() {				//used to read in the stuff that other clients send over to you. more of the multilplayer portion
		
		Scanner scan = new Scanner(System.in);
			
		try {
			while(true) {
				Message message = (Message)ois.readObject(); 
												//System.out.println("                       Client run() ois read:      tag: " + message.getTag() + "    msg: " + message.getMessage());
												
				if(message.getTag().equals("login")) {
					System.out.println("No acount exists with those credentials.");
					System.out.print("Would you like to create a new account? ");
					String userInput = scan.nextLine();
					if(userInput.toUpperCase().equals("YES")) {
						System.out.print("Would you like to use the username and password above? ");
						userInput = scan.nextLine();
						if(userInput.toUpperCase().equals("YES")) {
							//create new account in server
							String msg0 = message.getMessage();
							String username = msg0.substring(0, msg0.indexOf(','));
							String password = msg0.substring(msg0.indexOf(',')+1);
							Message message0 = new Message("createNewAccount", msg0);
							
							try {
								oos.writeObject(message0);
								oos.flush();
							} catch(IOException ioe) {
								System.out.println("ioe near Client 232: " + ioe.getMessage());
								System.out.println(ioe.getCause());
								ioe.printStackTrace();
							}

						}
						else if (userInput.toUpperCase().equals("NO")) {
							//loop back ask for login details again.
							this.askForLoginInfo();
						}
						else {	//loop back ask for login details again.
							System.out.println("That is not a valid input.");
							System.out.println("");
						}
					}
					else {	//loop back ask for login details again.
						System.out.println("Please enter a valid username and password.");
						System.out.println("");
						this.askForLoginInfo();
					}
				}
				
				else if (message.getTag().equals("loginAgain,WrongPassword")) {	//user typed in incorrect password for existing account
					System.out.println("Please enter a valid username and password.");
					System.out.println("");
					this.askForLoginInfo();
				}
				
				else if (message.getTag().equals("loginSuccessfully")) {
					String msg = message.getMessage();
					String [] msgArr = msg.split(",");
					String username = msg.substring(0, msg.indexOf(','));
					String password = msg.substring(msg.indexOf(',')+1);
					System.out.println("");
					System.out.println("Great! You are now logged in as " + username + ".");
					System.out.println("");
					
					Message message0 = new Message("getRecord1", username + "," + password);
					
					try {
						oos.writeObject(message0);
						oos.flush();
					} catch(IOException ioe) {
						System.out.println("ioe near Client 271: " + ioe.getMessage());
						System.out.println(ioe.getCause());
						ioe.printStackTrace();
					}
				}
				
				
				
				else if (message.getTag().equals("printRecord1")) {
					String msg = message.getMessage();
					String [] msgArr = msg.split(",");
					
					String username = msgArr[0];
					String numWins = msgArr[1];
					String numLosses = msgArr[2];
					
					String printRecord = username + "'s Record";
					String dashedLines = "";
					for(int i = 0; i < printRecord.length(); i++) {
						dashedLines += "-";
					}
					System.out.println(printRecord);
					System.out.println(dashedLines);
					System.out.println("Wins - " + numWins);
					System.out.println("Losses - " + numLosses);
					
					
					Message newMessage = new Message("doNothing1", username);
					
					try {
						oos.writeObject(newMessage);
						oos.flush();
					} catch(IOException ioe) {
						System.out.println("ioe near Client 303: " + ioe.getMessage());
						System.out.println(ioe.getCause());
						ioe.printStackTrace();
					}
					
				}
				
				
				
				else if (message.getTag().equals("startOrJoinGame") || message.getTag().equals("startOrJoinGame2") || message.getTag().equals("startOrJoinGame3") || message.getTag().equals("startOrJoinGame4")) {
					String msg = message.getMessage();
					String [] msgArr = msg.split(",");
					String username = msgArr[0];
						
					if(message.getTag().equals("startOrJoinGame2")) {
						String gameName = msgArr[1];
						System.out.println(gameName + " already exists.");
						System.out.println("");
					}
					
					if(message.getTag().equals("startOrJoinGame3")) {	//when client tries to join a game thats already full
						System.out.println("Cannot join that game since the game already reached the max number of players.");
						System.out.println("");
					}
					
					if(message.getTag().equals("startOrJoinGame4")) {	//client tries to join a game that doesn't exist
						System.out.println("Cannot join that game since that game does not exist.");
						System.out.println("");
					}
					
					System.out.println("");
					System.out.println("     1) Start a Game");
					System.out.println("     2) Join a Game");
					System.out.println("");
					System.out.print("Would you like to start a game or join a game (1 or 2)? ");
					String OptionOneTwo = scan.nextLine();
					String gameName = "";
					String gameName2 = "";
					if(OptionOneTwo.equals("1")) { //start a game
						System.out.println("");
						System.out.print("What is the name of the game? ");
						gameName = scan.nextLine();
						
						Message newMessage = new Message("checkIfGameExists", username + "," + gameName);
						try {		
							oos.writeObject(newMessage);
							oos.flush();
						} catch(IOException ioe) {
							System.out.println("ioe near Client 340: " + ioe.getMessage());
							System.out.println(ioe.getCause());
							ioe.printStackTrace();
						}
					}
					else if (OptionOneTwo.equals("2")) { //join a game	////////////////////////						
						System.out.print("What is the name of the game? ");
						gameName2 = scan.nextLine();
						System.out.println("");
						
						Message newMessage = new Message("joinGameGetRecords", gameName2 + "," + username);
						try {		
							oos.writeObject(newMessage);
							oos.flush();
						} catch(IOException ioe) {
							System.out.println("ioe near Client 365: " + ioe.getMessage());
							System.out.println(ioe.getCause());
							ioe.printStackTrace();
						}
						
					}
					
					
					else {	//User typed wrong input
						System.out.println("");
						System.out.println("That is not a valid input. Please enter 1 to start a game or 2 to join a game");
						System.out.println("");
						Message newMessage = new Message("doNothing1", username);
						try {		
							oos.writeObject(newMessage);
							oos.flush();
						} catch(IOException ioe) {
							System.out.println("ioe near Client 387: " + ioe.getMessage());
							System.out.println(ioe.getCause());
							ioe.printStackTrace();
						}
					}

				}
				
				
				
				
				else if (message.getTag().equals("askForNumPlayers")) {
					String msg = message.getMessage();
					String [] msgArr = msg.split(",");
					
					String username = msgArr[0];
					String gameName = msgArr[1];
					
					System.out.println("");
					System.out.print("How many users will be playing (1-4)? ");
					String numPlayers = scan.nextLine();
					Message newMessage = new Message("createGame", username + "," + gameName + "," + numPlayers);
					try {	
						oos.writeObject(newMessage);
						oos.flush();
					} catch(IOException ioe) {
						System.out.println("ioe near Client 407: " + ioe.getMessage());
						System.out.println(ioe.getCause());
						ioe.printStackTrace();
					}
				}
				
	
				
				else if (message.getTag().equals("waitForAllPlayers")) {	
					String msg = message.getMessage();
					String [] msgArr = msg.split(",");
					String numPlayerString = msgArr[2];
					String gameName = msgArr[1];
					String userName = msgArr[0];
					int numPlayersInt = Integer.parseInt(numPlayerString);
					
					
					if(message.getTag().equals("waitForAllPlayers")) {
						System.out.println("");
						System.out.println("Waiting for " + --numPlayersInt + " other users to join...");
					}
					
				}
				
				
				
				else if (message.getTag().equals("printCurrPlayersRecords")) {
					
					String msg = message.getMessage();
					String [] msgArr = msg.split(",");		// [username, #wins, #loss, username, #wins, #loss...]
					
					for(int i = 0; i < msgArr.length/3; i++) {
						System.out.println("User " + msgArr[i*3] + " is in the game.");
						System.out.println("");
						String printRecord = msgArr[i*3] + "'s Record";
						String dashedLines = "";
						for(int j = 0; j < printRecord.length(); j++) {
							dashedLines += "-";
						}
						System.out.println(printRecord);
						System.out.println(dashedLines);
						System.out.println("Wins - " + msgArr[(i*3) + 1]);
						System.out.println("Losses - " + msgArr[(i*3)+2]);
						System.out.println("");
					}
					
				}
				
				
				else if (message.getTag().equals("announceNewlyJoinedUser")) {		////////////////multiple clients run this because server broadcasts this to multiple clients			
					String msg = message.getMessage();								//then get record for newly joined user will run multiple times, and chain reaction
					String [] msgArr = msg.split(",");
					
					String newlyJoinedUsername = msgArr[0];
					String gameName = msgArr[1];
					
					System.out.println("User " + newlyJoinedUsername + " is in the game.");
					
				}	
				
				
				else if (message.getTag().equals("printRecordForNewlyJoinedUser")) {
					String msg = message.getMessage();
					String [] msgArr = msg.split(",");
					
					String username = msgArr[0];
					String numWins = msgArr[1];
					String numLosses = msgArr[2];
					String gameName = msgArr[3];
												String remainingNumPlayers = msgArr[4];
					
					System.out.println("");
					String printRecord = username + "'s Record";
					String dashedLines = "";
					for(int i = 0; i < printRecord.length(); i++) {
						dashedLines += "-";
					}
					System.out.println(printRecord);
					System.out.println(dashedLines);
					System.out.println("Wins - " + numWins);
					System.out.println("Losses - " + numLosses);
					System.out.println("");
					
					if(!remainingNumPlayers.equals("0")) {
						System.out.println("Waiting for " + remainingNumPlayers + " other users to join.");
						System.out.println("");
					}
					
				}
				
				
				
				else if(message.getTag().equals("startGame")) {
					String msg = message.getMessage();
					String [] msgArr = msg.split(",");
					
					String emptyWord = msgArr[0];
					String username = msgArr[1];
					String secretWord = msgArr[2];
					String usernameOfGuesser = msgArr[3];
					String gameName = msgArr[4];
					
					System.out.println("");
					System.out.println("All users have joined.");
					System.out.println("");
					System.out.println("Determining secret word...");
					System.out.println("");
					System.out.println("Secret Word: " + emptyWord);
					
					String indexOfGuesserUsername = "0";
					
					Message newMessage = new Message("askForGuessInput", username + "," + emptyWord + "," + secretWord + "," + "7" + "," + indexOfGuesserUsername + "," + gameName);
					try {
						oos.writeObject(newMessage);
						oos.flush();
					} catch(IOException ioe) {
						System.out.println("ioe near Client 541: " + ioe.getMessage());
						System.out.println(ioe.getCause());
						ioe.printStackTrace();
					}
				}
				
				
				
				else if (message.getTag().equals("printGameLogicUI") || message.getTag().equals("letterAlreadyGuessedBefore")) {
					String msg = message.getMessage();
					String [] msgArr = msg.split(",");
					
					String emptyWord = msgArr[0];
					String secretWord = msgArr[1];
					String numGuesses = msgArr[2];
					String indexOfGuesserUsername = msgArr[3];
					String usernameOfGuesser = msgArr[4];
					String gameName = msgArr[5];
					
					
					
//															System.out.println("emptyWord: " + emptyWord);
//															System.out.println("secretWord: " + secretWord);
//															System.out.println("numGuesses: " + numGuesses);
//															System.out.println("indexOfGuesserUsername: " + indexOfGuesserUsername);
//															System.out.println("usernameOfGuesser: " + usernameOfGuesser);
//															System.out.println("gameName: " + gameName);
															
					
					if(message.getTag().equals("letterAlreadyGuessedBefore")) {
						System.out.println("");
						System.out.println("This letter has already been guessed before. Try guessing a different letter.");
					}
					
					Boolean properUserInput = false;
					
					while(properUserInput == false) {
						System.out.println("");
						System.out.println("You have " + numGuesses + " incorrect guesses remaining.");
						System.out.println("");
						System.out.println("     1) Guess a letter.");
						System.out.println("     2) Guess the word.");
						System.out.println("");
						System.out.print("What would you like to do? ");
						String userInput = scan.nextLine();
						if(userInput.equals("1")) {	//guess a letter
							System.out.println("");
							System.out.print("Letter to guess - ");
							String guessedLetter = scan.nextLine();
							
							if(guessedLetter.length() > 1) {
								System.out.println("");
								System.out.println("You can only guess one letter at a time.");
								continue;
							}
							
							if (!Character.isLetter(guessedLetter.charAt(0))){
								System.out.println("");
								System.out.println("Please enter a letter");
								continue;
							}
							
							Message newMessage = new Message("checkGuessedLetter", emptyWord + "," + secretWord + "," + numGuesses + "," + indexOfGuesserUsername + "," + usernameOfGuesser + "," + gameName + "," + guessedLetter);
							try {
								oos.writeObject(newMessage);
								oos.flush();
							} catch(IOException ioe) {
								System.out.println("ioe near Client 596: " + ioe.getMessage());
								System.out.println(ioe.getCause());
								ioe.printStackTrace();
							}
							
							properUserInput = true;
						}
						
						else if (userInput.equals("2")) { //guess the word
							System.out.println("");
							System.out.print("What is the secret word? ");
							String guessedWord = scan.nextLine();
							
							Message newMessage = new Message("checkGuessedWord", emptyWord + "," + secretWord + "," + numGuesses + "," + indexOfGuesserUsername + "," + usernameOfGuesser + "," + gameName + "," + guessedWord);
							try {
								oos.writeObject(newMessage);
								oos.flush();
							} catch(IOException ioe) {
								System.out.println("ioe near Client 620: " + ioe.getMessage());
								System.out.println(ioe.getCause());
								ioe.printStackTrace();
							}
							properUserInput = true;
						}
						
						else {
							System.out.println("");
							System.out.println("That is not a valid input.");
						}
					}
					
					
				}
				
				
				else if (message.getTag().equals("waitingForOtherUserToGuess")) {
					String msg = message.getMessage();
					String [] msgArr = msg.split(",");
					
					String emptyWord = msgArr[0];
					String secretWord = msgArr[1];
					String numGuesses = msgArr[2];
					String indexOfGuesserUsername = msgArr[3];
					String usernameOfGuesser = msgArr[4];
					String gameName = msgArr[5];
					
//														System.out.println("emptyWord: " + emptyWord);
//														System.out.println("secretWord: " + secretWord);
//														System.out.println("numGuesses: " + numGuesses);
//														System.out.println("indexOfGuesserUsername: " + indexOfGuesserUsername);
//														System.out.println("usernameOfGuesser: " + usernameOfGuesser);
//														System.out.println("gameName: " + gameName);
														
					
					System.out.println("");
					System.out.println("You have " + numGuesses + " incorrect guesses remaining.");
					System.out.println("Waiting for " + usernameOfGuesser + " to do something...");
				}
				
				else if (message.getTag().equals("printGuessedLetterToOtherClients")) {
					String msg = message.getMessage();
					String [] msgArr = msg.split(",");
					
					String emptyWord = msgArr[0];
					String secretWord = msgArr[1];
					String numGuesses = msgArr[2];
					String indexOfGuesserUsername = msgArr[3];
					String usernameOfGuesser = msgArr[4];
					String gameName = msgArr[5];
					String guessedLetter = msgArr[6];
														
					
					System.out.println("");
					System.out.println(usernameOfGuesser + " has guessed letter '" + guessedLetter + "'.");
					System.out.println("");
				}
				
				
				else if (message.getTag().equals("printResultsOfGuessedLetter")) {					
					String msg = message.getMessage();
					String [] msgArr = msg.split(",");
					
					String emptyWord = msgArr[0];
					String secretWord = msgArr[1];
					String numGuesses = msgArr[2];
					String indexOfGuesserUsername = msgArr[3];
					String usernameOfGuesser = msgArr[4];
					String gameName = msgArr[5];
					String guessedLetter = msgArr[6];
					String wasGuessCorrect = msgArr[7];
														
					
					if(wasGuessCorrect.equals("1")) {	//guess was correct
						System.out.println("The letter '" + guessedLetter + "' is in the secret word.");
					}
					else if (wasGuessCorrect.equals("0")) {	//guess was incorrect
						System.out.println("The letter '" + guessedLetter + "' is not in the secret word.");
					}
					
					System.out.println("");
					System.out.println("Secret word: " + emptyWord);
					
					Message newMessage = new Message("nextGuess", emptyWord + "," + secretWord + "," + numGuesses + "," + indexOfGuesserUsername + "," + usernameOfGuesser + "," + gameName + "," + guessedLetter);
					try {
						oos.writeObject(newMessage);
						oos.flush();
					} catch(IOException ioe) {
						System.out.println("ioe near Client 702: " + ioe.getMessage());
						System.out.println(ioe.getCause());
						ioe.printStackTrace();
					}
				}
				
				
				
				else if (message.getTag().equals("printWinner") || message.getTag().equals("printWinnerMultiplayer")) {
					ArrayList<String> userNamesAL = new ArrayList<String>();
					ArrayList<String> numWinsAL = new ArrayList<String>();
					ArrayList<String> numLossesAL = new ArrayList<String>();
					
					String usernameOfGuesser = "";
					String numWins = "";
					String numLosses = "";
					
					String msg = message.getMessage();
					String [] msgArr = msg.split(",");
					
					for(int i = 0; i < msgArr.length; i++) {
						if(i%3 == 0) {
							usernameOfGuesser = msgArr[i];
							userNamesAL.add(usernameOfGuesser);
						}
						else if(i%3 == 1) {
							numWins = msgArr[i];
							numWinsAL.add(numWins);
						}
						else if (i%3 == 2) {
							numLosses = msgArr[i];
							numLossesAL.add(numLosses);
						}
					}
					
					
					
					if(message.getTag().equals("printWinner")) {
						System.out.println("");
						System.out.println("That is correct! You win!");
						System.out.println("");
						for(int i = 0; i < userNamesAL.size(); i++) {
							String printRecord = userNamesAL.get(i) + "'s Record";
							String dashedLines = "";
							for(int j = 0; j < printRecord.length(); j++) {
								dashedLines += "-";
							}
							System.out.println(printRecord);
							System.out.println(dashedLines);
							System.out.println("Wins - " + numWinsAL.get(i));
							System.out.println("Losses - " + numLossesAL.get(i));
							System.out.println("");
						}
						
						System.out.println("Thank you for playing hangman!");
						break;
					}
					
					else if (message.getTag().equals("printWinnerMultiplayer")) {
						userNamesAL.remove(userNamesAL.size()-1);
						System.out.println("");
						System.out.println("That is correct! You win!");
						System.out.println("");
						for(int i = 0; i < userNamesAL.size(); i++) {
							String printRecord = userNamesAL.get(i) + "'s Record";
							String dashedLines = "";
							for(int j = 0; j < printRecord.length(); j++) {
								dashedLines += "-";
							}
							System.out.println(printRecord);
							System.out.println(dashedLines);
							System.out.println("Wins - " + numWinsAL.get(i));
							System.out.println("Losses - " + numLossesAL.get(i));
							System.out.println("");
						}
						
						System.out.println("Thank you for playing hangman!");
						break;
						
					}
				}
				
				
				
				else if (message.getTag().equals("printLoser") || message.getTag().equals("printLoserMultiplayer")) {
					ArrayList<String> userNamesAL = new ArrayList<String>();
					ArrayList<String> numWinsAL = new ArrayList<String>();
					ArrayList<String> numLossesAL = new ArrayList<String>();
					
					String usernameOfGuesser = "";
					String numWins = "";
					String numLosses = "";
					
					String msg = message.getMessage();
					String [] msgArr = msg.split(",");
					
					String guessedWord = msgArr[msgArr.length-1];
					
					for(int i = 0; i < msgArr.length; i++) {
						if(i%3 == 0) {
							usernameOfGuesser = msgArr[i];
							userNamesAL.add(usernameOfGuesser);
						}
						else if(i%3 == 1) {
							numWins = msgArr[i];
							numWinsAL.add(numWins);
						}
						else if (i%3 == 2) {
							numLosses = msgArr[i];
							numLossesAL.add(numLosses);
						}
					}
					
					if(message.getTag().equals("printLoser")) {
						System.out.println("");
						System.out.println("That is incorrect. You lose.");
						System.out.println("");
						for(int i = 0; i < userNamesAL.size(); i++) {
							String printRecord = userNamesAL.get(i) + "'s Record";
							String dashedLines = "";
							for(int j = 0; j < printRecord.length(); j++) {
								dashedLines += "-";
							}
							System.out.println(printRecord);
							System.out.println(dashedLines);
							System.out.println("Wins - " + numWinsAL.get(i));
							System.out.println("Losses - " + numLossesAL.get(i));
							System.out.println("");
						}
						
						System.out.println("Thank you for playing hangman!");
						break;
					}
					
					else if (message.getTag().equals("printLoserMultiplayer")) {
						userNamesAL.remove(userNamesAL.size()-1);
						System.out.println("");
						System.out.println(userNamesAL.get(0) + " has guessed the word '" + guessedWord + "'.");
						System.out.println("");
						System.out.println(userNamesAL.get(0) + " guessed the word correctly. You lose!");
						System.out.println("");
						for(int i = userNamesAL.size()-1; i >= 0; i--) {
							String printRecord = userNamesAL.get(i) + "'s Record";
							String dashedLines = "";
							for(int j = 0; j < printRecord.length(); j++) {
								dashedLines += "-";
							}
							System.out.println(printRecord);
							System.out.println(dashedLines);
							System.out.println("Wins - " + numWinsAL.get(i));
							System.out.println("Losses - " + numLossesAL.get(i));
							System.out.println("");
						}
						
						System.out.println("Thank you for playing hangman!");
						break;
					}
				}
				
				
				else if (message.getTag().equals("1playerNoGuessesLeft")) {
					String msg = message.getMessage();
					String [] msgArr = msg.split(",");
					
					String usernameOfGuesser = msgArr[0];
					String numWins = msgArr[1];
					String numLosses = msgArr[2];
					
					System.out.println("");
					System.out.println("You ran out of guesses. You lose.");
					System.out.println("");

					String printRecord = usernameOfGuesser + "'s Record";
					String dashedLines = "";
					for(int j = 0; j < printRecord.length(); j++) {
						dashedLines += "-";
					}
					System.out.println(printRecord);
					System.out.println(dashedLines);
					System.out.println("Wins - " + numWins);
					System.out.println("Losses - " + numLosses);
					System.out.println("");
					
					System.out.println("Thank you for playing hangman!");
					break;
					
				}
				
				
				else if (message.getTag().equals("runOutOfGuessesMultiplayer")) {
					ArrayList<String> userNamesAL = new ArrayList<String>();
					ArrayList<String> numWinsAL = new ArrayList<String>();
					ArrayList<String> numLossesAL = new ArrayList<String>();
					
					String msg = message.getMessage();
					String [] msgArr = msg.split(",");
					
					String guessedLetter = msgArr[msgArr.length-1];
					String usernameOfGuesser = msgArr[msgArr.length-2];
					
					String numWins = "";
					String numLosses = "";
					
					String tempName = "";
					
					for(int i = 0; i < msgArr.length; i++) {
						if(i%3 == 0) {
							tempName = msgArr[i];
							userNamesAL.add(tempName);
						}
						else if(i%3 == 1) {
							numWins = msgArr[i];
							numWinsAL.add(numWins);
						}
						else if (i%3 == 2) {
							numLosses = msgArr[i];
							numLossesAL.add(numLosses);
						}
					}
					
					userNamesAL.remove(userNamesAL.size()-1);
					
					System.out.println("");
					System.out.println(usernameOfGuesser + " has guessed letter '" + guessedLetter + "'.");
					System.out.println("");
					System.out.println("You have run out of guesses. Everyone loses!");
					System.out.println("");
					
					for(int i = userNamesAL.size()-1; i >= 0; i--) {
						String printRecord = userNamesAL.get(i) + "'s Record";
						String dashedLines = "";
						for(int j = 0; j < printRecord.length(); j++) {
							dashedLines += "-";
						}
						System.out.println(printRecord);
						System.out.println(dashedLines);
						System.out.println("Wins - " + numWinsAL.get(i));
						System.out.println("Losses - " + numLossesAL.get(i));
						System.out.println("");
					}
					
					System.out.println("Thank you for playing hangman!");
					break;
					
				}
				
				
				else if (message.getTag().equals("guessedWrongWordMultiplayer")) {
					System.out.println("");
					System.out.println("You guessed the wrong word and lost. You will now spectate for the rest of the game.");
					System.out.println("");
				}
				
				
				else if (message.getTag().equals("otherUserGuessedWrongWordMultiplayer")) {
					String msg = message.getMessage();
					String [] msgArr = msg.split(",");
					
					String usernameOfGuesser = msgArr[0];
					
					System.out.println("");
					System.out.println(usernameOfGuesser + " guessed the word incorrectly and lost the game. " + usernameOfGuesser + " will now be in spectator mode.");
					System.out.println("");
					
				}
				
				
				
				else if (message.getTag().equals("printGameLogicUI2Player")) {
					String msg = message.getMessage();
					String [] msgArr = msg.split(",");
					
					String emptyWord = msgArr[0];
					String secretWord = msgArr[1];
					String numGuesses = msgArr[2];
					String indexOfGuesserUsername = msgArr[3];
					String usernameOfGuesser = msgArr[4];
					String gameName = msgArr[5];
															
					
					Boolean properUserInput = false;
					
					while(properUserInput == false) {
						System.out.println("");
						System.out.println("You have " + numGuesses + " incorrect guesses remaining.");
						System.out.println("");
						System.out.println("     1) Guess a letter.");
						System.out.println("     2) Guess the word.");
						System.out.println("");
						System.out.print("What would you like to do? ");
						String userInput = scan.nextLine();
						if(userInput.equals("1")) {	//guess a letter
							System.out.println("");
							System.out.print("Letter to guess - ");
							String guessedLetter = scan.nextLine();
							
							if(guessedLetter.length() > 1) {
								System.out.println("");
								System.out.println("You can only guess one letter at a time.");
								continue;
							}
							
							Message newMessage = new Message("checkGuessedLetter", emptyWord + "," + secretWord + "," + numGuesses + "," + indexOfGuesserUsername + "," + usernameOfGuesser + "," + gameName + "," + guessedLetter);
							try {
								oos.writeObject(newMessage);
								oos.flush();
							} catch(IOException ioe) {
								System.out.println("ioe near Client 596: " + ioe.getMessage());
								System.out.println(ioe.getCause());
								ioe.printStackTrace();
							}
							
							properUserInput = true;
						}
						
						else if (userInput.equals("2")) { //guess the word
							System.out.println("");
							System.out.print("What is the secret word? ");
							String guessedWord = scan.nextLine();
							
							Message newMessage = new Message("checkGuessedWord", emptyWord + "," + secretWord + "," + numGuesses + "," + indexOfGuesserUsername + "," + usernameOfGuesser + "," + gameName + "," + guessedWord);
							try {
								oos.writeObject(newMessage);
								oos.flush();
							} catch(IOException ioe) {
								System.out.println("ioe near Client 620: " + ioe.getMessage());
								System.out.println(ioe.getCause());
								ioe.printStackTrace();
							}
							properUserInput = true;
						}
						
						else {
							System.out.println("");
							System.out.println("That is not a valid input.");
						}
					}
					
					
				}
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
//				else if (message.getTag().equals("printGameLogicUI2")) {
//					String msg = message.getMessage();
//					String [] msgArr = msg.split(",");
//					
//					String emptyWord = msgArr[0];
//					String secretWord = msgArr[1];
//					String numGuesses = msgArr[2];
//					String indexOfGuesserUsername = msgArr[3];
//					String usernameOfGuesser = msgArr[4];
//					String gameName = msgArr[5];
//					
//					
//					
//															System.out.println("emptyWord: " + emptyWord);
//															System.out.println("secretWord: " + secretWord);
//															System.out.println("numGuesses: " + numGuesses);
//															System.out.println("indexOfGuesserUsername: " + indexOfGuesserUsername);
//															System.out.println("usernameOfGuesser: " + usernameOfGuesser);
//															System.out.println("gameName: " + gameName);
//															
//					
//					if(message.getTag().equals("letterAlreadyGuessedBefore")) {
//						System.out.println("");
//						System.out.println("This letter has already been guessed before. Try guessing a different letter.");
//					}
//					
//					Boolean properUserInput = false;
//					
//					while(properUserInput == false) {
//						System.out.println("");
//						System.out.println("You have " + numGuesses + " incorrect guesses remaining.");
//						System.out.println("");
//						System.out.println("     1) Guess a letter.");
//						System.out.println("     2) Guess the word.");
//						System.out.println("");
//						System.out.print("What would you like to do? ");
//						String userInput = scan.nextLine();
//						if(userInput.equals("1")) {	//guess a letter
//							System.out.println("");
//							System.out.print("Letter to guess - ");
//							String guessedLetter = scan.nextLine();
//							
//							if(guessedLetter.length() > 1) {
//								System.out.println("");
//								System.out.println("You can only guess one letter at a time.");
//								continue;
//							}
//							
//							Message newMessage = new Message("checkGuessedLetter2", emptyWord + "," + secretWord + "," + numGuesses + "," + indexOfGuesserUsername + "," + usernameOfGuesser + "," + gameName + "," + guessedLetter);
//							try {
//								oos.writeObject(newMessage);
//								oos.flush();
//							} catch(IOException ioe) {
//								System.out.println("ioe near Client 596: " + ioe.getMessage());
//								System.out.println(ioe.getCause());
//								ioe.printStackTrace();
//							}
//							
//							properUserInput = true;
//						}
//						
//						else if (userInput.equals("2")) { //guess the word
//							System.out.println("");
//							System.out.print("What is the secret word? ");
//							String guessedWord = scan.nextLine();
//							
//							Message newMessage = new Message("checkGuessedWord2", emptyWord + "," + secretWord + "," + numGuesses + "," + indexOfGuesserUsername + "," + usernameOfGuesser + "," + gameName + "," + guessedWord);
//							try {
//								oos.writeObject(newMessage);
//								oos.flush();
//							} catch(IOException ioe) {
//								System.out.println("ioe near Client 620: " + ioe.getMessage());
//								System.out.println(ioe.getCause());
//								ioe.printStackTrace();
//							}
//							properUserInput = true;
//						}
//						
//						else {
//							System.out.println("");
//							System.out.println("That is not a valid input.");
//						}
//					}
//					
//					
//				}
//				
//				
//				else if (message.getTag().equals("printResultsOfGuessedLetter2")) {					
//					String msg = message.getMessage();
//					String [] msgArr = msg.split(",");
//					
//					String emptyWord = msgArr[0];
//					String secretWord = msgArr[1];
//					String numGuesses = msgArr[2];
//					String indexOfGuesserUsername = msgArr[3];
//					String usernameOfGuesser = msgArr[4];
//					String gameName = msgArr[5];
//					String guessedLetter = msgArr[6];
//					String wasGuessCorrect = msgArr[7];
//														
//					
//					if(wasGuessCorrect.equals("1")) {	//guess was correct
//						System.out.println("The letter '" + guessedLetter + "' is in the secret word.");
//					}
//					else if (wasGuessCorrect.equals("0")) {	//guess was incorrect
//						System.out.println("The letter '" + guessedLetter + "' is not in the secret word.");
//					}
//					
//					System.out.println("");
//					System.out.println("Secret word: " + emptyWord);
//					
//					//////////////////////////////////////////////////////
//					Message newMessage = new Message("nextGuess2", emptyWord + "," + secretWord + "," + numGuesses + "," + indexOfGuesserUsername + "," + usernameOfGuesser + "," + gameName + "," + guessedLetter);
//					try {
//						oos.writeObject(newMessage);
//						oos.flush();
//					} catch(IOException ioe) {
//						System.out.println("ioe near Client 702: " + ioe.getMessage());
//						System.out.println(ioe.getCause());
//						ioe.printStackTrace();
//					}
//				}
				
				
				
				
				
//				else {
//					System.out.println("");
//																System.out.println("-------------sent something with unrecognized tag: " + message.getTag());
//					System.out.println("");	//prints empty lines when waiting for other user to do something
//				}
				
			}
			
		} catch(IOException ioe) {
			System.out.println("ioe in client run(): " + ioe.getMessage());
		} catch (ClassNotFoundException cnfe) {
			System.out.println("cnfe in client run(): " + cnfe.getMessage());
		}
		
	}		//end of run()
	

	
	
	
	

}
