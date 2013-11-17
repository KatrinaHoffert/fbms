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

package cmpt370.fbms.gui;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.nio.file.Path;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;

import cmpt370.fbms.DataRetriever;
import cmpt370.fbms.Errors;
import cmpt370.fbms.FileOp;
import cmpt370.fbms.Main;

/**
 * Defines the JFrame-like object that makes up the main window of the GUI. This window consists of
 * a toolbar, menu bar, and the file browser table.
 */
public class MainFrame extends JFrame
{
	public JTable table;
	public Path currentDirectory;
	public MainToolBar toolbar;
	public MainMenu menubar;
	public Path selectedFile = null;
	public Vector<String> columns;

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

		// Set the frame variable to null when the frame is closed, so we can keep track of whether
		// or not a frame is open
		addWindowListener(new WindowListener()
		{
			@Override
			public void windowClosed(WindowEvent arg0)
			{
				FrontEnd.frame = null;
			}

			public void windowActivated(WindowEvent e)
			{}

			public void windowClosing(WindowEvent e)
			{}

			public void windowDeactivated(WindowEvent e)
			{}

			public void windowDeiconified(WindowEvent e)
			{}

			public void windowIconified(WindowEvent e)
			{}

			public void windowOpened(WindowEvent e)
			{}
		});

		// Set the icon
		setIconImage(new ImageIcon("res/icon.png").getImage());

		// Create menu
		menubar = new MainMenu();
		setJMenuBar(menubar);

		// Create toolbar
		toolbar = new MainToolBar();
		add(toolbar, BorderLayout.NORTH);

		// Create table columns
		columns = new Vector<>();
		columns.add("");
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
		table.getTableHeader().setReorderingAllowed(false);

		// Insert the data into a modified table model based on the default. This lets us disable
		// editing of table cells.
		DataRetriever folderRetriever = new DataRetriever(Main.backupDirectory);
		Vector<Vector<Object>> rows = folderRetriever.getFolderContentsTable();
		table.setModel(new DefaultTableModel(rows, columns)
		{
			@Override
			public boolean isCellEditable(int row, int column)
			{
				return false;
			}

			@Override
			public Class<?> getColumnClass(int column)
			{
				return getValueAt(0, column).getClass();
			}
		});

		// Set some default column sizes as larger, so dates fit in better
		table.getColumnModel().getColumn(0).setMinWidth(25);
		table.getColumnModel().getColumn(0).setMaxWidth(25);
		table.getColumnModel().getColumn(1).setMinWidth(100);
		table.getColumnModel().getColumn(3).setMinWidth(115);
		table.getColumnModel().getColumn(4).setMinWidth(115);
		table.getColumnModel().getColumn(5).setMinWidth(115);

		// Make the enter key active the cell instead of going to the next cell. Basically, we
		// create a new keyboard mapping for the enter key which does nothing, allowing the listener
		// to pick up the enter key later.
		final String solve = "Solve";
		KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(enter, solve);
		table.getActionMap().put(solve, null);

		// Create scrollpane for the table
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		table.setFillsViewportHeight(true);
		add(scrollPane, BorderLayout.CENTER);

		// Set the current directory
		currentDirectory = Main.backupDirectory;

		// In the backup directory, the location bar is set to a single slash. The direction of the
		// slash is platform dependent. Windows uses \\ while Unix and Mac use /.
		toolbar.locationBar.setText(File.separator);

		// Necessary to revalidate the frame so that we can see the table
		revalidate();

