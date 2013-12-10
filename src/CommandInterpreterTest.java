import org.junit.*;
import static org.junit.Assert.*;

/**
 * The Test Class CommandInterpreterTest.
 * 
 * @author Tosin Afolabi
 * 
 *         Functional Regressive Tests for the Command Interpreter(CI) Class The
 *         Main Purpose of the (CI) is to accurately parse the input given by
 *         users And Respond Accordingly.
 * 
 *         Thus as long as the commands are interpreted correctly & the
 *         appropriate response is given we can be confident that the
 *         application is working as intended even though the code coverage may
 *         not be at 100%
 */

public class CommandInterpreterTest {

	CommandInterpreter ci;

	@Before
	public void setUp() {
		ci = new CommandInterpreter();
	}

	@Test
	public void testInterpreterStateAuthorization() {

		// Test using lower case commands are recognized aswell
		assertEquals("-ERR Syntax -> USER mailboxName(required)",
				ci.handleInput("user"));

		// Missing Arguements
		assertEquals("-ERR Syntax -> USER mailboxName(required)",
				ci.handleInput("USER"));
		assertEquals("-ERR Syntax -> PASS password(required)",
				ci.handleInput("PASS "));

		// Unkown User
		assertEquals("-ERR never heard of mailbox name",
				ci.handleInput("USER tosin"));
		assertEquals(
				"-ERR A successful USER Command needs to be run immediately before",
				ci.handleInput("PASS opebi"));

		// Known User, Wrong Password
		assertEquals("+OK name is a valid mailbox",
				ci.handleInput("USER test"));
		assertEquals("-ERR Invalid Password",
				ci.handleInput("PASS wrongPassword"));

		// Unsupported Command
		assertEquals("-ERR Unsupported Command Given",
				ci.handleInput("APOP test hbfdhjbjhfbjhdfjfh"));

		// Quit Command in Authorization State
		assertEquals("+OK POP3 Server Connection Terminated",
				ci.handleInput("QUIT"));

		// User & Password Authenticated
		assertEquals("+OK name is a valid mailbox",
				ci.handleInput("USER test"));
		assertEquals("+OK maildrop locked & ready",
				ci.handleInput("PASS password"));

	}

	@Test
	public void testInterpreterStateAuthorization2() {

		// None of these commands are allowed in the authorization stage
		assertEquals("-ERR User needs to be authenticated",
				ci.handleInput("STAT"));
		assertEquals("-ERR User needs to be authenticated",
				ci.handleInput("LIST 1"));
		assertEquals("-ERR User needs to be authenticated",
				ci.handleInput("RETR 1"));
		assertEquals("-ERR User needs to be authenticated",
				ci.handleInput("DELE 1"));
		assertEquals("-ERR User needs to be authenticated",
				ci.handleInput("NOOP"));
		assertEquals("-ERR User needs to be authenticated",
				ci.handleInput("RSET"));
		assertEquals("-ERR User needs to be authenticated",
				ci.handleInput("TOP 1 5"));
		assertEquals("-ERR User needs to be authenticated",
				ci.handleInput("UIDL 1"));
	}

	@Test
	public void testCommandsInTransactionState() {
		ci.handleInput("USER test");
		ci.handleInput("PASS password");

		// Command with an arugement longer than 40 characters
		assertEquals(
				"-ERR One of the arguements in the command is longer than 40 characters",
				ci.handleInput("USER testtesttesttesttesttesttesttesttesttesttesttest"));

		// USER Command
		assertEquals("-ERR maildrop already locked",
				ci.handleInput("USER test"));

		// PASS Command
		assertEquals("-ERR maildrop already locked",
				ci.handleInput("pass password"));

		// STAT Command
		assertEquals("+OK 0 0", ci.handleInput("STAT"));
		assertEquals("-ERR This command has no arguements",
				ci.handleInput("STAT 1"));

		// LIST Command
		assertEquals("+OK scan listing follows\n",
				ci.handleInput("LIST"));

		// LIST Command With Arguements
		assertEquals(
				"-ERR Message not found or has been marked for deletion",
				ci.handleInput("LIST 1"));
		assertEquals("-ERR Syntax -> LIST messageNumber(optional)",
				ci.handleInput("LIST 1 2"));

		// Invalid Arguement Type
		assertEquals("-Err Invalid Arguement Type",
				ci.handleInput("LIST a"));

		// RETR Command
		assertEquals("-ERR Syntax -> RETR messageNumber(required)",
				ci.handleInput("RETR"));

		// RETR Command With Arguement
		assertEquals(
				"-ERR Message not found or has been marked for deletion or invalid arguement",
				ci.handleInput("RETR 1"));
		assertEquals("-ERR Syntax -> RETR messageNumber(required)",
				ci.handleInput("RETR 1 2"));

		// DELE Command
		assertEquals("-ERR Syntax -> DELE messageNumber(required)",
				ci.handleInput("DELE"));

		// DELE Command With Arguement
		assertEquals(
				"-ERR Message not found or has already been marked for deletion",
				ci.handleInput("DELE 1"));

		// RSET Command
		assertEquals("+OK maildrop has 0 (0) octets",
				ci.handleInput("RSET"));

		// RSET Command With Arguement - It should ignore the arguement
		assertEquals("-ERR This command has no arguements",
				ci.handleInput("RSET 1"));

		// NOOP Command
		assertEquals("+OK", ci.handleInput("NOOP"));
		assertEquals("-ERR This command has no arguements",
				ci.handleInput("NOOP 5"));

		// TOP Command With No Arguements
		assertEquals(
				"-ERR Syntax -> TOP messageNumber, numberOfLines(non negative) (both required)",
				ci.handleInput("TOP"));

		// TOP Command
		assertEquals(
				"-ERR Syntax -> TOP messageNumber, numberOfLines(non negative) (both required)",
				ci.handleInput("TOP 1"));

		// TOP Command With Arguements
		assertEquals(
				"-ERR Message not found or has been marked for deletion",
				ci.handleInput("TOP 1 5"));

		// TOP Command With Last Arguement as a negative number
		assertEquals(
				"-ERR Non-Negative Number must be given with this command",
				ci.handleInput("TOP 1 -5"));

		// UIDL Command With No Arguements
		assertEquals("+OK Uniquie ID listing follows\n",
				ci.handleInput("UIDL"));

		// UIDL Command With Arguement
		assertEquals(
				"-ERR Message not found or has been marked for deletion or invalid arguement",
				ci.handleInput("UIDL 1"));
		assertEquals("-ERR Syntax -> UIDL messageNumber(optional)",
				ci.handleInput("UIDL 1 3"));

		// Unsupported Command
		assertEquals("-ERR Unsupported Command Given",
				ci.handleInput("TOPP 1 -5"));

		// Quit Command in Transaction State
		// Mock Database returns false
		assertEquals("-ERR This command has no arguements",
				ci.handleInput("QUIT 1 2"));
		assertEquals(
				"-ERR Some messages marked for deletion were not removed",
				ci.handleInput("QUIT"));
	}

}
