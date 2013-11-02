package cmpt370.fbms;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.Menu;
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
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import cmpt370.fbms.GUI.MainFrame;

public class FrontEnd
{
	private static void createAndShowGUI()
	{
		// Check the SystemTray support
		if(!SystemTray.isSupported())
		{
			System.out.println("SystemTray is not supported");
			return;
		}
		final PopupMenu popup = new PopupMenu();
		final TrayIcon trayIcon = new TrayIcon(createImage("res/icon.gif", "tray icon"));
		final SystemTray tray = SystemTray.getSystemTray();

		// Create a popup menu components
		MenuItem aboutItem = new MenuItem("About");
		Menu displayMenu = new Menu("Display");
		MenuItem exitItem = new MenuItem("Exit");
		MenuItem openGUI = new MenuItem("Open");

		// Add components to popup menu
		popup.add(aboutItem);
		popup.addSeparator();
		popup.addSeparator();
		popup.add(displayMenu);
		displayMenu.add(openGUI);
		popup.add(exitItem);

		trayIcon.setPopupMenu(popup);

		try
		{
			tray.add(trayIcon);
		}
		catch(AWTException e)
		{
			System.out.println("TrayIcon could not be added.");
			return;
		}

		trayIcon.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				MainFrame frame = new MainFrame();
				frame.setVisible(true);
			}
		});

		aboutItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JOptionPane.showMessageDialog(null,
						"This dialog box is run from the About menu item");
			}
		});


		ActionListener listener = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				MenuItem item = (MenuItem) e.getSource();
				// TrayIcon.MessageType type = null;
				System.out.println(item.getLabel());
				if("Error".equals(item.getLabel()))
				{
					// type = TrayIcon.MessageType.ERROR;
					trayIcon.displayMessage("Sun TrayIcon Demo", "This is an error message",
							TrayIcon.MessageType.ERROR);

				}
				else if("Warning".equals(item.getLabel()))
				{
					// type = TrayIcon.MessageType.WARNING;
					trayIcon.displayMessage("Sun TrayIcon Demo", "This is a warning message",
							TrayIcon.MessageType.WARNING);

				}
				else if("Info".equals(item.getLabel()))
				{
					// type = TrayIcon.MessageType.INFO;
					trayIcon.displayMessage("Sun TrayIcon Demo", "This is an info message",
							TrayIcon.MessageType.INFO);

				}
				else if("None".equals(item.getLabel()))
				{
					// type = TrayIcon.MessageType.NONE;
					trayIcon.displayMessage("Sun TrayIcon Demo", "This is an ordinary message",
							TrayIcon.MessageType.NONE);
				}
			}
		};

		// errorItem.addActionListener(listener);
		// warningItem.addActionListener(listener);
		// infoItem.addActionListener(listener);
		// noneItem.addActionListener(listener);

		exitItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				tray.remove(trayIcon);
				System.exit(0);
			}
		});

		openGUI.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				MainFrame frame = new MainFrame();
				frame.setVisible(true);
			}
		});
	}

	protected static Image createImage(String path, String description)
	{
		URL imageURL = null;
		try
		{
			imageURL = Paths.get("").toAbsolutePath().resolve(path).toUri().toURL();
		}
		catch(MalformedURLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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

	public static void main(String[] args)
	{
		/* Use an appropriate Look and Feel */
		try
		{
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			// UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		}
		catch(UnsupportedLookAndFeelException ex)
		{
			ex.printStackTrace();
		}
		catch(IllegalAccessException ex)
		{
			ex.printStackTrace();
		}
		catch(InstantiationException ex)
		{
			ex.printStackTrace();
		}
		catch(ClassNotFoundException ex)
		{
			ex.printStackTrace();
		}
		/* Turn off metal's use of bold fonts */
		UIManager.put("swing.boldMetal", Boolean.FALSE);
		// Schedule a job for the event-dispatching thread:
		// adding TrayIcon.
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				createAndShowGUI();
			}
		});
	}

}
