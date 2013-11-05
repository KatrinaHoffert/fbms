package cmpt370.fbms.GUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
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
		setSize(new Dimension(400, 125));
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
		JPanel optionsPanel = new JPanel(new GridLayout(2, 1));

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

		// Startup scan panel
		JPanel scanPanel = new JPanel();
		JCheckBox scanCheck = new JCheckBox("Scan for file changes on startup");
		// Figure out if the box is checked
		String status = DbManager.getConfig("startupScan");
		if(status == null || status.equals("true"))
		{
			scanCheck.setSelected(true);
		}
		scanPanel.add(scanCheck);
		optionsPanel.add(scanPanel);

		// Add the settings panels to the main panel
		panel.add(optionsPanel, BorderLayout.CENTER);

		// Tooltips for options
		// Note that to allow the tooltips to be wrapped, we must use the HTML tag and <br /> tags
		String trimToolTip = "<html>Revisions older than this will be removed from the database. Set<br />"
				+ " to -1 to disable. Defaults to disabled.";
		trimLabel1.setToolTipText(trimToolTip);
		trimLabel2.setToolTipText(trimToolTip);
		trimOption.setToolTipText(trimToolTip);

		String scanToolTip = "<html>If enabled, the program will scan for changes when the program is<br />"
				+ " first started, allowing detection of file changes that occured while the<br />"
				+ " program was not running. However, this adds overhead to startup, and<br />"
				+ " can be disabled if the program is always running.<br />";
		scanCheck.setToolTipText(scanToolTip);

		// Create the buttons at the bottom
		JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));
		JButton cancelButton = new JButton("Cancel");
		JButton acceptButton = new JButton("Accept");
		buttonsPanel.add(cancelButton);
		buttonsPanel.add(acceptButton);
		panel.add(buttonsPanel, BorderLayout.SOUTH);

		// Listeners for the buttons
		cancelButton.addActionListener(new CancelActionListener(this));
		acceptButton.addActionListener(new AcceptActionListener(this, trimOption, scanCheck));

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
	private JCheckBox scanField;

	public AcceptActionListener(JDialog frame, JTextField trim, JCheckBox scan)
	{
		dialog = frame;
		trimField = trim;
		scanField = scan;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		// Parse the trim field
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

		// Parse the scan field
		if(scanField.isSelected())
		{
			DbManager.setConfig("startupScan", "true");
		}
		else
		{
			DbManager.setConfig("startupScan", "false");
		}
		Control.logger.info("Set startupScan to: " + DbManager.getConfig("startupScan"));

		// Close the window
		dialog.dispose();
	}
}
