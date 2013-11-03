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


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JToolBar;

public class MainToolBar extends JToolBar
{
	private static final long serialVersionUID = 1L;

	private JButton upButton = new JButton("up");
	private JButton refreshButton = new JButton("refresh");
	private JTextField currentDirectory = new JTextField(5);

	public MainToolBar()
	{
		currentDirectory.setEnabled(false);

		// Refresh button clicked
		refreshButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				System.out.println("Clicked refresh");
			}
		});

		// Up button clicked
		upButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				System.out.println("Clicked up");
			}
		});

		this.setFloatable(false);
		this.add(upButton);
		this.add(refreshButton);
		this.add(currentDirectory);
	}
}
