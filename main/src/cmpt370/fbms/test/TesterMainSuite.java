package cmpt370.fbms.test;

import java.util.Enumeration;

import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class TesterMainSuite
{

	public static void main(String[] args)
	{

		// Create a test suite and add all classes.
		TestSuite ts = new TestSuite();
		TestResult result = new TestResult();

		// Mute JMIMEMagic.
		Logger.getLogger(net.sf.jmimemagic.Magic.class).setLevel(Level.OFF);
		Logger.getLogger(net.sf.jmimemagic.MagicParser.class).setLevel(Level.OFF);
		Logger.getLogger(net.sf.jmimemagic.MagicMatch.class).setLevel(Level.OFF);
		Logger.getLogger(net.sf.jmimemagic.MagicMatcher.class).setLevel(Level.OFF);
		Logger.getLogger(net.sf.jmimemagic.detectors.TextFileDetector.class).setLevel(Level.OFF);

		// Add tests and run.
		ts.addTestSuite(TesterFileOp.class);
		ts.addTestSuite(TesterDbConnection.class);
		ts.addTestSuite(TesterFileChangeHandler.class);
		ts.addTestSuite(TesterVisual.class);
		ts.addTestSuite(TesterDateRetriever.class);
		ts.run(result);

		// Output result.
		System.out.println("------\n" + "Test result for basic modules\n" + "------");
		System.out.println("All tests succeed: " + result.wasSuccessful());
		Enumeration<TestFailure> failureList = result.failures();
		while(failureList.hasMoreElements())
		{
			TestFailure testFailure = (TestFailure) failureList.nextElement();
			System.out.println(testFailure);
		}

		System.out.println("All tests are executed. Please check result.");
	}

}
