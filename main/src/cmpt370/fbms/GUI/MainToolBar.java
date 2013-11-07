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


import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;

import cmpt370.fbms.Control;
import cmpt370.fbms.Data;

public class MainToolBar extends JToolBar
{
	// Icons
	private ImageIcon upIcon = new ImageIcon("res/up.png");
	private ImageIcon refreshIcon = new ImageIcon("res/refresh.png");

	public JButton upButton = new JButton(upIcon);
	private JButton refreshButton = new JButton(refreshIcon);

	public JTextField currentDirectory = new JTextField(5);

	public MainToolBar()
	{
		// Format those buttons to look natural (no background, border, etc)
		upButton.setMargin(new Insets(0, 0, 0, 0));
		upButton.setBorder(null);
		upButton.setOpaque(false);
		upButton.setContentAreaFilled(false);
		upButton.setBorderPainted(false);
		upButton.setEnabled(false);
		refreshButton.setMargin(new Insets(0, 0, 0, 0));
		refreshButton.setBorder(null);
		refreshButton.setOpaque(false);
		refreshButton.setContentAreaFilled(false);
		refreshButton.setBorderPainted(false);

		// Format the directory text field to be disabled and a dark grey
		currentDirectory.setEnabled(false);
		currentDirectory.setDisabledTextColor(new Color(0.2f, 0.2f, 0.2f));

		// Refresh button clicked
		refreshButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				// Disable options that require a selected file
				FrontEnd.frame.topMenu.copyToOption.setEnabled(false);
				FrontEnd.frame.topMenu.revisionsOption.setEnabled(false);

				// Recreate the table
				FrontEnd.frame.table.setModel(new DefaultTableModel(
						Data.getTableData(FrontEnd.frame.currentDirectory), FrontEnd.frame.columns)
				{
					@Override
					public boolean isCellEditable(int row, int column)
					{
						return false;
					}
				});
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
				if(FrontEnd.frame.currentDirectory.equals(Control.backupDirectory))
				{
					FrontEnd.frame.topTool.upButton.setEnabled(false);
				}

				// Disable options that require a selected file
				FrontEnd.frame.topMenu.copyToOption.setEnabled(false);
				FrontEnd.frame.topMenu.revisionsOption.setEnabled(false);

				// And recreate the table
				FrontEnd.frame.table.setModel(new DefaultTableModel(
						Data.getTableData(FrontEnd.frame.currentDirectory), FrontEnd.frame.columns)
				{
					@Override
					public boolean isCellEditable(int row, int column)
					{
						return false;
					}
				});
			}
		});

		this.setFloatable(false);
		this.add(upButton);
		this.addSeparator();
		this.add(refreshButton);
		this.addSeparator();
		this.add(currentDirectory);
	}
}
