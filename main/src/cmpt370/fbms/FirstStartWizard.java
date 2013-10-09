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

public class FirstStartWizard extends JDialog
{
	public FirstStartWizard()
	{
		// Create the dialog window
		this.setTitle("Welcome to FBMS");
		this.setVisible(true);
		this.setSize(new Dimension(400, 250));
		this.setLocationRelativeTo(null);

		// ======= Panel for the intro
		JPanel wizardIntroPanel = new JPanel(new BorderLayout());
		JLabel wizardIntroLabel = new JLabel("<html>Welcome to the <b>F</b>ile <b>B</b>ackup and "
				+ "<b>M</b>anagement <b>S</b>ystem, or FBMS. Use the next and previous buttons to "
				+ "navigate this wizard.</html>");
		wizardIntroLabel.setFont(new Font("Sans serif", Font.PLAIN, 14));
		wizardIntroPanel.add(wizardIntroLabel, BorderLayout.NORTH);
		wizardIntroPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel buttonPanel = new JPanel(new GridLayout());
		JButton quitButton = new JButton("Quit");
		buttonPanel.add(quitButton);
		JButton previousButton = new JButton("Previous");
		buttonPanel.add(previousButton);
		JButton nextButton = new JButton("Next");
		buttonPanel.add(nextButton);
		wizardIntroPanel.add(buttonPanel, BorderLayout.SOUTH);

		this.add(wizardIntroPanel);

		// Required for text to display properly
		this.revalidate();

		// Event handler for next button press
		nextButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				System.out.println("Next");
			}
		});

		// Event handler for previous button press
		previousButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				System.out.println("Previous");
			}
		});

		// Event handler for quit button press
		quitButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				System.exit(0);
			}
		});

		// Event handler to quit the program if the wizard is terminated prematurely
		this.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		});
	}
}
