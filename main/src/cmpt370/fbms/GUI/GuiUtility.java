package cmpt370.fbms.GUI;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cmpt370.fbms.Errors;

/**
 * Basic utility methods for assisting the GUI with the display and formatting of data.
 */
public class GuiUtility
{
	/**
	 * Takes in a Unix time stamp and formats it as a human readable String
	 * 
	 * @param timestamp
	 *            The Unix time stamp (seconds since 1970-01-01 00:00:00)
	 * @return A String in ISO 8601 format.
	 */
	public static String formatDate(long timestamp)
	{
		// Specify the format that the date should be in (like ISO 8601)
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		// Note that the Date object is created with milliseconds, while we have seconds
		Date date = new Date(timestamp * 1000);

		return dateFormat.format(date);
	}

	/**
	 * Converts a human readable date from formatDate() into a time stamp (seconds since Unix epoch)
	 * that the system can use.
	 * 
	 * @param timestamp
	 *            The date in the format yyyy-mm-dd hh:mm:ss
	 * @return The seconds since 1970-01-01 00:00:00
	 */
	public static long unformatDate(String timestamp)
	{
		// Read the date in
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		try
		{
			// Convert it
			date = sdf.parse(timestamp);
		}
		catch(ParseException e)
		{
			Errors.nonfatalError("Could not retreive time stamp for revision.", e);
		}

		// Get the time in seconds since Unix epoch
		long timeInMsSinceEpoch = date.getTime();
		return timeInMsSinceEpoch / 1000;
	}

	/**
	 * Takes in a number of bytes and converts it to a human readable format (eg, 51355 bytes
	 * becomes 50.1 KiB.
	 * 
	 * @author aioobe <http://stackoverflow.com/users/276052/aioobe> from
	 *         <http://stackoverflow.com/a/3758880>
	 * 
	 * @param bytes
	 *            The number of bytes.
	 * @param si
	 *            If false, use powers of 2, if true, use powers of 10. For example, if using powers
	 *            of 2, then 1024 bytes = 1.0 KiB, while using powers of 10 has 1000 bytes = 1.0 kB.
	 * @return A String representation of a human readable byte count.
	 */
	public static String humanReadableBytes(long bytes, boolean si)
	{
		// Figure out if we're using powers of 2 (non-SI) or 10 (SI)
		int unit = si ? 1000 : 1024;
		if(bytes < unit)
		{
			return bytes + " B";
		}

		// Figure out the unit being used
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");

		// Return as a formatted string
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
}
