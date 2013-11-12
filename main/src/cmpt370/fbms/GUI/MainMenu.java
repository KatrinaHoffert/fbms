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

package cmpt370.fbms.GUI;

import java.awt.Desktop;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import cmpt370.fbms.Errors;
import cmpt370.fbms.GuiController;
import cmpt370.fbms.Main;

/**
 * This JMenuBar-like class makes up the menu bar of the main frame.
 */
class MainMenu extends JMenuBar
{
	public JMenuItem copyToOption = new JMenuItem("Copy to");
	public JMenuItem revisionsOption = new JMenuItem("View revisions");
	private JMenuItem restoreAllOption = new JMenuItem("Restore all");
	private JMenuItem settingsOption = new JMenuItem("Settings");
	private JMenuItem changeLiveDirOption = new JMenuItem("Change live directory");
	private JMenuItem changeBackupDirOption = new JMenuItem("Change backup directory");
	private JMenuItem exitOption = new JMenuItem("Exit");

	private JMenuItem helpOption = new JMenuItem("Display help");

	public MainMenu()
	{
		// Populate menus
		JMenu fileMenu = new JMenu("File");
		JMenu helpMenu = new JMenu("Help");

		this.add(fileMenu);
		this.add(helpMenu);

		fileMenu.add(copyToOption);
		fileMenu.add(revisionsOption);
		fileMenu.addSeparator();
		fileMenu.add(restoreAllOption);
		fileMenu.addSeparator();
		fileMenu.add(changeLiveDirOption);
		fileMenu.add(changeBackupDirOption);
		fileMenu.add(settingsOption);
		fileMenu.addSeparator();
		fileMenu.add(exitOption);

		helpMenu.add(helpOption);

		// Disable the copy to and view revisions options; they are enabled only when a file is
		// selected
		copyToOption.setEnabled(false);
		revisionsOption.setEnabled(false);

		// Create event handlers
		initFileActions();
		initHelpActions();
	}

	/**
	 * Creates event handlers for the file menu.
	 */
	private void initFileActions()
	{
		copyToOption.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				// Return value will be JFileChooser.APPROVE_OPTION iff a folder was chosen. Any
				// other value means the window was closed
				int returnVal = fileChooser.showOpenDialog(null);

				// We're a go
				if(returnVal == JFileChooser.APPROVE_OPTION)
				{
					GuiController.copyTo(FrontEnd.frame.selectedFile,
							fileChooser.getSelectedFile().toPath());
				}
			}
		});

		revisionsOption.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				RevisionDialog revisionWindow = new RevisionDialog(FrontEnd.frame.selectedFile);
				revisionWindow.setLocationRelativeTo(FrontEnd.frame);
				revisionWindow.setModalityType(ModalityType.APPLICATION_MODAL);
				revisionWindow.setVisible(true);
			}
		});

		restoreAllOption.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				// Return value will be JFileChooser.APPROVE_OPTION iff a folder was chosen. Any
				// other value means the window was closed
				int returnVal = fileChooser.showOpenDialog(null);

				// We're a go
				if(returnVal == JFileChooser.APPROVE_OPTION)
				{
					GuiController.restoreBackup(fileChooser.getSelectedFile().toPath());
					JOptionPane.showMessageDialog(FrontEnd.frame,
							"All files in the backup directory have been restored to "
									+ fileChooser.getSelectedFile().toString());
				}
			}
		});

		changeBackupDirOption.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				// Return value will be JFileChooser.APPROVE_OPTION iff a folder was chosen. Any
				// other value means the window was closed
				int returnVal = fileChooser.showOpenDialog(null);

				// We're a go
				if(returnVal == JFileChooser.APPROVE_OPTION)
				{
					Path chosenPath = fileChooser.getSelectedFile().toPath();

					// We must make sure that the selected path isn't a child of the live directory
					// or vice versa
					if(!chosenPath.startsWith(Main.liveDirectory)
							&& !Main.liveDirectory.startsWith(chosenPath))
					{
						GuiController.changeBackupDirectory(fileChooser.getSelectedFile().toPath());
					}
					else
					{
						JOptionPane.showMessageDialog(FrontEnd.frame,
								"Backup directory cannot be a child of the live directory and vice versa.");
					}
				}
			}
		});

		changeLiveDirOption.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				// Return value will be JFileChooser.APPROVE_OPTION iff a folder was chosen. Any
				// other value means the window was closed
				int returnVal = fileChooser.showOpenDialog(null);

				// We're a go
				if(returnVal == JFileChooser.APPROVE_OPTION)
				{
					Path chosenPath = fileChooser.getSelectedFile().toPath();

					// We must make sure that the selected path isn't a child of the live directory
					// or vice versa
					if(!chosenPath.startsWith(Main.backupDirectory)
							&& !Main.backupDirectory.startsWith(chosenPath))
					{
						GuiController.changeLiveDirectory(fileChooser.getSelectedFile().toPath());
						FrontEnd.frame.redrawTable(Main.liveDirectory);
					}
					else
					{
						JOptionPane.showMessageDialog(FrontEnd.frame,
								"Live directory cannot be a child of the live directory and vice versa.");
					}
				}
			}
		});

		settingsOption.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				// Create the setting dialog, position it in the center of the current window, and
				// lock the current window while the settings window is open.
				SettingsDialog settingsDialog = new SettingsDialog();
				settingsDialog.setLocationRelativeTo(FrontEnd.frame);
				settingsDialog.setModalityType(ModalityType.APPLICATION_MODAL);
				settingsDialog.setVisible(true);
			}
		});

		exitOption.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int choice = JOptionPane.showConfirmDialog(FrontEnd.frame,
						"Are you sure you want to quit?", "Confirmation", JOptionPane.YES_NO_OPTION);
				if(choice == JOptionPane.YES_OPTION)
				{
					System.exit(0);
				}
			}
		});
	}

	/**
	 * Creates event handlers for the help menu
	 */
	private void initHelpActions()
	{
		helpOption.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Desktop desktop = Desktop.getDesktop();
				try
				{
					desktop.browse(URI.create("https://code.google.com/p/fbms/wiki/Documentation"));
				}
				catch(IOException e1)
				{
					Errors.nonfatalError("Could not open browser to display help.", e1);
				}
			}
		});
	}
}
