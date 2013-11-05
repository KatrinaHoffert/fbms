/*
	FBMS: File Backup and Management System
	Copyright (C) 2013 Group 06

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package cmpt370.fbms.GUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import cmpt370.fbms.Control;
import cmpt370.fbms.Errors;

public class FirstStartWizard
{
	public static JDialog frame;
	public static int currentPanel = 1;
	public static JTextField liveDirectoryField;
	public static JTextField backupDirectoryField;
	public static JButton selectDirsNextButton;
	public static WindowListener listener;

	public static void run()
	{
		Control.logger.info("Started first run wizard");

		// Create the dialog window
		frame = new JDialog();
		frame.setTitle("Welcome to FBMS");
		frame.setSize(new Dimension(450, 250));
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);

		frame.add(introPanel());

		frame.setVisible(true);

		// Event handler to quit the program if the wizard is terminated prematurely. This was going
		// to be an anonymous class, but it must be removed for the final panel, so it's now a
		// not-so-anonymous class
		listener = new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				Control.logger.debug("First run wizard closed in panel "
						+ FirstStartWizard.currentPanel);
				System.exit(0);
			}
		};
		frame.addWindowListener(listener);
	}

	/**
	 * Creates the introduction panel, which summarizes the point of this wizard.
	 * 
	 * @return A panel for display.
	 */
	public static JPanel introPanel()
	{
		// Create panel contents
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("<html>Welcome to the <b>F</b>ile <b>B</b>ackup and "
				+ "<b>M</b>anagement <b>S</b>ystem, or FBMS. Use the next and previous buttons to "
				+ "navigate this wizard.</html>");
		label.setFont(new Font("Sans serif", Font.PLAIN, 14));
		panel.add(label, BorderLayout.NORTH);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Padding

		JPanel buttonPanel = new JPanel(new GridLayout());
		JButton quitButton = new JButton("Quit");
		buttonPanel.add(quitButton);
		JButton prevButton = new JButton("Previous");
		prevButton.setEnabled(false);
		buttonPanel.add(prevButton);
		JButton nextButton = new JButton("Next");
		buttonPanel.add(nextButton);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		// Event handler for next button press
		nextButton.addActionListener(new WizardActionListener(1));

		prevButton.addActionListener(new WizardActionListener(-1));

		// Event handler for quit button press
		quitButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				Control.logger.debug("First run wizard closed in panel "
						+ FirstStartWizard.currentPanel);
				System.exit(0);
			}
		});

		Control.logger.debug("First run wizard introduction panel drawn");

		return panel;
	}

	/**
	 * Creates the panel which asks the user if they want to import an old backup or create
	 * 
	 * @return A panel for display
	 */
	public static JPanel importPanel()
	{
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("<html>FBMS allows you to recover import old backups. "
				+ "Do you want to import an existing backup or do you want to create a new "
				+ "backup project?</html>");
		label.setFont(new Font("Sans serif", Font.PLAIN, 14));
		panel.add(label, BorderLayout.NORTH);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel buttonPanel = new JPanel(new GridLayout());
		JButton quitButton = new JButton("Quit");
		buttonPanel.add(quitButton);
		JButton nextButton = new JButton("Import existing");
		buttonPanel.add(nextButton);
		JButton prevButton = new JButton("Create new");
		buttonPanel.add(prevButton);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		// Event handler for next button press
		nextButton.addActionListener(new WizardActionListener(10));

		prevButton.addActionListener(new WizardActionListener(1));

		// Event handler for quit button press
		quitButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				Control.logger.debug("First run wizard closed in panel "
						+ FirstStartWizard.currentPanel);
				System.exit(0);
			}
		});

		Control.logger.debug("First run wizard import panel drawn");

		return panel;
	}

	/**
	 * Creates a panel for selecting the live and backup directories.
	 * 
	 * @return A panel for display
	 */
	public static JPanel selectDirsPanel()
	{
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("<html>Select a live directory to monitor and a backup "
				+ "directory to place your backed up files in.</html>");
		label.setFont(new Font("Sans serif", Font.PLAIN, 14));
		panel.add(label, BorderLayout.NORTH);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		// Panel for choosing the directories. They're just regular text fields, but will have an
		// event handler for clicking the fields, which will open a folder chooser
		JPanel centerPanel = new JPanel();
		centerPanel.setBorder(new EmptyBorder(25, 0, 0, 0));
		JLabel liveDirectoryLabel = new JLabel("Live directory:");
		liveDirectoryLabel.setPreferredSize(new Dimension(130, 25));
		liveDirectoryField = new JTextField();
		liveDirectoryField.setPreferredSize(new Dimension(250, 25));
		JLabel backupDirectoryLabel = new JLabel("Backup directory:");
		backupDirectoryLabel.setPreferredSize(new Dimension(130, 25));
		backupDirectoryField = new JTextField();
		backupDirectoryField.setPreferredSize(new Dimension(250, 25));
		centerPanel.add(liveDirectoryLabel);
		centerPanel.add(liveDirectoryField);
		centerPanel.add(backupDirectoryLabel);
		centerPanel.add(backupDirectoryField);
		panel.add(centerPanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new GridLayout());
		JButton quitButton = new JButton("Quit");
		buttonPanel.add(quitButton);
		JButton prevButton = new JButton("Previous");
		buttonPanel.add(prevButton);
		selectDirsNextButton = new JButton("Next");
		selectDirsNextButton.setEnabled(false);
		buttonPanel.add(selectDirsNextButton);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		// Event handler for next and previous buttons. Note the global next button, to allow the
		// event handler to modify the button's state
		selectDirsNextButton.addActionListener(new WizardActionListener(1));
		prevButton.addActionListener(new WizardActionListener(-1));

		// Event handler for directory choices
		backupDirectoryField.addMouseListener(new DirectoryListener(true));
		liveDirectoryField.addMouseListener(new DirectoryListener(false));

		// Event handler for quit button press
		quitButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				Control.logger.debug("First run wizard closed in panel "
						+ FirstStartWizard.currentPanel);
				System.exit(0);
			}
		});

		Control.logger.debug("First run wizard directory choice panel drawn");

		return panel;
	}

	/**
	 * Creates a panel for choosing an existing backup directory.
	 * 
	 * @return A panel for display
	 */
	public static JPanel selectOldDirPanel()
	{
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("<html>Specify the directory which held the existing backup"
				+ "(contains a file named \".revisions.db\").</html>");
		label.setFont(new Font("Sans serif", Font.PLAIN, 14));
		panel.add(label, BorderLayout.NORTH);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel centerPanel = new JPanel();
		centerPanel.setBorder(new EmptyBorder(25, 0, 0, 0));
		JLabel backupDirectoryLabel = new JLabel("Backup directory:");
		backupDirectoryLabel.setPreferredSize(new Dimension(130, 25));
		backupDirectoryField = new JTextField();
		backupDirectoryField.setPreferredSize(new Dimension(250, 25));
		centerPanel.add(backupDirectoryLabel);
		centerPanel.add(backupDirectoryField);
		panel.add(centerPanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new GridLayout());
		JButton quitButton = new JButton("Quit");
		buttonPanel.add(quitButton);
		JButton prevButton = new JButton("Previous");
		buttonPanel.add(prevButton);
		selectDirsNextButton = new JButton("Next");
		selectDirsNextButton.setEnabled(false);
		buttonPanel.add(selectDirsNextButton);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		// Event handler for next and previous button
		selectDirsNextButton.addActionListener(new WizardActionListener(-8));
		prevButton.addActionListener(new WizardActionListener(-10));

		// Event handler for directory choices
		backupDirectoryField.addMouseListener(new MouseListener()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				// Create the file chooser for selecting a directory
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				// Return value will be JFileChooser.APPROVE_OPTION iff a folder was chosen. Any
				// other value means the window was closed
				int returnVal = fileChooser.showOpenDialog(null);

				// Checks that the database file exists inside the specified folder
				if(returnVal == JFileChooser.APPROVE_OPTION)
				{
					if(fileChooser.getSelectedFile().toPath().resolve(".revisions.db").toFile().exists())
					{
						FirstStartWizard.backupDirectoryField.setText(fileChooser.getSelectedFile().toString());
						Control.backupDirectory = fileChooser.getSelectedFile().toPath();
						FirstStartWizard.selectDirsNextButton.setEnabled(true);

						Control.logger.debug("First run wizard existing backup folder chosen: "
								+ Control.backupDirectory);
					}
					else
					{
						JOptionPane.showMessageDialog(null,
								"The specified path is not a valid backup folder. The backup folder "
										+ "must contain the \".revisions.db\" file.", "Error",
								JOptionPane.WARNING_MESSAGE);
					}
				}
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{}

			@Override
			public void mousePressed(MouseEvent e)
			{}

			@Override
			public void mouseExited(MouseEvent e)
			{}

			@Override
			public void mouseEntered(MouseEvent e)
			{}
		});

		// Event handler for quit button press
		quitButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				Control.logger.debug("First run wizard closed in panel "
						+ FirstStartWizard.currentPanel);
				System.exit(0);
			}
		});

		Control.logger.debug("First run wizard import existing backup panel drawn");

		return panel;
	}

	/**
	 * The final panel which confirms that the live and backup directories have been set. On
	 * clicking the finish button, the "backup_location" file is created, allowing subsequent runs
	 * to be recognized as, well, subsequent.
	 * 
	 * @return A panel for display
	 */
	public static JPanel finishPanel()
	{
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("<html>Congratulations, the backup and live directories"
				+ " have been set. Your backup is now being run continuously in the"
				+ " background.</html>");
		label.setFont(new Font("Sans serif", Font.PLAIN, 14));
		panel.add(label, BorderLayout.NORTH);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel buttonPanel = new JPanel(new GridLayout());
		JButton quitButton = new JButton("Quit");
		quitButton.setEnabled(false);
		buttonPanel.add(quitButton);
		JButton prevButton = new JButton("Previous");
		prevButton.setEnabled(false);
		buttonPanel.add(prevButton);
		JButton nextButton = new JButton("Finish");
		buttonPanel.add(nextButton);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		// Event handler for next button press
		nextButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				writeBackupFile();
			}
		});

		// Remove the listener that terminates the program if the window is closed, otherwise we
		// risk the user closing the program, thinking it's properly shut down. Instead, we'll add a
		// new listener which does the same thing as if they clicked "Finish"
		frame.removeWindowListener(listener);
		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent event)
			{
				writeBackupFile();
			}
		});

		Control.logger.debug("First run wizard final panel drawn");

		return panel;
	}

	/**
	 * Utility function purely for the purpose of avoiding copying and pasting this code, which is
	 * run either when you click the "finish" button on the last panel or if you close the window at
	 * the last panel. It writes the backup location to the disk
	 */
	private static void writeBackupFile()
	{
		// Write the backup path to the disk
		FileOutputStream out;
		try
		{
			out = new FileOutputStream("backup_location");
			out.write(Control.backupDirectory.toString().getBytes());
			out.close();

			Control.logger.debug("Backup location file created, set to: " + Control.backupDirectory);
		}
		catch(IOException e)
		{
			Errors.fatalError(
					"Could not write backup path to disk. Is the program folder writeable?", e);
		}

		// And end the wizard
		Control.firstRunWizardDone = true;
		frame.dispose();
	}
}

