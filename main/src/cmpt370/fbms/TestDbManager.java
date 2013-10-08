package cmpt370.fbms;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestDbManager
{
	@Test
	public void testInit()
	{
		if(Control.liveDirectory == null)
		{
			assertTrue(DbManager.init());
		}
		else
		{
			assertFalse(DbManager.init());
		}
	}
}
