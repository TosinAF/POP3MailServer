import java.sql.SQLException;

/**
 * The Interface IDatabase.
 * 
 * @author Tosin Afolabi
 * 
 *         Interface for the Database class.
 */

public interface IDatabase {

	/**
	 * Determines if a user exists
	 * 
	 * @param username
	 *            the message number
	 * @return boolean true if user exists, false if not
	 */
	public boolean authenticateUser(String username);

	/**
	 * Authenticates a User
	 * 
	 * @param username
	 * @param password
	 * @return boolean true if authentication succceds, flase if not
	 */
	public boolean authenticatePassword(String username, String password);

	/**
	 * Returns the access status of a particular message
	 * 
	 * @param messageNumber
	 *            the message number
	 * @return boolean, true if message exists or is not marked for deletion
	 *         false if neither condition is true
	 * 
	 *         It is to be called before any method that needs to access a
	 *         message is called, this helps to avoid exceptions & also ensures
	 *         that we never access a message that has been marked for deletion
	 */
	public boolean getAccessStatusOfMessage(int messageNumber);

	/**
	 * Gets the Number of Mails In Mailbox. Does not count mail marked as
	 * deleted Can only be run once a user has been authenticated
	 * 
	 * @return the number of mails as an integer
	 */
	public int getNumberOfMailsInMaildrop();

	/**
	 * Gets the Mailbox Size in Octets. Does not count mail marked as deleted
	 * Can only be run once a user has been authenticated
	 * 
	 * @return the number of mails as an integer
	 */
	public int getSizeOfMessageInOctets(int messageNumber);

	/**
	 * Gets the Mailbox Size in Octets. Does not count mail marked as deleted
	 * Can only be run once a user has been authenticated
	 * 
	 * @return the number of mails as an integer
	 */
	public int getSizeOfMaildrop();

	/**
	 * Gets the drop listing of the maildrop
	 * 
	 * @return two comma seperated numbers -> x y x - number of messages in
	 *         maildrop y - exact size of maildrop in octets
	 */
	public String getDropListingOfMaildrop();

	/**
	 * Gets the scan listing of a particular message
	 * 
	 * @param messageNumber
	 *            the message number
	 * @return two comma seperated numbers -> x y x - message number y - exact
	 *         size of message in octets
	 */
	public String getScanListingOfMessage(int messageNumber);

	/**
	 * Gets the scan listing of all messages
	 * 
	 * @return string contiaining the scan listing of each message followed by a
	 *         'newline' character
	 */
	public String getScanListingOfAllMessages();

	/**
	 * Gets the unique-id listing of a particular message
	 * 
	 * @param messageNumber
	 *            the message number
	 * @return two comma seperated numbers -> x y x - message number y -
	 *         uniquie-id
	 */
	public String getUniqueIDListingOfMessage(int messageNumber);

	/**
	 * Gets the Unique-ID Listing of all messages
	 * 
	 * @return string contiaining the unique-id listing of each message followed
	 *         by a 'newline' character
	 */
	public String getUniqueIDListingOfAllMessages();

	/**
	 * Returns the Number Of Lines In The Message Body
	 * 
	 * @param messageNumber
	 *            the message number
	 * @return integer representing the number of lines in the message
	 */
	public int getNumberOfLinesInMessageBody(int messageNumber);

	/**
	 * Returns the message body of a particular message
	 * 
	 * @return string contiaining the unique-id listing of each message followed
	 *         by a 'newline' character
	 */
	public String getMessageBody(int messageNumber);

	/**
	 * Returns the message body of a particular message up to a specified number
	 * of lines
	 * 
	 * @param messageNumber
	 *            the message number
	 * @param numberOfLines
	 *            the number of lines of the message body that should be
	 *            returned
	 * 
	 * @return string contiaining the unique-id listing of each message followed
	 *         by a 'newline' character If numberOfLines exceeds the number of
	 *         lines of the message body, the whole message body is returned
	 */
	public String getXNumberOfLinesInMessageBody(int messageNumber,
			int numberOfLines);

	/**
	 * Marks a particlaur message for deletion
	 */
	public void markMessageForDeletion(int messageNumber);

	/**
	 * Umarks all messages that were marked for deletion
	 */
	public void unmarkAllMessagesMarkedForDeletion();

	/**
	 * Deletes all messages that were marked for deletion
	 * 
	 * @return true, if all the messages were deleted successfully false, if not
	 */
	public boolean deleteAllMessagesMarkedforDeletion();
	
	/**
	 * Closes the connection to the database
	 */
	public void closeConnection();
}
