/* ----------------------------------------------------------------------
   InstrumentCommTest - test cases for testing InstrumentComm

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/05/10 14:06:02 $
   $Source: /usr/export/cvsroot/util/deapi/java/test/testdealingtools/orc/deapi/InstrumentCommTest.java,v $
   $Revision: 1.1.1.1 $


   ---------------------------------------------------------------------- */

package testdealingtools.orc.deapi;

import junit.framework.*;

import dealingtools.orc.deapi.*;

public class InstrumentCommTest extends TestCase {
    static private MessageQueue orc = null;
    static private InstrumentComm icomm = null;

    public static void main(String args[]) {
        junit.textui.TestRunner.run(InstrumentCommTest.class);
    }

    public InstrumentCommTest(String name) {
	super(name);
    }

    public void setUp() {
	try {
	    orc = new MessageQueue("louis", "6980",
				   "instrumentcommtest", "dealingtools", true);
	    icomm = new InstrumentComm(orc);
	} catch (DeapiException e) {
	    fail(e.toString());
	}

    }

    public void testInstrumentGetByTag() {
	Instrument i = null;

	try {
	    i = icomm.get(1);
	} catch (DeapiException e) {
	    fail(e.toString());
	}
    }

    public void testInstrumentGetByString() {
	Instrument i = null;

	try {
	    i = icomm.get("instrument_tag=1");
	} catch (DeapiException e) {
	    fail(e.toString());
	}
    }
    public void testInstrumentGetByInstrumentId() {
	Instrument i = null, id;

	try {
	    id = new Instrument();
	    id.underlying("THUS");
	    id.kind("Spot");
	    i = icomm.get(id.instrumentId());
	} catch (DeapiException e) {
	    fail(e.toString());
	}
    }

    public void testDownload() {
	Message reply = null;

	try {
	    reply = icomm.download("market = RMP | download_mode = full");
	    reply.toXML("instrumentList");
	    reply = icomm.select("market=RMP");
	    assertTrue(!reply.isEmpty());
	    reply = icomm.select(new Message("market=Eurex V"));
	    assertTrue(reply.isEmpty());
	} catch (DeapiException e) {
	    fail(e.toString());
	}	
    }

    public void testTreeSelect() {
	Message reply = null;

	try {
	    icomm.download("market = RMP | download_mode = full");
	    reply = icomm.selectAsTree("underlying=MER");
	    assertTrue(!reply.isEmpty());
	    System.out.println(reply.toString());
	    reply = icomm.selectAsTree(new Message("market=Eurex V"));
	    assertTrue(reply.isEmpty());
	} catch (DeapiException e) {
	    fail(e.toString());
	}	
    }

    public void testCreate() {
	Instrument i = null;
	Message reply = null;
	String underlying;

	try {
	    i = icomm.get(1);
	} catch (DeapiException e) {
	    fail(e.toString());
	}
	try {
	    underlying = i.underlying();
	    i.resetId();
	    i.underlying(underlying + "NEW");
	} catch (DeapiException e) {
	    fail(e.toString());
	}
	try {
	    icomm.create(i);
	    assertTrue(i.tag() != 1);
	} catch (DeapiException e) {
	    fail(e.toString());
	}
	try {
	    i = icomm.get(i.tag());
	} catch (DeapiException e) {
	    fail(e.toString());
	}
	try {
	    icomm.delete(i.tag());
	} catch (DeapiException e) {
	    fail(e.toString());
	}
    }

    public void tearDown() {
	orc.logout();
    }
}







