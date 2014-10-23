/* ----------------------------------------------------------------------
   MessageQueueTest - test cases for testing message queue

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/03/24 01:19:53 $
   $Source: /usr/export/cvsroot/deapi/java/test/testdealingtools/orc/deapi/MessageQueueTest.java,v $
   $Revision: 1.3 $



   ---------------------------------------------------------------------- */

package testdealingtools.orc.deapi;

import junit.framework.*;

import dealingtools.orc.deapi.*;

public class MessageQueueTest extends TestCase {
    public static void main(String args[]) {
        junit.textui.TestRunner.run(ConnectionTest.class);
    }

    public MessageQueueTest(String name) {
	super(name);
    }

    public void testConstructors() {
	String hostname = "louis";
	String service = "6980";
	String loginid = "lee";
	String password = "lee";
	MessageQueue orc = null;

	try {
	    orc = new MessageQueue(hostname, service, loginid, password);
	} catch (DeapiException e) {
	    fail(e.toString());
	}
	if (orc != null) {
	    orc.logout();
	}
    }
}
