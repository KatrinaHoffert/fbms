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

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.nio.file.Path;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;

import cmpt370.fbms.Control;
import cmpt370.fbms.Data;

public class MainFrame extends JFrame
{
	public JTable table;
	public Path currentDirectory;
	public MainToolBar topTool;
	public MainMenu topMenu;
	public Path selectedFile = null;

	private JPanel contentPane;

	/**
	 * Create the frame.
	 */
	public MainFrame()
	{
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
		setTitle("FBMS: File Backup and Management System");

		// Set size and position
		setSize(900, 400);
		setLocationRelativeTo(null);

		// Create main panel
		contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		// Set the icon
		setIconImage(new ImageIcon("res/icon.png").getImage());

		// Create menu
		topMenu = new MainMenu();
		setJMenuBar(topMenu);

		// Create toolbar
		topTool = new MainToolBar();
		add(topTool, BorderLayout.NORTH);

		// Create table columns
		Vector<String> columns = new Vector<>();
		columns.add("Name");
		columns.add("Size");
		columns.add("Created date");
		columns.add("Accessed date");
		columns.add("Modified date");
		columns.add("Revisions");
		columns.add("Revision sizes");

		// Create table
		table = new JTable();
		table.setShowGrid(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Insert the data into a modified table model based on the default. This lets us disable
		// editing of table cells.
		table.setModel(new DefaultTableModel(Data.getTableData(Control.backupDirectory), columns)
		{
			@Override
			public boolean isCellEditable(int row, int column)
			{
				return false;
			}
		});

		// Set some default column sizes as larger, so dates fit in better
		table.getColumnModel().getColumn(0).setMinWidth(100);
		table.getColumnModel().getColumn(2).setMinWidth(90);
		table.getColumnModel().getColumn(3).setMinWidth(90);
		table.getColumnModel().getColumn(4).setMinWidth(90);

		// Create scrollpane for the table
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		table.setFillsViewportHeight(true);
		add(scrollPane, BorderLayout.CENTER);

		// Set the current directory
		currentDirectory = Control.backupDirectory;
		topTool.currentDirectory.setText("/");

		// Necessary to revalidate the frame so that we can see the table
		revalidate();

		table.addMouseListener(new TableSelectionListener());
		table.addKeyListener(new TableSelectionListener());
	}
}

/**
 * An event listener for finding changes to the currently selected row in the table (via either
 * clicking a row with the mouse or navigating via the keyboard).
 */
class TableSelectionListener implements MouseListener, KeyListener
{
	@Override
	public void mouseClicked(MouseEvent e)
	{
		// If we have selected a valid row
		if(FrontEnd.frame.table.getSelectedRow() != -1)
		{
			FrontEnd.frame.topMenu.copyToOption.setEnabled(true);
			FrontEnd.frame.topMenu.revisionsOption.setEnabled(true);

			// table.getValueAt() will get the value in the selected row. Use that to get the file
			// name.
			FrontEnd.frame.selectedFile = FrontEnd.frame.currentDirectory.resolve((String) FrontEnd.frame.table.getValueAt(
					FrontEnd.frame.table.getSelectedRow(), 0));

			// TODO: Remove this temporary line
			System.out.println(FrontEnd.frame.selectedFile.toString());
		}
		else
		{
			FrontEnd.frame.topMenu.copyToOption.setEnabled(false);
			FrontEnd.frame.topMenu.revisionsOption.setEnabled(false);
		}
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		mouseClicked(null);
	}

	@Override
	public void keyPressed(KeyEvent e)
	{}

	@Override
	public void mouseEntered(MouseEvent e)
	{}

	@Override
	public void mouseExited(MouseEvent e)
	{}

	@Override
	public void mousePressed(MouseEvent e)
	{}

	@Override
	public void mouseReleased(MouseEvent e)
	{}

	@Override
	public void keyTyped(KeyEvent e)
	{}

}
