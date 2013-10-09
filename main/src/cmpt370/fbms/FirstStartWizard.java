package cmpt370.fbms;

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

public class FirstStartWizard
{
	public static JDialog frame;
	public static int currentPanel = 1;
	public static JTextField liveDirectoryField;
	public static JTextField backupDirectoryField;
	public static JButton selectDirsNextButton;

	public static void run()
	{
		// Create the dialog window
		frame = new JDialog();
		frame.setTitle("Welcome to FBMS");
		frame.setVisible(true);
		frame.setSize(new Dimension(400, 250));
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);

		frame.add(introPanel());

		// Required for text to display properly
		frame.revalidate();

		// Event handler to quit the program if the wizard is terminated prematurely
		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		});
	}

	// The first panel
	public static JPanel introPanel()
	{
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("<html>Welcome to the <b>F</b>ile <b>B</b>ackup and "
				+ "<b>M</b>anagement <b>S</b>ystem, or FBMS. Use the next and previous buttons to "
				+ "navigate this wizard.</html>");
		label.setFont(new Font("Sans serif", Font.PLAIN, 14));
		panel.add(label, BorderLayout.NORTH);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

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
				System.exit(0);
			}
		});

		return panel;
	}

	// The second panel
	public static JPanel importPanel()
	{
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("<html>FBMS allows you to recover import old backups. Do "
				+ "you have an existing backup you wish to import? New users will want to choose "
				+ "\"no\".</html>");
		label.setFont(new Font("Sans serif", Font.PLAIN, 14));
		panel.add(label, BorderLayout.NORTH);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel buttonPanel = new JPanel(new GridLayout());
		JButton quitButton = new JButton("Quit");
		buttonPanel.add(quitButton);
		JButton prevButton = new JButton("No");
		buttonPanel.add(prevButton);
		JButton nextButton = new JButton("Yes");
		buttonPanel.add(nextButton);
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
				System.exit(0);
			}
		});

		return panel;
	}

	// The third panel
	public static JPanel selectDirsPanel()
	{
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("<html>Select a live directory to monitor and a backup "
				+ "directory to place your backed up files in.</html>");
		label.setFont(new Font("Sans serif", Font.PLAIN, 14));
		panel.add(label, BorderLayout.NORTH);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel centerPanel = new JPanel();
		centerPanel.setBorder(new EmptyBorder(25, 0, 0, 0));
		JLabel liveDirectoryLabel = new JLabel("Live directory:");
		liveDirectoryLabel.setPreferredSize(new Dimension(100, 25));
		liveDirectoryField = new JTextField();
		liveDirectoryField.setPreferredSize(new Dimension(250, 25));
		JLabel backupDirectoryLabel = new JLabel("Backup directory:");
		backupDirectoryLabel.setPreferredSize(new Dimension(100, 25));
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

		// Event handler for next button press
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
				System.exit(0);
			}
		});

		return panel;
	}

	// Special case: import old dir
	public static JPanel selectOldDirPanel()
	{
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("<html>Specify the directory which held the backup (contains "
				+ "a file named \".revisions.db\").</html>");
		label.setFont(new Font("Sans serif", Font.PLAIN, 14));
		panel.add(label, BorderLayout.NORTH);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel buttonPanel = new JPanel(new GridLayout());
		JButton quitButton = new JButton("Quit");
		buttonPanel.add(quitButton);
		JButton prevButton = new JButton("Previous");
		buttonPanel.add(prevButton);
		selectDirsNextButton = new JButton("Next");
		selectDirsNextButton.setEnabled(false);
		buttonPanel.add(selectDirsNextButton);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		JPanel centerPanel = new JPanel();
		centerPanel.setBorder(new EmptyBorder(25, 0, 0, 0));
		JLabel backupDirectoryLabel = new JLabel("Backup directory:");
		backupDirectoryLabel.setPreferredSize(new Dimension(100, 25));
		backupDirectoryField = new JTextField();
		backupDirectoryField.setPreferredSize(new Dimension(250, 25));
		centerPanel.add(backupDirectoryLabel);
		centerPanel.add(backupDirectoryField);
		panel.add(centerPanel, BorderLayout.CENTER);

		// Event handler for next button press
		selectDirsNextButton.addActionListener(new WizardActionListener(-8));

		prevButton.addActionListener(new WizardActionListener(-10));

		// Event handler for directory choices
		backupDirectoryField.addMouseListener(new MouseListener()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				int returnVal = fileChooser.showOpenDialog(null);

				// Checks that the database file exists inside the specified folder
				if(returnVal == JFileChooser.APPROVE_OPTION
						&& fileChooser.getSelectedFile().toPath().resolve(".revisions.db").toFile().exists())
				{
					FirstStartWizard.backupDirectoryField.setText(fileChooser.getSelectedFile().toString());
					Control.backupDirectory = fileChooser.getSelectedFile().toPath();
					FirstStartWizard.selectDirsNextButton.setEnabled(true);
				}
				else
				{
					JOptionPane.showMessageDialog(null,
							"The specified path is not a valid backup folder. The backup folder "
									+ "must contain the \".revisions.db\" file.", "Error",
							JOptionPane.WARNING_MESSAGE);
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
				System.exit(0);
			}
		});

		return panel;
	}

	// The last panel
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
				// Write the backup path to the disk
				FileOutputStream out;
				try
				{
					out = new FileOutputStream("backup_location");
					out.write(Control.backupDirectory.toString().getBytes());
					out.close();
				}
				catch(IOException e)
				{
					Control.logger.fatal("Could not write backup path to disk. Is program"
							+ "folder writeable?", e);
				}

				// And end the wizard
				Control.firstRunWizardDone = true;
				frame.dispose();
			}
		});

		return panel;
	}
}

