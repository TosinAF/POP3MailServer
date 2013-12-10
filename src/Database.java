import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;
import java.io.UnsupportedEncodingException;

public class Database implements IDatabase {
	
	/*
	private final static String DB_USERNAME = "root";
	private final static String DB_PASSWORD = "root";

	private final static String DB_PORT = "8889";
	private final static String DB_TYPE = "mysql";
	private final static String DB_NAME = "apr_db";
	private final static String DB_SERVER = "localhost";
	private final static String DB_DRIVER = "com.mysql.jdbc.Driver";

	private final static String DB_ENCODING = "useUnicode=true&characterEncoding=UTF8";
	*/

	private final static String DB_USERNAME = "ooa02u"; 
	private final static String DB_PASSWORD = "t5y6u7";
	
	private final static String DB_PORT = "3306"; 
	private final static String DB_TYPE = "mysql"; 
	private final static String DB_NAME = "ooa02u";
	private final static String DB_SERVER = "mysql.cs.nott.ac.uk"; 
	private final static String DB_DRIVER = "com.mysql.jdbc.Driver"; 
	
	private final static String DB_ENCODING = "useUnicode=true&characterEncoding=UTF8";
	 
	private Connection conn;
	private boolean databaseStarted;

	// Stores the userID that the maildrop has been locked to
	private int seissionUserID;

	public Database() {

		databaseStarted = false;

		try {
			conn = getConnection();
			conn.setAutoCommit(false);
			databaseStarted = true;
		} catch (SQLException e) {
			System.out.println("Database connection could not be started");
			printSQLException(e);
		} catch (ClassNotFoundException e) {
			System.err.println("Error loading driver: " + e);
		}

		seissionUserID = -1;

		// possibly use finnally in logs to show error ouccured?
		// dont handle makekrd for dleeted in individual methods about a message
		// as access status should be called first
	}

	private Connection getConnection() throws SQLException,
			ClassNotFoundException {

		Properties connectionProperties = new Properties();
		connectionProperties.put("user", DB_USERNAME);
		connectionProperties.put("password", DB_PASSWORD);

		Class.forName(DB_DRIVER);

		System.out.println("Connecting to a selected database");
		Connection conn = DriverManager.getConnection(
				"jdbc:" + DB_TYPE + "://" + DB_SERVER + ":" + DB_PORT + "/"
						+ DB_NAME + "?" + DB_ENCODING, connectionProperties);
		System.out.println("Connected to database successfully");

		return conn;
	}
	
	public boolean wasConnectionStarted() {
		return databaseStarted;
	}

	public boolean isConnectionValid() {
		try {
			return conn.isValid(0);
		} catch (SQLException e) {
			printSQLException(e);
			return false;
		}
	}

	public boolean authenticateUser(String username) {

		String query = "SELECT vchUsername FROM m_Maildrop WHERE tiLocked=0;";

		try (Statement statement = conn.createStatement()) {

			ResultSet rs = statement.executeQuery(query);

			while (rs.next()) {
				if (username.equals(rs.getString("vchUsername"))) {
					return true;
				}
			}

		} catch (SQLException e) {
			printSQLException(e);
		}

		return false;
	}

	public boolean authenticatePassword(String username, String password) {

		String query = "SELECT iMaildropID, vchUsername, vchPassword FROM m_Maildrop WHERE tiLocked=0;";

		try (Statement statement = conn.createStatement()) {

			ResultSet rs = statement.executeQuery(query);

			while (rs.next()) {
				if (username.equals(rs.getString("vchUsername"))) {

					if (password.equals(rs.getString("vchPassword"))) {

						seissionUserID = rs.getInt("iMaildropID");

						String setMaildropToLocked = "UPDATE m_Maildrop SET tiLocked=1 WHERE iMaildropID="
								+ seissionUserID + ";";

						statement.executeUpdate(setMaildropToLocked);
						conn.commit();
						return true;

					} else {
						return false;
					}
				}
			}

		} catch (SQLException e) {
			printSQLException(e);
		}

		return false;
	}
	
	@Override
	public int getNumberOfMailsInMaildrop() {

		int numberOfMails = 0;
		String query = "SELECT iMailID FROM m_Mail WHERE iMaildropID="
				+ seissionUserID;

		try (Statement statement = conn.createStatement()) {

			ResultSet rs = statement.executeQuery(query);
			numberOfMails = getRowCount(rs);

		} catch (SQLException e) {
			printSQLException(e);
		}

		return numberOfMails;
	}

	@Override
	public boolean getAccessStatusOfMessage(int messageNumber) {
		
		int numberOfMessages = getNumberOfMailsInMaildrop();
		
		if (messageNumber > numberOfMessages || messageNumber == 0)
			return false;
		else
			return true;

	}

