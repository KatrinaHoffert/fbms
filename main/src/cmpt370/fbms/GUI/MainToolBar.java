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
