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


import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;



public class MainMenu extends JMenuBar
{


	private static final long serialVersionUID = 1L;

	private JMenuItem revisionsOption = new JMenuItem("View revisions");
	private JMenuItem settingsOption = new JMenuItem("Settings");
	private JMenuItem exitOption = new JMenuItem("Exit");

	private JMenuItem helpOption = new JMenuItem("Display help");

	public MainMenu()
	{
		JMenu fileMenu = new JMenu("File");
		JMenu helpMenu = new JMenu("Help");

		this.add(fileMenu);
		this.add(helpMenu);

		fileMenu.add(revisionsOption);
		fileMenu.add(settingsOption);
		fileMenu.addSeparator();
		fileMenu.add(exitOption);

		helpMenu.add(helpOption);
		initFileActions();
		initHelpOptions();
	}

	private void initFileActions()
	{
		revisionsOption.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				System.out.println("Clicked revision");
			}
		});
		settingsOption.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
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
				System.exit(0);
			}
		});

	}

	private void initHelpOptions()
	{
		helpOption.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				System.out.println("Clicked help");
			}
		});
	}
}
