package cmpt370.fbms.GUI;

import java.awt.BorderLayout;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;

import cmpt370.fbms.Control;
import cmpt370.fbms.Data;

public class MainFrame extends JFrame
{
	private JPanel contentPane;

	/**
	 * Create the frame.
	 */
	public MainFrame()
	{
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);

		// Set size and position
		setSize(768, 400);
		setLocationRelativeTo(null);

		// Create main panel
		contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		// Create menu
		MainMenu topMenu = new MainMenu();
		setJMenuBar(topMenu);

		// Create toolbar
		MainToolBar topTool = new MainToolBar();
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
		JTable table = new JTable(Data.getTableData(Control.backupDirectory), columns);

		// Create scrollpane for the table
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		table.setFillsViewportHeight(true);
		add(scrollPane, BorderLayout.CENTER);

		// Necessary to revalidate the frame so that we can see the table
		revalidate();
	}
}