	@Override
	public int getSizeOfMessageInOctets(int messageNumber) {

		int sizeOfMessageInOctets = 0;
		String query = "SELECT txMailContent FROM m_Mail WHERE iMaildropID="
				+ seissionUserID + ";";

		try (Statement statement = conn.createStatement()) {

			ResultSet rs = statement.executeQuery(query);
			rs.absolute(messageNumber);
			sizeOfMessageInOctets = getNumberOfOctetsInString(rs
					.getString("txMailContent"));

		} catch (SQLException e) {
			printSQLException(e);
		}

		return sizeOfMessageInOctets;
	}

	@Override
	public int getSizeOfMaildrop() {

		int totalSizeOfMaildrop = 0;
		String query = "SELECT txMailContent FROM m_Mail WHERE iMaildropID="
				+ seissionUserID;

		try (Statement statement = conn.createStatement()) {

			ResultSet rs = statement.executeQuery(query);

			while (rs.next()) {

				totalSizeOfMaildrop += getNumberOfOctetsInString(rs
						.getString("txMailContent"));
			}

		} catch (SQLException e) {
			printSQLException(e);
		}

		return totalSizeOfMaildrop;
	}

	@Override
	public String getDropListingOfMaildrop() {
		return getNumberOfMailsInMaildrop() + " " + getSizeOfMaildrop();
	}

	@Override
	public String getScanListingOfMessage(int messageNumber) {
		return messageNumber + " " + getSizeOfMessageInOctets(messageNumber);
	}

	@Override
	public String getScanListingOfAllMessages() {
		
		StringBuilder scanListing = new StringBuilder();
		String query = "SELECT txMailContent FROM m_Mail WHERE iMaildropID="
				+ seissionUserID;

		try (Statement statement = conn.createStatement()) {

			ResultSet rs = statement.executeQuery(query);

			int numberOfMails = getNumberOfMailsInMaildrop();

			while (rs.next()) {
				
				int messageNumber = rs.getRow();
				int sizeInOctets = getNumberOfOctetsInString(rs
						.getString("txMailContent"));

				if (messageNumber == numberOfMails) {
					// For Nice Formatting
					scanListing.append(messageNumber + " "
							+ sizeInOctets);
				} else {
					scanListing.append(messageNumber + " "
							+ sizeInOctets + "\n");
				}

			}

		} catch (SQLException e) {
			printSQLException(e);
		}

		return scanListing.toString();
	}

	@Override
	public String getUniqueIDListingOfMessage(int messageNumber) {

		String uidl = "";
		String query = "SELECT vchUIDL FROM m_Mail WHERE iMaildropID="
				+ seissionUserID + ";";

		try (Statement statement = conn.createStatement()) {

			ResultSet rs = statement.executeQuery(query);
			rs.absolute(messageNumber);
			uidl = rs.getString("vchUIDL");

		} catch (SQLException e) {
			printSQLException(e);
		}

		return messageNumber + " " + uidl;
	}

	@Override
	public String getUniqueIDListingOfAllMessages() {

		StringBuilder uidlListing = new StringBuilder();
		String query = "SELECT iMailID, vchUIDL FROM m_Mail WHERE iMaildropID="
				+ seissionUserID;

		try (Statement statement = conn.createStatement()) {

			ResultSet rs = statement.executeQuery(query);

			int numberOfMails = getNumberOfMailsInMaildrop();


			while (rs.next()) {

				int messageNumber = rs.getRow();
				String uidl = rs.getString("vchUIDL");

				if (messageNumber == numberOfMails) {
					// For Nice Formatting
					uidlListing.append(messageNumber + " " + uidl);
				} else {
					uidlListing
							.append(messageNumber + " " + uidl + "\n");
				}

			}

		} catch (SQLException e) {
			printSQLException(e);
		}

		return uidlListing.toString();
	}

	@Override
	public int getNumberOfLinesInMessageBody(int messageNumber) {

		int numberOfLines = 0;
		String query = "SELECT txMailContent FROM m_Mail WHERE iMaildropID="
				+ seissionUserID + ";";

		try (Statement statement = conn.createStatement()) {

			ResultSet rs = statement.executeQuery(query);
			rs.absolute(messageNumber);
			String messageBody = rs.getString("txMailContent");
			numberOfLines = messageBody.split("\\n").length;

		} catch (SQLException e) {
			printSQLException(e);
		}

		return numberOfLines;
	}

