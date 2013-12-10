/**
 * The Class Command Interpreter.
 * 
 * @author Tosin Afolabi
 * @version 1.0
 * 
 *          This class will receive commands from the network Call the
 *          approaiate method on the Databse then send a response back to the
 *          client
 * 
 *          It will support the following commands USER, PASS, QUIT, STAT, LIST,
 *          RETR, DELE, NOOP, RSET, TOP, & UIDL
 * 
 *          Uses a Mock db Backend that implements the IDatabase Interface
 * 
 *          It follows the specfication written by J.Myers Of Carnegie Mellon
 *          
 *          **Added a few more methods to check whether database connection is active
 */

public class CommandInterpreter implements ICommandInterpreter {

	private Database db;
	private String usernameToAuthWithPassword;
	private boolean previousCommandWasASuccessfulUSERAuth;
	private InterpreterState currentState;

	public enum POP3Command {
		USER, PASS, QUIT, STAT, LIST, RETR, DELE, NOOP, RSET, TOP, UIDL, ERROR;
	}

	public enum InterpreterState {
		AUTHORIZATION, TRANSACTION, UPDATE;
	}

	public CommandInterpreter() {

		db = new Database();
		usernameToAuthWithPassword = "";
		previousCommandWasASuccessfulUSERAuth = false;
		currentState = InterpreterState.AUTHORIZATION;

	}

	public boolean checkInitialStatus() {
		return db.wasConnectionStarted();
	}
	
	public void closeInterpreter() {
		db.closeConnection();
	}

