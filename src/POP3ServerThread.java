import java.net.*;
import java.util.Date;
import java.io.*;

// added method to check that database was started succesfully before continuing
public class POP3ServerThread implements Runnable {

	private Socket socket;
	
	private final static String helloMessage = "+OK POP3 Server Ready";

	private final static String terminationMessage = "+OK POP3 Server Connection Terminated";
	private final static String terminationMessage2 = "-ERR Some messages marked for deletion were not removed";
	private final static String terminationMessage3 = "+OK Meassages Deleted, POP3 Server Connection Terminated";
	
	private final static String databaseErrorMessage = "-ERR Database Connection Could Not Be Started";
	private final static String databaseErrorMessage2 = "-ERR Database Connection Terminated Unexpectedly";

	private final static String serverTag = "[Server]: ";
	private final static String clientTag = "[Client]: ";

	public POP3ServerThread(Socket socket, int timeoutValue) {
		this.socket = socket;

		try {
			this.socket.setSoTimeout(timeoutValue * 1000);
		} catch (SocketException e) {
			System.err.println("Socket Exception - See Stack Trace Below");
			e.printStackTrace();
			closeSocket();
		}

		System.out.println("New Client Connection Started");
	}

	public void run() {

		try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));) {

			String inputLine, outputLine;
			CommandInterpreter ci = new CommandInterpreter();
			
			if (ci.checkInitialStatus()) {
				out.println(serverTag + helloMessage);
			} else {
				out.println(serverTag + databaseErrorMessage);
				closeSocket();
			}

			while (true) {
				
				out.printf(clientTag);
				inputLine = in.readLine();
				outputLine = ci.handleInput(inputLine);
				out.println(serverTag + outputLine);
				

				if (outputLine.equals(terminationMessage)
						|| outputLine.equals(terminationMessage2)
						|| outputLine.equals(terminationMessage3)
						|| outputLine.equals(databaseErrorMessage2)) {
					break;
				}

			}
			ci.closeInterpreter();
			closeSocket();

		} catch (IOException e) {
			System.err.println("SYSTEM ERROR/Timeout Due To Inactivity"
					+ new Date() + " - Stack Trace Shown Below.");
			e.printStackTrace();
		}
	}
	
	private void closeSocket() {
		try {
			System.out.println("Connection Terminated");
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
