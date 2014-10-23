/* ----------------------------------------------------------------------
   PriceCommTest - test cases for testing PriceComm

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/03/24 01:19:53 $
   $Source: /usr/export/cvsroot/deapi/java/test/testdealingtools/orc/deapi/PriceCommTest.java,v $
   $Revision: 1.5 $


   ---------------------------------------------------------------------- */

package testdealingtools.orc.deapi;

import junit.framework.*;

import dealingtools.orc.deapi.*;

public class PriceCommTest extends TestCase {
    MessageQueue orc = null;
    PriceComm pcomm = null;

    public static void main(String args[]) {
        junit.textui.TestRunner.run(PriceCommTest.class);
    }

    public PriceCommTest(String name) {
	super(name);
    }

    public void setUp() {
	try {
	    orc = new MessageQueue("louis", "6980",
				   "pricecommtest", "dealingtools", true);
	    pcomm = new PriceComm(orc);
	} catch (DeapiException e) {
	    fail(e.toString());
	}

    }

    public void testPriceCommGet() {
	Price p = null;

	try {
	    p = pcomm.get(new Instrument(1).instrumentId());
	} catch (DeapiException e) {
	    fail(e.toString());
	}
	System.out.println(p);
    }

    public void testPriceCommFeed() {
	try {
	    // toggle price feed on and hand off to PriceDisplay object
	    pcomm.toggle(new Instrument(1).instrumentId(), new PriceDisplay());
	} catch (DeapiException e) {
	    fail(e.toString());
	}
    }

    public void tearDown() {
	orc.logout();
    }

    public class PriceDisplay implements MessageCallback {
	private int i = 0;
	public void onMessage(Message message) {
	    Price p = new Price(message);
	    System.out.println((Message) p);
	    i++;
	    if (i > 5) {
		try {
		    pcomm.toggle(p.instrumentId(), false);
		} catch (DeapiException e) {}
	    }
	}
	public void onError(int error, String errorDescription) {
	    fail(errorDescription);
	}
    }
}









