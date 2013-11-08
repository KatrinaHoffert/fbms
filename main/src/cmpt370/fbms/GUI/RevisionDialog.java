package cmpt370.fbms.GUI;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.nio.file.Path;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;

import cmpt370.fbms.Data;


public class RevisionDialog extends JDialog
{
	public JTable table;
	public JButton viewRevisionButton, revertRevisionButton;

	/**
	 * Create the frame.
	 */
	public RevisionDialog(Path file)
	{
		setTitle("Revision Log");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setIconImage(new ImageIcon("res/icon.png").getImage());
		setSize(400, 250);

		// Create main panel
		JPanel contentPane = new JPanel(new BorderLayout());

		// Create table columns
		Vector<String> columns = new Vector<>();
		columns.add("Date");
		columns.add("File size");
		columns.add("Delta");

		// Create table
		table = new JTable();
		table.setShowGrid(false);

		// Create the data model
		table.setModel(new DefaultTableModel(Data.getRevisionData(file), columns)
		{
			@Override
			public boolean isCellEditable(int row, int column)
			{
				return false;
			}
		});

		// Create scrollpane for the table
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		table.setFillsViewportHeight(true);

		// Create buttons at the bottom
		JPanel buttonPanel = new JPanel(new FlowLayout());
		viewRevisionButton = new JButton("View revision");
		revertRevisionButton = new JButton("Revert revision");
		buttonPanel.add(viewRevisionButton);
		buttonPanel.add(revertRevisionButton);

		// And add to the main panel
		contentPane.add(scrollPane, BorderLayout.CENTER);
		contentPane.add(buttonPanel, BorderLayout.SOUTH);
		add(contentPane);

		// Necessary to revalidate the frame so that we can see the table
		revalidate();
	}
}
