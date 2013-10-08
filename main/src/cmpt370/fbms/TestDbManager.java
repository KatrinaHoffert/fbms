package cmpt370.fbms;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestDbManager
{
	@Test
	public void testInit()
	{
		// DbManager.init() returns true if it is the first run, meaning that
		// the live directory isn't set (will be null)
		boolean init = DbManager.init();
		assertTrue((init && Control.liveDirectory == null)
				|| (init && Control.liveDirectory != null));
	}
}
