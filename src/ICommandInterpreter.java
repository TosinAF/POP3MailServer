/**
 * The Interface ICommandInterpreter.
 *
 * @author Tosin Afolabi
 * 
 * Interface for the Command Interpreter class.
 */

public interface ICommandInterpreter {
	
	/**
	 * Interprets commands issued by the client
	 * then calls the appropriate database methods
	 *
	 * @param input contains the command keyword & arguements
	 * @return string the server's response to the command
	 */
	public String handleInput(String input);
	
}
