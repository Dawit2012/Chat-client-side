package name.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import name.common.ChatUserProfile;
import name.common.Chat;
import name.server.ChatServer;



public class ChatClient{
	
	private static ChatUserProfile myProfile;
	private static Chat myChatCoordinator; 
	private static ChatServer myChatServer;
	
	public ChatClient ()  {
		
	}
	
	// User is expected to start the application by providing name and IP address
	// Input format of this program: userName UserIPAddress MainServerName MainServerIPAddress
	public static void main(String args[]) {
		
		Remote rObject;
		String chatUserName = "Anonymous";
		String chatUserIPAddress = "127.0.0.1"; 
		String coordinatorChatServerName = "ChatServer";
		String coordinatorChatServerIP = "127.0.0.1";    
		
		//Get chat user name,  chat client IP address, main chat server name and 
		//main chat server IP address from the users
		//
		//If no value is given, use the default values 
		//(Anonymous, 127.0.0.1, ChatServer, 127.0.0.1) 
		
		if (args.length == 4) {
			chatUserName = args[0];
			chatUserIPAddress = args[1];
			coordinatorChatServerName = args[2];
			coordinatorChatServerIP = args[3];
		}
		else if (args.length != 0 && args.length != 4){
			System.err.println("Incorrect input! \n " +
					"  Usage: java name.client.ChatClient userName UserIPAddress CoordinatorServerName CoordinatorServerIPAddress \n " +
					"\t OR \n " +
					"  Give UserName UserIPAddress CoordinatorServerName CoordinatorServerIPAddress as arguments to eclipse \n");
			System.exit(1);
		}
		
		try {
			
			//Check if there is a coordinator registered with the coordinatorChatServerIP and coordinatorChatServerName
			//If there is no one, this client's server becomes coordinator
			String coordinatorChatServerLocation = "//" + coordinatorChatServerIP + "/" + coordinatorChatServerName;
			
			if (Naming.list(coordinatorChatServerLocation).length == 0 ){
				startChatServer(coordinatorChatServerName, coordinatorChatServerIP);
				//The first chat user becomes a coordinator 
				chatUserName = coordinatorChatServerName;
				chatUserIPAddress = coordinatorChatServerIP;
				//Wait until the server is started and registered
				while (Naming.list(coordinatorChatServerLocation).length == 0)
					Thread.sleep(30);
				System.out.println("No chat server was registered");
			}
			else{
				startChatServer(chatUserName, chatUserIPAddress);
				System.out.println("Registered chat server found - local chat server started");
			}
			
			rObject = Naming.lookup(coordinatorChatServerLocation);	
			myChatCoordinator = (Chat) rObject;
			myProfile = new ChatUserProfile(chatUserName, chatUserIPAddress);
			boolean successfulRegistration = myChatCoordinator.registerUser(myProfile);
			
			if (!successfulRegistration){
				System.err.println("Chat client error: \n\t :- " + chatUserName + " -: UserName already exists. Please use a different user name.");
				myChatCoordinator.stopMyChatServer();
				System.exit(1);
			}
			startSendingMessage();

		} catch (Exception e) {
			System.out.println("Client chat exception: " + e.getMessage());
			e.printStackTrace();
		}
	}
	

	/**
	 * This method starts the chat message listener thread for this client
	 * 
	 */
	private static void startChatServer(String serverName, String serverIP) {
			
	}

	
	/**
	 * This method asks the server to give it other registered users and 
	 * sends message to all users 
	 * 
	 * @param myChatCoordinator
	 * @throws RemoteException 
	 */
	private static void startSendingMessage() throws RemoteException {
		Set<ChatUserProfile> otherChatUsers = new HashSet<ChatUserProfile>();
		String currentMessage = "";
		boolean exitServer = false;
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		while (!exitServer) {
			
			try {
				currentMessage = in.readLine();
			} catch (IOException e) {
				System.err.println("Error while reading user chat message");
				e.printStackTrace();
			}
			
			if ("exit".equals(currentMessage.toLowerCase().trim())) {
				currentMessage = "Going out of chat room... bye bye!";
				exitServer =  true; 
			}
			otherChatUsers = myChatCoordinator.getCurrentUsers();
			sendMessageToAllChatUsers(currentMessage, otherChatUsers);
		}
		
		if (exitServer){
			myChatCoordinator.removeUser(myProfile);
			myChatServer.stopMyChatServer();
			System.exit(0);
		}
		
	}


	/**
	 * Sends the current chat message to all users
	 * 
	 * @param currentMessage
	 * @param otherChatUsers
	 */
	private static void sendMessageToAllChatUsers(String currentMessage,
			Set<ChatUserProfile> otherChatUsers) {
	
	}
}


