/* ----------------------------------------------------------------------
   testAll - test suite for util

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/06/03 10:35:26 $
   $Source: /usr/export/cvsroot/util/java/test/testdealingtools/util/testAll.java,v $
   $Revision: 1.1 $


   ---------------------------------------------------------------------- */

package testdealingtools.util;

import junit.framework.*;

public class testAll extends TestCase {
    public testAll(String name) {
	super(name);
    }

    public static Test suite() {
	TestSuite suite = new TestSuite(ArgsTest.class);
	suite.addTest(new TestSuite(LogTest.class));
	return suite;
    }
}
