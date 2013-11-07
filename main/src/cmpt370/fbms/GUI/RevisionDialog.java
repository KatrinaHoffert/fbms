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


public class RevisionDialog extends JDialog
{
	public JTable table;
	public JButton b1, b2;

	private JPanel contentPane;

	/**
	 * Create the frame.
	 */
	public RevisionDialog(Path file)
	{
		System.out.println(file.toString());
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
		setTitle("Revision Log");

		// Set size and position
		setSize(423, 274);
		setLocationRelativeTo(null);

		// Create main panel
		contentPane = new JPanel();
		setContentPane(contentPane);

		// Set the icon
		setIconImage(new ImageIcon("res/icon.png").getImage());
		contentPane.setLayout(new BorderLayout(0, 0));
		// Create table columns
		Vector<String> columns = new Vector<>();
		columns.add("Date");
		columns.add("Size");
		columns.add("Delta");


		// Create table
		table = new JTable();
		table.setShowGrid(false);


		// Set some default column sizes as larger, so dates fit in better
		table.getColumnModel().getColumn(0).setMinWidth(100);
		table.getColumnModel().getColumn(1).setMinWidth(90);
		table.getColumnModel().getColumn(2).setMinWidth(90);


		// Create scrollpane for the table
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		table.setFillsViewportHeight(true);
		getContentPane().add(scrollPane);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(b1);
		buttonPanel.add(b2);
		contentPane.add(buttonPanel);
		buttonPanel.setLayout(new FlowLayout());


		// Necessary to revalidate the frame so that we can see the table
		revalidate();


	}
}
