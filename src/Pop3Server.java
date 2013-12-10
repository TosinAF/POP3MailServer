/**
 * The Class POP3 Server.
 * 
 * @author Tosin Afolabi
 * @version 1.0
 * 
 *          This class will accept incoming connections from clients.
 *          It will create a new socket for each client & start a new POP3 Server thread 
 *          
 *          It opens a listening socket on the local machine, 
 *          on a port specified on the command line that starts the server
 *          
 *          When a client connects to the server, it accepts the connection, creates a new client socket 
 *          which is passed into a new POP3Server Thread 
 *          
 *          This thread routes the input from the client to the Command Interpreter
 *          Responses generated by the Command Interpreter are then routed back to the client over the network connection.
 *          
 *          Till the connection is closed or aborted
 *          
 *          Note - Only the formatting of the strings in Command Interpreter were changed.
 */

import java.net.*;
import java.io.*;

public class Pop3Server {

	private static int portNumber = 110;
	private static int timeoutValue = 600;
	private static boolean listening = true;
	private static ServerSocket serverSocket;
	private final static String usageMessage = "Usage: java Pop3Server <port number> <timeout value>(optional)";

	public static void main(String[] args) {

		System.out.println("Begin Server Log.");

		if (parseCommandArguementsIsSuccesful(args)) {

			if (startServerSocket()) {
				acceptClientConnections();
			}
		}

		System.exit(1);
	}

	private static boolean startServerSocket() {

		try {

			serverSocket = new ServerSocket(portNumber);
			System.out.println("Awaiting Connection...");
			return true;

		} catch (IOException e) {
			System.err
					.println("ERROR - Could not listen on port " + portNumber);
			return false;
		}
	}

	private static void acceptClientConnections() {

		while (listening) {

			try {

				new Thread(new POP3ServerThread(serverSocket.accept(),
						timeoutValue)).start();

			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("ERROR - Connection could not be accepted");
				stopServerSocket();
			}

		}
	}
	
	private static void stopServerSocket() {

		if (serverSocket != null && !serverSocket.isClosed()) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				System.err.println("ERROR - Server Socket Could Not Be Closed");
			}
		}
	}

	private static boolean parseCommandArguementsIsSuccesful(String[] args) {

		switch (args.length) {

		case 0:

			System.err.println(usageMessage);
			return false;

		case 1:
		case 2:

			if (!checkThatArguementsAreValid(args)) {

				System.err.println("ERROR - All Arguements must be integers");
				return false;

			} else if (args.length == 1) {

				portNumber = Integer.parseInt(args[0]);

			} else if (args.length == 2) {

				portNumber = Integer.parseInt(args[0]);
				timeoutValue = Integer.parseInt(args[1]);
			}

			return true;

		default:

			System.err.println("Too Many Arguements, " + usageMessage);
			return false;

		}
	}

	private static boolean checkThatArguementsAreValid(String[] args) {

		for (int i = 0; i < args.length; i++) {

			try {
				Integer.parseInt(args[i]);
			} catch (NumberFormatException e) {
				return false;
			}
		}
		return true;
	}
}