	public String handleInput(String input) {
		
		if (!db.isConnectionValid()) return "-ERR Database Connection Terminated Unexpectedly";

		String response = "";

		// Remove Extra Whitespace
		String trimmedInput = input.trim();

		// Split Input into Arguements
		String[] arguementsArray = trimmedInput.split(" ");

		// Check that no arguement is greater then 40 characters
		if (!lengthOfArguementsIsWithinLimit(arguementsArray)) {
			return "-ERR One of the arguements in the command is longer than 40 characters";
		}

		// Number Of arguements, not including the command keyword, e.g USER
		int numberOfArguements = arguementsArray.length - 1;

		POP3Command command = setPOP3CommandEnum(arguementsArray[0]);

		if (currentState == InterpreterState.AUTHORIZATION) {

			switch (command) {

			case USER:

				/*
				 * Provides username to the POP3 server. Must be followed by a
				 * PASS command.
				 * 
				 * @arguements - a string identifying a mailbox (required)
				 */

				if (numberOfArguements != 1)
					return "-ERR Syntax -> USER mailboxName(required)";

				String username = arguementsArray[1];
				response += authenticateUsername(username);
				break;

			case PASS:

				/*
				 * Provides a password to the POP3 server. Must follow a
				 * Successful USER command.
				 * 
				 * @arguements - a server/mailbox-specific password (required)
				 * 
				 * Spaces in the argument are treated as part of the password,
				 * instead of as argument separators.
				 */

				if (input.length() < 6)
					return "-ERR Syntax -> PASS password(required)";

				response += authenticatePassword(input);
				previousCommandWasASuccessfulUSERAuth = false;
				break;

			case QUIT:

				/*
				 * Ends the POP3 session
				 * 
				 * @arguements - none
				 */

				if (numberOfArguements != 0)
					return "-ERR This command has no arguements";

				response += "+OK POP3 Server Connection Terminated";
				usernameToAuthWithPassword = null;
				previousCommandWasASuccessfulUSERAuth = false;
				break;

			case ERROR:

				/*
				 * Unsupported Commands, e.g APOP
				 */

				response += "-ERR Unsupported Command Given";
				previousCommandWasASuccessfulUSERAuth = false;
				break;

			default:

				/*
				 * For all other supported commands, e.g LIST, RETR that are not
				 * allowed to run in AUTHROZIATION STATE
				 */

				response += "-ERR User needs to be authenticated";
				previousCommandWasASuccessfulUSERAuth = false;
				break;
			}

		} else if (currentState == InterpreterState.TRANSACTION) {

			int messageNumber = -1;

			// If the command has arguements, then arguementsArray[1] holds the
			// message number
			if (numberOfArguements > 0 && command != POP3Command.USER
					&& command != POP3Command.PASS) {

				if (stringCanBeParsedAsAnInteger(arguementsArray[1])) {
					messageNumber = Integer.parseInt(arguementsArray[1]);
				} else {
					return "-Err Invalid Arguement Type";
				}
			}

			switch (command) {

			case USER:
			case PASS:
				response += "-ERR maildrop already locked";
				break;

			case STAT:

				/*
				 * Returns the number of messages and total size of mailbox.
				 * 
				 * @arguements - none
				 */

				if (numberOfArguements > 0)
					return "-ERR This command has no arguements";

				response += getDropListing();
				break;

			case LIST:

				/*
				 * Lists message number and size of each message. If a message
				 * number is specified, returns the size of the specified
				 * message.
				 * 
				 * @arguements - messageNumber (optional)
				 */

				if (numberOfArguements > 1)
					return "-ERR Syntax -> LIST messageNumber(optional)";

				response += getScanListing(numberOfArguements, messageNumber);
				break;

			case RETR:

				/*
				 * Returns the full text of the specified message, and marks
				 * that message as read.
				 * 
				 * @arguements - messageNumber (required)
				 */

				if (numberOfArguements != 1)
					return "-ERR Syntax -> RETR messageNumber(required)";

				response += getMessageBody(messageNumber);
				break;

			case DELE:

				/*
				 * Marks the specified message for deletion.
				 * 
				 * @arguements - messageNumber (required)
				 */

				if (numberOfArguements != 1)
					return "-ERR Syntax -> DELE messageNumber(required)";

				response += markMessageForDeletion(messageNumber);
				break;

			case NOOP:

				/*
				 * Returns a simple acknowledgement, without performing any
				 * function.
				 * 
				 * @arguements - none
				 */

				if (numberOfArguements != 0)
					return "-ERR This command has no arguements";

				response += "+OK";
				break;

			case RSET:

				/*
				 * Umarks all Messages Marked For Deletion
				 * 
				 * @arguements - none
				 */

				if (numberOfArguements != 0)
					return "-ERR This command has no arguements";

				db.unmarkAllMessagesMarkedForDeletion();
				response += "+OK maildrop has "
						+ db.getNumberOfMailsInMaildrop() + " ("
						+ db.getSizeOfMaildrop() + ") octets";
				break;

			case TOP:

				/*
				 * Returns the specified number of lines from the specified
				 * mesasge number.
				 * 
				 * @arguements - messageNumber, numberOfLines(non negative)
				 * (both required)
				 */

				if (numberOfArguements != 2)
					return "-ERR Syntax -> TOP messageNumber, numberOfLines(non negative) (both required)";

				int numberOfLines = -1;

				if (stringCanBeParsedAsAnInteger(arguementsArray[2])) {
					numberOfLines = Integer.parseInt(arguementsArray[2]);
				} else {
					return "-Err Invalid Arguement Type\n\n";
				}

				if (numberOfLines >= 0) {
					response += getXNumberOfLinesInMessageBody(messageNumber,
							numberOfLines);
				} else {
					response += "-ERR Non-Negative Number must be given with this command";
				}
				break;

			case UIDL:

				/*
				 * Returns the UniqueID Listing of a Message or all messages
				 * 
				 * @arguements - messageNumber (optional)
				 */

				if (numberOfArguements > 1)
					return "-ERR Syntax -> UIDL messageNumber(optional)";

				response += getUniqueIDListing(trimmedInput, messageNumber);
				break;

			case QUIT:

				/*
				 * Ends the POP3 session & Changes State to UPDATE
				 * 
				 * @arguements - none
				 */

				if (numberOfArguements != 0)
					return "-ERR This command has no arguements";

				currentState = InterpreterState.UPDATE;
				if (db.deleteAllMessagesMarkedforDeletion()) {
					response += "+OK Messages Deleted, POP3 Server Connection Terminated";
				} else {
					response += "-ERR Some messages marked for deletion were not removed";
				}
				break;

			case ERROR:
			default:

				/*
				 * Unsupported Commands, e.g APOP
				 */

				response += "-ERR Unsupported Command Given";
				break;
			}
		} else if (currentState == InterpreterState.TRANSACTION) {
			response += "-ERR The connection to the mail server has been terminated";
		}

		return response;
	}

