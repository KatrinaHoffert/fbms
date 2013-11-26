package cmpt370.fbms.test;

import java.util.Enumeration;

import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.framework.TestSuite;

public class TesterMainSuite
{

	public static void main(String[] args)
	{
		TestSuite ts = new TestSuite();
		TestResult result = new TestResult();
		ts.addTestSuite(TesterFileOp.class);
		ts.addTestSuite(TesterServices.class);
		ts.addTestSuite(TesterFileChangeHandler.class);
		ts.addTestSuite(TesterServices.class);
		ts.run(result);
		System.out.println("------\n" + "Test result\n" + "------");
		System.out.println("All tests succeed: " + result.wasSuccessful());
		Enumeration<TestFailure> failureList = result.failures();
		while(failureList.hasMoreElements())
		{
			TestFailure testFailure = (TestFailure) failureList.nextElement();
			System.out.println(testFailure);
		}
	}

}
