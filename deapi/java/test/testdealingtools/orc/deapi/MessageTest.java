/* ----------------------------------------------------------------------
   MessageTest - test cases for testing message

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/03/18 01:32:05 $
   $Source: /usr/export/cvsroot/deapi/java/test/testdealingtools/orc/deapi/MessageTest.java,v $
   $Revision: 1.4 $



   ---------------------------------------------------------------------- */

package testdealingtools.orc.deapi;

import java.io.*;

import junit.framework.*;

import dealingtools.orc.deapi.*;

public class MessageTest extends TestCase {
    public static void main(String args[]) {
        junit.textui.TestRunner.run(MessageTest.class);
    }

    public MessageTest(String name) {
	super(name);
    }

    public void testParse() {
	String msg = "{A=B|C=D|E={F=G|H=I|J={K=L|M=N}}|O=P}";
	Message simple = null;
	Message inner = null;
	Message innerinner = null;
	try {
	    simple = new Message(msg);
	} catch (DeapiException e) {
	    fail(e.toString());
	}
	assertTrue(simple.get("A").equals("B"));
	assertTrue(simple.get("C").equals("D"));
	try {
	    inner = simple.getMessage("E");
	} catch (DeapiException d2) {
	    fail(d2.toString());
	}
	assertTrue(inner.get("F").equals("G"));
	assertTrue(inner.get("H").equals("I"));
	try {
	    innerinner = inner.getMessage("J");
	} catch (DeapiException d3) {
	    fail(d3.toString());
	}
	assertTrue(innerinner.get("K").equals("L"));
	assertTrue(innerinner.get("M").equals("N"));
	assertTrue(simple.get("O").equals("P"));

	msg = "AAA=BBB";
	simple = null;
	try {
	    simple = new Message(msg);
	} catch (DeapiException e) {
	    fail(e.toString());
	}
	assertTrue(simple.get("AAA").equals("BBB"));
    }

    public void testXML() {
	String msg = "{A=B|C=D|E={F=G|H=I|J={K=L|M=N}}|O=P}";
	Message simple = null;
	Message inner, innerinner;
	try {
	    simple = new Message(msg);
	    System.out.println(simple.toXML("simple"));
	} catch (DeapiException e) {
	    fail(e.toString());
	}
    }
}







