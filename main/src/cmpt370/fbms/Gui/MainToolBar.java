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

package cmpt370.fbms.Gui;

import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import cmpt370.fbms.Errors;
import cmpt370.fbms.Main;

/**
 * This JToolBar-like object makes up the toolbar of the main frame.
 */
public class MainToolBar extends JToolBar
{
	// Icons
	private ImageIcon upIcon = new ImageIcon("res/up.png");
	private ImageIcon refreshIcon = new ImageIcon("res/refresh.png");

	public JButton upButton = new JButton(upIcon);
	private JButton refreshButton = new JButton(refreshIcon);

	public JTextField locationBar = new JTextField(5);

	public MainToolBar()
	{
		// Format those buttons to look natural (no background, border, etc)
		upButton.setMargin(new Insets(0, 0, 0, 0));
		upButton.setBorder(null);
		upButton.setOpaque(false);
		upButton.setContentAreaFilled(false);
		upButton.setBorderPainted(false);
		upButton.setEnabled(false);
		upButton.setFocusPainted(false);
		refreshButton.setMargin(new Insets(0, 0, 0, 0));
		refreshButton.setBorder(null);
		refreshButton.setOpaque(false);
		refreshButton.setContentAreaFilled(false);
		refreshButton.setBorderPainted(false);
		refreshButton.setFocusPainted(false);

		// Format the directory text field to be disabled and a dark grey
		locationBar.setDisabledTextColor(new Color(0.2f, 0.2f, 0.2f));

		// Refresh button clicked
		refreshButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				// Disable options that require a selected file
				FrontEnd.frame.menubar.copyToOption.setEnabled(false);
				FrontEnd.frame.menubar.revisionsOption.setEnabled(false);

				// Recreate the table
				FrontEnd.frame.redrawTable(FrontEnd.frame.currentDirectory);
			}
		});

		// Up button clicked
		upButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				// Figure out the new path
				FrontEnd.frame.currentDirectory = FrontEnd.frame.currentDirectory.resolve("..").normalize();

				// Disable the up button if we're in the backup directory
				if(FrontEnd.frame.currentDirectory.equals(Main.backupDirectory))
				{
					FrontEnd.frame.toolbar.upButton.setEnabled(false);
				}

				// Disable options that require a selected file
				FrontEnd.frame.menubar.copyToOption.setEnabled(false);
				FrontEnd.frame.menubar.revisionsOption.setEnabled(false);

				// And recreate the table
				FrontEnd.frame.redrawTable(FrontEnd.frame.currentDirectory);
			}
		});

		// Enter key pressed within the location bar
		locationBar.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				// Remove the separate character (it's just for looks)
				String text = locationBar.getText();
				if(!text.equals("") && text.charAt(0) == File.separatorChar)
				{
					text = text.substring(1);
				}

				// Calculate the new path
				Path enteredPath = null;
				try
				{
					enteredPath = Main.backupDirectory.resolve(text).toFile().getCanonicalFile().toPath();
				}
				catch(IOException e1)
				{
					Errors.nonfatalError("Could not interpret entered path.", e1);
				}

				// Make sure that the path is a directory and not a parent of the backup directory
				if(enteredPath.toFile().isDirectory()
						&& (!Main.backupDirectory.startsWith(enteredPath.normalize()) || Main.backupDirectory.equals(enteredPath.normalize())))
				{
					FrontEnd.frame.redrawTable(enteredPath.normalize());
					FrontEnd.frame.currentDirectory = enteredPath.normalize();
				}
				else
				{
					JOptionPane.showMessageDialog(FrontEnd.frame,
							"The entered directory does not exist or is outside of the backup directory.");
				}

				// Possible disable or enable the up button
				if(FrontEnd.frame.currentDirectory.equals(Main.backupDirectory))
				{
					FrontEnd.frame.toolbar.upButton.setEnabled(false);
				}
				else
				{
					FrontEnd.frame.toolbar.upButton.setEnabled(true);
				}
			}
		});

		this.setFloatable(false);
		this.add(upButton);
		this.addSeparator();
		this.add(refreshButton);
		this.addSeparator();
		this.add(locationBar);
	}
}
