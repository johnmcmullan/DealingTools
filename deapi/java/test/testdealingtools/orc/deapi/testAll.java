/* ----------------------------------------------------------------------
   testAll - test suite for deapi

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/03/14 20:06:21 $
   $Source: /usr/export/cvsroot/deapi/java/test/testdealingtools/orc/deapi/testAll.java,v $
   $Revision: 1.6 $


   ---------------------------------------------------------------------- */

package testdealingtools.orc.deapi;

import junit.framework.*;

public class testAll extends TestCase {
    public testAll(String name) {
	super(name);
    }

    public static Test suite() {
	TestSuite suite = new TestSuite(ConnectionTest.class);
	suite.addTest(new TestSuite(MessageTest.class));
	suite.addTest(new TestSuite(MessageQueueTest.class));
	suite.addTest(new TestSuite(InstrumentTest.class));
	suite.addTest(new TestSuite(InstrumentCommTest.class));
	suite.addTest(new TestSuite(PriceCommTest.class));
	suite.addTest(new TestSuite(TradeCommTest.class));
	return suite;
    }
}