/**
 * Event handler for next and previous buttons. Takes in an offset and applies that to the current
 * panel to figure out which panel needs to be displayed.
 */
class WizardActionListener implements ActionListener
{
	private int offset;

	/**
	 * Just a way to get the direction forward or back a button takes us.
	 * 
	 * @param inOffset
	 *            The panel offset to move in (note that panels aren't entirely linear, since they
	 *            must branch based on whether the user wants to import an existing backup or not
	 */
	public WizardActionListener(int inOffset)
	{
		offset = inOffset;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(FirstStartWizard.currentPanel + offset == 1)
		{
			// We need to set the content pane with the new panel, then *validate* so it renders
			// correctly
			FirstStartWizard.frame.setContentPane(FirstStartWizard.introPanel());
			FirstStartWizard.frame.validate();
			FirstStartWizard.currentPanel = 1;
		}
		else if(FirstStartWizard.currentPanel + offset == 2)
		{
			FirstStartWizard.frame.setContentPane(FirstStartWizard.importPanel());
			FirstStartWizard.frame.validate();
			FirstStartWizard.currentPanel = 2;
		}
		else if(FirstStartWizard.currentPanel + offset == 3)
		{
			FirstStartWizard.frame.setContentPane(FirstStartWizard.selectDirsPanel());
			FirstStartWizard.frame.validate();
			FirstStartWizard.currentPanel = 3;
		}
		else if(FirstStartWizard.currentPanel + offset == 4)
		{
			FirstStartWizard.frame.setContentPane(FirstStartWizard.finishPanel());
			FirstStartWizard.frame.validate();
			FirstStartWizard.currentPanel = 4;
		}
		else if(FirstStartWizard.currentPanel + offset == 12)
		{
			FirstStartWizard.frame.setContentPane(FirstStartWizard.selectOldDirPanel());
			FirstStartWizard.frame.validate();
			FirstStartWizard.currentPanel = 12;
		}

		// We're on the folder chooser panel and going back, so we remove any set directories. This
		// is necessary because if we come back to this panel and the directories haven't been
		// reset, they will still be considered when evaluating if one folder is inside another
		if(FirstStartWizard.currentPanel == 3 && FirstStartWizard.currentPanel + offset < 3)
		{
			Control.backupDirectory = null;
			Control.liveDirectory = null;
		}

		Control.logger.debug("Moved to panel number " + FirstStartWizard.currentPanel);
	}
}