	private String authenticateUsername(String username) {

		if (db.authenticateUser(username)) {

			usernameToAuthWithPassword = username;
			previousCommandWasASuccessfulUSERAuth = true;
			return "+OK name is a valid mailbox";

		} else {

			previousCommandWasASuccessfulUSERAuth = false;
			return "-ERR never heard of mailbox name";

		}
	}

	private String authenticatePassword(String input) {

		if (previousCommandWasASuccessfulUSERAuth) {

			String password = input.substring(5);

			if (db.authenticatePassword(usernameToAuthWithPassword,
					password)) {

				currentState = InterpreterState.TRANSACTION;
				return "+OK maildrop locked & ready";

			} else {
				return "-ERR Invalid Password";
			}

		} else {
			return "-ERR A successful USER Command needs to be run immediately before";
		}
	}

	private String getDropListing() {
		return "+OK " + db.getNumberOfMailsInMaildrop() + " "
				+ db.getSizeOfMaildrop();
	}

	private String getScanListing(int numberOfArguements, int messageNumber) {

		if (numberOfArguements == 0) {

			// Command contains No Arguements

			if (db.getNumberOfMailsInMaildrop() == 0) {
				return "+OK no messages in maildrop";
			} else {
				return "+OK scan listing follows\n"
						+ db.getScanListingOfAllMessages();
			}

		} else {

			// Command contains Arguements

			if (db.getAccessStatusOfMessage(messageNumber)) {

				return "+OK " + db.getScanListingOfMessage(messageNumber);

			} else {
				return "-ERR Message not found or has been marked for deletion";
			}
		}
	}

	private String getMessageBody(int messageNumber) {

		if (db.getAccessStatusOfMessage(messageNumber)) {

			return "+OK " + db.getSizeOfMessageInOctets(messageNumber)
					+ " octets\n" + db.getMessageBody(messageNumber);

		} else {
			return "-ERR Message not found or has been marked for deletion or invalid arguement";
		}
	}

	private String getXNumberOfLinesInMessageBody(int messageNumber,
			int numberOfLines) {

		if (db.getAccessStatusOfMessage(messageNumber)) {

			return "+OK \n"
					+ db.getXNumberOfLinesInMessageBody(messageNumber,
							numberOfLines);

		} else {
			return "-ERR Message not found or has been marked for deletion";
		}
	}

	private String markMessageForDeletion(int messageNumber) {

		if (db.getAccessStatusOfMessage(messageNumber)) {

			db.markMessageForDeletion(messageNumber);
			return "+OK Message successfully marked for deletion";

		} else {
			return "-ERR Message not found or has already been marked for deletion";
		}
	}

	private String getUniqueIDListing(String trimmedInput, int messageNumber) {

		if (trimmedInput.length() == 4) {

			// Command contains No Arguements

			return "+OK Uniquie ID listing follows\n"
					+ db.getUniqueIDListingOfAllMessages();

		} else {

			// Command contains Arguements

			if (db.getAccessStatusOfMessage(messageNumber)) {

				return "+OK "
						+ db.getUniqueIDListingOfMessage(messageNumber);

			} else {
				return "-ERR Message not found or has been marked for deletion or invalid arguement";
			}
		}
	}

	/*
	 * Checks that each arguement does not exceed a character count of 40
	 * Returns True, if none of the arguements exceeds the limit. False, if not.
	 */
	private boolean lengthOfArguementsIsWithinLimit(String[] arguementsArray) {
		for (String arguement : arguementsArray) {
			if (arguement.length() > 40)
				return false;
		}
		return true;
	}

	/*
	 * Matches a given command string to a particular POP3CommandEnum If not
	 * possible, it is set to the POP3Command Error Enum
	 */
	private POP3Command setPOP3CommandEnum(String command) {
		try {

			command = command.toUpperCase();
			return POP3Command.valueOf(command);

		} catch (Exception IIlegalArguementException) {

			return POP3Command.ERROR;

		}
	}

	private boolean stringCanBeParsedAsAnInteger(String number) {

		try {
			Integer.parseInt(number);
		} catch (NumberFormatException e) {
			return false;
		}

		return true;
	}
}