// Event handler for next and previous buttons. Takes in an offset and applies that to the current
// panel to figure out which panel needs to be displayed
class WizardActionListener implements ActionListener
{
	private int offset;

	// Just a way to get the direction forward or back a button takes us
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

		// We're on the folder chooser screen and going back, so remove any set directories
		if(FirstStartWizard.currentPanel == 3 && FirstStartWizard.currentPanel + offset < 3)
		{
			Control.backupDirectory = null;
			Control.liveDirectory = null;
		}
	}
}

// Event handler for clicks inside the folder choosers
class DirectoryListener implements MouseListener
{
	private boolean backup;

	public DirectoryListener(boolean type)
	{
		// True = backup directory, false = live directory
		backup = type;
	}

	@Override
	public void mouseClicked(MouseEvent arg0)
	{
		// We're selecting the backup folder. Show a file chooser for folders only
		if(backup)
		{
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			int returnVal = fileChooser.showOpenDialog(null);
			if(returnVal == JFileChooser.APPROVE_OPTION && fileChooser.getSelectedFile().exists())
			{
				FirstStartWizard.backupDirectoryField.setText(fileChooser.getSelectedFile().toString());
				Control.backupDirectory = fileChooser.getSelectedFile().toPath();
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
			}
		}

		try
		{
			// Skip this if either directory hasn't been set
			if(Control.liveDirectory != null && Control.backupDirectory != null)
			{
				// Ensure that neither the live or backup folder is a child of the other
				if(Control.liveDirectory.toFile().getCanonicalPath().startsWith(
						Control.backupDirectory.toFile().getCanonicalPath().toString())
						|| Control.backupDirectory.toFile().getCanonicalPath().startsWith(
								Control.liveDirectory.toFile().getCanonicalPath().toString()))
				{
					if(backup)
					{
						// Remove the backup directory: the user will have to enter a new one
						FirstStartWizard.backupDirectoryField.setText("");
						Control.backupDirectory = null;
						JOptionPane.showMessageDialog(
								null,
								"The backup directory cannot be a child of the live directory and vice versa.",
								"Error", JOptionPane.WARNING_MESSAGE);
					}
					else
					{
						// Remove the backup directory: the user will have to enter a new one
						FirstStartWizard.liveDirectoryField.setText("");
						Control.liveDirectory = null;
						JOptionPane.showMessageDialog(
								null,
								"The live directory cannot be a child of the backup directory and vice versa.",
								"Error", JOptionPane.WARNING_MESSAGE);
					}
				}
				// Otherwise paths are valid: Enable next button
				else
				{
					FirstStartWizard.selectDirsNextButton.setEnabled(true);
				}
			}
		}
		catch(IOException e)
		{
			Control.logger.error("Could not check paths", e);
		}
	}

	// Not using any of these, just here for the interface
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