/**
 * Event handler for clicks inside the folder choosers.
 */
class DirectoryListener implements MouseListener
{
	// True = backup directory, false = live directory
	private boolean backup;

	/**
	 * The functionality of the event handler depends on the folder we're listening on
	 * 
	 * @param type
	 *            True if we're specifying the backup directory, false for the live directory
	 */
	public DirectoryListener(boolean type)
	{
		backup = type;
	}

	@Override
	public void mouseClicked(MouseEvent arg0)
	{
		// We're selecting the backup folder
		if(backup)
		{
			// Show a file chooser for folders only
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = fileChooser.showOpenDialog(null);

			if(returnVal == JFileChooser.APPROVE_OPTION && fileChooser.getSelectedFile().exists())
			{
				FirstStartWizard.backupDirectoryField.setText(fileChooser.getSelectedFile().toString());
				Control.backupDirectory = fileChooser.getSelectedFile().toPath();

				Control.logger.info("First run wizard backup folder chosen: "
						+ Control.backupDirectory);
			}
		}
		// Otherwise we're choosing the live directory
		else
		{
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = fileChooser.showOpenDialog(null);

			if(returnVal == JFileChooser.APPROVE_OPTION && fileChooser.getSelectedFile().exists())
			{
				FirstStartWizard.liveDirectoryField.setText(fileChooser.getSelectedFile().toString());
				Control.liveDirectory = fileChooser.getSelectedFile().toPath();

				Control.logger.info("First run wizard live folder chosen: " + Control.liveDirectory);
			}
		}

		try
		{
			// We need to ensure that neither directory is inside the other. So if both directories
			// are set, make sure that they aren't parent directories of one or the other
			if(Control.liveDirectory != null && Control.backupDirectory != null)
			{
				if(Control.liveDirectory.toFile().getCanonicalPath().startsWith(
						Control.backupDirectory.toFile().getCanonicalPath().toString())
						|| Control.backupDirectory.toFile().getCanonicalPath().startsWith(
								Control.liveDirectory.toFile().getCanonicalPath().toString()))
				{
					// In the event of one of the directories being inside the other, we need to
					// remove the directory that we just chose (although the user can manually
					// change the other, if they wish)
					if(backup)
					{
						FirstStartWizard.backupDirectoryField.setText("");
						Control.backupDirectory = null;
						JOptionPane.showMessageDialog(
								null,
								"The backup directory cannot be a child of the live directory and vice versa.",
								"Error", JOptionPane.WARNING_MESSAGE);

						Control.logger.debug("Chosen backup directory is a child or parent of live directory");
					}
					else
					{
						FirstStartWizard.liveDirectoryField.setText("");
						Control.liveDirectory = null;
						JOptionPane.showMessageDialog(
								null,
								"The live directory cannot be a child of the backup directory and vice versa.",
								"Error", JOptionPane.WARNING_MESSAGE);

						Control.logger.debug("Chosen live directory is a child or parent of backup directory");
					}
				}
				// Otherwise paths are valid: Enable next button
				else
				{
					FirstStartWizard.selectDirsNextButton.setEnabled(true);

					Control.logger.debug("Chosen directories are valid");
				}
			}
		}
		catch(IOException e)
		{
			Errors.fatalError("Could not access specified directories", e);
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0)
	{}

	@Override
	public void mousePressed(MouseEvent arg0)
	{}

	@Override
	public void mouseExited(MouseEvent arg0)
	{}

	@Override
	public void mouseEntered(MouseEvent arg0)
	{}
}
