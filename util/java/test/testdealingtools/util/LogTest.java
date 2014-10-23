/* ----------------------------------------------------------------------
   LogTest - test cases for testing Log

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/06/03 10:35:26 $
   $Source: /usr/export/cvsroot/util/java/test/testdealingtools/util/LogTest.java,v $
   $Revision: 1.1 $


   ---------------------------------------------------------------------- */

package testdealingtools.util;

import java.io.*;

import junit.framework.*;

import dealingtools.util.*;

public class LogTest extends TestCase {
    public static void main(String args[]) {
        junit.textui.TestRunner.run(ArgsTest.class);
    }

    public LogTest(String name) {
	super(name);
    }

    public void testWrite() {
	Log.start("LogTest", "logtest");
	Log.write("modulename", "This is a log entry");
    }
}
