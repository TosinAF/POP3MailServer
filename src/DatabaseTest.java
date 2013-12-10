import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DatabaseTest {

	Database db;

	@Before
	public void setUp() {
		db = new Database();
		db.authenticatePassword("alex", "hello123");

		// what if the other methods - explain how it will return false & simply
		// not work
		// creates new connection eact time - is that bad?
		
		//cant easily test get message body with the test data given
		//cant test delete methods easily here
	}
	
	@After
	public void close() {
		db.closeConnection();
	}

	@Test
	public void testDatabase() {
		assertNotNull(db);
	}

	@Test
	public void testAuthenticateUser() {

		// Invalid User
		assertEquals(false, db.authenticateUser("test"));

		// Valid User
		assertEquals(true, db.authenticateUser("alex"));
	}

	@Test
	public void testAuthenticatePassword() {

		// Invalid username & password
		assertEquals(false, db.authenticatePassword("test", "password"));

		// Invalid username
		assertEquals(false, db.authenticatePassword("test", "hello123"));

		// Invalid password
		assertEquals(false, db.authenticatePassword("alex", "qwerty"));

		// Valid username & password
		assertEquals(true, db.authenticatePassword("alex", "hello123"));
	}

	@Test
	public void testGetAccessStatusOfMessage() {

		// Invalid meassage number
		assertEquals(false, db.getAccessStatusOfMessage(4));

		// valid message number
		assertEquals(true, db.getAccessStatusOfMessage(1));
	}

	@Test
	public void testGetNumberOfMailsInMaildrop() {
		assertEquals(3, db.getNumberOfMailsInMaildrop());
		
		db.markMessageForDeletion(1);
		assertEquals(2, db.getNumberOfMailsInMaildrop());
		
		db.unmarkAllMessagesMarkedForDeletion();
	}

	@Test
	public void testGetSizeOfMessageInOctets() {
		assertEquals(1301, db.getSizeOfMessageInOctets(1));
	}

	@Test
	public void testGetSizeOfMaildrop() {
		assertEquals(173293, db.getSizeOfMaildrop());
		
		db.markMessageForDeletion(3);
		assertEquals(4023, db.getSizeOfMaildrop());
		
		db.unmarkAllMessagesMarkedForDeletion();
	}

	@Test
	public void testGetDropListingOfMaildrop() {
		assertEquals("3 173293", db.getDropListingOfMaildrop());
	}

	@Test
	public void testGetScanListingOfMessage() {
		assertEquals("1 1301", db.getScanListingOfMessage(1));
	}

	@Test
	public void testGetScanListingOfAllMessages() {
		assertEquals("1 1301\n2 2722\n3 169270",
				db.getScanListingOfAllMessages());
		
		db.markMessageForDeletion(3);
		assertEquals("1 1301\n2 2722",
				db.getScanListingOfAllMessages());
		
		db.unmarkAllMessagesMarkedForDeletion();
	}

	@Test
	public void testGetUniqueIDListingOfMessage() {
		assertEquals("1 Nj!oogfjA?`f!fhsjXop4J&yby?XW,L0xM:R>y\"]sR@b<Pz+SC",
				db.getUniqueIDListingOfMessage(1));
	}

	@Test
	public void testGetUniqueIDListingOfAllMessages() {
		db.authenticatePassword("claire", "qazwsx");
		String result = "1 }al9Is9S*Cp=^<.#Z]<|dJj}3)+Rn;W+U2E-RZiQ!UEW@6iO?>\n2 [7(7mGn8?wkR5U*2W9MKmvaG6nHT`Rwsl$SQ/XVs_}mR|d.rI<";
		assertEquals(result, db.getUniqueIDListingOfAllMessages());
		
		db.markMessageForDeletion(2);
		result = "1 }al9Is9S*Cp=^<.#Z]<|dJj}3)+Rn;W+U2E-RZiQ!UEW@6iO?>";
		assertEquals(result, db.getUniqueIDListingOfAllMessages());
		
		db.unmarkAllMessagesMarkedForDeletion();
	}

	@Test
	public void testGetNumberOfLinesInMessageBody() {
		assertEquals(45, db.getNumberOfLinesInMessageBody(1));
	}

	@Test
	public void testGetXNumberOfLinesInMessageBody() {
		assertEquals("Received: from pat.cs.nott.ac.uk by robin.Cs.Nott.AC.UK id ac08796;", db.getXNumberOfLinesInMessageBody(1, 1));
	}
}
