/* ----------------------------------------------------------------------
   PriceCommTest - test case for testing PriceComm

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/05/10 14:06:02 $
   $Source: /usr/export/cvsroot/util/yahoo/java/test/testdealingtools/yahoo/finance/PriceCommTest.java,v $
   $Revision: 1.1.1.1 $


   ---------------------------------------------------------------------- */

package testdealingtools.yahoo.finance;

import junit.framework.*;

import dealingtools.yahoo.finance.*;

public class PriceCommTest extends TestCase {
    public static void main(String args[]) {
        junit.textui.TestRunner.run(PriceCommTest.class);
    }

    public PriceCommTest(String name) {
	super(name);
    }

    public void setUp() {
    }

    public void testGet() {
	PriceComm pcomm = new PriceComm();
	pcomm.subscribe("THUS.L");
	pcomm.subscribe("VOD.L");
	pcomm.subscribe("BARC.L");

	System.out.println("Getting prices for IBM and Merrill");
	System.out.println(pcomm);
	pcomm.getPrices();
    }

    public void tearDown() {
    }
}







