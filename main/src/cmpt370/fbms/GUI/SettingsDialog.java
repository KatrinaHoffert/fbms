package cmpt370.fbms.GUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cmpt370.fbms.Control;
import cmpt370.fbms.DbManager;

class SettingsDialog extends JDialog
{
	/**
	 * Initializes the settings dialog
	 */
	SettingsDialog()
	{
		setTitle("Settings");
		setSize(new Dimension(450, 250));
		setResizable(false);

		add(createSettings());
	}

	/**
	 * Creates the conents of the settings panel.
	 * 
	 * @return A panel for display.
	 */
	private JPanel createSettings()
	{
		JPanel panel = new JPanel(new BorderLayout());

		// Create the options panel
		JPanel optionsPanel = new JPanel(new GridLayout(1, 1));

		// Trim panel
		JPanel trimPanel = new JPanel();
		JLabel trimLabel1 = new JLabel("Remove revisions older than ");
		JTextField trimOption = new JTextField(3);
		trimOption.setText(DbManager.getConfig("trimDate"));
		JLabel trimLabel2 = new JLabel(" days.");
		trimPanel.add(trimLabel1);
		trimPanel.add(trimOption);
		trimPanel.add(trimLabel2);
		optionsPanel.add(trimPanel);

		panel.add(optionsPanel);

		// Create the buttons at the bottom
		JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));
		JButton cancelButton = new JButton("Cancel");
		JButton acceptButton = new JButton("Accept");
		buttonsPanel.add(cancelButton);
		buttonsPanel.add(acceptButton);
		panel.add(buttonsPanel, BorderLayout.SOUTH);

		// Listeners for the buttons
		cancelButton.addActionListener(new CancelActionListener(this));
		acceptButton.addActionListener(new AcceptActionListener(this, trimOption));

		return panel;
	}
}

/**
 * Closes the window without doing anything.
 */
class CancelActionListener implements ActionListener
{
	private JDialog dialog;

	public CancelActionListener(JDialog frame)
	{
		dialog = frame;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(dialog != null)
		{
			dialog.dispose();
		}
	}
}

/**
 * Modifies the database settings table, then closes the window.
 */
class AcceptActionListener implements ActionListener
{
	private JDialog dialog;
	private JTextField trimField;

	public AcceptActionListener(JDialog frame, JTextField trim)
	{
		dialog = frame;
		trimField = trim;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		// Parse the settings fields
		if(trimField.getText().matches("-?[0-9]+"))
		{
			if(Integer.parseInt(trimField.getText()) < 0)
			{
				DbManager.setConfig("trimDate", "-1");
			}
			else
			{
				DbManager.setConfig("trimDate", trimField.getText());
			}
		}
		else
		{
			DbManager.setConfig("trimDate", "-1");
		}

		Control.logger.info("Set trimDate to: " + DbManager.getConfig("trimDate"));

		// Close the window
		dialog.dispose();
	}
}
