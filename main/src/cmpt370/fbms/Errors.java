package cmpt370.fbms;

import javax.swing.JOptionPane;

public class Errors
{
	/**
	 * Used for alerting the user to fatal errors. Should *only* be used for errors that cannot be
	 * recovered from and require the program to terminate. Will log the error (along with the stack
	 * trace) as well as prompt the user.
	 * 
	 * @param message
	 *            The error message to print
	 * @param error
	 *            An exception object
	 */
	public static void fatalError(String message, Throwable error)
	{
		Control.logger.fatal(message, error);

		// Convert stack trace into a string
		StackTraceElement stackTrace[] = error.getStackTrace();
		String stackTraceContent = "See log file.\n\nStack trace:\n";
		for(StackTraceElement line : stackTrace)
		{
			stackTraceContent += line + "\n";
		}

		JOptionPane.showMessageDialog(null, message + "\n\n" + stackTraceContent, "Fatal error",
				JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}

	/**
	 * Works the same way as fatalError(String, Throwable), but only requires a message.
	 * 
	 * @param message
	 *            The error message to print
	 */
	public static void fatalError(String message)
	{
		Control.logger.fatal(message);

		JOptionPane.showMessageDialog(null, message, "Fatal error", JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}
}
