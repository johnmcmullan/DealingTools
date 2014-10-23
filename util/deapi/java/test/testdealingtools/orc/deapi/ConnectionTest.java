/* ----------------------------------------------------------------------
   ConnectionTest - test cases for testing connection

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/05/10 14:06:02 $
   $Source: /usr/export/cvsroot/util/deapi/java/test/testdealingtools/orc/deapi/ConnectionTest.java,v $
   $Revision: 1.1.1.1 $



   ---------------------------------------------------------------------- */

package testdealingtools.orc.deapi;

import junit.framework.*;

import java.io.*;
import java.net.*;

import dealingtools.orc.deapi.*;

public class ConnectionTest extends TestCase {
    public static void main(String args[]) {
        junit.textui.TestRunner.run(ConnectionTest.class);
    }

    public ConnectionTest(String name) {
	super(name);
    }

    public void testConstructors() {
	Connection asInt, asString;
	String hostname = "louis";

	try {
	    asInt = new Connection(hostname, 6980);
	} catch (UnknownHostException uhe) {
	    fail(hostname + " is not a valid hostname");
	} catch (IOException ioe) {
	    fail("asInt: " + ioe);
	}

	try {
	    asInt = new Connection(hostname, "6980");
	} catch (UnknownHostException uhe) {
	    fail(hostname + " is not a valid hostname");
	} catch (IOException ioe) {
	    fail("asString: " + ioe);
	}

    }
}
