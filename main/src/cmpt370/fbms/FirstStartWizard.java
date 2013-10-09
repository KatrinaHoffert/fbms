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

	public static JPanel introPanel()
	{
		JPanel introPanel = new JPanel(new BorderLayout());
		JLabel introLabel = new JLabel("<html>Welcome to the <b>F</b>ile <b>B</b>ackup and "
				+ "<b>M</b>anagement <b>S</b>ystem, or FBMS. Use the next and previous buttons to "
				+ "navigate this wizard.</html>");
		introLabel.setFont(new Font("Sans serif", Font.PLAIN, 14));
		introPanel.add(introLabel, BorderLayout.NORTH);
		introPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel buttonPanel = new JPanel(new GridLayout());
		JButton quitButton = new JButton("Quit");
		buttonPanel.add(quitButton);
		JButton introPrevious = new JButton("Previous");
		introPrevious.setEnabled(false);
		buttonPanel.add(introPrevious);
		JButton introNext = new JButton("Next");
		buttonPanel.add(introNext);
		introPanel.add(buttonPanel, BorderLayout.SOUTH);

		// Event handler for next button press
		introNext.addActionListener(new WizardActionListener(1));

		introPrevious.addActionListener(new WizardActionListener(-1));

		// Event handler for quit button press
		quitButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				System.exit(0);
			}
		});

		return introPanel;
	}

	public static JPanel importPanel()
	{
		JPanel introPanel = new JPanel(new BorderLayout());
		JLabel introLabel = new JLabel("<html>Wanna import?</html>");
		introLabel.setFont(new Font("Sans serif", Font.PLAIN, 14));
		introPanel.add(introLabel, BorderLayout.NORTH);
		introPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel buttonPanel = new JPanel(new GridLayout());
		JButton quitButton = new JButton("Quit");
		buttonPanel.add(quitButton);
		JButton introPrevious = new JButton("Previous");
		buttonPanel.add(introPrevious);
		JButton introNext = new JButton("Next");
		buttonPanel.add(introNext);
		introPanel.add(buttonPanel, BorderLayout.SOUTH);

		// Event handler for next button press
		introNext.addActionListener(new WizardActionListener(1));

		introPrevious.addActionListener(new WizardActionListener(-1));

		// Event handler for quit button press
		quitButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				System.exit(0);
			}
		});

		return introPanel;
	}
}

class WizardActionListener implements ActionListener
{
	private int offset;

	public WizardActionListener(int inOffset)
	{
		offset = inOffset;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(FirstStartWizard.currentPanel + offset == 1)
		{
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
	}
}