	@Override
	public String getMessageBody(int messageNumber) {

		String messageBody = "";
		String query = "SELECT txMailContent FROM m_Mail WHERE iMaildropID="
				+ seissionUserID + ";";

		try (Statement statement = conn.createStatement()) {

			ResultSet rs = statement.executeQuery(query);
			rs.absolute(messageNumber);
			messageBody = rs.getString("txMailContent");

		} catch (SQLException e) {
			printSQLException(e);
		}

		return messageBody;
	}

	@Override
	public String getXNumberOfLinesInMessageBody(int messageNumber,
			int numberOfLines) {

		StringBuilder message = new StringBuilder();

		if (numberOfLines > getNumberOfLinesInMessageBody(messageNumber)) {
			return getMessageBody(messageNumber);

		} else {

			String[] messageBodyArray = getMessageBody(messageNumber).split(
					"\\n");

			for (int i = 0; i < numberOfLines; i++) {
				if (i == numberOfLines - 1) {
					message.append(messageBodyArray[i]);
				} else {
					message.append(messageBodyArray[i] + "\n");
				}
			}
		}
		return message.toString();
	}

	@Override
	public void markMessageForDeletion(int messageNumber) {
		
		String mailIDQuery = "SELECT iMailID FROM m_Mail WHERE iMaildropID="
				+ seissionUserID + ";";

		try (Statement statement = conn.createStatement()) {

			ResultSet rs = statement.executeQuery(mailIDQuery);
			rs.absolute(messageNumber);
			
			String deleteQuery = "DELETE FROM m_MAil WHERE iMaildropID=" + seissionUserID
					+ " AND " + "iMAilID=" + rs.getInt("iMailID") + ";";
			statement.executeUpdate(deleteQuery);

		} catch (SQLException e) {
			rollbackCommit();
			printSQLException(e);
		}
	}

	@Override
	public void unmarkAllMessagesMarkedForDeletion() {
		rollbackCommit();
	}

	@Override
	public boolean deleteAllMessagesMarkedforDeletion() {

		try {

			conn.commit();
			unlockMaildrop();

		} catch (SQLException e) {
			printSQLException(e);
			rollbackCommit();
			closeConnection();
			return false;
		}

		closeConnection();
		return true;
	}

	private void unlockMaildrop() {

		try (Statement statement = conn.createStatement()) {

			String unlockMaildrop = "UPDATE m_Maildrop "
					+ "SET tiLocked=0 WHERE iMaildropID=" + seissionUserID
					+ ";";

			statement.executeUpdate(unlockMaildrop);
			conn.commit();

		} catch (SQLException e) {
			printSQLException(e);
		}
	}

	public void closeConnection() {
		try {
			if (conn != null)
				unlockMaildrop();
			conn.close();
		} catch (SQLException e) {
			printSQLException(e);
		}
	}
	
	private int getRowCount(ResultSet rs) {

		int count = 0;

		try {
			rs.absolute(-1);
			count = rs.getRow();
			rs.beforeFirst();
		} catch (SQLException e) {
			printSQLException(e);
		}

		return count;
	}

	private int getNumberOfOctetsInString(String string) {

		int num = 0;

		try {
			final byte[] utf8Bytes = string.getBytes("UTF-8");
			num = utf8Bytes.length;
		} catch (UnsupportedEncodingException e) {
			e.getMessage();
		}

		return num;
	}

	private void rollbackCommit() {
		try {
			conn.rollback();
		} catch (SQLException e) {
			printSQLException(e);
		}
	}

	/**
	 * The following methods were obtained from
	 * http://docs.oracle.com/javase/tutorial/jdbc/basics/sqlexception.html
	 * 
	 * They privide a useful way of handling SQL Exceptions They will produce
	 * clear & readable error meesages to aid debugging
	 * 
	 */
	
	private void printSQLException(SQLException ex) {

		for (Throwable e : ex) {
			if (e instanceof SQLException) {
				if (ignoreSQLException(((SQLException) e).getSQLState()) == false) {

					e.printStackTrace(System.err);
					System.err.println("SQLState: "
							+ ((SQLException) e).getSQLState());

					System.err.println("Error Code: "
							+ ((SQLException) e).getErrorCode());

					System.err.println("Message: " + e.getMessage());

					Throwable t = ex.getCause();
					while (t != null) {
						System.out.println("Cause: " + t);
						t = t.getCause();
					}
				}
			}
		}
	}

	private boolean ignoreSQLException(String sqlState) {

		if (sqlState == null) {
			System.out.println("The SQL state is not defined!");
			return false;
		}

		// X0Y32: Jar file already exists in schema
		if (sqlState.equalsIgnoreCase("X0Y32"))
			return true;

		// 42Y55: Table already exists in schema
		if (sqlState.equalsIgnoreCase("42Y55"))
			return true;

		return false;
	}

}
