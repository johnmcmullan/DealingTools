/* ----------------------------------------------------------------------
   InstrumentTest - test cases for testing Instrument

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/05/10 14:06:02 $
   $Source: /usr/export/cvsroot/util/deapi/java/test/testdealingtools/orc/deapi/InstrumentTest.java,v $
   $Revision: 1.1.1.1 $


   ---------------------------------------------------------------------- */

package testdealingtools.orc.deapi;

import java.io.*;

import junit.framework.*;

import dealingtools.orc.deapi.*;

public class InstrumentTest extends TestCase {
    public static void main(String args[]) {
        junit.textui.TestRunner.run(InstrumentTest.class);
    }

    public InstrumentTest(String name) {
	super(name);
    }

    public void testInstrumentId() {
	String msg = "instrument_id={instrument_tag=12|underlying=DBK|market=Eurex V|feedcode=DBKOPT|kind=Call|expirydate=2004-12-12|expirytype=American|multiplier=10|strikeprice=40|assettype=Equities|currency=EUR|description=DBK 40 Dec 04 Call}";
	Instrument inst = null;

	try {
	    inst = new Instrument(msg);
	    System.out.println(inst);
	    assertTrue(inst.tag() == 12);
	    assertTrue(inst.underlying().equals("DBK"));
	    assertTrue(inst.market().equals("Eurex V"));
	    assertTrue(inst.feedcode().equals("DBKOPT"));
	    assertTrue(inst.kind().equals("Call"));
	    assertTrue(inst.expirydate().equals("2004-12-12"));
	    assertTrue(inst.expirytype().equals("American"));
	    assertTrue(inst.multiplier().equals("10"));
	    assertTrue(inst.strikeprice().equals("40"));
	    assertTrue(inst.assettype().equals("Equities"));
	    assertTrue(inst.currency().equals("EUR"));
	    assertTrue(inst.description().equals("DBK 40 Dec 04 Call"));
	} catch (DeapiException e) {
	    fail(e.toString());
	}
    }

    public void testSetInstrumentId() {
	String msg = "{instrument_id={instrument_tag=12|underlying=DBK|market=Eurex V|feedcode=DBKOPT|kind=Call|expirydate=2004-12-12|expirytype=American|multiplier=10|strikeprice=40|assettype=Equities|currency=EUR|description=DBK 40 Dec 04 Call}|parameters={description=DBK 40 Dec 04 Call}|instrument_attributes={underlying=DBK|kind=Call|currency=EUR|strikeprice=40|expirydate=2004-12-12}}";
	Instrument inst = null;

	try {
	    inst = new Instrument(msg);
	    System.out.println(inst);
	    assertTrue(inst.tag() == 12);
	    assertTrue(inst.underlying().equals("DBK"));
	    assertTrue(inst.market().equals("Eurex V"));
	    assertTrue(inst.feedcode().equals("DBKOPT"));
	    assertTrue(inst.kind().equals("Call"));
	    assertTrue(inst.expirydate().equals("2004-12-12"));
	    assertTrue(inst.expirytype().equals("American"));
	    assertTrue(inst.multiplier().equals("10"));
	    assertTrue(inst.strikeprice().equals("40"));
	    assertTrue(inst.assettype().equals("Equities"));
	    assertTrue(inst.currency().equals("EUR"));
	    assertTrue(inst.description().equals("DBK 40 Dec 04 Call"));
	} catch (DeapiException e) {
	    fail(e.toString());
	}

	try {
	    inst.feedcode("VODOPT");
	    assertTrue(inst.feedcode().equals("VODOPT"));
	    inst.market("Liffe Futures");
	    assertTrue(inst.market().equals("Liffe Futures"));
	    inst.underlying("VOD");
	    assertTrue(inst.underlying().equals("VOD"));
	    inst.kind("Put");
	    assertTrue(inst.kind().equals("Put"));
	    inst.expirydate("2004-11-11");
	    assertTrue(inst.expirydate().equals("2004-11-11"));
	    inst.strikeprice("12");
	    assertTrue(inst.strikeprice().equals("12"));
	    System.out.println(inst);
	    // assettype, multiplier, expirytype and description
	    // cannot be set in instrument_id but they will be set
	    // in parameters (description) and instrument_attributes (others)
	} catch (DeapiException e2) {
	    fail(e2.toString());
	}
    }
}
