package cmpt370.fbms.GUI;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class MainMenu extends JMenuBar
{


	private static final long serialVersionUID = 1L;

	private JMenuItem revisionsOption = new JMenuItem("View revisions");
	private JMenuItem exitOption = new JMenuItem("Exit");

	private JMenuItem helpOption = new JMenuItem("Display help");

	public MainMenu()
	{
		JMenu fileMenu = new JMenu("File");
		JMenu helpMenu = new JMenu("Help");

		this.add(fileMenu);
		this.add(helpMenu);

		fileMenu.add(revisionsOption);
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
