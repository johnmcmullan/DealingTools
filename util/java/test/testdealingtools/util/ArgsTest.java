/* ----------------------------------------------------------------------
   ArgsTest - test cases for testing Args

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/06/03 10:35:26 $
   $Source: /usr/export/cvsroot/util/java/test/testdealingtools/util/ArgsTest.java,v $
   $Revision: 1.1 $


   ---------------------------------------------------------------------- */

package testdealingtools.util;

import java.io.*;

import junit.framework.*;

import dealingtools.util.*;

public class ArgsTest extends TestCase {
    public static String argv[];
    public static void main(String args[]) {
	argv = args;
        junit.textui.TestRunner.run(ArgsTest.class);
    }

    public ArgsTest(String name) {
	super(name);
    }

    public void testArgs() {
	Args args = new Args("testApp", argv);

	assertTrue(args.orcServer().equals("localhost"));
	assertTrue(args.orcPort().equals("6980"));
	assertTrue(args.orcLoginId().equals("NO_LOGIN_ID"));
	assertTrue(args.orcPassword().equals(""));
	assertTrue(args.appName().equals("testApp"));
	assertTrue(args.logname().equals("testApp"));
    }
}
