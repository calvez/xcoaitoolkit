/**
  * Copyright (c) 2009 University of Rochester
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the MIT/X11 license. The text of the  
  * license can be found at http://www.opensource.org/licenses/mit-license.php and copy of the license can be found on the project
  * website http://www.extensiblecatalog.org/. 
  *
  */

package test.extensiblecatalog.OAIToolkit.db.managers;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for test.extensiblecatalog.OAIToolkit.db.managers");
		//$JUnit-BEGIN$
		suite.addTestSuite(MainDataMgrTestCase.class);
		suite.addTestSuite(SetSpecsTestCase.class);
		suite.addTestSuite(SetsMgrTestCase.class);
		//$JUnit-END$
		return suite;
	}

}