		table.addMouseListener(new TableSelectionListener());
		table.addKeyListener(new TableSelectionListener());
	}

	public void redrawTable(Path directory)
	{
		// Recreate the data model
		DataRetriever folderRetriever = new DataRetriever(directory);
		Vector<Vector<Object>> rows = folderRetriever.getFolderContentsTable();
		table.setModel(new DefaultTableModel(rows, columns)
		{
			@Override
			public boolean isCellEditable(int row, int column)
			{
				return false;
			}

			@Override
			public Class<?> getColumnClass(int column)
			{
				return getValueAt(0, column).getClass();
			}
		});

		// Set the appropriate widths
		table.getColumnModel().getColumn(0).setMinWidth(25);
		table.getColumnModel().getColumn(0).setMaxWidth(25);
		table.getColumnModel().getColumn(1).setMinWidth(100);
		table.getColumnModel().getColumn(3).setMinWidth(115);
		table.getColumnModel().getColumn(4).setMinWidth(115);
		table.getColumnModel().getColumn(5).setMinWidth(115);

		// Set the location bar to the current directory relative to the
		String locationBarText = directory.toString().substring(
				Main.backupDirectory.toString().length());
		FrontEnd.frame.toolbar.locationBar.setText(locationBarText);

		// Blank the selected file
		FrontEnd.frame.selectedFile = null;

		// If we're in the backup directory, set the location bar to a single slash
		if(locationBarText.equals(""))
		{
			FrontEnd.frame.toolbar.locationBar.setText(File.separator);
		}
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
		selectRow();

		// Get double clicks (and make sure that we actually clicked a row)
		if(e.getClickCount() == 2 && FrontEnd.frame.table.rowAtPoint(e.getPoint()) != -1)
		{
			activateRow();
		}

	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		selectRow();

		// If the enter key was pressed, "enter" that row (go into folders and open revision window
		// for files)
		if(e.getKeyCode() == KeyEvent.VK_ENTER)
		{
			activateRow();
		}
	}

	/**
	 * Called when a row is selected. Sets that file as the current file and enables
	 * context-specific menu options.
	 */
	private void selectRow()
	{
		// If we have selected a valid row
		if(FrontEnd.frame.table.getSelectedRow() != -1)
		{
			// table.getValueAt() will get the value in the selected row. Use that to get the file
			// name.
			FrontEnd.frame.selectedFile = FrontEnd.frame.currentDirectory.resolve((String) FrontEnd.frame.table.getValueAt(
					FrontEnd.frame.table.getSelectedRow(), 1));
			Main.logger.debug("Selected file/folder: " + FrontEnd.frame.selectedFile.toString());

			// Enable menu options that require a selected file (view revisions is only accessible
			// if a file is selected
			if(FrontEnd.frame.selectedFile.toFile().isFile())
			{
				FrontEnd.frame.menubar.revisionsOption.setEnabled(true);
			}
			else
			{
				FrontEnd.frame.menubar.revisionsOption.setEnabled(false);
			}
			FrontEnd.frame.menubar.copyToOption.setEnabled(true);
		}
		else
		{
			FrontEnd.frame.menubar.copyToOption.setEnabled(false);
			FrontEnd.frame.menubar.revisionsOption.setEnabled(false);
		}
	}

	/**
	 * Catches a double click or enter key on a row, which goes into folders and opens the revision
	 * info window for files.
	 */
	private void activateRow()
	{
		// Make sure the file exists (and that something is selected)
		if(FrontEnd.frame.selectedFile != null && FrontEnd.frame.selectedFile.toFile().exists())
		{
			// Go into directories
			if(FrontEnd.frame.selectedFile.toFile().isDirectory())
			{
				Main.logger.debug("Activated on folder: " + FrontEnd.frame.selectedFile.toString());

				// Set the new directory
				FrontEnd.frame.currentDirectory = FrontEnd.frame.currentDirectory.resolve(FrontEnd.frame.selectedFile.getFileName());

				// And recreate the table
				FrontEnd.frame.redrawTable(FrontEnd.frame.currentDirectory);

				// Disable options that require a selected file
				FrontEnd.frame.menubar.copyToOption.setEnabled(false);
				FrontEnd.frame.menubar.revisionsOption.setEnabled(false);

				FrontEnd.frame.toolbar.upButton.setEnabled(true);
			}
			// Display revision window for files
			else
			{
				Main.logger.debug("Activated on file: " + FrontEnd.frame.selectedFile.toString());

				RevisionDialog revisionWindow = new RevisionDialog(
						FileOp.convertPath(FrontEnd.frame.selectedFile));
				revisionWindow.setLocationRelativeTo(FrontEnd.frame);
				revisionWindow.setModalityType(ModalityType.APPLICATION_MODAL);
				revisionWindow.setVisible(true);
			}
		}
		else if(FrontEnd.frame.selectedFile != null)
		{
			Errors.nonfatalError("The selected file no longer exists.");
		}
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
