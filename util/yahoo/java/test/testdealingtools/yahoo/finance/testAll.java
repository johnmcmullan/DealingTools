/* ----------------------------------------------------------------------
   testAll - test suite for deapi

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/05/10 14:06:02 $
   $Source: /usr/export/cvsroot/util/yahoo/java/test/testdealingtools/yahoo/finance/testAll.java,v $
   $Revision: 1.1.1.1 $


   ---------------------------------------------------------------------- */

package testdealingtools.yahoo.finance;

import junit.framework.*;

public class testAll extends TestCase {
    public testAll(String name) {
	super(name);
    }

    public static Test suite() {
	TestSuite suite = new TestSuite(PriceCommTest.class);
	return suite;
    }
}
