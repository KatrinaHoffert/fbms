/*
 * FBMS: File Backup and Management System Copyright (C) 2013 Group 06
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package cmpt370.fbms;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * A utility class for reporting and logging of errors that the user should know about.
 */
public class Errors
{
	private static boolean errorBeingDisplayed = false;

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
		Main.logger.fatal(message, error);

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
		Main.logger.fatal(message);

		JOptionPane.showMessageDialog(null, message, "Fatal error", JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}

	/**
	 * Displays a non-obtrusive notification at the bottom corner of the screen. For use with
	 * recoverable errors. Also logs the message.
	 * 
	 * @param message
	 *            The message to display
	 * @param header
	 *            The header (title) of the notification
	 * @param error
	 *            An exception object
	 */
	public static void nonfatalError(String message, String header, Throwable error)
	{
		DbManager db = DbManager.getInstance();
		String disableNonFatalErrors = db.getConfig("disableNonFatalErrors");
		if(disableNonFatalErrors == null)
		{
			disableNonFatalErrors = "false";
		}

		// Never more than one at a time
		if(!errorBeingDisplayed && disableNonFatalErrors.equals("false"))
		{
			errorBeingDisplayed = true;

			if(error != null)
			{
				Main.logger.error(message, error);
			}
			else
			{
				Main.logger.error(message);
			}

			// The frame
			final JDialog frame = new JDialog();
			frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			frame.setSize(300, 100);
			frame.setUndecorated(true);

			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory.createLineBorder(Color.black));
			panel.setLayout(new GridBagLayout());
			GridBagConstraints constraints = new GridBagConstraints();

			// The heading
			JLabel headingLabel = new JLabel("FBMS: " + header);
			headingLabel.setFont(new Font("Sans Serif", Font.BOLD, 18));
			headingLabel.setOpaque(false);

			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.weightx = 1;
			constraints.weighty = -1;
			constraints.insets = new Insets(5, 5, 5, 5);
			constraints.fill = GridBagConstraints.BOTH;
			panel.add(headingLabel, constraints);

			// The close button
			// Make the button close the window on click
			JButton closeButton = new JButton(new AbstractAction("X")
			{
				// So Eclipse will shut the hell up
				private static final long serialVersionUID = -3379092798847301811L;

				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					frame.dispose();
				}
			});

			closeButton.setMargin(new Insets(1, 4, 1, 4));
			closeButton.setFocusable(false);
			closeButton.setContentAreaFilled(false);
			closeButton.setBorderPainted(false);

			constraints.gridx = 1;
			constraints.weightx = 0;
			constraints.weighty = 0;
			constraints.fill = GridBagConstraints.NONE;
			constraints.anchor = GridBagConstraints.NORTH;
			panel.add(closeButton, constraints);

			// The message
			JLabel messageLabel = new JLabel("<html>" + message);
			messageLabel.setFont(new Font("Sans Serif", Font.PLAIN, 12));
			constraints.gridx = 0;
			constraints.gridy = 1;
			constraints.weightx = 1;
			constraints.weighty = 1;
			constraints.insets = new Insets(5, 5, 5, 5);
			constraints.fill = GridBagConstraints.HORIZONTAL;
			panel.add(messageLabel, constraints);

			// Get dimension of screen and tool bar(s)
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			Insets toolBarOffset = Toolkit.getDefaultToolkit().getScreenInsets(
					frame.getGraphicsConfiguration());

			// Offset from the left is the width of the screen minus the size of the frame minus and
			// any
			// toolbars (so it works with, say, a taskbar that is on the right side of the screen)
			// Offset from the top is similar
			frame.setLocation(screenSize.width - frame.getWidth() - toolBarOffset.right,
					screenSize.height - toolBarOffset.bottom - frame.getHeight());

			frame.add(panel);
			frame.setAlwaysOnTop(true);
			frame.setVisible(true);

			// Use a thread to make the frame disappear after a period of time
			new Thread()
			{
				@Override
				public void run()
				{
					try
					{
						Thread.sleep(7500);
						frame.dispose();
					}
					catch(InterruptedException e)
					{
						Errors.fatalError("Thread was interupted", e);
					}
				};
			}.start();

			errorBeingDisplayed = false;
		}
	}

	/**
	 * Displays a non-obtrusive notification at the bottom corner of the screen. For use with
	 * recoverable errors. Also logs the message.
	 * 
	 * @param message
	 *            The message to display
	 * @param error
	 *            An exception object
	 */
	public static void nonfatalError(String message, Throwable error)
	{
		nonfatalError(message, "Error", error);
	}

	/**
	 * Displays a non-obtrusive notification at the bottom corner of the screen. For use with
	 * recoverable errors. Also logs the message.
	 * 
	 * @param message
	 *            The message to display
	 * @param header
	 *            The header (title) of the notification
	 */
	public static void nonfatalError(String message, String header)
	{
		nonfatalError(message, header, null);
	}

	/**
	 * Displays a non-obtrusive notification at the bottom corner of the screen. For use with
	 * recoverable errors. Also logs the message.
	 * 
	 * @param message
	 *            The message to display
	 */
	public static void nonfatalError(String message)
	{
		nonfatalError(message, "Error", null);
	}
}
