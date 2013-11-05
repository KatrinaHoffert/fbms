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


import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JToolBar;

public class MainToolBar extends JToolBar
{
	private static final long serialVersionUID = 1L;

	// Icons
	private ImageIcon upIcon = new ImageIcon("res/up.png");
	private ImageIcon refreshIcon = new ImageIcon("res/refresh.png");

	private JButton upButton = new JButton(upIcon);
	private JButton refreshButton = new JButton(refreshIcon);

	private JTextField currentDirectory = new JTextField(5);

	public MainToolBar()
	{
		// Format those buttons to look natural (no background, border, etc)
		upButton.setMargin(new Insets(0, 0, 0, 0));
		upButton.setBorder(null);
		upButton.setOpaque(false);
		upButton.setContentAreaFilled(false);
		upButton.setBorderPainted(false);
		refreshButton.setMargin(new Insets(0, 0, 0, 0));
		refreshButton.setBorder(null);
		refreshButton.setOpaque(false);
		refreshButton.setContentAreaFilled(false);
		refreshButton.setBorderPainted(false);

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
		this.addSeparator();
		this.add(refreshButton);
		this.addSeparator();
		this.add(currentDirectory);
	}
}
