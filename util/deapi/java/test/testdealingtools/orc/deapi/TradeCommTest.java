/* ----------------------------------------------------------------------
   TradeCommTest - test cases for testing TradeComm

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/05/10 14:06:02 $
   $Source: /usr/export/cvsroot/util/deapi/java/test/testdealingtools/orc/deapi/TradeCommTest.java,v $
   $Revision: 1.1.1.1 $


   ---------------------------------------------------------------------- */

package testdealingtools.orc.deapi;

import junit.framework.*;

import dealingtools.orc.deapi.*;

public class TradeCommTest extends TestCase {
    static private MessageQueue orc = null;
    static private TradeComm tcomm = null;

    public static void main(String args[]) {
        junit.textui.TestRunner.run(TradeCommTest.class);
    }

    public TradeCommTest(String name) {
	super(name);
    }

    public void setUp() {
	try {
	    orc = new MessageQueue("louis", "6980",
				   "tradecommtest", "dealingtools", true);
	    tcomm = new TradeComm(orc);
	} catch (DeapiException e) {
	    fail(e.toString());
	}
    }

    public void testTradeDownload() {
	Message trades = null;

	try {
	    trades = tcomm.download();
	} catch (DeapiException e) {
	    fail(e.toString());
	}
    }

    public void tearDown() {
	orc.logout();
    }
}







