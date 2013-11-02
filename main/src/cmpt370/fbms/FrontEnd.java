package cmpt370.fbms;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

import javax.swing.ImageIcon;

import cmpt370.fbms.GUI.MainFrame;

public class FrontEnd
{
	public static void createAndShowGUI()
	{
		// Check the SystemTray support
		if(!SystemTray.isSupported())
		{
			System.out.println("SystemTray is not supported");
			return;
		}

		// Create the icon for the tray
		final TrayIcon trayIcon = new TrayIcon(createImage("res/icon.gif", "FBMS"));
		final SystemTray tray = SystemTray.getSystemTray();

		// Create a popup menu components (when right clicking on icon)
		final PopupMenu popup = new PopupMenu();
		MenuItem displayItem = new MenuItem("Display");
		MenuItem exitItem = new MenuItem("Exit");

		// Add components to popup menu
		popup.add(displayItem);
		popup.add(exitItem);

		// And set that menu to the tray icon
		trayIcon.setPopupMenu(popup);

		try
		{
			// Add to the tray
			tray.add(trayIcon);
		}
		catch(AWTException e)
		{
			Errors.fatalError("Cannot create system tray icon", e);
		}

		// Listener for when the tray iucon is clicked
		trayIcon.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				MainFrame frame = new MainFrame();
				frame.setVisible(true);
			}
		});

		// Listener for when the display item in the right click menu is clicked
		displayItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				MainFrame frame = new MainFrame();
				frame.setVisible(true);
			}
		});

		// Listener for when the exit item in the right click menu is clicked
		exitItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				tray.remove(trayIcon);
				System.exit(0);
			}
		});
	}

	/**
	 * Loads an image from the specified path.
	 * 
	 * @param path
	 * @param description
	 * @return
	 */
	private static Image createImage(String path, String description)
	{
		URL imageURL = null;
		try
		{
			// Try to load the image
			imageURL = Paths.get("").toAbsolutePath().resolve(path).toUri().toURL();
		}
		catch(MalformedURLException e)
		{
			Errors.fatalError("Error loading image.", e);
		}

		// Couldn't load the image
		if(imageURL == null)
		{
			System.err.println("Resource not found: " + path);
			return null;
		}
		else
		{
			return (new ImageIcon(imageURL, description)).getImage();
		}
	}
}
