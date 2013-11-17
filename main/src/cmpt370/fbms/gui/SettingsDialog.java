package cmpt370.fbms.gui;

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

import org.apache.log4j.Logger;

import cmpt370.fbms.DbConnection;
import cmpt370.fbms.Main;

/**
 * Extends a JDialog to create the window that allows settings to be chosen.
 */
@SuppressWarnings("serial")
class SettingsDialog extends JDialog
{
	// Logger instance
	private static Logger logger = Logger.getLogger(Main.class);

	private DbConnection db = DbConnection.getInstance();

	/**
	 * Initializes the settings dialog
	 */
	SettingsDialog()
	{
		setTitle("Settings");
		setSize(new Dimension(325, 175));
		setResizable(false);

		add(createSettings());

		logger.info("Setting dialog drawn");
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
		JPanel optionsPanel = new JPanel(new GridLayout(4, 1));

		// Trim panel
		JPanel trimPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel trimLabel1 = new JLabel("Remove revisions older than ");
		JTextField trimOption = new JTextField(3);
		trimOption.setText(db.getConfig("trimDate"));
		JLabel trimLabel2 = new JLabel(" days");
		trimPanel.add(trimLabel1);
		trimPanel.add(trimOption);
		trimPanel.add(trimLabel2);
		optionsPanel.add(trimPanel);

		// Max size panel
		JPanel maxSizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel maxSizeLabel1 = new JLabel("Don't revision files larger than ");
		JTextField maxSizeOption = new JTextField(3);
		String maxSize = db.getConfig("maxSize");
		if(maxSize == null)
		{
			maxSizeOption.setText("5");
		}
		else
		{
			maxSizeOption.setText(maxSize);
		}
		JLabel maxSizeLabel2 = new JLabel("MB");
		maxSizePanel.add(maxSizeLabel1);
		maxSizePanel.add(maxSizeOption);
		maxSizePanel.add(maxSizeLabel2);
		optionsPanel.add(maxSizePanel);

		// Startup scan panel
		JPanel scanPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JCheckBox scanCheck = new JCheckBox("Scan for file changes on startup");
		// Figure out if the box is checked
		String statusStartupScan = db.getConfig("startupScan");
		if(statusStartupScan == null || statusStartupScan.equals("true"))
		{
			scanCheck.setSelected(true);
		}
		scanPanel.add(scanCheck);
		optionsPanel.add(scanPanel);

		// disable errors panel
		JPanel disableErrorsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JCheckBox disableErrorsCheck = new JCheckBox("Don't display non-fatal errors");
		// Figure out if the box is checked
		String statusErrors = db.getConfig("disableNonFatalErrors");
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

		String maxSizeToolTip = "<html>Files larger than this will not be revisioned, and will just be<br />"
				+ " copied to the backup directory.";
		maxSizeLabel1.setToolTipText(maxSizeToolTip);
		maxSizeLabel2.setToolTipText(maxSizeToolTip);
		maxSizeOption.setToolTipText(maxSizeToolTip);

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
		acceptButton.addActionListener(new AcceptActionListener(this, trimOption, maxSizeOption,
				scanCheck, disableErrorsCheck));

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
	// Logger instance
	private static Logger logger = Logger.getLogger(Main.class);

	private DbConnection db = DbConnection.getInstance();
	private JDialog dialog;
	private JTextField trimField;
	private JTextField maxSizeField;
	private JCheckBox scanField;
	private JCheckBox disableErrorsField;

	public AcceptActionListener(JDialog frame, JTextField trim, JTextField maxSize, JCheckBox scan,
			JCheckBox errors)
	{
		dialog = frame;
		trimField = trim;
		maxSizeField = maxSize;
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
				db.setConfig("trimDate", "-1");
			}
			else
			{
				db.setConfig("trimDate", trimField.getText());
			}
		}
		else
		{
			db.setConfig("trimDate", "-1");
		}
		logger.info("Set trimDate to: " + db.getConfig("trimDate"));

		// Parse the maxSize field
		if(maxSizeField.getText().matches("[0-9]+\\.?[0-9]*"))
		{
			try
			{
				float maxSizeInMb = Float.parseFloat(maxSizeField.getText());
				db.setConfig("maxSize", Float.toString(maxSizeInMb));
			}
			catch(NumberFormatException e1)
			{
				// Invalid numbers will fallback to the default
				db.setConfig("maxSize", "5");
			}
		}
		else
		{
			// Invalid number
			db.setConfig("maxSize", "5");
		}
		logger.info("Set maxSize to: " + db.getConfig("maxSize"));

		// Parse the scan field
		if(scanField.isSelected())
		{
			db.setConfig("startupScan", "true");
		}
		else
		{
			db.setConfig("startupScan", "false");
		}
		logger.info("Set startupScan to: " + db.getConfig("startupScan"));

		// Parse the disable errors field
		if(disableErrorsField.isSelected())
		{
			db.setConfig("disableNonFatalErrors", "true");
		}
		else
		{
			db.setConfig("disableNonFatalErrors", "false");
		}
		logger.info("Set disableNonFatalErrors to: " + db.getConfig("disableNonFatalErrors"));

		// Close the window
		dialog.dispose();
	}
}
