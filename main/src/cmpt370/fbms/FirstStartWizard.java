package cmpt370.fbms;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class FirstStartWizard
{
	public static JDialog frame;
	public static int currentPanel = 1;

	public static void run()
	{
		// Create the dialog window
		frame = new JDialog();
		frame.setTitle("Welcome to FBMS");
		frame.setVisible(true);
		frame.setSize(new Dimension(400, 250));
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);

		frame.add(introPanel());

		// Required for text to display properly
		frame.revalidate();

		// Event handler to quit the program if the wizard is terminated prematurely
		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		});
	}

	// The first panel
	public static JPanel introPanel()
	{
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("<html>Welcome to the <b>F</b>ile <b>B</b>ackup and "
				+ "<b>M</b>anagement <b>S</b>ystem, or FBMS. Use the next and previous buttons to "
				+ "navigate this wizard.</html>");
		label.setFont(new Font("Sans serif", Font.PLAIN, 14));
		panel.add(label, BorderLayout.NORTH);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel buttonPanel = new JPanel(new GridLayout());
		JButton quitButton = new JButton("Quit");
		buttonPanel.add(quitButton);
		JButton prevButton = new JButton("Previous");
		prevButton.setEnabled(false);
		buttonPanel.add(prevButton);
		JButton nextButton = new JButton("Next");
		buttonPanel.add(nextButton);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		// Event handler for next button press
		nextButton.addActionListener(new WizardActionListener(1));

		prevButton.addActionListener(new WizardActionListener(-1));

		// Event handler for quit button press
		quitButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				System.exit(0);
			}
		});

		return panel;
	}

	// The second panel
	public static JPanel importPanel()
	{
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("<html>FBMS allows you to recover import old backups. Do "
				+ "you have an existing backup you wish to import? New users will want to choose "
				+ "\"no\".</html>");
		label.setFont(new Font("Sans serif", Font.PLAIN, 14));
		panel.add(label, BorderLayout.NORTH);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel buttonPanel = new JPanel(new GridLayout());
		JButton quitButton = new JButton("Quit");
		buttonPanel.add(quitButton);
		JButton prevButton = new JButton("No");
		buttonPanel.add(prevButton);
		JButton nextButton = new JButton("Yes");
		buttonPanel.add(nextButton);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		// Event handler for next button press
		nextButton.addActionListener(new WizardActionListener(10));

		prevButton.addActionListener(new WizardActionListener(1));

		// Event handler for quit button press
		quitButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				System.exit(0);
			}
		});

		return panel;
	}

	// The third panel
	public static JPanel selectDirsPanel()
	{
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("<html>Select a live directory to monitor and a backup "
				+ "directory to place your backed up files in.</html>");
		label.setFont(new Font("Sans serif", Font.PLAIN, 14));
		panel.add(label, BorderLayout.NORTH);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel buttonPanel = new JPanel(new GridLayout());
		JButton quitButton = new JButton("Quit");
		buttonPanel.add(quitButton);
		JButton prevButton = new JButton("Previous");
		buttonPanel.add(prevButton);
		JButton nextButton = new JButton("Next");
		nextButton.setEnabled(false);
		buttonPanel.add(nextButton);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		// Event handler for next button press
		nextButton.addActionListener(new WizardActionListener(1));

		prevButton.addActionListener(new WizardActionListener(-1));

		// Event handler for quit button press
		quitButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				System.exit(0);
			}
		});

		return panel;
	}

	// Special case: import old dir
	public static JPanel selectOldDirPanel()
	{
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("<html>Specify the directory which held the backup (contains "
				+ "a file named \".revisions.db\").</html>");
		label.setFont(new Font("Sans serif", Font.PLAIN, 14));
		panel.add(label, BorderLayout.NORTH);
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel buttonPanel = new JPanel(new GridLayout());
		JButton quitButton = new JButton("Quit");
		buttonPanel.add(quitButton);
		JButton prevButton = new JButton("Previous");
		buttonPanel.add(prevButton);
		JButton nextButton = new JButton("Next");
		nextButton.setEnabled(false);
		buttonPanel.add(nextButton);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		// Event handler for next button press
		nextButton.addActionListener(new WizardActionListener(1));

		prevButton.addActionListener(new WizardActionListener(-10));

		// Event handler for quit button press
		quitButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				System.exit(0);
			}
		});

		return panel;
	}
}

// Event handler for next and previous buttons. Takes in an offset and applies that to the current
// panel to figure out which panel needs to be displayed
class WizardActionListener implements ActionListener
{
	private int offset;

	// Just a way to get the direction forward or back a button takes us
	public WizardActionListener(int inOffset)
	{
		offset = inOffset;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(FirstStartWizard.currentPanel + offset == 1)
		{
			// We need to set the content pane with the new panel, then *validate* so it renders
			// correctly
			FirstStartWizard.frame.setContentPane(FirstStartWizard.introPanel());
			FirstStartWizard.frame.validate();
			FirstStartWizard.currentPanel = 1;
		}
		else if(FirstStartWizard.currentPanel + offset == 2)
		{
			FirstStartWizard.frame.setContentPane(FirstStartWizard.importPanel());
			FirstStartWizard.frame.validate();
			FirstStartWizard.currentPanel = 2;
		}
		else if(FirstStartWizard.currentPanel + offset == 3)
		{
			FirstStartWizard.frame.setContentPane(FirstStartWizard.selectDirsPanel());
			FirstStartWizard.frame.validate();
			FirstStartWizard.currentPanel = 3;
		}
		else if(FirstStartWizard.currentPanel + offset == 12)
		{
			FirstStartWizard.frame.setContentPane(FirstStartWizard.selectOldDirPanel());
			FirstStartWizard.frame.validate();
			FirstStartWizard.currentPanel = 12;
		}
	}
}
