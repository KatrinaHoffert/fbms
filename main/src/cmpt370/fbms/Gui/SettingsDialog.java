package cmpt370.fbms.Gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cmpt370.fbms.DbManager;
import cmpt370.fbms.Main;

/**
 * Extends a JDialog to create the window that allows settings to be chosen.
 */
class SettingsDialog extends JDialog
{
	/**
	 * Initializes the settings dialog
	 */
	SettingsDialog()
	{
		setTitle("Settings");
		setSize(new Dimension(300, 150));
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
		setIconImage(new ImageIcon("res/icon.png").getImage());

		// Create the options panel
		JPanel optionsPanel = new JPanel(new GridLayout(3, 1));

		// Trim panel
		JPanel trimPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel trimLabel1 = new JLabel("Remove revisions older than ");
		JTextField trimOption = new JTextField(3);
		trimOption.setText(DbManager.getConfig("trimDate"));
		JLabel trimLabel2 = new JLabel(" days.");
		trimPanel.add(trimLabel1);
		trimPanel.add(trimOption);
		trimPanel.add(trimLabel2);
		optionsPanel.add(trimPanel);

		// Startup scan panel
		JPanel scanPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JCheckBox scanCheck = new JCheckBox("Scan for file changes on startup");
		// Figure out if the box is checked
		String statusStartupScan = DbManager.getConfig("startupScan");
		if(statusStartupScan == null || statusStartupScan.equals("true"))
		{
			scanCheck.setSelected(true);
		}
		scanPanel.add(scanCheck);
		optionsPanel.add(scanPanel);

		// Startup scan panel
		JPanel disableErrorsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JCheckBox disableErrorsCheck = new JCheckBox("Don't display non-fatal errors");
		// Figure out if the box is checked
		String statusErrors = DbManager.getConfig("disableNonFatalErrors");
		if(statusErrors != null && statusErrors.equals("true"))
		{
			disableErrorsCheck.setSelected(true);
		}
		disableErrorsPanel.add(disableErrorsCheck);
		optionsPanel.add(disableErrorsPanel);

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

		String disableErrorsToolTip = "<html>If enabled, non-fatal errors, which normally bring up a warning<br />"
				+ " box in the bottom right corner of the screen, will be disabled. This is not<br />"
				+ " recommended, as you can be oblivious to issues the program is facing, but is<br />"
				+ " an option if you have permission issues that cause error messages frequently.<br />";
		disableErrorsCheck.setToolTipText(disableErrorsToolTip);

		// Create the buttons at the bottom
		JPanel buttonsPanel = new JPanel();
		JButton cancelButton = new JButton("Cancel");
		JButton acceptButton = new JButton("Accept");
		buttonsPanel.add(cancelButton);
		buttonsPanel.add(acceptButton);
		panel.add(buttonsPanel, BorderLayout.SOUTH);

		// Listeners for the buttons
		cancelButton.addActionListener(new CancelActionListener(this));
		acceptButton.addActionListener(new AcceptActionListener(this, trimOption, scanCheck,
				disableErrorsCheck));

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
	private JCheckBox disableErrorsField;

	public AcceptActionListener(JDialog frame, JTextField trim, JCheckBox scan, JCheckBox errors)
	{
		dialog = frame;
		trimField = trim;
		scanField = scan;
		disableErrorsField = errors;
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
		Main.logger.info("Set trimDate to: " + DbManager.getConfig("trimDate"));

		// Parse the scan field
		if(scanField.isSelected())
		{
			DbManager.setConfig("startupScan", "true");
		}
		else
		{
			DbManager.setConfig("startupScan", "false");
		}
		Main.logger.info("Set startupScan to: " + DbManager.getConfig("startupScan"));

		// Parse the disable errors field
		if(disableErrorsField.isSelected())
		{
			DbManager.setConfig("disableNonFatalErrors", "true");
		}
		else
		{
			DbManager.setConfig("disableNonFatalErrors", "false");
		}
		Main.logger.info("Set disableNonFatalErrors to: "
				+ DbManager.getConfig("disableNonFatalErrors"));

		// Close the window
		dialog.dispose();
	}
}
