import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerThread extends Thread {

	private Server server;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	public Boolean printGameLogicUIToThisClient;
	
		
	public ServerThread(Socket s, Server server) {
		//this serverthread is responsible for each client. THis thread needs to know how to read and send back out messages
	
		this.server = server;				//all the Chatroom variables here are written to grab the chatroom instance (mainly the vector of messages
		
									//System.out.println("___________initialize a ServerThread using constructor");
		
		try {
			ois = new ObjectInputStream(s.getInputStream());
			oos = new ObjectOutputStream(s.getOutputStream());
			
			printGameLogicUIToThisClient = true;
			
			this.start();
								
		} catch(IOException ioe) {
			System.out.println("ioexception in ServerThread: " + ioe.getMessage());
		}
	
	}


	public void run() {	//this method needs to receive objects from each client
	
		try {
			while(true) {

				Message message = (Message)ois.readObject();
				String tag = message.getTag();
				String msg = message.getMessage();
				
				if(tag.equals("login")) {
					server.login(msg,this);
				}
				
				else if (tag.equals("createNewAccount")) {
					server.createNewAccount(msg, this);
				}
	
				else if(tag.equals("makeGame")) {
					server.createGame(msg, this);
				}
				
				else if (tag.equals("getRecord1")) {
					server.getRecord1(msg, this);
				}
				
				else if (tag.equals("doNothing1")) {
					server.doNothing1(msg, this);
				}
				
				else if (tag.equals("checkIfGameExists")) {
					server.checkIfGameExists(msg, this);
				}
				
				else if (tag.equals("createGame")) {
					server.createGame(msg, this);
				}
				
				else if (tag.equals("joinGameGetRecords")) {
					server.joinGameGetRecords(msg, this);
				}
				
				
				else if (tag.equals("askForGuessInput")) {
					server.askForGuessInput(msg, this);
				}
				
				else if (tag.equals("checkGuessedLetter")) {
					server.checkGuessedLetter(msg, this);
				}
				
				else if (tag.equals("nextGuess")) {
					server.nextGuess(msg, this);
				}
				
				else if (tag.equals("checkGuessedWord")) {
					server.checkGuessedWord(msg, this);
				}
				
				
				
				
				
				
				
				
//				else if (tag.equals("checkGuessedLetter2")) {
//					server.checkGuessedLetter2(msg, this);
//				}
//				
////				else if (tag.equals("checkGuessedWord2")) {
////					server.checkGuessedWord2(msg, this);
////				}
//				
//				else if (tag.equals("nextGuess2")) {
//					server.nextGuess2(msg, this);
//				}
				
			}
			
										
		} catch(IOException ioe) {
			System.out.println("ioe in St.run(): " + ioe.getMessage());
		} catch (ClassNotFoundException cnfe) {
			System.out.println("cnfe in St.run(): " + cnfe.getMessage());
		}
	
	
	}

	
	public void sendMessage(Message message) {
//								System.out.println("_______enter serverthread sendMessage()");
		try {
			oos.writeObject(message);
			oos.flush();
								//System.out.println("                             finish serverthread sendMessage() with tag: " + message.getTag() + "     message: " + message.getMessage());
		} catch(IOException ioe) {
			System.out.println("ioe: " + ioe.getMessage());
		}
	}
	
}
