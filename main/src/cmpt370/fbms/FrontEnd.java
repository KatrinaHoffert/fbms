package cmpt370.fbms;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

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
		BufferedImage trayIconImage = null;
		try
		{
			trayIconImage = ImageIO.read(Paths.get("res/icon.png").toFile());
		}
		catch(IOException e)
		{
			Control.logger.error("Could not load program icon", e);
		}

		// And scale it to the appropriate size using smooth scaling
		int trayIconWidth = new TrayIcon(trayIconImage).getSize().width;
		final TrayIcon trayIcon = new TrayIcon(trayIconImage.getScaledInstance(trayIconWidth, -1,
				Image.SCALE_SMOOTH));

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
}
