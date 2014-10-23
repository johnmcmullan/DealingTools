/* ----------------------------------------------------------------------
   CommandLineException - command line server exceptions for CommandInterpreter

   Original Author: John McMullan (2004)

   $Author: john $
   $Date: 2004/06/03 10:35:23 $
   $Source: /usr/export/cvsroot/util/java/main/dealingtools/util/CommandLineException.java,v $
   $Revision: 1.1 $

   ---------------------------------------------------------------------- */

package dealingtools.util;

import java.io.*;

public class CommandLineException extends Exception {
    public CommandLineException(String message) {
	super(message);
    }
}

/*
  $Log: CommandLineException.java,v $
  Revision 1.1  2004/06/03 10:35:23  john
  Initial checkin

*/
